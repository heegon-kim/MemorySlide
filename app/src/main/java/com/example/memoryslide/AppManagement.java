package com.example.memoryslide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

        Spinner spinner = (Spinner) v.findViewById(R.id.sort);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {   // 선택된 정렬기준
                mAppInfo = initDataset(position);
                mAdapter = new RecyclerDataAdapter(mAppInfo);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;
    }

    public ArrayList<AppInfo> initDataset(int sorting) {
        ArrayList<AppInfo> contacts = new ArrayList<AppInfo>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 설치된 앱 목록 가져오기
        ArrayList<String> apps = new ArrayList<String>();
        List<ResolveInfo> pack = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0); // 실행가능한 package만
        // String[] arrayPkgName = new String[pack.size()];

        Collections.sort(pack, new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));   //앱이름 기준으로 정렬
        if (sorting == 1)
            Collections.reverse(pack);
        for (int i = 0; i < pack.size(); i++) { //apps에 패키지명 담기
            apps.add(pack.get(i).activityInfo.packageName);
            //arrayPkgName[i] = pack.get(i).activityInfo.packageName;
        }

        String[] app_name = new String[pack.size() + 1];     // 앱 이름
        Drawable[] app_icon = new Drawable[pack.size() + 1];// 패키지명을 이용해 아이콘 가져오기

        for (int i = 0; i < pack.size(); i++) {
            try {
                app_name[i] = getActivity().getPackageManager().getApplicationLabel
                        (getActivity().getPackageManager().getApplicationInfo
                                (apps.get(i), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                        .toString();

                app_icon[i] = getActivity().getPackageManager().getApplicationIcon(apps.get(i));

                long intstallTimeMillisec = getActivity().getPackageManager().getPackageInfo(apps.get(i), 0).firstInstallTime;  // 설치날짜 정보
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(intstallTimeMillisec);
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                if (mMonth == 0) mMonth = 1;
                String installDate = "" + mYear + "/" + mMonth + "/" + mDay;

                contacts.add(new AppInfo(apps.get(i), app_name[i], app_icon[i], installDate));
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("Package", "패키지에서 에러");
            }
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
