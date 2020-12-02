package com.example.our_capstone;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PromiseDetailActivity extends AppCompatActivity {
    private static final String TAG = "hyunsoo";
    String KEY;
    String PROMISE_KEY;
    Calendar current,date_time;
    Button set_time, set_date, set_location,arrive_button,delete,modify;
    TextView distance_view,later_list;
    LocationManager lm;
    VoPromiseInfo promise;
    float distance;
    List<HashMap> later;
    FirebaseUser user;
    Double user_lon,user_lat;
    int limit_distance=2000;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promise_detail);
        user = FirebaseAuth.getInstance().getCurrentUser();                //파이어베이스의 인증 (회원관리) 이용해서 로그인정보 가져오기
        if (user == null) {                                                             //현재 상태가 로그인된 상태가 아니라면
            Log.d(TAG, "로그인된상태가아니야??");                                                       //로그인창으로 이동하기
        }
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
            Intent intent = getIntent();    //데이터 수신
            promise = (VoPromiseInfo) intent.getSerializableExtra("promise");
            later = promise.get_late_comer();
            PROMISE_KEY = promise.get_key();
            Toast.makeText(getApplicationContext(), promise.get_attender().get(0).getClass().getName() + "-" + promise.get_attender().get(0), Toast.LENGTH_LONG).show();
            Log.d(TAG, promise.get_attender().get(0).getClass().getName() + "-" + promise.get_attender().get(0) + "!!!!!!!!!!!!!!!!!!!!!!!!");
            String room_key = intent.getExtras().getString("room_key");
            KEY = room_key;
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            arrive_button = (Button) findViewById(R.id.arrive_button);
            later_list = (TextView) findViewById(R.id.later_list);
            distance_view = (TextView) findViewById(R.id.distance);
            delete=(Button)findViewById(R.id.delete);
            modify=(Button)findViewById(R.id.modify);

//-----------------------------------------------------------------
            current = Calendar.getInstance();
            set_date = findViewById(R.id.setdate);
            date_time = promise.get_date_time();
            set_date.setText(date_time.get(Calendar.YEAR) + "년 " + (date_time.get(Calendar.MONTH) + 1)
                    + "월 " + date_time.get(Calendar.DAY_OF_MONTH) + "일");

            set_time = findViewById(R.id.settime);
            int hh = date_time.get(Calendar.HOUR_OF_DAY);
            int mm = date_time.get(Calendar.MINUTE);
            if (mm == 0)
                set_time.setText(hh + ":" + "00");
            else
                set_time.setText(hh + ": " + mm);

            set_location = findViewById(R.id.setlocation);
            set_location.setText(promise.get_location());
//----------------------------------------------------------------- //날짜나타내기
            Button set_member = findViewById(R.id.member);
            List<HashMap> attender = promise.get_attender();
            String text = "";
            for (HashMap mem : attender) {
                String name = (String) mem.get("_name");
                text += name + ",";
            }
            text = text.substring(0, text.length() - 1); //마지막, cut}
            set_member.setText(text);
//-----------------------------------------------------------------//member 나타내기

            if (before_promisetime()) { //약속시간전
                arrive_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (calculate_distance(user_lon, user_lat) < limit_distance) { //도착!
                            out_from_later();
                        } else {
                            Toast.makeText(getApplicationContext(), "도착전", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }   //약속시간 전 도착버튼 누르면 later리스트에서 사라짐
            else {
                    arrive_button.setVisibility(View.GONE);
                    distance_view.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    modify.setVisibility(View.GONE);
                    later_list.setVisibility(View.VISIBLE);
                    String late_list=later_list.getText().toString();
                    for(HashMap mem:later){
                        late_list+=mem.get("_name")+",";
                    }
                    if(later.size()>0){ //지각자 한명이라도 있을경우
                        late_list=late_list.substring(0,late_list.length()-1);
                        later_list.setText(late_list);
                    }
                    else
                        later_list.setText("지각자 없음!");
            }//약속시간 이후
//-----------------------------------------------------------------------------약속시간 전 후, 거리계산+지각자리스트 나타내기

//-----------------------------------------------------------------------------약속삭제
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_dialog(v);
            }
        });



//-----------------------------------------------------------------------------약속수정
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PromiseDetailActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            } else {
                Location location = getLocation();
                user_lon = location.getLongitude();
                user_lat = location.getLatitude();
                double altitude = location.getAltitude();
                calculate_distance(user_lon, user_lat);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
            }
        }
