package com.example.our_capstone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PromiseDetailActivity extends AppCompatActivity {
    private static final String TAG = "hyunsoo";
    Intent intent = new Intent();
    String KEY;
    Calendar current;
    Button set_time, set_date, set_location;
    TextView distance_view;
    LocationManager lm;
    VoPromiseInfo promise;
    double distance;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promise_detail);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navlistener);
        Intent intent = getIntent();    //데이터 수신
        promise= (VoPromiseInfo) intent.getSerializableExtra("promise");
        Toast.makeText(getApplicationContext(),promise.get_attender().get(0).getClass().getName()+"-"+promise.get_attender().get(0),Toast.LENGTH_LONG).show();
        Log.d(TAG,promise.get_attender().get(0).getClass().getName()+"-"+promise.get_attender().get(0)+"!!!!!!!!!!!!!!!!!!!!!!!!");
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        current = Calendar.getInstance();
        set_date = findViewById(R.id.setdate);
        Calendar date_time=promise.get_date_time();
        set_date.setText(date_time.get(Calendar.YEAR) + "년 " + (date_time.get(Calendar.MONTH)+1)
                + "월 " + date_time.get(Calendar.DAY_OF_MONTH) + "일");

        set_time=findViewById(R.id.settime);
        int hh=date_time.get(Calendar.HOUR_OF_DAY);
        int mm=date_time.get(Calendar.MINUTE);
        if (mm == 0)
            set_time.setText(hh + ":" + "00");
        else
            set_time.setText(hh + ": " + mm);

        set_location=findViewById(R.id.setlocation);
        set_location.setText(promise.get_location());

        Button set_member=findViewById(R.id.member);
        List<HashMap> attender=promise.get_attender();
        String text="";
        for(HashMap mem:attender){
            String name=(String)mem.get("_name");
            String birthday = "("+((String)mem.get("_birth")).substring(0,2) + ")";
            String attender_info=name+birthday;
            text+=attender_info+",";
        }
            text=text.substring(0,text.length()-1); //마지막, cut}
            set_member.setText(text);
//        set_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDate();
//            }
//        });
//        set_time = findViewById(R.id.settime);
//        set_time.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                showTime();
//            }
//        });
//        set_location = findViewById(R.id.setlocation);
//        set_location.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gotoMapActivity(KEY);
//            }
//        });
//
//        add_promise = (Button) findViewById(R.id.add_promise);
//        add_promise.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String date = (String) set_date.getText();
//                String time = (String) set_time.getText();
//                String attender = (String) set_member.getText();
//                String location = (String) set_location.getText();
//                if (date.equals("날짜설정")) {
//                    Toast.makeText(getApplicationContext(), "날짜설정요망", Toast.LENGTH_LONG).show();
//                } else if (time.equals("시간설정")) {
//                    Toast.makeText(getApplicationContext(), "시간설정요망", Toast.LENGTH_LONG).show();
//                } else if (attender.equals("멤버설정"))
//                    Toast.makeText(getApplicationContext(), "멤버설정요망", Toast.LENGTH_LONG).show();
//                else if (location.equals("장소설정"))
//                    Toast.makeText(getApplicationContext(), "장소설정요망", Toast.LENGTH_LONG).show();      //하나라도 설정 안되었을경우
//                else {
//                    //Toast.makeText(getApplicationContext(),voPromiseInfo.get_date_time().toString(),Toast.LENGTH_LONG).show();      //하나라도 설정 안되었을경우
//                    apply(voPromiseInfo);
//                    gotoLateCheckActivity(KEY);
//                }
//            }
//        });
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( PromiseDetailActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        else{
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

//            txtResult.setText("위치정보 : " + provider + "\n" +
//                    "위도 : " + longitude + "\n" +
//                    "경도 : " + latitude + "\n" +
//                    "고도  : " + altitude);
            distance_view=(TextView)findViewById(R.id.distance);
            calculate_distance(longitude,latitude);
            Log.d(TAG,"longitude: "+longitude+"latitude: "+latitude);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
        }
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
    private void calculate_distance(Double user_lon,Double user_lat){
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
            distance=distance/1000;
            distance_view.setText("목적지로부터 거리"+format.format(distance)+"km");
        }//1km변환
        else{
            distance_view.setText("목적지로부터 거리"+format.format(distance)+"m");
        }
    }

final LocationListener gpsLocationListener = new LocationListener() {
public void onLocationChanged(Location location) {
//    String provider = location.getProvider();
    double longitude = location.getLongitude();
    double latitude = location.getLatitude();
//    double altitude = location.getAltitude();
    calculate_distance(longitude,latitude);
}
public void onStatusChanged(String provider, int status, Bundle extras) {}
public void onProviderEnabled(String provider) {}
public void onProviderDisabled(String provider){}
    };
}

