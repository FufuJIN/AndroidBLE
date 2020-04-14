package com.example.databasetest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BLEService extends Service {
    private final static String TAG = BLEService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    //蓝牙连接状态
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_SPO2 =
            "com.example.bluetooth.le.EXTRA_SPO2";
    public final static String EXTRA_PR =
            "com.example.bluetooth.le.EXTRA_PR";
    //写服务的UUID，血氧仪没有
    public final static UUID UUID_WRITE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    //血氧仪的UUID，连续测量
    public final static UUID UUID_PLX_CONTINUOUS_MEASUREMENT_SERVICE  =
            UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_PLX_CONTINUOUS_MEASUREMENT  =
            UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_PLX_FEATURE  =
            UUID.fromString("00002a60-0000-1000-8000-00805f9b34fb");

    public BLEService() {
    }
    @Override
    public void onCreate(){//服务首次创建运行
        super.onCreate();
        Log.d(TAG, "onCreate: executed");
    }
    @Override
    public int onStartCommand(Intent intent,int flag,int StartId){//每次启动服务运行
        Log.d(TAG, "onStartCommand: executed");
        return super.onStartCommand(intent,flag,StartId);
    }
    @Override
    public void onDestroy(){//每次停止服务运行
        super.onDestroy();
        Log.d(TAG, "onDestroy: executed");
    }
    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    private final IBinder mBinder = new LocalBinder();
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    //蓝牙回调
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        //连接状态改变时回调
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {//新状态为连接上GATT
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);                 //更新广播：连接上蓝牙
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                //扫描服务
                mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//连接断开
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                Log.i(TAG, "连接已经断开");
                broadcastUpdate(intentAction);            //更新广播：蓝牙连接已断开
            }
        }
        @Override
        //发现服务的回调
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {//发现服务成功
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);    //更新广播：发现蓝牙服务成功
                Log.i(TAG, "发现服务成功");
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);//打印失败信息
            }
        }
        @Override
        //读操作的回调
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.e(TAG, "CharacteristicRead: " +characteristic.getValue());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);      //更新广播:读取到数据
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);//打印失败信息
            }
        }
        @Override
        //写操作的回调
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "write value: " +characteristic.getValue());   //打印信息，看写入数据
        }
        @Override
        //数据返回的回调,通知回调
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "CharacteristicChanged: " +characteristic.getValue());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);   //更新广播：数据接收成功
        }
    };
    //广播更新
    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    //带数据的广播
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);//将action放入Intent作为广播信息
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        int permission=characteristic.getPermissions();
        Log.e(TAG, String.format("permission: %d", permission));
        if(UUID_PLX_CONTINUOUS_MEASUREMENT.equals(characteristic.getUuid())){
            int flag_PLX = characteristic.getProperties();   //属性获取
            int format_PLX = -1;                             //格式获取
            Log.d(TAG, "UUID PLX");
            if((flag_PLX & 0x01)!=0){                       //16位
                format_PLX = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "PLX format UINT16.");
            }
            else {                                           //8位
                format_PLX = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "PLX format UINT8.");
            }
            final int SPO2 = characteristic.getIntValue(format_PLX, 1);
            final int PR   = characteristic.getIntValue(format_PLX, 3);
            Log.d(TAG, String.format("Received SPO2: %d", SPO2));
            Log.d(TAG, String.format("Received PR  : %d", PR  ));
            intent.putExtra(EXTRA_SPO2, String.valueOf(SPO2));
            intent.putExtra(EXTRA_PR, String.valueOf(PR  ));
        }
        else if(UUID_PLX_FEATURE.equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                    Log.d(TAG, String.format("Received Data: %02X", byteChar));
                }
                //Log.e(TAG, String.format("data length: %d", data));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                    Log.d(TAG, String.format("Received Data: %02X", byteChar));
                }
                //Log.e(TAG, String.format("data length: %d", data));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }
    //蓝牙初始化
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");//不能获得蓝牙适配器
            return false;
        }
        return true;
    }
    //蓝牙连接
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }
    //**
    //     * Disconnects an existing connection or cancel a pending connection. The disconnection result
    //     * is reported asynchronously through the
    //     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
    //     * callback.
    //     */
    public void disconnect() {//取消蓝牙连接
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    //蓝牙读取数据
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    //蓝牙写入数据
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,String message) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
    //  BluetoothGattCharacteristic mGattCharacteristic =
    //  mBluetoothGatt.getCharacteristic(writeUuid);
        characteristic.setValue(message);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    //蓝牙写入通知描述符
    /**
     * Enables or disables notification on a give characteristic
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //血氧仪的通知，每次发生改变时发出通知
        if (UUID_PLX_CONTINUOUS_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));//描述属性
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    //返回所有的蓝牙服务
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
    //返回某个UUID对应的蓝牙服务
    public BluetoothGattService getSupportedGattService(UUID uuid) {
        if (mBluetoothGatt == null) return null;
        return (BluetoothGattService) mBluetoothGatt.getService(uuid);
    }


}
