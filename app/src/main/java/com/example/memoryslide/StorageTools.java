package com.example.memoryslide;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StorageTools
{
    static long start_t,end_t;

    private static boolean isExternStorageAccessable() { //외부 저장소 접근 가능 유무 체크
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static long getExternalStorageMemorySize(int mode) {
        if (isExternStorageAccessable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalSize = stat.getBlockCountLong() * blockSize;
            long availSize = stat.getAvailableBlocksLong() * blockSize;

            if (mode == 0) {//total
                return totalSize;
            } else if (mode == 1) {//available
                return availSize;
            } else if (mode == 2) {//using;
                return totalSize - availSize;
            }
        }
        return 0;
    }

    public static long getExternalStorageMemorySize(File dir, int mode) {
        if (isExternStorageAccessable()) {
            StatFs stat = new StatFs(dir.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalSize = stat.getBlockCountLong() * blockSize;
            long availSize = stat.getAvailableBlocksLong() * blockSize;

            if (mode == 0) {//total
                return totalSize;
            } else if (mode == 1) {//available
                return availSize;
            } else if (mode == 2) {//using;
                return totalSize - availSize;
            }
        }
        return 0;
    }

    public static long getInternStorageMemorySize(int mode) {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalSize = stat.getBlockCountLong() * blockSize;
        long availSize = stat.getAvailableBlocksLong() * blockSize;

        if (mode == 0) {//total
            return totalSize;
        } else if (mode == 1) {//available
            return availSize;
        } else if (mode == 2) {//using;
            return totalSize - availSize;
        }
        return 0;
    }

    public static long readTest(File testPath, int repeat)
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
                average += path.length();
            }
            end_t = System.currentTimeMillis();
            average *= 1048576;
            average /= (end_t-start_t)*1000;

            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return average;
    }

    public static long writeTest(File testPath,int repeat)
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

                average += path.length();
            }
            end_t = System.currentTimeMillis();
            average *= 1048576;
            average /= (end_t-start_t)*1000;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return average;
    }

    public static String getFileSize(long size)
    {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}