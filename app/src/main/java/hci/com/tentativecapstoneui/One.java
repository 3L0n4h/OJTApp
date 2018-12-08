package hci.com.tentativecapstoneui;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class One extends android.support.v4.app.Fragment implements ImageAdapter.OnItemClickListener {

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
    private List<Post> mKeys;

    android.support.v4.app.Fragment fragment = null;

    public One() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_one, container, false);

        mKeys = new ArrayList<>();

        //initialize recyclerview and FIrebase objects
        recyclerView = rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //RETRIEVE THE PRE-APPROVE
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);

//        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
//        mDatabase.keepSynced(true);

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post").child("Accepted");
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
        }
//        mAuth.addAuthStateListener(mAuthListener);
        else if (currentUser != null) {

            // SendUserToLoginActivity();
            FirebaseRecyclerAdapter<Post, One.PostViewHolder> FBRA = new FirebaseRecyclerAdapter<Post, One.PostViewHolder>(
                    Post.class,
                    R.layout.card_items,
                    One.PostViewHolder.class,
                    mDatabase
            ) {
                @Override
                protected void populateViewHolder(One.PostViewHolder viewHolder, Post model, int position) {
                    final String post_key = getRef(position).getKey().toString();

                    if (getItemViewType(position) == R.layout.card_items) {

                        final Post upload = mKeys.get(position);
                        viewHolder.setPostTitle(model.getPostTitle());
                        viewHolder.setProfilePic(getContext(), model.getPostUid());
                        viewHolder.setPostDescription(model.getPostDescription());
                        viewHolder.setImageUrl(getContext(), model.getImageUrl());
                        viewHolder.setUserName(model.getUserName());
                        viewHolder.setPostDate(model.getPostDate());
                        viewHolder.setLikeButton(post_key);
//                        viewHolder.setFullname(model.getFullname());
//                        viewHolder.setCommentPostButton(post_key);
                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsIntent = new Intent(getContext(), CommentActivity.class);
                                commentsIntent.putExtra("PostKey", post_key);
                                startActivity(commentsIntent);

                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), PostSummary.class);
                                i.putExtra("postId", upload.getmKey());
                                startActivity(i);
                            }
                        });


                        //LIKE
                        viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mProcessLike = true;

                                mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mProcessLike) {
                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mProcessLike = false;

                                            } else {
                                                //to be changed

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(fullname);
//                                            mDatabaseLikes.child("Fullname").getValue().toString();

                                                mProcessLike = false;

                                            }
                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }

                        });

                    } else if (getItemViewType(position) == R.layout.card_item_alert) {

                        final Post upload = mKeys.get(position);

                        viewHolder.setPostTitle2(model.getPostTitle());
                        viewHolder.setProfilePic(getContext(), model.getPostUid());
                        viewHolder.setPostDescription2(model.getPostDescription());
                        viewHolder.setUserName2(model.getUserName());
                        viewHolder.setPostDate2(model.getPostDate());
                        viewHolder.setLikeButton2(post_key);

                        viewHolder.CommentPostButton2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsIntent = new Intent(getContext(), CommentActivity.class);
                                commentsIntent.putExtra("PostKey", post_key);
                                startActivity(commentsIntent);

                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), PostSummary.class);
                                i.putExtra("postId", upload.getmKey());
                                startActivity(i);
                            }
                        });


                        //LIKE
                        viewHolder.mLikeButton2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mProcessLike = true;

                                mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mProcessLike) {
                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mProcessLike = false;

                                            } else {
                                                //to be changed

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(fullname);
//                                            mDatabaseLikes.child("Fullname").getValue().toString();

                                                mProcessLike = false;

                                            }
                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }

                        });
                    } else if (getItemViewType(position) == R.layout.card_item_wfile) {

                        final Post upload = mKeys.get(position);
                        viewHolder.setProfilePic(getContext(), model.getPostUid());
                        viewHolder.setFileName(model.getPostNameFile());
                        viewHolder.setFileTitle(model.getPostTitle());
                        viewHolder.setFileDesc(model.getPostDescription());
                        viewHolder.setFilePostName(model.getUserName());
                        viewHolder.setFileTime(model.getPostDate());
                        viewHolder.setLikeButton3(post_key);

                        viewHolder.CommentPostButton3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsIntent = new Intent(getActivity(), CommentActivity.class);
                                commentsIntent.putExtra("PostKey", post_key);
                                startActivity(commentsIntent);

                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), PostSummary.class);
                                i.putExtra("postId", upload.getmKey());
                                startActivity(i);
                            }
                        });

                        //LIKE
                        viewHolder.mLikeButton3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mProcessLike = true;

                                mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mProcessLike) {
                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mProcessLike = false;

                                            } else {
                                                //to be changed

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(fullname);
//                                            mDatabaseLikes.child("Fullname").getValue().toString();

                                                mProcessLike = false;

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }

                        });
                    }else if (getItemViewType(position) == R.layout.card_item_wimgfile) {

                        final Post upload = mKeys.get(position);
                        viewHolder.setProfilePic(getContext(), model.getPostUid());
                        viewHolder.setFileName(model.getPostNameFile());
                        viewHolder.setFileTitle(model.getPostTitle());
                        viewHolder.setImageUrl(getContext(), model.getImageUrl());
                        viewHolder.setFileDesc(model.getPostDescription());
                        viewHolder.setFilePostName(model.getUserName());
                        viewHolder.setFileTime(model.getPostDate());
                        viewHolder.setLikeButton3(post_key);

                        viewHolder.CommentPostButton3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsIntent = new Intent(getActivity(), CommentActivity.class);
                                commentsIntent.putExtra("PostKey", post_key);
                                startActivity(commentsIntent);

                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), PostSummary.class);
                                i.putExtra("postId", upload.getmKey());
                                startActivity(i);
                            }
                        });

                        //LIKE
                        viewHolder.mLikeButton3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mProcessLike = true;

                                mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mProcessLike) {
                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                mProcessLike = false;

                                            } else {
                                                //to be changed

                                                mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(fullname);
//                                            mDatabaseLikes.child("Fullname").getValue().toString();

                                                mProcessLike = false;

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }

                        });
                    }
                }

                //COMMENT

                //end populate view holder
