package com.example.memoryslide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Storage extends Fragment {
    private OnFragmentInteractionListener mListener;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //초기화
        updateView();
    }

    private void updateView()
    {//뷰를 데이터에 맞게 업데이트
        View v = getView();
        long size_total = checkInternMemorySize();
        long size_current = size_total - checkInternMemoryAvail();
        long percentage_Current = (long)((float)size_current/(float)size_total*100);

        ProgressBar progressbar_CurrentStorage = v.findViewById(R.id.storage_progressbar_StorageSpace);
        TextView text_CurrentStorage = v.findViewById(R.id.storage_text_CurrentStorage);
        TextView text_TotalStorage = v.findViewById(R.id.storage_text_FullStorage);
        TextView text_percentageStorage = v.findViewById(R.id.storage_text_PercentageStorage);

        text_CurrentStorage.setText(getFileSize(size_current));
        text_TotalStorage.setText(getFileSize(size_total));
        text_percentageStorage.setText(Long.toString(percentage_Current));
        progressbar_CurrentStorage.setProgress((int)percentage_Current);

    }

    private String getFileSize(long size)
    {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("000.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
