package com.studio.millionares.barberbooker;

import java.util.ArrayList;
import java.util.HashMap;

public class Barber {

    String id, name, rating, imageURL;
    HashMap<String, HashMap<String, Object>> workingHours;
    ArrayList<HashMap<String, String>> bookings;

    public Barber(String id, String name, String rating, String imageURL, HashMap<String, HashMap<String, Object>> workingHours, ArrayList<HashMap<String, String>> bookings){
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.imageURL = imageURL;
        this.workingHours = workingHours;
        this.bookings = bookings;
    }

    public HashMap<String, Object> getBarberDetails(){
        HashMap<String, Object> barberDetails = new HashMap<>();

        barberDetails.put("id", id);
        barberDetails.put("name", name);
        barberDetails.put("rating", rating);
        barberDetails.put("imageURL", imageURL);
        barberDetails.put("workingHours", workingHours);
        barberDetails.put("bookings", bookings);

        return barberDetails;
    }

}
