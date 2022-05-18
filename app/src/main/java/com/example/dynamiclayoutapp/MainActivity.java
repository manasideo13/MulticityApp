package com.example.dynamiclayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.dynamiclayoutapp.adapter.AutoCompleteAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    ImageView imgAdd, imgAddClear;
    LinearLayout linearDynamic;
    //List<View> data = new ArrayList<>();
    AutoCompleteTextView autoCompleteTextViewPickup;
    AutocompletePrediction item;
    AutoCompleteAdapter adapter;
    PlacesClient placesClient;

    String address = "", city = "", pickup_city = "", drop_city = "", pickup_address = "", drop_address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearDynamic = findViewById(R.id.linearDynamic);
        imgAdd = findViewById(R.id.imgAdd);
        imgAddClear = findViewById(R.id.imgAddClear);



        autoCompleteTextViewPickup =(AutoCompleteTextView) findViewById(R.id.auto_pickup);


        /*initialized place sdk*/
        if (!Places.isInitialized()) {
            Log.d("TAG", "key = " + R.string.google_maps_key);
            Places.initialize(MainActivity.this, String.valueOf(R.string.google_maps_key));
        }

        /*create new place client instance*/
        placesClient = Places.createClient(MainActivity.this);
        adapter = new AutoCompleteAdapter(MainActivity.this, placesClient);


        autoCompleteTextViewPickup.setOnItemClickListener(autocompleteClickListener);


        autoCompleteTextViewPickup.setAdapter(adapter);
        //list locate on map

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                imgAdd.setVisibility(View.GONE);
                imgAddClear.setVisibility(View.VISIBLE);
                addView();
            }
        });

        autoCompleteTextViewPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                autoCompleteTextViewPickup.setSelectAllOnFocus(true);


                //Toast.makeText(MainActivity.this, "Pickup called", Toast.LENGTH_SHORT).show();
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

                                /* if pickup flag is true (selected from drop down location) */


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
        AutoCompleteTextView auto_destination = view.findViewById(R.id.auto_destination);
        ImageView imgAddToLayout = view.findViewById(R.id.imgAddToLayout);
        ImageView imgClear = view.findViewById(R.id.image_remove);

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
}