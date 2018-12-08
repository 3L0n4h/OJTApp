package hci.com.tentativecapstoneui;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

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

import hci.com.tentativecapstoneui.model.CalendarModelClass;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarUser extends Fragment {


    CalendarView calendarView;
    TextView myDate, txt_what, txt_where, txt_AM, txt_PM;
    Button btn_saveEvent;
    private DatabaseReference clinicDatabase, primaryDatabase, mDatabaseUsers;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String userPrivs;
    Spinner spinnerAM, spinnerPM;
    String twhat, twhere, date, email, password, tam, tpm;
    String user;


    public CalendarUser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);


        calendarView = rootView.findViewById(R.id.calendarView);
        myDate = rootView.findViewById(R.id.myDate);
        btn_saveEvent = rootView.findViewById(R.id.btn_saveEvent);
        txt_what = rootView.findViewById(R.id.txt_what);
        txt_where = rootView.findViewById(R.id.txt_where);
        txt_AM = rootView.findViewById(R.id.txt_AM);
        txt_PM = rootView.findViewById(R.id.txt_PM);
        clinicDatabase = database.getInstance().getReference().child("Post").child("Events");

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        primaryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Admins");
        mCurrentUser = mAuth.getCurrentUser();



        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                date = (i1 + 1) + "/" + i2 + "/" + i;
                myDate.setText(date);
            }
        });
        //time text view
        txt_AM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String AM_PM;
                        if (hour < 12) {
                            AM_PM = "AM";
                        } else {
                            AM_PM = "PM";
                        }
                        if (hour > 12) {
                            hour = hour - 12;
                        } else if (hour == 0) {
                            hour = hour + 12;
                        }
                        StringBuffer strBuf = new StringBuffer();
                        strBuf.append("");
                        strBuf.append(String.format("%02d", hour));
                        strBuf.append(":");
                        strBuf.append(String.format("%02d", minute));
                        TextView timePickerValueTextView = (TextView) getActivity().findViewById(R.id.txt_AM);
                        timePickerValueTextView.setText(strBuf.toString() + " " + AM_PM);
                    }

                };

                final Calendar now = Calendar.getInstance();
                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int minute = now.get(java.util.Calendar.MINUTE);
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth,
                                onTimeSetListener, hour, minute, false);
                timePickerDialog.setTitle("Please select time.");
                timePickerDialog.show();
            }
        });
        //pm
        txt_PM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String AM_PM;
                        if (hour < 12) {
                            AM_PM = "AM";
                        } else {
                            AM_PM = "PM";
                        }
                        if (hour > 12) {
                            hour = hour - 12;
                        } else if (hour == 0) {
                            hour = hour + 12;
                        }
                        StringBuffer strBuf = new StringBuffer();
                        strBuf.append("");
                        strBuf.append(String.format("%02d", hour));
                        strBuf.append(":");
                        strBuf.append(String.format("%02d", minute));
                        TextView timePickerValueTextView = (TextView) getActivity().findViewById(R.id.txt_PM);
                        timePickerValueTextView.setText(strBuf.toString() + " " + AM_PM);
                    }
                };
                Calendar now = Calendar.getInstance();
                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int minute = now.get(java.util.Calendar.MINUTE);
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth,
                                onTimeSetListener, hour, minute, false);
                timePickerDialog.setTitle("Please select time.");
                timePickerDialog.show();
            }
        });
        ///posting
        btn_saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twhat = txt_what.getText().toString().trim();
                twhere = txt_where.getText().toString().trim();
                date = myDate.getText().toString().trim();
                tam = txt_AM.getText().toString().trim();
                tpm = txt_PM.getText().toString().trim();

                if (tam.isEmpty()) {
                    txt_AM.setError("Enter event time");
                    txt_AM.requestFocus();
                    return;
                }
                if (tpm.isEmpty()) {
                    txt_PM.setError("Enter event time");
                    txt_PM.requestFocus();
                    return;
                }
                if (twhat.isEmpty()) {
                    txt_what.setError("Enter event name");
                    txt_what.requestFocus();
                    return;
                }
                if (twhere.isEmpty()) {
                    txt_where.setError("Enter event place");
                    txt_where.requestFocus();
                    return;
                }
                if (date.isEmpty()) {
                    myDate.setError("Enter event place");
                    myDate.requestFocus();
                    return;
                }
                if (!TextUtils.isEmpty(twhere) && !TextUtils.isEmpty(twhere) && !TextUtils.isEmpty(date)
                        && !TextUtils.isEmpty(tam) && !TextUtils.isEmpty(tpm)) {
                    uploadEvent();
                } else {
                    //enter else statement here
                    Toast.makeText(getActivity(), "Error in Creating Event", Toast.LENGTH_SHORT).show();

                }

            }

        });

        return rootView;
    }

    //try


    private void uploadEvent() {

        getUser();
        String nameOfUser = user;
        final String twhat = txt_what.getText().toString().trim();
        final String twhere = txt_where.getText().toString().trim();
        final String date = myDate.getText().toString().trim();
        final String tAM = txt_AM.getText().toString().trim();
        final String tPM = txt_PM.getText().toString().trim();
        final DatabaseReference clinicPost = clinicDatabase.push();

        clinicPost.setValue(new CalendarModelClass(twhat, twhere, date, tAM, tPM, nameOfUser));

    }

    public void getUser() {
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        primaryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.child(userId).child("FirstName").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
