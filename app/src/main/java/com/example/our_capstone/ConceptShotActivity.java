package com.example.our_capstone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
public class ConceptShotActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    private RecyclerView rcv;
    private MyAdapter_rcv adpt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concept_shot);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        rcv = findViewById(R.id.rcv);
        rcv.setHasFixedSize(true);
        rcv.setLayoutManager(new GridLayoutManager(this,2));
        /*그리드형식으로 사진들 보여주기*/
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child("conceptshot");
        final ArrayList<RecycleModel> items = new ArrayList<RecycleModel>();
        adpt = new MyAdapter_rcv(this,items);
        rcv.setAdapter(adpt);
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }
                        for (final StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        // Glide 이용하여 이미지뷰에 로딩
                                        RecycleModel rcm = new RecycleModel(task.getResult(), item.getName());
                                        items.add(rcm);
                                        adpt.notifyDataSetChanged();
                                    } else {
                                        // URL을 가져오지 못하면 토스트 메세지
                                        Toast.makeText(ConceptShotActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });
        adpt.setOnItemClickListener(new MyAdapter_rcv.OnItemClickListener() {
            @Override
            public void onitemClick(View v, int pos, ArrayList<RecycleModel> items) {
                gotoPopupPicsActivity(items.get(pos).getNm());
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
    static class ViewHolder{
        ImageView im;
        public int pos=0;
    }
}
