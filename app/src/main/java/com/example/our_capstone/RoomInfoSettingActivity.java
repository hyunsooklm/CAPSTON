package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoomInfoSettingActivity extends AppCompatActivity {                                   //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info_setting);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        findViewById(R.id.apply_btn).setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            Intent intent = getIntent();    //데이터 수신
            String room_key = intent.getExtras().getString("room_key");
            switch (v.getId()){
                case R.id.apply_btn:                                       //22행에서 findView없으면 실행안댐
                    apply(KEY);
                    gotoRoomActivity(room_key);
                    break;
            }
        }
    };
    private void apply(String room_key){
        Log.d(TAG, "DocumentSnapshot successfully updated!"+room_key);
        FirebaseFirestore db = FirebaseFirestore.getInstance();                             //파이어베이스의 firestore (DB) 인스턴스 초기화
        DocumentReference nmRef = db.collection("rooms").document(room_key);
        EditText editText = (EditText)findViewById(R.id.room_nm);

// Set the "isCapital" field of the city 'DC'
        nmRef
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
        gotoRoomActivity(room_key);
    }
    private void gotoRoomActivity(String room_key) {
        Intent intent=new Intent(this,RoomActivity.class);
        intent.putExtra("room_key",room_key);
        startActivity(intent);
    }
}
