package com.example.our_capstone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlbumInfoSettingActivity extends AppCompatActivity {                                   //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String RKEY ;
    private String AKEY ;
    private static final int REQUEST_CODE = 0;
    private ImageView imageView;
    private Uri filepath;
    private String sto_pho_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info_setting);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        String album_key = intent.getExtras().getString("album_key");
        RKEY = room_key;
        AKEY = album_key;
        imageView = (ImageView)findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener(){                                    //이미지뷰 누르면 갤러리나오게
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        findViewById(R.id.apply_btn).setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            Intent intent = getIntent();    //데이터 수신
            String room_key = intent.getExtras().getString("room_key");
            switch (v.getId()){
                case R.id.apply_btn:                                                                //22행에서 findView없으면 실행안댐
                    apply(RKEY, AKEY);
                    break;
            }
        }
    };
    private void updt_roomInfo(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();                                     //파이어베이스의 firestore (DB) 인스턴스 초기화
        DocumentReference nmRef = db.collection("rooms").document(RKEY).collection("albums").document(AKEY);
        EditText editText = (EditText)findViewById(R.id.room_nm);
        nmRef                                                                                       //DB에서 해당 방 이름 변경
                .update("name", editText.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        if(sto_pho_path!=null){
            nmRef                                                                                   //DB에서 해당 방의 포토 경로 재설정
                    .update("photo", sto_pho_path)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        }
    }
    private void apply(String room_key, String album_key){                                                            //적용버튼을 눌렀을 때
        FirebaseFirestore db = FirebaseFirestore.getInstance();                                     //파이어베이스의 firestore (DB) 인스턴스 초기화
        DocumentReference nmRef = db.collection("rooms").document(room_key).collection("albums").document(album_key);


        //Firestore에 사진저장하고 사진 경로 만들기
        if (filepath != null) {
            uploadPhoto();
        }
        else{
        //그 저장된 이름으로 DB의 photo에 저장(나중에 default인지 확인해서 사진 불러올것임)
            updt_roomInfo();
            Toast.makeText(getApplicationContext(), "앨범 설정 변경 완료!", Toast.LENGTH_SHORT).show();
            gotoAlbumActivity(room_key);
        }
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        AlbumInfoSettingActivity.this.finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                 //갤러리에서 사진 선택하면 그걸 이미지뷰에 띄우려고
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                filepath = data.getData();
                Log.d(TAG, "uri:" + String.valueOf(filepath));
                try {
                        //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                        imageView.setImageBitmap(bitmap);
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void uploadPhoto(){
        if (filepath != null) {                                                                     //업로드할 파일이 있어야지 실행
            final ProgressDialog progressDialog = new ProgressDialog(this);                 //진행사항보여주기
            progressDialog.setTitle("업로드중...");
            progressDialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();                                //storage 인스턴스 생성
            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMDD_HHmmss");             //이름이 년월일_시분초로 생성
            Date now = new Date();
            String filename = formatter.format(now) + ".png";                                       //최종 이름이 될 녀석
            sto_pho_path = filename;                                                                //전역변수에도 넣어줌
                                                                                                    //storage 주소와 폴더 파일명을 지정해 준다.(방Key의 하위폴더에 사진 넣기)
            StorageReference storageRef = storage.getReferenceFromUrl("gs://our-capstone-613a9.appspot.com").child(RKEY+"/" + AKEY +"/"+filename);
            storageRef.putFile(filepath)                                                            //올리기 시작
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            updt_roomInfo();
                            Toast.makeText(getApplicationContext(), "앨범 설정 변경 완료!", Toast.LENGTH_SHORT).show();
                            gotoAlbumActivity(RKEY);

                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoAlbumActivity(RKEY);
    }
}
