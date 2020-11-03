package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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

public class AlbumActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        bottomNavigationView.setSelectedItemId(R.id.nav_album);
        /*------------------------------위는 액티비티 복붙--------------------------------*/

        findViewById(R.id.grp_crt_btn2).setOnClickListener(onClickListener);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .collection("albums")                                                  //그 room의 모든 앨범들
                .orderBy("photo", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_album adapter = new GridListAdapter_album();
                        List<String> albums = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                                VoAlbumInfo album = new VoAlbumInfo(doc.getId(), doc.get("name").toString(),doc.get("photo").toString());
                                adapter.addAlbum(album);
                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                VoAlbumInfo album = (VoAlbumInfo) adapter.getItem(position);
                                gotoAlbumDetailActivity(KEY,album.getKey());
                            }
                        });
                    }
                });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면 그 모임의 메인화면으로
        super.onBackPressed();
        moveTaskToBack(true);
        gotoRoomActivity(KEY);
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
                            gotoChatActivity();
                            return true;

                        case R.id.nav_menu:
                            gotoMenuActivity(KEY);
                            return true;

                        case R.id.nav_album:
                            return true;

                        case R.id.nav_member:
                            gotoMemberActivity();
                            return true;

                    }
                    return false;
                }
            };
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumActivity.this.finish();
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumActivity.this.finish();
    }
    private void gotoChatActivity() {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        AlbumActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        AlbumActivity.this.finish();
    }
    private void gotoAlbumInfoSettingActivity(String room_key, String album_key) {                  //앨범의 이름과 썸네일 설정하는 액티비티
        Intent intent=new Intent(this,AlbumInfoSettingActivity.class);
        intent.putExtra("room_key",room_key);
        intent.putExtra("album_key",album_key);
        startActivity(intent);
        AlbumActivity.this.finish();
    }
    private void gotoAlbumDetailActivity(String room_key, String album_key) {                  //앨범의 이름과 썸네일 설정하는 액티비티
        Intent intent=new Intent(this,AlbumDetailActivity.class);
        intent.putExtra("room_key",room_key);
        intent.putExtra("album_key",album_key);
        startActivity(intent);
        AlbumActivity.this.finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.grp_crt_btn2:
                    crt_album();
                    break;
            }
        }
    };

    /*--------------------------------위는 액티비티 복붙----------------------------------*/

    class GridListAdapter_album extends BaseAdapter {
        ArrayList<VoAlbumInfo> albums = new ArrayList<VoAlbumInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addAlbum(VoAlbumInfo room){
            albums.add(room);
        }
        @Override
        public int getCount() {
            return albums.size();
        }
        @Override
        public Object getItem(int position) {
            return albums.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoAlbumInfo room = albums.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_rooms, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.nm);                                        //각 방의 이름이 들어갈 텍스트뷰
            nm.setText(room.getName());                                                             //각 방이름 설정
            if(!room.getPhoto().equals("default")) {                                                //방의 메인 화면이 지정되어있다면
                final ImageView im = convertView.findViewById(R.id.imageView1);
                StorageReference ref = FirebaseStorage.getInstance().getReference();
                StorageReference pathReference = ref.child(KEY+"/"+room.getKey()+"/"+room.getPhoto());
                pathReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // Glide 이용하여 이미지뷰에 로딩
                            Glide.with(AlbumActivity.this)
                                    .load(task.getResult())
                                    .into(im);
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(AlbumActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            return convertView;
        }
    }
    private void crt_album(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();                             //파이어베이스의 firestore (DB) 인스턴스 초기화
        Map<String, Object> album = new HashMap<>();
        album.put("name", "unknown");
        album.put("photo","default");
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .collection("albums")                                                  //그 room의 모든 앨범들
                .add(album)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showToast("새 앨범을 생성하였습니다.");
                        gotoAlbumInfoSettingActivity(KEY, documentReference.getId());               //앨범 만들면 앨범설정창으로 이동
                    }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.toString());
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
    private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
