package com.example.memoryslide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class AppManagement extends Fragment {
    static FragmentActivity activity;
    UsageStatsManager mUsageStatsManager;
    private RecyclerView mRecyclerView;
    private RecyclerDataAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<AppInfo> mAppInfo = null;
    private OnFragmentInteractionListener mListener;
    private TextView appCountText;  // 설치된 어플 개수
    public int noTimeCount = 0;   // 10일이내 사용기록이 없는 어플 개수
    public TextView noUsedApp;
    private ProgressDialog progressBar;
    private DateFormat mDateFormat = new SimpleDateFormat();
    private int mPosition;

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

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmentActivity) getActivity();
        mUsageStatsManager = (UsageStatsManager) getActivity()
                .getSystemService("usagestats"); //Context.USAGE_STATS_SERVICE
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_management, container, false);  // findViewByID 못쓰기때문에 요걸로 대체해야댐.
        mAdapter = new RecyclerDataAdapter();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager((getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mAdapter);
        Spinner spinner = (Spinner) v.findViewById(R.id.sort);
        appCountText = (TextView) v.findViewById(R.id.appCount);
        noUsedApp = (TextView) v.findViewById(R.id.noUsedAppCount);
        progressBar = new ProgressDialog(getActivity());    // 앱 로딩시 진행중 다이얼로그
        progressBar.setCancelable(true);
        progressBar.setMessage("로딩중입니다...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.show();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {   // 선택된 기준에 따른 처리
                progressBar.show();
                mPosition = position;
                noTimeCount = 0;
                StatsUsageInterval statsUsageInterval = null;
                if (position == 0 || position == 1)
                    statsUsageInterval = StatsUsageInterval.getValue("Weekly");
                else
                    statsUsageInterval = StatsUsageInterval.getValue("Daily");
                if (statsUsageInterval != null) {
                    List<UsageStats> usageStatsList =
                            getUsageStatistics(statsUsageInterval.mInterval);
                    if (position == 0)  // 최근실행 오름차순
                        Collections.sort(usageStatsList, new LastTimeLaunchedComparatorAsc());
                    else if (position == 1)  // 최근실행 내림차순
                        Collections.sort(usageStatsList, new LastTimeLaunchedComparatorDesc());
                    else if (position == 2)  // 실행시간 오름차순
                        Collections.sort(usageStatsList, new executeComparatorDesc());
                    // else if (position == 3) // 실행시간 내림차순
                    //     Collections.sort(usageStatsList, new executeComparatorDesc());

                    updateAppsList(usageStatsList);
                }
                progressBar.dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return v;
    }

    void updateAppsList(List<UsageStats> usageStatsList) {
        List<AppInfo> customUsageStatsList = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 설치된 앱 목록 가져오기
        List<ResolveInfo> pack = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0); // 실행가능한 package만
        long appLastTimeRecord;
        int addingCount = 0;
        for (int i = 0; i < usageStatsList.size(); i++) {
            AppInfo customUsageStats = new AppInfo();
            customUsageStats.usageStats = usageStatsList.get(i);
            try {
                if (mPosition == 0 || mPosition == 1) {
                    if (i > 0 && i + 1 < usageStatsList.size() && customUsageStatsList.get(addingCount - 1).usageStats.getPackageName().equals(usageStatsList.get(i).getPackageName())
                            && customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() < usageStatsList.get(i).getLastTimeUsed()) { // add한것보다 크면 add한거 remove하고 더 큰거 add
                        if (customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() <= 0)
                            noTimeCount--;
                        customUsageStatsList.remove(addingCount--);
                    } else if (i > 0 && i + 1 < usageStatsList.size() && customUsageStatsList.get(addingCount - 1).usageStats.getPackageName().equals(usageStatsList.get(i).getPackageName())
                            && customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() >= usageStatsList.get(i).getLastTimeUsed()) { //add한것보다 작으면 걍 continue
                        continue;
                    }

                }
            } catch (Exception e) {
                Log.d("count", "" + i);
            }
            for (int j = 0; j < pack.size(); j++) {
                if (pack.get(j).activityInfo.packageName.equals(customUsageStats.usageStats.getPackageName())) {       // 실행 가능한 어플만 골라냄
                    try {
                        String appName = getActivity().getPackageManager().getApplicationLabel
                                (getActivity().getPackageManager().getApplicationInfo
                                        (customUsageStats.usageStats.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                                .toString();
                        customUsageStats.appName = appName;

                        Drawable appIcon = getActivity().getPackageManager()
                                .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                        customUsageStats.appIcon = appIcon;

                        long intstallTimeMillisec = getActivity().getPackageManager().getPackageInfo(customUsageStats.usageStats.getPackageName(), 0).firstInstallTime;  // 설치날짜 정보

                        String installDate = "" + mDateFormat.format(new Date(intstallTimeMillisec));
                        customUsageStats.installDate = installDate;

                        String executeTime = "" + customUsageStats.usageStats.getTotalTimeInForeground() / 60000 + "분";
                        customUsageStats.executeTime = executeTime;

                        if (customUsageStats.usageStats.getLastTimeUsed() <= 0)
                            noTimeCount++;

                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, String.format("App Icon is not found for %s",
                                customUsageStats.usageStats.getPackageName()));
                        customUsageStats.appIcon = getActivity().getDrawable(R.drawable.ic_default_app_launcher);
                    }
                    customUsageStatsList.add(addingCount++, customUsageStats);
                    break;
                }
            }
        }
        if (mPosition == 0) {
            Collections.sort(customUsageStatsList, new myComparatorDesc());
            noUsedApp.setText(" " + noTimeCount + " 개");
            appCountText.setText(" " + --addingCount + " 개"); // 어플 개수 TextView
        } else if (mPosition == 1) {
            Collections.sort(customUsageStatsList, new myComparatorAsc());
        }
        mAdapter.setCustomUsageStatsList(customUsageStatsList);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter); // Adapter 부착
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.scrollToPosition(0);
    }

    public List<UsageStats> getUsageStatistics(int intervalType) {
        // Get the app statistics since one year ago from the current time.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (queryUsageStats.size() == 0) {
            Log.i(TAG, "The user may not allow the access to apps usage. ");
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
        return queryUsageStats;
    }

    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {     // 내림차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            //return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
            return left.getPackageName().compareTo(right.getPackageName());   // 이름 오름차순
        }
    }

    private static class LastTimeLaunchedComparatorAsc implements Comparator<UsageStats> {     // 오름차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            //return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
            return left.getPackageName().compareTo(right.getPackageName());   // 이름 오름차순
        }
    }

    private static class executeComparatorAsc implements Comparator<UsageStats> {     // 오름차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            long left_intstallTimeMillisec = left.getTotalTimeInForeground();
            long right_intstallTimeMillisec = right.getTotalTimeInForeground();

            return Long.compare(left_intstallTimeMillisec, right_intstallTimeMillisec);
        }
    }

    private static class executeComparatorDesc implements Comparator<UsageStats> {     // 오름차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            long left_intstallTimeMillisec = left.getTotalTimeInForeground();
            long right_intstallTimeMillisec = right.getTotalTimeInForeground();

            return Long.compare(right_intstallTimeMillisec, left_intstallTimeMillisec);
        }
    }

    private static class myComparatorDesc implements Comparator<AppInfo> {     // 오름차순 정렬
        @Override
        public int compare(AppInfo left, AppInfo right) {

            return Long.compare(left.usageStats.getLastTimeUsed(), right.usageStats.getLastTimeUsed());
        }
    }

    private static class myComparatorAsc implements Comparator<AppInfo> {     // 오름차순 정렬
        @Override
        public int compare(AppInfo left, AppInfo right) {

            return Long.compare(right.usageStats.getLastTimeUsed(), left.usageStats.getLastTimeUsed());
        }
    }

    static enum StatsUsageInterval {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

        private int mInterval;
        private String mStringRepresentation;

        StatsUsageInterval(String stringRepresentation, int interval) {
            mStringRepresentation = stringRepresentation;
            mInterval = interval;
        }

        static StatsUsageInterval getValue(String stringRepresentation) {
            for (StatsUsageInterval statsUsageInterval : values()) {
                if (statsUsageInterval.mStringRepresentation.equals(stringRepresentation)) {
                    return statsUsageInterval;
                }
            }
            return null;
        }
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
