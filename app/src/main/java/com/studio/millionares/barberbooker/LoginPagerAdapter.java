package com.studio.millionares.barberbooker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class LoginPagerAdapter extends FragmentStatePagerAdapter {

    /*
        VIEWPAGER ADAPTER FOR LoginActivity
    */

    Context context;
    private int noOfTabs;

    public LoginPagerAdapter(FragmentManager fm, int noOfTabs) {
        super(fm);
        this.context = context;
        this.noOfTabs = noOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                SignInFragment signInFragment = new SignInFragment();
                return signInFragment;
            case 1:
                SignUpFragment signUpFragment = new SignUpFragment();
                return signUpFragment;
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return noOfTabs;
    }
}
