package com.studio.millionares.barberbooker;

import java.util.HashMap;

public class Review {

    private String reviewerName, reviewMsg, dateOfMsg, rating;

    public Review(HashMap<String, String> reviewDetails){
        this.reviewerName = reviewDetails.get("name");
        this.reviewMsg = reviewDetails.get("message");
        this.dateOfMsg = reviewDetails.get("date");
        this.rating = reviewDetails.get("rating");
    }

    public HashMap<String, String> getReviewDetails(){
        HashMap<String, String> reviewDetails = new HashMap<>();

        reviewDetails.put("name", reviewerName);
        reviewDetails.put("message", reviewMsg);
        reviewDetails.put("date", dateOfMsg);
        reviewDetails.put("rating", rating);

        return reviewDetails;
    }

}
