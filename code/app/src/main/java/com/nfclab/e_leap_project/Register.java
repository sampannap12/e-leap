package com.nfclab.e_leap_project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword;
    CheckBox mAccountype;
    Button mRegisterBtn;
    Button mLoginBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword =findViewById(R.id.password);
        mAccountype = (CheckBox) findViewById(R.id.checkBox);
        mRegisterBtn =findViewById(R.id.registerButton);
        mLoginBtn =findViewById(R.id.loginButton);



        fAuth = FirebaseAuth.getInstance();
        fstore =FirebaseFirestore.getInstance();
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
            boolean mAccountypeChecked = mAccountype.isChecked();
            double balance = 0;
            String AccountType;
            if (mAccountypeChecked == true)
            {
                AccountType = "Student";
            }
            else
            {
                AccountType= "Adult";
            }

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
                        userID = fAuth.getCurrentUser().getUid();




                        DocumentReference documentReference = fstore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("Full_Name",fullname );
                        user.put("Email", email);
                        user.put("Account_Type", AccountType);
                        user.put("Balance", balance);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "profile created  " + userID);
                            }
                        });
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