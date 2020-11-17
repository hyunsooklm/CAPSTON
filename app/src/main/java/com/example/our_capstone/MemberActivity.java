package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MemberActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        bottomNavigationView.setSelectedItemId(R.id.nav_member);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d(TAG, "Current data: " + snapshot.getData()+snapshot.get("users"));
                            String emails = snapshot.get("users").toString();
                            emails = emails.replace("[","");
                            emails = emails.replace("]","");
                            emails = emails.replace(" ","");
                            String[] es = emails.split(",");

                            final GridView gridView = findViewById(R.id.grid_rooms);
                            final GridListAdapter_mem adapter = new GridListAdapter_mem();
                            for(int i=0 ; i< es.length; i++){
                                Log.w(TAG, "-----------"+es[i]+es.length);
                                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                db1.collection("people").whereEqualTo("email", es[i])
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }
                                        for (QueryDocumentSnapshot doc : value) {
                                            if (doc.getId() != null) {                              // 따온 정보들 그리드뷰에 넣어주기
                                                Log.d(TAG, "Current data: " + doc.get("name").toString());
                                                VoChatInfo chat = new VoChatInfo(doc.get("name").toString(),doc.get("birth").toString(),"VO","123");       //순서대로 name, content, date
                                                adapter.addChat(chat);
                                                gridView.setAdapter(adapter);
                                            }
                                        }
                                    }
                                });
                            }

                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                    // 해당 채팅 클릭해도 아무 행동도 안함.
                                }
                            });

                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });

                    /*@Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        GridView gridView = findViewById(R.id.grid_rooms);
                        final ChatActivity.GridListAdapter_chat adapter = new ChatActivity.GridListAdapter_chat();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {

                                VoChatInfo chat = new VoChatInfo(doc.get("name").toString(),doc.get("content").toString(),doc.get("date").toString());
                                adapter.addChat(chat);
                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setSelection(value.size()-1);


                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                // 해당 채팅 클릭해도 아무 행동도 안함.
                            }
                        });
                    }
                });*/
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoRoomActivity();
    }
    private void gotoRoomActivity() {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MemberActivity.this.finish();
    }
    private void gotoChatActivity() {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MemberActivity.this.finish();
    }
    private void gotoAlbumActivity() {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MemberActivity.this.finish();
    }
    private void gotoMenuActivity() {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        MemberActivity.this.finish();
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
                            gotoMenuActivity();
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity();
                            return true;

                        case R.id.nav_member:
                            return true;

                    }
                    return false;
                }
            };
    class GridListAdapter_mem extends BaseAdapter {
        ArrayList<VoChatInfo> chats = new ArrayList<VoChatInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addChat(VoChatInfo chat){
            chats.add(chat);
        }
        @Override
        public int getCount() {
            return chats.size();
        }
        @Override
        public Object getItem(int position) {
            return chats.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoChatInfo chat = chats.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_member, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.textView11);                                        //각 방의 이름이 들어갈 텍스트뷰
            TextView ct = convertView.findViewById(R.id.textView12);                                        //각 방의 이름이 들어갈 텍스트뷰

            String birth = chat.getContent()+"";
            birth = birth.substring(2);
            nm.setText(chat.getName()+"");                                                          //멤버의 이름
            ct.setText(birth+"");                                                       //멤버의 생일

            return convertView;
        }
    }
}
