package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.StructuredQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {                                   //메인클래스
    private static final String TAG = "AppCompatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                //파이어베이스의 인증 (회원관리) 이용해서 로그인정보 가져오기
        if (user == null) {                                                             //현재 상태가 로그인된 상태가 아니라면
            gotoSignInActivity();                                                       //로그인창으로 이동하기
        }
        else {
            findViewById(R.id.logout_btn).setOnClickListener(onClickListener);
            findViewById(R.id.grp_crt_btn).setOnClickListener(onClickListener);

            db.collection("rooms")
                    .whereArrayContains("users", user.getEmail())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            GridView gridView = findViewById(R.id.grid_rooms);
                            final GridListAdapter adapter = new GridListAdapter();
                            List<String> rooms = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.getId() != null) {
                                    ArrayList valueList = new ArrayList(doc.getData().values());
                                    VoRoomInfo room = new VoRoomInfo(doc.getId(), valueList, doc.get("name").toString(),doc.get("photo").toString());
                                    adapter.addRoom(room);
                                }
                            }
                            gridView.setAdapter(adapter);
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                    VoRoomInfo room = (VoRoomInfo)adapter.getItem(position);
                                    gotoRoomActivity(room);
                                }
                            });
                        }
                    });
        }
    }

    private void gotoRoomActivity(VoRoomInfo room) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room.getKey());
        startActivity(intent);
    }


    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.logout_btn:                                       //24행에서 findView없으면 실행안댐
                    FirebaseAuth.getInstance().signOut();                   //파이어베이스를 통해서 로그아웃하기
                    gotoSignInActivity();
                    break;
                case R.id.grp_crt_btn:                                      //25행 없으면 실행불가능
                    grpCreate();
                    break;
            }
        }
    };

    private void gotoSignInActivity(){
        Intent intent=new Intent(this,SignInActivity.class);
        startActivity(intent);
    }
    private void gotoRoomInfoSettingActivity(String room_key) {
        Intent intent=new Intent(this,RoomInfoSettingActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);                                                               //어차피 방정보 변경은 여기로 돌아오는것을 함수로 해서 없에줘야함
    }
    private void grpCreate(){                                                               //모임 생성하기 함수
        FirebaseFirestore db = FirebaseFirestore.getInstance();                             //파이어베이스의 firestore (DB) 인스턴스 초기화
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                    //파이어베이스의 인증 (회원관리) 이용해서 로그인정보 가져오기

        Map<String, Object> room = new HashMap<>();
        ArrayList users = new ArrayList();
        users.add(user.getEmail());
        room.put("users", users);
        room.put("name", "unknown");
        room.put("photo","default");
        room.put("index",1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");                        //이름이 년월일로 생성
        Date now = new Date();
        room.put("date",formatter.format(now));


// Add a new document with a generated ID
        db.collection("rooms")                                                      //새 컬렉션 시작 (상위 디렉토리 생성????) 넣을 때마다 이 컬렉션의 이름으로 계속 들어가지만 key값은 다 다름
                .add(room)                                                                       //map을 넣어줍니다.
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showToast("새 모임을 생성하였습니다.");
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Map<String, Object> our = new HashMap<>();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");             //이름이 년월일_시분초로 생성
                        Date now = new Date();
                        our.put("title",formatter.format(now) + "_OUR에 오신 것을 환영합니다.");
                        our.put("content","OUR에 오신 것을 환영합니다.");
                        our.put("author","관리자");
                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        CollectionReference citiesRef = db1.collection("rooms");       //아랫줄에서 방금 만든 방에 chats,앨범s,ours라는 하위 콜렉션 생성
                        //citiesRef.document(documentReference.getId()).collection("albums").add(album);
                        citiesRef.document(documentReference.getId()).collection("ours").add(our);
                        gotoRoomInfoSettingActivity(documentReference.getId());                     //방 만들면 방설정창으로 이동
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

    class GridListAdapter extends BaseAdapter{
        ArrayList<VoRoomInfo> rooms = new ArrayList<VoRoomInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addRoom(VoRoomInfo room){
            rooms.add(room);
        }
        @Override
        public int getCount() {
            return rooms.size();
        }
        @Override
        public Object getItem(int position) {
            return rooms.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoRoomInfo room = rooms.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_rooms, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.nm);                                        //각 방의 이름이 들어갈 텍스트뷰
            nm.setText(room.getName());                                                             //각 방이름 설정
            if(!room.getPhoto().equals("default")) {                                                //방의 메인 화면이 지정되어있다면
                final ImageView im = convertView.findViewById(R.id.imageView1);
                StorageReference ref = FirebaseStorage.getInstance().getReference();
                StorageReference pathReference = ref.child(room.getKey()+"/"+room.getPhoto());
                pathReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            // Glide 이용하여 이미지뷰에 로딩
                            Glide.with(MainActivity.this)
                                    .load(task.getResult())
                                    .into(im);
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            return convertView;
        }
    }
        private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}