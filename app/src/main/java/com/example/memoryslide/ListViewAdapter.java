package com.example.memoryslide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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


public class ListViewAdapter extends ArrayAdapter implements View.OnClickListener  {


// 버튼 클릭 이벤트를 위한 Listener 인터페이스 정의.
public interface ListBtnClickListener {
    void onListBtnClick(int position) ;
}


    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    // 생성자로부터 전달된 ListBtnClickListener  저장.
    private ListBtnClickListener listBtnClickListener ;


    // ListViewBtnAdapter 생성자. 마지막에 ListBtnClickListener 추가.
    ListViewAdapter(Context context, int resource, ArrayList<ListViewItem> list, ListBtnClickListener clickListener) {//, ListBtnClickListener clickListener
        super(context, resource, list) ;

        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;
        this.listBtnClickListener = clickListener ;
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
            convertView = inflater.inflate(this.resourceId/*R.layout.listview_btn_item*/, parent, false);
        }

        final AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        final ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);

        final TextView textTextView = (TextView) convertView.findViewById(R.id.textView1);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final ListViewItem listViewItem = (ListViewItem) getItem(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        textTextView.setText(listViewItem.getText());


        final Button button1 = (Button) convertView.findViewById(R.id.button1);
        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context,""+position,Toast.LENGTH_LONG);

                /*Drawable temp = iconImageView.getDrawable();
                Drawable tg_on = getContext().getResources().getDrawable(R.drawable.toggle_on);
                Drawable tg_off = getContext().getResources().getDrawable(R.drawable.toggle_off);

                final Bitmap tmpBitmap = ( (BitmapDrawable)temp).getBitmap();
                final Bitmap tg_onBitmap = ( (BitmapDrawable)tg_on). getBitmap();
                Bitmap tg_offBitmap = ( (BitmapDrawable)tg_off). getBitmap();*/ //이미지 비교
               // ad.show();
                if(position == 0 ){
                    //Toast.makeText(getContext(),Integer.toString(position+1),Toast.LENGTH_SHORT).show();
                    //iconImageView.setImageResource(R.drawable.toggle_off);

                    Log.d("position", ""+position);

                    ad.setMessage("밝기를 몇으로 할까요?");
                    ad.setView(et);
                    ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                            // Event
                        }
                    });
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContext().getContentResolver(), "screen_brightness", Integer.parseInt(et.getText().toString()));
                            Toast.makeText(context, "밝기 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();     //닫기
                            // Event
                        }
                    });

                    ad.show();
                }
                else if(position == 1){
                    final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if(wifiManager.isWifiEnabled()){

                    }

                    final AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

                    if(wifiManager.isWifiEnabled()){
                        ad.setMessage("현재 Wifi On 상태입니다." +
                                "\n\n종료 화면으로 이동할까요?");
                        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();     //닫기
                                // Event
                            }
                        });

                        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClassName("com.android.settings","com.android.settings.wifi.WifiSettings");
                                context.startActivity(intent);
                            }
                        });

                    }
                    else{
                        ad.setMessage("현재 Wifi Off 상태입니다.");

                    }
                    ad.show();

                        wifiManager.setWifiEnabled(true);
                    wifiManager.setWifiEnabled(false);
                }





                /*switch (position+1) {
                    case 1:{ // wifi
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.DisplaySettings");
                        context.startActivity(intent);
                        break;
                    }
                    case 2:{
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.SecuritySettings");
                        context.startActivity(intent);
                        break;
                    }
                    case 3:{
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.SecuritySettings");
                        context.startActivity(intent);
                        break;
                    }
                    case 4:{
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.DisplaySettings");
                        context.startActivity(intent);
                        break;
                    }


                }*/



            }
        });

        return convertView;
    }
    public void onClick(View v) {
        // ListBtnClickListener(MainActivity)의 onListBtnClick() 함수 호출.
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick((int)v.getTag()) ;
        }
    }



}
