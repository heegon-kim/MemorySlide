package com.example.memoryslide;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BlockingAppService extends Service {
    public List<String> blockingAppList = new ArrayList<>();  // 차단한 어플 리스트
    public Thread thread = new Thread();

    public BlockingAppService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String appName = intent.getExtras().getString("AppName");
        String appPackageName = intent.getExtras().getString("AppPackageName");
        boolean blockFlag = intent.getExtras().getBoolean("BlockFlag");

        if (blockFlag) {    // 차단
            blockingAppList.add(appPackageName);
            Toast.makeText(this, "\"" + appName + "\" " + "어플이 차단되었습니다.", Toast.LENGTH_SHORT).show();
        } else {    // 차단해제
            for (int i = 0; i < blockingAppList.size(); i++) {
                if (blockingAppList.get(i).equals(appPackageName)) {
                    blockingAppList.remove(i);
                    break;
                }
            }
            Toast.makeText(this, "\"" + appName + "\" " + "어플이 차단이 해제되었습니다.", Toast.LENGTH_SHORT).show();
        }
        if (thread.isAlive()) {
            thread.interrupt();
        }
        if (blockingAppList.size() != 0) {
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            KillRunningApps();  // blockingAppList에 있는 어플 차단
                        }
                    } catch (Exception e) {
                        Log.d("log_tag", "Blocking Thread exception");
                    }
                }
            };
            thread.start();
        }
        return START_NOT_STICKY;
    }

    void KillRunningApps() {    // 어플 차단 메서드 ( blockingAppList의 어플을 실행 시키면 홈화면으로 전화 후 프로세스 종료 )
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
       /* Log.d("log_tag", "Try kill process..");
        Log.d("log_tag", "" + getForegroundPackageName());*/

        for (int i = 0; i < blockingAppList.size(); i++) {
            try {
                if (getForegroundPackageName().equals(blockingAppList.get(i))) {    // 현재 실행 어플이 blockingAppList에 있는 어플이면
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);  // 홈화면으로 전환
                    activityManager.killBackgroundProcesses(blockingAppList.get(i));    // 백그라운드 상태로 된 어플을 죽임
                }
            } catch (Exception e) {
                Log.d("log_tag", "KillRunningApps Method Exception");
            }
        }
        System.gc();
    }

    private String getForegroundPackageName() {     // 현재 실행중인 어플의 패키지
        String packageName = null;
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        final long endTime = System.currentTimeMillis();
        final long beginTime = endTime - 10000;
        final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                packageName = event.getPackageName();
            }
        }
        return packageName;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
