package com.nfclab.e_leap_project;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Topup extends Fragment {

    private Button payButton;
    String PublishableKey= "pk_test_51M9TmXEmmGrai2qsgxqPP0ynZIK8kNDxl7mJSFF98Jq8kNgB6it7oHCuwWJnMccEFAGMX99CZK4kwvMtjWbN8akh00VBqp3etQ";
    String SecreteKey = "sk_test_51M9TmXEmmGrai2qsSDlAxm8nYk8gFtglwmLF7IntK1MB7Ndn5Xx43tdf9hkIWhnJlyqCptUqKM1pQhpPRDQ4MDM800rticnD7V";
    String CustomerId;
    String EphericalKey;
    String ClientSecret;
    PaymentSheet paymentSheet;
    String item;
    Double balanceResult;
    StringRequest request;
    String amount;
    Double new_balance;
    private Context mContext;
    boolean flag = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topup, container, false);
        payButton = view.findViewById(R.id.top);

        PaymentConfiguration.init(mContext,PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);


        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.Top_Up_Amount, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).equals("Select Top Up Amount")) {
                    flag = false;
                } else {
                    flag = true;
                    item = parent.getItemAtPosition(position).toString();
                    amount = item;
                    request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject object = new JSONObject(response);
                                        CustomerId = object.getString("id");
                                        Toast.makeText(mContext,"Client id"+ CustomerId,Toast.LENGTH_SHORT).show();
                                        getEphericalKey();


                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> header = new HashMap<>();
                            header.put("Authorization", "Bearer " + SecreteKey);
                            return header;

                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                    requestQueue.add(request);

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }


        });


        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( flag == true){
                    paymentFlow();
                }
                else
                {
                    Toast.makeText(mContext,"Please select a top up amount",Toast.LENGTH_SHORT).show();
                }

            }
        });


        return view;
    }



    private void getEphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");
                            Toast.makeText(mContext,"Ephericalkey"+ EphericalKey,Toast.LENGTH_SHORT).show();
                            getClientSecret(ClientSecret,EphericalKey);


                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+ SecreteKey);
                header.put("Stripe-Version", "2022-08-01");
                return header;

            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer",CustomerId);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);

    }

    private void getClientSecret(String clientId, String ephericalKey) {

        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Toast.makeText(mContext,"ClientSecret "+ ClientSecret,Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+ SecreteKey);
                header.put("Stripe-Version", "2022-08-01");
                return header;

            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("customer",CustomerId);
                params.put("amount", (item + "00"));
                params.put ("currency", "eur");
                params.put("automatic_payment_methods[enabled]", "true");

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);

    }

    public void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("E-Leap", new PaymentSheet.CustomerConfiguration(
                CustomerId,
                EphericalKey
        )));

    }

    public void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {

        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(mContext, "Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Display for example, an order confirmation screen
            Toast.makeText(mContext, "Payment Completed", Toast.LENGTH_SHORT).show();

            FirebaseFirestore fstore = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DocumentReference reference;

            String currentID = user.getUid();
            reference = fstore.collection("users").document(currentID);
            reference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.getResult().exists()){

                                balanceResult = task.getResult().getDouble("Balance");
                                new_balance = balanceResult+ Double.valueOf(amount);
                            }


                            final DocumentReference sDoc = fstore.collection("users").document(currentID);
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

                                            Toast.makeText(mContext, "Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, "Error Updating" +e, Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    });
        }
    }

}

