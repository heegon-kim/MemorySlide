package com.example.memoryslide.Stroage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompatSideChannelService;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.pm.PackageStats;
import android.widget.Toast;

import com.example.memoryslide.R;

public class Storage extends Fragment {
    private OnFragmentInteractionListener mListener;
    updateViewThread th = new updateViewThread();
    performanceHandler han = new performanceHandler();
    View v;
    TextView text_StoragePerformance;
    ProgressBar progressbar_Storage;
    TextView text_StorageSize;
    TextView text_StoragePercentage;
    TextView text_Comment;
    TextView text_totalCacheSize;
    TextView text_totalCacheShare;
    TextView text_cacheComment;
    public static final int FETCH_PACKAGE_SIZE_COMPLETED = 100;

    //---------------------------------------------------------------LIFE TIME-----------------------------------------------------------------------//
    public Storage() {
        // Required empty public constructor
    }

    public static Storage newInstance() {
        Storage fragment = new Storage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            th.start();
        } catch (IllegalThreadStateException e) {
            //스레드가 종료되지 않은 상태
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        v = getView();
        text_StoragePerformance = v.findViewById(R.id.text_dataReadWriteSpeed);

        Button button_cachecleaner = (Button) v.findViewById(R.id.button_CacheCleaner);
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        button_cachecleaner.setOnClickListener(onClickListener);

        progressbar_Storage = v.findViewById(R.id.progressBar_Storage);
        text_StorageSize = v.findViewById(R.id.text_StorageSize);
        text_StoragePercentage = v.findViewById(R.id.text_StoragePercentage);
        text_Comment = v.findViewById(R.id.text_storageComment);
        text_totalCacheSize = v.findViewById(R.id.TotalCacheSize);
        text_totalCacheShare = v.findViewById(R.id.TotalCacheShare);
        text_cacheComment = v.findViewById(R.id.cacheComment);

        if(!permissionCheck())
        {
            Toast.makeText(getActivity(), "권한이 허가되지 않아 저장장치 측정이 불가능 합니다.", Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                packageSize = 0;
                getpackageSize();
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        th.interrupt();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    //---------------------------------------------------------------LIFE TIME-----------------------------------------------------------------------//


    //---------------------------------------------------------------------뷰------------------------------------------------------------------------//
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class updateViewThread extends Thread {
        public void run() {
            while (true) {
                if(permissionCheck())
                {
                    Message msg = han.obtainMessage();
                    han.sendMessage(msg);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateView() {//뷰를 데이터에 맞게 업데이트
        long size_total = StorageTools.getExternalStorageMemorySize(0) + StorageTools.getInternStorageMemorySize(0);
        long size_current = StorageTools.getExternalStorageMemorySize(2) + StorageTools.getInternStorageMemorySize(2);
        long percentage_Current = (long) ((float) size_current / (float) size_total * 100);

        float sharesum = 0;
        long sizeofApp = StorageTools.getInternStorageMemorySize(1);
        float sizeofPic, sizeofMov, sizeofMus, sizeofExt;


        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        sharesum += StorageTools.getExternalStorageMemorySize(directory, 2);
        sizeofPic = StorageTools.getExternalStorageMemorySize(directory, 2);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        sharesum += StorageTools.getExternalStorageMemorySize(directory, 2);
        sizeofMov = StorageTools.getExternalStorageMemorySize(directory, 2);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        sharesum += StorageTools.getExternalStorageMemorySize(directory, 2);
        sizeofMus = StorageTools.getExternalStorageMemorySize(directory, 2);

        sizeofExt = StorageTools.getExternalStorageMemorySize(directory, 0) - sharesum;
        sharesum += StorageTools.getExternalStorageMemorySize(directory, 0) - sharesum;

        sharesum += sizeofApp;

        sizeofApp /= sharesum;
        sizeofPic /= sharesum;
        sizeofMov /= sharesum;
        sizeofMus /= sharesum;
        sizeofExt /= sharesum;

        LinearLayout.LayoutParams parameter;

        ProgressBar progressBar_shareApp = v.findViewById(R.id.progressBar_StorageShareApp);
        ProgressBar progressBar_sharePic = v.findViewById(R.id.progressBar_StorageSharePic);
        ProgressBar progressBar_shareMov = v.findViewById(R.id.progressBar_StorageShareVid);
        ProgressBar progressBar_shareMus = v.findViewById(R.id.progressBar_StorageShareMus);
        ProgressBar progressBar_shareExt = v.findViewById(R.id.progressBar_StorageShareExt);

        parameter = (LinearLayout.LayoutParams) progressBar_shareApp.getLayoutParams();
        parameter.weight = sizeofApp;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams) progressBar_sharePic.getLayoutParams();
        parameter.weight = sizeofPic;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams) progressBar_shareMov.getLayoutParams();
        parameter.weight = sizeofMov;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams) progressBar_shareMus.getLayoutParams();
        parameter.weight = sizeofMus;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams) progressBar_shareExt.getLayoutParams();
        parameter.weight = sizeofExt;
        progressBar_shareApp.setLayoutParams(parameter);

        text_StorageSize.setText(StorageTools.getFileSize(size_current) + " / " + StorageTools.getFileSize(size_total));
        text_StoragePercentage.setText(Long.toString(percentage_Current) + " %");
        progressbar_Storage.setProgress((int) percentage_Current);

        if (percentage_Current > 80) {
            text_Comment.setText(R.string.storage_comment_lack);
            text_Comment.setTextColor(Color.parseColor("#FF0000"));
        } else if (percentage_Current > 90) {
            text_Comment.setText(R.string.storage_comment_almostFull);
            text_Comment.setTextColor(Color.parseColor("#FF4500"));
        } else {
            text_Comment.setText(R.string.storage_comment_default);
            text_Comment.setTextColor(Color.parseColor("#32CD32"));
        }
    }

    public class performanceHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            super.handleMessage(msg);

            text_StoragePerformance.setText("쓰기 속도 : " + StorageTools.getFileSize(StorageTools.writeTest(Environment.getExternalStorageDirectory(), 100)) + "/ sec\n"
                        + "읽기 속도 : " + StorageTools.getFileSize(StorageTools.readTest(Environment.getExternalStorageDirectory(), 100)) + "/ sec");
            updateView();
        }
    }
    //---------------------------------------------------------------------뷰------------------------------------------------------------------------//


    //-------------------------------------------------------------------이벤트----------------------------------------------------------------------//
    public class BtnOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.button_CacheCleaner) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        packageSize = 0;
                        getpackageSize();
                    }
                }).start();
            }
        }
    }
    //-------------------------------------------------------------------이벤트----------------------------------------------------------------------//


    //-------------------------------------------------------------------캐시------------------------------------------------------------------------//
    long packageSize = 0, size = 0;
    AppDetails cAppDetails;
    public ArrayList<AppDetails.PackageInfoStruct> res;

    private void getpackageSize() {

        cAppDetails = new AppDetails(this.getActivity());
        res = cAppDetails.getPackages();
        if (res == null)
            return;
        for (int m = 0; m < res.size(); m++)
        {
            PackageManager pm = getActivity().getPackageManager();
            Method getPackageSizeInfo;
            try {
                getPackageSizeInfo = pm.getClass().getMethod(
                        "getPackageSizeInfo", String.class,
                        IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm, res.get(m).pname,
                        new cachePackState());
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler cacheSizeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FETCH_PACKAGE_SIZE_COMPLETED:
                    if (packageSize > 0) {
                        long size_current = StorageTools.getExternalStorageMemorySize(1) + StorageTools.getInternStorageMemorySize(1);
                        float percent = (float) Math.round((float) packageSize / size_current * 10000) / 100;

                        text_totalCacheSize.setText("Cache Size : " + StorageTools.getFileSize(packageSize));
                        text_totalCacheShare.setText("Cache Share : " + percent + "%" + "   (CacheSize / RemainStorage)");
                        if (percent < 5) {
                            text_cacheComment.setText(R.string.storage_cache_default);
                            text_cacheComment.setTextColor(Color.parseColor("#32CD32"));

                        } else if (percent < 10) {
                            text_cacheComment.setText(R.string.storage_cache_little);
                            text_cacheComment.setTextColor(Color.parseColor("#FF4500"));
                        } else {
                            text_cacheComment.setText(R.string.storage_cache_floods);
                            text_cacheComment.setTextColor(Color.parseColor("#FF0000"));
                        }
                        break;
                    }
                default:
                    break;
            }
        }
    };

    private class cachePackState extends IPackageStatsObserver.Stub {
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            packageSize = packageSize + pStats.cacheSize;
            cacheSizeHandler.sendEmptyMessage(FETCH_PACKAGE_SIZE_COMPLETED);
        }
    }
    //-------------------------------------------------------------------캐시------------------------------------------------------------------------//

    //-------------------------------------------------------------------Permission------------------------------------------------------------------//

    int permissionWriteExternalStroage;

    private boolean permissionCheck() {
        permissionWriteExternalStroage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionWriteExternalStroage == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            Toast.makeText(getActivity(), "권한 승인이 필요합니다", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))
            {
                Toast.makeText(getActivity(), "저장장치 성능 측정을 위해 외부 저장소 쓰기권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            else
            {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, permissionWriteExternalStroage);
                Toast.makeText(getActivity(), "저장장치 성능 측정을 위해 외부 저장소 쓰기권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
        return  false;
    }
    //-------------------------------------------------------------------Permission------------------------------------------------------------------//
}
