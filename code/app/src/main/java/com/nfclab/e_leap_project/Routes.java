package com.nfclab.e_leap_project;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Routes extends Fragment {

    private EditText Original_View;
    private EditText Destination_View;
    private List<String> geoList = new ArrayList<>();

    private TextView Results;
    private Button routes_button;
    LatLng latLng;
    Context mContext;
    String Origin;
    String Destination;
    String geo ;
    List<String> resultList;

    public Routes() {
        // Empty constructor required
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        Original_View = view.findViewById(R.id.origin);
        Destination_View = view.findViewById(R.id.destination);
        routes_button = view.findViewById(R.id.route_button);
        Results = view.findViewById(R.id.resultssss);

        routes_button.setOnClickListener(v -> {

            Origin = Original_View.getText().toString();
            Destination = Destination_View.getText().toString();
            getLongLat(Origin);
            getLongLat(Destination);
            });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SendHereApiRequest();
    }

    private void SendHereApiRequest() {
        String origin = null;
        String destination = null;
        for ( int i= 0 ; i< geoList.size(); i++){

            if( i == 0 )
            {
                origin = geoList.get(i);
            }
            else if (i == 1) {
                destination = geoList.get(i);
            }
        }

        Toast.makeText(mContext, "origin " + origin, Toast.LENGTH_LONG).show();
        Toast.makeText(mContext, "Destination " + destination, Toast.LENGTH_LONG).show();


       String url = "http://www.sampannapathak.com/routes";
       // String fullUrl = url + origin + "/" + destination;
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the JSON response
                        resultList = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            String result = jsonObject.getString("polyline");
                            resultList.add(result);
                            Toast.makeText(mContext, "data " + resultList, Toast.LENGTH_LONG).show();
                            Results.setText(resultList.toString());
                        }

                        // Use resultList as needed
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley Error", error.toString()));

        queue.add(request);


        }

    private void sendresults(List<String> resultList) {


        Toast.makeText(mContext, "data" + resultList, Toast.LENGTH_LONG).show();

    }


    private void getLongLat(String address) {

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + Uri.encode(address) + "&sensor=true&key=AIzaSyAvJo_gRhEEHdD6guDdjKWK6Q8wmMwt5Ew";
        RequestQueue queue = Volley.newRequestQueue(this.mContext);
        JsonObjectRequest stateReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject location;
                try {
                    // Get JSON Array called "results" and then get the 0th
                    // complete object as JSON
                    location = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    // Get the value of the attribute whose name is
                    // "formatted_string"

                    if (location.getDouble("lat") != 0 && location.getDouble("lng") != 0) {
                        latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                        geo = latLng.latitude +","+latLng.longitude;
                        geoList.add(geo); // Add value of geo to the list
                        if (geoList.size() == 2) {
                            SendHereApiRequest();
                        }


                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        });
        // add it to the queue
        queue.add(stateReq);




    }
}
