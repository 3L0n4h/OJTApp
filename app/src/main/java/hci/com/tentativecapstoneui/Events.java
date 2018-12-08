package hci.com.tentativecapstoneui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hci.com.tentativecapstoneui.model.CalendarModelClass;


/**
 * Rica Monteverde 12/04/18
 */
public class Events extends android.support.v4.app.Fragment implements ImageAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabase, mUsers, mDatabaseLikes, mDatabaseComments;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean mProcessLike = false;
    private FirebaseDatabase database;
    private String Post_Key;
    private String fullname;
    private NavigationView navigationView;
    private ImageView NavProfileImage;
    private TextView navProfileName;
    String accountPrivs = "";
    Button addpost;
    String imageUrl;
    String urls;
    String urlOpen;
    private ValueEventListener mDBListener;
    private List<CalendarModelClass> mKeys;

    android.support.v4.app.Fragment fragment = null;

    public Events() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        mKeys = new ArrayList<>();

        //initialize recyclerview and FIrebase objects
        recyclerView = rootView.findViewById(R.id.recyclerview2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //RETRIEVE THE PRE-APPROVE
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);


        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post").child("Events");
        mAuth = FirebaseAuth.getInstance();
        mDBListener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mKeys.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CalendarModelClass upload = postSnapshot.getValue(CalendarModelClass.class);
                    upload.setmKey(postSnapshot.getKey());
                    mKeys.add(upload);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(getContext(), Register.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }
            }
        };
//        postBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, PostActivity.class);
//                startActivity(intent);
//            }
//        });
        if (mAuth.getCurrentUser() != null) {
            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        imageUrl = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();
                        accountPrivs = "Student";
                        fullname = firstName + " " + lastName;
                    } else if (dataSnapshot.child("Admins").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        imageUrl = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();
                        accountPrivs = "Admin";
                        fullname = firstName + " " + lastName;
                    } else {
                        Toast.makeText(getContext(), "User account not found", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return rootView;
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(getContext(), Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

        getActivity().finish();
    }

    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(current_user_id)) {
                    sendUserToSetupActivity();
                }
                if (dataSnapshot.child("Students").hasChild(current_user_id)) {
                    String firstName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                    String lastName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                    accountPrivs = "Student";
                    fullname = firstName + " " + lastName;
                } else if (dataSnapshot.child("Admins").hasChild(current_user_id)) {
                    String firstName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                    String lastName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                    accountPrivs = "Admin";
                    fullname = firstName + " " + lastName;
                } else {
                    Toast.makeText(getContext(), "User account not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(getContext(), Register.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);

        getActivity().finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            SendUserToLoginActivity();
        } else if (currentUser != null) {

            FirebaseRecyclerAdapter<CalendarModelClass, Events.PostViewHolder> FBRA = new FirebaseRecyclerAdapter<CalendarModelClass, Events.PostViewHolder>(
                    CalendarModelClass.class,
                    R.layout.pending_item_clinic,
                    Events.PostViewHolder.class,
                    mDatabase
            ) {
                @Override
                protected void populateViewHolder(Events.PostViewHolder viewHolder, CalendarModelClass model, int position) {
                    final String post_key = getRef(position).getKey().toString();

//                    if (getItemViewType(position) == R.layout.card_items) {

                    final CalendarModelClass upload = mKeys.get(position);
                    Log.e("What", model.getWhat());
                    Log.e("Where", model.getWhere());
                    viewHolder.setPostWhat(model.getWhat());
                    viewHolder.setPostWhere("Place: " + model.getWhere());
                    viewHolder.setPostWhen("Date "+model.getDate()+" Time: " + model.getStartTime() + model.getEndTime());
                    viewHolder.setClinicName(model.getHeldBy());
                    viewHolder.setClinicDate(model.getPostDate());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getContext(), PostSummary.class);
                            i.putExtra("postId", upload.getmKey());
                            startActivity(i);
                        }
                    });

                }

            };
            recyclerView.setAdapter(FBRA);
        } else

        {
            checkUserExistence();
        }

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView post_what, post_where, post_when, post_clinicname, post_clinicdate;
        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            post_what = mView.findViewById(R.id.post_what);
            post_where = mView.findViewById(R.id.post_where);
            post_when = mView.findViewById(R.id.post_when);
            post_clinicname = mView.findViewById(R.id.post_clinicname);
            post_clinicdate = mView.findViewById(R.id.post_clinicdate);
            mAuth = FirebaseAuth.getInstance();
        }

        public void setProfilePic(final Context ctx, final String userId) {
            DatabaseReference mUsers = FirebaseDatabase.getInstance().getReference("Users");

            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(userId)) {
                        String imageUrl = dataSnapshot.child("Students").child(userId).child("ImageUrl").getValue().toString();

                        CircleImageView profilePic = mView.findViewById(R.id.profile_pic);
                        Picasso.with(ctx).load(imageUrl).into(profilePic);
                    } else if (dataSnapshot.child("Admins").hasChild(userId)) {
                        String imageUrl = dataSnapshot.child("Admins").child(userId).child("ImageUrl").getValue().toString();

                        CircleImageView profilePic = mView.findViewById(R.id.profile_pic);
                        Picasso.with(ctx).load(imageUrl).into(profilePic);
                    } else {
                        Toast.makeText(ctx, "User account not found", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setPostWhat(String what) {
            post_what.setText(what);
        }

        public void setPostWhere(String where) {
            post_where.setText(where);
        }
        public void setPostWhen(String when) {
            post_when.setText(when);
        }
        public void setClinicName(String name) {
            post_clinicname.setText(name);
        }
        public void setClinicDate(String date) {
            post_clinicdate.setText(date);
        }

    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {

    }

}
