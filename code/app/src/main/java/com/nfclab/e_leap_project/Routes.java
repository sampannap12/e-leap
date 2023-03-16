package com.nfclab.e_leap_project;

import static com.nfclab.e_leap_project.Register.TAG;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.type.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapScene;
import com.here.sdk.mapviewlite.MapStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.PixelFormat;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Routes extends Fragment {

    private EditText Original_View;
    private EditText Destination_View;
    private List<String> geoList = new ArrayList<>();
    List<GeoCoordinates> coordinates = new ArrayList<GeoCoordinates>();
    List<String> polylineList = new ArrayList<>();
    //List<MapObject> mapObjectList = new ArrayList<MapObject>();
    List<MapPolyline> mapPolylineList = new ArrayList<MapPolyline>();
    //List<MapMarker> mapLabeledMarker =
    private ArrayList<LatLng> points;

    List<MapMarker> mapMarkerList = new ArrayList<MapMarker>();
    private MapPolyline mapPolyline;
    private Button routes_button;
    private MapViewLite mapView;
    int flag = 0;
    LatLng latLng;
    Context mContext;
    String Origin;
    String Destination;
    String geo;
    List<String> resultList;
    private LatLng positions;

    private MapView mMapView;
    private GoogleMap googleMap;
    private PolylineOptions polylineOptions=new PolylineOptions();
    public FlexiblePolylineEncoderDecoder m_flexiblePolylineEncoderDecoder = new FlexiblePolylineEncoderDecoder();

    public Routes() {
        // Empty constructor required
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        Original_View = view.findViewById(R.id.origin);
        Destination_View = view.findViewById(R.id.destination);
        routes_button = view.findViewById(R.id.route_button);
        // Get a MapViewLite instance from the layout.
        //mapView = view.findViewById(R.id.map_container);
        //mapView.onCreate(savedInstanceState);
        //loadMapScene();
        //initializeHERESDK();

        mMapView = (MapView) view.findViewById(R.id.map_container);

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

        String url = "http://www.sampannapathak.com/routes?";
        String fullUrl = url + "origin=" + origin + "&" + "destination=" + destination;

        Toast.makeText(mContext, "Origin" + origin, Toast.LENGTH_SHORT).show();
        Toast.makeText(mContext, "Destination" + destination, Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, fullUrl, null,
                response -> {
                    // Parse the JSON response

                    try {
                        sendresults(response);
                    } catch (JSONException | InstantiationErrorException e) {
                        e.printStackTrace();
                    }

                },
                error -> Log.e("Volley Error", error.toString()));

        queue.add(request);


    }


    private void sendresults(JSONObject resultList) throws JSONException, InstantiationErrorException {


        JSONArray routes = resultList.getJSONArray("routes");
        Log.d(TAG, String.valueOf(routes));

        for (int j = 0; j < resultList.length(); j++) {
            JSONObject route = routes.getJSONObject(j);

            JSONArray sections = route.getJSONArray("sections");

            Log.d(TAG, String.valueOf(sections));


            for (int i = 0; i < sections.length(); i++) {

                JSONObject section = sections.getJSONObject(i);

                String id = section.getString("id");
                // Toast.makeText(mContext, "id " + id, Toast.LENGTH_LONG).show();
                JSONObject transport = section.getJSONObject("transport");
                //Toast.makeText(mContext, "transport " + transport, Toast.LENGTH_LONG).show();
                String flexiblePolyline = section.getString("polyline");
                polylineList.add(flexiblePolyline);
                List<FlexiblePolylineEncoderDecoder.LatLngZ> flexibleCoordinates = m_flexiblePolylineEncoderDecoder.decode((flexiblePolyline));
                for (int ii = 0; ii < flexibleCoordinates.size(); ii++) {
                    points = new ArrayList<>();
                    double lat = Double.parseDouble(String.valueOf(flexibleCoordinates.get(ii).lat));
                    double lng = Double.parseDouble(String.valueOf(flexibleCoordinates.get(ii).lng));
                    LatLng positions = new LatLng(lat, lng);

                    points.add(positions);
                    googleMap.addMarker(new MarkerOptions().position(points.get(0)));
                    String arrival_place = section.getString("arrival");
                    String depature_place = section.getString("departure");
                    if (transport.getString("mode").toUpperCase().equals("BUS")) {
                        polylineOptions.addAll(points);
                        polylineOptions
                                .width(5)
                                .color(R.color.Red);


                    }
                    else if (transport.getString("mode").toUpperCase().equals("PEDESTRIAN")) {
                        polylineOptions.addAll(points);
                        polylineOptions
                                .width(5)
                                .color(R.color.white);

                    }



                }
                googleMap.addPolyline(polylineOptions);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(points.get(0)).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            /*    if (transport.getString("mode").toUpperCase().equals("BUS")) {
                    //YELLOW
                    mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);


                    JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                    for (int iii = 0; iii < intermediateStops.length(); iii++) {
                        String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                        String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                        String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");
                        //mapObjectList.add(mapMarker);
                        mapLabeledMarker = new MapMarker(new GeoCoordinates(Double.parseDouble(intermediateStop_Lat), Double.parseDouble(intermediateStop_Lng)));
                        mapLabeledMarker.setCoordinate(new GeoCoordinates(Double.parseDouble(intermediateStop_Lat), Double.parseDouble(intermediateStop_Lng)));
                        mapLabeledMarker.setIcon(IconCategory.BUS_STATION);
                        mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));
                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);
                        mapLabeledMarkerList.add(mapLabeledMarker);
                    }
                } else if (transport.getString("mode").toUpperCase().equals("PEDESTRIAN")) {
                    //BLUE
                    mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);
                    mapPolylineStyle.setWidthInPixels(5);
                    mapPolylineList.add(mapPolyline);
                }

                mapView.getMapScene().addMapPolyline(mapPolyline);

                mapView.getMapScene().addMapMarker(mapLabeledMarker);
            }
*/              //googleMap.addPolyline(polylineOptions);







/* PolylineOptions polylineOptions = new PolylineOptions();

// Create polyline options with existing LatLng ArrayList
                polylineOptions.addAll(coordList);
                polylineOptions
                        .width(5)
                        .color(Color.RED);

// Adding multiple points in map using polyline and arraylist
                gMap.addPolyline(polylineOptions);



                GeoPolyline geoPolyline;

                geoPolyline = new GeoPolyline(coordinates);

                MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
                mapPolylineStyle.setWidthInPixels(5);
                mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);
                mapPolyline = new MapPolyline(geoPolyline, mapPolylineStyle);



               *//*
*/
/* if (transport.getString("mode").toUpperCase().equals("BUS")) {
                    //YELLOW
                    mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);


                    JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                    for (int iii = 0; iii < intermediateStops.length(); iii++) {
                        String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                        String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                        String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");
                        //mapObjectList.add(mapMarker);
                        mapLabeledMarker = new MapMarker(new GeoCoordinates(Double.parseDouble(intermediateStop_Lat), Double.parseDouble(intermediateStop_Lng)));
                       *//*
*/
/**//*
*/
/* mapLabeledMarker.setCoordinate(new GeoCoordinates(Double.parseDouble(intermediateStop_Lat), Double.parseDouble(intermediateStop_Lng)));
                        mapLabeledMarker.setIcon(IconCategory.BUS_STATION);
                        mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));*//*
*/
/**//*
*/
/*
//                                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);

                        //mapLabeledMarkerList.add(mapLabeledMarker);
                    }
                } else if (transport.getString("mode").toUpperCase().equals("PEDESTRIAN")) {
                    //BLUE
                    mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);
                    mapPolylineStyle.setWidthInPixels(5);

                    mapPolylineList.add(mapPolyline);
                }

//                            m_map.addMapObjects(mapObjectList);
                mapView.getMapScene().addMapPolyline(mapPolyline);

                mapView.getMapScene().addMapMarker(mapLabeledMarker);
*/



            }


        }

       /*// mapView.getMapScene().addMapMarker(mapLabeledMarker);
        mapView.getMapScene().addMapPolyline(mapPolyline);
        Log.d("Error.Response", coordinates.toString());*/

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

