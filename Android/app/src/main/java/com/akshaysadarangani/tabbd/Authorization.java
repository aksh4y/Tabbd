package com.akshaysadarangani.tabbd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class Authorization extends AppCompatActivity {
    PrefManager prefManager;
    CheckBox cb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        prefManager = new PrefManager(this);
        Intent intent = getIntent();
        final String userID = intent.getStringExtra("uid");
        final String userName = intent.getStringExtra("userName");
        TextView token = findViewById(R.id.token);
        cb = findViewById(R.id.checkBox);
        Button btn = findViewById(R.id.proceedBtn);
        token.setText(userID);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cb.isChecked())
                    prefManager.setDevicePaired(true);
                Intent myIntent = new Intent(Authorization.this, MainActivity.class);
                myIntent.putExtra("userName", userName);
                myIntent.putExtra("uid", userID);
                Authorization.this.startActivity(myIntent);
                finish();
            }
        });
    }
}
