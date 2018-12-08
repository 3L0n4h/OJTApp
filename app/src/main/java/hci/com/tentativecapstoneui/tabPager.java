package hci.com.tentativecapstoneui;

import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by user on 7/5/2018.
 */

public class tabPager extends FragmentStatePagerAdapter {

    String[] tabarray= new String[]{"","","",""};
    Integer tabnumber = 4;

    public tabPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabarray[position ];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                One Onel = new One();
                return Onel;
            case 1:
                Two Two1 = new Two();
                return Two1;
            case 2:
                Three Three1 = new Three();
                return Three1;
            case 3:
                Four Fourth1 = new Four();
                return Fourth1;
        }

        return null;
    }

    @Override
    public int getCount() {
        return tabnumber;
    }
}
