package com.example.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationAct extends AppCompatActivity {

    //declaring global variables
    Button btnBack , btnRegisterNow;
    RadioGroup radioGroupRegister;
    RadioButton radioButtonRegister;
    EditText edTxtEmail, edTxtPass;

    FirebaseAuth firebaseAuth;

    int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //assign values for variables
        btnBack = findViewById(R.id.btnBack);
        radioGroupRegister = findViewById(R.id.btnGroup);
        btnRegisterNow = findViewById(R.id.btnRegistrationNow);

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int radioId = radioGroupRegister.getCheckedRadioButtonId();
                radioButtonRegister = findViewById(radioId);

                //assign values for variables
                edTxtEmail= findViewById(R.id.edTxtRegEmail);
                edTxtPass = findViewById(R.id.edTxtRegPass);

                String email = edTxtEmail.getText().toString();
                String password = edTxtPass.getText().toString();

                //create account using firebase Authentication
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegistrationAct.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //getting current user id
                        String user_id = firebaseAuth.getCurrentUser().getUid();
                        if (task.isSuccessful()){
                            switch (radioId)
                            {   //after creating account  whether user is a driver or a student is stored in a firebase realtime database
                                case R.id.btnDriver:
                                    id=1;
                                    DatabaseReference current_user_driver = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(user_id);
                                    current_user_driver.setValue(true);
                                    break;

                                case R.id.btnStudent:
                                    id=2;
                                    DatabaseReference current_user_student = FirebaseDatabase.getInstance().getReference().child("User").child("Student").child(user_id);
                                    current_user_student.setValue(true);
                                    break;
                            }
                        }
                        else {
                            Toast.makeText(RegistrationAct.this,"Registration Failed", Toast.LENGTH_LONG).show();
                        }
                        //if id=1 , its mean he is driver so he will redirect to Driversetting Activity
                        if (id ==1){
                            Intent intent = new Intent(RegistrationAct.this,DriverSettingsAct.class);
                            String register ="register";
                            intent.putExtra("KEY1",register);
                            startActivity(intent);
                            finish();
                        }
                        //if id=2 , its mean he is driver so he will redirect to Studentsetting Activity
                        if (id ==2){
                            Intent intent = new Intent(RegistrationAct.this,StudentSettingAct.class);
                            String register ="register";
                            intent.putExtra("KEY1",register);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationAct.this,MainActivity.class);;
                startActivity(intent);
                finish();
            }
        });
    }
}