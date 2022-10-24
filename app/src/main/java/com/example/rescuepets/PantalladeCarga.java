package com.example.rescuepets;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PantalladeCarga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantallade_carga);

        //Esto se representa en segundos, que demora la pantalla de carga
        final int Duracion = 2500;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Esto se ejecutara pasado los segundos que hemos establecido
                Intent intent = new Intent(PantalladeCarga.this,MainActivity.class);
                startActivity(intent);
                //Nos dirige de esta actividad, al mainactivity
            }
        },Duracion);


    }
}