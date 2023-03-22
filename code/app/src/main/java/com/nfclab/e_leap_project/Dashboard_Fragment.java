package com.nfclab.e_leap_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
public class Dashboard_Fragment extends Fragment{
    TextView user_name,balance,account_type;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashbord, container, false);
        user_name = view.findViewById(R.id.name);
        account_type = view.findViewById(R.id.account_type);
        balance = view.findViewById(R.id.Balance);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add the logout method here
        getActivity().findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), Login.class));
            requireActivity().finish();
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        DocumentReference reference;
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        reference = fstore.collection("users").document(userID);
        reference.get()
                .addOnCompleteListener(task -> {

                    if(task.getResult().exists()){

                        String nameResult= task.getResult().getString("Full_Name");
                        String AccountResult= task.getResult().getString("Account_Type");
                        Double balanceResult = task.getResult().getDouble("Balance");

                        user_name.setText(nameResult);
                        account_type.setText(AccountResult);
                        balance.setText(String.valueOf(balanceResult));
                    }

                });
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Get updated balance from snapshot
                   double balanceResult = snapshot.getDouble("Balance");
                    balance.setText(String.valueOf(balanceResult));
                }
            }
        });
    }
}
