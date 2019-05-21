package com.starnet.projects.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordChangeFragment extends Fragment {
    private View view;
    private EditText oldPassword;
    private EditText newPassword;
    private Button button;
    MsgReceiver msgReceiver = null;

    public static PasswordChangeFragment newInstance(String content) {
        PasswordChangeFragment fragment = new PasswordChangeFragment();
        return fragment;
    }
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("updateSuccess")){
                Toast.makeText(getContext(), "密码更新成功", Toast.LENGTH_SHORT).show();
            }else if(intent.getAction().equals("updateFailed")){
                Toast.makeText(getContext(), "密码更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("updateSuccess");
        intentFilter.addAction("updateFailed");
        getActivity().registerReceiver(msgReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_password_change, container, false);
        init();
        return view;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(msgReceiver);
    }

    private String encoder(String message){
        return Base64.encodeToString(message.getBytes(),Base64.DEFAULT);
    }

    private void init() {
        oldPassword = view.findViewById(R.id.et_fragment_change_password_old);
        newPassword = view.findViewById(R.id.et_fragment_change_password_new);
        button = view.findViewById(R.id.bt_fragment_change_password);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(oldPassword.getText().toString().equals("")||newPassword.getText().toString().equals("")){
                    Toast.makeText(getContext(), "旧密码和新密码不能为空", Toast.LENGTH_SHORT).show();
                }else{
                    LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
                    lcdClientActivity.getConnectService().sendMessage("105,"+encoder(((LCDClientActivity) getActivity()).getUserName()+","+oldPassword.getText().toString()+","+newPassword.getText().toString()));
                }
            }
        });
    }
}
