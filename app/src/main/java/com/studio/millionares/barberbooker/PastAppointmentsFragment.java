package com.studio.millionares.barberbooker;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class PastAppointmentsFragment extends Fragment {

    private ArrayList<HashMap<String, Object>> pastBookings;

    RecyclerView pastBookingsList;
    CurrentAndPastAppointmentsListAdapter pastAppointmentsListAdapter;

    public PastAppointmentsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public PastAppointmentsFragment(ArrayList<HashMap<String, Object>> pastBookings) {
        this.pastBookings = pastBookings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_appointments, container, false);

        pastBookingsList = view.findViewById(R.id.past_bookings);

        pastBookingsList.setHasFixedSize(true);
        LinearLayoutManager pastBookingsListLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        pastBookingsList.setLayoutManager(pastBookingsListLinearLayoutManager);

        pastAppointmentsListAdapter = new CurrentAndPastAppointmentsListAdapter("past", pastBookings);
        pastBookingsList.setAdapter(pastAppointmentsListAdapter);

        return view;
    }

}