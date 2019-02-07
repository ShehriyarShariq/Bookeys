package com.studio.millionares.barberbooker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MyAppointmentsActivity extends AppCompatActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager currentAndPastAppointmentsPager;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    LoaderDialog loaderDialog;

    ArrayList<HashMap<String, Object>> currentBookings, pastBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        currentAndPastAppointmentsPager = findViewById(R.id.current_and_past_appointments_pager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tabLayout.addTab(tabLayout.newTab().setText("Current"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));

        loaderDialog = new LoaderDialog(this, "InfoLoader");
        loaderDialog.showDialog();

        currentBookings = new ArrayList<>();
        pastBookings = new ArrayList<>();

        firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot customerDetails : dataSnapshot.getChildren()){
                    if(customerDetails.getKey().equals("bookingsHistory")){

                    } else if(customerDetails.getKey().equals("currentBooking")){

                    }
                }

                currentBookings.add(new HashMap<String, Object>());
                currentBookings.add(new HashMap<String, Object>());

                pastBookings.add(new HashMap<String, Object>());
                pastBookings.add(new HashMap<String, Object>());
                pastBookings.add(new HashMap<String, Object>());
                pastBookings.add(new HashMap<String, Object>());
                pastBookings.add(new HashMap<String, Object>());

                loaderDialog.hideDialog();

                currentAndPastAppointmentsPager.setAdapter(new CurrentAndPastBookingsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), currentBookings, pastBookings));

                currentAndPastAppointmentsPager.setCurrentItem(0);
                currentAndPastAppointmentsPager.setSaveFromParentEnabled(false);
                currentAndPastAppointmentsPager.setOffscreenPageLimit(tabLayout.getTabCount() - 1);

                currentAndPastAppointmentsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                currentAndPastAppointmentsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int i) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        currentAndPastAppointmentsPager.setCurrentItem(tab.getPosition(), true);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;

    }
}
