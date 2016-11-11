package com.coderming.worldcar.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.coderming.worldcar.BuildConfig;
import com.coderming.worldcar.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String MAIN_FRAG = "com.coderming.main";
    private static final String SIDE_FRAG = "com.coderming.side";

    private static final int PERMISSIONS_LOCATION = 1;
//    private LocationServices mLocationServices;

    private WCMapFragmentWrapper mMapWrapper;
    private MediaFragment mMediaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, BuildConfig.MAPBOX_API_KEY);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
           // Build mapboxMap

            // Create map fragment
            mMapWrapper = new WCMapFragmentWrapper(MainActivity.this);
            // Add map fragment to parent container
            transaction.add(R.id.large_container, mMapWrapper.getMapFragment(), MAIN_FRAG);
            transaction.commit();

            mMediaFragment = new MediaFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.small_container, mMediaFragment, SIDE_FRAG)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();

       } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(MAIN_FRAG);
            mMapWrapper = new WCMapFragmentWrapper(MainActivity.this, fragment);
            mMediaFragment = (MediaFragment) getSupportFragmentManager().findFragmentByTag(SIDE_FRAG);
        }
        mMapWrapper.synchMap();

        LocationServices locationServices = LocationServices.getLocationServices(this);
        if (!locationServices.areLocationPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
        } else {
            locationServices.addLocationListener(mMapWrapper.locationListener);
        }
        // Enable GPS location tracking.
        locationServices.toggleGPS(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View mapView = mMapWrapper.getMapFragment().getView();
                View videoView = mMediaFragment.getView();
                ViewGroup viewParent1 = (ViewGroup) mapView.getParent();
                ViewGroup viewParent2 = (ViewGroup) videoView.getParent();
                viewParent1.removeView(mapView);
                viewParent2.removeView(videoView);
                viewParent1.addView(videoView);
                viewParent2.addView(mapView);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(LOG_TAG, "onRequestPermissionsResult: reqCode="+requestCode);
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationServices.getLocationServices(this).addLocationListener(mMapWrapper.locationListener);
            } else {
                // TODO: use dialogbox to display message
                // User denied location permission, user marker won't be shown.
                Toast.makeText(this,
                        "Enable location for example to work properly", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    // TODO: temporary,
    public void toggleDrivingMode(View view) {
        // change icon and text
    }

    public void toggleDrivingGear(View view) {
        // rotate icon and text
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "+++++ onStop");
    }
}
