package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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
                                    VoRoomInfo room = new VoRoomInfo(doc.getId(), valueList);
                                    adapter.addRoom(room);
                                    rooms.add(doc.getId());
                                }
                            }
                            gridView.setAdapter(adapter);
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    private void grpCreate(){                                                               //모임 생성하기 함수
        FirebaseFirestore db = FirebaseFirestore.getInstance();                             //파이어베이스의 firestore (DB) 인스턴스 초기화
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                    //파이어베이스의 인증 (회원관리) 이용해서 로그인정보 가져오기

        Map<String, Object> room = new HashMap<>();
        ArrayList users = new ArrayList();
        users.add(user.getEmail());
        room.put("users", users);

// Add a new document with a generated ID
        db.collection("rooms")                                                      //새 컬렉션 시작 (상위 디렉토리 생성????) 넣을 때마다 이 컬렉션의 이름으로 계속 들어가지만 key값은 다 다름
                .add(room)                                                                       //map을 넣어줍니다.
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showToast("새 모임을 생성하였습니다.");
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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
            TextView nm = convertView.findViewById(R.id.nm);
            nm.setText(room.getKey());
            return convertView;
        }
    }
        private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}