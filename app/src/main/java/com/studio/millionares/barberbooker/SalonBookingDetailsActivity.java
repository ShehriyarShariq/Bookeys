package com.studio.millionares.barberbooker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class SalonBookingDetailsActivity extends AppCompatActivity{

    private static final int SERVICE_COST = 10;

    android.support.v7.widget.Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    CardView makeBookingBtn, cancelBtn, nextBtn, finishBookingBtn, datePickerBtn, timePickerBtn;
    RelativeLayout originalLayout, bookingLayout, servicesSelectionLayout, datePicker, timePicker, getDirectionsBtnLayout;
    ScrollView timeSelectionLayout, barberSelectionLayout, finalReceiptLayout;
    TextView salonName, salonPlaceHolderName, salonRating, bookingDate, bookingTime, expectedTime, netTotal, serviceTax, totalCost, selectedDate, selectedTime, selectedBarberTxt;
    RecyclerView bookingServicesList, availableTimeSlotsList, selectedServicesList, barberSelectionList;
    ImageView backToServicesSelectionBtn, backToTimeSelectionBtn, backToBarberSelectionBtn;
    LinearLayout cancelAndNextBtnLayout;
    ImageButton addToFavouritesBtn, removeFromFavouritesBtn;

    String salonIDStr, salonNameStr, salonRatingStr;
    LatLng salonLocation;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;


    private HashMap<String, Object> salonWorkingHoursAndDays;
    private ArrayList<HashMap<String, String>> bookedTimeSlots;
    private ArrayList<String> closedDays;
    private ArrayList<Barber> allBarbers;

    ArrayList<Service> allServices, tempAllServices;
    BookingServicesListAdapter bookingServicesListAdapter;

    ArrayList<HashMap<String, String>> rawAvailableTimeSlots;
    AvailableTimeSlotsListAdapter availableTimeSlotsListAdapter;

    BarberSelectionListAdapter barberSelectionListAdapter;

    int selectionTimeInMins;

    Salon currentSalon;

    LoaderDialog loaderDialog;

    Calendar mCalendar;
    String selectedBookingDate;
    String selectedBookingStartTime, selectedBookingEndTime;
    Barber selectedBarber;
    ArrayList<Service> allSelectedServices;
    ArrayList<HashMap<String, String>> allBookedTimeSlots;
    int netCost, totalCostVal;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_booking_details);

        mActivity = this;

        // Get previously loaded salon details
        Intent salonIntent = getIntent();
        salonIDStr = salonIntent.getStringExtra("id");
        salonNameStr = salonIntent.getStringExtra("name");
        salonRatingStr = salonIntent.getStringExtra("rating");
        salonLocation = new LatLng(salonIntent.getDoubleExtra("latitude", 0), salonIntent.getDoubleExtra("longitude", 0));

        // Firebase database and auth instances
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        /* UI Views */
        toolbar = findViewById(R.id.toolbar);
        // Services, Reviews, Profile viewpager screen
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.fragments_holder);
        makeBookingBtn = findViewById(R.id.make_booking_btn);
        originalLayout = findViewById(R.id.original_layout);
        salonName = findViewById(R.id.saloon_name_txt);
        salonPlaceHolderName = findViewById(R.id.saloon_name_placeholder_txt);
        salonRating = findViewById(R.id.rating_txt);
        addToFavouritesBtn = findViewById(R.id.add_to_favourites_btn);
        removeFromFavouritesBtn = findViewById(R.id.remove_from_favourites_btn);
        getDirectionsBtnLayout = findViewById(R.id.get_directions_btn_layout);
        // Booking layout
        bookingLayout = findViewById(R.id.booking_layout);
        cancelBtn = findViewById(R.id.cancel_btn);
        nextBtn = findViewById(R.id.next_btn);
        servicesSelectionLayout = findViewById(R.id.service_selection_layout);
        timeSelectionLayout = findViewById(R.id.time_selection_layout);
        availableTimeSlotsList = findViewById(R.id.available_time_slots_list);
        backToServicesSelectionBtn = findViewById(R.id.back_btn);
        finalReceiptLayout = findViewById(R.id.final_receipt_layout);
        backToBarberSelectionBtn = findViewById(R.id.back_to_barber_selection_btn);
        finishBookingBtn = findViewById(R.id.finish_booking_btn);
        bookingDate = findViewById(R.id.booking_date);
        bookingTime = findViewById(R.id.booking_time);
        expectedTime = findViewById(R.id.expected_time);
        netTotal = findViewById(R.id.net_total);
        serviceTax = findViewById(R.id.service_tax);
        totalCost = findViewById(R.id.total_cost);
        selectedServicesList = findViewById(R.id.selected_services_list);
        cancelAndNextBtnLayout = findViewById(R.id.cancel_next_btn_layout);
        barberSelectionList = findViewById(R.id.barber_selection_list);
        backToTimeSelectionBtn = findViewById(R.id.back_to_time_selection_btn);
        datePickerBtn = findViewById(R.id.date_picker_layout);
        timePickerBtn = findViewById(R.id.time_picker_layout);
        selectedDate = findViewById(R.id.selected_date);
        selectedTime = findViewById(R.id.selected_start_time);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        barberSelectionLayout = findViewById(R.id.barber_selection_layout);
        selectedBarberTxt = findViewById(R.id.selected_barber);

        // Set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        salonName.setText(salonNameStr);
        salonPlaceHolderName.setText(salonNameStr);
        salonRating.setText(salonRatingStr);

        allServices = new ArrayList<>();
        salonWorkingHoursAndDays = new HashMap<>();
        bookedTimeSlots = new ArrayList<>();
        closedDays = new ArrayList<>();
        allBarbers = new ArrayList<>();

        allSelectedServices = new ArrayList<>();

        // Loader dialog
        loaderDialog = new LoaderDialog(this, "InfoLoader");
        loaderDialog.showDialog();

        // For getting all required salon booking data
        firebaseDatabase.child("Salons").child(salonIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot details : dataSnapshot.getChildren()) {
                    if (details.getKey().equals("allServices")) { // All services provided by a Salon
                        String id, cost = "", expectedTime = "", name = "";
                        ArrayList<String> byBarbers = new ArrayList<>();
                        for (DataSnapshot service : details.getChildren()) {
                            if (service.getKey().equals("serviceID")) {
                                continue;
                            }

                            id = service.getKey();
                            for (DataSnapshot serviceDetail : service.getChildren()) {
                                if (serviceDetail.getKey().equals("name")) { // Service description
                                    name = serviceDetail.getValue().toString();
                                } else if (serviceDetail.getKey().equals("cost")) { // Cost of service
                                    cost = serviceDetail.getValue().toString();
                                } else if (serviceDetail.getKey().equals("expectedTime")) { // Expected time span in minutes
                                    expectedTime = serviceDetail.getValue().toString();
                                } else if (serviceDetail.getKey().equals("byBarbers")) { // Service by barbers
                                    for (DataSnapshot barber : serviceDetail.getChildren()) {
                                        byBarbers.add(barber.getKey());
                                    }
                                }
                            }

                            allServices.add(new Service(id, name, cost, expectedTime, byBarbers));
                        }
                    } else if (details.getKey().equals("barbers")) { // Salon barbers
                        for (DataSnapshot barber : details.getChildren()) {
                            if(barber.getKey().equals("barberID")){ // Skip for placeholder barberID
                                continue;
                            }

                            String id, name = null, rating = null, imageURL = null;
                            HashMap<String, HashMap<String, Object>> workingHours = new HashMap<>();
                            ArrayList<HashMap<String, String>> barberBookings = new ArrayList<>();

                            id = barber.getKey();
                            for (DataSnapshot barberDetails : barber.getChildren()) {
                                if (barberDetails.getKey().equals("name")) { // Barber name
                                    name = barberDetails.getValue().toString();
                                } else if (barberDetails.getKey().equals("rating")) { // Barber rating
                                    rating = barberDetails.getValue().toString();
                                } else if (barberDetails.getKey().equals("imageURL")) { // Barber image URL
                                    imageURL = barberDetails.getValue().toString();
                                } else if (barberDetails.getKey().equals("workingHours")) { // Barber Working Hours for all days of a week
                                    for(DataSnapshot day : barberDetails.getChildren()){
                                        HashMap<String, Object> dayDetailsMap = new HashMap<>();

                                        for(DataSnapshot dayDetails : day.getChildren()){
                                            if(dayDetails.getKey().equals("unavailableSlots")){ // Breaks on a certain day
                                                ArrayList<HashMap<String, String>> unavailableSlots = new ArrayList<>();
                                                for(DataSnapshot slot : dayDetails.getChildren()){
                                                    HashMap<String, String> slotTimings = new HashMap<>();
                                                    for(DataSnapshot timing : slot.getChildren()){
                                                        slotTimings.put(timing.getKey(), timing.getValue().toString());
                                                    }
                                                    unavailableSlots.add(slotTimings);
                                                }
                                                dayDetailsMap.put("breaks", unavailableSlots);
                                            } else {
                                                // Shift start and end time
                                                dayDetailsMap.put(dayDetails.getKey(), dayDetails.getValue().toString());
                                            }
                                        }

                                        workingHours.put(day.getKey(), dayDetailsMap);
                                    }
                                } else if(barberDetails.getKey().equals("bookedTimeSlots")){ // Barber booked time slots
                                    for(DataSnapshot booking : barberDetails.getChildren()){
                                        HashMap<String, String> bookingTimings = new HashMap<>();
                                        bookingTimings.put("id", booking.getKey());
                                        for (DataSnapshot time : booking.getChildren()) {
                                            bookingTimings.put(time.getKey(), time.getValue().toString());
                                        }

                                        barberBookings.add(bookingTimings);
                                    }
                                }
                            }

                            allBarbers.add(new Barber(id, name, rating, imageURL, workingHours, barberBookings));
                        }
                    } else if (details.getKey().equals("bookedTimeSlots")) { // Salon booked time slots
                        for (DataSnapshot booking : details.getChildren()) {
                            HashMap<String, String> bookingDetails = new HashMap<>();
                            bookingDetails.put("id", booking.getKey());

                            for (DataSnapshot detail : booking.getChildren()) { // Customer ID and Barber ID for every booking
                                bookingDetails.put(detail.getKey(), detail.getValue().toString());
                            }

                            bookedTimeSlots.add(bookingDetails);
                        }
                    } else if (details.getKey().equals("closedDays")) { // Salon closed days
                        for (DataSnapshot day : details.getChildren()) {
                            closedDays.add(day.getKey());
                        }
                    } else if (details.getKey().equals("workingDays")) { // Open days of the week for the Salon
                        salonWorkingHoursAndDays.put(details.getKey(), details.getValue().toString());
                    } else if (details.getKey().equals("workingHours")) { // Salon working hours for all days of the week
                        ArrayList<HashMap<String, Object>> daysWorkingHours = new ArrayList<>();
                        for (DataSnapshot day : details.getChildren()) {
                            HashMap<String, Object> dayDetails = new HashMap<>();
                            dayDetails.put("id", day.getKey()); // Day id
                            for (DataSnapshot timeType : day.getChildren()) {
                                if (timeType.getKey().equals("breaks")) { // Breaks on a certain day
                                    ArrayList<HashMap<String, String>> breakSlots = new ArrayList<>();
                                    for (DataSnapshot breakSlot : timeType.getChildren()) {
                                        HashMap<String, String> breakSlotTimings = new HashMap<>();
                                        for (DataSnapshot time : breakSlot.getChildren()) {
                                            breakSlotTimings.put(time.getKey(), time.getValue().toString());
                                        }
                                        breakSlots.add(breakSlotTimings);
                                    }

                                    dayDetails.put("breaks", breakSlots);
                                } else {
                                    dayDetails.put(timeType.getKey(), timeType.getValue().toString());
                                }
                            }
                            daysWorkingHours.add(dayDetails);
                        }
                        salonWorkingHoursAndDays.put("daysWorkingHours", daysWorkingHours);
                    }
                }

                currentSalon = new Salon(salonIDStr, allServices, allBarbers, bookedTimeSlots, closedDays, salonWorkingHoursAndDays);
                loaderDialog.hideDialog(); // Hide loader dialog once all data is loaded

                // Tabs on main page
                tabLayout.addTab(tabLayout.newTab().setText("Services"));
                tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
                tabLayout.addTab(tabLayout.newTab().setText("Profile"));

                viewPager.setAdapter(new SalonProfilePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), currentSalon));

                viewPager.setCurrentItem(0);
                viewPager.setSaveFromParentEnabled(false);
                viewPager.setOffscreenPageLimit(tabLayout.getTabCount() - 1);
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        //Toast.makeText(getContext(), "Changed! " + position, Toast.LENGTH_SHORT).show();
                        if (position == 0) {
                            // Services
                        } else if (position == 1) {
                            // Reviews
                        } else {
                            // Profile
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition(), true);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

                // Booking Services Selection
                bookingServicesList = findViewById(R.id.booking_services_list);

                allServices = (ArrayList<Service>) currentSalon.getSalonBookingDetails().get("allServices");

                bookingServicesList.setHasFixedSize(true);
                LinearLayoutManager servicesListLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
                bookingServicesList.setLayoutManager(servicesListLinearLayoutManager);

                bookingServicesListAdapter = new BookingServicesListAdapter(allServices);
                bookingServicesList.setAdapter(bookingServicesListAdapter);

                // Booking Time Selection
                bookedTimeSlots = (ArrayList<HashMap<String, String>>) currentSalon.getSalonBookingDetails().get("bookedTimeSlots");
                salonWorkingHoursAndDays = (HashMap<String, Object>) currentSalon.getSalonBookingDetails().get("workingHoursAndDays");
                rawAvailableTimeSlots = new ArrayList<>();

                availableTimeSlotsList.setHasFixedSize(true);
                LinearLayoutManager availableTimeSlotsListLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                availableTimeSlotsList.setLayoutManager(availableTimeSlotsListLinearLayoutManager);

                availableTimeSlotsListAdapter = new AvailableTimeSlotsListAdapter(rawAvailableTimeSlots);
                availableTimeSlotsList.setAdapter(availableTimeSlotsListAdapter);

                // Booking Barber Selection
                barberSelectionList.setHasFixedSize(true);
                LinearLayoutManager barberSelectionListLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                barberSelectionList.setLayoutManager(barberSelectionListLinearLayoutManager);

                final ArrayList<Barber> availableSalonBarbers = new ArrayList<>();
                // For none selection
                availableSalonBarbers.add(new Barber("none", "none", "none", "none", new HashMap<String, HashMap<String, Object>>(), new ArrayList<HashMap<String, String>>()));

                availableSalonBarbers.addAll(allBarbers);

                barberSelectionListAdapter = new BarberSelectionListAdapter(availableSalonBarbers, new BarberSelectionRecyclerViewListClickListener() {
                    @Override
                    public void barberSelectionRecyclerViewListClicked(int position) {
                        // Click listener for the barber selection
                        selectedBarber = availableSalonBarbers.get(position);

                        if(selectedBarber.getBarberDetails().get("name").equals("none")){
                            selectedBarber = availableSalonBarbers.get(1);
                        }

                        barberSelectionLayout.setVisibility(View.GONE);
                        finalReceiptLayout.setVisibility(View.VISIBLE);
                        finishBookingBtn.setVisibility(View.VISIBLE);

                        bookingDate.setText(selectedBookingDate);
                        bookingTime.setText(fixTimeStamp(selectedBookingStartTime) + " to " + fixTimeStamp(selectedBookingEndTime));
                        expectedTime.setText(getTimeStringFromMins(selectionTimeInMins));
                        selectedBarberTxt.setText(selectedBarber.getBarberDetails().get("name").toString());

                        SelectedServicesListAdapter selectedServicesListAdapter = new SelectedServicesListAdapter(allSelectedServices);
                        selectedServicesList.setAdapter(selectedServicesListAdapter);

                        netCost = 0;
                        for (Service service : allSelectedServices) {
                            netCost += Double.parseDouble(service.getServiceDetails().get("cost").toString());
                        }

                        totalCostVal = netCost + SERVICE_COST;

                        netTotal.setText("R.s " + netCost + "/-");
                        serviceTax.setText("R.s " + SERVICE_COST + "/-");
                        totalCost.setText("R.s " + totalCostVal + "/-");
                    }
                });
                barberSelectionList.setAdapter(barberSelectionListAdapter);

                // Final receipt
                selectedServicesList.setHasFixedSize(true);
                LinearLayoutManager selectedServicesListLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
                selectedServicesList.setLayoutManager(selectedServicesListLinearLayoutManager);

                getDirectionsBtnLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Parses URL for directions from current location to salon coordinates
                        String directionsURL = "https://www.google.com/maps/dir/?api=1&destination=" + salonLocation.latitude + "," + salonLocation.longitude;
                        Uri directionsURI = Uri.parse(directionsURL);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, directionsURI);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent); // Open google maps app
                    }
                });

                addToFavouritesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Add salon to favourites list

                        removeFromFavouritesBtn.setVisibility(View.VISIBLE);
                        addToFavouritesBtn.setVisibility(View.GONE);
                    }
                });

                removeFromFavouritesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove salon from favourites list

                        addToFavouritesBtn.setVisibility(View.VISIBLE);
                        removeFromFavouritesBtn.setVisibility(View.GONE);
                    }
                });

                makeBookingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Booking start button

                        originalLayout.setVisibility(View.GONE);
                        bookingLayout.setVisibility(View.VISIBLE);
                        servicesSelectionLayout.setVisibility(View.VISIBLE);
                        timeSelectionLayout.setVisibility(View.GONE);
                        finalReceiptLayout.setVisibility(View.GONE);

                        // To reset previously selected services
                        if (tempAllServices != null) {
                            if (tempAllServices.size() != 0) {
                                allServices.addAll(tempAllServices);
                                bookingServicesListAdapter.notifyDataSetChanged();
                                tempAllServices.clear();
                            }
                        }
                    }
                });

                backToServicesSelectionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Back button to go back to services selection layout from time selection layout

                        servicesSelectionLayout.setVisibility(View.VISIBLE);
                        timeSelectionLayout.setVisibility(View.GONE);

                        // Reset time selection layout and previously selected time/Date
                        selectedDate.setText("Select Date");
                        selectedTime.setText("Select Time");
                        selectedBookingDate = null;
                        selectedBookingStartTime = null;
                        timePicker.setVisibility(View.GONE);
                        availableTimeSlotsListAdapter.refreshDataset(new ArrayList<HashMap<String, String>>());
                    }
                });

                backToTimeSelectionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Back button to go back to time selection layout from barber selection layout

                        barberSelectionLayout.setVisibility(View.GONE);
                        timeSelectionLayout.setVisibility(View.VISIBLE);
                        cancelAndNextBtnLayout.setVisibility(View.VISIBLE);

                        selectedBarber = null;
                        barberSelectionListAdapter.refreshDataset(new ArrayList<Barber>());
                    }
                });

                backToBarberSelectionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Back button to go back to barber selection layout from final receipt layout

                        finalReceiptLayout.setVisibility(View.GONE);
                        barberSelectionLayout.setVisibility(View.VISIBLE);
                        finishBookingBtn.setVisibility(View.GONE);
                    }
                });

                datePickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Date picker

                        mCalendar = Calendar.getInstance();

                        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                        int month = mCalendar.get(Calendar.MONTH);
                        int year = mCalendar.get(Calendar.YEAR);

                        // If no date selected then set selected date in dialog to current date
                        if (selectedBookingDate != null) {
                            HashMap<String, String> parsedDate = parseDate(selectedBookingDate);
                            day = Integer.parseInt(parsedDate.get("dayDate"));
                            month = Integer.parseInt(parsedDate.get("month")) - 1;
                            year = Integer.parseInt(parsedDate.get("year"));
                        }

                        new DatePickerDialog(mActivity, mDateSet, year, month, day).show();
                    }
                });

                timePickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Time picker

                        mCalendar = Calendar.getInstance();

                        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                        int minute = mCalendar.get(Calendar.MINUTE);

                        // If no time selected previously then set selected time in dialog to current time
                        if(selectedBookingStartTime != null){
                            String[] timeSplit = selectedBookingStartTime.split(":");
                            hour = Integer.parseInt(timeSplit[0]);
                            minute = Integer.parseInt(timeSplit[1]);
                        }

                        new TimePickerDialog(mActivity, mTimeSet, hour, minute, false).show();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Cancel button

                        bookingLayout.setVisibility(View.GONE);
                        originalLayout.setVisibility(View.VISIBLE);
                        bookingServicesList.removeAllViewsInLayout();
                        cancelAndNextBtnLayout.setVisibility(View.VISIBLE);
                        finishBookingBtn.setVisibility(View.GONE);

                        // Clear selected services
                        tempAllServices = new ArrayList<>();
                        tempAllServices.addAll(allServices);
                        allServices.clear();
                        allSelectedServices.clear();
                        bookingServicesListAdapter.notifyDataSetChanged();
                        bookingServicesListAdapter.clearSelectedServices();

                        servicesSelectionLayout.setVisibility(View.VISIBLE);
                        timeSelectionLayout.setVisibility(View.GONE);
                        barberSelectionLayout.setVisibility(View.GONE);
                        finalReceiptLayout.setVisibility(View.GONE);

                        // Set selected date and time to null
                        selectedBookingDate = null;
                        selectedBookingStartTime = null;
                        selectedDate.setText("Select Date");
                        selectedTime.setText("Select Time");
                        timePicker.setVisibility(View.GONE);

                        // Clear available time slot list
                        rawAvailableTimeSlots.clear();
                        availableTimeSlotsListAdapter.refreshDataset(rawAvailableTimeSlots);
                    }
                });

                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Move to next step button
                        if (servicesSelectionLayout.getVisibility() == View.VISIBLE) {
                            // Move from the service selection to time selection layout
                            if (bookingServicesListAdapter.getAllSelectedServices().size() == 0) { // No services selected
                                Toast.makeText(getApplicationContext(), "No services selected!", Toast.LENGTH_SHORT).show();
                            } else {
                                timeSelectionLayout.setVisibility(View.VISIBLE);
                                servicesSelectionLayout.setVisibility(View.GONE);

                                allSelectedServices.clear();
                                allSelectedServices.addAll(bookingServicesListAdapter.getAllSelectedServices());

                                selectionTimeInMins = 0;

                                for (Service service : allSelectedServices) {
                                    selectionTimeInMins += getTotalMinsFromTimeStamp(service.getServiceDetails().get("expectedTime").toString());
                                }
                            }
                        } else if (timeSelectionLayout.getVisibility() == View.VISIBLE) {
                            if(selectedBookingStartTime == null){
                                Toast.makeText(getApplicationContext(), "Time not specified...", Toast.LENGTH_SHORT).show();
                            } else {
                                timeSelectionLayout.setVisibility(View.GONE);
                                barberSelectionLayout.setVisibility(View.VISIBLE);
                                cancelAndNextBtnLayout.setVisibility(View.GONE);

                                ArrayList<Barber> filteredBarbersList = new ArrayList<>();
                                filteredBarbersList.add(new Barber("none", "none", "none", "none", new HashMap<String, HashMap<String, Object>>(), new ArrayList<HashMap<String, String>>()));

                                for(Barber barber : allBarbers){
                                    ArrayList<HashMap<String, String>> barberAvailableTimeSlots = barberAvailableSlots(barber, bookedTimeSlots, selectedBookingDate);
                                    for(HashMap<String, String> slot : barberAvailableTimeSlots){
                                        if((compareTimeStrings(selectedBookingStartTime, slot.get("startTime")) != -1) && (compareTimeStrings(selectedBookingEndTime, slot.get("endTime")) != 1)){
                                            filteredBarbersList.add(barber);
                                            break;
                                        }
                                    }
                                }

                                barberSelectionListAdapter.refreshDataset(filteredBarbersList);
                            }
                        }
                    }
                });

                finishBookingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final LoaderDialog bookingCompletionLoader = new LoaderDialog(mActivity, "BookingCompletion");
                        bookingCompletionLoader.showDialog();

                        // To check if no booking has already been made for the selected time slot
                        firebaseDatabase.child("Salons").child(salonIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<Barber> tempBarbers = new ArrayList<>();
                                ArrayList<HashMap<String, String>> tempSalonAllBookedSlots = new ArrayList<>();
                                HashMap<String, Object> tempSalonWorkingHoursAndDays = new HashMap<>();

                                for (DataSnapshot details : dataSnapshot.getChildren()) {
                                    if (details.getKey().equals("barbers")) { // Salon barbers
                                        for (DataSnapshot barber : details.getChildren()) {
                                            if(barber.getKey().equals("barberID")){ // Skip for placeholder barberID
                                                continue;
                                            }

                                            String id = barber.getKey();
                                            HashMap<String, HashMap<String, Object>> workingHours = new HashMap<>();
                                            ArrayList<HashMap<String, String>> barberBookings = new ArrayList<>();

                                            for (DataSnapshot barberDetails : barber.getChildren()) {
                                                if (barberDetails.getKey().equals("workingHours")) { // Barber Working Hours for all days of a week
                                                    for(DataSnapshot day : barberDetails.getChildren()){
                                                        HashMap<String, Object> dayDetailsMap = new HashMap<>();

                                                        for(DataSnapshot dayDetails : day.getChildren()){
                                                            if(dayDetails.getKey().equals("unavailableSlots")){ // Breaks on a certain day
                                                                ArrayList<HashMap<String, String>> unavailableSlots = new ArrayList<>();
                                                                for(DataSnapshot slot : dayDetails.getChildren()){
                                                                    HashMap<String, String> slotTimings = new HashMap<>();
                                                                    for(DataSnapshot timing : slot.getChildren()){
                                                                        slotTimings.put(timing.getKey(), timing.getValue().toString());
                                                                    }
                                                                    unavailableSlots.add(slotTimings);
                                                                }
                                                                dayDetailsMap.put("breaks", unavailableSlots);
                                                            } else {
                                                                // Shift start and end time
                                                                dayDetailsMap.put(dayDetails.getKey(), dayDetails.getValue().toString());
                                                            }
                                                        }

                                                        workingHours.put(day.getKey(), dayDetailsMap);
                                                    }
                                                } else if(barberDetails.getKey().equals("bookedTimeSlots")){ // Barber booked time slots
                                                    for(DataSnapshot booking : barberDetails.getChildren()){
                                                        HashMap<String, String> bookingTimings = new HashMap<>();
                                                        bookingTimings.put("id", booking.getKey());
                                                        for (DataSnapshot time : booking.getChildren()) {
                                                            bookingTimings.put(time.getKey(), time.getValue().toString());
                                                        }

                                                        barberBookings.add(bookingTimings);
                                                    }
                                                }
                                            }

                                            tempBarbers.add(new Barber(id, "", "", "", workingHours, barberBookings));
                                        }
                                    } else if (details.getKey().equals("bookedTimeSlots")) { // Salon booked time slots
                                        for (DataSnapshot booking : details.getChildren()) {
                                            HashMap<String, String> bookingTimings = new HashMap<>();
                                            bookingTimings.put("id", booking.getKey());

                                            for (DataSnapshot barberID : booking.getChildren()) { // Booking ID and barber ID for every booking
                                                bookingTimings.put(barberID.getKey(), barberID.getValue().toString());
                                            }

                                            tempSalonAllBookedSlots.add(bookingTimings);
                                        }
                                    } else if (details.getKey().equals("workingDays")) { // Open days of the week for the Salon
                                        tempSalonWorkingHoursAndDays.put(details.getKey(), details.getValue().toString());
                                    } else if (details.getKey().equals("workingHours")) { // Salon working hours for all days of the week
                                        ArrayList<HashMap<String, Object>> daysWorkingHours = new ArrayList<>();
                                        for (DataSnapshot day : details.getChildren()) {
                                            HashMap<String, Object> dayDetails = new HashMap<>();
                                            dayDetails.put("id", day.getKey()); // Day id
                                            for (DataSnapshot timeType : day.getChildren()) {
                                                if (timeType.getKey().equals("breaks")) { // Breaks on a certain day
                                                    ArrayList<HashMap<String, String>> breakSlots = new ArrayList<>();
                                                    for (DataSnapshot breakSlot : timeType.getChildren()) {
                                                        HashMap<String, String> breakSlotTimings = new HashMap<>();
                                                        for (DataSnapshot time : breakSlot.getChildren()) {
                                                            breakSlotTimings.put(time.getKey(), time.getValue().toString());
                                                        }
                                                        breakSlots.add(breakSlotTimings);
                                                    }

                                                    dayDetails.put("breaks", breakSlots);
                                                } else {
                                                    dayDetails.put(timeType.getKey(), timeType.getValue().toString());
                                                }
                                            }
                                            daysWorkingHours.add(dayDetails);
                                        }
                                        tempSalonWorkingHoursAndDays.put("daysWorkingHours", daysWorkingHours);
                                    }
                                }

                                final ArrayList<HashMap<String, String>> tempAllActualBookedTimeSlots = new ArrayList<>();
                                for(HashMap<String, String> booking : tempSalonAllBookedSlots){
                                    String barberID = booking.get("barberID");
                                    String bookingID = booking.get("id");
                                    for(Barber barber : tempBarbers){
                                        if(barber.getBarberDetails().get("id").toString().equals(barberID)){
                                            ArrayList<HashMap<String, String>> barberBookings = (ArrayList<HashMap<String, String>>) barber.getBarberDetails().get("bookings");
                                            for(HashMap<String, String> barberBooking : barberBookings){
                                                if(barberBooking.get("id").equals(bookingID)){
                                                    boolean areOtherBarbersFree = true;
                                                    for(Barber otherBarbers : allBarbers){
                                                        if(!otherBarbers.getBarberDetails().get("id").toString().equals(barberID)){
                                                            ArrayList<HashMap<String, String>> bookings = (ArrayList<HashMap<String, String>>) otherBarbers.getBarberDetails().get("bookings");
                                                            for(HashMap<String, String> otherBarberBooking : bookings){
                                                                if(otherBarberBooking.get("date").equals(barberBooking.get("date"))){
                                                                    if((compareTimeStrings(barberBooking.get("startTime"), otherBarberBooking.get("startTime")) != 1) && (compareTimeStrings(barberBooking.get("endTime"), otherBarberBooking.get("endTime")) != -1)){
                                                                        areOtherBarbersFree = true;
                                                                    } else {
                                                                        areOtherBarbersFree = false;
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }

                                                    if(areOtherBarbersFree){
                                                        tempAllActualBookedTimeSlots.add(barberBooking);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                ArrayList<HashMap<String, String>> allCurrentUpdatedSlots = getValidBookingSlots(availableTimeSlot(tempAllActualBookedTimeSlots, tempSalonWorkingHoursAndDays, selectedBookingDate), selectionTimeInMins);
                                boolean isTimeSelectionValid = false;

                                for(HashMap<String, String> slot : allCurrentUpdatedSlots){
                                    if((compareTimeStrings(selectedBookingStartTime, slot.get("startTime")) != -1) && (compareTimeStrings(selectedBookingEndTime, slot.get("endTime")) != 1)){
                                        isTimeSelectionValid = true;
                                    }
                                }

                                if(isTimeSelectionValid){
                                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                    final long pushKey = calendar.getTimeInMillis();
                                    firebaseDatabase.child("Salons").child(salonIDStr).child("bookedTimeSlots").child(String.valueOf(pushKey)).child("barberID").setValue(selectedBarber.getBarberDetails().get("id").toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                HashMap<String, String> bookingDetails = new HashMap<>();
                                                bookingDetails.put("date", selectedBookingDate);
                                                bookingDetails.put("endTime", selectedBookingEndTime);
                                                bookingDetails.put("startTime", selectedBookingStartTime);

                                                firebaseDatabase.child("Salons").child(salonIDStr).child("barbers").child(selectedBarber.getBarberDetails().get("id").toString()).child("bookedTimeSlots").child(String.valueOf(pushKey)).setValue(bookingDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            HashMap<String, Object> customerBookingMap = new HashMap<>();
                                                            customerBookingMap.put("amount", String.valueOf(totalCostVal));
                                                            customerBookingMap.put("barberID", selectedBarber.getBarberDetails().get("id").toString());
                                                            customerBookingMap.put("salonID", salonIDStr);

                                                            HashMap<String, HashMap<String, String>> bookingServices = new HashMap<>();

                                                            for(Service service : allSelectedServices){
                                                                HashMap<String, Object> serviceDetails = service.getServiceDetails();
                                                                HashMap<String, String> usefulDetailsMap = new HashMap<>();

                                                                usefulDetailsMap.put("cost", serviceDetails.get("cost").toString());
                                                                usefulDetailsMap.put("expectedTime", serviceDetails.get("expectedTime").toString());
                                                                usefulDetailsMap.put("name", serviceDetails.get("name").toString());

                                                                bookingServices.put(serviceDetails.get("id").toString(), usefulDetailsMap);
                                                            }

                                                            customerBookingMap.put("servicesAvailed", bookingServices);

                                                            firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("currentBooking").child(String.valueOf(pushKey)).setValue(customerBookingMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        bookingCompletionLoader.hideDialog();

                                                                        bookingLayout.setVisibility(View.GONE);
                                                                        originalLayout.setVisibility(View.VISIBLE);
                                                                        bookingServicesList.removeAllViewsInLayout();
                                                                        cancelAndNextBtnLayout.setVisibility(View.VISIBLE);
                                                                        finishBookingBtn.setVisibility(View.GONE);

                                                                        // Clear selected services
                                                                        tempAllServices = new ArrayList<>();
                                                                        tempAllServices.addAll(allServices);
                                                                        allServices.clear();
                                                                        bookingServicesListAdapter.notifyDataSetChanged();
                                                                        bookingServicesListAdapter.clearSelectedServices();

                                                                        servicesSelectionLayout.setVisibility(View.VISIBLE);
                                                                        timeSelectionLayout.setVisibility(View.GONE);
                                                                        barberSelectionLayout.setVisibility(View.GONE);
                                                                        finalReceiptLayout.setVisibility(View.GONE);

                                                                        // Set selected date and time to null
                                                                        selectedBookingDate = null;
                                                                        selectedBookingStartTime = null;
                                                                        selectedDate.setText("Select Date");
                                                                        selectedTime.setText("Select Time");
                                                                        timePicker.setVisibility(View.GONE);

                                                                        // Clear available time slot list
                                                                        rawAvailableTimeSlots.clear();
                                                                        availableTimeSlotsListAdapter.refreshDataset(rawAvailableTimeSlots);

                                                                        allBookedTimeSlots.clear();
                                                                        allBookedTimeSlots.addAll(tempAllActualBookedTimeSlots);

                                                                    } else {
                                                                        firebaseDatabase.child("Salons").child(salonIDStr).child("bookedTimeSlots").child(String.valueOf(pushKey)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                firebaseDatabase.child("Salons").child(salonIDStr).child("barbers").child(selectedBarber.getBarberDetails().get("id").toString()).child("bookedTimeSlots").child(String.valueOf(pushKey)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        bookingCompletionLoader.hideDialog();
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            firebaseDatabase.child("Salons").child(salonIDStr).child("bookedTimeSlots").child(String.valueOf(pushKey)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    bookingCompletionLoader.hideDialog();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            } else {

                                            }
                                        }
                                    });
                                } else {
                                    // Booking failed as slot already occupied
                                    bookingCompletionLoader.hideDialog();

                                    bookingLayout.setVisibility(View.GONE);
                                    originalLayout.setVisibility(View.VISIBLE);
                                    bookingServicesList.removeAllViewsInLayout();
                                    cancelAndNextBtnLayout.setVisibility(View.VISIBLE);
                                    finishBookingBtn.setVisibility(View.GONE);

                                    // Clear selected services
                                    tempAllServices = new ArrayList<>();
                                    tempAllServices.addAll(allServices);
                                    allServices.clear();
                                    bookingServicesListAdapter.notifyDataSetChanged();
                                    bookingServicesListAdapter.clearSelectedServices();

                                    servicesSelectionLayout.setVisibility(View.VISIBLE);
                                    timeSelectionLayout.setVisibility(View.GONE);
                                    barberSelectionLayout.setVisibility(View.GONE);
                                    finalReceiptLayout.setVisibility(View.GONE);

                                    // Set selected date and time to null
                                    selectedBookingDate = null;
                                    selectedBookingStartTime = null;
                                    selectedDate.setText("Select Date");
                                    selectedTime.setText("Select Time");
                                    timePicker.setVisibility(View.GONE);

                                    // Clear available time slot list
                                    rawAvailableTimeSlots.clear();
                                    availableTimeSlotsListAdapter.refreshDataset(rawAvailableTimeSlots);

                                    allBookedTimeSlots.clear();
                                    allBookedTimeSlots.addAll(tempAllActualBookedTimeSlots);
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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

    private final DatePickerDialog.OnDateSetListener mDateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dayFixed = (dayOfMonth) < 10 ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
            String monthFixed = (month + 1) < 10 ? "0" + String.valueOf(month + 1) : String.valueOf(month + 1);

            selectedBookingDate = dayFixed + "-" + monthFixed + "-" + year;

            rawAvailableTimeSlots.clear();

            if(allBookedTimeSlots == null){
                allBookedTimeSlots = new ArrayList<>();
                for(HashMap<String, String> booking : bookedTimeSlots){
                    String barberID = booking.get("barberID");
                    String bookingID = booking.get("id");
                    for(Barber barber : allBarbers){
                        if(barber.getBarberDetails().get("id").toString().equals(barberID)){
                            ArrayList<HashMap<String, String>> barberBookings = (ArrayList<HashMap<String, String>>) barber.getBarberDetails().get("bookings");
                            for(HashMap<String, String> barberBooking : barberBookings){
                                if(barberBooking.get("id").equals(bookingID)){
                                    boolean areOtherBarbersFree = true;
                                    for(Barber otherBarbers : allBarbers){
                                        if(!otherBarbers.getBarberDetails().get("id").toString().equals(barberID)){
                                            ArrayList<HashMap<String, String>> bookings = (ArrayList<HashMap<String, String>>) otherBarbers.getBarberDetails().get("bookings");
                                            for(HashMap<String, String> otherBarberBooking : bookings){
                                                if((compareTimeStrings(barberBooking.get("startTime"), otherBarberBooking.get("startTime")) != 1) && (compareTimeStrings(barberBooking.get("endTime"), otherBarberBooking.get("endTime")) != -1)){
                                                    areOtherBarbersFree = true;
                                                    //break;
                                                } else {
                                                    areOtherBarbersFree = false;
                                                }
                                            }
                                        }

                                    }

                                    if(areOtherBarbersFree){
                                        allBookedTimeSlots.add(barberBooking);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            rawAvailableTimeSlots.addAll(getValidBookingSlots(availableTimeSlot(allBookedTimeSlots, salonWorkingHoursAndDays, selectedBookingDate), selectionTimeInMins));
            availableTimeSlotsListAdapter.refreshDataset(rawAvailableTimeSlots);

            if(rawAvailableTimeSlots.size() == 0){
                selectedDate.setText("Select Date");
                timePicker.setVisibility(View.GONE);

                selectedBookingDate = null;
            } else {
                timePicker.setVisibility(View.VISIBLE);

                selectedBookingStartTime = null;

                selectedDate.setText(selectedBookingDate);
                selectedTime.setText("Select Time");
            }
        }
    };

    private final TimePickerDialog.OnTimeSetListener mTimeSet = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);

            String hourFixed = (hourOfDay < 10) ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
            String minuteFixed = (minute < 10) ? "0" + String.valueOf(minute) : String.valueOf(minute);

            int endTimeMins = (minute + selectionTimeInMins) % 60;
            int endTimeHours = hourOfDay + ((minute + selectionTimeInMins) / 60);

            String endTimeHoursStr = (endTimeHours < 10) ? "0" + String.valueOf(endTimeHours) : String.valueOf(endTimeHours);
            String endTimeMinsStr = (endTimeMins < 10) ? "0" + String.valueOf(endTimeMins) : String.valueOf(endTimeMins);
            String endTime = endTimeHoursStr + ":" + endTimeMinsStr;

            selectedBookingStartTime = hourFixed + ":" + minuteFixed;
            selectedBookingEndTime = endTime;

            boolean isTimeSelectionValid = false;

            for(HashMap<String, String> slot : rawAvailableTimeSlots){
                if((compareTimeStrings(selectedBookingStartTime, slot.get("startTime")) != -1) && (compareTimeStrings(selectedBookingEndTime, slot.get("endTime")) != 1)){
                    isTimeSelectionValid = true;
                }
            }

            if(!isTimeSelectionValid){
                Toast.makeText(getApplicationContext(), "Invalid Time!", Toast.LENGTH_SHORT).show();
                selectedBookingStartTime = null;
            } else {
                selectedTime.setText(fixTimeStamp(selectedBookingStartTime));
            }
        }
    };

    private ArrayList<HashMap<String, String>> availableTimeSlot(ArrayList<HashMap<String, String>> bookedTimeSlots, HashMap<String, Object> salonMap, String forDate) {
        ArrayList<HashMap<String, String>> availableTimeSlots = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String currentTime = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        String dayDate = (calendar.get(Calendar.DAY_OF_MONTH)) < 10 ? "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = (calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + String.valueOf(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String currentDate = dayDate + "-" + month + "-" + calendar.get(Calendar.YEAR);

        if(compareDateStrings(forDate, currentDate) == -1){
            return availableTimeSlots;
        }

        int dayOfWeek = 0;

        try {
            Date timeStringParse = new SimpleDateFormat("dd-MM-yyyy").parse(forDate);
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(timeStringParse);
            dayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int[] workingDays = getWorkingDays(salonMap.get("workingDays").toString());
        if (workingDays[today] == 1) {
            ArrayList<HashMap<String, Object>> daysWorkingHours = (ArrayList<HashMap<String, Object>>) salonMap.get("daysWorkingHours");
            for (HashMap<String, Object> day : daysWorkingHours) {
                if (((day.get("id")).toString()).equals(String.valueOf(dayOfWeek))) {
                    String openingTime = day.get("openingTime").toString();
                    String closingTime = day.get("closingTime").toString();
                    String startTimePointer;
                    ArrayList<HashMap<String, String>> breaks = (ArrayList<HashMap<String, String>>) day.get("breaks");
                    ArrayList<HashMap<String, String>> allUnavailableTimeSlots = new ArrayList<>();

                    ArrayList<HashMap<String, String>> filteredBookedTimeSlot = new ArrayList<>();

                    if(forDate.equals(currentDate)){
                        startTimePointer = currentTime;
                    } else {
                        startTimePointer = openingTime;
                    }

                    for (HashMap<String, String> booking : bookedTimeSlots) {
                        if (booking.get("date").equals(forDate)) {
                            filteredBookedTimeSlot.add(booking);
                        }
                    }

                    allUnavailableTimeSlots.addAll(breaks);
                    allUnavailableTimeSlots.addAll(filteredBookedTimeSlot);

                    sortTimeStamps(allUnavailableTimeSlots);

                    for (HashMap<String, String> timeSlot : allUnavailableTimeSlots) {
                        if (compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == -1) {
                            HashMap<String, String> slotMap = new HashMap<>();
                            slotMap.put("startTime", startTimePointer);
                            slotMap.put("endTime", timeSlot.get("startTime"));
                            availableTimeSlots.add(slotMap);

                            startTimePointer = timeSlot.get("endTime");
                        } else if (((compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == 1) && (compareTimeStrings(startTimePointer, timeSlot.get("endTime")) == -1)) || (compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == 0)) {
                            startTimePointer = timeSlot.get("endTime");
                        }
                    }

                    if (compareTimeStrings(startTimePointer, closingTime) == -1) {
                        HashMap<String, String> slotMap = new HashMap<>();
                        slotMap.put("startTime", startTimePointer);
                        slotMap.put("endTime", closingTime);
                        availableTimeSlots.add(slotMap);
                    }
                }
            }
        }

        return availableTimeSlots;
    }

    private ArrayList<HashMap<String, String>> barberAvailableSlots(Barber barber, ArrayList<HashMap<String, String>> bookedTimeSlots, String forDate){
        ArrayList<HashMap<String, String>> allBarbersAvailableSlots = new ArrayList<>();

        int dayOfWeek = 0;

        try {
            Date timeStringParse = new SimpleDateFormat("dd-MM-yyyy").parse(forDate);
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(timeStringParse);
            dayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashMap<String, HashMap<String, Object>> workingHours = (HashMap<String, HashMap<String, Object>>) barber.getBarberDetails().get("workingHours");
        HashMap<String, Object> selectedDayWorkingHours = workingHours.get(String.valueOf(dayOfWeek));
        ArrayList<HashMap<String, String>> breaks = (ArrayList<HashMap<String, String>>) selectedDayWorkingHours.get("breaks");


        ArrayList<HashMap<String, String>> bookings = (ArrayList<HashMap<String, String>>) barber.getBarberDetails().get("bookings");
        ArrayList<HashMap<String, String>> slotsBookedForDate = new ArrayList<>();

        for(HashMap<String, String> slot : bookings){
            if(slot.get("date").equals(forDate)){
                slotsBookedForDate.add(slot);
            }
        }

        ArrayList<HashMap<String, String>> allUnavailableSlots = new ArrayList<>();
        allUnavailableSlots.addAll(breaks);
        allUnavailableSlots.addAll(slotsBookedForDate);

        sortTimeStamps(allUnavailableSlots);

        String dayStartTime = selectedDayWorkingHours.get("startTime").toString();
        String dayEndTime = selectedDayWorkingHours.get("endTime").toString();
        String startTimePointer = dayStartTime;

        for (HashMap<String, String> timeSlot : allUnavailableSlots) {
            if (compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == -1) {
                HashMap<String, String> slotMap = new HashMap<>();
                slotMap.put("startTime", startTimePointer);
                slotMap.put("endTime", timeSlot.get("startTime"));
                allBarbersAvailableSlots.add(slotMap);

                startTimePointer = timeSlot.get("endTime");
            } else if (((compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == 1) && (compareTimeStrings(startTimePointer, timeSlot.get("endTime")) == -1)) || (compareTimeStrings(startTimePointer, timeSlot.get("startTime")) == 0)) {
                startTimePointer = timeSlot.get("endTime");
            }
        }

        if (compareTimeStrings(startTimePointer, dayEndTime) == -1) {
            HashMap<String, String> slotMap = new HashMap<>();
            slotMap.put("startTime", startTimePointer);
            slotMap.put("endTime", dayEndTime);
            allBarbersAvailableSlots.add(slotMap);
        }

        return allBarbersAvailableSlots;
    }

    private int[] getWorkingDays(String workingDays) {
        int[] workingDaysParsed = new int[7];

        String[] daysSplit = workingDays.split(",");

        for (String day : daysSplit) {
            workingDaysParsed[Integer.parseInt(day)] = 1;
        }

        return workingDaysParsed;
    }

    private int compareTimeStrings(String timeString1, String timeString2) {
        String[] timeString1Split = timeString1.split(":");
        String[] timeString2Split = timeString2.split(":");

        if (Integer.parseInt(timeString1Split[0]) > Integer.parseInt(timeString2Split[0])) {
            return 1;
        } else if (Integer.parseInt(timeString1Split[0]) == Integer.parseInt(timeString2Split[0])) {
            if (Integer.parseInt(timeString1Split[1]) > Integer.parseInt(timeString2Split[1])) {
                return 1;
            } else if (Integer.parseInt(timeString1Split[1]) == Integer.parseInt(timeString2Split[1])) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int compareDateStrings(String dateString1, String dateString2){
        // DD-MM-YYYY
        String[] dateString1Split = dateString1.split("-");
        String[] dateString2Split = dateString2.split("-");

        for(int i = dateString1Split.length - 1; i >= 0 ; i--){
            if(Integer.parseInt(dateString1Split[i]) > Integer.parseInt(dateString2Split[i])){
                return 1;
            }
        }

        return dateString1.equals(dateString2) ? 0 : -1;
    }

    private String fixTimeStamp(String timeStamp){
        // Converts the timestamp from 24-hour format to 12-hour format

        String[] timeStampSplit = timeStamp.split(":");
        String hours = timeStampSplit[0];
        String suffix;

        if(Integer.parseInt(hours) > 12){ // If 13 hours or above in 24 hours
            timeStampSplit[0] = String.valueOf(Integer.parseInt(hours) % 12);
        }

        // At and after 12 in 24-hour clock, PM starts
        if(Integer.parseInt(hours) >= 12){
            suffix = " PM";
        } else {
            suffix = " AM";
        }

        // Add 0 to single digits
        for(int i = 0; i < timeStampSplit.length; i++){
            if(timeStampSplit[i].length() == 1){
                timeStampSplit[i] = "0" + timeStampSplit[i];
            }
        }

        return timeStampSplit[0] + ":" + timeStampSplit[1] + suffix;
    }

    private ArrayList<HashMap<String, String>> getValidBookingSlots(ArrayList<HashMap<String, String>> bookingSlots, int selectionTimeInMins) {
        ArrayList<HashMap<String, String>> filteredAvailableBookingSlots = new ArrayList<>();

        for (HashMap<String, String> slot : bookingSlots) {
            if (selectionTimeInMins <= timeDifference(slot.get("startTime"), slot.get("endTime"))) {
                filteredAvailableBookingSlots.add(slot);
            }
        }

        return filteredAvailableBookingSlots;
    }

    private int timeDifference(String timeString1, String timeString2) {
        String[] timeString1Split = timeString1.split(":");
        String[] timeString2Split = timeString2.split(":");

        int hour = Integer.parseInt(timeString2Split[0]) - Integer.parseInt(timeString1Split[0]);
        int mins = Integer.parseInt(timeString2Split[1]) - Integer.parseInt(timeString1Split[1]);

        return ((hour * 60) + mins);
    }

    private void sortTimeStamps(ArrayList<HashMap<String, String>> timeStamps) {
        for (int i = 1; i < timeStamps.size(); i++) {
            HashMap<String, String> key = timeStamps.get(i);
            int j = i - 1;
            while ((j >= 0) && (compareTimeStrings(timeStamps.get(j).get("startTime"), key.get("startTime")) == 1)) {
                timeStamps.set(j + 1, timeStamps.get(j--));
            }
            timeStamps.set(j + 1, key);
        }
    }

    private int getTotalMinsFromTimeStamp(String rawTimeString) {
        // Format 0H45M
        int hourCharInd = rawTimeString.indexOf('H');
        int minCharInd = rawTimeString.indexOf('M');

        int hours = Integer.parseInt(rawTimeString.substring(0, hourCharInd));
        int minutes = Integer.parseInt(rawTimeString.substring(hourCharInd + 1, minCharInd));

        minutes += 60 * hours;

        return minutes;
    }

    private String getTimeStringFromMins(int selectedServicesTimeInMins) {
        int hours = selectedServicesTimeInMins / 60;
        int mins = selectedServicesTimeInMins % 60;

        if (hours == 0) {
            return mins + " Mins";
        } else {
            return hours + " Hr " + mins + " Mins";
        }
    }

    private HashMap<String, String> parseDate(String dateString) {
        HashMap<String, String> parsedDate = new HashMap<>();

        String[] splittedDateStr = dateString.split("-");
        String dayDate = splittedDateStr[0];
        String month = splittedDateStr[1];
        String year = splittedDateStr[2];

        parsedDate.put("dayDate", dayDate);
        parsedDate.put("month", month);
        parsedDate.put("year", year);

        return parsedDate;
    }
}
