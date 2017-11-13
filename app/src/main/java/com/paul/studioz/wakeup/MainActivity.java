package com.paul.studioz.wakeup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    WifiManager wifi;
    CoordinatorLayout coordLayout;
    TextView wifiInfo;
    Button wifiToggle, wifiMore;
    boolean scanningWifi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Looking for devices", Snackbar.LENGTH_LONG).show();

            }
        });
        coordLayout = (CoordinatorLayout) findViewById(R.id.coord_layout);
        wifiInfo = (TextView) findViewById(R.id.connection_info);
        wifiToggle = (Button) findViewById(R.id.wifi_toggle_btn);
        wifiToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleWIFI();
        }
        });
        wifiMore = (Button) findViewById(R.id.wifi_details_btn);
        wifiMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifi != null)
                    wifi.startScan();
                scanningWifi = true;
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //permission for location
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("MainActivity", "Permission denied");
            makeRequest();
        }

        setupWifi();
        updateWifiInfo();

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, intentFilter);

        super.onResume();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id  = item.getItemId();

        if (id == R.id.nav_devices) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void makeRequest(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
    }

    public void setupWifi(){
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


    }

    public void toggleWIFI(){

        if(wifi != null)
            wifi.setWifiEnabled(!wifi.isWifiEnabled());
        else
            Snackbar.make(coordLayout, "Unable to access WiFi", Snackbar.LENGTH_LONG).show();
    }

    public void updateWifiInfo(){       //update UI with WIFI info
        if (wifi.isWifiEnabled())
            wifiToggle.setText("Disable");
        else
            wifiToggle.setText("Enable");

        if(!wifi.isWifiEnabled())
            wifiInfo.setText("WiFi connectivity is disabled");
        else{
            if(wifi.getConnectionInfo().getNetworkId() == -1)
                wifiInfo.setText("WiFi connections available");
            else
                wifiInfo.setText(String.format("Connected to %s", wifi.getConnectionInfo().getSSID()));
        }

    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState state =  intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    switch(state){
                        case COMPLETED:
                            updateWifiInfo();
                            break;
                        case DISCONNECTED:
                            updateWifiInfo();
                            break;
                    }
                    updateWifiInfo();
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    if(scanningWifi) {
                        List<ScanResult> results = wifi.getScanResults();
                        Collections.sort(results, new Comparator<ScanResult>() {
                            @Override
                            public int compare(ScanResult o1, ScanResult o2) {
                                return -(Integer.compare(o1.level, o2.level));
                            }
                        });

                        StringBuilder sb = new StringBuilder();
                        sb.append("Available networks:\n");
                        for (ScanResult result : results) {
                            sb.append(result.SSID + "  ");
                            sb.append(result.level);
                            sb.append("\n");
                        }
                        wifiInfo.append("\n" + sb);
                        scanningWifi = false;
                    }
                    break;

            }
        }
    };



}
