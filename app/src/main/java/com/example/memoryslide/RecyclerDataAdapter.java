package com.example.memoryslide;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerDataAdapter extends RecyclerView.Adapter<ViewHolder> {
    private ArrayList<AppInfo> arrayListOfAppInfo = null;

    public RecyclerDataAdapter(ArrayList<AppInfo> arrayListOfAppInfo) {
        this.arrayListOfAppInfo = arrayListOfAppInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Context를 부모로 부터 받아와서
        Context context = parent.getContext();

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
//RecyclerView에 들어갈 Data(Student로 이루어진 ArrayList 배열인 arrayListOfStudent)를 기반으로 Row를 생성할 때
        //해당 row의 위치에 해당하는 Student를 가져와서
        AppInfo appInfo = arrayListOfAppInfo.get(position);

        //넘겨받은 ViewHolder의 Layout에 있는 View들을 어떻게 다룰지 설정
        //ex. TextView의 text를 어떻게 설정할지, Button을 어떻게 설정할지 등등...
        TextView txtName = holder.txtName;
        txtName.setText(appInfo.getAppName());

        TextView txtPackage = holder.txtPackage;
        txtPackage.setText(appInfo.getAppPackageName());

        ImageView appIcon = holder.appIcon;
        appIcon.setImageDrawable(appInfo.getAppIcon());

        TextView installDate = holder.install;
        installDate.setText(appInfo.getInstallDate());

    }

    @Override
    public int getItemCount() {
        if (arrayListOfAppInfo == null) return 0;
        return arrayListOfAppInfo.size();
    }
}

class ViewHolder extends RecyclerView.ViewHolder {

    public TextView txtName;
    public TextView txtPackage;
    public ImageView appIcon;
    public TextView install;

    //com.example.memoryslide.ViewHolder 생성
    public ViewHolder(View itemView) {
        super(itemView);

        //View를 넘겨받아서 ViewHolder를 완성
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        txtPackage = (TextView) itemView.findViewById(R.id.txtPackage);
        appIcon = (ImageView) itemView.findViewById(R.id.appIcon);
        install = (TextView) itemView.findViewById(R.id.txtInstall);

    }
}

