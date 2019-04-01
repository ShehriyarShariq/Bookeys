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

    ArrayList<Appointment> currentBookings, pastBookings;

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
                        for(DataSnapshot pastBooking : customerDetails.getChildren()){
                            if(!pastBooking.getKey().equals("bookingID")){
                                final ArrayList<HashMap<String, String>> servicesAvailed = new ArrayList<>();
                                final HashMap<String, String> bookingDetails = new HashMap<>();
                                for(DataSnapshot bookingDetail : pastBooking.getChildren()){
                                    if(bookingDetail.getKey().equals("servicesAvailed")){
                                        for(DataSnapshot service : bookingDetail.getChildren()){
                                            HashMap<String, String> serviceDetailsMap = new HashMap<>();
                                            for(DataSnapshot serviceDetail : service.getChildren()){
                                                serviceDetailsMap.put(serviceDetail.getKey(), serviceDetail.getValue().toString());
                                            }
                                            servicesAvailed.add(serviceDetailsMap);
                                        }
                                    } else {
                                        bookingDetails.put(bookingDetail.getKey(), bookingDetail.getValue().toString());
                                    }
                                }

                                bookingDetails.put("bookingID", pastBooking.getKey());

                                final String[] salonName = {""};
                                final String[] salonContactNum = {""};
                                firebaseDatabase.child("Salons").child(bookingDetails.get("salonID")).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot salonDetail : dataSnapshot.getChildren()){
                                            if(salonDetail.getKey().equals("name")){
                                                salonName[0] = dataSnapshot.getValue().toString();
                                            } else if(salonDetail.getKey().equals("contactNum")){
                                                salonContactNum[0] = dataSnapshot.getValue().toString();
                                            }
                                        }

                                        Appointment appointment = new Appointment(
                                                bookingDetails.get("bookingID"),
                                                bookingDetails.get("amount"),
                                                bookingDetails.get("dateAndTime"),
                                                bookingDetails.get("rating"),
                                                bookingDetails.get("review"),
                                                bookingDetails.get("barberID"),
                                                bookingDetails.get("barberName"),
                                                bookingDetails.get("salonID"),
                                                salonName[0],
                                                bookingDetails.get("status"),
                                                servicesAvailed,
                                                salonContactNum[0]
                                        );
                                        pastBookings.add(appointment);

                                        currentAndPastAppointmentsPager.setAdapter(new CurrentAndPastBookingsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), currentBookings, pastBookings));
                                        loaderDialog.hideDialog();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    } else if(customerDetails.getKey().equals("currentBooking")){
                        for(DataSnapshot currentBooking : customerDetails.getChildren()){
                            if(!currentBooking.getKey().equals("bookingID")){
                                final ArrayList<HashMap<String, String>> servicesAvailed = new ArrayList<>();
                                final HashMap<String, String> bookingDetails = new HashMap<>();

                                for(DataSnapshot bookingDetail : currentBooking.getChildren()){
                                    if(bookingDetail.getKey().equals("servicesAvailed")){
                                        for(DataSnapshot service : bookingDetail.getChildren()){
                                            HashMap<String, String> serviceDetailsMap = new HashMap<>();
                                            for(DataSnapshot serviceDetail : service.getChildren()){
                                                serviceDetailsMap.put(serviceDetail.getKey(), serviceDetail.getValue().toString());
                                            }
                                            servicesAvailed.add(serviceDetailsMap);
                                        }
                                    } else {
                                        bookingDetails.put(bookingDetail.getKey(), bookingDetail.getValue().toString());
                                    }
                                }

                                bookingDetails.put("bookingID", currentBooking.getKey());

                                final String[] salonName = {""};
                                final String[] salonContactNum = {""};
                                firebaseDatabase.child("Salons").child(bookingDetails.get("salonID")).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot salonDetail : dataSnapshot.getChildren()){
                                            if(salonDetail.getKey().equals("name")){
                                                salonName[0] = salonDetail.getValue().toString();
                                            } else if(salonDetail.getKey().equals("contactNum")){
                                                salonContactNum[0] = salonDetail.getValue().toString();
                                            }
                                        }

                                        Appointment appointment = new Appointment(
                                                bookingDetails.get("bookingID"),
                                                bookingDetails.get("amount"),
                                                bookingDetails.get("dateAndTime"),
                                                bookingDetails.get("rating"),
                                                bookingDetails.get("review"),
                                                bookingDetails.get("barberID"),
                                                bookingDetails.get("barberName"),
                                                bookingDetails.get("salonID"),
                                                salonName[0],
                                                bookingDetails.get("status"),
                                                servicesAvailed,
                                                salonContactNum[0]
                                        );
                                        currentBookings.add(appointment);

                                        currentAndPastAppointmentsPager.setAdapter(new CurrentAndPastBookingsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), currentBookings, pastBookings));
                                        loaderDialog.hideDialog();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                }

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
