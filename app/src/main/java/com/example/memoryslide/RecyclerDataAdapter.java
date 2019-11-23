package com.example.memoryslide;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.ViewHolder> {
    private Context context;
    private List<AppInfo> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat();
    private View recordView;

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
    public int getItemCount() {
        if (mCustomUsageStatsList == null) return 0;
        return mCustomUsageStatsList.size();
    }


    public void setCustomUsageStatsList(List<AppInfo> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private AppInfo appInfo;
        private int position;
        public RelativeLayout parentRelativeLayout;
        public TextView appName;
        public TextView mLastTimeUsed;
        public ImageView appIcon;
        public TextView install;
        public TextView executeTime;
        public String appPackageName;

        //com.example.memoryslide.ViewHolder 생성
        public ViewHolder(View itemView) {
            super(itemView);

            //View를 넘겨받아서 ViewHolder를 완성
            parentRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            appName = (TextView) itemView.findViewById(R.id.txtName);
            mLastTimeUsed = (TextView) itemView.findViewById(R.id.textview_last_time_used);
            appIcon = (ImageView) itemView.findViewById(R.id.appIcon);
            install = (TextView) itemView.findViewById(R.id.txtInstall);
            executeTime = (TextView) itemView.findViewById(R.id.executeTime);

            recordView = itemView;
        }

        public TextView getLastTimeUsed() {
            return mLastTimeUsed;
        }

        void onBind(AppInfo appInfo, int position) {    // 받아온 appInfo의 정보들을 set함
            this.appInfo = appInfo;
            this.position = position;

            appName.setText(appInfo.getAppName());

            long lastTimeUsed = mCustomUsageStatsList.get(position).usageStats.getLastTimeUsed();
            if (lastTimeUsed <= 0) // 실행기록이 없을 때의 lastTimeUsed의 값
                getLastTimeUsed().setText("이번 주 실행기록이 없습니다!");
            else
                getLastTimeUsed().setText("실행 기록: " + mDateFormat.format(new Date(lastTimeUsed)));
            appIcon.setImageDrawable(appInfo.getAppIcon());

            TextView installDate = install;
            installDate.setText("설치된 날짜: " + appInfo.getInstallDate());

            executeTime.setText("실행 시간: " + appInfo.getExecuteTime());

            appPackageName = mCustomUsageStatsList.get(position).usageStats.getPackageName();

            itemView.setOnCreateContextMenuListener(this);
            parentRelativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.relativeLayout:

                    break;
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem Delete = menu.add(Menu.NONE, R.id.menu_delete, 1, "어플 삭제");
            MenuItem Block = menu.add(Menu.NONE, R.id.menu_block, 2, "어플 차단");
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
                        Intent it = new Intent(Intent.ACTION_DELETE, uri);
                        context.startActivity(it);
                        return true;
                    case R.id.menu_block:
                        return true;
                    case R.id.menu_info:// 선택 어플 정보
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + appPackageName));
                        context.startActivity(intent);
                        return true;
                }
                return false;
            }
        };
    }
}



