package hci.com.tentativecapstoneui;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import hci.com.tentativecapstoneui.model.Chat;
import hci.com.tentativecapstoneui.model.Message;
import hci.com.tentativecapstoneui.model.User;
import hci.com.tentativecapstoneui.ui.ChatActivity;
import hci.com.tentativecapstoneui.ui.ChatMessagesActivity;
import hci.com.tentativecapstoneui.ui.FriendsListActivity;
import hci.com.tentativecapstoneui.utils.Constants;
import hci.com.tentativecapstoneui.utils.EmailEncoding;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class Three extends android.support.v4.app.Fragment {

    private static final String TAG = "Three";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ListView mChatListView;
    private FirebaseListAdapter mChatAdapter;
    private String mUsername;
    private ValueEventListener mValueEventListener;
    private DatabaseReference mUserDatabaseReference;
    private ImageView addConversationButton;

    private EditText mSearchField;
    private ImageButton mFriendBtn;


    public Three() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_chat_main, container, false);

    }

    public void onStart() {
        super.onStart();

//        Toolbar toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

//        setHasOptionsMenu(true);

        mSearchField = getActivity().findViewById(R.id.search_field);
        mFriendBtn = getActivity().findViewById(R.id.friend_btn);

        mFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String searchText = mSearchField.getText().toString();
//                firebaseUserSearch(searchText);

                Intent intent = new Intent(getActivity(),FriendsListActivity.class);
                startActivity(intent);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Nav to ChatListActivity
                    createUser(user);
                    onSignedInInitialize(user);
                } else {
                    // User is signed out
                    //onSignedOutCleanup();
                    Intent signIn = new Intent(getActivity(),Login.class);
                    startActivity(signIn);
                }
            }
        };

    }

//    private void firebaseUserSearch(String searchText) {
//
//        Toast.makeText(getActivity(),"Searching...", Toast.LENGTH_LONG).show();
//
//        Query firebaseSearchQuery = mFirebaseDatabase.getReference(Constants.CHAT_LOCATION).startAt(searchText).endAt(searchText + "\uf8ff");
//
//        mChatAdapter = new FirebaseListAdapter<User>(getActivity(), User.class, R.layout.content_chat_main, mUserDatabaseReference)
//        {
//            @Override
//            protected void populateView(final View view, User user, final int position) {
//
//                mFirebaseDatabase.getReference(Constants.CHAT_LOCATION
//                        + "/" + user.getEmail());
//
//            }
//        };
//
//        mChatListView.setAdapter(mChatAdapter);
//    }

    private void hideShowAddChatButton(FirebaseUser user) {
        addConversationButton = (ImageView) getActivity().findViewById(R.id.add_conversation);
        final String userLoggedIn = user.getEmail();
        final DatabaseReference friendsCheckRef = mFirebaseDatabase.getReference(Constants.FRIENDS_LOCATION
                + "/" + EmailEncoding.commaEncodePeriod(userLoggedIn));
        friendsCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                String strLong = Long.toString(size);
                if (size > 0) {
                    addConversationButton.setVisibility(View.VISIBLE);
                } else {
                    addConversationButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onSignedInInitialize(FirebaseUser user) {
        mUsername = user.getDisplayName();
        mChatDatabaseReference = mFirebaseDatabase.getReference()
                .child(Constants.USERS_LOCATION
                        + "/" + EmailEncoding.commaEncodePeriod(user.getEmail()) + "/"
                        + Constants.CHAT_LOCATION );
        mUserDatabaseReference = mFirebaseDatabase.getReference()
                .child(Constants.USERS_LOCATION);

        hideShowAddChatButton(user);

        //Initialize screen variables
        mChatListView = getActivity().findViewById(R.id.chatListView);

        mChatAdapter = new FirebaseListAdapter<Chat>(getActivity(), Chat.class, R.layout.chat_item, mChatDatabaseReference) {
            @Override
            protected void populateView(final View view, Chat chat, final int position) {
                //Log.e("TAG", "");
                //final Friend addFriend = new Friend(chat);
                ((TextView) view.findViewById(R.id.messageTextView)).setText(chat.getChatName());

                //Fetch last message from chat
                final DatabaseReference messageRef =
                        mFirebaseDatabase.getReference(Constants.MESSAGE_LOCATION
                                + "/" + chat.getUid());

                final TextView latestMessage = view.findViewById(R.id.nameTextView);
                final ImageView senderPic = view.findViewById(R.id.photoImageView);

                messageRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                        Message newMsg = dataSnapshot.getValue(Message.class);
                        latestMessage.setText(EmailEncoding.commaDecodePeriod(newMsg.getSender()) + ": " + newMsg.getMessage());

                        mUserDatabaseReference.child(newMsg.getSender())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User msgSender = dataSnapshot.getValue(User.class);
                                        if(msgSender != null && msgSender.getImageUrl() != null){
                                            StorageReference storageRef = FirebaseStorage.getInstance()
                                                    .getReference().child(msgSender.getImageUrl());
                                            Glide.with(view.getContext())
                                                    .using(new FirebaseImageLoader())
                                                    .load(storageRef)
                                                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                                                    .into(senderPic);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                //Replace this with the most recent message from the chat

            }
        };



        mChatListView.setAdapter(mChatAdapter);
        //Add on click listener to line items
        mChatListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String messageLocation = mChatAdapter.getRef(position).toString();

                if(messageLocation != null){
                    Intent intent = new Intent(view.getContext(), ChatMessagesActivity.class);
                    String messageKey = mChatAdapter.getRef(position).getKey();
                    intent.putExtra(Constants.MESSAGE_ID, messageKey);
                    Chat chatItem = (Chat)mChatAdapter.getItem(position);
                    intent.putExtra(Constants.CHAT_NAME, chatItem.getChatName());
                    startActivity(intent);

                }

                //Log.e("TAG", mChatAdapter.getRef(position).toString());
            }
        });

        mValueEventListener = mChatDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                //Check if any chats exists
                if (chat == null) {
                    //finish();
                    return;
                }
                mChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Sign in canceled", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else if (resultCode == RESULT_OK) {

        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
////        if (id == R.id.sign_out) {
////            AuthUI.getInstance()
////                    .signOut(this);
////        }
//        if (id == R.id.listFriends) {
//            //Open up activity where a user can add and view friends
//            Intent intent = new Intent(getActivity(), FriendsListActivity.class);
//            startActivity(intent);
//        }

//        if (id == R.id.profilePage) {
//            //Open up activity where a user can add and view friends
//            Intent intent = new Intent(this, ProfileActivity.class);
//            startActivity(intent);
//        }

//        return true;
//    }

    private void createUser(FirebaseUser user) {
        final DatabaseReference usersRef = mFirebaseDatabase.getReference(Constants.USERS_LOCATION);
        final String encodedEmail = EmailEncoding.commaEncodePeriod(user.getEmail());
        final DatabaseReference userRef = usersRef.child(encodedEmail);
        final String username = user.getDisplayName();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    User newUser = new User(username, encodedEmail);
                    userRef.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
