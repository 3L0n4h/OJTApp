package hci.com.tentativecapstoneui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.Calendar;
import java.util.UUID;


public class Register extends AppCompatActivity {
    private Spinner spinnerCourse;
    private Spinner spinnerGender;
    private static final int GALLERY_REQUEST_CODE = 2;
    private FirebaseAuth mAuth;
    private StorageReference storage;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private DatabaseReference mDatabase;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextInputLayout inputFirstname, inputLastname, inputEmail, inputPassword, inputPhone, inputStud;
    private EditText editFirst, editLast, editEmail, editPassword, editPhone;
    private EditText editStud;
    private ProgressBar mProgress;
    private ImageView image;
    private StorageReference storageReference;
    private FirebaseStorage storage1;
    private TextView datee;
    private DatabaseReference myPendings;
    private String preReg;


    String fname;
    String lname;
    String email;
    String password;
    String phone;
    String date;
    String gender;
    String course;
    String studNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        storage1 = FirebaseStorage.getInstance();
        mProgress = findViewById(R.id.progressBar3);
        storageReference = storage1.getReference();
        image = findViewById(R.id.image);
        inputFirstname = findViewById(R.id.inputFirst);
        editFirst = findViewById(R.id.editFirst);
        editFirst.addTextChangedListener(new MyTextWatcher(editFirst));
        inputLastname = findViewById(R.id.inputLast);
        editLast = findViewById(R.id.editLast);
        editLast.addTextChangedListener(new MyTextWatcher(editLast));
        inputEmail = findViewById(R.id.inputEmail);
        editEmail = findViewById(R.id.editEmail);
        editEmail.addTextChangedListener(new MyTextWatcher(editEmail));
        inputPassword = findViewById(R.id.inputPassword);
        editPassword = findViewById(R.id.editPassword);
        editPassword.addTextChangedListener(new MyTextWatcher(editPassword));
        inputPhone = findViewById(R.id.inputPhone);
        editPhone = findViewById(R.id.editPhone);
        editPhone.addTextChangedListener(new MyTextWatcher(editPhone));
        inputStud = findViewById(R.id.inputStud);
        editStud = findViewById(R.id.editStud);
        editStud.addTextChangedListener(new MyTextWatcher(editStud));
        datee = findViewById(R.id.date);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerGender = findViewById(R.id.spinnerGender);

