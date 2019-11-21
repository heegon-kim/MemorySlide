package com.example.memoryslide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Storage extends Fragment {
    private OnFragmentInteractionListener mListener;
    long start_t,end_t;
    updateViewThread th = new updateViewThread();
    performanceHandler han = new performanceHandler();
    TextView text_StoragePerformance;
    View v;

    public class BtnOnClickListener implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if(v.getId() == R.id.button_CacheCleaner)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.storage_title_cacheCleaner);
                builder.setMessage(R.string.storage_notice_cacheCleanCheck);
                builder.setIcon(R.drawable.icon_cachecleaner);

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        //실행 X
                    }
                });

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        //캐시파일 삭제로직
                    }
                });

                builder.show();
            }
        }
    }

    public Storage()
    {
        // Required empty public constructor
    }

    public static Storage newInstance()
    {
        Storage fragment = new Storage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        try
        {
            th.start();
        }
        catch(IllegalThreadStateException e)
        {

        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        v = getView();
        text_StoragePerformance = v.findViewById(R.id.text_dataReadWriteSpeed);

        Button button_cachecleaner = (Button)v.findViewById(R.id.button_CacheCleaner);
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        button_cachecleaner.setOnClickListener(onClickListener);
    }

    public class updateViewThread extends Thread
    {
        public void run()
        {
            while (true) {
                Message msg = han.obtainMessage();
                han.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class performanceHandler extends Handler
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            text_StoragePerformance.setText("쓰기 속도 : " + getFileSize(writeTest(Environment.getExternalStorageDirectory(), 100)) + "/ sec\n"
                    + "읽기 속도 : " + getFileSize(readTest(Environment.getExternalStorageDirectory(), 100)) + "/ sec");
            updateView();
        }
    }

    private void updateView()
    {//뷰를 데이터에 맞게 업데이트
        long size_total = checkInternMemorySize()+checkExternMemorySize();
        long size_current = size_total - checkInternMemoryAvail()-checkExternMemoryAvail();
        long percentage_Current = (long)((float)size_current/(float)size_total*100);


        ProgressBar progressbar_Storage = v.findViewById(R.id.progressBar_Storage);
        TextView text_StorageSize = v.findViewById(R.id.text_StorageSize);
        TextView text_StoragePercentage = v.findViewById(R.id.text_StoragePercentage);
        TextView text_Comment = v.findViewById(R.id.text_storageComment);

        text_StorageSize.setText(getFileSize(size_current) +" / " +getFileSize(size_total));
        text_StoragePercentage.setText(Long.toString(percentage_Current) +" %");

        progressbar_Storage.setProgress((int)percentage_Current);

        float sharesum=0;
        long sizeofApp = checkInternMemorySize()-checkInternMemoryAvail();
        float sizeofPic, sizeofMov, sizeofMus, sizeofExt;


        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        sharesum += sizeofFolder_extern(directory);
        sizeofPic = sizeofFolder_extern(directory);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        sharesum += sizeofFolder_extern(directory);
        sizeofMov = sizeofFolder_extern(directory);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        sharesum += sizeofFolder_extern(directory);
        sizeofMus = sizeofFolder_extern(directory);

        sizeofExt = checkExternMemorySize()-sharesum;
        sharesum += checkExternMemorySize()-sharesum;

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

        parameter = (LinearLayout.LayoutParams)progressBar_shareApp.getLayoutParams();
        parameter.weight = sizeofApp;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams)progressBar_sharePic.getLayoutParams();
        parameter.weight = sizeofPic;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams)progressBar_shareMov.getLayoutParams();
        parameter.weight = sizeofMov;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams)progressBar_shareMus.getLayoutParams();
        parameter.weight = sizeofMus;
        progressBar_shareApp.setLayoutParams(parameter);

        parameter = (LinearLayout.LayoutParams)progressBar_shareExt.getLayoutParams();
        parameter.weight = sizeofExt;
        progressBar_shareApp.setLayoutParams(parameter);

        if(percentage_Current > 80)
        {
            text_Comment.setText(R.string.storage_comment_lack);
        }
        else if(percentage_Current > 90)
        {
            text_Comment.setText(R.string.storage_comment_almostFull);
        }
        else
        {
            text_Comment.setText(R.string.storage_comment_default);
        }
    }

    private long readTest(File testPath, int repeat)
    {
        BufferedReader buf=null;
        String line = null;
        StringBuffer stb = new StringBuffer();
        long average=0;
        File path = new File(testPath.getPath() + "/test");
        if(!path.exists())
        {
            path.mkdir();
        }

        try {
            start_t = System.currentTimeMillis();
            for(int i=0; i<repeat;i++)
            {
                buf = new BufferedReader(new FileReader(path + "/testfile.txt"));
                while ((line = buf.readLine()) != null)
                {
                    stb.append(line + "\n");
                }
                average += path.length()*1000000;
            }
            end_t = System.currentTimeMillis();
            average /= (end_t-start_t)*1000;

            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return average;
    }

    private long writeTest(File testPath,int repeat)
    {
        long average=0;
        long testdata;
        Date date;
        SimpleDateFormat sdf;
        BufferedWriter buf=null;
        String nowTime;

        File path = new File(testPath.getPath() + "/test");
        if(!path.exists())
        {
            path.mkdir();
        }

        try
        {
            testdata = System.currentTimeMillis();
            date = new Date(testdata);
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nowTime = sdf.format(date);

            start_t = System.currentTimeMillis();
            for(int i =0; i<repeat ; i++)
            {
                buf = new BufferedWriter(new FileWriter(path + "/testfile.txt", false));
                for(int j=0; j<repeat ; j++)
                {
                    buf.append(nowTime);
                }
                buf.flush();
                buf.close();

                average += path.length()*1000000;
            }
            end_t = System.currentTimeMillis();
            average /= (end_t-start_t)*1000;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return average;
    }

    float sizeofFolder_extern(File dir)
    {
        if(isExternStorageAccessable()) {
            StatFs stat = new StatFs(dir.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalSize = stat.getBlockCountLong();
            long availSize = stat.getAvailableBlocksLong();

            return blockSize*(totalSize-availSize);
        }
        return 0;
    }

    private String getFileSize(long size)
    {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private boolean isExternStorageAccessable()
    { //외부 저장소 접근 가능 유무 체크
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private long checkExternMemorySize()
    {
        if(isExternStorageAccessable())
        {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalSize = stat.getBlockCountLong();

            return blockSize * totalSize;
        }
        return 0;
    }

    private long checkExternMemoryAvail()
    {
        if(isExternStorageAccessable())
        {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = stat.getBlockSizeLong();
            long availSize = stat.getAvailableBlocksLong();

            return blockSize * availSize;
        }
        return 0;
    }

    private long checkInternMemorySize()
    {//내부 저장소의 전체 크기
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalSize = stat.getBlockCountLong();

        return blockSize * totalSize;
    }

    private long checkInternMemoryAvail()
    {//내부 저장소의 사용가능한 크기
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long availSize = stat.getAvailableBlocksLong();

        return blockSize * availSize;
    }

    @Override
    public void onPause() {
        super.onPause();
        th.interrupt();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



}
