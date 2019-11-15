package com.example.memoryslide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Battery extends Fragment {
    private OnFragmentInteractionListener mListener;

    TextView mstatus;
    Context _context;
    //Activity activity;
    BroadcastReceiver br;
    View view;

    public Battery() {
        // Required empty public constructor
    }

    public static Battery newInstance() {
        Battery fragment = new Battery();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_battery, container, false);
        mstatus = view.findViewById(R.id.percent_text);
        setup();
        mstatus.setText("456");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
        _context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        _context.unregisterReceiver(br);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setup()
    {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    int health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                    int level = intent.getIntExtra("level", 0);
                    int plug = intent.getIntExtra("plugged", 0);
                    int scale = intent.getIntExtra("scale", 100);
                    int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                    String technology = intent.getStringExtra("technology");
                    int temperature = intent.getIntExtra("temperature", 0);
                    int voltage = intent.getIntExtra("voltage", 0);
                }
            }
        };
        _context.registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
}
