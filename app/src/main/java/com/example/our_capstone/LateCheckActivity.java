package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LateCheckActivity extends AppCompatActivity {
    private String KEY;
    private String TAG="hyunsoo_debug";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_check);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms")
                .document(KEY)
                .collection("promise")
                .orderBy("Date", Query.Direction.ASCENDING)
                .orderBy("Time",Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {                                                 //게시판의 글 불러오기
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        GridView gridView = findViewById(R.id.grid_rooms);
                        final GridListAdapter_qna adapter = new GridListAdapter_qna();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                                Log.d(TAG,(String)doc.get("Date")+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                VoPromiseInfo promise=make_promise(doc.get("Date").toString(),doc.get("Time").toString(),doc.get("Location").toString(),
                                        (Double)doc.get("lon"),(Double)doc.get("lat"),
                                        (ArrayList)doc.get("attender"),(ArrayList)doc.get("Later"),doc.getId());
                                try {
                                    if(yet_promise_day(promise.get_date_time())) //약속당일 아직 안되었을경우
                                        adapter.addpromise(promise); //약속리스트에서 보여준다.
                                    else{ //약속일 지났으면 db에서 삭제
                                        FirebaseFirestore delete_db = FirebaseFirestore.getInstance();
                                        delete_db.collection("rooms")
                                                .document(KEY)
                                                .collection("promise").document(doc.getId())
                                                .delete();
                                    }
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        }
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {          //해당 영역 클릭시 이동하게해줌
                                VoPromiseInfo promise = (VoPromiseInfo)adapter.getItem(position);
                                //Toast.makeText(getApplicationContext(),promise.get_location(),Toast.LENGTH_LONG).show();
                                gotoPromiseDetail(promise);
                            }
                        });
                    }
                });

        Button add_promise=(Button)findViewById(R.id.add_promise);
        add_promise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSetPromiseActivity();
            }
        });

    }
    public boolean yet_promise_day(Calendar promise_day) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar current=Calendar.getInstance();
        try {
            String pro_=sdf.format(promise_day.getTime());
            Date cur_date1 = sdf.parse(sdf.format(current.getTime()));
            Date promise_date2 = sdf.parse(sdf.format(promise_day.getTime()));
            Calendar cur = Calendar.getInstance();
            Calendar pro_day = Calendar.getInstance();
            cur.setTime(cur_date1);
            pro_day.setTime(promise_date2);

            if(cur.after(pro_day)){
//                Log.d(TAG,"약속어김!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                Log.d(TAG,"오늘: "+sdf.format(cur.getTime())+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                Log.d(TAG,"약속일:"+sdf.format(pro_day.getTime())+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return false;
            }
//            Log.d(TAG,"오늘: "+sdf.format(cur.getTime())+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            Log.d(TAG,"약속일:"+sdf.format(pro_day.getTime())+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return true;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoMenuActivity(KEY);
    }
    BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
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
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent=new Intent(this,AlbumActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoChatActivity(String room_key) {
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoMemberActivity() {
        Intent intent=new Intent(this,MemberActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoSetPromiseActivity() {
        Intent intent=new Intent(this,SetPromiseActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private void gotoPromiseDetail(VoPromiseInfo promise) {
        Intent intent=new Intent(this,PromiseDetailActivity.class);
        intent.putExtra("room_key",KEY);
        intent.putExtra("promise",promise);
        startActivity(intent);
        LateCheckActivity.this.finish();
    }
    private VoPromiseInfo make_promise(String date,String Time,String location,Double lon,Double lat,ArrayList<VoPromiseInfo.Member> attender,ArrayList<VoPromiseInfo.Member> later,String key){
        final int _year=2; final int _month=4;
        Calendar calendar=Calendar.getInstance();
        //201220
        String year,month,day;
        if(date.length()==6){
            year="20";
            year+=date.substring(0,2);
            month=date.substring(2,4);
            day=date.substring(4);
        }else{//20201220
            year=date.substring(0,4);
            month=date.substring(4,6);
            day=date.substring(6);
        }
        calendar.set(Integer.parseInt(year),Integer.parseInt(month)-1,Integer.parseInt(day)); //날짜설정
        int hh=Integer.parseInt(Time.substring(0,2));
        int mm=Integer.parseInt(Time.substring(3));
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);
        return new VoPromiseInfo(calendar,location,key,lon,lat,attender,later);
    }

    class GridListAdapter_qna extends BaseAdapter {
        ArrayList<VoPromiseInfo> promise_list = new ArrayList<VoPromiseInfo>();
        Context context;                //어플맄케이션 정보를 담고있는 객체
        public void addpromise(VoPromiseInfo promise){
            promise_list.add(promise);
        }
        @Override
        public int getCount() {
            return promise_list.size();
        }
        @Override
        public Object getItem(int position) {
            return promise_list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();;
            VoPromiseInfo promise = promise_list.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.greedy_promise, parent, false);     // greedy view안에는 각각 greedy_our.xml을 적용
            }
            String date_time="";
            SimpleDateFormat day_formatter = new SimpleDateFormat("yy.MM.dd-HH:mm");
            date_time=day_formatter.format(promise.get_date_time().getTime());//날짜
            String location=promise.get_location(); //장소
            int attender_num=promise.get_attender().size(); //인원
            TextView date = convertView.findViewById(R.id.Date_time);                                        //각 방의 이름이 들어갈 텍스트뷰
            date.setText(date_time);                                                             //각 방이름 설정
            TextView loca = convertView.findViewById(R.id.location);                                        //각 방의 이름이 들어갈 텍스트뷰
            loca.setText(location);                                                             //각 방이름 설정
            TextView attend_num = convertView.findViewById(R.id.attend_num);                                        //각 방의 이름이 들어갈 텍스트뷰
            attend_num.setText(attender_num+"");                                                             //각 방이름 설정
            return convertView;
        }
    }

}