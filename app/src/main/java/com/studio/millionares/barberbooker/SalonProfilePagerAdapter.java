package com.studio.millionares.barberbooker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SalonProfilePagerAdapter extends FragmentStatePagerAdapter {

    /*VIEWPAGER ADAPTER FOR SalonBookingDetailsActivity*/

    private int numOfTabs;
    private Salon salon;

    public SalonProfilePagerAdapter(FragmentManager fm, int numOfTabs, Salon salon) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.salon = salon;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new SalonServicesFragment(salon);
            case 1:
                return new SalonReviewsFragment();
            case 2:
                return new SalonProfileFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
