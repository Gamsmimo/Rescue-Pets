package com.example.rescuepets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Objects;

public class IniciarSesion extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    TextView noTenercuentaTV;
    Button mLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progres Dialog
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        //Barra de acción y su titulo
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setTitle("Iniciar Sesión");
        //Habilitar boton de retroceso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordlEt);
        noTenercuentaTV = findViewById(R.id.noTener_cuentaTV);
        mLoginBtn = findViewById(R.id.login_btn);

        //Boton de iniciar sesión
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input data
                String email = mEmailEt.getText().toString();
                String password = mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //invalid email paatern set error
                    mEmailEt.setError("Correo Invalido");
                    mEmailEt.setFocusable(true);
                }
                else {
                    //valid email pattern
                    loginUser(email, password);
                }

            }
        });
        //not have accoun textview click
        noTenercuentaTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IniciarSesion.this, Registro.class));
                finish();
            }
        });


        //init progress Dialog
        pd = new ProgressDialog(this);
    }

    private void ShowRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Contraseña");

        //set layout linear layout
        LinearLayout LinearLayout = new LinearLayout(this);
        //views to set in dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        /* sets  the min width of a EditView to fit a text n 'M' letters regardless of the actual text
        extension and text size*/
        emailEt.setMinEms(16);

        LinearLayout.addView(emailEt);
        LinearLayout.setPadding(10, 10, 10, 10  );

        builder.setView(LinearLayout);

        //buttons recover
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //buttons cancel
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        //show Progress Dialog
        pd.setMessage("Enviando Correo Electronico...");
        pd.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(IniciarSesion.this, "Correo Invalido", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(IniciarSesion.this, "Error De Carga", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get and show proper error message
                Toast.makeText(IniciarSesion.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void loginUser(String email, String password) {
        //show Progress Dialog
        pd.setMessage("Iniciar Sesión...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                //if user is sining in first time then get and show user info from google account
                                Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();
                                //Get user email and uid from auth
                                assert user != null;
                                String email = user.getEmail();
                                String uid = user.getUid();

                                //when user is registered store user info in firebase realtime database too
                                //using HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put info in hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", "");   //will add later (e.g edit profile)
                                hashMap.put("Phone", ""); //will add later (e.g edit profile)
                                hashMap.put("image", "");   //will add later (e.g edit profile)
                                //Firebase database instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //path to store user data named "Users"
                                DatabaseReference reference = database.getReference("Users");
                                //put data within hashmap in database
                                reference.child(uid).setValue(hashMap);
                            }



                            //user is logged in, so start IniciarSesión
                            startActivity(new Intent(IniciarSesion.this, Dashboard.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(IniciarSesion.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dismiss progress dialog
                        pd.dismiss();
                        //error, get and show error message
                        Toast.makeText(IniciarSesion.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // ir a la actividad anterior
        return super.onSupportNavigateUp();
    }
}