package com.studio.millionares.barberbooker;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

public class Appointment implements Parcelable {

    // Appointment object description

    private String bookingID, amount, dateAndTime, rating, review, barberID, barberName, salonID, salonName, status, contactNum;
    private ArrayList<HashMap<String, String>> servicesAvailed;

    public Appointment(String bookingID, String amount, String dateAndTime, String rating, String review, String barberID, String barberName, String salonID, String salonName, String status, ArrayList<HashMap<String, String>> servicesAvailed, String contactNum){
        this.bookingID = bookingID;
        this.amount = amount;
        this.dateAndTime = dateAndTime;
        this.rating = rating;
        this.review = review;
        this.barberID = barberID;
        this.barberName = barberName;
        this.salonID = salonID;
        this.salonName = salonName;
        this.status = status;
        this.servicesAvailed = servicesAvailed;
        this.contactNum = contactNum;
    }

    // For parcelable class constructor
    protected Appointment(Parcel in) {
        bookingID = in.readString();
        amount = in.readString();
        dateAndTime = in.readString();
        rating = in.readString();
        review = in.readString();
        barberID = in.readString();
        barberName = in.readString();
        salonID = in.readString();
        salonName = in.readString();
        status = in.readString();
        servicesAvailed = in.readArrayList(getClass().getClassLoader());
        contactNum = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookingID);
        dest.writeString(amount);
        dest.writeString(dateAndTime);
        dest.writeString(rating);
        dest.writeString(review);
        dest.writeString(barberID);
        dest.writeString(barberName);
        dest.writeString(salonID);
        dest.writeString(salonName);
        dest.writeString(status);
        dest.writeList(servicesAvailed);
        dest.writeString(contactNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    // Get all appointment details
    public HashMap<String, Object> getAppointmentDetails(){
        HashMap<String, Object> appointmentDetails = new HashMap<>();

        appointmentDetails.put("bookingID", this.bookingID);
        appointmentDetails.put("amount", this.amount);
        appointmentDetails.put("dateAndTime", this.dateAndTime);
        appointmentDetails.put("rating", this.rating);
        appointmentDetails.put("review", this.review);
        appointmentDetails.put("barberID", this.barberID);
        appointmentDetails.put("barberName", this.barberName);
        appointmentDetails.put("salonID", this.salonID);
        appointmentDetails.put("salonName", this.salonName);
        appointmentDetails.put("status", this.status);
        appointmentDetails.put("contactNum", this.contactNum);
        appointmentDetails.put("servicesAvailed", servicesAvailed);

        return appointmentDetails;
    }

}
