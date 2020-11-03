package com.example.our_capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuActivity extends AppCompatActivity {
    private String KEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);
        findViewById(R.id.qna).setOnClickListener(onClickListener);
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoRoomActivity();
    }
    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            gotoRoomActivity();
                            return true;

                        case R.id.nav_chat:
                            gotoChatActivity();
                            return true;

                        case R.id.nav_menu:
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity();
                            return true;

                        case R.id.nav_member:
                            gotoMemberActivity();
                            return true;

                    }
                    return false;
                }
            };
    private void gotoRoomActivity() {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MenuActivity.this.finish();
    }
    private void gotoAlbumActivity() {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MenuActivity.this.finish();
    }
    private void gotoQnaActivity() {
        Intent intent=new Intent(this,QnaActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MenuActivity.this.finish();
    }
    private void gotoChatActivity() {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MenuActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MenuActivity.this.finish();
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.qna:
                    gotoQnaActivity();
                    break;
            }
        }
    };
}