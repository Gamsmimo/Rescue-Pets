package com.example.rescuepets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registro extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mTenerCuentaTv;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Barra de acción y su titulo
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setTitle("Registrarse");
        //Habilitar boton de retroceso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordlEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mTenerCuentaTv = findViewById(R.id.Tener_cuentaTV);


        //In the onCreate() method, initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("registrando usuario...");


        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validamos
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set   error and focuss to email  edittext
                    mEmailEt.setError("Correo Invalido");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    //set   error and focuss to email  edittext
                    mPasswordEt.setError("mínimo 6 caracteres");
                    mPasswordEt.setFocusable(true);
                }
                else{
                    registerUser(email, password); //register the user 
                }
            }
        });

        //handle login textview click listener
        mTenerCuentaTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Registro.this, IniciarSesion.class));
                finish();

            }
        });




    }

    private void registerUser(String email, String password) {
        // el patrón de correo electrónico y contraseña es válido, muestra el diálogo de progreso y comienza a registrar al usuario
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicie sesión correctamente, cierre el cuadro de diálogo e inicie la actividad de registro
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //when user is registered store user info in firebase realtime database too
                            //using HashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put info in hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("nombre", "");   //will add later (e.g edit profile)
                            hashMap.put("telefono", ""); //will add later (e.g edit profile)
                            hashMap.put("imagen", "");   //will add later (e.g edit profile)
                            //Firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(Registro.this, "Registrado...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Registro.this, Dashboard.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(Registro.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //error, descartar el diálogo de progreso y obtener y mostrar el mensaje de error
                        progressDialog.dismiss();
                        Toast.makeText(Registro.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // ir a la actividad anterior
        return super.onSupportNavigateUp();
    }
}