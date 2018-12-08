package hci.com.tentativecapstoneui;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hci.com.tentativecapstoneui.model.Company;

public class AddCompany extends AppCompatActivity {

    private EditText editCompany;
    private EditText editAddress;
    private EditText editContactNo;
    private EditText editContactPerson;
    private EditText editEmailAddress;
    private TextInputLayout inputCompany, inputAddress, inputContactNo, inputContactPerson, inputEmailAddress;
    private FirebaseDatabase storage6;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference companyPending;

    String company;
    String address;
    String contactNo;
    String contactPerson;
    String emailAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        storage6 = FirebaseDatabase.getInstance();
        editCompany = findViewById(R.id.editCompany);
        editCompany.addTextChangedListener(new MyTextWatcher(editCompany));
        inputCompany = findViewById(R.id.inputCompany);
        editAddress = findViewById(R.id.editAddress);
        editAddress.addTextChangedListener(new MyTextWatcher(editAddress));
        inputAddress = findViewById(R.id.inputAddress);
        editContactNo = findViewById(R.id.editContactNo);
        editContactNo.addTextChangedListener(new MyTextWatcher(editContactNo));
        inputContactNo = findViewById(R.id.inputContactNo);
        editContactPerson = findViewById(R.id.editContactPerson);
        editContactPerson.addTextChangedListener(new MyTextWatcher(editContactPerson));
        inputContactPerson = findViewById(R.id.inputContactPerson);
        editEmailAddress = findViewById(R.id.editEmailAddress);
        editEmailAddress.addTextChangedListener(new MyTextWatcher(editEmailAddress));
        inputEmailAddress = findViewById(R.id.inputEmailAddress);
        companyPending = FirebaseDatabase.getInstance().getReference("AddCompany").child("Pending");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("AddCompany");
        findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                submitForm();
                company = editCompany.getText().toString().trim();
                address = editAddress.getText().toString().trim();
                contactNo = editContactNo.getText().toString().trim();
                contactPerson = editContactPerson.getText().toString().trim();
                emailAdd = editEmailAddress.getText().toString().trim();

                if (company.isEmpty()) {
                    editCompany.setError("Enter Company Name");
                    editCompany.requestFocus();
                    return;
                }
                if (address.isEmpty()) {
                    editAddress.setError("Enter Address");
                    editAddress.requestFocus();
                    return;
                }
                if (contactNo.isEmpty()) {
                    editContactNo.setError("Enter Contact Number");
                    editContactNo.requestFocus();
                    return;
                }
                if (contactPerson.isEmpty()) {
                    editContactPerson.setError("Enter Contact Person");
                    editContactPerson.requestFocus();
                    return;
                }
                if (emailAdd.isEmpty()) {
                    editEmailAddress.setError("Enter Email Address");
                    editEmailAddress.requestFocus();
                    return;
                }
                if (!TextUtils.isEmpty(company) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(contactNo) && !TextUtils.isEmpty(contactPerson)
                        && !TextUtils.isEmpty(emailAdd)) {

                    companyPending.child(company).setValue(new Company(company,address,contactNo,contactPerson,emailAdd)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(AddCompany.this, "Company Registration is Currently Pending", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(AddCompany.this, "Error Occurred:  " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }

        });
    }

    private void submitForm() {
        if (!validateCompany()) {
            return;
        }
        if (!validateAddress()) {
            return;
        }
        if (!validateContactNo()) {
            return;
        }
        if (!validateContactPerson()) {
            return;
        }
        if (!validateEmailAddress()) {
            return;
        }
    }

    private boolean validateContactPerson() {
        if (editContactPerson.getText().toString().trim().isEmpty()) {
            inputContactPerson.setError("Enter your Contact Person");
            requestFocus(inputContactPerson);
            return false;
        } else {
            inputContactPerson.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateAddress() {
        if (editAddress.getText().toString().trim().isEmpty()) {
            inputAddress.setError("Enter your Address");
            requestFocus(inputAddress);
            return false;
        } else {
            inputAddress.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateCompany() {
        if (editCompany.getText().toString().trim().isEmpty()) {
            inputCompany.setError("Enter your Company");
            requestFocus(inputCompany);
            return false;
        } else {
            inputCompany.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateContactNo() {
        if (editContactNo.getText().toString().trim().isEmpty()) {
            inputContactNo.setError("Enter your Contact Number");
            requestFocus(inputContactNo);
            return false;
        } else {
            inputContactNo.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmailAddress() {
        if (editEmailAddress.getText().toString().trim().isEmpty()) {
            inputEmailAddress.setError("Enter your Email Address");
            requestFocus(inputEmailAddress);
            return false;
        } else {
            inputEmailAddress.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;
        private MyTextWatcher(View view){this.view = view;}

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()){
                case R.id.inputCompany:
                    validateCompany();
                    break;

                case R.id.inputAddress:
                    validateAddress();
                    break;
                case R.id.inputContactNo:
                    validateContactNo();
                    break;
                case R.id.inputContactPerson:
                    validateContactPerson();
                    break;
                case R.id.inputEmailAddress:
                    validateEmailAddress();
                    break;
            }

        }
    }

}