        myPendings = FirebaseDatabase.getInstance().getReference("Users").child("Pending");
        preReg = getIntent().getExtras().get("PreReg").toString();
        editStud.setText(preReg);
        editStud.setEnabled(false);

//        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CourseData.courseName));
        final TextView datee = findViewById(R.id.date);
        Button dateBtn = findViewById(R.id.btnDate);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
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
                        Register.this,
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
        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitForm();
                mProgress.setVisibility(View.VISIBLE);
                fname = editFirst.getText().toString().trim();
                lname = editLast.getText().toString().trim();
                email = editEmail.getText().toString().trim();
                password = editPassword.getText().toString().trim();
                phone = editPhone.getText().toString().trim();
                date = datee.getText().toString().trim();
                studNo = editStud.getText().toString().trim();
//                code = CourseData.courseAreaCodes[spinner.getSelectedItemPosition()];
                if ((String.valueOf(spinnerCourse.getSelectedItem())).equals("Select Course")) {
                    Toast.makeText(Register.this, "Please select a course", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((String.valueOf(spinnerGender.getSelectedItem())).equals("Select Gender")) {
                    Toast.makeText(Register.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (studNo.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editStud.setError("Enter Student Number");
                    editStud.requestFocus();
                    return;
                }

                if (fname.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editFirst.setError("Enter firstname");
                    editFirst.requestFocus();
                    return;
                }
                if (lname.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editLast.setError("Enter lastname");
                    editLast.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editEmail.setError("Enter email address");
                    editEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editPassword.setError("Enter password");
                    editPassword.requestFocus();
                    return;
                }
                if (phone.isEmpty()) {
                    mProgress.setVisibility(View.INVISIBLE);
                    editPhone.setError("Enter phone number");
                    editPhone.requestFocus();
                    return;
                }
                if (!TextUtils.isEmpty(fname) && !TextUtils.isEmpty(lname) && !TextUtils.isEmpty(email) &&
                        !TextUtils.isEmpty(password) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(date)
                        && !(String.valueOf(spinnerCourse.getSelectedItem())).equals("Select Course")
                        && !(String.valueOf(spinnerGender.getSelectedItem())).equals("Select Gender")) {
                    uploadImage();

                } else {
                    mProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void submitForm() {
        if (!validateFirstname()) {
            return;
        }
        if (!validateLastname()) {
            return;
        }
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }
        if (!validatePhone()) {
            return;
        }
        if (!validateStudentNumber()) {
            return;
        }
    }

    private boolean validateStudentNumber() {
        if (editStud.getText().toString().trim().isEmpty()) {
            inputStud.setError("Enter your student number");
            requestFocus(inputStud);
            return false;
        } else {
            inputStud.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateFirstname() {
        if (editFirst.getText().toString().trim().isEmpty()) {
            inputFirstname.setError("Enter your first name");
            requestFocus(inputFirstname);
            return false;
        } else {
            inputFirstname.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateLastname() {
        if (editLast.getText().toString().trim().isEmpty()) {
            inputLastname.setError("Enter your last name");
            requestFocus(inputLastname);
            return false;
        } else {
            inputLastname.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputEmail.setError("Enter your Email");
            requestFocus(inputEmail);
            return false;
        } else {
            inputEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (editPassword.getText().toString().trim().isEmpty()) {
            inputPassword.setError("Enter your password");
            requestFocus(inputPassword);
            return false;
        } else {
            inputPassword.setErrorEnabled(false);
        }
        return true;
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
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.inputFirst:
                    validateFirstname();
                    break;
                case R.id.inputLast:
                    validateLastname();
                    break;
                case R.id.inputEmail:
                    validateEmail();
                    break;
                case R.id.inputPassword:
                    validatePassword();
                    break;
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            Picasso.with(this).load(filePath).into(image);
        }
    }

    private void uploadImage() {

        if (filePath != null) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        final DatabaseReference myPendingAccounts = FirebaseDatabase.getInstance().getReference("Users").child("Pending");

                        ProcessRegistration();

                        myPendingAccounts.child(preReg).removeValue();


                    } else {
                        mProgress.setVisibility(View.INVISIBLE);
                        Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            mProgress.setVisibility(View.INVISIBLE);
            Toast.makeText(Register.this, "Please upload image", Toast.LENGTH_LONG).show();
        }
    }

    private void SendUserToSetupActivity() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent setupIntent = new Intent(Register.this, Login.class);
        setupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
    private void ProcessRegistration(){

        final DatabaseReference myRegistered = FirebaseDatabase.getInstance().getReference("Users").child("Registered");
        myPendings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(preReg)) {
                    myRegistered.child(preReg).child("Fullname").setValue(dataSnapshot.child(preReg).child("Fullname").getValue().toString());
                    myRegistered.child(preReg).child("Password").setValue(dataSnapshot.child(preReg).child("Password").getValue().toString());
                    myRegistered.child(preReg).child("StudentNumber").setValue(dataSnapshot.child(preReg).child("StudentNumber").getValue().toString());
                    myRegistered.child(preReg).child("Email").setValue(email);

                    CreateAccount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void CreateAccount(){
        final ProgressDialog progressDialog = new ProgressDialog(Register.this);
        progressDialog.setTitle("Creating your Account...");
        progressDialog.show();
        final String fname = editFirst.getText().toString().trim();
        final String lname = editLast.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();
        final String date = datee.getText().toString().trim();
        final String course = String.valueOf(spinnerCourse.getSelectedItem());
        final String gender = String.valueOf(spinnerGender.getSelectedItem());
        final String studNo = editStud.getText().toString().trim();

        StorageReference ref = storageReference.child("user_images/" + UUID.randomUUID().toString());
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                myPendings.child(preReg).removeValue();

                progressDialog.dismiss();
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String user_id = mAuth.getCurrentUser().getUid();
                DatabaseReference newOfficer = mDatabase.child("Students").child(user_id);

                newOfficer.child("FirstName").setValue(fname);
                newOfficer.child("LastName").setValue(lname);
                newOfficer.child("Email").setValue(email);
                newOfficer.child("Birthday").setValue(date);
                newOfficer.child("PhoneNumber").setValue(phone);
                newOfficer.child("Course").setValue(course);
                newOfficer.child("Gender").setValue(gender);
                newOfficer.child("StudentNumber").setValue(studNo);
                newOfficer.child("ImageUrl").setValue(downloadUrl.toString());

                mProgress.setVisibility(View.INVISIBLE);


                SendUserToSetupActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            startActivity(intent);
//        }
//    }

