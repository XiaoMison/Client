package com.starnet.projects.client;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private ConnectService connectService = null;
    private Intent loginIntent;
    MsgReceiver msgReceiver = null;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectService.ClientBinder localBinder = (ConnectService.ClientBinder)service;
            connectService = localBinder.getConnectService();
            connectService.startConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectService = null;
        }
    };

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("loginSuccess")){
                Intent LCDClientIntent = new Intent();
                String userName = userNameEditText.getText().toString();
                LCDClientIntent.putExtra("userName",userName);
                LCDClientIntent.setClass(LoginActivity.this,LCDClientActivity.class);
                startActivity(LCDClientIntent);
            }else if(intent.getAction().equals("loginFailed")){
                Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }else if(intent.getAction().equals("connectFailure")){
                Toast.makeText(getApplicationContext(), intent.getStringExtra("connectFailure"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userNameEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
        loginIntent = new Intent(LoginActivity.this,ConnectService.class);
        getApplicationContext().bindService(loginIntent,connection,Service.BIND_AUTO_CREATE);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("loginSuccess");
        intentFilter.addAction("loginFailed");
        intentFilter.addAction("connectFailure");
        registerReceiver(msgReceiver, intentFilter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        loginIntent = new Intent(LoginActivity.this,ConnectService.class);
        getApplicationContext().bindService(loginIntent,connection,Service.BIND_AUTO_CREATE);
    }

    @Override
    protected  void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        connectService.disConnect();
        unbindService(connection);
        unregisterReceiver(msgReceiver);
    }

    public void login(View view){
        String userName = userNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if(userName.length()==0||password.length()==0){
            Toast.makeText(getApplicationContext(), "账号和密码不能为空", Toast.LENGTH_SHORT).show();
        }else{
            connectService.sendMessage("101,"+encoder(userName+","+password));
        }
    }

    private String encoder(String message){
        return Base64.encodeToString(message.getBytes(),Base64.DEFAULT);
    }
}
