package com.coderming.worldcar.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.coderming.worldcar.R;
import com.coderming.worldcar.model.PulseMarkerView;
import com.coderming.worldcar.model.PulseMarkerViewOptions;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.turf.TurfException;
import com.mapbox.services.commons.turf.TurfMeasurement;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.navigation.v5.RouteUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by linna on 11/1/2016.
 */

public class WCMapFragmentWrapper implements SwitchableView {
    private static final String LOG_TAG = WCMapFragmentWrapper.class.getSimpleName();

    private static final int ANIMATE_DURATION = 3000;
    private static final LatLng DefaultLatLng = new LatLng(-52.6885, -70.1395);

    private Context mContext;
    private SupportMapFragment mMapFragment;
    private MapboxMap mMap;

    private Location mLastLocation;
    private MarkerView carMarker;
    private AnimatorSet animatorSet;
    private Marker mDestMarker;

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private boolean bHasRoute;
    private boolean bInitiated;
//    private FloatingActionButton mFab;
    private LatLng[] mPoints;

    LatLng mDestination;
    private Handler mHandler;
    private Runnable mRunnable;
    private boolean mRouteFinished;
    private boolean mReRoute;
    private int mCount;
    private long mDistance;
    private RouteUtils routeUtils = new RouteUtils(0.001);      //1 meters off



    public WCMapFragmentWrapper(Context context, Fragment fragment) {
        mContext = context;
        assert (fragment instanceof SupportMapFragment);
        mMapFragment = (SupportMapFragment) fragment;
    }
    public WCMapFragmentWrapper(Context context) {
        mContext = context;
        mLastLocation = LocationServices.getLocationServices(context).getLastLocation();
        LatLng lastCoor = (mLastLocation != null) ? new LatLng(mLastLocation) : DefaultLatLng;

        MapboxMapOptions options = new MapboxMapOptions();
        options.styleUrl(Style.LIGHT);
        options.camera(new CameraPosition.Builder()
                .target(lastCoor)
                .zoom(9)
                .build());
        mMapFragment = SupportMapFragment.newInstance(options);
    }
    public SupportMapFragment getMapFragment() {
        return mMapFragment;
    }
    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v(LOG_TAG, "+++++locationListener: location="+location+", mMap="+mMap);
            if (location != null) {
                mLastLocation = location;
                if (mMap != null) {
                    initiate(mMap, mLastLocation);
                }
            }
        }
    };
    public void synchMap( ) {
        mMapFragment.getMapAsync( new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMap = mapboxMap;
                Log.v(LOG_TAG, "+++++getMapAsync: mLastLocation="+mLastLocation+", mMap="+mMap);
                if (mLastLocation != null)  {
                    initiate(mapboxMap, mLastLocation);
                }
            }});
    }
