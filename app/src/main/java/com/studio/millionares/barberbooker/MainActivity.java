package com.studio.millionares.barberbooker;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final int LOCATION_REQUEST = 101;
    private final int MAX_SPREAD_IN_METERS = 5000;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;

    DrawerLayout mainDrawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    EditText searchBar;
    CardView dateTimeSelector, searchItemsContainer;
    //TextView selectedTime;
    RecyclerView nearbySalonsList, searchItemsList;
    ImageButton menuBtn, clearSearchTxtBtn, searchBtn;
    RelativeLayout searchItemsListLayout;

    ActionBarDrawerToggle drawerToggle;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    Calendar mCalendar;
    Activity mActivity;

    HashMap<String, Integer> chosenDateAndTime;
    ArrayList<Salon> allSalons;

    SearchItemsListAdapter searchItemsListAdapter;
    NearbySalonsListAdapter nearbySalonsListAdapter;

    private Location customerCurrentLocation;
    private MarkerOptions customerCurrentLocationMarkerOptions;
    private Marker customerCurrentLocationMarker;
    ArrayList<String> unAllowedPermissions, permissionsRejected;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mainDrawerLayout = findViewById(R.id.main_drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        nearbySalonsList = findViewById(R.id.nearby_salons_list);
        dateTimeSelector = findViewById(R.id.date_time_selector);
        menuBtn = findViewById(R.id.menu_btn);
        searchBar = findViewById(R.id.search_inp);
        searchItemsListLayout = findViewById(R.id.search_items_list_layout);
        searchItemsContainer = findViewById(R.id.search_items_container);
        clearSearchTxtBtn = findViewById(R.id.clear_search_txt_btn);
        searchItemsList = findViewById(R.id.search_items_list);
        searchBtn = findViewById(R.id.search_btn);
        navigationView = findViewById(R.id.navigation_drawer);

        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.my_appointments:
                        mainDrawerLayout.closeDrawer(GravityCompat.START);
                        startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
                        break;
                    case R.id.log_out:
                        mainDrawerLayout.closeDrawer(GravityCompat.START);
                        firebaseAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                }

                return false;
            }
        });

        drawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, R.string.open, R.string.close);
        mainDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mActivity = this;
        chosenDateAndTime = new HashMap<>();

        allSalons = new ArrayList<>();

        searchItemsList.setHasFixedSize(true);
        LinearLayoutManager searchItemsListLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        searchItemsList.setLayoutManager(searchItemsListLinearLayoutManager);

        searchItemsListAdapter = new SearchItemsListAdapter(allSalons);
        searchItemsList.setAdapter(searchItemsListAdapter);

        nearbySalonsList.setHasFixedSize(true);
        LinearLayoutManager nearbySalonsListLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        nearbySalonsList.setLayoutManager(nearbySalonsListLinearLayoutManager);

        nearbySalonsListAdapter = new NearbySalonsListAdapter(this, allSalons);
        nearbySalonsList.setAdapter(nearbySalonsListAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestLocationPermission();

        buildGoogleApiClient();

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(20000);
//        mLocationRequest.setFastestInterval(10000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        mLocationCallback = new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if(locationResult == null){
//                    return;
//                }
//
//                for(Location location : locationResult.getLocations()){
//                    customerCurrentLocation = location;
//                    Toast.makeText(getApplicationContext(), customerCurrentLocation.getLatitude() + ", " + customerCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        };

        //startLocationUpdates();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestSingleUpdate(criteria, this, null);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0, 0.0f, (LocationListener) this);

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchBar.hasFocus()){
                    searchBar.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } else {
                    if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
                        mainDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mainDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            }
        });

        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    searchItemsListLayout.setVisibility(View.VISIBLE);
                    menuBtn.setImageResource(R.drawable.ic_back);
                    dateTimeSelector.setVisibility(View.GONE);
                    clearSearchTxtBtn.setVisibility(View.VISIBLE);
                    searchBtn.setVisibility(View.VISIBLE);

                    if(searchBar.getText().toString().isEmpty()){
                        searchItemsListAdapter.filterList(new ArrayList<Salon>());
                    }
                } else {
                    searchItemsListLayout.setVisibility(View.GONE);
                    menuBtn.setImageResource(R.drawable.ic_menu);
                    dateTimeSelector.setVisibility(View.VISIBLE);
                    clearSearchTxtBtn.setVisibility(View.GONE);
                    searchBtn.setVisibility(View.GONE);
                }
            }
        });



        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().isEmpty()){
                    searchItemsListAdapter.filterList(new ArrayList<Salon>());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()){
                    searchItemsListAdapter.filterList(new ArrayList<Salon>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    searchItemsListAdapter.filterList(new ArrayList<Salon>());
                } else {
                    filterSearch(s.toString());
                }

            }
        });

        clearSearchTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searchBar.getText().toString().isEmpty()){
                    String searchStr = searchBar.getText().toString();
                    if(mMap != null){
                        searchBar.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        mMap.clear();

                        if(customerCurrentLocationMarkerOptions != null){
                            mMap.addMarker(customerCurrentLocationMarkerOptions);
                        }

                        ArrayList<Salon> filteredSalons = new ArrayList<>();

                        for(Salon salon : allSalons){
                            HashMap<String, Object> salonDetails = salon.getSalonDetails();
                            if(salonDetails.get("name").toString().toLowerCase().contains(searchStr.toLowerCase()) || salonDetails.get("city").toString().toLowerCase().contains(searchStr.toLowerCase())){
                                filteredSalons.add(salon);
                            }
                        }

                        nearbySalonsListAdapter.filterList(filteredSalons);

                        for(Salon salon : filteredSalons){
                            LatLng salonLoc = (LatLng) salon.getSalonDetails().get("location");
                            mMap.addMarker(new MarkerOptions().position(salonLoc).title(salon.getSalonDetails().get("name").toString()));
                        }
                    }


                }
            }
        });

        dateTimeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar = Calendar.getInstance();
                new DatePickerDialog(mActivity, mDateSet, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        //startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        firebaseDatabase.child("Salons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(allSalons.size() == 0){
                    for(DataSnapshot salon : dataSnapshot.getChildren()){

                        String id = "", name = "", city = "", rating = "", address = "";
                        double lat = 0, lon = 0;

                        id = salon.getKey();
                        for(DataSnapshot details : salon.getChildren()){
                            if(details.getKey().equals("name")){
                                name = details.getValue().toString();
                            } else if (details.getKey().equals("address")) {
                                for(DataSnapshot addressDetails : details.getChildren()){
                                    if(addressDetails.getKey().equals("city")){
                                        city = addressDetails.getValue().toString();
                                    } else if(addressDetails.getKey().equals("simpleAddress")){
                                        address = addressDetails.getValue().toString();
                                    }
                                }
                            } else if(details.getKey().equals("location")){
                                for(DataSnapshot coordinates : details.getChildren()){
                                    if(coordinates.getKey().equals("latitude")){
                                        lat = (double) coordinates.getValue();
                                    } else if(coordinates.getKey().equals("longitude")){
                                        lon = (double) coordinates.getValue();
                                    }
                                }
                            } else if(details.getKey().equals("rating")){
                                rating = details.getValue().toString();
                            }
                        }

                        LatLng location = new LatLng(lat, lon);

                        allSalons.add(new Salon(id, name, address, city, rating,"", location));
                    }

                    sortByDistance(allSalons);
                    searchItemsListAdapter.notifyDataSetChanged();
                    nearbySalonsListAdapter.notifyDataSetChanged();
                }

                for(Salon salon : allSalons){
                    LatLng salonLoc = (LatLng) salon.getSalonDetails().get("location");

                    float[] results = new float[3];
                    Location.distanceBetween(customerCurrentLocation.getLatitude(), customerCurrentLocation.getLongitude(), salonLoc.latitude, salonLoc.longitude, results);
                    float distBtwPoints = results[0];
                    if(distBtwPoints <= MAX_SPREAD_IN_METERS){
                    mMap.addMarker(new MarkerOptions().position(salonLoc).title(salon.getSalonDetails().get("name").toString()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                //v.getGlobalVisibleRect(outRect);
                int[] containerLoc = new int[2];
                searchItemsContainer.getLocationOnScreen(containerLoc);

                int bottom = containerLoc[1] + searchItemsContainer.getHeight();

                outRect.set(0, 0, searchItemsListLayout.getRight(), bottom);

                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private final DatePickerDialog.OnDateSetListener mDateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            chosenDateAndTime.put("year", year);
            chosenDateAndTime.put("month", month);
            chosenDateAndTime.put("day", dayOfMonth);

            new TimePickerDialog(mActivity, mTimeSet, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false).show();
        }
    };

    private final TimePickerDialog.OnTimeSetListener mTimeSet = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);

            chosenDateAndTime.put("hour", hourOfDay);
            chosenDateAndTime.put("minute", minute);

            String dateAndTime = chosenDateAndTime.get("day") + "/" +
                    chosenDateAndTime.get("month") +
                    "/" + chosenDateAndTime.get("year") +
                    ", " + chosenDateAndTime.get("hour") +
                    ":" + chosenDateAndTime.get("minute");

            Toast.makeText(mActivity, dateAndTime, Toast.LENGTH_LONG).show();
        }
    };

    private void filterSearch(String queryString){
        ArrayList<Salon> filteredSalons = new ArrayList<>();

        for(Salon salon : allSalons){
            HashMap<String, Object> salonDetails = salon.getSalonDetails();
            if(salonDetails.get("name").toString().toLowerCase().contains(queryString.toLowerCase()) || salonDetails.get("city").toString().toLowerCase().contains(queryString.toLowerCase())){
                filteredSalons.add(salon);
            }
        }

        searchItemsListAdapter.filterList(filteredSalons);
    }

    private void sortByDistance(ArrayList<Salon> allSalons){
        LatLng customerCurrentLatLng = new LatLng(customerCurrentLocation.getLatitude(), customerCurrentLocation.getLongitude());

        for(int i = 1; i < allSalons.size(); i++){
            Salon key = allSalons.get(i);
            int j = i - 1;

            LatLng salonLatLng = (LatLng) key.getSalonDetails().get("location");

            while((j >= 0) && (Float.compare(getDistBetween(customerCurrentLatLng, (LatLng) allSalons.get(j).getSalonDetails().get("location")), getDistBetween(customerCurrentLatLng, salonLatLng)) > 0)){
                allSalons.set(j + 1, allSalons.get(j--));
            }
            allSalons.set(j + 1, key);
        }
    }

    private float getDistBetween(LatLng start, LatLng end){
        float[] results = new float[3];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        float distBtwPoints = results[0];

        return distBtwPoints;
    }

    private void requestLocationPermission(){
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        unAllowedPermissions = new ArrayList<>();

        for(String permission : permissions){
            if(!isPermissionAlreadyGranted(permission)){
                unAllowedPermissions.add(permission);
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(unAllowedPermissions.size() > 0){
                requestPermissions(unAllowedPermissions.toArray(new String[unAllowedPermissions.size()]), LOCATION_REQUEST);
            }
        }

    }

    private  boolean isPermissionAlreadyGranted(String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST){
            permissionsRejected = new ArrayList<>();
            for (String perm : unAllowedPermissions) {
                if (!isPermissionAlreadyGranted(perm)) {
                    permissionsRejected.add(perm);
                }
            }

            if (permissionsRejected.size() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                        new AlertDialog.Builder(MainActivity.this).
                                setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(permissionsRejected.
                                                    toArray(new String[permissionsRejected.size()]), LOCATION_REQUEST);
                                        }
                                    }
                                }).setNegativeButton("Cancel", null).create().show();

                        return;
                    }
                }

            } else {
                if (googleApiClient != null) {
                    googleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        customerCurrentLocation = location;

        firebaseDatabase.child("Salons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(allSalons.size() == 0){
                    for(DataSnapshot salon : dataSnapshot.getChildren()){

                        String id = "", name = "", city = "", rating = "", address = "";
                        double lat = 0, lon = 0;

                        id = salon.getKey();
                        for(DataSnapshot details : salon.getChildren()){
                            if(details.getKey().equals("name")){
                                name = details.getValue().toString();
                            } else if (details.getKey().equals("address")) {
                                for(DataSnapshot addressDetails : details.getChildren()){
                                    if(addressDetails.getKey().equals("city")){
                                        city = addressDetails.getValue().toString();
                                    } else if(addressDetails.getKey().equals("simpleAddress")){
                                        address = addressDetails.getValue().toString();
                                    }
                                }
                            } else if(details.getKey().equals("location")){
                                for(DataSnapshot coordinates : details.getChildren()){
                                    if(coordinates.getKey().equals("latitude")){
                                        lat = (double) coordinates.getValue();
                                    } else if(coordinates.getKey().equals("longitude")){
                                        lon = (double) coordinates.getValue();
                                    }
                                }
                            } else if(details.getKey().equals("rating")){
                                rating = details.getValue().toString();
                            }
                        }

                        LatLng location = new LatLng(lat, lon);

                        allSalons.add(new Salon(id, name, address, city, rating,"", location));
                    }

                    sortByDistance(allSalons);
                    searchItemsListAdapter.notifyDataSetChanged();
                    nearbySalonsListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LatLng currentLoc = new LatLng(customerCurrentLocation.getLatitude(), customerCurrentLocation.getLongitude());
        customerCurrentLocationMarkerOptions = new MarkerOptions().position(currentLoc).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker));
        if(customerCurrentLocationMarker != null){
            customerCurrentLocationMarker.setPosition(currentLoc);
        } else {
            customerCurrentLocationMarker = mMap.addMarker(customerCurrentLocationMarkerOptions);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
