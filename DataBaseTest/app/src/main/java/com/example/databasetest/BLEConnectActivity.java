package com.example.databasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.databasetest.ui.bleconnect.BleconnectFragment;
import com.example.databasetest.ui.bleconnect.DatabaseUIFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import DatabaseVersion.DatabaseVersionManager;
import DatabaseVersion.MyDatabaseHelper;
import MPAndroidChart.ChartLineManager;

public class BLEConnectActivity extends AppCompatActivity {
    private FrameLayout contentLayout;//容器
    private BottomNavigationView mainBottomView;//底部导航
    private BleconnectFragment mBleconnectFragment;      //首页
    private DatabaseUIFragment mDatabaseUIFragment;      //数据库页面
    private List<Fragment> fragmentList = new ArrayList<>();   //Fragment列表

    private final static String TAG = BLEConnectActivity.class.getSimpleName();
    //获取蓝牙设备名称数据
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //标题栏
    private TextView mTxtTitle;                           //登陆界面标题
    private Button   mBtnBack,mBtnEdit;                   //标题栏按钮
    //MPAndroidChart库需要的变量或类
    private LineChart mLineChart;
    private ChartLineManager mChartLineManager;
    private List<Integer> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合
    private boolean STOP  = false;                   //曲线暂停标志位
    //数据库
    private SQLiteDatabase db;
    private DatabaseVersionManager mDatabaseVersionManager = new DatabaseVersionManager();
    private MyDatabaseHelper dbHelper;
    private Date date = new java.util.Date();  //java的日期和时间类
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd"); //格式
    private String TableName;
    //蓝牙
    private BluetoothGattCharacteristic PLXCharacteristic;
    private boolean mConnected = false;
    private BLEService mBLEService;   //蓝牙服务
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();      //蓝牙特征集合
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String mDeviceName;
    private String mDeviceAddress;
    private String mUserName;
    //数据显示
    private TextView mTxtSPO2,mTxtPR,mTxtData;
    // Code to manage Service lifecycle.
    //活动与服务绑定与解绑时调用
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        //活动与服务绑定时调用
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BLEService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBLEService.connect(mDeviceAddress);
        }
        @Override
        //活动与服务解除绑定时调用
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };
    //广播接收器
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver;
    {
        mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    updateConnectionState(R.string.connected);         //更新蓝牙连接状态
                    invalidateOptionsMenu();
                } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    updateConnectionState(R.string.disconnected);     //更新蓝牙连接状态
                    invalidateOptionsMenu();
                    clearData();                                      //清楚数据显示
                } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(mBLEService.getSupportedGattServices());//获取到了所有服务
                } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                    displayData(intent.getStringExtra(BLEService.EXTRA_DATA));  //显示接收到的数据
                    String Spo2_string = intent.getStringExtra(BLEService.EXTRA_SPO2);
                    String PR_string = intent.getStringExtra(BLEService.EXTRA_PR);
                    int Spo2_int = Integer.valueOf(Spo2_string).intValue();
                    int PR_int   = Integer.valueOf(PR_string).intValue();
                    mTxtSPO2.setText(Spo2_string);
                    mTxtPR.setText(PR_string);
                    if(Spo2_int>0 && PR_int>0 && STOP ==false){//不暂停
                        dbHelper.newData(db,TableName,Spo2_int,PR_int);
                    }
                    if(!STOP) {
                        list.add(Spo2_int);
                        list.add(PR_int);
                        mChartLineManager.addEntry(list);
                        list.clear();
                    }
                    Log.e(TAG, "onReceive: time");
                }
            }
        };
    }
    private void clearData() {
        mTxtData.setText(R.string.no_data);
    }
    //底部导航
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (mBleconnectFragment == null) {
                        mBleconnectFragment = new BleconnectFragment();
                    }
                    addFragment(mBleconnectFragment);
                    showFragment(mBleconnectFragment);
                    return true;
                case R.id.navigation_dashboard:
                    if (mDatabaseUIFragment == null) {
                        mDatabaseUIFragment = new DatabaseUIFragment();
                    }
                    addFragment(mDatabaseUIFragment);
                    showFragment(mDatabaseUIFragment);
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bleconnect_activity);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        mBleconnectFragment = new BleconnectFragment();
        addFragment(mBleconnectFragment);
        showFragment(mBleconnectFragment);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, BleconnectFragment.newInstance())
//                    .commitNow();
//        }
        //获取连接蓝牙的名称和地址
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mUserName      = intent.getStringExtra(MainActivity.EXTRAS_USER_NAME);
        //数据库
        dbHelper =new MyDatabaseHelper(BLEConnectActivity.this,"PULSE.db",null, mDatabaseVersionManager.VERSION);
        db = dbHelper.getWritableDatabase();
        java.util.Date date=new java.util.Date();
        String str=sdf.format(date);   //获取当前时间
        TableName=mUserName+str;      //应该建立的数据库名称
        dbHelper.newDataTable(db,mUserName,false);
        //标题
        mTxtTitle = findViewById(R.id.TitleText);
        mTxtTitle.setText("连接中");
        //底部导航
        mainBottomView = findViewById(R.id.bottom_view);
        contentLayout = (FrameLayout) findViewById(R.id.container);   //包含了Fragment
        mainBottomView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //绑定服务
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLEService != null) {
            final boolean result = mBLEService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        mTxtSPO2   = findViewById(R.id.txt_spo2);
        mTxtPR   = findViewById(R.id.txt_pr);
        mTxtData = findViewById(R.id.receive_data);
        mLineChart = (LineChart)findViewById(R.id.Chart);
        names.add("SPO2");
        names.add("PR");
        //折线颜色
        colour.add(Color.RED);
        colour.add(Color.BLUE);
        mChartLineManager = new ChartLineManager(mLineChart, names, colour);
        mChartLineManager.setYAxis(150, -10, 10);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBLEService = null;
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtTitle.setText(resourceId);
            }
        });
    }
    private void displayData(String data) {
        if (data != null) {
            mTxtData.setText(data);
        }
    }
    private void displayGattServices(List<BluetoothGattService> gattServices){
        if(mBLEService.getSupportedGattService(BLEService.UUID_PLX_CONTINUOUS_MEASUREMENT_SERVICE)!=null){
            PLXCharacteristic = mBLEService.getSupportedGattService
                    (BLEService.UUID_PLX_CONTINUOUS_MEASUREMENT_SERVICE).getCharacteristic
                    (UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb"));
            final int charaProp = PLXCharacteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {//可读
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBLEService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                Log.d(TAG, "onChildClick: "+PLXCharacteristic.getUuid());
                mBLEService.readCharacteristic(PLXCharacteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {//可通知
                mNotifyCharacteristic = PLXCharacteristic;
                mBLEService.setCharacteristicNotification(
                        PLXCharacteristic, true);
            }
        }


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /*添加fragment*/
    private void addFragment(Fragment fragment) {
        /*判断该fragment是否已经被添加过  如果没有被添加  则添加*/
        if (!fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            /*添加到 fragmentList*/
            fragmentList.add(fragment);
        }
    }
    /*显示fragment*/
    private void showFragment(Fragment fragment) {
        for (Fragment frag : fragmentList) {
            if (frag != fragment) {
                /*先隐藏其他fragment*/
                getSupportFragmentManager().beginTransaction().hide(frag).commit();
            }
        }
        getSupportFragmentManager().beginTransaction().show(fragment).commit();
    }
}
