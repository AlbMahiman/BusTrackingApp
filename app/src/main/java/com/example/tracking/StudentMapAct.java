package com.example.tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tracking.databinding.ActivityStudentMapBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StudentMapAct extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    private ActivityStudentMapBinding binding;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Button btnback,btnFindBus,btnConfirmRide;
    Location liveLocation,pickupMarkerLocation,driverLocation;
    LatLng latLng,pickupMarkerLatLng;
    private GeoFire studentPickupLocationgeoFire;
    GeoQuery geoQuery;
    private String userId,driverFoundId,pushKey,firebaseID;
    private Marker driverMarker,pickupMarker;
    private DatabaseReference driverLocationRef,driverRef,studentPickupLocationRef,deleteRequestReference,unavailableDriverLocationRef,getClosestDriverLocation;
    private boolean requestBoolean = false, confirmRideBoolean=false;
    Boolean driverFound = false;
    private ValueEventListener driverLocationRefListener,unavailableDriverLocationRefListener;
    private ChildEventListener deleteRequestReferenceListener;
    int radius = 1;
    SupportMapFragment mapFragment;
    private LinearLayout driverInfoLinearLayout,findBusLinearLayout;
    private TextView txtDriverPhone,txtDriverName,txtBusLNO,txtBusDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StudentMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        btnConfirmRide= findViewById(R.id.btnStdConfirmRide);
        btnback = findViewById(R.id.btnBackStudentMap);
        btnFindBus = findViewById(R.id.btnFindBus);

        txtBusDistance=findViewById(R.id.txtDrvDistance);
        txtDriverName=findViewById(R.id.txtDrvName);
        txtDriverPhone=findViewById(R.id.txtDrvPhone);
        txtBusLNO=findViewById(R.id.txtBusLNO);

        findBusLinearLayout=findViewById(R.id.findBus);
        driverInfoLinearLayout=findViewById(R.id.driverInfo);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestBoolean= false;
                driverFound= false;

                Intent intent = new Intent(StudentMapAct.this,StudentMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        
        Toast.makeText(StudentMapAct.this,"Please set Pickup Marker", Toast.LENGTH_LONG).show();

        if(pickupMarker !=null){
            btnFindBus.setVisibility(View.VISIBLE);
        }
        btnFindBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBoolean){
                    btnback.setVisibility(View.VISIBLE);
                    requestBooleanFalse();

                }
                else {
                    requestBoolean = true;

                    userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    studentPickupLocationRef = FirebaseDatabase.getInstance().getReference("StudentPickupLocation");
                    studentPickupLocationgeoFire = new GeoFire(studentPickupLocationRef);
                    studentPickupLocationgeoFire.setLocation(userId,new GeoLocation(pickupMarkerLatLng.latitude, pickupMarkerLatLng.longitude));

                    btnback.setVisibility(View.GONE);
                    getClosestBus();
                }

            }
        });
    }

    public void getClosestBus(){
        getClosestDriverLocation = FirebaseDatabase.getInstance().getReference().child("AvailableBusLocation");
        GeoFire geoFire = new GeoFire(getClosestDriverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBoolean){
                    driverFound= true;
                    driverFoundId=key;
                    userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

                    driverRef = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverFoundId).child("STDPassengerUID");
                    driverRef.push().setValue(userId);

                    btnFindBus.setText("Cancel the Ride");
                    getDriverLocation();
                    getAssignedDriverInfo();

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){ // driverfound = true

                    if (radius <20){
                        radius++;
                        getClosestBus();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"No available Bus in Your Area",Toast.LENGTH_LONG).show();
                        requestBoolean= false;
                        driverFound= false;
                        pickupMarker.remove();
                        btnFindBus.setVisibility(View.GONE);
                        findBusLinearLayout.setVisibility(View.GONE);
                        btnFindBus.setText("Find-BUS");
                        btnback.setVisibility(View.VISIBLE);
                        studentPickupLocationgeoFire.removeLocation(userId);
                    }



                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StudentMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                pickupMarkerLatLng= latLng;
                if (pickupMarker != null){
                    pickupMarker.remove();
                }
                pickupMarker= mMap.addMarker(new MarkerOptions().position(latLng).title("Pick ME").icon(BitmapFromVector(getApplicationContext(),R.drawable.pickupmarkericon)));
                pickupMarkerLocation = new Location("");
                pickupMarkerLocation.setLatitude(pickupMarkerLatLng.latitude);
                pickupMarkerLocation.setLongitude(pickupMarkerLatLng.longitude);

                int distanceBetweenMarkerAndLiveLocation = (int) liveLocation.distanceTo(pickupMarkerLocation);
                if (distanceBetweenMarkerAndLiveLocation<1000){
                    findBusLinearLayout.setVisibility(View.VISIBLE);
                    btnFindBus.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Too-far From Your Area",Toast.LENGTH_LONG).show();
                    pickupMarker.remove();
                    findBusLinearLayout.setVisibility(View.GONE);
                    btnFindBus.setVisibility(View.GONE);
                }



            }
        });
    }
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        liveLocation = location;
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StudentMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getDriverLocation(){
        btnFindBus.setText("Cancel-RIDE");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("AvailableBusLocation").child(driverFoundId).child("l");
        unavailableDriverLocationRef = FirebaseDatabase.getInstance().getReference("UnavailableBusLocation").child(driverFoundId).child("l");


        driverLocationRefListener =driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && requestBoolean){
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat =0;
                    double locationLng =0;
                    if(map.get(0) !=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) !=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(driverMarker != null){
                        driverMarker.remove();
                    }

                    driverLocation = new Location("");
                    driverLocation.setLatitude(driverLatLng.latitude);
                    driverLocation.setLongitude(driverLatLng.longitude);

                    int distanceBetweenDriverAndMarker = (int) pickupMarkerLocation.distanceTo(driverLocation);
                    txtBusDistance.setText("Distance To Bus "+distanceBetweenDriverAndMarker+"m");

                    if (distanceBetweenDriverAndMarker<400){
                        Toast.makeText(StudentMapAct.this,"Bus is at your Pick-Up Marker", Toast.LENGTH_SHORT).show();
                        if (distanceBetweenDriverAndMarker<350){
                            confirmRide();
                        }

                    }

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your BUS").icon(BitmapFromVector(getApplicationContext(),R.drawable.buslocationmarkericon)));


                }

                else {
                    unavailableDriverLocationRefListener = unavailableDriverLocationRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && requestBoolean){
                                List<Object> map = (List<Object>) snapshot.getValue();
                                double locationLat =0;
                                double locationLng =0;
                                if(map.get(0) !=null){
                                    locationLat = Double.parseDouble(map.get(0).toString());
                                }
                                if(map.get(1) !=null){
                                    locationLng = Double.parseDouble(map.get(1).toString());
                                }
                                LatLng driverLatLng = new LatLng(locationLat,locationLng);
                                if(driverMarker != null){
                                    driverMarker.remove();
                                }

                                driverLocation = new Location("");
                                driverLocation.setLatitude(driverLatLng.latitude);
                                driverLocation.setLongitude(driverLatLng.longitude);

                                int distanceBetweenDriverAndMarker = (int) pickupMarkerLocation.distanceTo(driverLocation);
                                txtBusDistance.setText("Distance To Bus"+distanceBetweenDriverAndMarker);

                                if (distanceBetweenDriverAndMarker<400){
                                    Toast.makeText(StudentMapAct.this,"Bus is at your Pick-Up Marker", Toast.LENGTH_SHORT).show();
                                    if (distanceBetweenDriverAndMarker<350){
                                        confirmRide();

                                    }

                                }
                                driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your BUS"));


                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Bus is Not Available Now",Toast.LENGTH_LONG).show();

                                requestBooleanFalse();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void getAssignedDriverInfo(){
        driverInfoLinearLayout.setVisibility(View.VISIBLE);
        DatabaseReference driverInfoDatabase = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverFoundId);
        driverInfoDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("BusLNO")!=null){
                        txtBusLNO.setText("BUS License NO:-"+map.get("BusLNO").toString());
                    }
                    if (map.get("Name") !=null){
                        txtDriverName.setText("Driver Name:-"+map.get("Name").toString());
                    }
                    if (map.get("Phone") !=null){
                        txtDriverPhone.setText("Driver Contact NO:-"+map.get("Phone").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    final int LOCATION_REQUEST_CODE= 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please Provide the Permission",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void requestBooleanFalse(){
        requestBoolean= false;
        btnback.setVisibility(View.VISIBLE);
        geoQuery.removeAllListeners();

        driverLocationRef.removeEventListener(driverLocationRefListener);
        try {
            unavailableDriverLocationRef.removeEventListener(unavailableDriverLocationRefListener);
        }
        catch (Exception ex){

        }

        if (driverFoundId != null){
            deleteRequestReference = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverFoundId).child("STDPassengerUID");
            deleteRequestReferenceListener = deleteRequestReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()){
                        pushKey=snapshot.getKey();
                        firebaseID= snapshot.getValue().toString();
                        if (userId.equals(firebaseID)){
                            FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverFoundId).child("STDPassengerUID").child(pushKey).removeValue();
                            driverFoundId=  null;
                            deleteRequestReference.removeEventListener(deleteRequestReferenceListener);
                        }

                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
        }

        driverFound= false;
        radius= 1;

        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        studentPickupLocationgeoFire.removeLocation(userId);

        if (pickupMarker != null){
            pickupMarker.remove();
        }
        if (driverMarker !=null){
            driverMarker.remove();
        }
        btnFindBus.setText("Find Bus");
        //
        driverInfoLinearLayout.setVisibility(View.GONE);
        findBusLinearLayout.setVisibility(View.GONE);
        txtBusLNO.setText("");
        txtDriverName.setText("");
        txtDriverPhone.setText("");

        mMap.clear();
    }

    public void confirmRide(){
        btnConfirmRide.setVisibility(View.VISIBLE);
        btnConfirmRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmRideBoolean){

                    confirmRideBoolean=false;
                    btnConfirmRide.setVisibility(View.GONE);
                    btnConfirmRide.setText("Confirm-Ride");
                    requestBooleanFalse();
                    Toast.makeText(getApplicationContext(),"Ride is Finish",Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    confirmRideBoolean= true;
                    btnFindBus.setVisibility(View.GONE);
                    txtBusDistance.setText("");

                    userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

                    studentPickupLocationgeoFire.removeLocation(userId);

                    if (pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if (driverMarker !=null){
                        driverMarker.remove();
                    }
                    txtBusDistance.setVisibility(View.GONE);
                    btnConfirmRide.setText("Finish-Ride");
                }

            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}

/*

 */