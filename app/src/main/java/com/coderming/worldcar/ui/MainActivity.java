package com.coderming.worldcar.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coderming.worldcar.BuildConfig;
import com.coderming.worldcar.R;
import com.coderming.worldcar.model.AutowareTask;
import com.coderming.worldcar.model.UIUpdateHandler;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.location.LocationServices;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements UIUpdateHandler {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String DEFAULT_MASTER_IP = "192.168.0.196";
    public static final String LAST_MASTER_KEY = "LAST_MASTER_KEY";

    private static final String MAIN_FRAG = "com.coderming.main";
    private static final String SIDE_FRAG = "com.coderming.side";

    private static final int PERMISSIONS_LOCATION = 1;
//    private LocationServices mLocationServices;

    private WCMapFragmentWrapper mMapWrapper;
    private MediaFragment mMediaFragment;

    private TextView mAccel;
    private TextView mBrake;
    private TextView mTwistLinear;
    private TextView mTwistAnguar;
    private ImageView mSteel;

    private AutowareTask mAutowareTask;
    private String mMasterIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(getApplicationContext(), BuildConfig.MAPBOX_API_KEY);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBrake = (TextView) findViewById(R.id.auto_brake) ;
        mTwistLinear = (TextView) findViewById(R.id.auto_twist_linear);
        mTwistAnguar = (TextView) findViewById(R.id.auto_twist_angular);
        mAccel = (TextView) findViewById(R.id.driving_speed);
        mSteel = (ImageView) findViewById(R.id.auto_steer);

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

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        final int largeScreenWidth = (displaySize.x * 2) /3;                // large than2/3
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
                boolean isMap2LargeScreen = viewParent2.getWidth() > largeScreenWidth;
                mMediaFragment.switched(!isMap2LargeScreen);
                mMapWrapper.switched(isMap2LargeScreen);
            }
        });

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mMasterIp = pref.getString(LAST_MASTER_KEY, DEFAULT_MASTER_IP);
        mAutowareTask = new AutowareTask(this);
        mAutowareTask.execute(mMasterIp);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void updateAccel(int val) {
        mAccel.setText(Integer.toString(val));
    }

    @Override
    public void updateBrake(int val) {
        mBrake.setText(Integer.toString(val));
    }

    @Override
    public void updateSteer(int val) {
        mSteel.setRotation(val);
    }
    @Override
    public void updateTwist(double[] linear, double[] angular) {
        mTwistLinear.setText(Arrays.toString(linear));
        mTwistAnguar.setText(Arrays.toString(angular));
    }
}
