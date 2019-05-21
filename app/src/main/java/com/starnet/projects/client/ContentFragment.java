package com.starnet.projects.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ContentFragment extends Fragment {
    public static final String TAG = "content";
    private View view;
    private TextView contentTextView;
    private TextView timeTextView;
    private TextView dateTextView;
    private TextView weekTextView;
    private TextView addressTextView;
    private TextView ariConditionTextView;
    private TextView temperatureTextView;
    private String content;
    private MsgReceiver msgReceiver;
    public static ContentFragment newInstance(String content) {
        ContentFragment fragment = new ContentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Content");
        getActivity().registerReceiver(msgReceiver, intentFilter);
    }

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("Content")){
                content = intent.getStringExtra("content");
                Handler handler = new Handler();
                handler.post(runnable);
            }
        }
    }

    public void setContent(String content){
        this.content = content;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String[] detail = content.split(",");
            timeTextView.setText(detail[0]);
            temperatureTextView.setText(detail[1]);
            dateTextView.setText(detail[2]);
            ariConditionTextView.setText(detail[3]);
            weekTextView.setText(detail[4]);
            addressTextView.setText(detail[5]);
            contentTextView.setText(detail[6]);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(msgReceiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
        lcdClientActivity.getConnectService().sendMessage("109:Please give me the content");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_content, container, false);
        init();
        LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
        lcdClientActivity.getConnectService().sendMessage("109:Please give me the content");
        return view;
    }

    private void init() {
        contentTextView = view.findViewById(R.id.tv_content_information);
        timeTextView = view.findViewById(R.id.tv_time);
        dateTextView = view.findViewById(R.id.tv_date);
        temperatureTextView = view.findViewById(R.id.tv_temperature);
        addressTextView = view.findViewById(R.id.tv_address);
        weekTextView = view.findViewById(R.id.tv_week);
        ariConditionTextView = view.findViewById(R.id.tv_weather_air_condition);
    }
}
