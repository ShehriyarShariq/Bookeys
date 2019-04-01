package com.studio.millionares.barberbooker;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentAppointmentsFragment extends Fragment {

    private ArrayList<Appointment> currentBookings;
    CurrentAppointmentsListAdapter currentAppointmentsListAdapter;

    RecyclerView currentBookingsList;

    public CurrentAppointmentsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public CurrentAppointmentsFragment(ArrayList<Appointment> currentBookings) {
        this.currentBookings = currentBookings;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_appointments, container, false);

        currentBookingsList = view.findViewById(R.id.current_bookings);

        currentBookingsList.setHasFixedSize(true);
        LinearLayoutManager currentBookingsListLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        currentBookingsList.setLayoutManager(currentBookingsListLinearLayoutManager);

        currentAppointmentsListAdapter = new CurrentAppointmentsListAdapter("current", currentBookings, new AppointmentsRecyclerViewListClickListener() {
            @Override
            public void AppointmentListOnClick(Appointment appointment) {
                Intent appointmentIntent = new Intent(getActivity(), AppointmentDetailsActivity.class);
                appointmentIntent.putExtra("appointment", appointment);

                startActivity(appointmentIntent);
            }
        });
        currentBookingsList.setAdapter(currentAppointmentsListAdapter);


        return view;
    }

}
