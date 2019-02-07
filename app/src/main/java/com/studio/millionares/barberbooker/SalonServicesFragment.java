package com.studio.millionares.barberbooker;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SalonServicesFragment extends Fragment {

    RecyclerView servicesList;
    ProgressBar servicesLoader;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    private Salon salon;

    ArrayList<Service> allServices;
    SalonServicesListAdapter salonServicesListAdapter;

    public SalonServicesFragment() {
    }

    @SuppressLint("ValidFragment")
    public SalonServicesFragment(Salon salon) {
        // Required empty public constructor
        this.salon = salon;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_services, container, false);

        servicesList = view.findViewById(R.id.services_list);

        servicesList.setHasFixedSize(true);
        LinearLayoutManager servicesListLinearLayoutManager = new LinearLayoutManager(getActivity());
        servicesList.setLayoutManager(servicesListLinearLayoutManager);

        allServices = (ArrayList<Service>) salon.getSalonBookingDetails().get("allServices");
        salonServicesListAdapter = new SalonServicesListAdapter(allServices);
        servicesList.setAdapter(salonServicesListAdapter);

        return view;
    }

}
