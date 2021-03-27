package com.marvel.marvel;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.marvel.marvel.MyUtils.counter;
import static com.marvel.marvel.MyUtils.isOnline;

public class MainActivity extends AppCompatActivity
        implements
        EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks
        , MainListAdapter.ImageClickListener,NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;

    private static final int RC_STORAGE_PERMISSIONS = 123;
    private DatabaseReference mDatabase;
    private RecyclerView rc;
    private TextView tv_warning;
    private boolean isFirstTime = false;
    private String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AdView mAdView;
    private String url = "";
    private SpinKitView spinKitView;
    Toolbar toolbar;
    private AlertDialog.Builder builder,builder2;

    private BroadcastReceiver networkBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isOnline(context)) {
                tv_warning.setVisibility(View.GONE);
                spinKitView.setVisibility(View.VISIBLE);
                isFirstTime = false;
                loadBanner();
                loadData();
            } else {
                if(isFirstTime) {
                    spinKitView.setVisibility(View.GONE);
                    tv_warning.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        isFirstTime = true;
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        spinKitView = findViewById(R.id.spin_kit);
        tv_warning = findViewById(R.id.no_net);
        setSupportActionBar(toolbar);
        setupNavigationDrawerMenu();
        setNavigationViewListener();

        MobileAds.initialize(this,
                getResources().getString(R.string.app_id));

        spinKitView.setVisibility(View.VISIBLE);

        mAdView = findViewById(R.id.adView);
        loadBanner();

        rc = findViewById(R.id.main_list);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Wallpapers");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

        registerReceiver(networkBroadCast, intentFilter);


    }

    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    private void setupNavigationDrawerMenu() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadData() {
        final ArrayList<String> urls = new ArrayList<>();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    urls.add(dataSnapshot1.getValue().toString());
                }

                rc.setAdapter(new MainListAdapter(MainActivity.this, urls));
                rc.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                spinKitView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("lksjdf", databaseError.toString());
                spinKitView.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(networkBroadCast);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EasyPermissions.hasPermissions(this, perms)) {
            requestPermissions();
        }
        if (!isOnline(this)) {
            if (isFirstTime)
            {
                tv_warning.setVisibility(View.VISIBLE);
            }
        }


    }

    private void requestPermissions() {
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, RC_STORAGE_PERMISSIONS, perms)
                        .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Toast.makeText(MainActivity.this, "Expanded", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Toast.makeText(MainActivity.this, "Collapsed", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
        SearchView searchView =(SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Search...");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "mistersumandey@gmail.com"));
                 intent.putExtra(Intent.EXTRA_SUBJECT,"feedback");
                 intent.putExtra(Intent.EXTRA_TEXT,"Sir,");
                 startActivity(intent);
                break;

            case R.id.nav_exit:
                new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            MainActivity.this.finish();

                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;


            case R.id.nav_share:
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(Intent.createChooser(s,"Share"));
                break;



        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (!EasyPermissions.hasPermissions(this, this.perms)) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this).build().show();
            }
            if (EasyPermissions.somePermissionDenied(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions();
            }
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (!(resultCode == RESULT_OK)) {
                new AppSettingsDialog.Builder(this).build().show();
            }
        }
    }

    @Override
    public void imageClicked(String url) {
        this.url = url;
        counter++;
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("image_url", url);
        startActivity(intent);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.about:

                //edit this
                builder2 = new AlertDialog.Builder(this);
                View customLayout2 = getLayoutInflater().inflate(R.layout.custom_layout2,null);
                TextView link = customLayout2.findViewById(R.id.fourth);
                link.setMovementMethod(LinkMovementMethod.getInstance());
                builder2.setView(customLayout2);
                builder2.show();
                break;
            case R.id.nav_rate_us:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName())));
                break;

            case R.id.privacy:
                builder = new AlertDialog.Builder(this);
                View customLayout = getLayoutInflater().inflate(R.layout.custom_layout,null);
                builder.setView(customLayout);
                builder.show();
                break;
            case R.id.nav_exit:
                new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            MainActivity.this.finish();

                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
           //case R.id.nav_more:
            //    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Suman Dey")));
             //  break;

            case R.id.nav_share:
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(Intent.createChooser(s,"Share"));
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
}
}
