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
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.example.tracking.databinding.ActivityDriverMapBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverMapAct extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    private ActivityDriverMapBinding binding;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Button btnBack,btnStartRide;
    private String studentId="",userId,driverId;
    Marker pickupMarker;
    private DatabaseReference assignedStudentPickupLocationRef;
    private ValueEventListener assignedStudentPickupLocationListener;
    FirebaseAuth firebaseAuth;
    GeoFire availableBusLocationRefGeofire,unavailableBusLocationRefGeofire;
    SupportMapFragment mapFragment;
    Location lastLocation;
    private int childrenCount;
    private long chilCountLong;
    private boolean requestBoolean = false;
    private DatabaseReference availableBusLocationRef,assignedStudentGetKeyRef,assignedStudentGetChildrenCountRef,unavailableBusLocationRef;
    private ChildEventListener assignedStudentGetKeyListener;
    LatLng latLng,PickupLOcation;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.black};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gmapdriver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        else {
            mapFragment.getMapAsync(this);
        }

        btnStartRide= findViewById(R.id.btnStartRide);
        btnBack = findViewById(R.id.btnBackDriverMap);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapAct.this,DriverMainMenuAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });


        btnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBoolean){
                    mMap.clear();
                    studentId ="";
                    requestBoolean = false;
                    btnBack.setVisibility(View.VISIBLE);

                    availableBusLocationRefGeofire.removeLocation(userId);

                    GeoFire unavailableBusLocationRefGeofire = new GeoFire(unavailableBusLocationRef);
                    unavailableBusLocationRefGeofire.removeLocation(userId);

                    FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(userId).child("STDPassengerUID").removeValue();
                    btnStartRide.setText("Start-Ride");
                    if (assignedStudentPickupLocationListener != null){
                        assignedStudentPickupLocationRef.removeEventListener(assignedStudentPickupLocationListener);
                    }
                    assignedStudentGetKeyRef.removeEventListener(assignedStudentGetKeyListener);


                }
                else {
                    if (studentId.equals(null)){
                        if (pickupMarker != null){
                            pickupMarker.remove();
                        }
                    }
                    btnBack.setVisibility(View.GONE);
                    requestBoolean = true;

                    userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

                    unavailableBusLocationRef = FirebaseDatabase.getInstance().getReference("UnavailableBusLocation");
                    unavailableBusLocationRefGeofire = new GeoFire(unavailableBusLocationRef);

                    availableBusLocationRef = FirebaseDatabase.getInstance().getReference("AvailableBusLocation");
                    availableBusLocationRefGeofire = new GeoFire(availableBusLocationRef);
                    availableBusLocationRefGeofire.setLocation(userId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                    btnStartRide.setText("Cancel-Ride");

                    getChilCount();
                    getAssignedStudent();
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

    public void getChilCount(){
        driverId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        assignedStudentGetChildrenCountRef =FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverId).child("STDPassengerUID");
        assignedStudentGetChildrenCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    childrenCount = (int) snapshot.getChildrenCount();
                    chilCountLong = snapshot.getChildrenCount();

                    if (chilCountLong==2){

                        availableBusLocationRefGeofire.removeLocation(userId);

                        unavailableBusLocationRefGeofire.setLocation(userId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));



                    }
                    if (chilCountLong<2){

                        unavailableBusLocationRefGeofire.removeLocation(userId);

                        availableBusLocationRefGeofire.setLocation(userId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedStudent() {

        assignedStudentGetKeyRef = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverId).child("STDPassengerUID");
         assignedStudentGetKeyListener = assignedStudentGetKeyRef.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                 if (snapshot.exists()){

                     studentId = snapshot.getValue().toString();

                     getAssignedStudentPickupLocation();
                     getChilCount();

                 }
                 else {
                     studentId ="";
                     if (pickupMarker != null){
                         pickupMarker.remove();
                     }
                     if (assignedStudentPickupLocationListener != null){
                         assignedStudentPickupLocationRef.removeEventListener(assignedStudentPickupLocationListener);
                     }
                 }
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                 studentId="";
                 mMap.clear();
                 getChilCount();
                 getAssignedStudent();

             }

             @Override
             public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    private void getAssignedStudentPickupLocation(){

        assignedStudentPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("StudentPickupLocation").child(studentId).child("l");
        assignedStudentPickupLocationListener=assignedStudentPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat =0;
                    double locationLng =0;
                    if(map.get(0) !=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) !=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng PickupLOcation = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(PickupLOcation).title("Pick-Up").icon(BitmapFromVector(getApplicationContext(),R.drawable.pickupmarkericon)));
                }
                else {
                    if (pickupMarker != null){
                        pickupMarker.remove();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        if (pickupMarker != null){
            pickupMarker.remove();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLocation = location;
        firebaseAuth = FirebaseAuth.getInstance();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
DatabaseReference busChildCountRef = FirebaseDatabase.getInstance().getReference().child("User").child("Bus").child(driverFoundId).child("STDPassengerUID");
                    busChildCountRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long childrenCountLong = snapshot.getChildrenCount();
                            childrenCountInt = (int) childrenCountLong;

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
 */