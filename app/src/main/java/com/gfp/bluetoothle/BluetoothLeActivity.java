package com.gfp.bluetoothle;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Carmen from Coin (Coin)
 * Jun 18, 16:57
 * <p/>
 * Hi Harvey,
 * <p/>
 * I spoke with one of our Android developers. He shared that at a high level,
 * the app is always scanning for Coin, once it finds Coin, it connects, does
 * the secure handshake and disconnects. At this point, the physical bluetooth
 * link is terminated and the phone restarts scanning for Coin.
 * <p/>
 * Pre-Lollipop, we use the following API:
 * adapter.startLeScan(callback);
 * where the adapter is the BluetoothAdapter instance. This initiates active
 * scanning and we scan until we get a callback
 * On Motorola devices, we stop scanning after 5 seconds and restart it after
 * 1 second due a bug in the way the scanner would just stop scanning
 * unpredictably.
 * <p/>
 * Post Lollipop, we use the following API:
 * final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
 * final ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(Helper.COIN_SERVICE.toString())).build();
 * final ScanSettings settings = new ScanSettings.Builder().setReportDelay(0).setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
 * scanner.startScan(JavaUtils.list(filter), settings, callback);
 * <p/>
 * Once scanning has been completed and the secure handshake is also completed, we call
 * gatt.close() and restart scanning
 * <p/>
 * The advantage of using SCAN_MODE_LOW_POWER is that it does very low % duty cycling at the hardware level, which can give an opportunity for your wifi radio to turn on intermittently during its own scanning (at least thats the hope).
 * <p/>
 * Thanks a lot for offering to write a test app that sees if scanning can be optimized for your device / OS. My suspicion is that if you do a <50% duty-cycle scanning manually in software using timers, it could alleviate the issue.
 */
public class BluetoothLeActivity extends ActionBarActivity {
    public static final String TAG = "BluetoothLeActivity";

    private WifiManager mWifiManager;
    private ArrayAdapter mArrayAdapter;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_le);

        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mArrayAdapter);

//        // Constant...
//        // Handle BT state events
//        BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
//            @Override
//            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                Log.i(TAG, device.toString());
//
//            }
//        };
//        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//
//        if (bluetoothAdapter.isEnabled())
//            bluetoothAdapter.startLeScan(leScanCallback);

        // Once every N seconds?
        // If BT ON, but scan not running, start it
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mHandler = new Handler();

        listWifiScanResults();
    }

    public List<String> currentSsids() {
        List<String> ssids = new ArrayList<String>();
        if (mWifiManager == null || !mWifiManager.isWifiEnabled())
            return ssids;

        // TODO: can this ever return null?
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        if (scanResults == null)
            return ssids;

        for (ScanResult scanResult : scanResults)
            ssids.add(scanResult.SSID);
        return ssids;
    }

    public void printWifiScanResults(List<String> ssids ) {
        StringBuilder stringBuilder = new StringBuilder("Wifi Results: ");
        for (String ssid : ssids)
            stringBuilder.append(ssid + ", ");
        Log.i(TAG, stringBuilder.toString());
    }

    public void listWifiScanResults() {
        List<String> ssids = currentSsids();
        mArrayAdapter.clear();
        mArrayAdapter.addAll(ssids);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listWifiScanResults();
            }
        }, 5000);

        printWifiScanResults(ssids);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_le, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
