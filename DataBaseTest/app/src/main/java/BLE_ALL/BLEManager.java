package BLE_ALL;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.databasetest.MainActivity;
import com.example.databasetest.R;

import java.util.ArrayList;
import java.util.List;

public class BLEManager extends BaseAdapter{
    // Adapter for holding devices found through scanning.
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;
    private Context mContext;

    public BLEManager(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mInflator =  LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.ble_device_items, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.DeviceAddress);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.DeviceName);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


}
