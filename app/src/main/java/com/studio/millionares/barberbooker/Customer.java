package com.studio.millionares.barberbooker;

import java.util.HashMap;

public class Customer {

    // Customer Object
    // Defines all the properties of a customer

    private String username, email, phoneNum, imgLink;

    public Customer(String username, String email, String phoneNum, String imgLink){
        this.username = username;
        this.email = email;
        this.phoneNum = phoneNum;
        this.imgLink = imgLink;
    }

    // For initializing a customer
    // This is not accessed later since all needed data is fetched in real-time
    public HashMap<String, Object> getCustomerMap(){
        HashMap<String, Object> customerMap = new HashMap<>();

        customerMap.put("name", username);
        customerMap.put("email", email);
        customerMap.put("phoneNum", phoneNum);
        customerMap.put("walletAmount", 0);
        customerMap.put("imgLink", imgLink);

        // For any current bookings
        HashMap<String, String> currentBooking = new HashMap<>();
        currentBooking.put("bookingID", "bookingID");
        currentBooking.put("salonID", "salonID");

        customerMap.put("currentBooking", currentBooking);

        // For bookings made in the past
        HashMap<String, Object> bookingsHistory = new HashMap<>();
        HashMap<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("amount", "amount");
        bookingDetails.put("dateAndTime", "dateAndTime");
        bookingDetails.put("rating", "rating");
        bookingDetails.put("review", "review");
        bookingDetails.put("salonID", "salonID");
        bookingDetails.put("status", "cancelledOrCompleted");

        // Services availed in particular booking
        HashMap<String, Object> servicesAvailed = new HashMap<>();
        HashMap<String, String> serviceDetails = new HashMap<>();
        serviceDetails.put("amount", "amount");
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
