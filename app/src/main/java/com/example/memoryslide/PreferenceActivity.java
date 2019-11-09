package com.example.memoryslide;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.memoryslide.databinding.ActivityPreferenceBinding;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPreferenceBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_preference);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new PreferenceFragment())
                .commit();
    }
}