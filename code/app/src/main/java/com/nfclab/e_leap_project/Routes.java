package com.nfclab.e_leap_project;

import static com.nfclab.e_leap_project.Register.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.LatLng;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
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
    private Button routes_button;
    private MapViewLite mapView;
    int flag = 0;
    LatLng latLng;
    Context mContext;
    String Origin;
    String Destination;
    String geo;
    List<String> resultList;
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
        mapView = view.findViewById(R.id.map_container);
        mapView.onCreate(savedInstanceState);
        loadMapScene();
        //initializeHERESDK();

        routes_button.setOnClickListener(v -> {
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

    /*private void initializeHERESDK() {
        // Set your credentials for the HERE SDK.

        StringRequest request = new StringRequest(Request.Method.GET, "http://www.sampannapathak.com/",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        accessKeyID = object.getString("here_accessKeyID");
                        accessKeySecret = object.getString("here_accessKeySecret");

                        Toast.makeText(mContext, "Please Enter Starting Location"+ accessKeyID, Toast.LENGTH_SHORT).show();
                        Toast.makeText(mContext, "Please Enter Starting Location"+ accessKeySecret, Toast.LENGTH_SHORT).show();
                       *//* SDKOptions options = new SDKOptions(accessKeyID, accessKeySecret);
                        try {
                            SDKNativeEngine.makeSharedInstance(mContext, options);
                        } catch (InstantiationErrorException e) {
                            throw new RuntimeException("Initialization of HERE SDK failed: " + e.error.name());
                        }

*//*

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);


    }
*/
    private void loadMapScene() {
        // Load a scene from the SDK to render the map with a map style.
        mapView.getMapScene().loadScene(MapStyle.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapScene.ErrorCode errorCode) {
                if (errorCode == null) {
                    mapView.getCamera().setTarget(new GeoCoordinates( 53.35626108474569, -6.2794289087861195));

                    mapView.getCamera().setZoomLevel(12);
                } else {
                    Log.d("loadMapScene()", "onLoadScene failed: " + errorCode.toString());
                }
            }
        });
    }
/*

    private void disposeHERESDK() {
        // Free HERE SDK resources before the application shuts down.
        // Usually, this should be called only on application termination.
        // Afterwards, the HERE SDK is no longer usable unless it is initialized again.
        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose();
            // For safety reasons, we explicitly set the shared instance to null to avoid situations,
            // where a disposed instance is accidentally reused.
            SDKNativeEngine.setSharedInstance(null);
        }
    }
*/



    private void sendresults(JSONObject resultList) throws JSONException, InstantiationErrorException {



        JSONArray routes = resultList.getJSONArray("routes");
        Log.d(TAG, String.valueOf(routes));

        for (int j=0 ; j<resultList.length();j++) {
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
                    GeoCoordinates coordinate = new GeoCoordinates(flexibleCoordinates.get(ii).lat, flexibleCoordinates.get(ii).lng);
                    coordinates.add(coordinate);
                }

                String arrival_place = section.getString("arrival");
                String depature_place = section.getString("departure");

            }


        }
        Log.d("Error.Response", coordinates.toString());


        GeoPolyline geoPolyline ;

        geoPolyline = new GeoPolyline(coordinates);

        MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
        mapPolylineStyle.setWidthInPixels(5);
        mapPolylineStyle.setColor(0xFF0000A0, PixelFormat.RGBA_8888);
        MapPolyline mapPolyline = new MapPolyline(geoPolyline, mapPolylineStyle);


        mapView.getMapScene().addMapPolyline(mapPolyline);

/*

            if (transport.getString("mode").toUpperCase().equals("SUBWAY")){
                //RED
                mapPolyline.setLineColor(-65536);

                JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                for (int iii = 0; iii < intermediateStops.length(); iii++) {
                    String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                    String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                    String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");


//                                        MapMarker mapMarker = new MapMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)), new Image());
//                                        mapObjectList.add(mapMarker);

                    MapLabeledMarker mapLabeledMarker = new MapLabeledMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                    mapLabeledMarker.setCoordinate(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                    mapLabeledMarker.setIcon(IconCategory.METRO_STATION);
                    mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));
//                                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);
                    mapLabeledMarkerList.add(mapLabeledMarker);
                }

            }
            else if (transport.getString("mode").toUpperCase().equals("BUS")){
                //YELLOW
                mapPolyline.setLineColor(-256);

                JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                for (int iii = 0; iii < intermediateStops.length(); iii++) {
                    String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                    String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                    String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");


//                                        MapMarker mapMarker = new MapMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)), new Image());
//                                        mapObjectList.add(mapMarker);

                    MapLabeledMarker mapLabeledMarker = new MapLabeledMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                    mapLabeledMarker.setCoordinate(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                    mapLabeledMarker.setIcon(IconCategory.BUS_STATION);
                    mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));
//                                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);

                    mapLabeledMarkerList.add(mapLabeledMarker);
                }
            }
            else if (transport.getString("mode").toUpperCase().equals("PEDESTRIAN")){
                //BLUE
                mapPolyline.setLineColor(-16776961);}

            mapPolyline.setLineWidth(13);
//                                mapObjectList.add(mapPolyline);
            mapPolylineList.add(mapPolyline);
        }

//                            m_map.addMapObjects(mapObjectList);
        m_map.addMapObjects(mapPolylineList);
        m_map.addMapObjects(mapLabeledMarkerList);

     */

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
}
