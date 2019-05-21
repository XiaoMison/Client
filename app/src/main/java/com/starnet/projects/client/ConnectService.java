package com.starnet.projects.client;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectService extends Service {
    public static final String TAG = ConnectService.class.getSimpleName();
    private int mHeart_spacetime = 3 * 1000; //心跳间隔时间
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private ReadThread mReadThread;
    private Handler mHandler = new Handler();
    private Socket mSocket;
    private ExecutorService mExecutorService;
    private int tryCount = 0;//重试次数


    @Override
    public IBinder onBind(Intent intent) {
        return new ClientBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public class ClientBinder extends Binder {
        public ConnectService getConnectService(){
            return ConnectService.this;
        }
    }

    public void startConnect() {
        //在子线程进行网络操作
        // Service也是运行在主线程，千万不要以为Service意思跟后台运行很像，就以为Service运行在后台子线程
        if (mExecutorService == null) {
            mExecutorService = Executors.newCachedThreadPool();
        }
        if(mSocket == null){
            mExecutorService.execute(connectRunnable);
        }
    }

    public boolean isConnected(){
        if(mSocket == null||mSocket.isConnected()){
            return false;
        }else{
            return true;
        }
    }

    public void reconnect(){
        if (!isConnected()){
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (mSocket != null)
                        mSocket = null;
                    mExecutorService.execute(connectRunnable);
                }
            });
        }
    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                // 建立Socket连接
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress("192.168.43.240", 8888), 10);
                bis = new BufferedInputStream(mSocket.getInputStream());
                bos = new BufferedOutputStream(mSocket.getOutputStream());
                // 创建读取服务器心跳的线程
                mReadThread = new ReadThread();
                mReadThread.start();
                //开启心跳,每隔15秒钟发送一次心跳
                mHandler.post(mHeartRunnable);
                tryCount = 1;
            } catch (Exception e) {
                tryCount ++ ;
                e.printStackTrace();
                Log.d(TAG, "Socket连接建立失败,正在尝试第"+ tryCount + "次重连");
                Intent connectFailureIntent = new Intent("connectFailure");
                connectFailureIntent.putExtra("connectFailure","连接建立失败,正在尝试第"+ tryCount + "次重连");
                sendBroadcast(connectFailureIntent);
                mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mExecutorService.execute(connectRunnable);
                        }
                        },mHeart_spacetime);
            }
        }
    };

    public class ReadThread extends Thread {
        @Override
        public void run() {
            int size;
            byte[] buffer = new byte[1024];
            try {
                while ((size = bis.read(buffer)) != -1) {
                    String str = new String(buffer, 0, size,"UTF-8");
                    //收到心跳消息以后，首先移除断连消息，然后创建一个新的60秒后执行断连的消息。
                    //这样每次收到心跳后都会重新创建一个60秒的延时消息，在60秒后还没收到心跳消息，表明服务器已死，就会执行断开Socket连接
                    //在60秒钟内如果收到过一次心跳消息，就表明服务器还活着，可以继续与之通讯。
                    if(str.equals("102:LoginSuccess")){
                        Intent loginSuccessIntent = new Intent("loginSuccess");
                        sendBroadcast(loginSuccessIntent);
                    }else if(str.equals("102:LoginFailed")){
                        Intent loginFailedIntent = new Intent("loginFailed");
                        sendBroadcast(loginFailedIntent);
                    }else if(str.equals("104:SetContentSuccess")){
                        Toast.makeText(getApplicationContext(), "设置LCD板成功", Toast.LENGTH_SHORT).show();
                    }
                    else if(str.equals("106:UpdateSuccess")){
                        Intent updateSuccessIntent = new Intent("updateSuccess");
                        sendBroadcast(updateSuccessIntent);
                        Toast.makeText(getApplicationContext(), "密码更新成功", Toast.LENGTH_SHORT).show();
                    }else if(str.equals("106:UpdateFailed")){
                        Intent updateFailedIntent = new Intent("updateFailed");
                        sendBroadcast(updateFailedIntent);
                        Toast.makeText(getApplicationContext(), "密码更新失败", Toast.LENGTH_SHORT).show();
                    }
                    else if(str.startsWith("110")){
                        Intent contentIntent = new Intent("Content");
                        contentIntent.putExtra("content",str.substring(4));
                        sendBroadcast(contentIntent);
                    }
                    mHandler.removeCallbacks(disConnectRunnable);
                    mHandler.postDelayed(disConnectRunnable, mHeart_spacetime * 40);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(final String message){
        if (mSocket==null||bis==null||bos==null){
        }else{
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        bos.write(message.getBytes());
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private Runnable mHeartRunnable = new Runnable() {
        @Override
        public void run() {
            sendData();
        }
    };

    private void sendData() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    bos.write("000:Can you recieve this message?".getBytes());
                    //一定不能忘记这步操作
                    bos.flush();
                    //发送成功以后，重新建立一个心跳消息
                    mHandler.postDelayed(mHeartRunnable, mHeart_spacetime);
                    Log.d(TAG, "000:Can you recieve this message?");
                } catch (Exception e) {
                    if(tryCount <= 10){
                        e.printStackTrace();
                        Log.d(TAG, "心跳任务发送失败，正在尝试第"+ tryCount + "次重连");
                        mExecutorService.execute(connectRunnable);
                    }else{
                        mExecutorService.execute(disConnectRunnable);
                    }

                }
            }
        });
    }

    private Runnable disConnectRunnable = new Runnable() {
        @Override
        public void run() {
            disConnect();
        }
    };

    public void disConnect() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "正在执行断连: disConnect");
                    tryCount = 0;
                    //执行Socket断连
                    mHandler.removeCallbacks(mHeartRunnable);
                    if (mReadThread != null) {
                        mReadThread.interrupt();
                    }

                    if (bos != null) {
                        bos.close();
                    }

                    if (bis != null) {
                        bis.close();
                    }

                    if (mSocket != null) {
                        mSocket.shutdownInput();
                        mSocket.shutdownOutput();
                        mSocket.close();
                        mSocket = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
