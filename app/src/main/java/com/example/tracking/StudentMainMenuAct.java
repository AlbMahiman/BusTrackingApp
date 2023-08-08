package com.example.tracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class StudentMainMenuAct extends AppCompatActivity {

    //declaring variables
    Button btnStudentMap, btnStudentLogOut,btnStudentSettings,btnTimeTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main_menu);

        //assign values for variables
        btnTimeTable= findViewById(R.id.btnStudentTimeTable);
        btnStudentMap = findViewById(R.id.btnStudentMap);
        btnStudentLogOut = findViewById(R.id.btnStudentLogOut);
        btnStudentSettings= findViewById(R.id.btnStudentSetting);

        btnStudentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentMainMenuAct.this,StudentSettingAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        btnStudentMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentMainMenuAct.this,StudentMapAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnStudentLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(StudentMainMenuAct.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentMainMenuAct.this,TimeTableAct.class);
                String KEY ="Student";
                intent.putExtra("KEYTOTABLESTD",KEY);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

}