package com.example.our_capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.auth.Session;

import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.exception.KakaoException;

import java.util.HashMap;
import java.util.Map;


public class SignInActivity extends AppCompatActivity {                         //메인클래스
    private FirebaseAuth mAuth;                                                 //파이어 베이스 인스턴스 선언
    private SessionCallback sessionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                        //메인함수
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);                              //이 자바가 참조할 페이지, 첫화면으로 지정하는 것은 AndroidManifest.xml에서!!!
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();                                     //파이어베이스 인스턴스 초기화
        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);

        findViewById(R.id.sign_up_btn).setOnClickListener(onClickListener);     // xml에서 sign_up_btn이라는 id의 위젯 가져오고 이 친구의 클릭리스너는 밑의 onClickListener이다!!!
        findViewById(R.id.goto_sign_up_btn).setOnClickListener(onClickListener);
        //findViewById(R.id.btn_kakao_login).setOnClickListener(onClickListener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();// 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        String email, password, name, birthday;

        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();
                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }  //여기 맞아
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(), "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    Log.d("a","success!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    password = "" + result.getId();
                    Log.d("a","password:"+password);
                    UserAccount kakaoAccount = result.getKakaoAccount();
                    if (kakaoAccount != null) {
                        if (kakaoAccount.getEmail() != null) {
                            email = kakaoAccount.getEmail();//이메일행
                            Log.d("aaa", "email:" + email);
                        }
                        if (kakaoAccount.getBirthday() != null) {
                            birthday = kakaoAccount.getBirthday();//생일
                            Log.d("aaa", "birthday:" + birthday);
                        }
                        Profile profile = kakaoAccount.getProfile();
                        if (profile != null) {
                            name = profile.getNickname();//이름
                            Log.d("aaa", "name:" + name);
                        }
                        kakao_signIn(email, password, name, birthday);
                    }
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();                  //현재 로그인이 되어있는지 firebase에서 로그인 상태가져오기
    }

    @Override
    public void onBackPressed() {                                  //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);                                               //뒤로 가지말고 꺼버리기
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_up_btn:                                         //클릭을 sign_up_btn 누를때(31행 없으면 실행안댐)
                    signIn();
                    break;
                case R.id.goto_sign_up_btn:                                    //(32행없으면 실행안댐)
                    gotoSignUpActivity();
                    break;
            }
        }
    };                                                                          //왜 세미콜론이 들어가는지 모르겠음...

    private void signIn() {
        String email = ((EditText) findViewById(R.id.sign_up_id)).getText().toString();          //email이라는 String에 입력한 값 받아와서 string으로 넣어주기
        String password = ((EditText) findViewById(R.id.sign_up_pw)).getText().toString();
        if (email.length() > 0 || password.length() > 0) {

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
        } else {
            showToast("이메일 혹은 비밀번호를 입력해주세요.");
        }
    }
    private void kakao_signIn(final String email, final String password, final String name, final String birthday) {
        /*카카오로 회원가입->다시 재로그인 과정*/
        mAuth.signInWithEmailAndPassword(email, password) //로그인
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//이미 등록된 카카오id는 바로 로그인
                            showToast("로그인 성공!");
                            FirebaseUser user = mAuth.getCurrentUser();
                            gotoMainActivity();
                        } else {//회원가입되지 않은 카카오id는 회원가입시켜주고 재로그인
                            if(kakao_signUp(email, password, name, birthday)){ //회원가입성공시
                                kakao_signIn(email, password, name, birthday);//재로그인
                            }
                        }
                    }
                });
    }

    private void showToast(String msg){                                                            //메세지 alert띄우기
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);                      //실행하려는 엑티비티 이름 intent 인스턴스에 넣어주기
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                                       //메인창에서 뒤로가기 눌렀을 때 바로 로그인창이 아니라 꺼지게 하려고!!!
        startActivity(intent);                                                                  //엑티비티 이동
    }

    private void gotoSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);                     //실행하려는 엑티비티 이름 intent 인스턴스에 넣어주기
        startActivity(intent);                                                                  //엑티비티 이동
    }
    public boolean kakao_signUp(final String email, final String password, final String name, final String birthday) {
        /*카카오로 회원가입, TF는 회원가입 성공/실패 여부 담는 변수, 그냥 boolean은 안되고 저 형태여야 되더라고..*/
        //알아서 중복되는 id는 못올라가고 에러나게 파이어베이스가 막드라!!!
        final boolean[] TF = {false};
        if (email.length() > 0 && password.length() > 0) {
            mAuth.createUserWithEmailAndPassword(email, password) //회원가입코드
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                String fakebirthday = "00"+birthday;
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name+'_'+fakebirthday)                                     // 이름 넣기(생년월일 넣을 때가 없어서 여기따가 넣겠습니다.)
                                        //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))  //프사 넣기
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("kakaosignup", "User profile updated.");
                                                }
                                            }
                                        });
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> person = new HashMap<>();
                                person.put("name",name);
                                person.put("birth",fakebirthday);
                                person.put("email",email);
                                db.collection("people").document(user.getUid()).set(person);
                                showToast("카카오 연동 회원가입 완료!"); //여기까지가 카카오로그인 누르면 회원가입
                                TF[0] = true; //회원가입성공
                            } else {
                                showToast("카카오 연동 회원가입 실패!"); //여기까지가 카카오로그인 누르면 회원가입
                            }
                        }
                    });//회원가입코드
            return TF[0];
        } else{
            showToast("KAKAO_EMAIL 및 PASSWORD 확인요망");
            return false;
        }
    }
}
