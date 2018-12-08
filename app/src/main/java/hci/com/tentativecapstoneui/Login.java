package hci.com.tentativecapstoneui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity {
    private EditText email, password, studentNumber;
    private Button login, register;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog loadingBar;
    private TextView forgotpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (sharedpreferences.getBoolean("is_first_exec", false)) {
            editor.putBoolean("is_first_exec", false);
            Toast.makeText(this, "First", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }

        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(Login.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

//        DatabaseReference mUsers = FirebaseDatabase.getInstance().getReference("Users");
//        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String id = mAuth.getCurrentUser().getUid();
//                if (dataSnapshot.child("Admins").hasChild(id) || dataSnapshot.child("Students").hasChild(id)) {
//                    Intent intent = new Intent(Login.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//                    startActivity(intent);
//                }else{
//                    mAuth.signOut();
////                    Intent intent = new Intent(Login.this, Login.class);
////                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////
////                    startActivity(intent);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        loadingBar = new ProgressDialog(this);
        forgotpass = findViewById(R.id.txtforgotpass);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotintent = new Intent(Login.this, PasswordActivity.class);
                startActivity(forgotintent);
            }
        });


        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        studentNumber = findViewById(R.id.login_studentNumber);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                final String userEmail = email.getText().toString().trim();
                final String userPassword = password.getText().toString().trim();
                final String userStudentNumber = studentNumber.getText().toString().trim();

                password.setTransformationMethod(new PasswordTransformationMethod());

                if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword)) {
                    loadingBar.setTitle("Login");
                    loadingBar.setMessage("Connecting... ");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);
                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child("Registered").hasChild(userStudentNumber)) {
//                                String checkEmail = dataSnapshot.child("Registered").child(userStudentNumber).child("Email").getValue().toString();
                                if (dataSnapshot.child("Registered").child(userStudentNumber).child("Email").getValue().toString().contentEquals(userEmail)) {
                                    mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Login.this, "Login Successful...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                                checkUserExistence();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(Login.this, "Error Occurred:  " + message, Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(Login.this, "Email does not match to the Student Number", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }

                            } else {
                                loadingBar.dismiss();
                                Toast.makeText(Login.this, "Student Number is not Registered", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(Login.this, "Complete all fields...  ", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, PreRegister.class);
                startActivity(intent);
            }
        });
    }

    private void checkUserExistence() {
//        DatabaseReference user_id = FirebaseDatabase.getInstance().getReference().child("tblUsers");
//        final DatabaseReference userVerification = user_id.child("userVerification");
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Students").hasChild(user_id)) {
                    Intent mainIntent = new Intent(Login.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else if (dataSnapshot.child("Admins").hasChild(user_id)) {
                    Intent mainIntent = new Intent(Login.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "User not verified!", Toast.LENGTH_SHORT).show();
//                    mProgress.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent();
                intent.setClass(Login.this, PasswordActivity.class);
                startActivity(intent);

            }
        });

    }
}
