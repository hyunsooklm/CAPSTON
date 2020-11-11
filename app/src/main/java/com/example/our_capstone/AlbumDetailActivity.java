package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AlbumDetailActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String RKEY="";
    private String AKEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        Intent intent = getIntent();                                                                //데이터 수신
        RKEY = intent.getExtras().getString("room_key");
        AKEY = intent.getExtras().getString("album_key");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        findViewById(R.id.grp_crt_btn2).setOnClickListener(onClickListener);
        /*그리드형식으로 사진들 보여주기*/
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child(RKEY+"/"+AKEY+"/pics");
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_album_detail adapter = new AlbumDetailActivity.GridListAdapter_album_detail();

                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }

                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            adapter.addAlbum(item);
                        }
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoAlbumActivity(RKEY);
    }
    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            gotoRoomActivity(RKEY);
                            return true;

                        case R.id.nav_chat:
                            gotoChatActivity();
                            return true;

                        case R.id.nav_menu:
                            gotoMenuActivity(RKEY);
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity(RKEY);
                            return true;

                        case R.id.nav_member:
                            gotoMemberActivity();
                            return true;

                    }
                    return false;
                }
            };
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    private void gotoChatActivity() {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",RKEY);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",RKEY);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    private void gotoAlbumPicsActivity(String room_key, String album_key) {
        Intent intent=new Intent(this,AlbumPicsActivity.class);
        intent.putExtra("room_key",room_key);
        intent.putExtra("album_key",album_key);
        startActivity(intent);
        AlbumDetailActivity.this.finish();
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.grp_crt_btn2:
                    gotoAlbumPicsActivity(RKEY,AKEY);
                    break;
            }
        }
    };
    class GridListAdapter_album_detail extends BaseAdapter {
        ArrayList<StorageReference> items = new ArrayList<StorageReference>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addAlbum(StorageReference item){
            items.add(item);
        }
        @Override
        public int getCount() {
            return items.size();
        }
        @Override
        public Object getItem(int position) {
            return items.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            StorageReference item = items.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_rooms, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.nm);                                        //각 방의 이름이 들어갈 텍스트뷰
            //nm.setText(item.getName());                                                             //각 방이름 설정
            final ImageView im = convertView.findViewById(R.id.imageView1);
            item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // Glide 이용하여 이미지뷰에 로딩
                            Glide.with(AlbumDetailActivity.this)
                                    .load(task.getResult())
                                    .into(im);
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(AlbumDetailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            return convertView;
        }
    }
}
