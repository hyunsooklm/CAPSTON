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
                            String[] es =emails.split(",");
                            num_roulette = es.length;
                            for(int i=0;i<num_roulette;i++) {
                                Log.d(TAG, "onCreate: "+es[i]);
                                STRINGS.add(es[i]);}
                            circleManager = new CircleManager(RulletActivity.this, num_roulette);
                            layoutRoulette.addView(circleManager);
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
        String text = "";
        angle = angle % 360;

        Log.d("roulette", "getResult : " + angle);
        int sweepAngle = 360 / num_roulette;
        if(angle>90 && angle<(90+sweepAngle)%360){
            text = STRINGS.get(0);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle)%360 && angle<(90+sweepAngle*2)%360){
            text = STRINGS.get(1);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*2)%360 && angle<(90+sweepAngle*3)%360){
            text = STRINGS.get(2);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*3)%360 && angle<(90+sweepAngle*4)%360){
            text = STRINGS.get(3);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*4)%360 && angle<(90+sweepAngle*5)%360){
            text = STRINGS.get(4);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*5)%360 && angle<(90+sweepAngle*6)%360){
            text = STRINGS.get(5);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*6)%360 && angle<(90+sweepAngle*7)%360){
            text = STRINGS.get(6);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*7)%360 && angle<(90+sweepAngle*8)%360){
            text = STRINGS.get(7);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*8)%360 && angle<(90+sweepAngle*9)%360){
            text = STRINGS.get(8);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*9)%360 && angle<(90+sweepAngle*10)%360){
            text = STRINGS.get(9);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*10)%360 && angle<(90+sweepAngle*11)%360){
            text = STRINGS.get(10);
            buildAlert(text);
        }
        else if(angle>(90+sweepAngle*11)%360 && angle<(90+sweepAngle*12)%360){
            text = STRINGS.get(11);
            buildAlert(text);
        }

        /*if (num_roulette == 5) {
            if (angle > 342 || angle <= 54) { // 11   2
                text = STRINGS.get(3);
                buildAlert(text);
            } else if (angle > 54 && angle <= 126) { // 333   3
                text = STRINGS.get(2);
                buildAlert(text);
            } else if (angle > 126 && angle <= 198) { // 222   4
                text = STRINGS.get(1);
                buildAlert(text);
            } else if (angle > 198 && angle <= 270) { // 111    0
                text = STRINGS.get(0);
                buildAlert(text);
            } else if (angle > 270 && angle <= 342) { // 22     1
                text = STRINGS.get(4);
                buildAlert(text);
            }
        } else if (num_roulette == 6) {
            if (angle > 330 || angle <= 30) { // 22
                text = STRINGS.get(4);
                buildAlert(text);
            } else if (angle > 30 && angle <= 90) { // 11
                text = STRINGS.get(3);
                buildAlert(text);
            } else if (angle > 90 && angle <= 150) { // 333
                text = STRINGS.get(2);
                buildAlert(text);
            } else if (angle > 150 && angle <= 210) { // 222
                text = STRINGS.get(1);
                buildAlert(text);
            } else if (angle > 210 && angle <= 270) { // 111
                text = STRINGS.get(0);
                buildAlert(text);
            } else if (angle > 270 && angle <= 330) { // 3
                text = STRINGS.get(5);
                buildAlert(text);
            }
        }*/
        tvResult.setText("Result : " + text);
    }

    // if you want use AlertDialog then use this
    private void buildAlert(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations")
                .setMessage("You have earned " + text + " points!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
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
        private int[] COLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.YELLOW, Color.WHITE};
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
                paint.setColor(COLORS[i]);
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
                canvas.drawText(STRINGS.get(i), textX, textY, paint);
                temp += sweepAngle;
            }
        }
    }
}
