package hci.com.tentativecapstoneui;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class OjtProgram extends Fragment {

    CardView objective;
    CardView policy;
    CardView guidelines;
    CardView grade;
    Fragment fragment;

    public OjtProgram() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ojt_program, container, false);

        rootView.getRootView().setFocusableInTouchMode(true);
        rootView.getRootView().requestFocus();
        rootView.getRootView().setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, new OjtProgram())
                            .commit();
                    return true;
                }
                return false;
            }
        });

        objective = rootView.findViewById(R.id.cardView1);
        objective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragment = new OjtObjective2();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });

        policy = rootView.findViewById(R.id.cardView2);
        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragment = new OjtPolicy2();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });

        guidelines = rootView.findViewById(R.id.cardView3);
        guidelines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getContext(), OjtViewpager.class);
                startActivity(i);
            }
        });

        grade = rootView.findViewById(R.id.cardView4);
        grade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragment = new OjtGrade2();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });

        return rootView;
    }

}
