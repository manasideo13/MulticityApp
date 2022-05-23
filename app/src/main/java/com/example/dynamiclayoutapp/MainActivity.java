package com.example.dynamiclayoutapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.dynamiclayoutapp.adapter.AutoCompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
        {

    /* google map */
    GoogleMap mMap;
    Marker pickupMarker, dropMarker;
    private Polyline mPolyline;


    Context mContext;
    GoogleApiClient mGoogleApiClient;
    ImageView imgAdd, imgAddClear;
    LinearLayout linearDynamic;
    ImageView imageMarker;
    AutoCompleteTextView autoCompleteTextViewPickup, autoCompleteTextViewDrop;
    AutocompletePrediction item;
    AutoCompleteAdapter adapter;
    PlacesClient placesClient;
    AppCompatButton btnFindCar, btnConfirm, btnCurrentLocation,btnLocationOnMap;
    LinearLayout linearFooterButtons,btnViewCabRoundTrip, linearBtnConfirm;

    double convertedDistance = 0.0, currentLatitude = 0.0, currentLongitude = 0.0, pickup_lat = 0.0,
            pickup_lng = 0.0, drop_lat = 0.0, drop_lng = 0.0;

    boolean isCurrent = false, isDDSelected = false, isLocated = false, isCameraMove = false,isNetworkAvailable = false;

    String address = "", city = "", pickup_city = "", drop_city = "", pickup_address = "", drop_address = "";

    private LatLng mCenterLatLong, mOrigin, mDestination;

    // location
    LocationManager locationManager;
    String latitude, longitude;
    private static final int REQUEST_LOCATION = 1;

    FusedLocationProviderClient fusedLocationProviderClient;
    boolean isPickup = false, isDrop = false, isBtnCurrent = false, isOnCreate = false;

            ArrayList<LatLng> pickupLocationList = new ArrayList<>();
            ArrayList<LatLng> dropLocationList = new ArrayList<>();


    double lat ,longi;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearDynamic = findViewById(R.id.linearDynamic);
        imgAdd = findViewById(R.id.imgAdd);
        imgAddClear = findViewById(R.id.imgAddClear);


        btnFindCar = findViewById(R.id.btnFindCar);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnLocationOnMap = findViewById(R.id.btnLocationOnMap);
        linearFooterButtons = findViewById(R.id.linearFooterButtons);
        linearBtnConfirm = findViewById(R.id.linearBtnConfirm);
        btnViewCabRoundTrip = findViewById(R.id.btnViewCabRoundTrip);
        imageMarker = findViewById(R.id.imageMarker);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
       /* txtDropDatePicker = view.findViewById(R.id.txtDropDatePicker);
        pickup_spinner_time = view.findViewById(R.id.pickup_spinner_time);
        txtPickupDatePicker = view.findViewById(R.id.txtPickupDatePicker);*/


        autoCompleteTextViewPickup = findViewById(R.id.auto_pickup);
        autoCompleteTextViewDrop = findViewById(R.id.auto_destination);

        String apiKey = getString(R.string.google_maps_key);
        if (apiKey.isEmpty()) {
            autoCompleteTextViewPickup.setText(getString(R.string.error));
            return;
        }
        /*initialized place sdk*/
        if (!Places.isInitialized()) {
            //Log.d("TAG", "key = " + R.string.google_maps_key);
            Places.initialize(this, apiKey);
        }

        /*create new place client instance*/
        placesClient = Places.createClient(this);
        //Log.d("TAG", "placesClient value = " + placesClient);
        adapter = new AutoCompleteAdapter(this, placesClient);

        /* pickup */
        autoCompleteTextViewPickup.setOnItemClickListener(autocompleteClickListener);
        autoCompleteTextViewPickup.setAdapter(adapter);

        /* drop */
        autoCompleteTextViewDrop.setOnItemClickListener(autocompleteClickListener);
        autoCompleteTextViewDrop.setAdapter(adapter);


        mContext = this;
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                isOnCreate = true;
                getLocation();
        }






        /*click events of autocomplete pickup */
        autoCompleteTextViewPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //isLocated = false;
                autoCompleteTextViewPickup.setSelectAllOnFocus(true);
            }
        });

        autoCompleteTextViewPickup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isPickup = true;
                isDrop = false;
                isLocated = false;

                Log.d("TAG", isPickup + " " + isDrop);

                autoCompleteTextViewPickup.setSelectAllOnFocus(true);
                autoCompleteTextViewPickup.selectAll();
                linearFooterButtons.setVisibility(View.VISIBLE);
                btnCurrentLocation.setVisibility(View.VISIBLE);
                linearBtnConfirm.setVisibility(View.GONE);
                btnViewCabRoundTrip.setVisibility(View.GONE);

                return false;
            }
        });

        autoCompleteTextViewPickup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 15) {
                    autoCompleteTextViewPickup.dismissDropDown();
                }
            }
        });

        /* drop events */


        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgAdd.setVisibility(View.GONE);
                imgAddClear.setVisibility(View.VISIBLE);
                addView();
            }
        });

        //current location button

        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    isBtnCurrent = true;
                    pickupLocationList.clear();
                    getLocation();
                }
                hideKeyboardFrom(MainActivity.this,autoCompleteTextViewPickup);
                linearFooterButtons.setVisibility(View.GONE);
               // linearBtnConfirm.setVisibility(View.VISIBLE);
            }
        });

        btnLocationOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageMarker.setVisibility(View.VISIBLE);
                linearFooterButtons.setVisibility(View.GONE);
                linearBtnConfirm.setVisibility(View.VISIBLE);

                if (isPickup)
                {
                    isCameraMove = true;
                    hideKeyboardFrom(MainActivity.this, autoCompleteTextViewPickup);
                } else {
                    hideKeyboardFrom(MainActivity.this, autoCompleteTextViewDrop);
                }

                if (!isLocated) {
                    CameraChange();
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //not change camera position
                imageMarker.setVisibility(View.GONE);
                linearBtnConfirm.setVisibility(View.GONE);

                //set current location
                isCameraMove = false;
                pickupMarker.remove();

                DrawMarker(pickup_lat, pickup_lng, pickup_city);
                Log.d("TAG", "new Latlng = " + pickup_lat + " " + pickup_lng);

                if (pickupLocationList.size() > 0) {
                    pickupLocationList.set(0, new LatLng(pickup_lat, pickup_lng));
                    Log.d("TAG", "size update pickup = " + pickupLocationList.size());
                } else {
                    Log.d("TAG", "pickup size = " + pickupLocationList.size());
                }
            }
        });
    }

            /* after moving marker on map location changes and then display that location on textview */
            public void CameraChange()
            {
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(@NonNull CameraPosition cameraPosition)
                    {

                        if(!isLocated)
                        {
                            Log.d("Camera position change " + "", cameraPosition + "");
                            if(isCameraMove){
                                mCenterLatLong = cameraPosition.target;
                            }


                           // isCameraMove = false;

                            try
                            {
                                Location mLocation = new Location("");
                                mLocation.setLatitude(mCenterLatLong.latitude);
                                mLocation.setLongitude(mCenterLatLong.longitude);

                                if(isPickup)
                                {
                                    pickup_lat = mCenterLatLong.latitude;
                                    pickup_lng = mCenterLatLong.longitude;

                                    if(pickup_lat != 0.0 && pickup_lng != 0.0)
                                    {
                                        getAddressFromCurrentLocation(pickup_lat, pickup_lng);
                                    }
                                }
                                else
                                {
                                    drop_lat = mCenterLatLong.latitude;
                                    drop_lng = mCenterLatLong.longitude;

                                    if(drop_lat != 0.0 && drop_lng != 0.0)
                                    {
                                        getAddressFromCurrentLocation(drop_lat, drop_lng);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Log.d("TAG", "is located true");
                        }
                    }
                });
            }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    pickup_lat = location.getLatitude();
                    pickup_lng= location.getLongitude();
                    Log.d("TAG", "Location" + pickup_lat + "" + pickup_lng);
                    getAddressFromCurrentLocation(pickup_lat, pickup_lng);
                    pickupLocationList.add(new LatLng(pickup_lat, pickup_lng));
                    Log.d("TAG","list value"+pickupLocationList.toString());

                }
            });
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
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
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        pickup_lat = location.getLatitude();
                        pickup_lng= location.getLongitude();
                        Log.d("TAG", "Location" + pickup_lat + "" + pickup_lng);
                        getAddressFromCurrentLocation(pickup_lat, pickup_lng);
                        pickupLocationList.add(new LatLng(pickup_lat, pickup_lng));

                        Log.d("TAG","list value"+pickupLocationList.toString());

                    }
                });


            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    //for getting address from latitude and langitude
    public void getAddressFromCurrentLocation(Double latitude, Double longitude) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = null;
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();


            isPickup = true;

            if(isBtnCurrent){
                pickupMarker.remove();
                DrawMarker(latitude,longitude,city);
                autoCompleteTextViewPickup.setText(address);
                isBtnCurrent = false;
            }else if(isOnCreate) {
                DrawMarker(latitude, longitude, city);
                autoCompleteTextViewPickup.setText(address);
                isOnCreate = false;
            }else if (isCameraMove){
                autoCompleteTextViewPickup.setText(address);
                Toast.makeText(mContext, "Called", Toast.LENGTH_SHORT).show();

            }



        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

            /* draw marker depends on flag */
            public void DrawMarker(Double latitude, Double longitude, String city)
            {
                if(isPickup)
                {

                    pickupMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(city)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                            (new LatLng(latitude, longitude), 10.0f));
                }
                else if(isDrop)
                {
                    dropMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(city)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                            (new LatLng(latitude, longitude), 10.0f));
                }
                else
                {
                    Log.d("TAG", "current location");
                }
            }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("TAG", "OnMapReady");
        if (mMap != null) {
            mMap.clear();
            autoCompleteTextViewDrop.setText("");
        }
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
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
        mMap.setMyLocationEnabled(true);
    }

    /* hide keyboard */
    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /* autocompletetextview listener for drop down places list display */
    public AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                item = adapter.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

                /* To specify which data types to return, pass an array of Place.
                Fields in your FetchPlaceRequest
                Use only those fields which are required.*/
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FetchPlaceResponse> task)
                        {
                            if(task.getResult() != null)
                            {
                                FetchPlaceResponse places = task.getResult();
                                final Place place = places.getPlace();
                                Log.d("TAG", "places = " + place.getAddress());
                                Log.d("TAG", "places = " + place.getLatLng());


                                pickup_lat = places.getPlace().getLatLng().latitude;
                                pickup_lng = places.getPlace().getLatLng().longitude;
                                pickup_address = places.getPlace().getAddress();

                                /* if pickup flag is true (selected from drop down location) */

                                if(pickupLocationList.size() > 0 ){
                                   // pickupLocationList.clear();
                                    pickupLocationList.set(0, new LatLng(pickup_lat,pickup_lng));
                                    pickupMarker.remove();
                                    DrawMarker(pickup_lat,pickup_lng,pickup_address);
                                    hideKeyboardFrom(MainActivity.this,autoCompleteTextViewPickup);
                                    linearFooterButtons.setVisibility(View.GONE);
                                }
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Please check your internet connection.!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            autoCompleteTextViewPickup.setText(e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };




    public void addView()
    {
        final View view = getLayoutInflater().inflate(R.layout.item_add_layout, null);
        AutoCompleteTextView addAutoDestination = view.findViewById(R.id.addAutoDestination);
        ImageView imgAddToLayout = view.findViewById(R.id.imgAddToLayout);
        ImageView imgClear = view.findViewById(R.id.image_remove);

        addAutoDestination.setOnItemClickListener(autocompleteClickListener);
        addAutoDestination.setAdapter(adapter);

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                removeView(view);
                imgAdd.setVisibility(View.VISIBLE);
                imgAddClear.setVisibility(View.GONE);
            }
        });

        linearDynamic.addView(view);
    }

    public void removeView(View view)
    {
        linearDynamic.removeView(view);
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStart() {
        super.onStart();


    }




}