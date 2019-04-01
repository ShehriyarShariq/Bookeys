package com.studio.millionares.barberbooker;

import java.util.HashMap;

public class Customer {

    // Customer Object
    // Defines all the properties of a customer

    private String username, email, password, phoneNum, imgLink;

    public Customer(String username, String email, String password, String phoneNum, String imgLink){
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNum = phoneNum;
        this.imgLink = imgLink;
    }

    // For initializing a customer
    // This is not accessed later since all needed data is fetched in real-time
    public HashMap<String, Object> getCustomerMap(){
        HashMap<String, Object> customerMap = new HashMap<>();

        customerMap.put("name", username);
        customerMap.put("email", email);
        customerMap.put("password", password);
        customerMap.put("phoneNum", phoneNum);
        customerMap.put("walletAmount", 0);
        customerMap.put("imgLink", imgLink);

        // For any current bookings
        HashMap<String, Object> booking = new HashMap<>();
        HashMap<String, Object> currentBooking = new HashMap<>();
        currentBooking.put("amount", "amount");
        currentBooking.put("dateAndTime", "dateAndTime");
        currentBooking.put("barberID", "barberID");
        currentBooking.put("barberName", "barberName");
        currentBooking.put("salonID", "salonID");
        currentBooking.put("salonName", "salonName");
        currentBooking.put("status", "cancelledInProgressOrCompleted");

        // Services availed in particular booking
        HashMap<String, Object> servicesAvailed = new HashMap<>();
        HashMap<String, String> serviceDetails = new HashMap<>();
        serviceDetails.put("cost", "cost");
        serviceDetails.put("expectedTime", "expectedTime");
        serviceDetails.put("name", "serviceName");
        servicesAvailed.put("00", serviceDetails);
        currentBooking.put("servicesAvailed", servicesAvailed);
        booking.put("bookingID", currentBooking);
        customerMap.put("currentBooking", booking);

        // For bookings made in the past
        HashMap<String, Object> bookingsHistory = new HashMap<>();
        HashMap<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("amount", "amount");
        bookingDetails.put("dateAndTime", "dateAndTime");
        bookingDetails.put("rating", "rating");
        bookingDetails.put("review", "review");
        bookingDetails.put("barberID", "barberID");
        bookingDetails.put("salonID", "salonID");
        bookingDetails.put("status", "cancelledOrCompleted");

        // Services availed in particular booking
        servicesAvailed = new HashMap<>();
        serviceDetails = new HashMap<>();
        serviceDetails.put("cost", "cost");
        serviceDetails.put("expectedTime", "expectedTime");
        serviceDetails.put("name", "serviceName");

        servicesAvailed.put("00", serviceDetails);
        bookingDetails.put("servicesAvailed", servicesAvailed);
        bookingsHistory.put("bookingID", bookingDetails);
        customerMap.put("bookingsHistory", bookingsHistory);

        HashMap<String, String> verified = new HashMap<>();
        verified.put("email", "false");
        verified.put("phoneNum", "false");
        customerMap.put("verified", verified);


        return customerMap;
    }

}
