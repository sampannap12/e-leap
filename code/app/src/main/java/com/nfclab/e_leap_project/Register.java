package com.nfclab.e_leap_project;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
        mAccountype = findViewById(R.id.checkBox);
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
            Double balance = 0.0;
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
                mEmail.setError("Email is Required"); //checks email is added
                return;
            }
            if(TextUtils.isEmpty(password)){
                mEmail.setError("Password is Required"); //checks password is added
                return;
            }


            if(TextUtils.isEmpty(fullname)){
                mFullName.setError("Full Name is Required"); //checks password is added
                return;
            }
            if(password.length() <6)
            {
                mPassword.setError("Password must be more than 5 characters"); //checks if password length is less than 6
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
                        userID = fAuth.getCurrentUser().getUid(); // get the user id
                        //get the 'users' collection where the user info is stored
                        DocumentReference documentReference = fstore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        //update the users information
                        user.put("Full_Name",fullname );
                        user.put("Email", email);
                        user.put("Account_Type", AccountType);
                        user.put("Balance", balance);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Successful
                                Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();
                                //get the current user and sent email verification
                                FirebaseUser user = fAuth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(Register.this, "Email Verification Sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Register.this, "Failed to send email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    }
                    else
                    {
                        Toast.makeText(Register.this, "Error." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

    }
}