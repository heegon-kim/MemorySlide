package com.example.memoryslide;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoryslide.AppManagement.AppInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.ViewHolder> {
    private Context context;    // context 정보를 담을 그릇
    private List<AppInfo> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat("yy.MM.dd a h:mm");
    private DateFormat mDateFormat2 = new SimpleDateFormat("yy.MM.dd");

    public List<String> mBlockingAppList = new ArrayList<>();    // 차단 앱 리스트

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Context를 부모로 부터 받아와서
        context = parent.getContext();

        //받은 Context를 기반으로 LayoutInflater를 생성
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        //생성된 LayoutInflater로 어떤 Layout을 가져와서 어떻게 View를 그릴지 결정
        View appInfoView = layoutInflater.inflate(R.layout.list_recyclerview_layout, parent, false);
        //View 생성 후, 이 View를 관리하기위한 ViewHolder를 생성
        ViewHolder viewHolder = new ViewHolder(appInfoView);

        //생성된 ViewHolder를 OnBindViewHolder로 넘겨줌
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(mCustomUsageStatsList.get(position), position);
    }

    @Override
    public int getItemCount() {     // 아이템 항목 개수 반환
        if (mCustomUsageStatsList == null) return 0;
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<AppInfo> customUsageStats, List<String> blockingAppList) {
        mCustomUsageStatsList = customUsageStats;
        mBlockingAppList = blockingAppList;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private AppInfo appInfo;
        public RelativeLayout parentRelativeLayout; // 부모 relativeLayout
        public TextView appName;    // 어플 이름
        public TextView mLastTimeUsed;  // 가장 최근에 사용한 시간
        public ImageView appIcon;   // 앱 아이콘
        public TextView install;    // 설치 날짜 시간
        public TextView executeTime;    // 해당일 실행하고 있던 누적시간
        public String appPackageName;   // 어플 패키지 이름
        public TextView blockState;     // 차단 현황
        public String foldername = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test";
        public String filename = "blockingAppList.txt";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test/blockingAppList.txt";

        //com.example.memoryslide.ViewHolder 생성mLastTimeUsed
        public ViewHolder(View itemView) {
            super(itemView);

            //View를 넘겨받아서 ViewHolder를 완성
            parentRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);     // 아래 항목들을 담을 Layout
            appName = (TextView) itemView.findViewById(R.id.appName);   // 어플 이름
            mLastTimeUsed = (TextView) itemView.findViewById(R.id.textview_last_time_used);     // 마지막 실행기록
            appIcon = (ImageView) itemView.findViewById(R.id.appIcon);      // 어플 아이콘
            install = (TextView) itemView.findViewById(R.id.txtInstall);    // 설치 날짜시간
            executeTime = (TextView) itemView.findViewById(R.id.executeTime);   // 해당일 실행시간
            blockState = (TextView) itemView.findViewById(R.id.blockState); // 차단 현황
        }

        public TextView getLastTimeUsed() {
            return mLastTimeUsed;
        }

        void onBind(AppInfo appInfo, int position) {    // 받아온 appInfo의 정보들을 set함
            this.appInfo = appInfo;

            appName.setText(appInfo.getAppName());
            // 실행기록 분류
            long nowTime = System.currentTimeMillis();
            long lastTimeUsed = mCustomUsageStatsList.get(position).usageStats.getLastTimeUsed();
            long gap = nowTime - lastTimeUsed;
            long days = gap / (60 * 60 * 24 * 1000);
            if (lastTimeUsed <= 0) { // 실행기록이 없을 때의 lastTimeUsed의 값
                getLastTimeUsed().setText("한 달 이내의 실행기록이 없습니다!");
                getLastTimeUsed().setTextColor(0xFFDC143C);
            } else if (days <= 30 && days > 14) {
                getLastTimeUsed().setText("2주 이내의 실행기록이 없습니다!");
                getLastTimeUsed().setTextColor(0xFFFF6347);
            } else {
                if (mCustomUsageStatsList.get(position).spinnerPos == 2)
                    getLastTimeUsed().setText("실행 기록: " + mDateFormat2.format(new Date(lastTimeUsed)));
                else
                    getLastTimeUsed().setText("실행 기록: " + mDateFormat.format(new Date(lastTimeUsed)));
                getLastTimeUsed().setTextColor(0x5C000000);
            }
            appIcon.setImageDrawable(appInfo.getAppIcon());

            TextView installDate = install;
            installDate.setText("설치된 날짜: " + appInfo.getInstallDate());

            executeTime.setText("실행 시간: " + appInfo.getExecuteTime());

            appPackageName = mCustomUsageStatsList.get(position).usageStats.getPackageName();

            if (appInfo.blockingState == true)
                blockState.setText("(차단중)");
            else
                blockState.setText("");

            for (int i = 0; i < mBlockingAppList.size(); i++) {     // txt에 저장되어 있는 차단 리스트들을 차단함
                if(mBlockingAppList.get(i).equals(appPackageName)) {
                    Intent blockIntent = new Intent(context, BlockingAppService.class); // BlockingService 클래스에 intent 내용 전달
                    blockIntent.putExtra("AppName", appInfo.getAppName());
                    blockIntent.putExtra("AppPackageName", appPackageName);
                    blockIntent.putExtra("BlockFlag", appInfo.blockingState);
                    context.startService(blockIntent);  // BlockingApp서비스 시작
                }
            }

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {   // 어플 아이템항목 롱클릭 했을때 나오는 메뉴
            MenuItem Block;

            MenuItem Delete = menu.add(Menu.NONE, R.id.menu_delete, 1, "어플 삭제");
            if (appInfo.blockingState == false)     // 차단을 안했으면
                Block = menu.add(Menu.NONE, R.id.menu_block, 2, "어플 차단");
            else    // 이미 차단을 했으면
                Block = menu.add(Menu.NONE, R.id.menu_block, 2, "차단 해제");
            MenuItem Info = menu.add(Menu.NONE, R.id.menu_info, 3, "어플 정보");

            Delete.setOnMenuItemClickListener(onMenuItemClickListener);
            Block.setOnMenuItemClickListener(onMenuItemClickListener);
            Info.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:  // 선택 어플 제거
                        Uri uri = Uri.fromParts("package", appPackageName, null);
                        Intent delIntent = new Intent(Intent.ACTION_DELETE, uri);
                        context.startActivity(delIntent);   // 어플삭제 인텐트 시작
                        return true;
                    case R.id.menu_block:   // 선택 어플 차단/차단해제
                        Intent blockIntent = new Intent(context, BlockingAppService.class); // BlockingService 클래스에 intent 내용 전달
                        if (appInfo.blockingState) {    // 차단되어 있는 상태
                            for (int i = 0; i < mBlockingAppList.size(); i++) {
                                if (mBlockingAppList.get(i).equals(appPackageName))
                                    mBlockingAppList.remove(i);
                            }
                            WriteTextFile2(foldername, filename, mBlockingAppList);  // 차단해제 한 어플을 List에서 제거 후 txt파일로 다시 씀
                            blockState.setText("");
                        } else {     // 차단되어 있지 않은 상태
                            mOnFileWrite(appPackageName);   // txt파일에 차단할 패키지명 추가
                            blockState.setText("(차단중)");
                        }
                        appInfo.blockingState = !appInfo.blockingState;
                        blockIntent.putExtra("AppName", appInfo.getAppName());
                        blockIntent.putExtra("AppPackageName", appPackageName);
                        blockIntent.putExtra("BlockFlag", appInfo.blockingState);
                        context.startService(blockIntent);  // BlockingApp서비스 시작
                        return true;
                    case R.id.menu_info:// 선택 어플 정보
                        Intent infoIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        infoIntent.setData(Uri.parse("package:" + appPackageName));
                        context.startActivity(infoIntent);  // 어플정보 인텐트 시작
                        return true;
                }
                return false;
            }
        };

        public void mOnFileWrite(String appPackageName) {
            String contents = appPackageName + "\n";
            WriteTextFile(foldername, filename, contents);
        }

        //텍스트내용을 경로의 텍스트 파일에 쓰기
        public void WriteTextFile(String foldername, String filename, String contents) {
            try {
                File dir = new File(foldername);
                //디렉토리 폴더가 없으면 생성함
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //파일 output stream 생성
                FileOutputStream fos = new FileOutputStream(foldername + "/" + filename, true);
                //파일쓰기
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                writer.write(contents);
                writer.flush();
                writer.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //append 기능 x
        public void WriteTextFile2(String foldername, String filename, List<String> contents) {
            try {
                File dir = new File(foldername);
                //디렉토리 폴더가 없으면 생성함
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //파일 output stream 생성
                FileOutputStream fos = new FileOutputStream(foldername + "/" + filename, false);
                //파일쓰기
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

                for (int i = 0; i < contents.size(); i++) {
                    writer.append(contents.get(i) + "\n");
                }
                writer.flush();
                writer.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}



