package com.example.memoryslide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AppManagement extends Fragment {
    TextView text;
    private OnFragmentInteractionListener mListener;

    public AppManagement() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AppManagement newInstance() {
        AppManagement fragment = new AppManagement();
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
        View v = inflater.inflate(R.layout.fragment_app_management, container, false);  // findViewByID 못쓰기때문에 요걸로 대체해야댐.

        // 설치된 앱 목록 가져오기
        ArrayList<String> apps = new ArrayList<String>();
        List<PackageInfo> pack = getActivity().getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < pack.size(); i++) {
            apps.add(pack.get(i).packageName);
        }

        // 패키지명을 이용해 아이콘 가져오기
        try {
            Drawable App_icon = getActivity().getPackageManager().getApplicationIcon(apps.get(1));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 패키지명을 이용해 앱 이름 가져오기
        String App_name = null;

        //ScrollView 위에 LinearLayout 위에 TextView들(앱목록)
        ScrollView scroller = new ScrollView(getActivity());
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        TextView[] textViews = new TextView[4000];
        for (int i = 0; i < pack.size(); i++) {
            textViews[i] = new TextView(getActivity()); //TextView 생성
            try {
                App_name = getActivity().getPackageManager().getApplicationLabel
                        (getActivity().getPackageManager().getApplicationInfo
                                (apps.get(i), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                        .toString();
            } catch (PackageManager.NameNotFoundException e) {
            }
            textViews[i].setText(App_name);
            linear.addView(textViews[i]); //LinearLayout에 TextView(앱 목록)추가
        }
        scroller.addView(linear);//ScrollView에 LinearLayout추가
        return scroller;
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
