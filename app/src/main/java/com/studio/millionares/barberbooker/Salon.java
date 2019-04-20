package com.studio.millionares.barberbooker;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class Salon {

    /*
        SALON OBJECT CLASS DEFINITION
    */

    private String salonID, name, imageURL, city, rating, address, salonDescription;
    private LatLng location;
    private ArrayList<Service> allProvidedServices;
    private ArrayList<Barber> allBarbers;
    private ArrayList<HashMap<String, String>> bookedTimeSlots;
    private ArrayList<String> closedDays;
    private HashMap<String, Object> workingHoursAndDays;
    private ArrayList<Review> reviews;

    public Salon(String salonID, String name, String address, String city, String rating, String imageURL, LatLng location) {
        this.salonID = salonID;
        this.name = name;
        this.rating = rating;
        this.imageURL = imageURL;
        this.city = city;
        this.address = address;
        this.location = location;
    }

    public Salon(String salonID, ArrayList<Service> allProvidedServices, ArrayList<Barber> allBarbers, ArrayList<HashMap<String, String>> bookedTimeSlots, ArrayList<String> closedDays, HashMap<String, Object> workingHoursAndDays, String salonDescription, ArrayList<Review> reviews){
        this.salonID = salonID;
        this.allProvidedServices = allProvidedServices;
        this.allBarbers = allBarbers;
        this.bookedTimeSlots = bookedTimeSlots;
        this.workingHoursAndDays = workingHoursAndDays;
        this.closedDays = closedDays;
        this.salonDescription = salonDescription;
        this.reviews = reviews;
    }

    public HashMap<String, Object> getSalonDetails() {
        HashMap<String, Object> simpleMap = new HashMap<>();

        simpleMap.put("id", salonID);
        simpleMap.put("name", name);
        simpleMap.put("address", address);
        simpleMap.put("city", city);
        simpleMap.put("rating", rating);
        simpleMap.put("imageURL", imageURL);
        simpleMap.put("location", location);
        simpleMap.put("description", salonDescription);
        simpleMap.put("reviews", reviews);

        return simpleMap;
    }

    public HashMap<String, Object> getSalonBookingDetails(){
        HashMap<String, Object> salonBookingDetails = new HashMap<>();

        salonBookingDetails.put("id", salonID);
        salonBookingDetails.put("allServices", allProvidedServices);
        salonBookingDetails.put("allBarbers", allBarbers);
        salonBookingDetails.put("bookedTimeSlots", bookedTimeSlots);
        salonBookingDetails.put("closedDays", closedDays);
        salonBookingDetails.put("workingHoursAndDays", workingHoursAndDays);

        return salonBookingDetails;

    }
}
