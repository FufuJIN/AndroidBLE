package com.example.databasetest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import BLE_ALL.BLEManager;
import DatabaseVersion.DatabaseVersionManager;
import DatabaseVersion.MyDatabaseHelper;

import Netty.coder.Client;
import Netty.coder.MessageResolver.BackMessageResolver;
import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import Netty.coder.data.SessionIdManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Client client =new Client("106.54.16.31",8585);
    //创建sessionId
    public static SessionIdManager sessionIdManager= BackMessageResolver.sessionIdManager;
    //创建自动增加计数器
    private static final AtomicLong counter = new AtomicLong(1);


    private Handler mHandler;
    private int     mStatus=1;
    //蓝牙开启
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    //蓝牙连接
    private static final long SCAN_PERIOD = 100000;
    private BLEManager mBLEDeviceAdapter;
    private ListView mList_Device;

    private boolean mScanning=false;
    private boolean mOpen=false;
    //登陆界面传递的字符串
    public final static String EXTRAS_LOGIN_STATUS = "LOGIN_STATUS";
    public final static String EXTRAS_USER_NAME    = "USER_NAME";
    //数据库版本管理，登入用户管理
    private DatabaseVersionManager mDatabaseVersionManager= new DatabaseVersionManager();
    //数据库使用
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    //登陆状态与登入用户名
    private boolean LoginStatus=false;
    public static String  UserNameLogin;
    private TextView mTxtUserName;

    private FloatingActionButton fab;
    //fab按钮颜色设置
    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
        int[] colors = new int[] { pressed, focused, normal, focused, unable, normal };
        int[][] states = new int[6][];
        states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
        states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
        states[2] = new int[] { android.R.attr.state_enabled };
        states[3] = new int[] { android.R.attr.state_focused };
        states[4] = new int[] { android.R.attr.state_window_focused };
        states[5] = new int[] {};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

    //导航栏
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mStatus=1;
                    setSelectStatus(mStatus);
                    return true;
                case R.id.navigation_dashboard:
                    mStatus=2;
                    setSelectStatus(mStatus);
                    return true;
                case R.id.navigation_notifications:
                    mStatus=3;
                    setSelectStatus(mStatus);
                    return true;
            }
            return false;
        }
    };

    private void setSelectStatus(int index){
        switch (index){
            case 1:
                mList_Device.setVisibility(View.VISIBLE);
                break;
            case 2:
                mList_Device.setVisibility(View.GONE);
                break;
            case 3:
                mList_Device.setVisibility(View.GONE);
                default:break;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        LoginStatus   = intent.getBooleanExtra(EXTRAS_LOGIN_STATUS,false);
        UserNameLogin = intent.getStringExtra(EXTRAS_USER_NAME);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        //数据库
        dbHelper =new MyDatabaseHelper(MainActivity.this,"PULSE.db",null,mDatabaseVersionManager.VERSION);
        db = dbHelper.getWritableDatabase();
        //蓝牙显示列表管理
        mHandler = new Handler();
        mList_Device = findViewById(R.id.device_name);
        mBLEDeviceAdapter = new BLEManager(MainActivity.this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mOpen){
                    mBluetoothAdapter.enable();//打开蓝牙
                    Snackbar.make(view, "蓝牙开启中", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    fab.setBackgroundTintList(createColorStateList(Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE)); //nomal,pressed,focused,unable
                    openBle();
                }
                else if(mOpen){
                    mBluetoothAdapter.disable();//关闭蓝牙
                    Snackbar.make(view, "蓝牙关闭中", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    fab.setBackgroundTintList(createColorStateList(Color.RED, Color.RED, Color.RED, Color.RED)); //nomal,pressed,focused,unable
                    mOpen =false;
                }

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        BottomNavigationView bottomView = findViewById(R.id.bottom_menu);
        bottomView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerLayout=navigationView.getHeaderView(0);
        TextView mTxtUserName=headerLayout.findViewById(R.id.username);
        mTxtUserName.setText(UserNameLogin);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mList_Device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = mBLEDeviceAdapter.getDevice(i);
                if (device == null) return;
                final Intent intent = new Intent(MainActivity.this, BLEConnectActivity.class);
                intent.putExtra(BLEConnectActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(BLEConnectActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                intent.putExtra(MainActivity.EXTRAS_USER_NAME, UserNameLogin);
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                startActivity(intent);
            }
        });
    }

    @Override
    //onResume():是当该activity与用户能进行交互时被执行,
    protected void onResume() {
        super.onResume();
        initBluetooth();
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mBLEDeviceAdapter.clear();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if(!mScanning) {
            Log.d("MainActivity", "mscaning");
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }else{
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(dbHelper.isExistUserName(db,"jinhu")){
                Toast.makeText(MainActivity.this,  "该用户已存在", Toast.LENGTH_SHORT).show();
            }
            else {
                dbHelper.newUser(db, "jinhu", "jin");
            }
            return true;
        }else if(id == R.id.action_sendMessage){
            //发送消息
            Message message = new Message();
            message.setSessionId(sessionIdManager.getSessionId());
            message.setMessageType(MessageTypeEnum.REQUEST);
            message.setBody("this is my " + counter.getAndIncrement() + " message.");
            message.addAttachment("SPO2", "50");
            message.addAttachment("PR", "40");
            Date date = new Date();
            long time = date.getTime();
            //long型数据转为字符串的两种方法
            //1、String s = String.valueOf(long);
            //2、String s = Long.toString(long);
            message.addAttachment("TIME",Long.toString(time));
            client.sendMsg(message);

        }else if(id == R.id.action_connect){
            //连接服务器的代码
            System.out.println("连接服务器！");
            new Thread(){
                @Override
                public void run() {
                    connect();
                }
            }.start();
        }
        else if(id == R.id.action_close) {
            disconnect();
        }
        else if(id == R.id.menu_scan){
            mScanning = true;
            mBLEDeviceAdapter.clear();
            scanLeDevice(true);
           return true;
        }
        else if(id == R.id.menu_stop){
            mScanning = false;
            scanLeDevice(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    //接收由其他activity返回的数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            mOpen=false;
            fab.setBackgroundTintList(createColorStateList(Color.RED, Color.RED, Color.RED, Color.RED)); //nomal,pressed,focused,unable
            return;
        }
        mOpen=true;
        fab.setBackgroundTintList(createColorStateList(Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE)); //nomal,pressed,focused,unable
        super.onActivityResult(requestCode, resultCode, data);
    }
    //获得蓝牙适配器
    private void initBluetooth(){
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if(manager != null){
            mBluetoothAdapter = manager.getAdapter();
            if(mBluetoothAdapter != null){
                if(!mBluetoothAdapter.isEnabled()){
                    openBle();
                }
            }
        }

    }

    //启用蓝牙
    public void openBle(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
    }

    //蓝牙扫描
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // 设备搜索回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                //scanRecord广告的内容记录提供的远程设备。
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBLEDeviceAdapter.addDevice(device);
                            mBLEDeviceAdapter.notifyDataSetChanged();
                            mList_Device.setAdapter(mBLEDeviceAdapter);
                        }
                    });
                }
    };

    public void connect(){
        try {
            //client=new Client("106.54.16.31",8585);
            client.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disconnect(){
        client.disconnect();
    }


}
