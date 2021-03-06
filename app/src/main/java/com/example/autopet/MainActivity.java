package com.example.autopet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private IntentFilter mIntentFilter = null;
    private NetworkChangeReceiver mNetworkChangleReceiver = null;
    private boolean networkStatus = false;

    private ProgressDialog mProgreeDialog = null;
    private Thread TCPThread = null;
    private Handler mHandler = null;

    public ClientSocket mClient = null;

    private ImageView mcuIcon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgreeDialog = new ProgressDialog(this);
        initProgressDialog();

        mcuIcon = findViewById(R.id.mcu);

        mIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkChangleReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkChangleReceiver, mIntentFilter);


        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 'a':   {
                        Toast.makeText(MainActivity.this, "TCP is setup!!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 'b':   {
                        Toast.makeText(MainActivity.this, "TCP is break", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 'c':   {
                        Toast.makeText(MainActivity.this, "MCU is connected", Toast.LENGTH_SHORT).show();
                        mcuIcon.setBackground(getResources().getDrawable(R.drawable.ic_mcu));
                        break;
                    }
                    case 'd':   {
                        Toast.makeText(MainActivity.this, "MCU is disconnected", Toast.LENGTH_SHORT).show();
                        mcuIcon.setBackground(getResources().getDrawable(R.drawable.ic_break));
                        break;
                    }
                }
            }
        };

        mClient = ClientSocket.getInstance();
        mClient.setConnectStatusListener(new ClientSocket.ConnectStatusListener() {
            @Override
            public void OnConnectTCP() {
                Message msg = new Message();
                msg.arg1 = 0;
                msg.what = 'a';
                mHandler.sendMessage(msg);
                mClient.heartBeatThread();
                mClient.recThread();
            }

            @Override
            public void OnDisconnectTCP() {
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = 'b';
                mHandler.sendMessage(msg);
            }
        });

        mClient.setMcuConnectListener(new ClientSocket.MCUConnectListener() {
            @Override
            public void onMCUConnected() {
                Message msg = new Message();
                msg.arg1 = 2;
                msg.what = 'c';
                mHandler.sendMessage(msg);
            }

            @Override
            public void onMCUDisconnected() {
                Message msg = new Message();
                msg.arg1 = 3;
                msg.what = 'd';
                mHandler.sendMessage(msg);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkChangleReceiver);
        mClient.resetClient();
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
        if ((id == R.id.settings_tcp) && (networkStatus) && (mClient.getConnectStatus() == false)) {
            mProgreeDialog.show();
            TCPThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mClient.setupTCP();
                    mProgreeDialog.cancel();
                }
            });
            TCPThread.start();
            return true;
        }
        if (id == R.id.settings_caltime){
            if(networkStatus == false){
                Toast.makeText(this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return true;
            }
            if(mClient.getConnectStatus() == false){
                Toast.makeText(this, "TCP未连接", Toast.LENGTH_SHORT).show();
                return true;
            }
            // send time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date(System.currentTimeMillis());
            String caltime = "@st" + simpleDateFormat.format(date) + "#";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mClient.sendData(caltime);
                }
            }).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                networkStatus = true;
                Toast.makeText(context, "当前网络可用", Toast.LENGTH_SHORT).show();
            } else {
                networkStatus = false;
                Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void initProgressDialog(){
        mProgreeDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        mProgreeDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        mProgreeDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        mProgreeDialog.setMessage("TCP is setting...");
    }

    ClientSocket getmClient(){return mClient;}


}