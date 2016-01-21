package cycu.nclab.demo.neuroskydemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

/**
 * Created by hhliu on 2015/12/16.
 */
public class SensingActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName();
    BluetoothAdapter bluetoothAdapter;
    TGDevice tgDevice;
    final boolean rawEnabled = true;


    TextView state, att, med;
    Button bt;
    ProgressBar p1, p2;

    DB_neurosky db;
    ContentValues cv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neurosensing);
        state = (TextView) findViewById(R.id.textView2);
        att = (TextView) findViewById(R.id.textView5);
        med = (TextView) findViewById(R.id.textView6);
        bt = (Button) findViewById(R.id.button);

        p1 = (ProgressBar) findViewById(R.id.progressBar);
        p2 = (ProgressBar) findViewById(R.id.progressBar2);

        db = new DB_neurosky(this);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            /* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
            if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
                tgDevice.connect(rawEnabled);
                db = new DB_neurosky(this);
                db.openDBWriteable();
            } else {
                Toast.makeText(this, "NeuroSkey MindWave didn't start", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onDestroy() {
        db.close();
        tgDevice.close();
        super.onDestroy();
    }


    public void Stop(View view) {
        if (tgDevice.getState() == TGDevice.STATE_CONNECTING || tgDevice.getState() == TGDevice.STATE_CONNECTED)
            tgDevice.close();
        //tgDevice.ena
    }

    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "in handler");
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            state.setText("Connecting...");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            state.setText("Connected.");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            state.setText("Can't find");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            state.setText("not paired");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            state.setText("Disconnected mang");
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    state.setText("PoorSignal: " + msg.arg1);
                    break;
                case TGDevice.MSG_RAW_DATA:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_RAW, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.RAW_TABLE, cv);
                    break;
                case TGDevice.MSG_HEART_RATE:
                    //tv.append("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_ATTENTION, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.ATT_TABLE, cv);
                    att.setText(String.valueOf(msg.arg1));
                    p1.setProgress(msg.arg1);
                case TGDevice.MSG_MEDITATION:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_MEDITATION, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.MED_TABLE, cv);
                    med.setText(String.valueOf(msg.arg1));
                    p2.setProgress(msg.arg1);
                    break;
                case TGDevice.MSG_BLINK:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_BLANK, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.BLANK_TABLE, cv);
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
                default:
                    break;
            }
        }
    };

}

