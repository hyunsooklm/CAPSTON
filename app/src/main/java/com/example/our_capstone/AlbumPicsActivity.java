package com.example.our_capstone;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlbumPicsActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String RKEY="";
    private String AKEY="";
    private static final int REQUEST_CODE = 0;
    private ImageView imageView;
    private Uri filepath;
    private ClipData clipData;
    private String sto_pho_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_pics);
        Intent intent = getIntent();                                                                //데이터 수신
        RKEY = intent.getExtras().getString("room_key");
        AKEY = intent.getExtras().getString("album_key");

        findViewById(R.id.apply_btn).setOnClickListener(onClickListener);
        imageView = (ImageView)findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener(){                                    //이미지뷰 누르면 갤러리나오게
            @Override
            public void onClick(View v) {                                                        //이미지 클릭시 갤러리 띄우기
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);                           //이미지 여러개도 가능
                intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
            }
        });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoAlbumDetailActivity(RKEY,AKEY);
    }
    private void gotoAlbumDetailActivity(String room_key, String album_key) {
        Intent intent=new Intent(this,AlbumDetailActivity.class);
        intent.putExtra("room_key",room_key);
        intent.putExtra("album_key",album_key);
        startActivity(intent);
        AlbumPicsActivity.this.finish();
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.apply_btn:                                                                //22행에서 findView없으면 실행안댐
                    apply(RKEY, AKEY);
                    break;
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                 //갤러리에서 사진 선택하면 그걸 이미지뷰에 띄우려고
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                clipData = data.getClipData();                                             //갤러리에서 받은 사진들
                if(clipData!=null){
                    GridView gridView = findViewById(R.id.grid_rooms);
                    final GridListAdapter_album_pics adapter = new GridListAdapter_album_pics();
                    for(int i=0;i<clipData.getItemCount();i++){
                        Uri urione =  clipData.getItemAt(i).getUri();                               //사진들 하나하나
                        adapter.addAlbum(urione);
                    }
                    gridView.setAdapter(adapter);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                            //VoAlbumInfo album = (VoAlbumInfo) adapter.getItem(position);
                        }
                    });
                }
                filepath = data.getData();
                Log.d(TAG, "uri:" + String.valueOf(filepath));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }
    class GridListAdapter_album_pics extends BaseAdapter {
        ArrayList<Uri> items = new ArrayList<Uri>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addAlbum(Uri item){
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
            Uri item = items.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_rooms, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.nm);                                        //각 방의 이름이 들어갈 텍스트뷰
            //nm.setText(item.getName());                                                             //각 방이름 설정
            final ImageView im = convertView.findViewById(R.id.imageView1);
            im.setImageURI(item);
            return convertView;
        }
    }
    private void apply(String room_key, String album_key){                                                            //적용버튼을 눌렀을 때
        //Firestore에 사진저장하고 사진 경로 만들기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                //파이어베이스의 인증 (회원관리) 이용해서 로그인정보 가져오기
        if(clipData!=null){
            for(int i=0;i<clipData.getItemCount();i++){
                final ProgressDialog progressDialog = new ProgressDialog(this);                 //진행사항보여주기
                progressDialog.setTitle("업로드중...");
                progressDialog.show();

                Uri urione =  clipData.getItemAt(i).getUri();                                           //사진들 하나하나
                FirebaseStorage storage = FirebaseStorage.getInstance();                                //storage 인스턴스 생성
                //Unique한 파일명을 만들자.
                SimpleDateFormat formatter = new SimpleDateFormat("yyMMDD_HHmmssSSSS");             //이름이 년월일_시분초로 생성
                Date now = new Date();
                String filename = formatter.format(now)+ user.getDisplayName() + ".png";                                       //최종 이름이 될 녀석
                sto_pho_path = filename;                                                                //전역변수에도 넣어줌
                //storage 주소와 폴더 파일명을 지정해 준다.(방Key의 하위폴더에 사진 넣기)
                StorageReference storageRef = storage.getReferenceFromUrl("gs://our-capstone-613a9.appspot.com").child(RKEY+"/" + AKEY +"/pics/"+filename);
                storageRef.putFile(urione)                                                            //올리기 시작
                        //성공시
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                Toast.makeText(getApplicationContext(), "앨범 설정 변경 완료!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        //실패시
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //progressDialog.dismiss();
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
            }
        }
    }
}
