package com.example.our_capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Main2Activity extends Activity {

    String email;
    Long id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvNickname = findViewById(R.id.tvNickname);

        Intent intent = getIntent();
        email = intent.getStringExtra("user_email");
        id = intent.getLongExtra("user_id", 0);
        Log.d("in main","in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!in main2!!");
        Log.d("what???",email+id);
        Log.d("what???",email+id);
        Log.d("what???",email+id);
    }
}
