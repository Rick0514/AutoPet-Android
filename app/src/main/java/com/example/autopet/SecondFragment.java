package com.example.autopet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondFragment extends Fragment {

    private boolean sendingWS;

    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String, Object>> mDatas = new ArrayList<Map<String, Object>>();

    private Handler mHandler = null;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        sendingWS = true;
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // back bottom
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendingWS = false;
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        mListView = view.findViewById(R.id.info);
        initDatas();
        mSimpleAdapter = new SimpleAdapter(
                getActivity(),
                mDatas,
                R.layout.list_item,
                new String[]{"icon", "value"},
                new int[]{R.id.list_img, R.id.list_value}
        );
        mListView.setAdapter(mSimpleAdapter);

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 'a':{
                        String[] ws = ClientSocket.getInstance().getWS();
                        changeDatas(0, ws[0] + "℃");
                        changeDatas(1, ws[1] + "%");
                        mSimpleAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        };

        ClientSocket.getInstance().setReadWSListener(new ClientSocket.ReadWSListener() {
            @Override
            public void OnReadWS() {
                Message msg = new Message();
                msg.arg1 = 0;
                msg.what = 'a';
                mHandler.sendMessage(msg);
            }
        });

        if(ClientSocket.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(sendingWS){
                        ClientSocket.getInstance().sendData("@ws#");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }else{
            Toast.makeText(getActivity(), "No tcp connected", Toast.LENGTH_LONG).show();
        }
    }

    private void initDatas(){
        Map map1 = new HashMap();
        map1.put("icon", R.drawable.ic_temp);
        map1.put("value", "receiving...");
        mDatas.add(map1);

        Map map2 = new HashMap();
        map2.put("icon", R.drawable.ic_tum);
        map2.put("value", "receiving...");
        mDatas.add(map2);
    }


    public void changeDatas(int pos, String val){
        if(pos < 2){
            Map map = mDatas.get(pos);
            map.put("value", val);
        }
    }
}