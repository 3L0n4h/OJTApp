package hci.com.tentativecapstoneui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import hci.com.tentativecapstoneui.model.CompanyR;

public class Map extends AppCompatActivity implements OnMapReadyCallback,
        ConnectionCallbacks,
        DirectionCallback,
        LocationListener, View.OnClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnConnectionFailedListener
{
    LatLng currLng;
    DatabaseReference databaseref;
    private GoogleMap googleMap;
    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    boolean btn_traffc_clicked=false;
    List<Double> distance;
    List<String> listAddrs;//list of Address
    public static ArrayList<LatLng> listCoord;//list of coordinates

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition mCameraPosition;

    String selectedAddress; //autocomplete

    private List<CompanyR> companiesList;
    HashMap<LatLng, Double> address_distance;

    ArrayList<LatLng> coordinates;//stores loc coordinates
    private String serverKey = "AIzaSyD7z_O9pRgwsdDRqtEJZf5sTLkIafoQM0M";

    Geocoder geocoder;
    String numMarkers[] =
            {       "firststop","secondstop",
                    "thirdstop","fourthstop","fifstop", "sixthstop","sevstop",
                    "eightstop"
            };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        companiesList = new ArrayList<>();
        // Retrieve location and camera position from saved instance state.

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        address_distance = new HashMap<LatLng, Double>();

        coordinates = getIntent().getParcelableArrayListExtra("coordinates"); //gets coordinates from SearchCompanies activity
        if(coordinates==null){

        }else {
            requestDirection();
        }

        geocoder = new Geocoder(this, Locale.getDefault());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyCTzJZZrOIuel-pJLjFlBpgIoOdJ05MNGk")
                .setApplicationId("hci.com.tentativecapstoneui")
                .setDatabaseUrl("https://high-service-220515.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(this /* Context */, options, "secondary");
        FirebaseApp app = FirebaseApp.getInstance("secondary");
      /*  //mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseDatabase secondaryDatabase = FirebaseDatabase.getInstance(app);*/
        databaseref = FirebaseDatabase.getInstance(app).getReference("companyInfo");

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();



        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) { //search companies near designated loc
                selectedAddress = place.getAddress().toString();
                Log.d("autofrag","autofrag: "+selectedAddress);
                //   Toast.makeText(WaypointsDirectionActivity.this,"Place: " + place.getName() +" address: "+selectedAddress,Toast.LENGTH_LONG).show();
                LatLng latLng = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude); //lat long

                //query database all.. change it to search by keyword later
                databaseref.orderByChild("Company Name").addChildEventListener(new ChildEventListener() //this works
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists()) {
                            Log.d("dbref: ", dataSnapshot.getKey());
                            String compAdd = dataSnapshot.getKey();
                            CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),
                                    dataSnapshot.getKey(), dataSnapshot.child("ContactPerson").toString(),
                                    dataSnapshot.child("TimePref").toString(),
                                    dataSnapshot.child("remarks").toString());
                            companiesList.add(cor);

                            Log.d("ChildAdd:",companiesList+" ");
                            for(int i=0;i<companiesList.size();i++) {
                     //           Log.d("doGeocode: ",""+doGeocode(companiesList.get(i).getCompanyAddres()).toString());
                                Log.d("sizeCL", companiesList.size()+"");
                               // listCoord.add(doGeocode(companiesList.get(i).getCompanyAddres()));
                            }

                           /* LatLng YourSelectedPlace= doGeocode(selectedAddress); //get Latlng of address
                            for(int z=0;z<listCoord.size();z++){
                                //check distance between selectedPlace and database address
                                Double meters= CalculateDistance(YourSelectedPlace,listCoord.get(z)); //source Dest
                                if(meters<3000){
                                    //plot
                                    MarkerOptions userMarkerOptions = new MarkerOptions();
                                    userMarkerOptions.position(listCoord.get(z)); //LatLng
                                    userMarkerOptions.icon(BitmapDescriptorFactory
                                            .fromBitmap(resizeMapIcons("thepin",100,100)));
                                    googleMap.addMarker(userMarkerOptions);
                                }
                                distance.add(meters); //returns float

                                //check if distance is
                                Collections.sort(distance);
                                address_distance.put(listCoord.get(z),distance.get(z)); //puts distance in hashmap
                            }*/






