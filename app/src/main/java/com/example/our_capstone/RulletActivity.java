package com.example.our_capstone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RulletActivity extends AppCompatActivity {
    private CircleManager circleManager;
    private RelativeLayout layoutRoulette;
    private Button btnDrawRoulette5;
    private Button btnDrawRoulette6;
    private Button btnRotate;
    private TextView tvResult;
    private TextView emResult;
    private ArrayList<String> STRINGS2 = new ArrayList<String>();
    private ArrayList<String> STRINGS = new ArrayList<String>();
    private float initAngle = 0.0f;
    private int num_roulette;
    private static final String TAG = "AppCompatActivity";
    private String KEY ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rullet);
        Intent intent = getIntent();    //데이터 수신
        String room_key = intent.getExtras().getString("room_key");
        KEY = room_key;
        tvResult = findViewById(R.id.tvResult);
        emResult = findViewById(R.id.em_res);
        btnRotate = findViewById(R.id.btnRotate);
        layoutRoulette = findViewById(R.id.layoutRoulette);
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
                            Log.d(TAG, "Current data: " + snapshot.getData()+snapshot.get("users"));
                            String emails = snapshot.get("users").toString();
                            emails = emails.replace("[","");
                            emails = emails.replace("]","");
                            emails = emails.replace(" ","");
                            final String[] es =emails.split(",");                                     //이메일에 대한 문자열 배열로 이름들 얻기
                            for(int i=0 ; i< es.length; i++){
                                Log.w(TAG, "-----------"+es[i]+es.length);
                                STRINGS.add(es[i]);
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
                                                        Log.d(TAG, "Current data: " + doc.get("name").toString());
                                                        STRINGS2.add(doc.get("name").toString());
                                                        num_roulette = es.length;
                                                        Log.d(TAG, "onEvent: "+STRINGS2.size()+"============"+es.length);
                                                        if(es.length == STRINGS2.size()){
                                                            circleManager = new CircleManager(RulletActivity.this, num_roulette);
                                                            layoutRoulette.addView(circleManager);
                                                            Log.d(TAG, "-----------------onEvent: paint!--------------------");
                                                        }
                                                    }
                                                }

                                            }
                                        });
                            }

                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });


        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateLayout(layoutRoulette, num_roulette);
            }
        });
    }
    @Override public void onBackPressed(){                                                          //뒤로가기 버튼 눌리면
        super.onBackPressed();
        moveTaskToBack(true);
        gotoMenuActivity();
    }
    private void gotoMenuActivity() {
        Intent intent=new Intent(this,MenuActivity.class);
        intent.putExtra("room_key",KEY);
        startActivity(intent);
        RulletActivity.this.finish();
    }

    public void rotateLayout(final RelativeLayout layout, final int num) {
        final float fromAngle = getRandom(360) + 3600 + initAngle;
        Log.d(TAG, "rotateLayout: "+ fromAngle);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getResult(fromAngle, num); // start when animation complete
            }
        }, 3000);

        RotateAnimation rotateAnimation = new RotateAnimation(initAngle, fromAngle,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator));
        rotateAnimation.setDuration(3000);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setFillAfter(true);
        layout.startAnimation(rotateAnimation);
    }

    // get Angle to random
    private int getRandom(int maxNumber) {
        double r = Math.random();
        return (int)(r * maxNumber);
    }

    private void getResult(float angle, int num_roulette) {
        String text1 = "";
        String text2 = "";
        angle = angle % 360;


        Log.d("roulette", "getResult : " + angle);
        int sweepAngle = 360 / num_roulette;
        //270 - sweepAngle ~ 270 돌아야 0번 갖고오기 가능
        if(angle>270 - sweepAngle && angle<270){
            text1 = STRINGS2.get(0);
            text2 = STRINGS.get(0);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*2)%360 && angle<(((36000+270 - sweepAngle*1)%360)==0 ? 360 :(36000+270 - sweepAngle*1)%360)){
            text1 = STRINGS2.get(1);
            text2 = STRINGS.get(1);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*3)%360 && angle<(((36000+270 - sweepAngle*2)%360)==0 ? 360 :(36000+270 - sweepAngle*2)%360)){
            text1 = STRINGS2.get(2);
            text2 = STRINGS.get(2);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*4)%360 && angle<(((36000+270 - sweepAngle*3)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)) {
            text1 = STRINGS2.get(3);
            text2 = STRINGS.get(3);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*5)%360 && angle<(((36000+270 - sweepAngle*4)%360)==0 ? 360 :(36000+270 - sweepAngle*4)%360)){
            text1 = STRINGS2.get(4);
            text2 = STRINGS.get(4);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*6)%360 && angle<(((36000+270 - sweepAngle*5)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(5);
            text2 = STRINGS.get(5);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*7)%360 && angle<(((36000+270 - sweepAngle*6)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(6);
            text2 = STRINGS.get(6);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*8)%360 && angle<(((36000+270 - sweepAngle*7)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(7);
            text2 = STRINGS.get(7);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*9)%360 && angle<(((36000+270 - sweepAngle*8)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(8);
            text2 = STRINGS.get(8);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*10)%360 && angle<(((36000+270 - sweepAngle*9)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(9);
            text2 = STRINGS.get(9);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*11)%360 && angle<(((36000+270 - sweepAngle*10)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(10);
            text2 = STRINGS.get(10);
            buildAlert(text1);
        }
        else if(angle>(36000+270 - sweepAngle*12)%360 && angle<(((36000+270 - sweepAngle*11)%360)==0 ? 360 :(36000+270 - sweepAngle*3)%360)){
            text1 = STRINGS2.get(11);
            text2 = STRINGS.get(11);
            buildAlert(text1);
        }

        tvResult.setText("당첨 : " + text1);
        emResult.setText(text2);
    }

    // if you want use AlertDialog then use this
    private void buildAlert(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("---당첨자---")
                .setMessage("당첨자는 " + text + " 입니다!")
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        layoutRoulette.setRotation(360 - initAngle);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class CircleManager extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int[] COLORS = {Color.rgb(224,255,255), Color.rgb(255,240,245), Color.rgb(255,255,224)};
        private int num;

        public CircleManager(Context context, int num) {
            super(context);
            this.num = num;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = layoutRoulette.getWidth();
            int height = layoutRoulette.getHeight();
            int sweepAngle = 360 / num;

            RectF rectF = new RectF(0, 0, width, height);
            Rect rect = new Rect(0, 0, width, height);

            int centerX = (rect.left + rect.right) / 2;
            int centerY = (rect.top + rect.bottom) / 2;
            int radius = (rect.right - rect.left) / 2;

            int temp = 0;

            for (int i = 0; i < num; i++) {
                if(i == num -1) paint.setColor(COLORS[2]);
                else if(i%2==0) paint.setColor(COLORS[0]);
                else paint.setColor(COLORS[1]);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setAntiAlias(true);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawArc(rectF, temp, sweepAngle, true, paint);

                float medianAngle = (temp + (sweepAngle / 2f)) * (float) Math.PI / 180f;

                paint.setColor(Color.BLACK);
                paint.setTextSize(64);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);

                float arcCenterX = (float) (centerX + (radius * Math.cos(medianAngle))); // Arc's center X
                float arcCenterY = (float) (centerY + (radius * Math.sin(medianAngle))); // Arc's center Y

                // put text at middle of Arc's center point and Circle's center point
                float textX = (centerX + arcCenterX) / 2;
                float textY = (centerY + arcCenterY) / 2;

                Log.d(TAG, "onDraw: "+STRINGS.get(i));
                canvas.drawText(STRINGS2.get(i), textX, textY, paint);
                temp += sweepAngle;
            }
        }
    }
}
