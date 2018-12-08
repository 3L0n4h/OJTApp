package hci.com.tentativecapstoneui;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import android.Manifest;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.security.AccessController.getContext;

public class PostActivity extends AppCompatActivity {
    // imports
    private ImageButton imageBtn;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int GALLERY_REQUEST_CODE = 2;
    private Uri uri = null;
    private EditText textTitle, textDesc;
    private Button postBtn;
    private StorageReference storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef, databaseRef2, mDatabaseUsers, primaryDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String userPrivs;
    private Boolean answer = false;
    String phone, message;
    TextView txt_filename;
    Button upload, selectFile, fileUpload;
    //    ProgressDialog progressDialog;
    Uri pdfUri = null;
    String file;
    FirebaseStorage firebaseStorage;


    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        // initializing objects
        postBtn = findViewById(R.id.postBtn);
        textDesc = findViewById(R.id.textDesc);
        textTitle = findViewById(R.id.textTitle);
        txt_filename = findViewById(R.id.txt_filename);
        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance().getReference();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Post").child("Pending");
        databaseRef2 = FirebaseDatabase.getInstance().getReference().child("Post").child("Accepted");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        primaryDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        fileUpload = findViewById(R.id.txt_fileUpload);
        txt_filename = findViewById(R.id.txt_filename);
        firebaseStorage = FirebaseStorage.getInstance();

        primaryDatabase.child("Admins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                    userPrivs = "Admins";
                    mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(userPrivs).child(mCurrentUser.getUid());

                } else {
                    primaryDatabase.child("Students").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                userPrivs = "Students";
                                mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(userPrivs).child(mCurrentUser.getUid());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        imageBtn = findViewById(R.id.imageBtn);

        View view_instance = findViewById(R.id.imageBtn);
        ViewGroup.LayoutParams params = view_instance.getLayoutParams();

        if (uri == null) {

            params.width = 300;
            params.height = 300;
            view_instance.setLayoutParams(params);
        }
        //picking image from gallery
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });
        fileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }

            }
        });


        // posting to Firebase
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String PostTitle = textTitle.getText().toString().trim();
                String PostDesc = textDesc.getText().toString().trim();


                List<String> words = Arrays.asList("Fuck", "FUCK", "fuck", "SHIT", "Shit", "shit", "Bitch", "bitch", "BITCH", "Btch",
                        "TANGINA", "TANGNA", "POTA", "PUTA", "puta", "putangina", "shet", "sht");
                for (String word : words) {
                    Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
                    PostDesc = rx.matcher(PostDesc).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
                }
                final String PostDesc1 = PostDesc;
                // do a check for empty fields

                Toast.makeText(PostActivity.this, "Posting...", Toast.LENGTH_LONG).show();


                if (!TextUtils.isEmpty(PostDesc) && !TextUtils.isEmpty(PostTitle)) {
                    if (uri != null) {
                        DatabaseReference check = FirebaseDatabase.getInstance().getReference("Users").child("Admins");
                        check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                    StorageReference filepath = storage.child("post_images").child(uri.getLastPathSegment());
                                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            if (taskSnapshot != null) {
                                            @SuppressWarnings("VisibleForTests")
                                            //getting the post image download url
                                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            final DatabaseReference newPost = databaseRef2.push();
                                            final String refId = newPost.getKey();

                                            //adding post contents to database reference
                                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
                                                    Date date = new Date();
                                                    String strDate = dateFormat.format(date).toString();

                                                    String firstName = dataSnapshot.child("FirstName").getValue().toString();
                                                    String lastName = dataSnapshot.child("LastName").getValue().toString();
                                                    String userName = firstName + " " + lastName;
                                                    if (pdfUri != null) {
                                                        uploadFile(refId);
                                                        String fileName = txt_filename.getText().toString();
                                                        String[] fileName2 = fileName.split(": ");
                                                        String postNameFile = fileName2[1];

                                                        String fileLink = getRealPathFromURI(pdfUri);

                                                        newPost.setValue(new Post(PostTitle, PostDesc1, downloadUrl.toString(), mCurrentUser.getUid(), strDate, userName, postNameFile, fileLink)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);

                                                            }
                                                        });
                                                    } else {

                                                        newPost.setValue(new Post(PostTitle, PostDesc1, downloadUrl.toString(), mCurrentUser.getUid(), strDate, userName, "", "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);

                                                            }
                                                        });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    });

                                } else {
                                    StorageReference filepath = storage.child("post_images").child(uri.getLastPathSegment());
                                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            if (taskSnapshot != null) {
                                            @SuppressWarnings("VisibleForTests")
                                            //getting the post image download url
                                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            final DatabaseReference newPost = databaseRef.push();
                                            final String refId = newPost.getKey();

                                            //adding post contents to database reference
                                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
                                                    Date date = new Date();
                                                    String strDate = dateFormat.format(date).toString();

                                                    String firstName = dataSnapshot.child("FirstName").getValue().toString();
                                                    String lastName = dataSnapshot.child("LastName").getValue().toString();
                                                    String userName = firstName + " " + lastName;
                                                    if (pdfUri != null) {
                                                        uploadFile(refId);
                                                        String fileName = txt_filename.getText().toString();
                                                        String[] fileName2 = fileName.split(": ");
                                                        String postNameFile = fileName2[1];

                                                        String fileLink = getRealPathFromURI(pdfUri);

                                                        newPost.setValue(new Post(PostTitle, PostDesc1, downloadUrl.toString(), mCurrentUser.getUid(), strDate, userName, postNameFile, fileLink)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);

                                                            }
                                                        });
                                                    } else {

                                                        newPost.setValue(new Post(PostTitle, PostDesc1, downloadUrl.toString(), mCurrentUser.getUid(), strDate, userName, "", "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);

                                                            }
                                                        });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