//    public void setFab(FloatingActionButton fab) {
//        mFab = fab;
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mRouteFinished = false;
//                mCount = 0;
//                simulateDriving();
//
//            }
//        });
//        mFab.setEnabled(false);
//    }
    private void initiate(MapboxMap mapboxMap, Location lastLocation ) {
        LatLng lastCoor = new LatLng(lastLocation);
        Log.v(LOG_TAG, "initiate: location.lat=" + lastCoor.getLatitude() + ", lng=" + lastCoor.getLongitude());
        if (!bInitiated) {
            mapboxMap.getMarkerViewManager().addMarkerViewAdapter(new PulseMarkerViewAdapter(mContext));
            carMarker = mapboxMap.addMarker(new PulseMarkerViewOptions()
                    .position(lastCoor)
                    .anchor(0.5f, 0.5f)
                    .flat(true));
            animateMarker(carMarker);
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCoor, 9));   // or 16 ?
            mapboxMap.setOnMapLongClickListener(myMapClickListener);
            bInitiated = true;
        }
    }
    private MapboxMap.OnMapLongClickListener myMapClickListener = new MapboxMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(@NonNull LatLng point) {
        try {
            if (mLastLocation != null) {
                mDestination = point;
                getRoute(new LatLng(mLastLocation), point);
            } else {
                Log.e(LOG_TAG, "onMapClick: unknown current position!");
            }
        } catch (ServicesException servicesException) {
            servicesException.printStackTrace();
        }
        }
    };
    private void getRoute(LatLng origin, final LatLng destination) throws ServicesException {
        client = new MapboxDirections.Builder()
                .setOrigin(Position.fromCoordinates(origin.getLongitude(), origin.getLatitude()))
                .setDestination(Position.fromCoordinates(destination.getLongitude(), destination.getLatitude()))
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(LOG_TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(LOG_TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(LOG_TAG, "No routes found");
                    return;
                }

                if (bHasRoute) {
                    removeDestination();
                }
                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Log.d(LOG_TAG, "Distance: " + currentRoute.getDistance());
//                Toast.makeText(
//                        mContext,
//                        "Route is " + currentRoute.getDistance() + " meters long.",
//                        Toast.LENGTH_SHORT).show();
                if (animatorSet != null) {
                    removeAnimation(carMarker);
                    animatorSet = null;
                }
                // Draw the route on the map
                String destDescription = String.format("Distance=%f miles, Duration=%f min",
                        (currentRoute.getDistance() * 0.000621371),
                        (currentRoute.getDuration() / 60));
                mDestMarker =  mMap.addMarker(new MarkerOptions()
                        .position(destination)
                        .title("Destination")
                        .snippet(destDescription));
                drawRoute(currentRoute);
                if (!bHasRoute) {
                    bHasRoute = true;
                    startSimulation();
                }

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(LOG_TAG, "Error: " + throwable.getMessage());
                Toast.makeText(mContext, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void removeDestination() {
        mMap.removeMarker(mDestMarker);
        List<Polyline> lines = mMap.getPolylines();
        for (Polyline pl : lines) {
            mMap.removePolyline(pl);
        }
    }
    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        mPoints = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            mPoints[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }
        int color = ContextCompat.getColor(mContext, R.color.map_route);
        mMap.addPolyline(new PolylineOptions()
                .add(mPoints)
                .color(color)
                .width(5));
    }
    private void simulateDriving( ) {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if ((mPoints.length - 1) > mCount) {
                    mDistance = (long) carMarker.getPosition().distanceTo(mPoints[mCount]) * 10;
                    ValueAnimator markerAnimator = ObjectAnimator.ofObject(carMarker, "position",
                            new LatLngEvaluator(), carMarker.getPosition(), mPoints[mCount]);
                    markerAnimator.setDuration(mDistance);
                    markerAnimator.setInterpolator(new LinearInterpolator());
                    markerAnimator.start();
                    mMap.getMarkerViewManager().scheduleViewMarkerInvalidation();
                    // Rotate the car (marker) to the correct orientation.
                    carMarker.setRotation((float) computeHeading(carMarker.getPosition(), mPoints[mCount]));
                    // Check that the vehicles off route or not. If you aren't simulating the car,
                    // and want to use this example in the real world, the checkingIfOffRoute method
                    // should go in a locationListener.
                    try {
                        checkIfOffRoute();
                    } catch (ServicesException | TurfException turfException) {
                        turfException.printStackTrace();
                        Log.e(LOG_TAG, "check if off route error: " + turfException.getMessage());
                    }
                    // Keeping the current point count we are on.
                    mCount++;
                    mHandler.postDelayed(this, mDistance);
                } else {
                    Log.v(LOG_TAG, "+++++ simulateDriving: finished ");
                    mRouteFinished = true;
                    Location loc = new Location(LocationManager.GPS_PROVIDER);
                    loc.setLatitude(mPoints[mCount-1].getLatitude());
                    loc.setLongitude(mPoints[mCount-1].getLongitude());
                    mLastLocation = loc;
                    animateMarker(carMarker);
                }
            }
        };
        mHandler.post(mRunnable);
    }
    private void startSimulation() {
        mRouteFinished = false;
        mCount = 0;
        simulateDriving();
    }
    private void stopSimulation() {
        if (mHandler != null) {
            if (mRunnable != null) {
                mHandler.removeCallbacks(mRunnable);
            }
        }
    }
    private void checkIfOffRoute() throws ServicesException, TurfException {
        Position carCurrentPosition =  Position.fromCoordinates(carMarker.getPosition().getLatitude(),
                carMarker.getPosition().getLongitude());
        // TODO currently making the assumption that only 1 leg in route exist.
        if (routeUtils.isOffRoute(carCurrentPosition, currentRoute.getLegs().get(0))) {
            // Display message to user and stop simulation.
//            Toast.makeText(mContext, "Off route", Toast.LENGTH_LONG).show();
            stopSimulation();
            // Reset our variables
            mReRoute = false;
            mCount = 0;
            // Get the route from car position to destination and begin simulating.
            getRoute(carMarker.getPosition(), mDestination);
        }
    } // End checkIfOffRoute
    private static double computeHeading(LatLng from, LatLng to) {
        // Compute bearing/heading using Turf and return the value.
        return TurfMeasurement.bearing(
                Position.fromCoordinates(from.getLongitude(), from.getLatitude()),
                Position.fromCoordinates(to.getLongitude(), to.getLatitude())
        );
    }

    @Override
    public void switched(boolean inLargeView) {
        // TODO:  mMap.setMaxZoom() acccording to Window size
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }
    private void animateMarker(MarkerView marker) {

        View view = mMap.getMarkerViewManager().getView(marker);
        if (view != null) {
            View backgroundView = view.findViewById(R.id.background_imageview);
            backgroundView.setVisibility(View.INVISIBLE);

            ValueAnimator scaleCircleX = ObjectAnimator.ofFloat(backgroundView, "scaleX", 1.8f);
            ValueAnimator scaleCircleY = ObjectAnimator.ofFloat(backgroundView, "scaleY", 1.8f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0f);

            scaleCircleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleCircleY.setRepeatCount(ValueAnimator.INFINITE);
            fadeOut.setRepeatCount(ObjectAnimator.INFINITE);

            animatorSet = new AnimatorSet();
            animatorSet.play(scaleCircleX).with(scaleCircleY).with(fadeOut);
            animatorSet.setDuration(ANIMATE_DURATION);
            animatorSet.start();
        }
    }
    private void removeAnimation( MarkerView marker ) {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        View view = mMap.getMarkerViewManager().getView(marker);
        if (view != null) {
            View backgroundView = view.findViewById(R.id.background_imageview);
            backgroundView.setVisibility(View.INVISIBLE);
        }
    }
    // Custom marker view used for pulsing the background view of marker.
    private static class PulseMarkerViewAdapter extends MapboxMap.MarkerViewAdapter<PulseMarkerView> {
        private LayoutInflater inflater;

        public PulseMarkerViewAdapter(@NonNull Context context) {
            super(context);
            this.inflater = LayoutInflater.from(context);
        }

        @Nullable
        @Override
        public View getView(@NonNull PulseMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.view_pulse_marker, parent, false);
                viewHolder.foregroundImageView = (ImageView) convertView.findViewById(R.id.foreground_imageView);
                viewHolder.backgroundImageView = (ImageView) convertView.findViewById(R.id.background_imageview);
                convertView.setTag(viewHolder);
            }
            return convertView;
        }
        private static class ViewHolder {
            ImageView foregroundImageView;
            ImageView backgroundImageView;
        }
    }
}
