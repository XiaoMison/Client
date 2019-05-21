package com.starnet.projects.client;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LCDClientActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Fragment> fragments;
    private DrawerLayout drawer;
    private ConnectService connectService = null;
    private Intent lcdClientIntent;
    public String userName = null;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectService.ClientBinder localBinder = (ConnectService.ClientBinder)service;
            connectService = localBinder.getConnectService();
            connectService.startConnect();
            selectItem(0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcdclient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userName = getIntent().getStringExtra("userName");

        drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        init();
        lcdClientIntent = new Intent(LCDClientActivity.this,ConnectService.class);
        getApplicationContext().bindService(lcdClientIntent,connection,Service.BIND_AUTO_CREATE);
    }
    protected ConnectService getConnectService(){
        return connectService;
    }

    @Override
    protected void onResume(){
        super.onResume();
        lcdClientIntent = new Intent(LCDClientActivity.this,ConnectService.class);
        getApplicationContext().bindService(lcdClientIntent,connection,Service.BIND_AUTO_CREATE);
    }
    @Override
    protected void onPause(){
        super.onPause();
        //unbindService(connection);
    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void selectItem(int position){
        Fragment fragment = fragments.get(position);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.activity_fragment_container,fragment).commit();
    }

    private void init(){
        fragments =new ArrayList<Fragment>();
        fragments.add(ContentFragment.newInstance("ConntentFragment"));
        fragments.add(SetContentFragment.newInstance("SetContentFragment"));
        fragments.add(CommunicationStateFragment.newInstance());
        fragments.add(PasswordChangeFragment.newInstance("PasswordChangeFragment"));
    }

    public String getUserName(){
        return userName;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lcdclient, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            selectItem(0);
        } else if (id == R.id.nav_gallery) {
            selectItem(1);
        } else if (id == R.id.nav_slideshow) {
            selectItem(2);
        } else if (id == R.id.nav_manage) {
            selectItem(3);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
