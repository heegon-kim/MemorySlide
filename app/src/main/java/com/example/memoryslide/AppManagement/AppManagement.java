package com.example.memoryslide;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
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

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    static FragmentActivity activity;   // getActivity() 그릇
    UsageStatsManager mUsageStatsManager;   // UsageStatsManager 사용을 위한 객체
    private RecyclerView mRecyclerView;
    private RecyclerDataAdapter mAdapter;   // RecyclerView에 붙일 Apdapter
    private RecyclerView.LayoutManager mLayoutManager;
    private OnFragmentInteractionListener mListener;
    private TextView appCountText;  // 설치된 어플 개수
    public int noTimeCount = 0;   // 10일이내 사용기록이 없는 어플 개수
    public TextView noUsedApp;     // 10일이내 사용기록이 없는 어플 개수 표시
    private ProgressDialog progressDial; //  어플 로딩시 나타낼 다이얼로그
    private DateFormat mDateFormat = new SimpleDateFormat();    // 날짜 형식 만들기
    public int mPosition;  // spinner에서 몇번째 항목을 눌렀는지
    public String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test/blockingAppList.txt";
    public List<String> blockingAppList;

    public static AppManagement newInstance() {
        AppManagement fragment = new AppManagement();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {   // fragment 생성
        super.onCreate(savedInstanceState);
        activity = (FragmentActivity) getActivity();    // activity를 저장함
        mUsageStatsManager = (UsageStatsManager) activity
                .getSystemService("usagestats"); //Context.USAGE_STATS_SERVICE  // UsageStatsManager를 이용하여 현재 시스템서비스를 가져옴
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_management, container, false);  // fragment에서는 바로 findViewByID 못쓰기때문에 요걸로 대체해야댐.
        mAdapter = new RecyclerDataAdapter();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager((getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mAdapter);
        Spinner spinner = (Spinner) v.findViewById(R.id.sort);  // 정렬 기준 정하기
        appCountText = (TextView) v.findViewById(R.id.appCount);    // 설치된 어플
        noUsedApp = (TextView) v.findViewById(R.id.noUsedAppCount); // 사용되지 않는 어플

        progressDial = new ProgressDialog(getActivity());    // 앱 로딩시 진행중 다이얼로그
        progressDial.setCancelable(true);
        progressDial.setMessage("로딩중입니다...");
        progressDial.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDial.setProgress(0);
        progressDial.show();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    // 정렬기준 spinner 아이템 항목 선택
            @SuppressLint("LongLogTag")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {   // 선택된 기준에 따른 처리
                progressDial.show();    // 진행중 다이얼로그 띄우기
                blockingAppList = new ArrayList<>();
                ReadTextFile(filePath); // 차단 어플 리스트 받아오기
                mPosition = position;   // Spinner 몇번째 메뉴인지
                noTimeCount = 0;    // 최근 실행기록이 없는 어플 개수
                StatsUsageInterval statsUsageInterval = null;
                if (position == 0 || position == 1) // 선택한 아이템이 "오래전에 실행" / "최근에 실행"
                    statsUsageInterval = StatsUsageInterval.getValue("Weekly"); // Weekly 단위로 검색
                else    // 실행기록이 높은 순
                    statsUsageInterval = StatsUsageInterval.getValue("Daily"); // Daily 단위로 검색
                if (statsUsageInterval != null) {
                    List<UsageStats> usageStatsList = getUsageStatistics(statsUsageInterval.mInterval); // 내 폰의 이용정보를 가져옴
                    if (position == 0)  // 최근실행 오름차순
                        Collections.sort(usageStatsList, new LastTimeLaunchedComparatorAsc());
                    else if (position == 1)  // 최근실행 내림차순
                        Collections.sort(usageStatsList, new LastTimeLaunchedComparatorDesc());
                    else if (position == 2)  // 실행시간 오름차순
                        Collections.sort(usageStatsList, new executeComparatorDesc());

                    updateAppsList(usageStatsList); // 데이터를 뽑아쓰기 위해 정렬한 List를 넘김
                }
                progressDial.dismiss();  // 다이얼로그 종료
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return v;
    }

    void updateAppsList(List<UsageStats> usageStatsList) {  // 넘겨받은 이용목록 List를 가지고 필요한 항목들을 뽑아냄
        List<AppInfo> customUsageStatsList = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 설치된 앱 목록 가져오기
        List<ResolveInfo> pack = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0); // 실행가능한 package만
        int addingCount = 0;    // customUsageStatsList의 index 번호
        for (int i = 0; i < usageStatsList.size(); i++) {
            AppInfo customUsageStats = new AppInfo();   // 필요한 정보를 뽑아 저장하기 위한 AppInfo클래스의 객체
            customUsageStats.usageStats = usageStatsList.get(i);
            try {
                if (mPosition == 0 || mPosition == 1) { // 선택한 아이템이 "오래전에 실행" / "최근에 실행"
                    if (i > 0 && i + 1 < usageStatsList.size() && customUsageStatsList.get(addingCount - 1).usageStats.getPackageName().equals(usageStatsList.get(i).getPackageName())
                            && customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() < usageStatsList.get(i).getLastTimeUsed()) { // add한것보다 크면 add한거 remove하고 더 큰거 add
                        if (customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() <= 0)    // 마지막실행 검색이 안되면 count에서 제외
                            noTimeCount--;
                        customUsageStatsList.remove(addingCount--);
                    } else if (i > 0 && i + 1 < usageStatsList.size() && customUsageStatsList.get(addingCount - 1).usageStats.getPackageName().equals(usageStatsList.get(i).getPackageName())
                            && customUsageStatsList.get(addingCount - 1).usageStats.getLastTimeUsed() >= usageStatsList.get(i).getLastTimeUsed()) { //add한것보다 작으면 걍 continue
                        continue;
                    }
                }
            } catch (Exception e) {
                Log.d("log_tag", "updateAppList method");
            }
            for (int j = 0; j < pack.size(); j++) {
                if (pack.get(j).activityInfo.packageName.equals(customUsageStats.usageStats.getPackageName())) {       // 실행 가능한 어플만 골라냄
                    try {
                        String appName = getActivity().getPackageManager().getApplicationLabel
                                (getActivity().getPackageManager().getApplicationInfo
                                        (customUsageStats.usageStats.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                                .toString();    // 어플이름 뽑아내기
                        customUsageStats.appName = appName;

                        Drawable appIcon = getActivity().getPackageManager()
                                .getApplicationIcon(customUsageStats.usageStats.getPackageName());  // 어플 아이콘 뽑아내기
                        customUsageStats.appIcon = appIcon;

                        long intstallTimeMillisec = getActivity().getPackageManager().getPackageInfo(customUsageStats.usageStats.getPackageName(), 0).firstInstallTime;  // 설치날짜 정보
                        String installDate = "" + mDateFormat.format(new Date(intstallTimeMillisec));
                        customUsageStats.installDate = installDate;

                        String executeTime = "" + customUsageStats.usageStats.getTotalTimeInForeground() / 60000 + "분"; // 총 실행시간 뽑아내기
                        customUsageStats.executeTime = executeTime;

                        if (customUsageStats.usageStats.getLastTimeUsed() <= 0)     // 마지막 사용기간 검색이 먼 과거일 경우
                            noTimeCount++;

                        customUsageStats.spinnerPos = mPosition;    // Spinner에서 몇번째 메뉴인지

                        for (int k = 0; k < blockingAppList.size(); k++) {
                            if (blockingAppList.get(k).equals(customUsageStats.usageStats.getPackageName()))
                                customUsageStats.blockingState = true;
                        }



                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, String.format("App Icon is not found for %s",
                                customUsageStats.usageStats.getPackageName()));
                        customUsageStats.appIcon = getActivity().getDrawable(R.drawable.ic_default_app_launcher);
                    }
                    customUsageStatsList.add(addingCount++, customUsageStats);  // 필요한 정보들만 뽑아낸 customUsageStats을 list의 일부분으로 추가시킴
                    break;
                }
            }
        }
        if (mPosition == 0) {   // 정렬기준이 "오랜전에 실행"
            Collections.sort(customUsageStatsList, new myComparatorAsc());  // 실행기록 오름차순 정렬
            appCountText.setText(" " + --addingCount + " 개"); // 설치된 어플 개수 TextView
            noUsedApp.setText(" " + noTimeCount + " 개");    // 실행기록이 오래된 어플 개수
        } else if (mPosition == 1) {   // 정렬기준이 "최근에 실행"
            Collections.sort(customUsageStatsList, new myComparatorDesc());  // 실행기록 내림차순 정렬
        }
        mAdapter.setCustomUsageStatsList(customUsageStatsList, blockingAppList);
        mAdapter.notifyDataSetChanged();    // 바뀐 데이터에 대한 어댑터 갱신
        mRecyclerView.setAdapter(mAdapter); // Adapter 부착
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());   // Animation Defualt 설정
        mRecyclerView.scrollToPosition(0);  // 스크롤 설정
    }

    public List<UsageStats> getUsageStatistics(int intervalType) {     // 현재시간으로부터 과거의 어플 이용정보를 가져옴
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (queryUsageStats.size() == 0) {  // 이용정보를 가져올 수 없는 경우
            Log.i(TAG, "사용자가 사용정보 접근 허용을 하지 않았습니다. ");
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));   // 해당 설정화면을 인텐트로 띄어줌
        }
        return queryUsageStats; // 이용정보 쿼리를 반환
    }

    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {     // 마지막 실행 내림차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            return left.getPackageName().compareTo(right.getPackageName());   // 이름 오름차순
        }
    }

    private static class LastTimeLaunchedComparatorAsc implements Comparator<UsageStats> {     // 마지막 실행 오름차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            return left.getPackageName().compareTo(right.getPackageName());   // 이름 오름차순
        }
    }

    private static class executeComparatorDesc implements Comparator<UsageStats> {     // 총 실행시간 오름차순 정렬
        @Override
        public int compare(UsageStats left, UsageStats right) {
            long left_intstallTimeMillisec = left.getTotalTimeInForeground();
            long right_intstallTimeMillisec = right.getTotalTimeInForeground();

            return Long.compare(right_intstallTimeMillisec, left_intstallTimeMillisec);
        }
    }

    private static class myComparatorAsc implements Comparator<AppInfo> {     // 오름차순 정렬
        @Override
        public int compare(AppInfo left, AppInfo right) {
            return Long.compare(left.usageStats.getLastTimeUsed(), right.usageStats.getLastTimeUsed());
        }
    }

    private static class myComparatorDesc implements Comparator<AppInfo> {     // 오름차순 정렬
        @Override
        public int compare(AppInfo left, AppInfo right) {
            return Long.compare(right.usageStats.getLastTimeUsed(), left.usageStats.getLastTimeUsed());
        }
    }

    static enum StatsUsageInterval {    // 현재는 Weekly와 Monthly 단위만 사용중
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

    //경로의 텍스트 파일읽기
    public void ReadTextFile(String path) {
        StringBuffer strBuffer = new StringBuffer();
        try {
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                blockingAppList.add(line);
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
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
