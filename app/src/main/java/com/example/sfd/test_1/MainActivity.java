package com.example.sfd.test_1;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.R.id.content;
import static android.R.id.list;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int RECEIVE_DATA = 10;

    private static final String TAG = "MainActivity";
    private static final String UUID_READ = "00001a11-0000-1000-8000-00805f9b34fb";
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    private static final long SCAN_PERIOD = 5000; //10 seconds
    private boolean mscaning;
    private boolean flag;
    private static String mBluetoothDeviceAddress;
    private static String mConnectState;
    private String[] datas = {"选项1", "选项2", "选项3"};

    private Button mbutton = null;
    private Button buttonRxEn = null;
    private Button buttonPopTest = null;
    private Button buttonSend = null;
    private TextView receiveDataView = null;
    private EditText sendDataEdit =null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner mBluetoothLeScanner = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService service_test;
    BluetoothGattCharacteristic characteristic_test;
    BluetoothGattCharacteristic characteristic_rx_test;
    private static SimpleAdapter simpleAdapter;
    private static ListView listPop;
    private static PopupWindow window = null;

    static List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    static List<Map<String, Object>> listStore = new ArrayList<Map<String, Object>>();

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECEIVE_DATA:
                    dispReceiveData((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbutton = (Button) findViewById(R.id.main1_button);
        buttonRxEn = (Button) findViewById(R.id.main1_button2);
//        buttonPopTest = (Button) findViewById(R.id.main1_button3);
        buttonSend = (Button) findViewById(R.id.send_data_button);
        receiveDataView = (TextView) findViewById(R.id.receive_text);
        sendDataEdit = (EditText) findViewById(R.id.send_edit);

        //必须调用该方法，否则滚动条无法拖动
        receiveDataView.setMovementMethod(new ScrollingMovementMethod());

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bluetoothAdapter.isEnabled() || bluetoothAdapter == null){

//                    bluetoothAdapter.enable(); //隐式打开，比较流氓的行为
                    //下面这种方式可以让用户知道在申请打开蓝牙，比较合适
                    Intent enableBleIntent =
                            new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBleIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(MainActivity.this, "正在打开蓝牙，建议允许操作",
                            Toast.LENGTH_SHORT).show();
                }else {
//                    bluetoothAdapter.startDiscovery();//这种方法貌似很少用
                    Log.d(TAG, "step0");
                    list.clear();
                    listStore.clear();
                    setPopupListView();
                    scanLeDevice(true);
                }
            }
        });
        buttonRxEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableTXNotification();
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //测试发送数据给终端BLE设备
                service_test = mBluetoothGatt.
                        getService(UUID.
                            fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
                characteristic_test = service_test.
                        getCharacteristic(UUID.
                            fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));

//                characteristic_test.setValue(new byte[] {0x31, 0x32, 0x33});
                characteristic_test.setValue(sendDataEdit.getText().toString());
                characteristic_test.setWriteType
                        (BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mBluetoothGatt.writeCharacteristic(characteristic_test);
            }
        });

//        buttonPopTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setPopupListView();
////                //构建一个popupwindow的布局
////                View popupView = MainActivity.this.getLayoutInflater().
////                        inflate(R.layout.listpopupwindow, null);
////                //因为是要显示搜索到的蓝牙列表，所以设置一个列表
////                ListView listPop = (ListView) popupView.findViewById(R.id.list_popup);
////
////                Map<String, Object> map = new HashMap<String, Object>();
////                //这里的string必须和from中的对应起来
////                for(int i = 0; i<10; i++) {
////                    map.put("蓝牙名称", "我是测试的");
////                    map.put("蓝牙强度", "Rssi = -65dbm");
////                    map.put("蓝牙地址", "12.34.56.78");
////                    list.add(map);
////                }
////
////                String[] from = {"蓝牙名称","蓝牙强度",  "蓝牙地址"};
////                int[] to = {R.id.ble_name, R.id.ble_rssi, R.id.ble_mac};
////                SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this,
////                        list, R.layout.ble_item, from, to);
////
//////                listPop.setAdapter(new ArrayAdapter<String>(
//////                    MainActivity.this, android.R.layout.simple_list_item_1, datas));
//////
////                listPop.setAdapter(simpleAdapter);
////                //创建popwindow对象，指定宽度和高度
////                PopupWindow window = new PopupWindow(popupView, 500, 800);
////                window.setBackgroundDrawable(new ColorDrawable
////                        (Color.parseColor("#f0f0f0")));
////                window.setFocusable(true);
////                window.setOutsideTouchable(true);
////                window.setTouchable(true);
////                window.update();
//////                window.showAsDropDown(buttonPopTest, 0, 20);
////                window.showAtLocation(buttonPopTest, Gravity.CENTER,0,0);
//            }
//        });

