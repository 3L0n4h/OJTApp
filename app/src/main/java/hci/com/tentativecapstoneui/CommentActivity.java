package hci.com.tentativecapstoneui;


import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;
    private TextView DisplayNoOfComments;
    private DatabaseReference UsersRef, databaseRef;
    private FirebaseAuth mAuth;
    private String Post_key, current_user_id;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Post_key = getIntent().getExtras().get("PostKey").toString();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //dito ata mali ng pasa ng postkey
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Post").child("Accepted").child(Post_key).child("Comments");


        CommentsList = findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        postCommentButton = findViewById(R.id.post_comment_btn);
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("Students").hasChild(current_user_id)){
                            String firstName = dataSnapshot.child("Students").child(current_user_id).child("FirstName").getValue().toString();
                            String lastName = dataSnapshot.child("Students").child(current_user_id).child("LastName").getValue().toString();
                            String userName = firstName + " " + lastName;
                            ValidateComent(userName);
                            CommentInputText.setText("");
                        }else if(dataSnapshot.child("Admins").hasChild(current_user_id)){
                            String firstName = dataSnapshot.child("Admins").child(current_user_id).child("FirstName").getValue().toString();
                            String lastName = dataSnapshot.child("Admins").child(current_user_id).child("LastName").getValue().toString();
                            String userName = firstName + " " + lastName;
                            ValidateComent(userName);
                            CommentInputText.setText("");
                        }

                        if (dataSnapshot.exists()) {


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        databaseRef
                ) {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position) {
                final String post_key = getRef(position).getKey().toString();


                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
//                viewHolder.setCommentPostButton(post_key);

            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        DatabaseReference mDatabaseComment;
        int countComments;
        TextView DisplayNoOfComments;
        FirebaseAuth mAuth;


        //        public void setCommentPostButton(final String post_key) {
//            mDatabaseComment.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
//                        countComments = (int) dataSnapshot.child(post_key).getChildrenCount();
//                        DisplayNoOfComments.setText(Integer.toString(countComments) + " Comments/s");
//
//
//                    } else {
//                        countComments = (int) dataSnapshot.child(post_key).getChildrenCount();
//                        DisplayNoOfComments.setText(Integer.toString(countComments) + " Comment/s");
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//        }
        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            DisplayNoOfComments = mView.findViewById(R.id.display_no_of_comments);

        }

        public void setUsername(String username) {
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + username + " ");

        }

        public void setComment(String comment) {

            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);

        }

        public void setDate(String date) {

            TextView myDate = mView.findViewById(R.id.comment_date);
            myDate.setText(date);
        }

        public void setTime(String time) {

            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText(time);

        }

    }

    private void ValidateComent(String userName) {

        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText)) {

            Toast.makeText(CommentActivity.this, "Please write text to comment...", Toast.LENGTH_SHORT).show();

        } else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);

            databaseRef.child(RandomKey).setValue(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(CommentActivity.this, "you have commented successfully...", Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(CommentActivity.this, "Try again...", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        }
    }
}
