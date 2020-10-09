package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RoomActivity  extends AppCompatActivity {                                   //메인클래스
    private static final String TAG = "AppCompatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();    //데이터 수신
        String nm = intent.getExtras().getString("room_key");
        TextView room_nm = (TextView)findViewById(R.id.room_nm);
        room_nm.setText(nm);
    }
}