/*
                                String sentence = "Check this answer and you can find the keyword with this code";
                                String search  = "keyword";

                                if ( sentence.toLowerCase().indexOf(search.toLowerCase()) != -1 ) {
                                    System.out.println("I found the keyword");
                                } else {
                                    System.out.println("not found");
                                }
*/
                            // listCoord.add(doGeocode(companiesList.get(i).getCompanyAddres()));
                        }else{
                            Log.d("dbref:","dataSnap not found");
                        }
                    }

                /*    private String companyName;
                    private String companyAddres;
                    private String contactPerson;
                    private String contactTimePref;
                    private String remark;*/

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                /*databaseref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Log.d("dbref: ", dataSnapshot.getKey());
                        }else{
                            Log.d("dbref:","dataSnap not found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/


                /*if(listAddrs!=null){
                    List<Address> addresses = null;
                    for (int i=0; i<listAddrs.size();i++){
                        listCoord.add(doGeocode(listAddrs.get(i)));  //CALLS GEOCODE FUNCTION to convert address to coordinates
                    }

                    for(int z=0;z<listCoord.size();z++){
                        Double meters= CalculateDistance(listCoord.get(z));
                        distance.add(meters); //returns float
                        Collections.sort(distance);
                        address_distance.put(listCoord.get(z),distance.get(z)); //puts distance in hashmap
                    }

                    //check if distance is >3000 then add marker


                    Iterator it = address_distance.entrySet().iterator();
                    int c=0;
                    while (it.hasNext() || c<7) {
                        c++;
                        HashMap.Entry<LatLng, Double> pair = (HashMap.Entry)it.next();
                        System.out.println(pair.getKey() + " = " + pair.getValue());
                        if(pair.getValue()<3000){
                            userMarkerOptions.position(pair.getKey());
                            userMarkerOptions.icon(BitmapDescriptorFactory
                                    .fromBitmap(resizeMapIcons("thepin",100,100)));
                            googleMap.addMarker(userMarkerOptions);
                        }
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                }
*/


               /* userMarkerOptions.position(latLng);
                userMarkerOptions.title(selectedAddress);
                userMarkerOptions.icon(BitmapDescriptorFactory
                        .fromBitmap(resizeMapIcons("thepin",100,100)));*/
                // googleMap.addMarker(userMarkerOptions);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });

    }

    public LatLng doGeocode(String str){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(str, 1);
        } catch (IOException e) {
            Log.e("geocoderErr1: ", errorMessage, e);
        }
        return new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
    }


    @Override
    public void onBackPressed() {
        openActivity(MainActivity.class);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // getLocationPermission();
        googleMap = map;
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setOnMyLocationButtonClickListener(this);
        //  googleMap.setOnMyLocationClickListener(this);

    }
    /*private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }*/



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_companies) {
            openActivity(SearchCompanies.class);
            finish();
        }else if(id== R.id.btn_traffc){
            if(btn_traffc_clicked)
            { btn_traffc_clicked=false;
                googleMap.setTrafficEnabled(false);
            }
            else
            {btn_traffc_clicked=true;
                googleMap.setTrafficEnabled(true);
            }
        }
    }

    public void requestDirection() {
        LatLng urLoc = new LatLng(mLastLocation.getLatitude(), //urLoc is showing null
                mLastLocation.getLongitude());
        Toast.makeText(this, "Requesting Direction...", Toast.LENGTH_SHORT).show();
        //    Snackbar.make(btnRequestDirection, "Requesting Direction...", Snackbar.LENGTH_SHORT).show();
        //  GoogleDirectionConfiguration.getInstance().setLogEnabled(true);
        GoogleDirection.withServerKey(serverKey) //this isn't working
                //.from(urLng)
                .from(urLoc).to(coordinates.get(1))
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }






    /* public void requestDirection() { //gets coordinates then request Direction
         LatLng urLoc = new LatLng(mLastKnownLocation.getLatitude(),
                 mLastKnownLocation.getLongitude());
         Toast.makeText(this, "Requesting Routes...", Toast.LENGTH_SHORT).show();
         //    Snackbar.make(btnRequestDirection, "Requesting Direction...", Snackbar.LENGTH_SHORT).show();
         GoogleDirectionConfiguration.getInstance().setLogEnabled(true);
         GoogleDirection.withServerKey(serverKey)
                 //.from(urLng)
                 .from(urLoc)
            *//*     .and(coordinates.get(1))
                .and(coordinates.get(3))*//*
                .to(coordinates.get(2))
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }
*/
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) { //where waypoints icons is set
        // Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);
            int legCount = route.getLegList().size();
            for (int index = 0; index < legCount; index++) {
                Leg leg = route.getLegList().get(index);
                googleMap.addMarker(new MarkerOptions()
                        .position(leg.getStartLocation()
                                .getCoordination())
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(resizeMapIcons("thepin",100,100))));
                if (index == legCount - 1) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(leg.getEndLocation()
                                    .getCoordination())
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(resizeMapIcons(numMarkers[index],100,100)))); //sequence of Pin
                }

                List<Step> stepList = leg.getStepList();
                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter
                        .createTransitPolyline(this, stepList, 5, Color.rgb(161, 66, 244), 3, Color.BLUE);
                for (PolylineOptions polylineOption : polylineOptionList) {
                    googleMap.addPolyline(polylineOption);
                }
            }
            setCameraWithCoordinationBounds(route);
            //   btnRequestDirection.setVisibility(View.GONE); //rquestDirection is removed here
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        //   Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

    }



    private double CalculateDistance(LatLng srcArea, LatLng destArea)
    {
        //Convert LatLng to Location
        /*LatLng urLoc = new LatLng(mLastKnownLocation.getLatitude(),
                mLastKnownLocation.getLongitude());*/
        Location currLoc = new Location("Origin");
        currLoc.setLongitude(srcArea.longitude); //urLoc.longitude
        currLoc.setLatitude(srcArea.latitude);

        Location location = new Location("tmploc");
        location.setLatitude(destArea.latitude);
        location.setLongitude(destArea.longitude);

        return location.distanceTo(currLoc);
    }






    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /*private void getLocationPermission() {
     *//*
 * Request location permission, so that we can get the location of the
 * device. The result of the permission request is handled by a callback,
 * onRequestPermissionsResult.
 *//*
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }*/

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failure", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng currLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcons("currpin",120,120)));//change to custom
        mCurrLocationMarker = googleMap.addMarker(markerOptions);

        //move map camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }
    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
    public void openActivity(Class<?> cs) {
        startActivity(new Intent(this, cs));
    }

    /**

     * Saves the state of the map when the activity is paused.

     */

    @Override

    protected void onSaveInstanceState(Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
}
