package com.bitalino.bitalinodroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoFrame;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.client.Response;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity {

  private static final String TAG = "MainActivity";
  private static final boolean UPLOAD = false;
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
  @InjectView(R.id.log)
  private TextView tvLog;
  private boolean testInitiated = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // execute
    if (!testInitiated)
      new TestAsyncTask().execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  private class TestAsyncTask extends AsyncTask<Void, String, Void> {
    private TextView tvLog = (TextView) findViewById(R.id.log);
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bitalino;

    @Override
    protected Void doInBackground(Void... paramses) {
      try {
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

        bitalino = new BITalinoDevice(1000, new int[]{0, 1, 2, 3, 4, 5});
        publishProgress("Connecting to BITalino [" + remoteDevice + "]..");
        bitalino.open(sock.getInputStream(), sock.getOutputStream());
        publishProgress("Connected.");

        // get BITalino version
        publishProgress("Version: " + bitalino.version());

        // start acquisition on predefined analog channels
        bitalino.start();

        // read until task is stopped
        int counter = 0;
        while (counter < 100) {
          final int numberOfSamplesToRead = 1000;
          publishProgress("Reading " + numberOfSamplesToRead + " samples..");
          BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);

          if (UPLOAD) {
            // prepare reading for upload
            BITalinoReading reading = new BITalinoReading();
            reading.setTimestamp(System.currentTimeMillis());
            reading.setFrames(frames);
            // instantiate reading service client
            RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://server_ip:8080/bitalino")
                .build();
            ReadingService service = restAdapter.create(ReadingService.class);
            // upload reading
            Response response = service.uploadReading(reading);
            assert response.getStatus() == 200;
          }

          // present data in screen
          for (BITalinoFrame frame : frames)
            publishProgress(frame.toString());

          counter++;
        }

        // trigger digital outputs
        // int[] digital = { 1, 1, 1, 1 };
        // device.trigger(digital);
      } catch (Exception e) {
        Log.e(TAG, "There was an error.", e);
      }

      return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
      tvLog.append("\n".concat(values[0]));
    }

    @Override
    protected void onCancelled() {
      // stop acquisition and close bluetooth connection
      try {
        bitalino.stop();
        publishProgress("BITalino is stopped");

        sock.close();
        publishProgress("And we're done! :-)");
      } catch (Exception e) {
        Log.e(TAG, "There was an error.", e);
      }
    }

  }

}