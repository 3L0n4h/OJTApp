package hci.com.tentativecapstoneui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.nio.charset.MalformedInputException;

import de.hdodenhof.circleimageview.CircleImageView;
import hci.com.tentativecapstoneui.ui.ChatActivity;
import hci.com.tentativecapstoneui.ui.FriendsListActivity;
import hci.com.tentativecapstoneui.ui.ProfileActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ActionBar actionBar;
    private FloatingActionButton addPostBtn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsers;
    private String fullname;
    private CircleImageView circleimageview;
    Fragment fragment;
    TextView header_name;
    TextView header_email;
    TabLayout tabs;
    NavigationView navigationView;
    Menu nav_Menu;
    String uid = "";
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mUsers = FirebaseDatabase.getInstance().getReference("Users");
        if (mAuth.getCurrentUser() != null) {
            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String id = mAuth.getCurrentUser().getUid();
                    if (!(dataSnapshot.child("Admins").hasChild(id) || dataSnapshot.child("Students").hasChild(id))) {
                        Toast.makeText(MainActivity.this, "Please Login", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(MainActivity.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Intent i = new Intent(MainActivity.this, Login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#de000000")));

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myRef = FirebaseDatabase.getInstance().getReference("Users");

        nav_Menu = navigationView.getMenu();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(uid)) {
                        nav_Menu.findItem(R.id.menu_home).setVisible(true);
                        nav_Menu.findItem(R.id.menu_Settings).setVisible(true);
                        nav_Menu.findItem(R.id.menu_admin).setVisible(false);
                        nav_Menu.findItem(R.id.menu_others).setVisible(true);
                    } else if (dataSnapshot.child("Admins").hasChild(uid) && dataSnapshot.child("Admins").child(uid).child("FirstName")
                            .getValue().toString().contentEquals("Admin")) {

                        nav_Menu.findItem(R.id.menu_home).setVisible(true);
                        nav_Menu.findItem(R.id.menu_Settings).setVisible(true);
                        nav_Menu.findItem(R.id.menu_admin).setVisible(true);
                        nav_Menu.findItem(R.id.menu_others).setVisible(true);

                    } else if (dataSnapshot.child("Admins").hasChild(uid) && !dataSnapshot.child("Admins").child(uid).child("FirstName")
                            .getValue().toString().contentEquals("Admin")) {

                        nav_Menu.findItem(R.id.menu_home).setVisible(true);
                        nav_Menu.findItem(R.id.menu_Settings).setVisible(true);
                        nav_Menu.findItem(R.id.menu_admin).setVisible(true);
                        nav_Menu.findItem(R.id.nav_Csv).setVisible(false);
                        nav_Menu.findItem(R.id.nav_Csv).setVisible(false);
                        nav_Menu.findItem(R.id.nav_pending).setVisible(false);
                        nav_Menu.findItem(R.id.menu_others).setVisible(true);

                    } else {

                        nav_Menu.findItem(R.id.menu_home).setVisible(false);
                        nav_Menu.findItem(R.id.menu_Settings).setVisible(false);
                        nav_Menu.findItem(R.id.menu_admin).setVisible(false);
                        nav_Menu.findItem(R.id.menu_others).setVisible(false);

                        mAuth.signOut();
                        Intent i = new Intent(MainActivity.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        tabs = findViewById(R.id.tabs);

        tabPager tabPager = new tabPager(getSupportFragmentManager());


        fragment = new One();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
        setTitle("News Feed");
//        addPostBtn.setVisibility(View.VISIBLE);

        tabs.getTabAt(0).setIcon(R.mipmap.home);
        tabs.getTabAt(1).setIcon(R.mipmap.backpack);
        tabs.getTabAt(2).setIcon(R.mipmap.text);
        tabs.getTabAt(3).setIcon(R.mipmap.mail);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new One();
                        setTitle("News Feed");
                        break;
                    case 1:
                        fragment = new Two();
                        setTitle("Backpack");
                        break;
                    case 2:
                        fragment = new Three();
                        setTitle("Chat");
                        break;
                    case 3:
                        fragment = new Four();
                        setTitle("Notification");
                        break;
                }
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new One();
                        setTitle("News Feed");
                        break;
                    case 1:
                        fragment = new Two();
                        setTitle("Backpack");
                        break;
                    case 2:
                        fragment = new Three();
                        setTitle("Chat");
                        break;
                    case 3:
                        fragment = new Four();
                        setTitle("Notification");
                        break;
                }
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        header_name = headerView.findViewById(R.id.acc_name);
        header_email = headerView.findViewById(R.id.acc_email);
        circleimageview = headerView.findViewById(R.id.acc_image);


        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference().child("Users");

        if (mAuth.getCurrentUser() != null) {
            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        String email = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("Email").getValue().toString();
                        String imageUrl = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();

                        fullname = firstName + " " + lastName;

                        header_name.setText("Welcome, " + fullname);
                        header_email.setText(email);

                        Glide.with(MainActivity.this).load(imageUrl).fitCenter().crossFade().into(circleimageview);
//                        Picasso.with(MainActivity.this).load(imageUrl).into(circleimageview);
                    } else if (dataSnapshot.child("Admins").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        String email = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("Email").getValue().toString();
                        String imageUrl = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();

                        fullname = firstName + " " + lastName;

                        header_name.setText("Welcome, " + fullname);
                        header_email.setText(email);
                        Glide.with(MainActivity.this).load(imageUrl).fitCenter().crossFade().into(circleimageview);
//                        Picasso.with(MainActivity.this).load(imageUrl).into(circleimageview);
                    } else {
                        Toast.makeText(MainActivity.this, "User account not found", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void createNewChat(View view){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.listFriends) {
//            //Open up activity where a user can add and view friends
//            Intent intent = new Intent(this, FriendsListActivity.class);
//            startActivity(intent);
//        }
        if (id == R.id.profilePage) {
            //Open up activity where a user can add and view friends
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            Intent button = new Intent(MainActivity.this, MainActivity.class);
            button.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(button);
        } else if (id == R.id.nav_pending) {
            Intent button = new Intent(MainActivity.this, PendingPost.class);
            startActivity(button);
        } else if (id == R.id.nav_calendar) {
            tabs.setVisibility(View.GONE);

            setTitle("Set a Schedule");
            fragment = new CalendarUser();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();

        } else if (id == R.id.nav_Csv) {

            tabs.setVisibility(View.GONE);

            setTitle("Upload Class List");
            fragment = new UploadCSV();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();

        } else if (id == R.id.nav_Help) {
//            Intent button = new Intent(MainActivity.this, Help.class);
//            startActivity(button);
        } else if (id == R.id.nav_About) {
            tabs.setVisibility(View.GONE);
            setTitle("About TIP");
            fragment = new About();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else if (id == R.id.nav_Events) {
            tabs.setVisibility(View.GONE);
            setTitle("Announcements");
            fragment = new Events();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else if (id == R.id.nav_Ojt) {
            tabs.setVisibility(View.GONE);
            setTitle("OJT Program");
            fragment = new OjtProgram();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else if (id == R.id.menu_Settings) {
            tabs.setVisibility(View.GONE);

            setTitle("Account Settings");
            fragment = new Settings();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else if (id == R.id.nav_map) {
            Intent map = new Intent(MainActivity.this, Map.class);
            startActivity(map);
        } else if (id == R.id.nav_Logout) {
            FirebaseAuth.getInstance().signOut();
            Intent signOut = new Intent(MainActivity.this, Login.class);
            signOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signOut);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}