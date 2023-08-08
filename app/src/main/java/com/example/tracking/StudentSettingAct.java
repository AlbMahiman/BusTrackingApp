package com.example.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class StudentSettingAct extends AppCompatActivity {

    //this is same as Driver setting Activity
    private Button btnStdBack,btnStdConfirm;
    private EditText edtxtStdPhone, edtxtStdName, edtxtStdNSBMID;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private String UserId,stdName,stdNSBMID,stdPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_setting);

        btnStdBack=findViewById(R.id.btnStdBack);
        btnStdConfirm = findViewById(R.id.btnStdConfirm);

        edtxtStdName=findViewById(R.id.edTxtStdName);
        edtxtStdNSBMID=findViewById(R.id.edTxtNSBMID);
        edtxtStdPhone=findViewById(R.id.edTxtStdPhone);

        Intent intent = getIntent();
        String result = intent.getStringExtra("KEY1");

        if (result !=null){
            btnStdBack.setVisibility(View.GONE);
        }

        btnStdBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentSettingAct.this,StudentMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        firebaseAuth= FirebaseAuth.getInstance();
        UserId= firebaseAuth.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("User").child("Student").child(UserId);

        getStudentInfo();

        btnStdConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveStudentInfo();
                Toast.makeText(StudentSettingAct.this,"User Information Saved Successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(StudentSettingAct.this,StudentMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    private void getStudentInfo(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("Name") !=null){
                        stdName = map.get("Name").toString();
                        edtxtStdName.setText(stdName);
                    }
                    if (map.get("Phone") !=null){
                        stdPhone = map.get("Phone").toString();
                        edtxtStdPhone.setText(stdPhone);
                    }
                    if (map.get("NSBMID") !=null){
                        stdNSBMID = map.get("NSBMID").toString();
                        edtxtStdNSBMID.setText(stdNSBMID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SaveStudentInfo(){
        stdName = edtxtStdName.getText().toString();
        stdPhone = edtxtStdPhone.getText().toString();
        stdNSBMID= edtxtStdNSBMID.getText().toString();

        Map studentInfo = new HashMap();
        studentInfo.put("Name",stdName);
        studentInfo.put("Phone",stdPhone);
        studentInfo.put("NSBMID",stdNSBMID);

        databaseReference.updateChildren(studentInfo);

    }
}