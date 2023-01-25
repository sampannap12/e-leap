package com.nfclab.e_leap_project;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {
    TextView user_name,balance,card_num,account_type;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_name = findViewById(R.id.name);
        balance = findViewById(R.id.Balance);
        card_num = findViewById(R.id.card_num);
        account_type = findViewById(R.id.account_type);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                user_name.setText(documentSnapshot.getString("Full_Name"));
                account_type.setText(documentSnapshot.getString("Account_Type"));
                long card_numb = documentSnapshot.getLong("Card_Number");
                String card_number = Long.toString(card_numb);
                card_num.setText(card_number);

                double balan = documentSnapshot.getDouble("Balance");
                String balanc = Double.toString(balan);
                balance.setText(balanc);
            }

        });
    }



    public void logout(View view) {
        FirebaseAuth.getInstance().signOut(); //log out user
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}