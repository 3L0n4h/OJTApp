package hci.com.tentativecapstoneui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import bolts.Task;

public class PostSummary extends AppCompatActivity {

    ImageView summary_post_profilePic;
    ImageView summary_post_image;
    TextView summary_post_name, summary_post_timestamp;
    TextView summary_post_title_txtview;
    TextView summary_post_desc_txtview;
    TextView summary_file_name;

    LinearLayout summary_file_layout;

    FirebaseDatabase mDatabase;
    DatabaseReference myRef;
    DatabaseReference myUsers;

    Button summary_button_download;

    String poster_profile_uri;
    String downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_summary);

        summary_post_profilePic = findViewById(R.id.summary_post_profilePic);
        summary_post_image = findViewById(R.id.summary_post_image);
        summary_post_name = findViewById(R.id.summary_post_name);
        summary_post_timestamp = findViewById(R.id.summary_post_timestamp);
        summary_post_title_txtview = findViewById(R.id.summary_post_title_txtview);
        summary_post_desc_txtview = findViewById(R.id.summary_post_desc_txtview);
        summary_button_download = findViewById(R.id.summary_button_download);
        summary_file_layout = findViewById(R.id.summary_file_layout);
        summary_file_name = findViewById(R.id.summary_file_name);


        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference("Post").child("Accepted");
        myUsers = mDatabase.getReference("Users");

        final String post_id = getIntent().getExtras().getString("postId");
        myRef.child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String posterUid = dataSnapshot.child("postUid").getValue().toString();
                getProfilePic(posterUid);
                if(!dataSnapshot.child("imageUrl").getValue().toString().contentEquals("")){
                    Picasso.with(PostSummary.this).load(dataSnapshot.child("imageUrl").getValue().toString()).into(summary_post_image);
                }else{
                    summary_post_image.setVisibility(View.GONE);
                }
                summary_post_name.setText(dataSnapshot.child("userName").getValue().toString());
                summary_post_timestamp.setText(dataSnapshot.child("postDate").getValue().toString());
                summary_post_title_txtview.setText(dataSnapshot.child("postTitle").getValue().toString());
                summary_post_desc_txtview.setText(dataSnapshot.child("postDescription").getValue().toString());

                if(!dataSnapshot.child("fileLink").getValue().toString().contentEquals("")){
                    summary_file_name.setText(dataSnapshot.child("postNameFile").getValue().toString());
                    downloadUrl = dataSnapshot.child("fileLink").getValue().toString();

                    summary_button_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setType(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(downloadUrl));
                            startActivity(intent);
                        }
                    });

                }else{
                    summary_button_download.setVisibility(View.GONE);
                    summary_file_layout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        Toast.makeText(PostSummary.this, post_id, Toast.LENGTH_SHORT).show();
    }

    public void getProfilePic(final String posterUid){
        myUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Admins").hasChild(posterUid)){
                    poster_profile_uri = dataSnapshot.child("Admins").child(posterUid).child("ImageUrl").getValue().toString();

                    Picasso.with(PostSummary.this).load(poster_profile_uri).into(summary_post_profilePic);
                }else if(dataSnapshot.child("Students").hasChild(posterUid)){
                    poster_profile_uri = dataSnapshot.child("Students").child(posterUid).child("ImageUrl").getValue().toString();

                    Picasso.with(PostSummary.this).load(poster_profile_uri).into(summary_post_profilePic);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }
}
