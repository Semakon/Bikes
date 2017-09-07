package com.semakon.bikes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean start;

    private long lastTime = System.currentTimeMillis();
    private static final int ACCELEROMETER_DELAY = 500;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (start) {
                    for (Location location : locationResult.getLocations()) {
                        String time = "" + location.getTime();
                        String longitude = "Longitude: " + location.getLongitude();
                        String latitude = "Latitude: " + location.getLatitude();

                        TextView tvTime = (TextView) findViewById(R.id.text_view_time2);
                        TextView tvLongitude = (TextView) findViewById(R.id.text_view_longitude);
                        TextView tvLatitude = (TextView) findViewById(R.id.text_view_latitude);

                        tvTime.setText(time);
                        tvLongitude.setText(longitude);
                        tvLatitude.setText(latitude);

                        System.out.println("Time: " + time);
                        System.out.println(longitude);
                        System.out.println(latitude);
                    }
                }
            }
        };

        start = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mCurrentLocation = location;
                            }
                        }
                    });
        }
        Sensor mySensor = event.sensor;
        final long curTime = System.currentTimeMillis();

        if (curTime - lastTime < ACCELEROMETER_DELAY) {
            return;
        }
        lastTime = curTime;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && start) {
            float y = event.values[1];

            String strY = "y: " + y;
            String time = curTime + "";

            TextView tvTime = (TextView) findViewById(R.id.text_view_time);
            TextView tvY = (TextView) findViewById(R.id.text_view_y);

            tvTime.setText(time);
            tvY.setText(strY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1500);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /** Called when the button is pressed. */
    public void start(View view) {
        if (view instanceof Button) {
            Button button = (Button) view;
            if (start) {
                start = false;
                button.setText(R.string.button_start);
            } else {
                start = true;
                button.setText(R.string.button_stop);
            }
        }
        System.out.println("start: " + start);
    }

}
