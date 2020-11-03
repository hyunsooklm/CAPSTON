package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QnaActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")
                .document(KEY)
                .collection("ours")
                .orderBy("title", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {                                                 //게시판의 글 불러오기
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_qna adapter = new GridListAdapter_qna();
                        List<String> rooms = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                                ArrayList valueList = new ArrayList(doc.getData().values());
                                VoQnaInfo qna = new VoQnaInfo(doc.getId(), doc.get("title").toString(),doc.get("author").toString(), doc.get("content").toString());
                                adapter.addQna(qna);
                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                VoQnaInfo qna = (VoQnaInfo)adapter.getItem(position);
                                gotoQnaDetailActivity(qna);
                            }
                        });
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
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaActivity.this.finish();
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaActivity.this.finish();
    }
    private void gotoChatActivity(String room_key) {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        QnaActivity.this.finish();
    }
    private void gotoQnaDetailActivity(VoQnaInfo qna) {
        Intent intent=new Intent(this,QnaDetailActivity.class);
        intent.putExtra("room_key",KEY);
        intent.putExtra("qna_key",qna.getKey());
        intent.putExtra("qna_title",qna.getTitle());
        intent.putExtra("qna_author",qna.getAuthor());
        intent.putExtra("qna_content",qna.getContent());
        startActivity(intent);
        QnaActivity.this.finish();
    }
    class GridListAdapter_qna extends BaseAdapter {
        ArrayList<VoQnaInfo> qnas = new ArrayList<VoQnaInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addQna(VoQnaInfo qna){
            qnas.add(qna);
        }
        @Override
        public int getCount() {
            return qnas.size();
        }
        @Override
        public Object getItem(int position) {
            return qnas.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoQnaInfo qna = qnas.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_our, parent, false);     // greedy view안에는 각각 greedy_our.xml을 적용
            }
            TextView nm = convertView.findViewById(R.id.title);                                        //각 방의 이름이 들어갈 텍스트뷰
            nm.setText(qna.getTitle());                                                             //각 방이름 설정
            return convertView;
        }
    }
}
