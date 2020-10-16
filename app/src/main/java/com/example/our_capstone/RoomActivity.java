package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RoomActivity extends AppCompatActivity {                                   //메인클래스
    private static final String TAG = "AppCompatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        TextView room_nm = (TextView)findViewById(R.id.room_nm);
        room_nm.setText(room_key);
        findViewById(R.id.change_rm_info).setOnClickListener(onClickListener);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            Intent intent = getIntent();    //데이터 수신
            String room_key = intent.getExtras().getString("room_key");
            switch (v.getId()){
                case R.id.change_rm_info:                                       //22행에서 findView없으면 실행안댐
                    gotoRoomInfoSettingActivity(room_key);
                    break;
            }
        }
    };
    private void gotoRoomInfoSettingActivity(String room_key) {
        Intent intent=new Intent(this,RoomInfoSettingActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
    }
     BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.home:
                            return true;

                        case R.id.chat:
                            return true;

                        case R.id.menu:
                            return true;

                        case R.id.album:
                            return true;

                        case R.id.member:
                            return true;

                    }
                    return false;
                }
            };

}
