package com.bitalino.bitalinodroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.bitalino.BITalinoDevice;
import com.bitalino.BITalinoException;
import com.bitalino.BITalinoFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";
  /*
   * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
   * #createRfcommSocketToServiceRecord(java.util.UUID)
   *
   * "Hint: If you are connecting to a Bluetooth serial board then try using the
   * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
   * are connecting to an Android peer then please generate your own unique
   * UUID."
   */
  private static final UUID MY_UUID = UUID
          .fromString("00001101-0000-1000-8000-00805F9B34FB");
  private BluetoothDevice dev = null;
  private BluetoothSocket sock = null;
  private InputStream is = null;
  private OutputStream os = null;
  private boolean testInitiated = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (!testInitiated)
      try {
        test();
      } catch (Exception e) {
        Log.e(TAG, "EXCEPTION: ", e);
      }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  void test() throws IOException, BITalinoException {
    // Let's get the remote Bluetooth device
    final String remoteDevice = "20:13:08:08:15:83";

    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    dev = btAdapter.getRemoteDevice(remoteDevice);

    /*
     * Establish Bluetooth connection
     *
     * Because discovery is a heavyweight procedure for the Bluetooth adapter,
     * this method should always be called before attempting to connect to a
     * remote device with connect(). Discovery is not managed by the Activity,
     * but is run as a system service, so an application should always call
     * cancel discovery even if it did not directly request a discovery, just to
     * be sure. If Bluetooth state is not STATE_ON, this API will return false.
     *
     * see
     * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
     * .html#cancelDiscovery()
     */
    Log.d(TAG, "Stopping Bluetooth discovery.");
    btAdapter.cancelDiscovery();

    sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
    sock.connect();
    testInitiated = true;

    BITalinoDevice bitalino = new BITalinoDevice(10, new int[]{0});
    bitalino.open(sock.getInputStream(), sock.getOutputStream());

    // get BITalino version
    Log.i(TAG, "VERSION: " + bitalino.version());

    // start acquisition on predefined analog channels
    bitalino.start();

    // read n samples
    final int numberOfSamplesToRead = 10;
    Log.i(TAG, "Reading " + numberOfSamplesToRead + " samples..");
    BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);
    for (BITalinoFrame frame : frames)
      Log.d(TAG, "FRAME: " + frame.toString());

    // trigger digital outputs
    // int[] digital = { 1, 1, 1, 1 };
    // device.trigger(digital);

    // stop acquisition and close bluetooth connection
    bitalino.stop();

    sock.close();
  }

}