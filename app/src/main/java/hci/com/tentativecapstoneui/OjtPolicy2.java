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
public class OjtPolicy2 extends Fragment {


    public OjtPolicy2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ojt_policy2, container, false);

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
        return rootView;
    }
}
