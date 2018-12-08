package hci.com.tentativecapstoneui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hci.com.tentativecapstoneui.model.CalendarModelClass;

public class PendingCalendarPost extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabase, mUsers, myRef, mClinicDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button postBtn;
    private FirebaseDatabase database;
    private String Post_Key, fullname;
    private NavigationView navigationView;
    private ImageView NavProfileImage;
    private TextView navProfileName;
    private String post_key, key;
    private List<CalendarModelClass> mKeys;
    private ValueEventListener mDBListener;
    private String accountPrivs = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_post);

        mKeys = new ArrayList<>();

        postBtn = findViewById(R.id.postBtn);
        //initialize recyclerview and FIrebase objects
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(PendingCalendarPost.this));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PendingCalendarPost.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //RETRIEVE THE PRE-APPROVE

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post").child("Events");
        mDatabase.keepSynced(true);

        myRef = FirebaseDatabase.getInstance().getReference().child("Post").child("Events").child("Pending");
        myRef.keepSynced(true);

//        mClinicDatabase = FirebaseDatabase.getInstance().getReference().child("Post").child("Events");
//        mClinicDatabase.keepSynced(true);

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
                Toast.makeText(PendingCalendarPost.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(PendingCalendarPost.this, Register.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }
            }
        };
        if (mAuth.getCurrentUser() != null) {
            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        accountPrivs = "Student";
                        fullname = firstName + " " + lastName;
                    } else if (dataSnapshot.child("Admins").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        accountPrivs = "Admin";
                        fullname = firstName + " " + lastName;
                    } else {
                        Toast.makeText(PendingCalendarPost.this, "User account not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(PendingCalendarPost.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

        finish();
    }

    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(current_user_id)) {
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(PendingCalendarPost.this, Register.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);

        finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            SendUserToLoginActivity();
        }
        else if (currentUser != null) {

            FirebaseRecyclerAdapter<CalendarModelClass, PendingCalendarPost.PostViewHolder> FBRA = new FirebaseRecyclerAdapter<CalendarModelClass, PendingCalendarPost.PostViewHolder>(
                    CalendarModelClass.class,
                    R.layout.pending_items,
                    PendingCalendarPost.PostViewHolder.class,
                    mDatabase
            ) {
                @Override
                protected void populateViewHolder(PendingCalendarPost.PostViewHolder viewHolder, final CalendarModelClass model, final int position) {
                    post_key = getRef(position).getKey().toString();


                    //layout for pending clinic post
                    if(getItemViewType(position)==R.layout.pending_item_clinic){
                        final CalendarModelClass upload = mKeys.get(position);

                        viewHolder.setPostWhat(model.getWhat());
//                        viewHolder.setPostWhen(model.getWhen());
                        viewHolder.setPostWhere(model.getWhere());
                        viewHolder.setUsername(model.getUserName());
                        viewHolder.setPostDate(model.getPostDate());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final DatabaseReference newPost = myRef.push();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PendingCalendarPost.this);
                                alertDialogBuilder.setMessage("Approve post?");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(upload.getmKey())) {
                                                            String postWhat = dataSnapshot.child(upload.getmKey()).child("postWhat").getValue().toString();
                                                            String postWhen = dataSnapshot.child(upload.getmKey()).child("postWhen").getValue().toString();
                                                            String postWhere = dataSnapshot.child(upload.getmKey()).child("postWhere").getValue().toString();
                                                            String userName = dataSnapshot.child(upload.getmKey()).child("userName").getValue().toString();
                                                            String postDate = dataSnapshot.child(upload.getmKey()).child("postDate").getValue().toString();
                                                            String userId = mAuth.getCurrentUser().getUid();
                                                            if (dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString() != null) {
                                                                String Filename = dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString();

                                                                myRef.push().setValue(new CalendarModelClass( postWhat,  postWhere,  postWhen,  userName,  postDate,  userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            } else {
                                                                myRef.push().setValue(new CalendarModelClass( postWhat,  postWhere,  postWhen,  userName,  postDate,  userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                            }
                                        });

                                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDatabase.child(upload.getmKey()).removeValue();
                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                            }
                        });

                    }

                }//end populate view holder
//                    KAPAG WALANG IMAGE CONDITION

                @Override
                public int getItemViewType(int position) {
                    CalendarModelClass model = getItem(position);
                    if(!model.getWhat().contentEquals("")){
                        //Layout for clinic and pysch
                        return R.layout.pending_item_clinic;
                    }else {
                        Toast.makeText(PendingCalendarPost.this, "Oof", Toast.LENGTH_SHORT).show();
                    }
                    return position;
                }

            };
            recyclerView.setAdapter(FBRA);
        } else {
            checkUserExistence();
        }
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView txtFileName;
        FirebaseAuth mAuth;
        String CurrentUserId, Post_key;


        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            txtFileName = mView.findViewById(R.id.txtfile);

            mAuth = FirebaseAuth.getInstance();
        }


        public void setPostWhat(String postWhat) {
            TextView post_what = mView.findViewById(R.id.post_what);
            post_what.setText(postWhat);
        }
        public void setPostWhen(String postWhen) {
            TextView post_what = mView.findViewById(R.id.post_what);
            post_what.setText(postWhen);
        }
        public void setPostWhere(String postWhere) {
            TextView post_where = mView.findViewById(R.id.post_where);
            post_where.setText(postWhere);
        }
        public void setUsername(String username) {
            TextView userName = mView.findViewById(R.id.post_clinicname);
            userName.setText(username);
        }
        public void setPostDate(String date) {
            TextView mydate = mView.findViewById(R.id.post_clinicdate);
            mydate.setText(date);
        }



    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(mDBListener);
    }
}