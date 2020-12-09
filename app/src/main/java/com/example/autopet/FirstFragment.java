package com.example.autopet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {

    public ClientSocket mClient = null;
    boolean feedFlag = true;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mClient = ClientSocket.getInstance();

        view.findViewById(R.id.feed_dog).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        if(feedFlag){
                            feedFlag = false;
                            v.getBackground().setAlpha(150);
                            // feed dog thread
                            if(mClient.getConnectStatus()){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int cnt = 5;
                                        mClient.sendData("@fd#");
                                        while(cnt > 0){
                                            cnt--;
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        feedFlag = true;
                                    }
                                }).start();
                            }else   Toast.makeText(getActivity(), "No tcp connected", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getActivity(), "Wait 10s to feed", Toast.LENGTH_LONG);
                        }

                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().setAlpha(255);
                        return true;
                    }
                }

                return false;
            }
        });

        view.findViewById(R.id.env).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

            }
        });
    }
}