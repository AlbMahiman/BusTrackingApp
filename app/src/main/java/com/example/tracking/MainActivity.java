package com.example.tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    //declaring variables
    private Button btnLoginActReg,btnLogin;
    EditText edTxtLoginEmail, edTxtLoginPass;
    public String stdID,driverID,currentUserId;

    FirebaseUser currentUser;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener firebaseAuthListner;
    private DatabaseReference studentReference,driverReference;


    //if user is already logged in , this will redirect user to main menu

    @Override
    public void onStart() {
        super.onStart();
        getUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        //assign values for variables
        btnLoginActReg = findViewById(R.id.btnRegistration);
        btnLogin = findViewById(R.id.btnLogin);
        edTxtLoginEmail=findViewById(R.id.edTxtLoginEmail);
        edTxtLoginPass = findViewById(R.id.edTxtLoginPass);

        //redirect to RegistrationAct Activity
        btnLoginActReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RegistrationAct.class);
                startActivity(intent);
                finish();
            }
        });

        //redirect to Main menu Activity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //declaring local variables and assign values for variables
                String email = edTxtLoginEmail.getText().toString();
                String password = edTxtLoginPass.getText().toString();

                //sing with username as email and password using firebase Authentication
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            currentUser = auth.getCurrentUser();
                            currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                            getUser();

                        }
                        else
                        {
                            //if email and password doest match this message will pop-up
                            Toast.makeText(MainActivity.this,"Please Check Your Username and Password", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }

    public void getUser(){
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = auth.getCurrentUser();
        if(currentUser != null){
            currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
            studentReference = FirebaseDatabase.getInstance().getReference().child("User").child("Student");
            driverReference = FirebaseDatabase.getInstance().getReference().child("User").child("Bus");

            studentReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()){
                        stdID =snapshot.getKey();
                        if (currentUserId.equals(stdID)){
                            Intent intent = new Intent(MainActivity.this,StudentMainMenuAct.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Your Account is Invalid", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            driverReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()){
                        driverID= snapshot.getKey();
                        if (currentUserId.equals(driverID)){
                            Intent intent = new Intent(MainActivity.this,DriverMainMenuAct.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Your Account is Invalid", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

}