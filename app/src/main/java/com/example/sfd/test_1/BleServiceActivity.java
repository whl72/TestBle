package com.example.sfd.test_1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BleServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_service);

        Intent getIntent = getIntent();
        String BleDeviceAddr = getIntent.getStringExtra("BleDeviceAddress");
        MainActivity mBle = new MainActivity();
        mBle.bleConnect(BleDeviceAddr);
    }

}
