package com.example.our_capstone;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class PopupPicsActivity extends Activity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    private String AKEY="";
    private String PICID="";
    private String FileName="";
    ProgressBar progressBar;
    private File file, dir;
    private String savePath= "ImageTemp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popuppics);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        AKEY = intent.getExtras().getString("album_key");
        PICID = intent.getExtras().getString("pic_id");
        final ImageView im = (ImageView) findViewById(R.id.imageViewPicDetail);
        if(AKEY.equals("concept")) {
            StorageReference ref = FirebaseStorage.getInstance().getReference();
            StorageReference pathReference = ref.child("conceptshot" + "/" + PICID);
            pathReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Glide 이용하여 이미지뷰에 로딩
                        Glide.with(PopupPicsActivity.this)
                                .load(task.getResult())
                                .into(im);
                    } else {
                        // URL을 가져오지 못하면 토스트 메세지
                        Toast.makeText(PopupPicsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            StorageReference ref = FirebaseStorage.getInstance().getReference();
            StorageReference pathReference = ref.child(KEY+"/"+AKEY+"/pics/" + PICID);
            pathReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Glide 이용하여 이미지뷰에 로딩
                        Glide.with(PopupPicsActivity.this)
                                .load(task.getResult())
                                .into(im);
                    } else {
                        // URL을 가져오지 못하면 토스트 메세지
                        Toast.makeText(PopupPicsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        findViewById(R.id.downpics).setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.downpics:                                                           //73행에서 findView없으면 실행안댐
                    downpic();
                    break;
            }
        }
    };
    private void downpic(){
        if(AKEY.equals("concept")) {
            StorageReference ref = FirebaseStorage.getInstance().getReference();
            StorageReference pathReference = ref.child("conceptshot" + "/" + PICID);
            File fdir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "download");
            if (!fdir.isDirectory()) fdir.mkdir();
            final File fdown = new File(fdir, PICID);
            pathReference.getFile(fdown).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    showToast("기기에 다운로드 성공!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        else{
            StorageReference ref = FirebaseStorage.getInstance().getReference();
            StorageReference pathReference = ref.child(KEY+"/"+AKEY+"/pics/" + PICID);
            File fdir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "download");
            if (!fdir.isDirectory()) fdir.mkdir();
            final File fdown = new File(fdir, PICID);
            pathReference.getFile(fdown).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    showToast("기기에 다운로드 성공!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }
    private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