//                    KAPAG WALANG IMAGE CONDITION

                @Override
                public int getItemViewType(int position) {
                    Post model = getItem(position);
                    if (model.getImageUrl().contentEquals("") && model.getPostNameFile().contentEquals("")) {
                        // Layout for an item with an image
                        return R.layout.card_item_alert;
                    } else if (!model.getImageUrl().contentEquals("") && model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.card_items;
                    } else if (model.getImageUrl().contentEquals("") && !model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.card_item_wfile;
                    } else if (!model.getImageUrl().contentEquals("") && !model.getPostNameFile().contentEquals("")) {
                        // Layout for an item without an image
                        return R.layout.card_item_wimgfile;
                    } else {
                        Toast.makeText(getContext(), "Oof", Toast.LENGTH_SHORT).show();
                    }
                    return position;
                }

            };
            recyclerView.setAdapter(FBRA);
        } else

        {
            checkUserExistence();
        }
        addpost = getView().findViewById(R.id.add_post_btn);
        addpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent newPostIntent = new Intent(getActivity(), PostActivity.class);
                startActivity(newPostIntent);

            }
        });
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton mLikeButton, CommentPostButton;
        ImageButton mLikeButton2, CommentPostButton2;
        ImageButton mLikeButton3, CommentPostButton3;
        TextView DisplayNoOfLikes;
        TextView DisplayNoOfLikes2, DisplayNoOfLikes3;
        int countLikes, countComments;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        String CurrentUserId, Post_key;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeButton = mView.findViewById(R.id.like_btn);
            CommentPostButton = mView.findViewById(R.id.comment_btn);
            DisplayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);
//
            mLikeButton2 = mView.findViewById(R.id.like_btn2);
            CommentPostButton2 = mView.findViewById(R.id.comment_btn2);
            DisplayNoOfLikes2 = mView.findViewById(R.id.display_no_of_likes2);

            mLikeButton3 = mView.findViewById(R.id.like_btn_wfile);
            CommentPostButton3 = mView.findViewById(R.id.comment_btn_wfile);
            DisplayNoOfLikes3 = mView.findViewById(R.id.display_no_of_likes_wfile);

