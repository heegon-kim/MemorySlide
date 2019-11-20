package com.example.memoryslide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class AppManagement extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerDataAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<AppInfo> mAppInfo;
    private OnFragmentInteractionListener mListener;
    private TextView appCountText;
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_management, container, false);  // findViewByID 못쓰기때문에 요걸로 대체해야댐.


        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager((getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mAppInfo = initialData();
        Spinner spinner = (Spinner) v.findViewById(R.id.sort);
        appCountText = (TextView)v.findViewById(R.id.appCount);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {   // 선택된
                mAppInfo = initDataset(position);
                appCountText.setText(""+mAppInfo.size());
                mAdapter = new RecyclerDataAdapter(mAppInfo);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {       }
        });

        return v;
    }

    public ArrayList<AppInfo> initialData() {
        ArrayList<AppInfo> contacts = new ArrayList<AppInfo>();
        return contacts;
    }

    public ArrayList<AppInfo> initDataset(int sorting) {
        ArrayList<AppInfo> contacts = new ArrayList<AppInfo>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 설치된 앱 목록 가져오기
        ArrayList<String> apps = new ArrayList<String>();
        List<ResolveInfo> pack = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0); // 실행가능한 package만

        Collections.sort(pack, new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));   //앱이름 기준으로 정렬
        if (sorting == 1)
            Collections.reverse(pack);
        for (int i = 0; i < pack.size(); i++) { //apps에 패키지명 담기
            apps.add(pack.get(i).activityInfo.packageName);
        }

        String[] app_name = new String[pack.size() + 1];     // 앱 이름
        Drawable[] app_icon = new Drawable[pack.size() + 1];// 패키지명을 이용해 아이콘 가져오기
        long[] intstallTimeMillisec = new long[pack.size() + 1];

        for (int i = 0; i < pack.size(); i++) {
            try {
                app_name[i] = getActivity().getPackageManager().getApplicationLabel
                        (getActivity().getPackageManager().getApplicationInfo
                                (apps.get(i), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                        .toString();

                app_icon[i] = getActivity().getPackageManager().getApplicationIcon(apps.get(i));

                intstallTimeMillisec[i] = getActivity().getPackageManager().getPackageInfo(apps.get(i), 0).firstInstallTime;  // 설치날짜 정보

            } catch (PackageManager.NameNotFoundException e) {
                Log.d("Package", "패키지에서 에러");
            }
        }

        if (sorting == 2) {   // 인스톨 날짜 기준
            String tempApps;
            String tempApp_name;
            Drawable tempApp_icon;
            long tempIntstallTimeMillisec;
            for (int i = 0; i < pack.size() - 1; i++) {
                for (int j = 1; j < pack.size() - i; j++)
                    if (intstallTimeMillisec[j - 1] < intstallTimeMillisec[j]) {
                        tempApps = apps.get(j - 1);
                        apps.set(j - 1, apps.get(j));
                        apps.set(j, tempApps);

                        tempApp_name = app_name[j - 1];
                        app_name[j - 1] = app_name[j];
                        app_name[j] = tempApp_name;

                        tempApp_icon = app_icon[j - 1];
                        app_icon[j - 1] = app_icon[j];
                        app_icon[j] = tempApp_icon;

                        tempIntstallTimeMillisec = intstallTimeMillisec[j-1];
                        intstallTimeMillisec[j-1] =  intstallTimeMillisec[j];
                        intstallTimeMillisec[j] = tempIntstallTimeMillisec;
                    }
            }
        }
        else if(sorting == 3){
            String tempApps;
            String tempApp_name;
            Drawable tempApp_icon;
            long tempIntstallTimeMillisec;
            for (int i = 0; i < pack.size() - 1; i++) {
                for (int j = 1; j < pack.size() - i; j++)
                    if (intstallTimeMillisec[j - 1] > intstallTimeMillisec[j]) {
                        tempApps = apps.get(j - 1);
                        apps.set(j - 1, apps.get(j));
                        apps.set(j, tempApps);

                        tempApp_name = app_name[j - 1];
                        app_name[j - 1] = app_name[j];
                        app_name[j] = tempApp_name;

                        tempApp_icon = app_icon[j - 1];
                        app_icon[j - 1] = app_icon[j];
                        app_icon[j] = tempApp_icon;

                        tempIntstallTimeMillisec = intstallTimeMillisec[j-1];
                        intstallTimeMillisec[j-1] =  intstallTimeMillisec[j];
                        intstallTimeMillisec[j] = tempIntstallTimeMillisec;
                    }
            }
        }
        for (int i = 0; i < pack.size(); i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(intstallTimeMillisec[i]);
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (mMonth == 0) mMonth = 1;
            String installDate = "" + mYear + "/" + mMonth + "/" + mDay;
            contacts.add(new AppInfo(apps.get(i), app_name[i], app_icon[i], installDate));
        }
        return contacts;
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
