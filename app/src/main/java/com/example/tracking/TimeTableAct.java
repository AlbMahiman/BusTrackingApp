package com.example.tracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TimeTableAct extends AppCompatActivity {

    Button btnMaharagama,btnGampaha,btnAvissawella,btnBack,btnMap;
    String getKeyFromSTD,getKeyFromDRV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        btnBack=findViewById(R.id.btnTimeTableBack);
        btnMap=findViewById(R.id.btnTimeTableMap);
        btnAvissawella=findViewById(R.id.btnAvissawella);
        btnMaharagama= findViewById(R.id.btnMaharagama);
        btnGampaha=findViewById(R.id.btnGampaha);

        Intent getKeyFromSTDintent = getIntent();
        getKeyFromSTD = getKeyFromSTDintent.getStringExtra("KEYTOTABLESTD");

        Intent getKeyFromDRVintent = getIntent();
        getKeyFromDRV = getKeyFromDRVintent.getStringExtra("KEYTOTABLEDRV");

        btnGampaha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimeTableAct.this,TimeTableMapAct.class);
                String KEY ="gamapaha";
                intent.putExtra("KEYTOMAPGAMPAHA",KEY);
                startActivity(intent);
                finish();
                return;

            }
        });

        btnMaharagama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimeTableAct.this,TimeTableMapAct.class);
                String KEY ="maharagama";
                intent.putExtra("KEYTOMAPMAHARAGAMA",KEY);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnAvissawella.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimeTableAct.this,TimeTableMapAct.class);
                String KEY ="avissawella";
                intent.putExtra("KEYTOMAPAVISSAWELLA",KEY);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getKeyFromDRV !=null){
                    Intent intent = new Intent(TimeTableAct.this,DriverMainMenuAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                if (getKeyFromSTD !=null){
                    Intent intent = new Intent(TimeTableAct.this,StudentMainMenuAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getKeyFromDRV!=null){
                    Intent intent = new Intent(TimeTableAct.this,DriverMapAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                if (getKeyFromSTD!=null){
                    Intent intent = new Intent(TimeTableAct.this,StudentMapAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });

    }
}