//            Post_key = getIntent().getExtras().get("PostKey").toString();

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);
        }


        public void setLikeButton(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton.setImageResource(R.mipmap.like_pink);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + " Like/s");


                    } else {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton.setImageResource(R.mipmap.like_black);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + " Like/s");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setLikeButton2(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton2.setImageResource(R.mipmap.like_pink);
                        DisplayNoOfLikes2.setText(Integer.toString(countLikes) + " Like/s");


                    } else {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton2.setImageResource(R.mipmap.like_black);
                        DisplayNoOfLikes2.setText(Integer.toString(countLikes) + " Like/s");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setLikeButton3(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton3.setImageResource(R.mipmap.like_pink);
                        DisplayNoOfLikes3.setText(Integer.toString(countLikes) + " Like/s");


                    } else {
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikeButton3.setImageResource(R.mipmap.like_black);
                        DisplayNoOfLikes3.setText(Integer.toString(countLikes) + " Like/s");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setPostTitle(String postTitle) {
            TextView post_title = mView.findViewById(R.id.post_title_txtview);
            post_title.setText(postTitle);
        }

        public void setPostDescription(String postDescription) {
            TextView post_desc = mView.findViewById(R.id.post_desc_txtview);
            post_desc.setText(postDescription);
        }

        public void setProfilePic(final Context ctx, final String userId) {
            DatabaseReference mUsers = FirebaseDatabase.getInstance().getReference("Users");

            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Students").hasChild(userId)) {
                        String imageUrl = dataSnapshot.child("Students").child(userId).child("ImageUrl").getValue().toString();

                        CircleImageView profilePic = mView.findViewById(R.id.profile_pic);
                        Glide.with(ctx).load(imageUrl).fitCenter().crossFade().into(profilePic);
//                        Picasso.with(ctx).load(imageUrl).into(profilePic);
                    } else if (dataSnapshot.child("Admins").hasChild(userId)) {
                        String imageUrl = dataSnapshot.child("Admins").child(userId).child("ImageUrl").getValue().toString();

                        CircleImageView profilePic = mView.findViewById(R.id.profile_pic);
                        Glide.with(ctx).load(imageUrl).fitCenter().crossFade().into(profilePic);
//                        Picasso.with(ctx).load(imageUrl).into(profilePic);
                    } else {
                        Toast.makeText(ctx, "User account not found", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setImageUrl(Context ctx, String imageUrl) {
            ImageView post_image = mView.findViewById(R.id.post_image);
            Glide.with(ctx).load(imageUrl).fitCenter().crossFade().into(post_image);
//            Picasso.with(ctx).load(imageUrl).into(post_image);
        }

        public void setUserName(String username) {
            TextView post_user = mView.findViewById(R.id.post_user);
            post_user.setText(username);
        }

        public void setPostDate(String postDate) {
            TextView post_Date = mView.findViewById(R.id.timestamp);
            post_Date.setText(postDate);
        }

        public void setPostTitle2(String postTitle) {
            TextView post_title = mView.findViewById(R.id.title);
            post_title.setText(postTitle);
        }

        public void setPostDescription2(String postDescription) {
            TextView post_desc = mView.findViewById(R.id.desc);
            post_desc.setText(postDescription);
        }

        public void setUserName2(String username) {
            TextView post_user = mView.findViewById(R.id.name);
            post_user.setText(username);
        }

        public void setPostDate2(String postDate) {
            TextView post_Date = mView.findViewById(R.id.time);
            post_Date.setText(postDate);
        }

        //nameOfFile2
        public void setFileName(String nameOfFile2) {
            TextView nameOfFile = mView.findViewById(R.id.nameOfFile2);
            nameOfFile.setText(nameOfFile2);
        }

//        public void setFileName(String fileName) {
//            TextView post_file = mView.findViewById(R.id.txtfile);
//            post_file.setText(fileName);
//        }

        public void setFilePostName(String fileName) {
            TextView post_file = mView.findViewById(R.id.name_wfile);
            post_file.setText(fileName);
        }

        public void setFileTime(String fileTime) {
            TextView post_file = mView.findViewById(R.id.time_wfile);
            post_file.setText(fileTime);
        }

        public void setFileTitle(String fileTitle) {
            TextView post_file = mView.findViewById(R.id.post_title_wfile);
            post_file.setText(fileTitle);
        }

        public void setFileDesc(String fileDesc) {
            TextView post_file = mView.findViewById(R.id.post_desc_wfile);
            post_file.setText(fileDesc);
        }
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {

    }

}

