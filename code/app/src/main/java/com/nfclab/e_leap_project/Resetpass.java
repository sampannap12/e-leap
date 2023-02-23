package com.nfclab.e_leap_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Resetpass extends AppCompatActivity {
    EditText mEmail;
    Button mButton;
    Context context1 = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpass);
        mEmail = findViewById(R.id.Reset_email);
        mButton = findViewById(R.id.reset_button);
        Toast.makeText(this, "Email" + mEmail, Toast.LENGTH_SHORT).show();

        mButton.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context1, "Password Reset Email Sent to "+ email, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context1, "Error Sending password reset ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }


    }