//        ListView listView = (ListView) findViewById(R.id.list_1);
//
//        Map<String, Object> map = new HashMap<String, Object>();
//         //这里的string必须和from中的对应起来
//        map.put("蓝牙名称", "我是测试的");
//        map.put("蓝牙强度", "我还是测试的");
//        map.put("蓝牙地址", "12.34.56.78");
//        list.add(map);
//
////        String[] from = {"蓝牙图片", "蓝牙名称", "蓝牙地址"};
//        String[] from = {"蓝牙名称","蓝牙强度",  "蓝牙地址"};
//        int[] to = {R.id.ble_name, R.id.ble_rssi, R.id.ble_mac};
//        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this,
//                list, R.layout.ble_item, from, to);
//        listView.setAdapter(simpleAdapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView,
//                                    View view, int i, long l) {
//                ListView listView = (ListView) findViewById(R.id.list_1);
//                HashMap<String, String> map =
//                        (HashMap<String, String>)listView.getItemAtPosition(i);
//               String deviceAddress = map.get("蓝牙地址");
//
//                bleConnect(deviceAddress);
////                Intent intent = new Intent(MainActivity.this, BleServiceActivity.class);
////                intent.putExtra("BleDeviceAddress", deviceAddress);
////                startActivity(intent);
//            }
//        });
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

    private void addDevice(BluetoothDevice device, int rssi) {
        List<Map<String, Object>> listTemp = new ArrayList<Map<String, Object>>();

        Log.e("发现设备", "[" + device.getName() + "]" + "    MAC:" +
                device.getAddress());
        if(flag == false) {
            flag = true;
        }else {
//            ListView listView = (ListView) findViewById(R.id.list_1);
            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> map1 = new HashMap<String, Object>();
//            map.put("蓝牙图片", R.drawable.ble_1);
            map1.put("蓝牙名称", device.getName());
            map1.put("蓝牙地址", device.getAddress());
            listTemp.add(map1);
            mscaning = true;
            for(int i=0; i<listStore.size(); i++){
                if(listStore.get(i).equals(listTemp.get(0))) {
                    mscaning = false;
                }
            }

            if(mscaning == true){
                listStore.add(map1);
                map.put("蓝牙名称", device.getName());
                map.put("蓝牙地址", device.getAddress());
                map.put("蓝牙强度", "Rssi = "+rssi+"dbm");
                list.add(map);
//                listView.invalidateViews();
                simpleAdapter.notifyDataSetChanged();
            }
        }
    }

    public boolean bleConnect(final String address){

        if((mBluetoothDeviceAddress != null) &&
                (address.equals(mBluetoothDeviceAddress)) &&
                (mBluetoothGatt != null)){//已经连接的设备，尝试重新连接
            Log.d(TAG, "Trying to use an existing mBluetoothGtt for connection");
            if(mBluetoothGatt.connect()){
//                mConnectState =
                return true;
            }else {
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.w(TAG, "Device not found. Unable to connect!");
            return false;
        }

        //该函数才是真正去执行连接
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection...");
        mBluetoothDeviceAddress = address;

        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override  //当连接上或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(newState == BluetoothProfile.STATE_CONNECTED){
                //连接成功，就去找出该设备的服务
                mBluetoothGatt.discoverServices();
                Log.d(TAG, "Connect success ! Ready to discoverServices...");
                sendtoDisp("Connected to"+
                        mBluetoothGatt.getDevice().getName());
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){//连接失败

            }
        }

        @Override//当设备是否找到服务时，会回调该函数
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS){//找到服务
                //这里可以对服务进行解析，寻找需要的服务
                Log.d(TAG, "Services is found ! received:"+status);

                displayGattServices(getSuppetedGattServices());
            }else {
                Log.w(TAG, "onServicesDiscovered received:"+status);
            }
        }

        @Override//当读取设备时会回调该函数
        public void onCharacteristicRead(BluetoothGatt gatt,
                     BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead = : "+status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                //读取到的数据存储在characteristic中，可以通过characteristic.getValue()
                //取出，然后再进行解析操作

                Log.d(TAG, "data = : "+characteristic.getValue());
            }
        }

        @Override//当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt,
                      BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite = :"+status+"descriptor = :"+descriptor);
        }

        @Override//当设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt,
                        BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getValue() != null){
                Log.d(TAG, "value=:"+characteristic.getStringValue(0));
                sendtoDisp("RX : "+characteristic.getStringValue(0));
            }
            Log.d(TAG, "-----------onCharacteristicChanged---------");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG, "rssi=: "+rssi);
        }

        @Override//当向characteristic写数据时会回调该函数
        public void onCharacteristicWrite(BluetoothGatt gatt,
                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "-----Write Success---status:"+status);
        }
    };

    public List<BluetoothGattService> getSuppetedGattServices(){
        if(mBluetoothGatt == null){
            return null;
        }else {
            return mBluetoothGatt.getServices();
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices){
        if(gattServices == null)
            return;
        for(BluetoothGattService gattService : gattServices){//遍历出gattServices所有服务
            //将设备其服务UUID打印出来
            Log.d(TAG, "Device service UUID = :"+gattService.getUuid());
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            //遍历每个服务里所有的特征值
            for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                //将设备其特征UUID打印出来
                Log.d(TAG, "Characteristic UUID = :"+gattCharacteristic.getUuid());
//                Log.d(TAG, "特征名： ");

//                if(gattCharacteristic.getUuid().toString().equals("需要通信的UUID")){
                if(gattCharacteristic.getUuid().toString().equals(UUID_READ)){
                    //有哪些UUID，每个UUID有什么属性及作用，一般硬件工程师都会给出相应说明
                    //此处可以根据UUID的类型对设备进行读操作、写操作、设置notifaction等操作
                }
//                readCharacteristic(gattCharacteristic);
//                writeCharacteristic(gattCharacteristic);

                int charaProp = gattCharacteristic.getProperties();
                if((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                    Log.d(TAG, "该特征的属性为： 可读");
                }
                if((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0){
                    Log.d(TAG, "该特征的属性为： 可写");
                }
                if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                    Log.d(TAG, "该特征的属性为： 具备通知属性");
                }
            }
        }

        //测试发送数据给终端BLE设备
        service_test = mBluetoothGatt.
                getService(UUID.
                        fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
        characteristic_test = service_test.
                getCharacteristic(UUID.
                        fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));

        characteristic_test.setValue(new byte[] {0x31, 0x32, 0x33});
        characteristic_test.setWriteType
                (BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic_test);
        //必须设置notification功能，否则收不到BLE终端设备发送的数据
//        characteristic_rx_test = service_test.
//                getCharacteristic(UUID.
//                        fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
//        boolean isEnable =  mBluetoothGatt.setCharacteristicNotification
//                (characteristic_rx_test, true);
//        if(isEnable){
//            List<BluetoothGattDescriptor> descriptorList =
//                    characteristic_rx_test.getDescriptors();
//            if (descriptorList != null && descriptorList.size() > 0){
//                for(BluetoothGattDescriptor descriptor : descriptorList){
//                    descriptor.setValue(
//                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    mBluetoothGatt.writeDescriptor(descriptor);
//                }
//            }
//        }

    }

    public void setCharacteristicNotification(
                    BluetoothGattCharacteristic characteristic, boolean enable){
        if((bluetoothAdapter ==null) || (mBluetoothGatt == null)){
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.
//                        fromString())
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if((bluetoothAdapter == null) || (mBluetoothGatt == null)){
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if((bluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void enableTXNotification(){
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
//            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            Log.d(TAG, "设备不支持UART");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
//            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            Log.d(TAG, "设备不支持UART");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void setPopupListView(){
        //构建一个popupwindow的布局
        final View popupView = MainActivity.this.getLayoutInflater().
                inflate(R.layout.listpopupwindow, null);
        //因为是要显示搜索到的蓝牙列表，所以设置一个列表
        listPop = (ListView) popupView.findViewById(R.id.list_popup);

        Map<String, Object> map = new HashMap<String, Object>();
        //这里的string必须和from中的对应起来
//        for(int i = 0; i<10; i++) {
//            map.put("蓝牙名称", "我是测试的");
//            map.put("蓝牙强度", "Rssi = -65dbm");
//            map.put("蓝牙地址", "12.34.56.78");
//            list.add(map);
//        }

        String[] from = {"蓝牙名称","蓝牙强度",  "蓝牙地址"};
        int[] to = {R.id.ble_name, R.id.ble_rssi, R.id.ble_mac};
        simpleAdapter = new SimpleAdapter(MainActivity.this,
                list, R.layout.ble_item, from, to);

//                listPop.setAdapter(new ArrayAdapter<String>(
//                    MainActivity.this, android.R.layout.simple_list_item_1, datas));
//
        listPop.setAdapter(simpleAdapter);
        //创建popwindow对象，指定宽度和高度
//        final PopupWindow window = new PopupWindow(popupView, 500, 800);
        window = new PopupWindow(popupView, 500, 800);
        window.setBackgroundDrawable(new ColorDrawable
                (Color.parseColor("#f0f0f0")));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.setTouchable(true);
        window.update();
        window.showAtLocation(mbutton, Gravity.CENTER,0,0);

        listPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
                HashMap<String, String> map =
                        (HashMap<String, String>)listPop.getItemAtPosition(i);
                String deviceAddress = map.get("蓝牙地址");

                bleConnect(deviceAddress);

                window.dismiss();
            }
        });
    }

    public void dispReceiveData(final String data){
        receiveDataView.append(data+'\n');
    }

    public void sendtoDisp(String data){
//        new Thread(){
//            @Override
//            public void run() {
//                Message msg = new Message();
//                msg.what = RECEIVE_DATA;
//                msg.obj = data.toString();
//            }
//        }
        Message tempMsg = mHandler.obtainMessage();
        tempMsg.what = RECEIVE_DATA;
        tempMsg.obj = data.toString();
        mHandler.sendMessage(tempMsg);
    }
}