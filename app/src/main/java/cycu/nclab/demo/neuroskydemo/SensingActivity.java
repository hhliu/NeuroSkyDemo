package cycu.nclab.demo.neuroskydemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
    BluetoothAdapter bluetoothAdapter;
    TGDevice tgDevice;
    final boolean rawEnabled = true;



    TextView state, att, med;
    Button bt;
    ProgressBar p1,p2;

    DB_neurosky db;
    ContentValues cv;

    // working thread
    private HandlerThread mThread;
    private Handler mThreadHandler;


    //SaveHandler handler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neurosensing);
        state = (TextView)findViewById(R.id.textView2);
        att = (TextView)findViewById(R.id.textView5);
        med = (TextView)findViewById(R.id.textView6);
        bt = (Button)findViewById(R.id.button);

        p1 = (ProgressBar)findViewById(R.id.progressBar);
        p2 = (ProgressBar)findViewById(R.id.progressBar2);

        db = new DB_neurosky(this);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else {
            mThread =  new HandlerThread("neurosky");
            mThread.start();
            mThreadHandler = new SaveHandler(mThread.getLooper());
        	/* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, mThreadHandler);
            if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
                tgDevice.connect(rawEnabled);
                db = new DB_neurosky(this);
                db.openDBWriteable();
            }
            else {
                Toast.makeText(this, "NeuroSkey MindWave didn't start", Toast.LENGTH_LONG).show();
            }
        }
    }




    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        tgDevice.close();
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
        }

        if (mThread != null) {
            mThread.quit();
        }
        super.onDestroy();
    }


    public void Stop(View view) {
        if(tgDevice.getState() == TGDevice.STATE_CONNECTING || tgDevice.getState() == TGDevice.STATE_CONNECTED)
            tgDevice.close();
        //tgDevice.ena
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Utils.logThread();
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
                case TGDevice.MSG_ATTENTION:
                    att.setText(String.valueOf(msg.arg1));
                    p1.setProgress(msg.arg1);
                    break;
                case TGDevice.MSG_MEDITATION:
                    med.setText(String.valueOf(msg.arg1));
                    p2.setProgress(msg.arg1);
                    break;
            }
        }
    };


    class SaveHandler extends Handler {
       private final String TAG = this.getClass().getSimpleName();
        public SaveHandler(android.os.Looper looper) {
            super (looper);
        }

    //負責儲存到DB，希望是背景執行
    //private final Handler mThreadHandler = new Handler(mThread.getLooper()) {
        /**
         * Handles messages from TGDevice
         */
        @Override
        public void handleMessage(Message msg) {
            Utils.logThread();
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            Message msg2 = new Message();
                            msg2.what = msg.what;
                            msg2.arg1 = msg.arg1;
                            handler.sendMessage(msg2);
                            // for git test
                        case TGDevice.STATE_CONNECTED:
                        case TGDevice.STATE_NOT_FOUND:
                        case TGDevice.STATE_NOT_PAIRED:
                        case TGDevice.STATE_DISCONNECTED:
                            Message msg3 = new Message();
                            msg3.what = msg.what;
                            msg3.arg1 = msg.arg1;
                            handler.sendMessage(msg3);
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    // tv.append("PoorSignal: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_DATA:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_RAW, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.RAW_TABLE, cv);
                    //raw1 = msg.arg1;
                    //tv.append("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    break;
                case TGDevice.MSG_ATTENTION:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_ATTENTION, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.ATT_TABLE, cv);
                    Message msg2 = new Message();
                    msg2.what = msg.what;
                    msg2.arg1 = msg.arg1;
                    handler.sendMessage(msg2);
                    //tv.append("Attention: " + msg.arg1 + "\n");
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_MEDITATION, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.MED_TABLE, cv);
                    Message msg3 = new Message();
                    msg3.what = msg.what;
                    msg3.arg1 = msg.arg1;
                    handler.sendMessage(msg3);
                    break;
                case TGDevice.MSG_BLINK:
                    cv = new ContentValues(2);
                    cv.put(db.KEY_BLANK, msg.arg1);
                    cv.put(db.KEY_TIMESTAMP, System.currentTimeMillis());
                    db.insert(DB_neurosky.BLANK_TABLE, cv);
                    //tv.append("Blink: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    //Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:

                    // 啊啊阿！NeuroSky MindWave沒有輸出這一項
//                    TGRawMulti rawM = (TGRawMulti)msg.obj;
//                    if (aaa == true) {
//                        String str = "Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2 + "\nRaw3: " + rawM.ch3
//                            + "\nRaw4: " + rawM.ch4 + "\nRaw5: " + rawM.ch5 + "\nRaw6: " + rawM.ch6
//                                + "\nRaw7: " + rawM.ch7 + "\nRaw8: " + rawM.ch8
//                                +"\nTimeStamp: " + rawM.timeStamp1;
//                        state.setText(str);
//                        aaa = false;
//                    }
                default:
                    break;
            }
        }
    }



}

