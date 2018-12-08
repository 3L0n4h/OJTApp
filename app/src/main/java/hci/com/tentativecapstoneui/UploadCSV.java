package hci.com.tentativecapstoneui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadCSV extends Fragment {

    Button btn_choose, btn_process;
    TextView fileText;
    Uri pdfUri = null;
    String file;
    String path = null;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    DatabaseReference myUsers;


    public UploadCSV() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_upload_csv, container, false);

        btn_choose = rootView.findViewById(R.id.btn_choose);
        btn_process = rootView.findViewById(R.id.btn_process);
        fileText = rootView.findViewById(R.id.file);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myUsers = database.getReference("Users").child("Pending");

        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }
            }
        });

        btn_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FileMetaData path = getFileMetaData(getContext(), pdfUri);
                if(pdfUri != null){
                    String path2 = getRealPathFromURI(pdfUri);
                    proImportCSV(new File(path2));
                }else{
                    Toast.makeText(getContext(), "Please Select a File", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return rootView;
    }


    private void proImportCSV(final File from) {

        try {

            CSVReader dataRead = new CSVReader(new FileReader(from));
            List<String[]> records = dataRead.readAll();

            for(String[] record : records) {

                myUsers.child(record[0]).child("StudentNumber").setValue(record[0]);
                myUsers.child(record[0]).child("Fullname").setValue(record[1]);
                myUsers.child(record[0]).child("Password").setValue(record[2]);

            }
            Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
            dataRead.close();

        } catch(Exception e){

            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPdf () {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), 86);
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
//            pdfUri = data.getData();
//            FileMetaData path = getFileMetaData(MainActivity.this, pdfUri);
//            String path2 = getRealPathFromURI(pdfUri);
//            String filename = path2.substring(path2.lastIndexOf("/") + 1);
//            if (filename.indexOf(".") > 0) {
//                file = filename.substring(0, filename.lastIndexOf("."));
//            } else {
//                file = filename;
//            }
//            fileText.setText("A file is selected: " + path.getDisplayName());
//        } else {
//            Toast.makeText(MainActivity.this, "Please Select a file", Toast.LENGTH_LONG).show();
//        }

        switch (requestCode) {
            case 86: {
                if (resultCode == RESULT_OK) {
                    pdfUri = data.getData();
                    path = pdfUri.getPath();
                    FileMetaData path2 = getFileMetaData(getContext(), pdfUri);
                    fileText.setText(path2.getDisplayName());
                }
            }
        }

    }

    public String getRealPathFromURI ( final Uri uri){
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(getContext(), uri)) {
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

                return getDataColumn(getContext(), contentUri, null, null);
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

                return getDataColumn(getContext(), contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(getContext(), uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn (Context context, Uri uri, String selection,
                                  String[]selectionArgs){

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

    private boolean isExternalStorageDocument (Uri uri){
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument (Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument (Uri uri){
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri (Uri uri){
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
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

    public static FileMetaData getFileMetaData (Context context, Uri uri){
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

}
