package com.studio.millionares.barberbooker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentAndPastBookingsPagerAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;
    private ArrayList<HashMap<String, Object>> currentBookings, pastBookings;

    public CurrentAndPastBookingsPagerAdapter(FragmentManager fm, int numOfTabs, ArrayList<HashMap<String, Object>> currentBookings, ArrayList<HashMap<String, Object>> pastBookings) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.currentBookings = currentBookings;
        this.pastBookings = pastBookings;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new CurrentAppointmentsFragment(currentBookings);
            case 1:
                return new PastAppointmentsFragment(pastBookings);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
