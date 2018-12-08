package hci.com.tentativecapstoneui;



import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Two extends android.support.v4.app.Fragment {

    RecyclerView recyclerView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Button upload;
    android.support.v4.app.Fragment fragment = null;
    FirebaseAuth mAuth ;

    public Two() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_two, container, false);
        rootView.getRootView().setFocusableInTouchMode(true);
        rootView.getRootView().requestFocus();
//        rootView.getRootView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    getFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.content_frame, new Home())
//                            .commit();
//                    return true;
//                }
//                return false;
//            }
//        });

        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Backpack");
        mAuth = FirebaseAuth.getInstance();

        databaseReference.child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String fileName = dataSnapshot.getKey();
                String url = dataSnapshot.getValue(String.class);
//                String url = dataSnapshot.
                ((BackpackConstruct) recyclerView.getAdapter()).update(fileName, url);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recyclerView = getView().findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        BackpackConstruct myAdapter = new BackpackConstruct(recyclerView, getContext(), new ArrayList<String>(), new ArrayList<String>());
        recyclerView.setAdapter(myAdapter);

        upload = getView().findViewById(R.id.btn_backpackUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new BackpackUpload();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();

            }
        });

    }
}
