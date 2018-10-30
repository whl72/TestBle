package com.example.sfd.test_1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BleProcessActivity extends AppCompatActivity {

    private String TAG = "BLE";

    private static final long SCAN_PERIOD = 10000; //10 seconds
//    private Handler mHandler;
    private boolean mscaning;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    private ListView listView = (ListView) this.findViewById(R.id.list_1);
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_process);

        Log.d(TAG, "BLE_step1");
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable){
        if(enable){
            //设置延时10s后关闭ble扫描
            Handler mHandler = new Handler();
            Runnable runnable = new Runnable(){
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.d(TAG, "step2");
                }
            };
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            mscaning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(TAG, "step1");
        }else {
            mscaning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d(TAG, "step9");
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice,
                                     int i, byte[] bytes) {
                    addDevice(bluetoothDevice, i);
                }
            };

    private void addDevice(BluetoothDevice device, int rssi){
        boolean deviceFound = false;

        Log.e("发现设备", "["+device.getName()+"]"+"    MAC:"+
                device.getAddress());

        //准备数据
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put(device.getName(), device.getAddress());
//        list.add(map);
//
//        String[] from = {"蓝牙名称", "蓝牙地址"};
//        int[] to = {R.id.ble_name, R.id.ble_mac};
//        SimpleAdapter simpleAdatper = new SimpleAdapter(BleProcessActivity.this,
//                list, R.layout.message_detail, from, to);
//
//        listView.setAdapter(simpleAdatper);

//        deviceList.add(device);
//        for (BluetoothDevice listDev : deviceList) {
//            if (listDev.getAddress().equals(device.getAddress())) {
//                deviceFound = true;
//                break;
//            }
//        }
//
//
////        devRssiValues.put(device.getAddress(), rssi);
//        if (!deviceFound) {
//            deviceList.add(device);
//            Log.d(TAG, "step4");
//        }
    }
}
