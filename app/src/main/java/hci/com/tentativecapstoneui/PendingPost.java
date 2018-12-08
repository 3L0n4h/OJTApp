package hci.com.tentativecapstoneui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PendingPost extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabase, mUsers, mDatabaseLikes, mDatabaseComments, myRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button postBtn;
    private Boolean mProcessLike = false;
    private FirebaseDatabase database;
    private String Post_Key;
    private String fullname;
    private NavigationView navigationView;
    private ImageView NavProfileImage;
    private TextView navProfileName;
    private String post_key;
    private String key;
    private List<Post> mKeys;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(PendingPost.this));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PendingPost.this);
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

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post").child("Pending");
        mDatabase.keepSynced(true);

        myRef = FirebaseDatabase.getInstance().getReference().child("Post").child("Accepted");
        myRef.keepSynced(true);


        mAuth = FirebaseAuth.getInstance();
//        currentUserId = mAuth.getCurrentUser().getUid();

        mDBListener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mKeys.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post upload = postSnapshot.getValue(Post.class);
                    upload.setmKey(postSnapshot.getKey());
                    mKeys.add(upload);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PendingPost.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(PendingPost.this, Register.class);
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
                        accountPrivs = "Student";
                        fullname = firstName + " " + lastName;
                    } else if (dataSnapshot.child("Admins").hasChild(mAuth.getCurrentUser().getUid())) {
                        String firstName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("FirstName").getValue().toString();
                        String lastName = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("LastName").getValue().toString();
                        accountPrivs = "Admin";
                        fullname = firstName + " " + lastName;
                    } else {
                        Toast.makeText(PendingPost.this, "User account not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(PendingPost.this, Login.class);
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
        Intent setupIntent = new Intent(PendingPost.this, Register.class);
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
//        mAuth.addAuthStateListener(mAuthListener);
        else if (currentUser != null) {

            // SendUserToLoginActivity();
            FirebaseRecyclerAdapter<Post, PendingPost.PostViewHolder> FBRA = new FirebaseRecyclerAdapter<Post, PendingPost.PostViewHolder>(
                    Post.class,
                    R.layout.pending_items,
                    PendingPost.PostViewHolder.class,
                    mDatabase
            ) {
                @Override
                protected void populateViewHolder(PendingPost.PostViewHolder viewHolder, final Post model, final int position) {
                    post_key = getRef(position).getKey().toString();

                    if (getItemViewType(position) == R.layout.pending_items) {

                        viewHolder.setPostTitle(model.getPostTitle());
                        viewHolder.setPostDescription(model.getPostDescription());
                        viewHolder.setImageUrl(PendingPost.this, model.getImageUrl());
                        viewHolder.setUserName(model.getUserName());
                        viewHolder.setPostDate(model.getPostDate());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Post upload = mKeys.get(position);

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PendingPost.this);
                                alertDialogBuilder.setMessage("Approve post?");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(upload.getmKey())) {
                                                            String postDate = dataSnapshot.child(upload.getmKey()).child("postDate").getValue().toString();
                                                            String postDescription = dataSnapshot.child(upload.getmKey()).child("postDescription").getValue().toString();
                                                            String postTitle = dataSnapshot.child(upload.getmKey()).child("postTitle").getValue().toString();
                                                            String userName = dataSnapshot.child(upload.getmKey()).child("userName").getValue().toString();
                                                            String imageUrl = dataSnapshot.child(upload.getmKey()).child("imageUrl").getValue().toString();
                                                            String fileLink = dataSnapshot.child(upload.getmKey()).child("fileLink").getValue().toString();
                                                            String userid = dataSnapshot.child(upload.getmKey()).child("postUid").getValue().toString();

                                                            if (dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString() != null) {
                                                                String Filename = dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString();

                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, Filename, fileLink)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            } else {
                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, "", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    } else if (getItemViewType(position) == R.layout.pending_item_alert) {

                        final Post upload = mKeys.get(position);

                        viewHolder.setPostTitle2(model.getPostTitle());
                        viewHolder.setPostDescription2(model.getPostDescription());
                        viewHolder.setUserName2(model.getUserName());
                        viewHolder.setPostDate2(model.getPostDate());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final DatabaseReference newPost = myRef.push();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PendingPost.this);
                                alertDialogBuilder.setMessage("Approve post?");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(upload.getmKey())) {
                                                            String postDate = dataSnapshot.child(upload.getmKey()).child("postDate").getValue().toString();
                                                            String postDescription = dataSnapshot.child(upload.getmKey()).child("postDescription").getValue().toString();
                                                            String postTitle = dataSnapshot.child(upload.getmKey()).child("postTitle").getValue().toString();
                                                            String userName = dataSnapshot.child(upload.getmKey()).child("userName").getValue().toString();
                                                            String imageUrl = dataSnapshot.child(upload.getmKey()).child("imageUrl").getValue().toString();
                                                            String fileLink = dataSnapshot.child(upload.getmKey()).child("fileLink").getValue().toString();
                                                            String userid = dataSnapshot.child(upload.getmKey()).child("postUid").getValue().toString();

                                                            if (dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString() != null) {
                                                                String Filename = dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString();

                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, Filename, fileLink)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            } else {
                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, "", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
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


                    } else if (getItemViewType(position) == R.layout.pending_item_wfile) {

                        final Post upload = mKeys.get(position);

                        viewHolder.setFileName(model.getPostNameFile());
                        viewHolder.setFileTitle(model.getPostTitle());
                        viewHolder.setFileDesc(model.getPostDescription());
                        viewHolder.setFilePostName(model.getUserName());
                        viewHolder.setFileTime(model.getPostDate());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final DatabaseReference newPost = myRef.push();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PendingPost.this);
                                alertDialogBuilder.setMessage("Approve post?");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(upload.getmKey())) {
                                                            String postDate = dataSnapshot.child(upload.getmKey()).child("postDate").getValue().toString();
                                                            String postDescription = dataSnapshot.child(upload.getmKey()).child("postDescription").getValue().toString();
                                                            String postTitle = dataSnapshot.child(upload.getmKey()).child("postTitle").getValue().toString();
                                                            String userName = dataSnapshot.child(upload.getmKey()).child("userName").getValue().toString();
                                                            String imageUrl = dataSnapshot.child(upload.getmKey()).child("imageUrl").getValue().toString();
                                                            String fileLink = dataSnapshot.child(upload.getmKey()).child("fileLink").getValue().toString();
                                                            String userid = dataSnapshot.child(upload.getmKey()).child("postUid").getValue().toString();

                                                            if (dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString() != null) {
                                                                String Filename = dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString();

                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, Filename, fileLink)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            } else {
                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, "", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    } else if (getItemViewType(position) == R.layout.pending_item_wimgfile) {

                        final Post upload = mKeys.get(position);

                        viewHolder.setFileName(model.getPostNameFile());
                        viewHolder.setFileTitle(model.getPostTitle());
                        viewHolder.setFileDesc(model.getPostDescription());
                        viewHolder.setFilePostName(model.getUserName());
                        viewHolder.setFileTime(model.getPostDate());
                        viewHolder.setImageUrl(PendingPost.this, model.getImageUrl());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final DatabaseReference newPost = myRef.push();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PendingPost.this);
                                alertDialogBuilder.setMessage("Approve post?");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(upload.getmKey())) {
                                                            String postDate = dataSnapshot.child(upload.getmKey()).child("postDate").getValue().toString();
                                                            String postDescription = dataSnapshot.child(upload.getmKey()).child("postDescription").getValue().toString();
                                                            String postTitle = dataSnapshot.child(upload.getmKey()).child("postTitle").getValue().toString();
                                                            String userName = dataSnapshot.child(upload.getmKey()).child("userName").getValue().toString();
                                                            String imageUrl = dataSnapshot.child(upload.getmKey()).child("imageUrl").getValue().toString();
                                                            String fileLink = dataSnapshot.child(upload.getmKey()).child("fileLink").getValue().toString();
                                                            String userid = dataSnapshot.child(upload.getmKey()).child("postUid").getValue().toString();

                                                            if (dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString() != null) {
                                                                String Filename = dataSnapshot.child(upload.getmKey()).child("postNameFile").getValue().toString();

                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, Filename, fileLink)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDatabase.child(upload.getmKey()).removeValue();
                                                                    }
                                                                });
                                                            } else {
                                                                myRef.push().setValue(new Post(postTitle, postDescription, imageUrl, userid, postDate, userName, "", "")).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    //COMMENT

                }//end populate view holder
