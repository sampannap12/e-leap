package com.nfclab.e_leap_project;


import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    public static final String Error_detected = "NFC tag not detected";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] readTagFilters;
    Context context;
    ProgressBar progressBar;
    FrameLayout frame;

    private FirebaseUser user ;
    private DocumentReference reference;
    private FirebaseFirestore fstore ;
    private double new_balance;
    private final double Student_fare = 1.0;
    private final double Dublin_Bus_fare = 2.0;
    private final double bus_Eireann_fare = 1.55;
    String AccountResult;


    BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progresBar);
        frame = findViewById(R.id.frame_layout);
        fstore = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(onNav);

        loadFragment(new Dashboard_Fragment());


        context = this;
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC is not supported", Toast.LENGTH_LONG).show();
            finish();
        }
        readIntent(getIntent());

        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        readTagFilters = new IntentFilter[]{tagDetected};
    }

    private NavigationBarView.OnItemSelectedListener onNav = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.dashboard:
                    fragment = new Dashboard_Fragment();
                    break;
                case R.id.Profile:
                    fragment = new Profile();
                    break;
                case R.id.Topup:
                    fragment = new Topup();
                    break;
                case R.id.Routes:
                    fragment = new Routes();
                    break;
            }
            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        }
    };

    void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }

    private void readIntent(Intent intent) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (currentFragment instanceof Dashboard_Fragment) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Parcelable[] Message = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage[] msg;
                if (Message != null) {
                    msg = new NdefMessage[Message.length];
                    for (int i = 0; i < Message.length; i++) {
                        msg[i] = (NdefMessage) Message[i];
                    }
                    buildTagViews(msg);
                } else {
                    Toast.makeText(context, Error_detected, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void buildTagViews(NdefMessage[] msg) {
        if (msg == null || msg.length == 0) return;
        String text = "";

        byte[] payload = msg[0].getRecords()[0].getPayload();
        //getting the text encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        //get Language code
        int languagecodelen = payload[0] & 0063; //0063 is "en"
        try {
            text = new String(payload, languagecodelen + 1, payload.length - languagecodelen - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        if (text.trim().equals("DublinBus_101")) {
            Toast.makeText(context, "Valid Dublin Bus NFC tag detected", Toast.LENGTH_LONG).show();
            user = FirebaseAuth.getInstance().getCurrentUser();
            String userID;
            userID = user.getUid();
            reference = fstore.collection("users").document(userID);
            reference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.getResult().exists()) {
                                AccountResult = task.getResult().getString("Account_Type");
                                assert AccountResult != null;
                                if (AccountResult.equals("Student")) {
                                    Busfare(Student_fare);

                                } else if (AccountResult.equals("Adult")) {
                                    Busfare(Dublin_Bus_fare);
                                } else {
                                    Toast.makeText(context, "Account Error", Toast.LENGTH_LONG).show();
                                }
                            }

                        }

                    });
        } else if (text.trim().equals("BusErien102")) {
            Toast.makeText(context, "Valid  BusErien NFC tag detected", Toast.LENGTH_LONG).show();
            user = FirebaseAuth.getInstance().getCurrentUser();
            String userID;
            userID = user.getUid();
            reference = fstore.collection("users").document(userID);
            reference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.getResult().exists()) {
                                AccountResult = task.getResult().getString("Account_Type");
                                assert AccountResult != null;
                                if (AccountResult.equals("Student")) {
                                    Busfare(Student_fare);

                                } else if (AccountResult.equals("Adult")) {
                                    Busfare(bus_Eireann_fare);
                                } else {
                                    Toast.makeText(context, "Account Error", Toast.LENGTH_LONG).show();
                                }
                            }

                        }

                    });
        } else {

            Toast.makeText(context, "Invalid NFC tag detected", Toast.LENGTH_LONG).show();
        }
    }

    private void Busfare(double v) {
        progressBar.setVisibility (View.VISIBLE);
        frame.setVisibility(View.INVISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();


        reference = fstore.collection("users").document(userID);
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){


                            double balanceResult = task.getResult().getDouble("Balance");
                            double old_balance = Double.parseDouble(String.valueOf(balanceResult));
                            if (old_balance>2) {
                                Toast.makeText(context, "Old Balance" + balanceResult, Toast.LENGTH_SHORT).show();
                                new_balance = old_balance - v;
                                final DocumentReference sDoc = fstore.collection("users").document(userID);
                                fstore.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                transaction.update(sDoc, "Balance", new_balance);
                                                // Success
                                                return null;
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Balance Updated Successfully" , Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility (View.INVISIBLE);
                                                frame.setVisibility(View.VISIBLE);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Error Updating" +e, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(context, "Please Top Up. Your Balance is  " + balanceResult, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readIntent(intent);
    }

    private void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE);

        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    private void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }
}
