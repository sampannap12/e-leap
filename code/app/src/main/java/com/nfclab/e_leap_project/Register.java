package com.nfclab.e_leap_project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText mFullName, mEmail, mPassword;
    CheckBox mAccountype;
    Button mRegisterBtn;
    Button mLoginBtn;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword =findViewById(R.id.password);
        mAccountype = findViewById(R.id.checkBox);
        mRegisterBtn =findViewById(R.id.registerButton);
        mLoginBtn =findViewById(R.id.loginButton);

        fAuth = FirebaseAuth.getInstance();
        // To check if the user is already logged in if so sent them to the main activity
        if (fAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }
        // Checking for errors
        mRegisterBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String fullname = mFullName.getText().toString().trim();

            if(TextUtils.isEmpty(email)){
                mEmail.setError("Email is Required");
                return;
            }
            if(TextUtils.isEmpty(password)){
                mEmail.setError("Password is Required");
                return;
            }


            if(TextUtils.isEmpty(fullname)){
                mFullName.setError("Full Name is Required");
                return;
            }
            if(password.length() <6)
            {
                mPassword.setError("Password must be more than 5 characters");
                return;
            }
            // Register the user
            fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Checking the response from the firebase
                    //if successful
                    if (task.isSuccessful()){
                        Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                    {
                        Toast.makeText(Register.this, "Error." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }) ;


        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

    }
}