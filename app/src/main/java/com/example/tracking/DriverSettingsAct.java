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

public class DriverSettingsAct extends AppCompatActivity {

    private Button btnDrvBack,btnDrvConfirm;
    private EditText edtxtDrvName, edtxtDrvPhone, edtxtDrvBusLNO;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private String UserId,drvName,drvPhone,drvBusLNO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        btnDrvBack=findViewById(R.id.btnDrvBack);
        btnDrvConfirm = findViewById(R.id.btnDrvConfirm);

        edtxtDrvName= findViewById(R.id.edTxtDrvName);
        edtxtDrvBusLNO=findViewById(R.id.edTxtDrvLNO);
        edtxtDrvPhone=findViewById(R.id.edTxtDrvPhone);

        Intent intent = getIntent();
        String result = intent.getStringExtra("KEY1");

        if (result !=null){
            btnDrvBack.setVisibility(View.GONE);
        }
        //back to driver main menu
        btnDrvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverSettingsAct.this,DriverMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        //database queries
        firebaseAuth= FirebaseAuth.getInstance();
        UserId= firebaseAuth.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(UserId);

        // getting driver information from firebase
        getDriverInfo();

        btnDrvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //saving driver information after change
                SaveDriverInfo();

                //After redirect to DriverMainMenu Activity
                Toast.makeText(DriverSettingsAct.this,"User Information Saved Successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DriverSettingsAct.this,DriverMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    public void getDriverInfo(){
        // getting user information from firebase and assign it to edit text
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("Name") !=null){
                        drvName = map.get("Name").toString();
                        edtxtDrvName.setText(drvName);
                    }
                    if (map.get("Phone") !=null){
                        drvPhone=map.get("Phone").toString();
                        edtxtDrvPhone.setText(drvPhone);
                    }
                    if (map.get("BusLNO") !=null){
                        drvBusLNO=map.get("BusLNO").toString();
                        edtxtDrvBusLNO.setText(drvBusLNO);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void SaveDriverInfo(){
        drvPhone= edtxtDrvPhone.getText().toString();
        drvName= edtxtDrvName.getText().toString();
        drvBusLNO= edtxtDrvBusLNO.getText().toString();

        Map driverInfo = new HashMap();
        driverInfo.put("Name",drvName);
        driverInfo.put("Phone",drvPhone);
        driverInfo.put("BusLNO",drvBusLNO);

        databaseReference.updateChildren(driverInfo);
    }
}