//                            else {
//                                Toast.makeText(getApplicationContext(), "Please insert image", Toast.LENGTH_SHORT).show();
//
//                            }
//                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        DatabaseReference check = FirebaseDatabase.getInstance().getReference("Users").child("Admins");
                        check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {


                                    final DatabaseReference newPost = databaseRef2.push();
                                    final String refId = newPost.getKey();
                                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
                                            Date date = new Date();
                                            String strDate = dateFormat.format(date).toString();

                                            String firstName = dataSnapshot.child("FirstName").getValue().toString();
                                            String lastName = dataSnapshot.child("LastName").getValue().toString();
                                            String userName = firstName + " " + lastName;
                                            if (pdfUri != null) {

                                                uploadFile(refId);
                                                String fileName = txt_filename.getText().toString();
                                                String[] fileName2 = fileName.split(": ");
                                                String postNameFile = fileName2[1];

                                                String fileLink = getRealPathFromURI(pdfUri);

                                                newPost.setValue(new Post(PostTitle, PostDesc1, "", mCurrentUser.getUid(), strDate, userName, postNameFile, fileLink)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);

                                                    }
                                                });
                                            } else {
                                                newPost.setValue(new Post(PostTitle, PostDesc1, "", mCurrentUser.getUid(), strDate, userName, "", "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else {

                                    final DatabaseReference newPost = databaseRef.push();
                                    final String refId = newPost.getKey();
                                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
                                            Date date = new Date();
                                            String strDate = dateFormat.format(date).toString();

                                            String firstName = dataSnapshot.child("FirstName").getValue().toString();
                                            String lastName = dataSnapshot.child("LastName").getValue().toString();
                                            String userName = firstName + " " + lastName;
                                            if (pdfUri != null) {

                                                uploadFile(refId);
                                                String fileName = txt_filename.getText().toString();
                                                String[] fileName2 = fileName.split(": ");
                                                String postNameFile = fileName2[1];

                                                String fileLink = getRealPathFromURI(pdfUri);

                                                newPost.setValue(new Post(PostTitle, PostDesc1, "", mCurrentUser.getUid(), strDate, userName, postNameFile, fileLink)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);

                                                    }
                                                });
                                            } else {
                                                newPost.setValue(new Post(PostTitle, PostDesc1, "", mCurrentUser.getUid(), strDate, userName, "", "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                } else {

                    Toast.makeText(PostActivity.this, "Error in Posting", Toast.LENGTH_SHORT).show();

                }

            }


        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            Toast.makeText(PostActivity.this, "Please provide Permission", Toast.LENGTH_LONG).show();
        }
    }

    private void selectPdf() {

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    @Override
    // image from gallery result
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            View view_instance = findViewById(R.id.imageBtn);
            ViewGroup.LayoutParams params = view_instance.getLayoutParams();
            if (uri != null) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = 375;
                view_instance.setLayoutParams(params);
            }
            imageBtn.setImageURI(uri);
        } else if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            FileMetaData path = getFileMetaData(PostActivity.this, pdfUri);
            String path2 = getRealPathFromURI(pdfUri);
            String filename = path2.substring(path2.lastIndexOf("/") + 1);
            if (filename.indexOf(".") > 0) {
                file = filename.substring(0, filename.lastIndexOf("."));
            } else {
                file = filename;
            }
            txt_filename.setText("A file is selected: " + path.getDisplayName());
        } else {
//                Toast.makeText(PostActivity.this, "Please Select a file", Toast.LENGTH_LONG).show();

        }

    }

    public String getRealPathFromURI(final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(PostActivity.this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(PostActivity.this, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(PostActivity.this, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(PostActivity.this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public void uploadFile(final String id) {
        FileMetaData path = getFileMetaData(PostActivity.this, pdfUri);

        final String fileName = path.getDisplayName();
        final String fileName1 = file;
        final StorageReference storageReference = firebaseStorage.getReference();


        storageReference.child("Backpack").child(mAuth.getCurrentUser().getUid()).child(fileName).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = taskSnapshot.getDownloadUrl().toString();
                DatabaseReference reference = database.getReference();

                reference.child("Post").child("Pending").child(id).child("fileLink").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            Toast.makeText(PostActivity.this, "File Successfully Uploaded", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PostActivity.this, "File not Successfully Uploaded", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                progressDialog.hide();
                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                progressDialog.setProgress(currentProgress);
            }
        });
    }

    public static class FileMetaData {
        public String displayName;
        public long size;
        public String mimeType;
        public String path;

        public FileMetaData() {

        }

        public FileMetaData(String displayName, long size, String mimeType, String path) {
            this.displayName = displayName;
            this.size = size;
            this.mimeType = mimeType;
            this.path = path;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "name : " + displayName + " ; size : " + size + " ; path : " + path + " ; mime : " + mimeType;
        }
    }

    public static FileMetaData getFileMetaData(Context context, Uri uri) {
        FileMetaData fileMetaData = new FileMetaData();

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            File file = new File(uri.getPath());
            fileMetaData.displayName = file.getName();
            fileMetaData.size = file.length();
            fileMetaData.path = file.getPath();

            return fileMetaData;
        } else {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            fileMetaData.mimeType = contentResolver.getType(uri);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    fileMetaData.displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    if (!cursor.isNull(sizeIndex))
                        fileMetaData.size = cursor.getLong(sizeIndex);
                    else
                        fileMetaData.size = -1;

                    try {
                        fileMetaData.path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    } catch (Exception e) {
                        // DO NOTHING, _data does not exist
                    }

                    return fileMetaData;
                }
            } catch (Exception e) {
//                Log.e(Log.TAG_CODE, e);

            } finally {
                if (cursor != null)
                    cursor.close();
            }

            return null;
        }
    }

    public boolean checkIfAdmin(final String userId) {
        DatabaseReference check = FirebaseDatabase.getInstance().getReference("Users").child("Admins");

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    answer = true;
                    Log.e("Answer", "True");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.e("Answer", "False");
        return answer;
    }
}