package com.example.our_capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PopupInviteActivity extends Activity {                                                //메인클래스
    private static final String TAG = "AppCompatActivity";
    private String KEY="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            //메인함수
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popupinvite);
        Intent intent = getIntent();                                                                //데이터 수신
        KEY = intent.getExtras().getString("room_key");


        findViewById(R.id.inviteintoroomBtn).setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            switch (v.getId()){
                case R.id.inviteintoroomBtn:                                                           //73행에서 findView없으면 실행안댐
                    invite();
                    break;
            }
        }
    };
    private void invite(){
        final EditText editText = (EditText)findViewById(R.id.emailOfFriend);
        if(editText.getText().toString().equals("") || editText.getText().toString() == null){
            showToast("친구의 이메일을 입력해주세요");
        }else{
            //FireSTore의 People 콜렉션에 있는 이메일인지 체크
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("people")
                    .whereEqualTo("email", editText.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // 회원가입이 되어있다면 추가하기
                                    FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                    db1.collection("rooms").document(KEY).update("users", FieldValue.arrayUnion(editText.getText().toString()));
                                    showToast("친구를 초대하였습니다.");
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    onBackPressed();
                                }
                            } else {
                                showToast("친구의 이메일을 확인하여주세요.");
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }
    private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
