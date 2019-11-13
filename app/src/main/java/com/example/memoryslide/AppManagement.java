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
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 설치된 앱 목록 가져오기
        ArrayList<String> apps = new ArrayList<String>();
        List<ResolveInfo> pack = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0); // 실행가능한 package만
        // String[] arrayPkgName = new String[pack.size()];
        Collections.sort(pack, new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));   //패키지명 기준으로 정렬

        for (int i = 0; i < pack.size(); i++) { //apps에 패키지명 담기
            apps.add(pack.get(i).activityInfo.packageName);
            //arrayPkgName[i] = pack.get(i).activityInfo.packageName;
        }

        // 패키지명을 이용해 아이콘 가져오기
        Drawable[] app_icon = new Drawable[pack.size() + 1];
        try {
            for (int i = 0; i < pack.size(); i++) {
                app_icon[i] = getActivity().getPackageManager().getApplicationIcon(apps.get(i));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // 패키지명을 이용해 앱 이름 가져오기
        String app_name = null;

        // ScrollView 위에 LinearLayout 위에 TextView들(앱목록)
        ScrollView scroller = new ScrollView(getActivity());
        LinearLayout rootLinearPanel = new LinearLayout(getActivity());
        rootLinearPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout[] linearPanel = new LinearLayout[pack.size() + 1];
        LinearLayout[] linearByAppInfo = new LinearLayout[pack.size() + 1]; // 불러운 패키지 개수만큼 할당
        TextView[] appName = new TextView[pack.size() + 1]; // 불러운 패키지 개수만큼 할당
        View line = new View(getActivity());
        line.setBackgroundColor(Color.GRAY);

        ViewGroup.LayoutParams lineParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lineParam.height = (int) getResources().getDimension(R.dimen.line_height);
        line.setLayoutParams(lineParam);
        float marginSize = getResources().getDimension(R.dimen.appname_margin); // java소스로는 dp size를 나타낼수 없어서 불러옴

        for (int i = 0; i < pack.size(); i++) {
            linearPanel[i] = new LinearLayout(getActivity());
            linearPanel[i].setOrientation(LinearLayout.HORIZONTAL);

            linearByAppInfo[i] = new LinearLayout(getActivity()); // LinearLayout 생성
            // linear에 대한 속성
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins((int) marginSize, (int) marginSize, (int) marginSize, (int) marginSize);
            linearPanel[i].setLayoutParams(params);
            //linearByAppInfo[i].setLayoutParams(params);
            linearByAppInfo[i].setOrientation(LinearLayout.VERTICAL);

            // 어플 아이콘 ImageView에 대한 속성
            ImageView appImage = new ImageView(getActivity());
            appImage.setImageDrawable(app_icon[i]);

            // 어플이름 textView에 대한 속성
            LinearLayout.LayoutParams appName_textParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            appName_textParam.setMargins((int) marginSize, (int) marginSize, 0, 0);
            appName[i] = new TextView(getActivity()); // TextView 생성
            // appName[i].setLayoutParams(appName_textParam);
            appName[i].setTypeface(null, Typeface.BOLD);
            try {
                app_name = getActivity().getPackageManager().getApplicationLabel
                        (getActivity().getPackageManager().getApplicationInfo
                                (apps.get(i), PackageManager.GET_UNINSTALLED_PACKAGES)) //apps.get(index)
                        .toString();
            } catch (PackageManager.NameNotFoundException e) {
            }
            appName[i].setText(app_name);
            linearByAppInfo[i].addView(appName[i], appName_textParam); //LinearLayout에 TextView(앱 목록)추가
            linearPanel[i].addView(appImage);
            linearPanel[i].addView(linearByAppInfo[i]);
            rootLinearPanel.addView(linearPanel[i]);// LinearLayout추가
            rootLinearPanel.removeView(line);
            rootLinearPanel.addView(line,lineParam);
        }
        scroller.addView(rootLinearPanel);//ScrollView에 LinearLayout추가
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
