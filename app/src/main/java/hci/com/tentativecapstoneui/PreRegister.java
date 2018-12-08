package hci.com.tentativecapstoneui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PreRegister extends AppCompatActivity {

    TextView pre_studentNumber;
    TextView pre_password;
    Button register;
    String student_num;
    String student_pass;

    FirebaseDatabase database;
    DatabaseReference myPreReg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register);

    }

    @Override
    protected void onStart() {
        super.onStart();
        pre_studentNumber = findViewById(R.id.pre_studentNumber);
        pre_password = findViewById(R.id.pre_password);
        register = findViewById(R.id.button_reg);

        database = FirebaseDatabase.getInstance();
        myPreReg = database.getReference("Users").child("Pending");


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                student_num = pre_studentNumber.getText().toString().trim();
                student_pass = pre_password.getText().toString().trim();

                myPreReg.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(student_num)){

                            String password = dataSnapshot.child(student_num).child("Password").getValue().toString();
                            if(student_pass.contentEquals(password)){
                                Intent intentRegister = new Intent(PreRegister.this, Register.class);
                                intentRegister.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentRegister.putExtra("PreReg", student_num);
                                startActivity(intentRegister);
                            }else{
                                Toast.makeText(PreRegister.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(PreRegister.this, "Student is not in the Enrolled List", Toast.LENGTH_SHORT).show();
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
