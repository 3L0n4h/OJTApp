package hci.com.tentativecapstoneui;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


public class Settings extends Fragment {

    private Spinner spinnerCourse;
    private Spinner spinnerGender;
    private static final int GALLERY_REQUEST_CODE = 2;
    private FirebaseAuth mAuth;
    private StorageReference storage;
    private Uri filePath;
    private Uri trueFile;
    private final int PICK_IMAGE_REQUEST = 71;
    private DatabaseReference mDatabase;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextInputLayout inputPhone;
    private EditText editPhone;
    private ProgressBar mProgress;
    private ImageView image;
    private StorageReference storageReference;
    private FirebaseStorage storage1;
    private TextView datee;
    private String userPrivs;

    String phone;
    String date;
    String gender;
    String course;


    public Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        rootView.getRootView().setFocusableInTouchMode(true);
        rootView.getRootView().requestFocus();
        rootView.getRootView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, new OjtProgram())
                            .commit();
                    return true;
                }
                return false;
            }
        });

        storage1 = FirebaseStorage.getInstance();
        mProgress = rootView.findViewById(R.id.progressBar3);
        storageReference = storage1.getReference();
        image = rootView.findViewById(R.id.image);

        inputPhone = rootView.findViewById(R.id.inputPhone);
        editPhone = rootView.findViewById(R.id.editPhone);
        editPhone.addTextChangedListener(new MyTextWatcher(editPhone));

        datee = rootView.findViewById(R.id.date);
        spinnerCourse = rootView.findViewById(R.id.spinnerCourse);
        spinnerGender = rootView.findViewById(R.id.spinnerGender);

//        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CourseData.courseName));
        final TextView datee = rootView.findViewById(R.id.date);
        Button dateBtn = rootView.findViewById(R.id.btnDate);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Students").hasChild(mAuth.getCurrentUser().getUid())) {
                    String course = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("Course").getValue().toString();
                    String gender = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("Gender").getValue().toString();
                    String phone = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("PhoneNumber").getValue().toString();
                    String date = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("Birthday").getValue().toString();
                    String pic = dataSnapshot.child("Students").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();
                    int final_course;
                    int final_gender;
                    if (course.contentEquals("BSIT")) {
                        final_course = 1;
                    } else if (course.contentEquals("BSCS")) {
                        final_course = 2;
                    } else {
                        final_course = 3;
                    }
                    if (gender.contentEquals("Male")) {
                        final_gender = 1;
                    } else {
                        final_gender = 2;
                    }
                    userPrivs = "Students";

                    editPhone.setText(phone);
                    datee.setText(date);
                    Picasso.with(getContext()).load(pic).into(image);
                    spinnerCourse.setSelection(final_course);
                    spinnerGender.setSelection(final_gender);

                } else if (dataSnapshot.child("Admins").hasChild(mAuth.getCurrentUser().getUid())) {
                    String course = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("Course").getValue().toString();
                    String gender = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("Gender").getValue().toString();
                    String phone = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("PhoneNumber").getValue().toString();
                    String date = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("Birthday").getValue().toString();
                    String pic = dataSnapshot.child("Admins").child(mAuth.getCurrentUser().getUid()).child("ImageUrl").getValue().toString();
                    int final_course;
                    int final_gender;
                    if (course.contentEquals("BSIT")) {
                        final_course = 1;
                    } else if (course.contentEquals("BSCS")) {
                        final_course = 2;
                    } else {
                        final_course = 3;
                    }
                    if (gender.contentEquals("Male")) {
                        final_gender = 1;
                    } else {
                        final_gender = 2;
                    }
                    userPrivs = "Admins";
                    editPhone.setText(phone);
                    datee.setText(date);
                    Picasso.with(getContext()).load(pic).into(image);
                    spinnerCourse.setSelection(final_course);
                    spinnerGender.setSelection(final_gender);
                } else {
                    Toast.makeText(getContext(), "User account not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                datePicker.setMaxDate(System.currentTimeMillis());
                month = month + 1;
                String date = month + "/" + dayOfMonth + "/" + year;
                datee.setText(date);
            }
        };
        rootView.findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitForm();
                mProgress.setVisibility(View.VISIBLE);
                phone = editPhone.getText().toString().trim();
                date = datee.getText().toString().trim();
