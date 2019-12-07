package com.example.memoryslide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class Battery extends Fragment {

    public static class BtInfo{

        static int health;
        static int status;
        static float temperature;
        static float voltage;
        static int chargeStatus;
        static int remain;
        // static long chargeTime=0;


    }


    private OnFragmentInteractionListener mListener;

    ImageView batteryImage;
    TextView percent_text;
    TextView mStatus;//충전 상태
    TextView mTemp;//온도
    TextView mVolta;//전압
    TextView mChargeStatus;//충전 방식
    TextView mHealth;//배터리 상태
    Button tipButton;

    Context _context;
    //Activity activity;
    BroadcastReceiver br;
    View view;
    SharedPreferences pref = null;
    private static Handler mHandler ;
    public Battery() {
        // Required empty public constructor
    }

    public static Battery newInstance() {
        Battery fragment = new Battery();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public void setMyView(){
        //밑부분 정보
        percent_text.setText(""+BtInfo.remain);
        //mStatus.setText(""+BtInfo.status);
        mTemp.setText(""+BtInfo.temperature+" °C");
        mVolta.setText(""+BtInfo.voltage+" V");
        //mChargeStatus.setText(""+BtInfo.chargeStatus);
        //mHealth.setText(""+BtInfo.health);

        //아이콘 잔량에 따라 변경
        if(BtInfo.remain >= 75)
            batteryImage.setImageResource(R.drawable.battery_green);
        else if(BtInfo.remain >= 50)
            batteryImage.setImageResource(R.drawable.battery_yellow);
        else if(BtInfo.remain >= 25)
            batteryImage.setImageResource(R.drawable.battery_brown);
        else if(BtInfo. remain >= 0)
            batteryImage.setImageResource(R.drawable.battry_red);

        //충전 상태 설정
        if(BtInfo.status == BatteryManager.BATTERY_STATUS_CHARGING){
            mStatus.setText("충전 중");
        } else if(BtInfo.status == BatteryManager.BATTERY_STATUS_FULL){
            mStatus.setText("충전 완료");
        } else if(BtInfo.status == BatteryManager.BATTERY_STATUS_NOT_CHARGING){
            mStatus.setText("충전 중이 아님");
        } else if(BtInfo.status == BatteryManager.BATTERY_STATUS_UNKNOWN){
            mStatus.setText("...식별중");
        }

        //충전 방식
        if(BtInfo.chargeStatus == 0){
            mChargeStatus.setText("충전 중이 아님");
        } else {
            if((BtInfo.chargeStatus & BatteryManager.BATTERY_PLUGGED_AC) != 0){
                mChargeStatus.setText("AC 충전기");
            }

            if((BtInfo.chargeStatus & BatteryManager.BATTERY_PLUGGED_USB) != 0){
                mChargeStatus.setText("USB 충전기");
            }

            if((BtInfo.chargeStatus & BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0){
                mChargeStatus.setText("무선 충전기");
            }
        }
        //배터리 상태
        if(BtInfo.health == BatteryManager.BATTERY_HEALTH_COLD){
            mHealth.setText("배터리 온도 낮음");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_DEAD){
            mHealth.setText("배터리 온도 낮음");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_GOOD){
            mHealth.setText("배터리 상태 좋음");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){
            mHealth.setText("배터리 과전압 상태");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_OVERHEAT){
            mHealth.setText("배터리 과열 상태");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_UNKNOWN){
            mHealth.setText("...");
        } else if(BtInfo.health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE){
            mHealth.setText("배터리 특정 불가");
        }

        tipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Log.d("mymy", "btnCLick");
                Intent intent = new Intent(getActivity(), BatteryReduceActivity.class);
                startActivity(intent);
            }
        });




    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getContext())) {
                Toast.makeText(getContext(), "권한 허가 완료", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(getContext(), "권한 허가가 필요합니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }
    private void setup() // broadcastReceiver + registerReceiver
    {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
              //  Log.d("mymy", "onReceive()");

                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    BtInfo.health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                    BtInfo.status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                    BtInfo.temperature = intent.getIntExtra("temperature", 0) / 10;
                    BtInfo.voltage = intent.getIntExtra("voltage", 0) / 1000;
                    BtInfo.chargeStatus = intent.getIntExtra("plugged", 0);
                    BtInfo.remain = intent.getIntExtra("level", 0) * 100
                            /intent.getIntExtra("scale", 100);
                  //  Log.d("mymy", "onReceive() after "+BtInfo.health);
                }
            }
        };
        _context.registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_battery, container, false);
        //mstatus = view.findViewById(R.id.percent_text);
        //int num = setup();
        Log.d("mymy", "setup()");
        Log.d("mymy", "setup()after ");
        //mstatus.setText(""+num);
        return view;
    }
    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        percent_text = view.findViewById(R.id.percent_text);
        mStatus = view.findViewById(R.id.mstatus);
        mTemp = view.findViewById(R.id.mtemperature);
        mVolta = view.findViewById(R.id.mvoltage);
        mChargeStatus = view.findViewById(R.id.mchargestatus);
        mHealth = view.findViewById(R.id.mhealth);
        batteryImage = view.findViewById(R.id.battery_image);
        tipButton = view.findViewById(R.id.tip_button);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                setup();
                setMyView();
                /*percent_text.setText(""+BtInfo.remain);
                mStatus.setText(""+BtInfo.status);
                mTemp.setText(""+BtInfo.temperature+" °C");
                mVolta.setText(""+BtInfo.voltage+" V");
                mChargeStatus.setText(""+BtInfo.chargeStatus);
                mHealth.setText(""+BtInfo.health);*/
            }
        } ;
        class NewRunnable implements Runnable{

            @Override
            public void run(){
                while(true){

                    try{
                        Thread.sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(0) ;
                }

            }
        }
        NewRunnable nr = new NewRunnable();
        Thread t = new Thread(nr);
        t.start();

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("mymy", "onActivityCreated()");

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



}
