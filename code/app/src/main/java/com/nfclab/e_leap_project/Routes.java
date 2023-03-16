package com.nfclab.e_leap_project;

import static com.nfclab.e_leap_project.Register.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.android.gms.maps.model.LatLng;

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
    int flag =0;
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



        routes_button.setOnClickListener(v -> {
            Origin = Original_View.getText().toString();
            Destination = Destination_View.getText().toString();

            if (Origin.equals(""))
            {
                Toast.makeText(mContext, "Please Enter Starting Location", Toast.LENGTH_SHORT).show();
            }
            else if (Destination.equals(""))
            {
                Toast.makeText(mContext, "Please Enter Your Destination", Toast.LENGTH_SHORT).show();
            }
            else {

                geoList.clear();
                flag = 0;
                getLongLat(Origin);
                getLongLat(Destination);

            }
        });

        return view;
    }


    private void SendHereApiRequest() {
        String origin= null ;
        String destination = null;
        for ( int i= 0 ; i< geoList.size(); i++){
            if( i == 0 )
            {
                origin = geoList.get(i);
                origin = origin.replaceAll("\\s", "");
            }
            else if (i == 1) {
                destination = geoList.get(i);
                destination = destination.replaceAll("\\s", "");
            }
        }

       String url = "http://www.sampannapathak.com/routes?";
       String fullUrl = url + "origin="+origin+ "&" +"destination="+destination;

        Toast.makeText(mContext, "Origin" + origin, Toast.LENGTH_SHORT).show();
        Toast.makeText(mContext, "Destination" + destination, Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, fullUrl, null,
                response -> {
                    try {
                        // Parse the JSON response
                        JsonNode responseNode = new ObjectMapper().readTree(String.valueOf(response));
                        if (responseNode.isArray()) {
                            ArrayNode arrayNode = (ArrayNode) responseNode;

                            sendresults(arrayNode);
                        }


                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley Error", error.toString()));

        queue.add(request);


        }

    private void sendresults(ArrayNode resultList) {



        Log.d(TAG, String.valueOf(resultList));

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

                    if (location.getDouble("lat") != 0 && location.getDouble("lng") != 0) {
                        latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                        geo = latLng.latitude +","+latLng.longitude;
                        geoList.add(geo);
                        flag = flag + 1;
                        if (flag== 2) {
                            SendHereApiRequest();
                        }

                    }
                    else {
                        Toast.makeText(mContext, "Could not fetch the location. Please try again.", Toast.LENGTH_LONG).show();
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
