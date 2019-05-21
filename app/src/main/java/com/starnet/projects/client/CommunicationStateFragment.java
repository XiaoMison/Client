package com.starnet.projects.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CommunicationStateFragment extends Fragment {
    private View view;
    private TextView textView;
    private Button button;


    public static CommunicationStateFragment newInstance() {
        CommunicationStateFragment fragment = new CommunicationStateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_communication_state, container, false);
        init();
        return view;
    }

    private void init() {
        textView = (TextView) view.findViewById(R.id.tv_communication_state);
        button = view.findViewById(R.id.bt_connect_or_disconnect);
        button.setText("断开连接");
        textView.setText("已经连接");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().equals("断开连接")){
                    LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
                    ((LCDClientActivity) getActivity()).getConnectService().sendMessage("111:Disconnect");
                    lcdClientActivity.getConnectService().disConnect();
                    button.setText("重新连接");
                    textView.setText("断开连接");
                }else{
                    LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
                    //lcdClientActivity.getConnectService().startConnect();
                    lcdClientActivity.getConnectService().reconnect();
                    button.setText("断开连接");
                    textView.setText("已经连接");
                }
            }
        });
    }
}
