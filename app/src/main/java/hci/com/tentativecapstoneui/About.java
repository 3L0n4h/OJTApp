package hci.com.tentativecapstoneui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class About extends Fragment {


    public About() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_about, container, false);

        rootview.getRootView().setFocusableInTouchMode(true);
        rootview.getRootView().requestFocus();
        rootview.getRootView().setOnKeyListener(new View.OnKeyListener() {
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

        return rootview;
    }

}
