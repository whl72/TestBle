package com.example.sfd.test_1;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by SFD on 2017/12/19.
 */

public class BlueToothReceiver extends BroadcastReceiver {

    String pin = "1234";

    //广播接收器，当蓝牙设备被发现时，回调函数onReceive()会被执行
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("action1=:", action);
        BluetoothDevice bluetoothDevice = null;
        bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        Log.d("BlueToothReceiver", "step1");
        if(BluetoothDevice.ACTION_FOUND.equals(action)){//发现设备
            Log.e("发现设备：", "["+bluetoothDevice.getName()+"]"+":"+
            bluetoothDevice.getAddress());
        }else {

        }
    }
}
