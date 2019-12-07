package com.example.memoryslide;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class BatteryReduceActivity extends AppCompatActivity implements ListViewAdapter.ListBtnClickListener {

    public boolean loadItemsFromDB(ArrayList<ListViewItem> list) {
        ListViewItem item ;
        int i ;

        if (list == null) {
            list = new ArrayList<ListViewItem>() ;
        }

        // 순서를 위한 i 값을 1로 초기화.
        i = 1 ;

        // 아이템 생성.
        item = new ListViewItem() ;
        item.setIcon(ContextCompat.getDrawable(this, R.drawable.toggle_off)) ;
       // item.setText(Integer.toString(i) + "번 아이템입니다.") ;
        item.setText("시스템 화면 밝기 조정");
        list.add(item) ;

        i++ ;

        item = new ListViewItem() ;
        item.setIcon(ContextCompat.getDrawable(this, R.drawable.toggle_off)) ;
        item.setText("Wifi 기능 관리") ; //  sdk29부터 불가능...
        list.add(item) ;
        i++ ;

        item = new ListViewItem() ;
        item.setIcon(ContextCompat.getDrawable(this, R.drawable.toggle_on)) ;
        item.setText(Integer.toString(i) + "번 아이템입니다.") ;
        list.add(item) ;
        i++ ;

        item = new ListViewItem() ;
        item.setIcon(ContextCompat.getDrawable(this, R.drawable.toggle_on)) ;
        item.setText(Integer.toString(i) + "번 아이템입니다.") ;
        list.add(item) ;

        return true ;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_reduce);
        // 코드 계속 ...

        ListView listview ;
        ListViewAdapter adapter;
        ArrayList<ListViewItem> items = new ArrayList<ListViewItem>() ;

        // items 로드.
        loadItemsFromDB(items) ;



        // Adapter 생성
        adapter = new ListViewAdapter(this, R.layout.listview_item, items, this) ;
        Log.d("mymy", "여기");
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // TODO : item click
            }
        }) ;
    }

    @Override
    public void onListBtnClick(int position) {
        Toast.makeText(this, Integer.toString(position+1) + " Item is selected..", Toast.LENGTH_SHORT).show() ;
    }
}