//                    KAPAG WALANG IMAGE CONDITION

                @Override
                public int getItemViewType(int position) {
                    Post model = getItem(position);
                    if (model.getImageUrl().contentEquals("") && model.getPostNameFile().contentEquals("")) {
                        // Layout for an item with an image
                        return R.layout.pending_item_alert;
                    } else if (!model.getImageUrl().contentEquals("") && model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.pending_items;
                    } else if (model.getImageUrl().contentEquals("") && !model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.pending_item_wfile;
                    } else if (!model.getImageUrl().contentEquals("") && !model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.pending_item_wimgfile;
                    } else {
                        Toast.makeText(PendingPost.this, "Oof", Toast.LENGTH_SHORT).show();
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
        ImageButton mLikeButton, CommentPostButton;
        ImageButton mLikeButton2, CommentPostButton2;
        TextView DisplayNoOfLikes;
        TextView DisplayNoOfLikes2;
        TextView txtFileName;
        int countLikes, countComments;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        String CurrentUserId, Post_key;


        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            txtFileName = mView.findViewById(R.id.txtfile);


            mAuth = FirebaseAuth.getInstance();
        }


        public void setPostTitle(String postTitle) {
            TextView post_title = mView.findViewById(R.id.post_title_txtview3);
            post_title.setText(postTitle);
        }

        public void setPostDescription(String postDescription) {
            TextView post_desc = mView.findViewById(R.id.post_desc_txtview3);
            post_desc.setText(postDescription);
        }

        public void setImageUrl(Context ctx, String imageUrl) {
            ImageView post_image = mView.findViewById(R.id.post_image3);
//            Picasso.with(ctx).load(imageUrl).into(post_image);
            Glide.with(ctx).load(imageUrl).fitCenter().crossFade().into(post_image);
        }

        public void setUserName(String username) {
            TextView post_user = mView.findViewById(R.id.post_user3);
            post_user.setText(username);
        }

        public void setPostDate(String postDate) {
            TextView post_Date = mView.findViewById(R.id.timestamp3);
            post_Date.setText(postDate);
        }

        public void setPostTitle2(String postTitle) {
            TextView post_title = mView.findViewById(R.id.title4);
            post_title.setText(postTitle);
        }

        public void setPostDescription2(String postDescription) {
            TextView post_desc = mView.findViewById(R.id.desc4);
            post_desc.setText(postDescription);
        }

        public void setUserName2(String username) {
            TextView post_user = mView.findViewById(R.id.name4);
            post_user.setText(username);
        }

        public void setPostDate2(String postDate) {
            TextView post_Date = mView.findViewById(R.id.time4);
            post_Date.setText(postDate);
        }

        public void setFileName(String fileName) {
            TextView post_file = mView.findViewById(R.id.txtfile);
            post_file.setText(fileName);
        }

        public void setFilePostName(String fileName) {
            TextView post_file = mView.findViewById(R.id.name5);
            post_file.setText(fileName);
        }

        public void setFileTime(String fileTime) {
            TextView post_file = mView.findViewById(R.id.time5);
            post_file.setText(fileTime);
        }

        public void setFileTitle(String fileTitle) {
            TextView post_file = mView.findViewById(R.id.title5);
            post_file.setText(fileTitle);
        }

        public void setFileDesc(String fileDesc) {
            TextView post_file = mView.findViewById(R.id.desc5);
            post_file.setText(fileDesc);
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