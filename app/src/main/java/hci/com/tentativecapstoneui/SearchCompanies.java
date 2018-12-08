package hci.com.tentativecapstoneui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import hci.com.tentativecapstoneui.model.Company;
import hci.com.tentativecapstoneui.model.CompanyR;


public class SearchCompanies extends AppCompatActivity
        implements View.OnClickListener{

    RecyclerView recyclerView;
    Company_Adapter adapter;
    List<CompanyR> companyList;

    private Spinner spnr_Op_Hr, spnr_Op_Min, spnr_Op_day,
            spnr_Clos_Hr, spnr_Clos_Min, spnr_Clos_day;
    private Button btn_show_route, btn_filter;
    private Button btn_m, btn_t, btn_w, btn_th,btn_f, btn_s, btn_su;
    private Button btn_transit, btn_driv, btn_walk;

    Boolean isTrans = false , isDriv = false , isWalk = false;
    Boolean isBtnM = false, isBtnT= false, isBtnW = false, isBtnTh = false,
            isBtnF= false, isBtnSat=false, isBtnSun=false;


    //db things
    DatabaseReference databaseref, refCName;

    List<String> monComp, tueComp, wedComp, thuComp, friComp, satComp;

    List<String> listAddrs;//list of Address
    public static ArrayList<LatLng> listCoord;//list of coordinates
    private List<CompanyR> companiesList;

    TextView timePickerValueTextView;
    private TextView txtResult,txtResultCount;
    private AlertDialog.Builder alertDialogBuilder;
    String k; //numOfInterns
    long resultCount; //

    //for geocode
    String theAddress="";
    EditText editAddress;
    ProgressBar progressBar;
    boolean fetchAddress;
    int fetchType = Constants.USE_ADDRESS_LOCATION;

    private Spinner spinner1; //intern count spinner

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_companies);

        companiesList = new ArrayList<>();
        buildRecyclerViewer();
        initUI();

        bundle = new Bundle(); //for passing data to another activity

        alertDialogBuilder = new AlertDialog.Builder(this);

        listAddrs = new ArrayList<String>();
        listCoord = new ArrayList<LatLng>();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyCTzJZZrOIuel-pJLjFlBpgIoOdJ05MNGk ")
                .setApplicationId("hci.com.tentativecapstoneui")
                .setDatabaseUrl("https://high-service-220515.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(this /* Context */, options, "third");
        FirebaseApp app = FirebaseApp.getInstance("third");
        FirebaseDatabase secondaryDatabase = FirebaseDatabase.getInstance(app);
        databaseref = secondaryDatabase.getReference("companyInfo");


        /*DatabaseReference dbProducts = FirebaseDatabase.getInstance().getReference("products");
recycler view on create
        dbProducts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot productSnapshot : dataSnapshot.getChildren()){
                        Product p = productSnapshot.getValue(Product.class);
                        productList.add(p);
                    }
                    adapter = new ProductsAdapter(MainActivity.this, productList);
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        //adds mock address to addressList
        listAddrs.add("Ortigas Center, Quezon City, 1110 Metro Manila ");
        listAddrs.add("Boni Ave, Barangka, Mandaluyong, Metro Manila");
        listAddrs.add("8/F Tower 2, The Rockwell Business Center, 1600, Ortigas Ave, Pasig, Metro Manila");
        listAddrs.add("Lot 5 Block 2 E-Commerce Road Eastwood, Bagumbayan, Quezon City, 1110 Metro Manila");

        // to get value
     /* Bundle b = getIntent().getExtras();
        double result = b.getDouble("key");*/
    }

    private void buildRecyclerViewer() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void initUI() {
        findViewById(R.id.btn_showRoute).setOnClickListener(this);
        findViewById(R.id.btn_transit).setOnClickListener(this);
        findViewById(R.id.btn_mon).setOnClickListener(this);
        findViewById(R.id.btn_tue).setOnClickListener(this);
        findViewById(R.id.btn_wed).setOnClickListener(this);
        findViewById(R.id.btn_thu).setOnClickListener(this);
        findViewById(R.id.btn_fri).setOnClickListener(this);
        findViewById(R.id.btn_sat).setOnClickListener(this);
        findViewById(R.id.btn_transit).setOnClickListener(this);
        findViewById(R.id.btn_driv).setOnClickListener(this);
        findViewById(R.id.btn_walk).setOnClickListener(this);
        timePickerValueTextView = (TextView)findViewById(R.id.txt_OpHr);
        txtResultCount = (TextView)findViewById(R.id.txt_resultCount);
        btn_m = (Button) findViewById(R.id.btn_mon);
        btn_t = (Button) findViewById(R.id.btn_tue);
        btn_w = (Button) findViewById(R.id.btn_wed);
        btn_th = (Button) findViewById(R.id.btn_thu);
        btn_f = (Button) findViewById(R.id.btn_fri);
        btn_s = (Button) findViewById(R.id.btn_sat);
        btn_transit  = (Button) findViewById(R.id.btn_transit);
        btn_walk = (Button) findViewById(R.id.btn_walk);
        btn_driv = (Button) findViewById(R.id.btn_driv);
    }


   /* @Override //this is async method
    protected void onStart() {
        super.onStart();

    }*/

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (v.getId()) {

            case R.id.btn_showRoute:
                Log.d("btn","shRoute is clicked");
                Log.d("option ", isTrans.toString()+" "+isDriv.toString().toString()+" "+ isWalk.toString());
                Log.d("Time: f",timePickerValueTextView.getText().toString());
                //get filters before showing route

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                String errorMessage = "";
                List<Address> addresses = null;
                for (int i=0; i<listAddrs.size();i++){
                    listCoord.add(doGeocode(listAddrs.get(i)));  //CALLS GEOCODE FUNCTION
                }
                //pass the list to MapActivity
                bundle.putParcelableArrayList("coordinates", listCoord);
                Intent intent = new Intent(SearchCompanies.this, Map.class);
                intent.putExtras(bundle);
                startActivity(intent);

                break;

            case R.id.btn_mon:
                if(isBtnM){ //off
                    isBtnM=false;
                    btn_m.setBackgroundColor(Color.parseColor("#4da4d3")); //#4da4d3 blue #fab22d yellow
                }else {
                    isBtnM = true; //on
                    btn_m.setBackgroundColor(Color.parseColor("#fab22d"));
                    queryMon();
                }
                btn_m.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_m.setBackgroundColor(Color.parseColor("#3a89c6")); //darkblue
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_tue:
                if(isBtnT){ //off
                    isBtnT=false;
                    btn_t.setBackgroundColor(Color.parseColor("#4da4d3"));
                }else {
                    isBtnT = true; //on
                    btn_t.setBackgroundColor(Color.parseColor("#fab22d"));
                    queryTue();
                }
                btn_t.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_t.setBackgroundColor(Color.parseColor("#3a89c6"));
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_wed:
                if(isBtnW){ //off
                    isBtnW=false;
                    btn_w.setBackgroundColor(Color.parseColor("#4da4d3"));
                }else {
                    isBtnW = true; //on
                    btn_w.setBackgroundColor(Color.parseColor("#fab22d"));
                    queryWed();
                }
                btn_w.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_w.setBackgroundColor(Color.parseColor("#3a89c6"));
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_thu:
                if(isBtnTh){ //off
                    isBtnTh=false;
                    btn_th.setBackgroundColor(Color.parseColor("#4da4d3"));
                }else {
                    isBtnTh = true; //on
                    btn_th.setBackgroundColor(Color.parseColor("#fab22d"));
                    queryThu();
                }
                btn_th.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_th.setBackgroundColor(Color.parseColor("#3a89c6"));
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_fri:
                if(isBtnF){ //off
                    isBtnF=false;
                    btn_f.setBackgroundColor(Color.parseColor("#4da4d3"));
                }else {
                    isBtnF = true; //on
                    btn_f.setBackgroundColor(Color.parseColor("#fab22d"));
                    queryFri();
                }
                btn_f.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_f.setBackgroundColor(Color.parseColor("#3a89c6"));
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_sat:
                if(isBtnSat){ //off
                    isBtnSat=false;
                    btn_s.setBackgroundColor(Color.parseColor("#4da4d3"));
                }else {
                    isBtnSat = true; //on
                    btn_s.setBackgroundColor(Color.parseColor("#fab22d"));
                    querySat();
                }
                btn_s.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            btn_s.setBackgroundColor(Color.parseColor("#3a89c6"));
                        }
                        return false;
                    }
                });
                break;

            case R.id.btn_transit:
                if(isDriv == true || isWalk == true){
                    btn_driv.setBackgroundColor(Color.parseColor("#4da4d3"));
                    btn_walk.setBackgroundColor(Color.parseColor("#4da4d3"));
                    isDriv=false;
                    isWalk=false;
                    btn_transit.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_transit.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                     intent.putExtras(bundle);           btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });

                    isTrans = true; //transit is on
                    btn_transit.setBackgroundColor(Color.parseColor("#fab22d"));

                    String modeOfTransport="transit";
                    //  Bundle bundle = new Bundle(); //can I put manyValues in bundle?
                    bundle.putString("modeOfTranspo",modeOfTransport);
                    intent = new Intent(SearchCompanies.this, Map.class);


                }else{
                    if(isTrans){ //off
                        isTrans=false;
                        btn_transit.setBackgroundColor(Color.parseColor("#4da4d3"));
                    }else {
                        isTrans = true; //on
                        btn_transit.setBackgroundColor(Color.parseColor("#fab22d"));
                    }
                    btn_transit.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_transit.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                                btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });
                }

                break;

            case R.id.btn_driv:
                if(isTrans == true || isWalk == true) {
                    btn_transit.setBackgroundColor(Color.parseColor("#4da4d3"));
                    btn_walk.setBackgroundColor(Color.parseColor("#4da4d3"));
                    isTrans = false;
                    isWalk = false;

                    btn_driv.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_driv.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                                btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });

                    isDriv = true; //on
                    btn_driv.setBackgroundColor(Color.parseColor("#fab22d"));

                    String modeOfTransport="drive";
                    //  Bundle bundle = new Bundle(); //can I put manyValues in bundle?
                    bundle.putString("modeOfTranspo",modeOfTransport);
                    intent = new Intent(SearchCompanies.this, Map.class);
                    intent.putExtras(bundle);
                }else{
                    if (isDriv) { //off
                        isDriv = false;
                        btn_driv.setBackgroundColor(Color.parseColor("#4da4d3"));
                    } else {
                        isDriv = true; //on
                        btn_driv.setBackgroundColor(Color.parseColor("#fab22d"));
                    }
                    btn_driv.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_driv.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                                btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });
                }
                break;

            case R.id.btn_walk:
                if(isTrans == true || isDriv == true) {
                    btn_transit.setBackgroundColor(Color.parseColor("#4da4d3"));
                    btn_driv.setBackgroundColor(Color.parseColor("#4da4d3"));
                    isTrans = false;
                    isDriv = false;

                    btn_walk.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_walk.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                                btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });

                    isWalk = true; //on
                    btn_walk.setBackgroundColor(Color.parseColor("#fab22d"));

                    String modeOfTransport="walking";
                    //  Bundle bundle = new Bundle(); //can I put manyValues in bundle?
                    bundle.putString("modeOfTranspo",modeOfTransport);
                    intent = new Intent(SearchCompanies.this, Map.class);

                }else {
                    if (isWalk) { //off
                        isWalk = false;
                        btn_walk.setBackgroundColor(Color.parseColor("#4da4d3"));
                    } else {
                        isWalk = true; //on
                        btn_walk.setBackgroundColor(Color.parseColor("#fab22d"));
                    }
                    btn_walk.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                btn_walk.setBackgroundColor(Color.parseColor("#3a89c6"));
                            }/* else if (event.getAction() == KeyEvent.ACTION_UP) {
                                btn_m.setBackgroundColor(Color.parseColor("#4da4d3"));
                            }*/
                            return false;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    public void queryMon(){
        databaseref.orderByChild("Mon").equalTo("Y").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) { //listen for currently selected item in recyclerview
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                            Toast.makeText(SearchCompanies.this, "currSelected is "+currSelected,
                                    Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }



    public void queryTue(){
        databaseref.orderByChild("Tue").equalTo("Y").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }


    public void queryWed(){
        databaseref.orderByChild("Wed").equalTo("Y").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }


    public void queryThu(){
        databaseref.orderByChild("Thu").equalTo("Y").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }



    public void queryFri(){
        databaseref.orderByChild("Fri").equalTo("Y").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }


    public void querySat(){
        databaseref.orderByChild("Sat").addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        })*/
    }

    public void queryInternNo(){
        databaseref.orderByChild("NumOfInterns").equalTo(k).addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) { //listen for currently selected item in recyclerview
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                            Toast.makeText(SearchCompanies.this, "currSelected is "+currSelected,
                                    Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }


    public void queryTime(){
        databaseref.orderByChild("TimePref").equalTo(timePickerValueTextView.getText().toString()
        ).addChildEventListener(new ChildEventListener() //this works
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.exists()){
                    Log.d("compAddress: ", dataSnapshot.getKey()); //works
                    //dataSnapshot has all the children
                    String addresses= dataSnapshot.getKey();
                    Log.d("compName: ", dataSnapshot.child("Company Name").getValue().toString()); //works
                    resultCount = dataSnapshot.getChildrenCount();
                    txtResultCount.setText("No. of results: "+ resultCount);

                    CompanyR cor = new CompanyR(dataSnapshot.child("Company Name").getValue().toString(),dataSnapshot.getKey());
                    companiesList.add(cor);
                    adapter= new Company_Adapter(SearchCompanies.this, companiesList);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new Company_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) { //listen for currently selected item in recyclerview
                            String currSelected = companiesList.get(position).getCompanyAddres();
                            //add Address to destinationList
                            Log.d("AdapterClick", currSelected);
                            Toast.makeText(SearchCompanies.this, "currSelected is "+currSelected,
                                    Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

       /* refCName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("compName",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })*/
    }


    public void pickOpHr(View view) {
        // Create a new OnTimeSetListener instance. This listener will be invoked when user click ok button in TimePickerDialog.
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String AM_PM ;
                if(hour < 12) {
                    AM_PM = "AM";
                } else {
                    AM_PM = "PM";
                }
                if(hour>12){hour=hour-12;}
                else if(hour==0){hour=hour+12;}
                StringBuffer strBuf = new StringBuffer();
                strBuf.append("");
                strBuf.append(String.format("%02d",hour));
                strBuf.append(":");
                strBuf.append(String.format("%02d",minute));
                timePickerValueTextView.setText(strBuf.toString()+" "+ AM_PM);

                Log.d("timePick",timePickerValueTextView.getText().toString());

                queryTime();




            }
        };
        Calendar now = Calendar.getInstance();
        int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = now.get(java.util.Calendar.MINUTE);
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(SearchCompanies.this,
                        android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth, onTimeSetListener, hour, minute, false);
        // timePickerDialog.setIcon(R.drawable.if_snowman);
        timePickerDialog.setTitle("Please select time.");
        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        openActivity(Map.class);
    }

    public void openActivity(Class<?> cs) {
        startActivity(new Intent(SearchCompanies.this, cs));
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

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //gets latitude show in textview
                        progressBar.setVisibility(View.GONE);
                        txtResult.setText("Latitude: " + address.getLatitude() + "\n" +
                                "Longitude: " + address.getLongitude() + "\n" +
                                "Address: " + resultData.getString(Constants.RESULT_DATA_KEY));
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      /*  progressBar.setVisibility(View.GONE);
                        infoText.setVisibility(View.VISIBLE);
                        infoText.setText(resultData.getString(Constants.RESULT_DATA_KEY));*/
                    }
                });
            }
        }
    }

    public class ItemSelectedListener implements AdapterView.OnItemSelectedListener {
        //get strings of first item
        String firstItem = String.valueOf(spinner1.getSelectedItem());
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            k = parent.getItemAtPosition(pos).toString();
            queryTime();
           /* if(k=="1"){
                Log.d("internNo","k: "+k);
            }else if(k.equals("2")){
                Log.d("internNo","k: "+k);
            }else if(k.equals("3")){
                Log.d("internNo","k: "+k);
            }else if(k.equals("4")){
                Log.d("internNo","k: "+k);
            }*/
               /* Toast.makeText(parent.getContext(),
                        "You have selected : " + parent.getItemAtPosition(pos).toString(),
                        Toast.LENGTH_LONG).show();*/  //this works
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg) {
        }
    }
}

