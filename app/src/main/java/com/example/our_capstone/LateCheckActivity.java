package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class LateCheckActivity extends AppCompatActivity {
    private String KEY ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_check);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("", "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            Log.d("", "Current data: " + snapshot.getData()+snapshot.get("users"));
                            String emails = snapshot.get("users").toString();
                            emails = emails.replace("[","");
                            emails = emails.replace("]","");
                            emails = emails.replace(" ","");
                            String[] es =emails.split(",");

                        } else {
                            Log.d("", "Current data: null");
                        }
                    }
                });
        Button add_promise=(Button)findViewById(R.id.add_promise);
        add_promise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMapActivity(KEY);
            }
        });

    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoMenuActivity(KEY);
    }
    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            gotoRoomActivity(KEY);
                            return true;

                        case R.id.nav_chat:
                            gotoChatActivity(KEY);
                            return true;

                        case R.id.nav_menu:
                            gotoMenuActivity(KEY);
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity(KEY);
                            return true;

                        case R.id.nav_member:
                            gotoMemberActivity();
                            return true;

                    }
                    return false;
                }
            };
    private void gotoMapActivity(String room_key) {
        Intent intent=new Intent(this,MapActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoChatActivity(String room_key) {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
}
