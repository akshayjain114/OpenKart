package com.example.sbarai.openkart;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sbarai.openkart.Adapters.RvProspectOrderAdapter;
import com.example.sbarai.openkart.Models.ProspectOrder;
import com.example.sbarai.openkart.Utils.FirebaseManager;
import com.firebase.geofire.GeoFire;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class OpenOrders extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    FloatingActionButton createProspectOrderMenu;
    FloatingActionButton testingButton;
    FloatingActionButton createSmartOrder;
    View createProspectOrderCard;
    Toolbar toolbar;
    public static FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    RecyclerView mRecyclerView;
    private RvProspectOrderAdapter adapter;
    GeoFire geoFire;
    static List<String> data = Collections.emptyList();
    double fetchRadiusInMiles = 0;
    boolean smartOrder = false;
    GeoQuery geoQuery;
    IndicatorSeekBar seekBar;
    SmoothProgressBar progressBar;
    public static Location location;
    private boolean hasDataLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        connectGoogleApiClients();
        defineVariables();
        setFABListeners();

        initVariables();
        setListeners();
        setRecyclerView();
//        executeOneTimeLocationListener();
        setRadiusSeekBar();
//        setRecyclerView();


    }

    private void connectGoogleApiClients() {
        if (checkIfGooglePlayServicesAreAvailable()) {
            //Get Access to the google service api
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            //Use Android Location Services
            //TODO:
        }
    }

    private boolean checkIfGooglePlayServicesAreAvailable() {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(errorCode,  this, 0).show();
            return false;
        }
        return true;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    private void setListeners() {
        createProspectOrderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateProspectOrderActivity();
            }
        });
    }

    private void executeOneTimeLocationListener() {
        Log.d("TAGG","ExecuteOneTimeLocationListener");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Does not have Location permission", Toast.LENGTH_SHORT).show();
            Log.d("TAGG","Does not have permission");
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("TAGG","Executed getLastLocation");
                            OpenOrders.location = location;
                            fetchData(location);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAGG","Location listener failed");
                executeOneTimeLocationListener();
            }
        });
    }



    private void initVariables() {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void defineVariables() {
        createProspectOrderMenu = findViewById(R.id.menu_item_1);
        createProspectOrderCard = findViewById(R.id.no_data_found);
        createSmartOrder = findViewById(R.id.menu_item_3);
        testingButton = findViewById(R.id.menu_item_2);
        mRecyclerView = findViewById(R.id.rv_open_orders);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geoFire = new GeoFire(FirebaseManager.getRefToGeofireForProspectOrders());
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setFABListeners() {
        createProspectOrderMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateProspectOrderActivity();
            }
        });

        createSmartOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateSmartOrderActivity();
            }
        });

        testingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    public void openCreateProspectOrderActivity() {
        startActivity(new Intent(OpenOrders.this, CreateProspectOrder.class));
    }

    public void openCreateSmartOrderActivity() {
        smartOrder=true;
        Intent orderIntent = new Intent(OpenOrders.this, CreateProspectOrder.class);
        orderIntent.putExtra("smart",smartOrder);
        startActivity(orderIntent);
    }

    public List<ProspectOrder> getData() {
        List<ProspectOrder> orders = new ArrayList<>();
        ProspectOrder order = new ProspectOrder();
        order.setDesiredStore("Walmart");
        ProspectOrder order2 = new ProspectOrder();
        order2.setDesiredStore("Cosco");
        ProspectOrder order3 = new ProspectOrder();
        order3.setDesiredStore("Apple bee");
        ProspectOrder order4 = new ProspectOrder();
        order4.setDesiredStore("NCSU store");
        ProspectOrder order5 = new ProspectOrder();
        order5.setDesiredStore("Walmart");

        orders.add(order);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);

        return orders;
    }

    public void fetchData(Location location) {
        Log.d("TAGG","fetchData called");
        data = new ArrayList<>();
//        totalKeysEntered = 0;
//        isGeoQueryReady = false;
        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), getKmFromMiles((float) fetchRadiusInMiles));
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
//                totalKeysEntered++;
                Log.d("TAGG", "onKeyEntered");
                insertIntoData(key);
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("TAGG", "onKeyExited");
                removeFromData(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//                isGeoQueryReady = true;
                progressBar.setVisibility(View.GONE);
                Log.d("TAGG", "Geoquery ready");
                adapter.dataSetChanged();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void removeFromData(String key) {
        adapter.removeFromData(key);
    }

    private void insertIntoData(String key) {
//        data.add(key);
        adapter.insertIntoData(key);
    }

    public void setRecyclerView() {
        Log.d("TAGG","setRecyclerView");
//        if (data == null){
//            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
//        }else if (data.size() == 0){
//            Log.d("TAGG","setRecyclerView - data size: " + data.size());
//        } else {
        adapter = new RvProspectOrderAdapter(this, data);
        adapter.setNoDataFound(findViewById(R.id.no_data_found));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        }
    }

    private void setRadiusSeekBar() {
        seekBar = findViewById(R.id.radius_seekbar);
        seekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                changeRadius(progressFloat);
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                recalculateSeekBarRange(seekBar);
            }
        });
        seekBar.setProgress((float) 1.5);
        changeRadius((float)1.5);
    }

    private void recalculateSeekBarRange(IndicatorSeekBar seekBar) {
        float currentValue = seekBar.getProgressFloat();
        float maxValue = seekBar.getMax();
        if (currentValue < 0.05 * maxValue) return;
        if (currentValue < 0.2 * maxValue) {
            seekBar.setMax((float) (maxValue*(3.0/5.0)));
        }
        if (currentValue > 0.8 * maxValue) {
            seekBar.setMax((float) (maxValue*(7.0/5.0)));

        }
        seekBar.setProgress(currentValue);
    }

    private void changeRadius(float progressFloat) {
        progressBar.setVisibility(View.VISIBLE);
        fetchRadiusInMiles = progressFloat;
        if (geoQuery != null)
            geoQuery.setRadius(getKmFromMiles(progressFloat));
        TextView radiusValue = findViewById(R.id.radius_value);
        String string = "" + progressFloat + " miles";
        radiusValue.setText(string);
    }

    private double getKmFromMiles(float progressFloat) {
        return progressFloat * 1.609344;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("TAGG","onConnected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        com.google.android.gms.location.LocationListener locationListener;
        locationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!hasDataLoaded){
                    hasDataLoaded = true;
                    executeOneTimeLocationListener();
                }
            }
        };
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,locationListener);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("TAGG","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("TAGG","onConnectionFailed");
    }
}