//                code = CourseData.courseAreaCodes[spinner.getSelectedItemPosition()];
                if ((String.valueOf(spinnerCourse.getSelectedItem())).equals("Select Course")) {
                    Toast.makeText(getContext(), "Please select a course", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((String.valueOf(spinnerGender.getSelectedItem())).equals("Select Gender")) {
                    Toast.makeText(getContext(), "Please select your gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phone.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editPhone.setError("Enter phone number");
                    editPhone.requestFocus();
                    return;
                }

                if (!TextUtils.isEmpty(phone)
                        && !TextUtils.isEmpty(date)
                        && !(String.valueOf(spinnerCourse.getSelectedItem())).equals("Select Course")
                        && !(String.valueOf(spinnerGender.getSelectedItem())).equals("Select Gender")) {
                    uploadImage();

                } else {
                    mProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
        rootView.findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter current password");
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_settings_dialog, (ViewGroup) getView(), false);
                final TextView email = viewInflated.findViewById(R.id.update_email);
                final EditText pass = viewInflated.findViewById(R.id.update_password);
                FirebaseUser user = mAuth.getCurrentUser();
                String userEmail = "Email: " + user.getEmail();
                email.setText(userEmail);

                builder.setView(viewInflated);
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mAuth.getCurrentUser() != null) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String finalEmail = user.getEmail();
                            String userEmail = "Email: " + user.getEmail();
                            email.setText(userEmail);
                            String update_password = pass.getText().toString();

                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(finalEmail, update_password);

                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                                Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                                                builder2.setTitle("Enter new password");

                                                View viewInflated2 = LayoutInflater.from(getContext()).inflate(R.layout.fragment_settings_change_password, (ViewGroup) getView(), false);
                                                final EditText newPass = viewInflated2.findViewById(R.id.update_password2);

                                                builder2.setView(viewInflated2);
                                                builder2.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        String newPassword = newPass.getText().toString();

                                                        user.updatePassword(newPassword)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(getContext(), "Password Successfully Changed!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                                builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                                builder2.show();

                                            } else {
                                                Toast.makeText(getContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), "User not Logged in", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        return rootView;
    }

    private void submitForm() {
        if (!validatePhone()) {
            return;
        }
    }


    private boolean validatePhone() {
        if (editPhone.getText().toString().trim().isEmpty()) {
            inputPhone.setError("Enter your phone number");
            requestFocus(editPhone);
            return false;
        } else {
            inputPhone.setErrorEnabled(false);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }

    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            Picasso.with(getContext()).load(filePath).into(image);
        }
    }

    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Updating your Account...");
        progressDialog.show();
        final String phone = editPhone.getText().toString().trim();
        final String date = datee.getText().toString().trim();
        final String course = String.valueOf(spinnerCourse.getSelectedItem());
        final String gender = String.valueOf(spinnerGender.getSelectedItem());

        StorageReference ref = storageReference.child("user_images/" + UUID.randomUUID().toString());
        if (filePath != null) {
            if (userPrivs.contentEquals("Students")) {
                ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference newOfficer = mDatabase.child("Students").child(user_id);

                        newOfficer.child("Birthday").setValue(date);
                        newOfficer.child("PhoneNumber").setValue(phone);
                        newOfficer.child("Course").setValue(course);
                        newOfficer.child("Gender").setValue(gender);
                        newOfficer.child("ImageUrl").setValue(downloadUrl.toString());

                        mProgress.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            } else {
                ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference newOfficer = mDatabase.child("Admins").child(user_id);

                        newOfficer.child("Birthday").setValue(date);
                        newOfficer.child("PhoneNumber").setValue(phone);
                        newOfficer.child("Course").setValue(course);
                        newOfficer.child("Gender").setValue(gender);
                        newOfficer.child("ImageUrl").setValue(downloadUrl.toString());

                        mProgress.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            }

        } else {
            if (userPrivs.contentEquals("Students")) {
                progressDialog.dismiss();
                String user_id = mAuth.getCurrentUser().getUid();
                DatabaseReference newOfficer = mDatabase.child("Students").child(user_id);

                newOfficer.child("Birthday").setValue(date);
                newOfficer.child("PhoneNumber").setValue(phone);
                newOfficer.child("Course").setValue(course);
                newOfficer.child("Gender").setValue(gender);

                Toast.makeText(getContext(), "Account info updated!", Toast.LENGTH_SHORT).show();

                mProgress.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else{
                progressDialog.dismiss();
                String user_id = mAuth.getCurrentUser().getUid();
                DatabaseReference newOfficer = mDatabase.child("Admins").child(user_id);

                newOfficer.child("Birthday").setValue(date);
                newOfficer.child("PhoneNumber").setValue(phone);
                newOfficer.child("Course").setValue(course);
                newOfficer.child("Gender").setValue(gender);

                Toast.makeText(getContext(), "Account info updated!", Toast.LENGTH_SHORT).show();

                mProgress.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

}

