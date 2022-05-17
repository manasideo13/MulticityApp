package com.example.dynamiclayoutapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    ImageView imgAdd, imgAddClear;
    LinearLayout linearDynamic;
    //List<View> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearDynamic = findViewById(R.id.linearDynamic);
        imgAdd = findViewById(R.id.imgAdd);
        imgAddClear = findViewById(R.id.imgAddClear);

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
    }

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