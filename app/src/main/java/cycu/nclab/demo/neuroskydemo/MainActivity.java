package cycu.nclab.demo.neuroskydemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import im.dino.dbinspector.activities.DbInspectorActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button bt1, bt2, bt3, bt4, bt5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        uiInit();

        //@SuppressWarnings("unused")
        //int check = getContentResolver().update(uri, null, null, null);	// 讓content provider有機會初始化
    }

    private void uiInit() {
        // TODO Auto-generated method stub
        bt1 = (Button) this.findViewById(R.id.button1);
        bt1.setOnClickListener(this);
        bt2 = (Button) this.findViewById(R.id.button2);
        bt2.setOnClickListener(this);
        bt3 = (Button) this.findViewById(R.id.button3);
        bt3.setOnClickListener(this);
        bt4 = (Button) this.findViewById(R.id.button4);
        bt4.setOnClickListener(this);
        bt5 = (Button) this.findViewById(R.id.button5);
        bt5.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button1:
                Intent intent = new Intent();
                intent.setClass(this, SensingActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                Intent intent2 = new Intent();
                intent2.setClass(this, DbInspectorActivity.class);
                startActivity(intent2);
                break;
            case R.id.button3:
                DB_neurosky db = new DB_neurosky(this);
                db.delete(null);
                break;
            case R.id.button4:
                Intent intent3 = new Intent();
                //TODO
                //intent3.setClass(this, ShowResult.class);
                startActivity(intent3);
                break;
            case R.id.button5:
                try {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    if (sd.canWrite()) {
                        String currentDBPath = "//data//cycu.nclab.demo.neuroskydemo//databases//sensorRecord.db";
                        String backupDBPath = "neuroskyDB.db";
                        File currentDB = new File(data, currentDBPath);
                        File backupDB = new File(sd, backupDBPath);

                        if (currentDB.exists()) {
                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    }
                } catch (Exception e) {
                }
                break;
        }
    }
}
