package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);             //키보드 누르면 그 눌린 창 키보드 위로 올라감
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        findViewById(R.id.reply_btn2).setOnClickListener(onClickListener);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .collection("chats")                                                   //그 room의 모든 채팅기록들
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_chat adapter = new GridListAdapter_chat();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {

                                VoChatInfo chat = new VoChatInfo(doc.get("name").toString(),doc.get("content").toString(),doc.get("date").toString(),doc.get("email").toString());
                                adapter.addChat(chat);
                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setSelection(value.size()-1);


                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                // 해당 채팅 클릭해도 아무 행동도 안함.
                            }
                        });
                    }
                });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoRoomActivity();
    }
    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            gotoRoomActivity();
                            return true;

                        case R.id.nav_chat:
                            return true;

                        case R.id.nav_menu:
                            gotoMenuActivity();
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity();
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
                case R.id.reply_btn2:                                         //클릭을 sign_up_btn 누를때
                    apply();
                    break;
            }
        }
    };
    private void gotoRoomActivity() {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        ChatActivity.this.finish();
    }
    private void gotoMenuActivity() {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        ChatActivity.this.finish();
    }
    private void gotoAlbumActivity() {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        ChatActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        ChatActivity.this.finish();
    }
    class GridListAdapter_chat extends BaseAdapter {
        ArrayList<VoChatInfo> chats = new ArrayList<VoChatInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addChat(VoChatInfo chat){
            chats.add(chat);
        }
        @Override
        public int getCount() {
            return chats.size();
        }
        @Override
        public Object getItem(int position) {
            return chats.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            VoChatInfo chat = chats.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_chat, parent, false);
            }
            TextView nm = convertView.findViewById(R.id.name_txt);                                        //각 방의 이름이 들어갈 텍스트뷰
            TextView ct = convertView.findViewById(R.id.content_txt);                                        //각 방의 이름이 들어갈 텍스트뷰
            TextView dt = convertView.findViewById(R.id.time_txt);                                        //각 방의 이름이 들어갈 텍스트뷰
            nm.setText(chat.getName()+"");                                                             //각 방이름 설정
            ct.setText(chat.getContent()+"");
            dt.setText(chat.getDate()+"");

            if(user.getEmail().equals(chat.getEmail()+"")){                                         //채팅이 내 채팅이라면
                Log.d(TAG, "getView: -----"+ user.getEmail()+chat.getEmail()+"------");
                LinearLayout totchatLinear = convertView.findViewById(R.id.totchat);                //findviewbuId 앞은 그리드뷰 내부니까 무조건 convertView
                totchatLinear.setGravity(Gravity.RIGHT);
            }else{                                                                                  //else절 없으면 레이아웃 id들이 같아서 다 똑같은거 적용함!!!!
                LinearLayout totchatLinear = convertView.findViewById(R.id.totchat);                //findviewbuId 앞은 그리드뷰 내부니까 무조건 convertView
                totchatLinear.setGravity(Gravity.LEFT);
            }
            return convertView;
        }
    }
    private void apply() {
        EditText editText = findViewById(R.id.reply3);
        if(!editText.getText().toString().equals("")){
            Map<String, Object> chat = new HashMap<>();
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");             //이름이 년월일_시분초로 생성
            Date now = new Date();
            TimeZone timezone;
            timezone = TimeZone.getTimeZone("Asia/Seoul");
            formatter.setTimeZone(timezone);
            Log.d(TAG, "apply: "+ formatter.format(now));
            chat.put("name",user.getDisplayName().split("_")[0]);                            //[0] -> 이름, [1] -> 생일
            chat.put("content",editText.getText().toString());
            chat.put("email",user.getEmail());
            chat.put("date",formatter.format(now));
            FirebaseFirestore db = FirebaseFirestore.getInstance();                                     //파이어베이스의 firestore (DB) 인스턴스 초기화
            db.collection("rooms").document(KEY).collection("chats")
                    .add(chat)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //gotoQnaDetailActivity();
                        }});
            editText.setText("");                                                                   // 다시 비워줌
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        else{
            Toast.makeText(this, "답글을 작성하여주세요", Toast.LENGTH_LONG).show();
        }
    }
}
