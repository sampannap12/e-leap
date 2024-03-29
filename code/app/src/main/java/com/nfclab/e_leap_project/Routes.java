package com.nfclab.e_leap_project;

import static com.nfclab.e_leap_project.Register.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Routes extends Fragment {

    public FlexiblePolylineEncoderDecoder m_flexiblePolylineEncoderDecoder = new FlexiblePolylineEncoderDecoder();
    int flag = 0;
    LatLng latLng;
    Context mContext;
    String Origin;
    String Destination;
    String geo;
    PolylineOptions polylineOptions1=new PolylineOptions();
    PolylineOptions polylineOptions2=new PolylineOptions();
    ArrayList<Polyline> lines = new ArrayList<>();
    MarkerOptions options = new MarkerOptions();
    private EditText Original_View;
    private EditText Destination_View;
    private List<String> geoList = new ArrayList<>();
    private ArrayList<LatLng> points;

    private MapView mMapView;
    private GoogleMap googleMap;
    StringRequest request;

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
        Button routes_button = view.findViewById(R.id.route_button);
        mMapView = view.findViewById(R.id.map_container);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                LatLng dub = new LatLng(53.350140, -6.266155);
                // For zooming functionality
                CameraPosition cameraPosition = new CameraPosition.Builder().target(dub).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        routes_button.setOnClickListener(v -> {

            googleMap.clear();
            polylineOptions1 = new PolylineOptions();
            polylineOptions2 = new PolylineOptions();
            options = new MarkerOptions();

            Origin = Original_View.getText().toString();
            Destination = Destination_View.getText().toString();


            if (Origin.equals("")) {
                Toast.makeText(mContext, "Please Enter Starting Location", Toast.LENGTH_SHORT).show();
            } else if (Destination.equals("")) {
                Toast.makeText(mContext, "Please Enter Your Destination", Toast.LENGTH_SHORT).show();
            } else {

                geoList.clear();
                flag = 0;

                getLongLat(Origin);
                getLongLat(Destination);


            }
        });

        return view;
    }
    private void SendHereApiRequest() {
        String origin = null;
        String destination = null;
        for (int i = 0; i < geoList.size(); i++) {
            if (i == 0) {
                origin = geoList.get(i);
                origin = origin.replaceAll("\\s", "");
            } else if (i == 1) {
                destination = geoList.get(i);
                destination = destination.replaceAll("\\s", "");
            }
        }
        //calling the backend server with origin and destination as parameter
        String url = "";//Please Enter your backend domain here
        String fullUrl = url + "origin=" + origin + "&" + "destination=" + destination;
        RequestQueue queue = Volley.newRequestQueue(mContext);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, fullUrl, null,
                response -> {
                    // Parse the JSON response

                    try {
                        sendresults(response);
                    } catch (JSONException  e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley Error", error.toString()));
        queue.add(request);

    }

    @SuppressLint("PotentialBehaviorOverride")
    private void sendresults(JSONObject resultList) throws JSONException {
        JSONArray routes = resultList.getJSONArray("routes");
        for (int j = 0; j < resultList.length(); j++) {
            JSONObject route = routes.getJSONObject(j);
            JSONArray sections = route.getJSONArray("sections");
            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                JSONObject arrival_place = sections.getJSONObject(i).getJSONObject("arrival");
                JSONObject departure_place = section.getJSONObject("departure");
                JSONObject transport = section.getJSONObject("transport");
                String mode = transport.getString("mode");
                String departureTime = null;
                String flexiblePolyline = section.getString("polyline");
                LatLng firstpositions = null;
                if (mode.equalsIgnoreCase("Bus")) {
                    departureTime = departure_place.getString("time");
                }
                List<FlexiblePolylineEncoderDecoder.LatLngZ> flexibleCoordinates = m_flexiblePolylineEncoderDecoder.decode((flexiblePolyline));
                for (int ii = 0; ii < flexibleCoordinates.size(); ii++) {
                    points = new ArrayList<>();
                    double lat = Double.parseDouble(String.valueOf(flexibleCoordinates.get(ii).lat));
                    double lng = Double.parseDouble(String.valueOf(flexibleCoordinates.get(ii).lng));
                    LatLng positions = new LatLng(lat, lng);
                    points.add(positions);
                    if (ii == 0) {
                        firstpositions = new LatLng(lat, lng);
                    }
                    if (transport.getString("mode").equalsIgnoreCase("Bus")) {
                        polylineOptions1.add(positions);
                        polylineOptions1
                                .width(15)
                                .color(-65536)
                                .geodesic(true);
                        // Toast.makeText(mContext, "bus ", Toast.LENGTH_LONG).show();
                        options = new MarkerOptions()
                                .position(positions);
                    } else if (transport.getString("mode").equalsIgnoreCase("PEDESTRIAN")) {
                        polylineOptions2.add(positions);
                        polylineOptions2
                                .width(10)
                                .color(-16711936)
                                .geodesic(true);
                        options = new MarkerOptions()
                                .position(positions);
                    }
                    lines.add(googleMap.addPolyline(polylineOptions2));
                    lines.add(googleMap.addPolyline(polylineOptions1));
                }
                assert firstpositions != null;
                googleMap.addMarker(new MarkerOptions().position(firstpositions));
                googleMap.addMarker(options).setTag(section); // set the section as the marker tag


                googleMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1)).title(arrival_place.getJSONObject("place").getString("name")).snippet(transport.getString("mode")));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(points.get(0)).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                polylineOptions1 = new PolylineOptions();
                polylineOptions2 = new PolylineOptions();
                lines.clear();
            }
        }


    }
    private void getLongLat(String address) {

        request = new StringRequest(Request.Method.GET, "",//Please Enter your Back- End domain here
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            String SecreteKey = object.getString("google_map_accessKeySecret");
                            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + Uri.encode(address) + "&sensor=true&key="+SecreteKey;
                            RequestQueue queue = Volley.newRequestQueue(mContext);
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
                                            geoList.add(geo); // adding the longitude and latitude in a list show that in can be accessed by the other method
                                            flag = flag + 1;
                                            if (flag== 2) {
                                                //Calls after getting th Long and Lat of both Origin and destination
                                                SendHereApiRequest();
                                            }
                                        }
                                    } catch (JSONException e1) {
                                        Toast.makeText(mContext, "Could not fetch the location. Please try again.", Toast.LENGTH_LONG).show();
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);

    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}


