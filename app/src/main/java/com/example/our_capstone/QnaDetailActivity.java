package com.example.our_capstone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class QnaDetailActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private static final int REQUEST_CODE = 0;
    private String RKEY="";
    private String QNAKEY="";
    private String QNATITLE="";
    private String QNAAUTHOR="";
    private String QNACONTENT="";
    private ImageView imageView;
    private Uri filepath;
    private String sto_pho_path;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna_detail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = getIntent();                                                                //데이터 수신
        RKEY = intent.getExtras().getString("room_key");
        QNAKEY = intent.getExtras().getString("qna_key");
        QNATITLE = intent.getExtras().getString("qna_title");
        QNAAUTHOR = intent.getExtras().getString("qna_author");
        QNACONTENT = intent.getExtras().getString("qna_content");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);

        TextView title = (TextView)findViewById(R.id.title);
        title.setText(QNATITLE.split("_")[1]);
        TextView content = (TextView)findViewById(R.id.content);
        content.setText(QNACONTENT);
        imageView = (ImageView)findViewById(R.id.reply_img);
        imageView.setOnClickListener(new View.OnClickListener(){                                    //이미지뷰 누르면 갤러리나오게
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        findViewById(R.id.reply_btn).setOnClickListener(onClickListener);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")
                .document(RKEY)
                .collection("ours")
                .document(QNAKEY)
                .collection("reply")
                .orderBy("author", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {                                                 //게시판의 글의 댓글들 불러오기
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_reply adapter = new GridListAdapter_reply();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                                VoReplyInfo reply = new VoReplyInfo(doc.getId(), doc.get("photo").toString(),doc.get("author").toString(), doc.get("content").toString());
                                adapter.addReply(reply);
                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                VoReplyInfo rep = (VoReplyInfo)adapter.getItem(position);
                                if(rep.getPhoto().equals("unknown")){
                                    Log.d(TAG, "onItemClick: "+ rep.getPhoto());
                                }else{
                                    Log.d(TAG, "onItemClick: "+ rep.getPhoto());
                                    gotoPopupPicsActivity(rep.getPhoto());
                                }
                            }
                        });
                    }
                });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoQnaActivity(RKEY);
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
                            gotoChatActivity(RKEY);
                            return true;

                        case R.id.nav_menu:
                            //gotoMenuActivity(RKEY);
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
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.reply_btn:                                         //클릭을 sign_up_btn 누를때
                    apply();
                    break;
            }
        }
    };
    private void gotoPopupPicsActivity(String pic_id){
        Intent intent = new Intent(this, PopupPicsActivity.class);
        intent.putExtra("room_key", "qna_"+RKEY);
        intent.putExtra("album_key", QNAKEY);
        intent.putExtra("pic_id", pic_id);
        startActivityForResult(intent, 1);
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoQnaActivity(String room_key) {
        Intent intent=new Intent(this,QnaActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoChatActivity(String room_key) {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",RKEY);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    private void gotoQnaDetailActivity() {
        Intent intent=new Intent(this,QnaDetailActivity.class);
        intent.putExtra("room_key",RKEY);
        intent.putExtra("qna_key",QNAKEY);
        intent.putExtra("qna_title",QNATITLE);
        intent.putExtra("qna_author",QNAAUTHOR);
        intent.putExtra("qna_content",QNACONTENT);
        startActivity(intent);
        QnaDetailActivity.this.finish();
    }
    class GridListAdapter_reply extends BaseAdapter {
        ArrayList<VoReplyInfo> replies = new ArrayList<VoReplyInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addReply(VoReplyInfo reply){
            replies.add(reply);
        }
        @Override
        public int getCount() {
            return replies.size();
        }
        @Override
        public Object getItem(int position) {
            return replies.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoReplyInfo reply = replies.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_reply, parent, false);     // greedy view안에는 각각 greedy_our.xml을 적용
            }
            TextView nm = convertView.findViewById(R.id.replies_txt);                                        //각 방의 이름이 들어갈 텍스트뷰
            TextView nm1 = convertView.findViewById(R.id.textView10);
            TextView nm2 = convertView.findViewById(R.id.textView13);
            String[] info = reply.getAuthor().split("_");
            nm1.setText(info[0]+"_"+info[1]);
            nm2.setText(info[2]);
            nm.setText(reply.getContent());                                                             //각 방이름 설정
            if(!reply.getPhoto().equals("unknown")){
                final ImageView im = convertView.findViewById(R.id.replies_img);
                StorageReference ref = FirebaseStorage.getInstance().getReference();
                StorageReference pathReference = ref.child(RKEY+"/"+QNAKEY+"/"+reply.getPhoto());
                pathReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // Glide 이용하여 이미지뷰에 로딩
                            Glide.with(QnaDetailActivity.this)
                                    .load(task.getResult())
                                    .into(im);
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(QnaDetailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                final ImageView im = convertView.findViewById(R.id.replies_img);
                im.setImageResource(0);
            }
            return convertView;
        }
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
    private void apply() {
        EditText editText = findViewById(R.id.reply);
        if(!editText.getText().toString().equals("")){
            if (filepath != null) {
                    uploadPhoto();
                }
                else{
                    //그 저장된 이름으로 DB의 photo에 저장(나중에 default인지 확인해서 사진 불러올것임)
                    updt_reply();
                }
        }
        else{
            Toast.makeText(this, "답글을 작성하여주세요", Toast.LENGTH_LONG).show();
        }
    }
    private void updt_reply() {
        EditText editText = (EditText)findViewById(R.id.reply);
            if(true) {
                Map<String, Object> reply = new HashMap<>();
                SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");             //이름이 년월일_시분초로 생성
                Date now = new Date();
                TimeZone timezone;
                timezone = TimeZone.getTimeZone("Asia/Seoul");
                formatter.setTimeZone(timezone);
                reply.put("author",formatter.format(now)+"_"+user.getDisplayName());
                if (sto_pho_path == null) {
                    reply.put("photo","unknown");
                }
                else{
                    reply.put("photo",sto_pho_path);
                }
                reply.put("content",editText.getText().toString());
                FirebaseFirestore db = FirebaseFirestore.getInstance();                                     //파이어베이스의 firestore (DB) 인스턴스 초기화
                db.collection("rooms").document(RKEY).collection("ours").document(QNAKEY).collection("reply")
                        .add(reply)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //gotoQnaDetailActivity();
                            }});
                editText.setText("");
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");             //이름이 년월일_시분초로 생성
            Date now = new Date();
            TimeZone timezone;
            timezone = TimeZone.getTimeZone("Asia/Seoul");
            formatter.setTimeZone(timezone);
            String filename = formatter.format(now) + ".png";                                       //최종 이름이 될 녀석
            sto_pho_path = filename;                                                                //전역변수에도 넣어줌
            //storage 주소와 폴더 파일명을 지정해 준다.(방Key의 하위폴더에 사진 넣기)
            StorageReference storageRef = storage.getReferenceFromUrl("gs://our-capstone-613a9.appspot.com").child(RKEY+"/"+QNAKEY+"/"+filename);
            storageRef.putFile(filepath)                                                            //올리기 시작
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            updt_reply();
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
}
