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

public class ConceptShotActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concept_shot);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        /*그리드형식으로 사진들 보여주기*/
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child("conceptshot");
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_album_detail adapter = new GridListAdapter_album_detail();

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
                                StorageReference pic = (StorageReference)adapter.getItem(position);
                                Log.d(TAG, "onItemClick: "+ pic.getName());
                                gotoPopupPicsActivity(pic.getName());
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
        gotoMenuActivity();
    }
    private void gotoPopupPicsActivity(String pic_id){
        Intent intent = new Intent(this, PopupPicsActivity.class);
        intent.putExtra("room_key", KEY);
        intent.putExtra("album_key", "concept");
        intent.putExtra("pic_id", pic_id);
        startActivityForResult(intent, 1);
    }
    private void gotoMenuActivity() {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        ConceptShotActivity.this.finish();
    }
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
                            Glide.with(ConceptShotActivity.this)
                                    .load(task.getResult())
                                    .into(im);
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(ConceptShotActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            return convertView;
        }
    }
}
