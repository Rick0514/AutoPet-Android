package com.example.autopet;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class ClientSocket {

    private final String ip_addr = "49.234.68.36";
    private final int port = 8193;
    private SocketAddress s_addr = null;

    private Socket client = null;
    private DataOutputStream mDOS = null;
    private BufferedReader mDIS = null;

    private boolean connectStatus;
    private ConnectStatusListener mConnectStatusListener = null;

    private int lostTcpNum = 0;
    private byte[] lock = new byte[0];

    private String[] ws = new String[2];
    private ReadWSListener mReadWSListener = null;

    // single instance
    private static final ClientSocket instance = new ClientSocket();

    private ClientSocket(){
        ws[0] = "receiving...";
        ws[1] = "receiving...";
        connectStatus = false;
        s_addr = new InetSocketAddress(ip_addr, port);
    }

    public static ClientSocket getInstance(){
        return instance;
    }

    public void setupTCP(){
        try{
            client = new Socket();
            client.connect(s_addr, 5000);  //5s timeout
            initDataStream();
            sendData("@and#");
        } catch (UnknownHostException e) {
            Log.e("setupTCP", e.toString());
            return;
        } catch (IOException e) {
            Log.e("setupTCP", e.toString());
            return;
        } catch (Exception e){
            return;
        }
        Log.i("setupTCP", "connected!!");
        connectStatus = true;
        mConnectStatusListener.OnConnectTCP();
    }

    private void initDataStream(){
        if(client != null) {
            try {
                mDOS = new DataOutputStream(client.getOutputStream());
                InputStream is = client.getInputStream();
                mDIS = new BufferedReader(new InputStreamReader(is));
            } catch (UnknownHostException e) {
                Log.e("DataStream", e.toString());
                return;
            } catch (IOException e) {
                Log.e("DataStream", e.toString());
                return;
            }
        }
    }

    public boolean sendData(String str){
        if(mDOS == null) {
            return false;
        }
        try {
            mDOS.writeBytes(str);
        } catch (UnknownHostException e) {
            Log.e("senddata", e.toString());
            return false;
        } catch (IOException e) {
            Log.e("senddata", e.toString());
            return false;
        }
        return true;
    }

    public String readData(){
        String result = null;
        try {
            result = mDIS.readLine();
            if(result!=null)    result.replaceAll("\n","");
        } catch (UnknownHostException e) {
            Log.e("readdata", e.toString());
            return null;
        } catch (IOException e) {
            Log.e("readdata", e.toString());
            return null;
        }
        return result;
    }

    public boolean resetClient(){
        try {
            if (mDIS != null)   mDIS.close();
            if (mDOS != null){
                mDOS.flush();
                mDOS.close();
            }
            if (client != null) client.close();
            lostTcpNum = 0;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        connectStatus = false;
        mConnectStatusListener.OnDisconnectTCP();
        return true;

    }

    public void recThread(){
        // keep read "tcp" send by server
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ret;
                while (true) {
                    String str = readData();
                    if(str == null){
                        // tcp is breaking
                        connectStatus = false;
                        resetClient();
                        break;
                    }
//                    Log.i("str", str);
//                    if(str.equals("tcp")){
//                        synchronized (lock){lostTcpNum = 0;}
//                    }
                    if(str.charAt(0) == 'w'){
                        ws[0] = str.substring(1);
                        mReadWSListener.OnReadWS();
                    }
                    if(str.charAt(0) == 's'){
                        ws[1] = str.substring(1);
                        mReadWSListener.OnReadWS();
                    }
                }
            }
        }).start();
    }

    public void heartBeatThread(){
        // keep counting
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (connectStatus){
//                    synchronized (lock){
//                        lostTcpNum++;
//                        Log.i("lock", "lockNum: " + lostTcpNum);
//                    }
//                    if(lostTcpNum > 5){
//                        connectStatus = false;
//                        resetClient();
//                    }
                    try {
                        Thread.sleep(2000);
                        sendData("@tcp#");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public boolean getConnectStatus(){  return connectStatus;   }

    public void setConnectStatusListener(ConnectStatusListener mListener){
        mConnectStatusListener = mListener;
    }

    public void setReadWSListener(ReadWSListener mListener){
        mReadWSListener = mListener;
    }

    static public interface ConnectStatusListener
    {
        public void OnConnectTCP();
        public void OnDisconnectTCP();
    }

    static public interface ReadWSListener
    {
        public void OnReadWS();
    }

    public String[] getWS(){return ws;}
}
