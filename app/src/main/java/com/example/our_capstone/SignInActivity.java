package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
//
import com.kakao.usermgmt.LoginButton;

//---------------------------=----
public class SignInActivity extends AppCompatActivity {                         //메인클래스
    private FirebaseAuth mAuth;
    //파이어 베이스 인스턴스 선언
    private static final String TAG = "SignUpActivity";
    private SessionCallback sessionCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {                        //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);                              //이 자바가 참조할 페이지, 첫화면으로 지정하는 것은 AndroidManifest.xml에서!!!
        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
       // Session.getCurrentSession().checkAndImplicitOpen(); //현재 앱에 유효한 카카오 로그인 토큰이 있다면, 바로 로그인일 시켜주는 함수
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();                                     //파이어베이스 인스턴스 초기화

        findViewById(R.id.sign_up_btn).setOnClickListener(onClickListener);     // xml에서 sign_up_btn이라는 id의 위젯 가져오고 이 친구의 클릭리스너는 밑의 onClickListener이다!!!
        findViewById(R.id.goto_sign_up_btn).setOnClickListener(onClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            Log.i("hello world","open!open!open!open!open!open!open!open!open!open!open!open!");
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();
                    Log.i("hello world","failfailfailfailfailfailfailfailfailfailfailfailfail");
                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                    UserAccount kakaoAccount = result.getKakaoAccount();
                    Log.i("hello world","hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!hello world!!!!!!!!");
                    Log.i("KAKAO_API", "id: " + result.getId());

                    intent.putExtra("user_id", result.getId());

                    if (kakaoAccount != null) {
                        //이메일
                        String email=kakaoAccount.getEmail();//email

                        Log.i("KAKAO_API", "email: " + email);
                        if(email!=null){
                            intent.putExtra("user_email", kakaoAccount.getEmail());
                        }else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                            // 동의 요청 후 이메일 획득 가능
                            // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                        } else {
                            // 이메일 획득 불가
                        }
                        //이름
                        Profile profile=kakaoAccount.getProfile();
                        if(profile!=null){
                            String name=profile.getNickname();
                            Log.i("KAKAO_API", "nickname: " + profile.getNickname());
                            intent.putExtra("user_name", profile.getNickname());
                        }else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                            // 동의 요청 후 프로필 정보 획득 가능

                        } else {
                            // 프로필 획득 불가
                        }
                    }
                    Log.d("before go home","before moveout!before moveout!before moveout!before moveout!before moveout!");
                    startActivity(intent);
                    finish();

                }
            });
        }
        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Log.i("hello world","no_internetno_internetno_internetno_internetno_internetno_internetno_internet");
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
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
