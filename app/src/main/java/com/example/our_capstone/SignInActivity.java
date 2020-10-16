package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.auth.Session;
//--------------------------------
import androidx.annotation.Nullable;

import com.kakao.auth.AuthType;
import com.kakao.usermgmt.LoginButton;

//---------------------------=----
public class SignInActivity extends AppCompatActivity {                         //메인클래스
    private FirebaseAuth mAuth;                                                 //파이어 베이스 인스턴스 선언
    private static final String TAG = "SignUpActivity";
    private Button btn_custom_login;
    private LoginButton btn_kakao_login;
//    private Button btn_custom_login_out;
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                        //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);                              //이 자바가 참조할 페이지, 첫화면으로 지정하는 것은 AndroidManifest.xml에서!!!
        // Initialize kakao_login button
//        btn_custom_login = (Button) findViewById(R.id.btn_custom_login);
        btn_kakao_login = (LoginButton) findViewById(R.id.btn_kakao_login);
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);
        btn_kakao_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_kakao_login.performClick();
                gotoMainActivity();
            }
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();                                     //파이어베이스 인스턴스 초기화

        findViewById(R.id.sign_up_btn).setOnClickListener(onClickListener);     // xml에서 sign_up_btn이라는 id의 위젯 가져오고 이 친구의 클릭리스너는 밑의 onClickListener이다!!!
        findViewById(R.id.goto_sign_up_btn).setOnClickListener(onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

      @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();                  //현재 로그인이 되어있는지 firebase에서 로그인 상태가져오기
    }

    @Override public void onBackPressed(){                                  //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);                                               //뒤로 가지말고 꺼버리기
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
             switch (v.getId()){
                 case R.id.sign_up_btn:                                         //클릭을 sign_up_btn 누를때(31행 없으면 실행안댐)
                     signIn();
                     break;
                 case R.id.goto_sign_up_btn:                                    //(32행없으면 실행안댐)
                     gotoSignUpActivity();
                     break;
             }
        }
    };                                                                          //왜 세미콜론이 들어가는지 모르겠음...

    private void signIn(){
        String email = ((EditText)findViewById(R.id.sign_up_id)).getText().toString();          //email이라는 String에 입력한 값 받아와서 string으로 넣어주기
        String password = ((EditText)findViewById(R.id.sign_up_pw)).getText().toString();
        if(email.length()>0 || password.length()>0 ){

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showToast("로그인 성공!");
                                FirebaseUser user = mAuth.getCurrentUser();
                                gotoMainActivity();
                            } else {
                                showToast(task.getException().toString());
                            }
                        }
                    });
        }
        else{
            showToast("이메일 혹은 비밀번호를 입력해주세요.");
        }
    }
    private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void gotoMainActivity(){
        Intent intent=new Intent(this,MainActivity.class);                      //실행하려는 엑티비티 이름 intent 인스턴스에 넣어주기
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                                       //메인창에서 뒤로가기 눌렀을 때 바로 로그인창이 아니라 꺼지게 하려고!!!
        startActivity(intent);                                                                  //엑티비티 이동
    }
    private void gotoSignUpActivity(){
        Intent intent=new Intent(this, SignUpActivity.class);                     //실행하려는 엑티비티 이름 intent 인스턴스에 넣어주기
        startActivity(intent);                                                                  //엑티비티 이동
    }
}
