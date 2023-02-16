package com.nfclab.e_leap_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class Dashboard_Fragment extends Fragment   {

    TextView user_name,balance,account_type;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashbord, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user_name = getActivity().findViewById(R.id.name);
        account_type = getActivity().findViewById(R.id.account_type);
        balance = getActivity().findViewById(R.id.Balance);
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
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){

                            String nameResult= task.getResult().getString("Full_Name");
                            String AccountResult= task.getResult().getString("Account_Type");
                            Double balanceResult = task.getResult().getDouble("Balance");



                            user_name.setText(nameResult);
                            account_type.setText(AccountResult);
                            balance.setText(String.valueOf(balanceResult));

                        }

                    }
                });
    }


}