package com.example.our_capstone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SetPromiseActivity extends AppCompatActivity {
    static final int REQ_ADD_CONTACT = 1;
    String TAG = "";
    Button set_time, set_date, set_location, set_member;
    int y = 0, m = 0, d = 0, h = 0, mi = 0;
    List Room_member;   //방 전체멤버
    List attender; //참가자

    private String KEY;
    String location;
    Double lon, lat;
    Calendar current;
    VoPromiseInfo voPromiseInfo;
    Calendar date_time;
    AlertDialog.Builder dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpromise);
        Intent intent = getIntent();    //데이터 수신
        voPromiseInfo = new VoPromiseInfo(); //약속객체
        Room_member = voPromiseInfo.VO_mem(); //방 전체 멤버
        attender = voPromiseInfo.get_attender(); //그 중 참가자들
        date_time = voPromiseInfo.get_date_time();
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        current = Calendar.getInstance();
        set_date = findViewById(R.id.setdate);
        set_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDate();
            }
        });
        set_time = findViewById(R.id.settime);
        set_time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTime();
            }
        });
        set_location = findViewById(R.id.setlocation);
        set_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMapActivity(KEY);
            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")                                                         //rooms 콜렉션 중에
                .document(KEY)                                                                      //현재 들어와있는 키값의 room
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d(TAG, "Current data: " + snapshot.getData() + snapshot.get("users"));
                            String emails = snapshot.get("users").toString();
                            emails = emails.replace("[", "");
                            emails = emails.replace("]", "");
                            emails = emails.replace(" ", "");
                            String[] es = emails.split(",");
                            //   final SetPromiseActivity.ListViewAdapter adapter = new SetPromiseActivity.ListViewAdapter();
                            for (int i = 0; i < es.length; i++) {
                                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                db1.collection("people").whereEqualTo("email", es[i])
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value,
                                                                @Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen failed.", e);
                                                    return;
                                                }
                                                for (QueryDocumentSnapshot doc : value) {
                                                    if (doc.getId() != null) {                              // 따온 정보들 그리드뷰에 넣어주기
                                                        String name = doc.get("name").toString();
                                                        Log.d(TAG, "Current data: " + name);
                                                        VoPromiseInfo.Member mem = new VoPromiseInfo.Member(name, doc.get("birth").toString());       //순서대로 name, content, date
                                                        Room_member.add(mem);
//                                                        adapter.add_mem(mem);
                                                        //gridView.setAdapter(adapter);
                                                    }
                                                }
                                            }
                                        });
                            }


                        }
                    }
                });
        set_member = findViewById(R.id.show_dialog);
        set_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListViewAdapter adapter = new ListViewAdapter(Room_member);
                AlertDialog.Builder builder = new AlertDialog.Builder(SetPromiseActivity.this)
               .setAdapter(adapter, null).setTitle("참가자선택")
                        .setPositiveButton("확정", new DialogInterface.OnClickListener() { // 버튼은 테마에 따라서 모양이 다르게 모임
                            public void onClick(DialogInterface dialog, int whichButton) {
                                        Toast.makeText(getApplicationContext(),"참여자 수: "+attender.size(),Toast.LENGTH_LONG).show();
                                }})//확정 버튼을 눌렀을때

                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                        Toast.makeText(getApplicationContext(),"참여자 수: "+attender.size(),Toast.LENGTH_LONG).show();
                            }}); //취소버튼을 눌렀을때

                AlertDialog alertDialog = builder.create();
                final ListView listView = alertDialog.getListView();
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // 여러 상품 선택을 위해 - 이 형태에서는 필요 없다
                listView.setDivider(new ColorDrawable(Color.LTGRAY));
                listView.setDividerHeight(1);
                listView.setFocusable(false); // false를 해줘야 row touch event 가능
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override//아이템이 눌렸을떄
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CheckBox cbGoLargeChecked = (CheckBox)view.findViewById(R.id.checkBox);
                        VoPromiseInfo.Member mem = (VoPromiseInfo.Member)Room_member.get(position); //mem가져와서
                        cbGoLargeChecked.setChecked(!cbGoLargeChecked.isChecked());//체크박스 기존꺼랑 반대로 check해주고
                        if(cbGoLargeChecked.isChecked()){
                            mem.set_Selected(true); //mem을 체크박스오 ㅏ동일하게 check
                            attender.add(mem);
                            Toast.makeText(getApplicationContext(),mem.get_name()+" 참여: "+mem.isSelected(),Toast.LENGTH_LONG).show();
                        }else{
                            mem.set_Selected(false);
                            attender.remove(mem);
                            Toast.makeText(getApplicationContext(),mem.get_name()+" 참여: "+mem.isSelected(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

                alertDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoMenuActivity(KEY);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            gotoRoomActivity(KEY);
                            return true;

                        case R.id.nav_chat:
                            gotoChatActivity(KEY);
                            return true;

                        case R.id.nav_menu:
                            gotoMenuActivity(KEY);
                            return true;

                        case R.id.nav_album:
                            gotoAlbumActivity(KEY);
                            return true;

                        case R.id.nav_member:
                            gotoMemberActivity();
                            return true;

                    }
                    return false;
                }
            };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { //location 설정
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_ADD_CONTACT) {
            if (resultCode == RESULT_OK) {
                // No 값을 int 타입에서 String 타입으로 변환하여 표시.
                location = intent.getStringExtra("location");
                set_location.setText(location);
                lat = intent.getDoubleExtra("lat", 0.0);
                lon = intent.getDoubleExtra("lon", 0.0);
                voPromiseInfo.set_location(location);
                voPromiseInfo.set_location_info(lat, lon);
            }
        }
    }

    protected void showDate() { //날짜설정
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { //확인눌렀을때
                date_time.set(Calendar.YEAR, year);
                date_time.set(Calendar.MONTH, month + 1);
                date_time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                set_date.setText(date_time.get(Calendar.YEAR) + "년 " + (date_time.get(Calendar.MONTH))
                        + "월 " + date_time.get(Calendar.DAY_OF_MONTH) + "일");
            }
        }, current.get(current.YEAR), current.get(current.MONTH), current.get(current.DATE));

        datePickerDialog.setMessage("메시지");
        datePickerDialog.show();
    }

    protected void showTime() {//시간설정
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new TimePickerDialog.OnTimeSetListener() { //spinner모드
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) { //확인눌렀을때
                        date_time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date_time.set(Calendar.MINUTE, minute);
                        if (minute == 0)
                            set_time.setText(hourOfDay + ":" + "00");
                        else
                            set_time.setText(hourOfDay + ": " + minute);
                    }
                }, 00, 00, false);
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.setMessage("메시지");
        timePickerDialog.show();
    }

    private void gotoMapActivity(String room_key) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, REQ_ADD_CONTACT);
    }

    private void gotoRoomActivity(String room_key) {
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        SetPromiseActivity.this.finish();
    }

    private void gotoMenuActivity(String room_key) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        SetPromiseActivity.this.finish();
    }

    private void gotoAlbumActivity(String room_key) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        SetPromiseActivity.this.finish();
    }

    private void gotoChatActivity(String room_key) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        SetPromiseActivity.this.finish();
    }

    private void gotoMemberActivity() {
        Intent intent = new Intent(this, MemberActivity.class);
        intent.putExtra("room_key", KEY);
        startActivity(intent);
        SetPromiseActivity.this.finish();
    }

    public void CheckboxClick(View view) {

    }


    public class ListViewAdapter extends BaseAdapter {
        Context context;                //어플맄케이션 정보를 담고있는 객체
        private List<VoPromiseInfo.Member> member;//어플맄케이션 정보를 담고있는 객체

        ListViewAdapter(List<VoPromiseInfo.Member> member){
            this.member=member;
        }
        @Override
        public int getCount() {
            return member.size();
        }

        @Override
        public Object getItem(int position) {
            return member.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void add_mem(VoPromiseInfo.Member mem) {
            member.add(mem);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();
            final VoPromiseInfo.Member mem_one = member.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.alert_set_member_row, parent, false);
            }
            final VoPromiseInfo.Member selected_mem=member.get(position); //선택받은 멤버

            final CheckBox cbProductChecked = (CheckBox)convertView.findViewById(R.id.checkBox);
            TextView nm = convertView.findViewById(R.id.name);                                        //각 방의 이름이 들어갈 텍스트뷰
            TextView bh = convertView.findViewById(R.id.birth);                                        //각 방의 이름이 들어갈 텍스트뷰

            cbProductChecked.setChecked(selected_mem.isSelected());  //selected_mem의 상태대로 check박스 체크되기(check박스 상태기억)
            cbProductChecked.setFocusable(false); // false를 해줘야 row touch event 가능
            cbProductChecked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cbProductChecked.isChecked()){
                        mem_one.set_Selected(cbProductChecked.isChecked());
                        attender.add(mem_one);
                    }else{
                        mem_one.set_Selected((cbProductChecked.isChecked()));
                        attender.remove(mem_one);
                    }
                }//체크박스 선택했을때
            });

           // cbProductChecked.setChecked(cbProductChecked.isChecked());
            String birthday = mem_one.get_birth() + "";
            birthday = birthday.substring(2);
            nm.setText(mem_one.get_name() + "");                                                          //멤버의 이름
            bh.setText(birthday + "");                                                       //멤버의 생일
            return convertView;
        }
    }
}
