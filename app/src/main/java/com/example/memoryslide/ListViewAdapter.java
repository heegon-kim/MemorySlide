package com.example.memoryslide;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ListViewAdapter extends ArrayAdapter   {


// 버튼 클릭 이벤트를 위한 Listener 인터페이스 정의.
public interface ListBtnClickListener {
    void onListBtnClick(int position) ;
}

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    // 생성자로부터 전달된 ListBtnClickListener  저장.
    private ListBtnClickListener listBtnClickListener ;


    // ListViewBtnAdapter 생성자. 마지막에 ListBtnClickListener 추가.
    ListViewAdapter(Context context, int resource, ArrayList<ListViewItem> list) {
        super(context, resource, list) ;

        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;

    }

    // 새롭게 만든 Layout을 위한 View를 생성하는 코드
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position ;
        final Context context = parent.getContext();

        final EditText et= new EditText(getContext());
        et.setHint("숫자를 입력해주세요 0~255");

        // 생성자로부터 저장된 resourceId(listview_btn_item)에 해당하는 Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId, parent, false);
        }

        //다이얼로그 생성
        final AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        final ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
        final TextView textTextView = (TextView) convertView.findViewById(R.id.textView1);

        // list view item에서 position에 위치한 데이터 참조 획득
        final ListViewItem listViewItem = (ListViewItem) getItem(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        textTextView.setText(listViewItem.getText());

        //버튼 클릭 리스너(다이얼로그 > 값 조정)
        final Button button1 = (Button) convertView.findViewById(R.id.button1);
        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context,""+position,Toast.LENGTH_LONG);
                if(position == 0 ){
                    ad.setMessage("밝기를 몇으로 할까요?");
                    ad.setView(et);
                    ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContext().getContentResolver(), "screen_brightness", Integer.parseInt(et.getText().toString()));
                            Toast.makeText(context, "밝기 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();     //닫기
                        }
                    });
                    ad.show();//장착
                }
                //위와 같음
                else if(position == 1){
                    final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    final AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

                    if(wifiManager.isWifiEnabled()){
                        ad.setMessage("현재 상태 : WIFI On" +
                                "\n\n 설정 화면으로 이동할까요?");
                        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();     //닫기
                            }
                        });

                        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //설정 화면으로 이동
                                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        });

                    }
                    else{
                        ad.setMessage("현재 상태 : WIFI Off");

                    }
                    ad.show();
                }
                //위와 같음
                else if(position == 2){
                    final LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    final AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                    if(locationManager.isLocationEnabled()){

                        ad.setMessage("현재 상태 : GPS On" +
                                "\n\n 설정 화면으로 이동할까요?");
                        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();     //닫기
                            }
                        });
                        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        });
                    }
                    ad.show();
                }
            }
        });
        return convertView;
    }
}
