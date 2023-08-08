package com.example.tracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverMainMenuAct extends AppCompatActivity {

    //declaring variables
    FirebaseAuth firebaseAuth;
    Button btnDriverMap, btnDriverLogOut,btnDriverSettings,btnTimeTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_menu);

        //assign values for variables
        btnTimeTable= findViewById(R.id.btnDriverTimeTable);
        btnDriverMap = findViewById(R.id.btnDriverMap);
        btnDriverLogOut = findViewById(R.id.btnDriverLogOut);
        btnDriverSettings = findViewById(R.id.btnDriverSetting);
        firebaseAuth = FirebaseAuth.getInstance();

        //redirect to Driver settings Activity
        btnDriverSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMainMenuAct.this,DriverSettingsAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        //redirect to Driver Map Activity
        btnDriverMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMainMenuAct.this,DriverMapAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        //USer logout from application and redirect to Main Activity
        btnDriverLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(DriverMainMenuAct.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMainMenuAct.this,TimeTableAct.class);
                String KEY ="Driver";
                intent.putExtra("KEYTOTABLEDRV",KEY);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}