//---------------------------------------------------------------------------------activity 켤때, 거리계산(최초)
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
    //------------------------------------------------------------------------navigation
    @Override
    public void onBackPressed() {                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoMenuActivity(KEY);
    }
    private void gotoAlbumActivity(String room_key) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }

    private void gotoChatActivity(String room_key) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }

    private void gotoMemberActivity() {
        Intent intent = new Intent(this, MemberActivity.class);
        intent.putExtra("room_key", KEY);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }
    private void gotoMenuActivity(String room_key) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }
    private void gotoLateCheckActivity(String room_key) {
        Intent intent = new Intent(this, LateCheckActivity.class);
        intent.putExtra("room_key", room_key);
        startActivity(intent);
        PromiseDetailActivity.this.finish();
    }
    private float calculate_distance(Double user_lon,Double user_lat){  //view에 거리나타내주고, 거리반환
        Location user_location = new Location("user_point");
        user_location.setLatitude(user_lat);
        user_location.setLongitude(user_lon);

        Location promise_location = new Location("promise_spot");
        promise_location.setLatitude(promise.get_lat());
        promise_location.setLongitude(promise.get_lon());
        distance = user_location.distanceTo(promise_location);
        DecimalFormat format=new DecimalFormat();
        format.applyLocalizedPattern("0.0");
        Log.d(TAG,"목적지로부터 거리"+distance+"M");
        if(distance>=1000){
            float distance_km=distance/1000;
            distance_view.setText("목적지로부터 거리"+format.format(distance_km)+"km");
        }//1km변환
        else{
            distance_view.setText("목적지로부터 거리"+format.format(distance)+"m");
        }
        return distance;
    }
//------------------------------------------------------------------------------거리계산함수
    private boolean before_promisetime(){
        if(current.after(this.date_time))
            return false;
        else
            return true;
    }
//------------------------------------------------------------------------------약속시간 전,후 확인함수
    private String user_name(){
        Log.d(TAG,"USR==NULL?:"+(user==null));
        String user_info=(String)user.getDisplayName();
        String name=user_info.split("_")[0];
        return name;
    }
    //------------------------------------------------------------------------------사용자 이름따오기
    private void out_from_later(){
        Iterator<HashMap> it=later.iterator();
        while(it.hasNext()){
            HashMap mem=(HashMap)it.next();
            if(mem.get("_name").equals(user_name())){
                it.remove();
                break;
            }
        }       //자바에서 loop도중 데이터 수정해야할시, iterator를 쓰도록하자.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference promise_Ref =db.collection("rooms").document(KEY).collection("promise").document(PROMISE_KEY);
        promise_Ref.update("Later",later).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"survived!!!",Toast.LENGTH_LONG).show();
            }
        });
    }
    //---------------------------------------------------------------------------------제시각에 도착한 사람 지각자 명단에서 빼주기
    public void delete_dialog(View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference promise_Ref =db.collection("rooms").document(KEY).collection("promise").document(PROMISE_KEY);
            AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(PromiseDetailActivity.this);
            // alert의 title과 Messege 세팅
            myAlertBuilder.setTitle("삭제");
            myAlertBuilder.setMessage("삭제하시겠습니까?");
            // 버튼 추가 (Ok 버튼과 Cancle 버튼 )
            myAlertBuilder.setPositiveButton("예",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    // OK 버튼을 눌렸을 경우
                    promise_Ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_LONG).show();
                            gotoLateCheckActivity(KEY);
                        }
                    });
                }
            });
            myAlertBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Cancle 버튼을 눌렸을 경우

                }
            });
            // Alert를 생성해주고 보여주는 메소드(show를 선언해야 Alert가 생성됨)
            myAlertBuilder.show();
        }
//------------------------------------------------------------------------------------------------약속 삭제
    public Location getLocation() {
        Location location=null;
        try {
            long MIN_TIME_BW_UPDATES=1000;
            float MIN_DISTANCE_CHANGE_FOR_UPDATES=1;
            // GPS, NETWORK 활성화 여부 확인
            Boolean isGPSEnabled = lm.
                    isProviderEnabled(LocationManager.GPS_PROVIDER);
            Boolean isNetworkEnabled =lm.
                    isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    lm.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, gpsLocationListener);//LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener
                    if (lm != null) {
                        location = lm
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                        }
                    }
                } else if (isGPSEnabled) {
                    if (location == null) {
                        lm.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, gpsLocationListener);
                        if (lm != null) {
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            user_lon = location.getLongitude();
            user_lat = location.getLatitude();
            distance=calculate_distance(user_lon,user_lat);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider){}
    };
}
