package com.nfclab.e_leap_project;

import static android.os.SystemClock.sleep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class Profile extends Fragment {

    EditText etemail, etname;
    Button button;
    FirebaseFirestore fstore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    Button password_reset;
    FirebaseAuth fAuth;

    private Context mContext;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = (View) inflater.inflate(R.layout.fragment_profile, container, false);
        etname = (EditText) view.findViewById(R.id.edit_full_name);
        etemail = (EditText) view.findViewById(R.id.edit_email);
        password_reset = (Button) view.findViewById(R.id.pass_reset);

        button = view.findViewById(R.id.save);
        button.setOnClickListener(v -> {
            updateProfile();
        });

        password_reset.setOnClickListener(v -> {
            passwordReset();
        });
        return view;
    }

    public void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentID = user.getUid();
        documentReference = fstore.collection("users").document(currentID);
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {

                            String nameResult = task.getResult().getString("Full_Name");
                            String AccountResult = task.getResult().getString("Account_Type");

                            String emailResult = task.getResult().getString("Email");
                            etname.setText(nameResult);
                            etemail.setText(emailResult);

                        }

                    }
                });
    }



    private void updateProfile() {

        String name = etname.getText().toString();
        String email = etemail.getText().toString();

        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentID = user.getUid();

        final DocumentReference sDoc = fstore.collection("users").document(currentID);
        fstore.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        //updating the information
                        transaction.update(sDoc, "Full_Name", name);
                        transaction.update(sDoc, "Email", email);

                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(mContext, "Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Error Updating", Toast.LENGTH_SHORT).show();

                    }
                });
        //making sure that the user is not null
        assert user != null;
        user.updateEmail(email).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                sleep(150);
                Toast.makeText(mContext, "Email  updated ", Toast.LENGTH_SHORT).show();
                //Sending verification email to the users new email addresss
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(mContext, "Email Verification Sent.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed to send email.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                sleep(160);
                Toast.makeText(mContext, "Error updating profile info (email) ", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void passwordReset() {
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String email = users.getEmail();
        //checking if the user email is verified or not before sending the password reset email
        if(users.isEmailVerified()) {
            //sending password reset email to the users email address.
            FirebaseAuth.getInstance().sendPasswordResetEmail(users.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(mContext, "Error Sending password reset", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            //if not sending the email verification email to the users email
            Toast.makeText(mContext, "Please Verify Your Email Address First", Toast.LENGTH_SHORT).show();
            users.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(mContext, "Email Verification Sent.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Failed to send email.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}