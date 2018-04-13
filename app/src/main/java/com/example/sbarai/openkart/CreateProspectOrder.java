package com.example.sbarai.openkart;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import com.example.sbarai.openkart.Utils.Constants;
import com.example.sbarai.openkart.Models.ProspectOrder;
import com.example.sbarai.openkart.Utils.FirebaseManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.Inflater;

public class CreateProspectOrder extends AppCompatActivity
    implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    Toolbar toolbar;
    GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Activity thisActivity;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener locationListener;
    private Marker mCurrLocationMarker;
    Location lastLoc;
    TextView orderDateText;
    long orderDate;
    int mYear, mDate, mMonth;

    Button submitButton;
    EditText desiredStore;
    EditText colabRadius;
    EditText targetTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_prospect_order);
        thisActivity = this;

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        initVariables();
        setViewActions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            checkGPS();
        }
    }

    public void initVariables(){
        orderDateText = findViewById(R.id.order_date);
        submitButton = findViewById(R.id.submitButton);
        colabRadius = findViewById(R.id.colabRadius);
        targetTotal = findViewById(R.id.targetTotal);
        desiredStore = findViewById(R.id.desiredStore);
    }

    private void setViewActions() {
        orderDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mCurrentDate = Calendar.getInstance();
                mYear = mCurrentDate.get(Calendar.YEAR);
                mMonth = mCurrentDate.get(Calendar.MONTH);
                mDate = mCurrentDate.get(Calendar.DATE);
                orderDate = LocalDate.of(mYear,mMonth + 1,mDate).atStartOfDay().atZone(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli();
                orderDateText.setText(LocalDate.of(mYear,mMonth + 1,mDate).toString());

                DatePickerDialog mDatePicker = new DatePickerDialog(CreateProspectOrder.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR,selectedYear);
                        myCalendar.set(Calendar.MONTH,selectedMonth);
                        myCalendar.set(Calendar.DATE,selectedDay);
                        String myFormat = "MM/dd/yy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        orderDateText.setText(sdf.format(myCalendar.getTime()));

                        mDate = selectedDay;
                        mMonth = selectedMonth;
                        mYear = selectedYear;
                        orderDate = LocalDate.of(mYear,mMonth + 1,mDate).atStartOfDay().atZone(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli();
                        orderDateText.setText(LocalDate.of(mYear,mMonth + 1,mDate).toString());
                    }
                }, mYear, mMonth, mDate);
                mDatePicker.show();

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    submitProspectOrder();

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(thisActivity, "Error: Please check the order details", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void submitProspectOrder() {
        ProspectOrder prospectOrder = new ProspectOrder();
        prospectOrder.dateTime = ServerValue.TIMESTAMP;
        prospectOrder.setLocation(lastLoc);
        prospectOrder.setDesiredStore(desiredStore.getText().toString());
        prospectOrder.setColabRadius(Float.parseFloat(colabRadius.getText().toString()));
        prospectOrder.setTargetTotal(Float.parseFloat(targetTotal.getText().toString()));
        prospectOrder.setOrderDate(orderDate);
        prospectOrder.setStatus(Constants.ProspectOrder.STATUS_ACTIVE);
        try {
            prospectOrder.setCreatorKey(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }catch (Exception e){
            Toast.makeText(thisActivity, "Error: user does not exist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isProspectOrderValid(prospectOrder)){
            DatabaseReference ref = FirebaseManager.getRefToProspectOrders();
            ref = ref.push();
            new GeoFire(FirebaseManager.getRefToGeofireForProspectOrders()).setLocation(ref.getKey(),new GeoLocation(prospectOrder.getLocLat(),prospectOrder.getLocLon()));
            ref.setValue(prospectOrder).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(thisActivity, "Submitted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            //For notifications!
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            sendRegistrationToServer(refreshedToken, ref.getKey());

        }
    }

    //Maintaining a mapping of order key and creator's token to send notification when target amount is reached
    private void sendRegistrationToServer(String refreshedToken, String key) {
        //TODO: Send data to server to maintain a mapping
    }

    private boolean isProspectOrderValid(ProspectOrder prospectOrder) {
        if (prospectOrder.getDesiredStore() == null || prospectOrder.getDesiredStore().equals("")){
            Toast.makeText(thisActivity, "Please enter Store name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (prospectOrder.getLocation() == null){
            Toast.makeText(thisActivity, "Error fetching location. Please try again", Toast.LENGTH_LONG).show();
            return false;
        }
        if (prospectOrder.getColabRadius() == 0){
            Toast.makeText(thisActivity, "Error: Collaboration radius cannot be zero.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (prospectOrder.getTargetTotal() == 0){
            Toast.makeText(thisActivity, "Error: Target total cannot be zero", Toast.LENGTH_LONG).show();
            return false;
        }
        if (prospectOrder.getOrderDate() < LocalDate.now().atStartOfDay().atZone(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli()){
            Toast.makeText(thisActivity, "Error: Please select current or future date", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CreateProspectOrder.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                    checkGPS();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void checkGPS(){
//        Toast.makeText(thisActivity, "isLocationEnabled: " + isLocationEnabled(), Toast.LENGTH_SHORT).show();
        if (!isLocationEnabled()){
            new AlertDialog.Builder(this)
                    .setTitle("Please enable GPS")
                    .setMessage("This app requires GPS to work as intended, please enable to continue")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent gpsOptionsIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public Boolean isLocationEnabled(){
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(thisActivity, "onConnected", Toast.LENGTH_SHORT).show();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLoc = location;
//                Toast.makeText(thisActivity, "onLocationChanged called", Toast.LENGTH_SHORT).show();
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mMap.addMarker(markerOptions);

                //move map camera
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
            }
        };
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,locationListener);
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,  this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "ERROR: Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "ERROR: Connection failed", Toast.LENGTH_SHORT).show();
    }

}
