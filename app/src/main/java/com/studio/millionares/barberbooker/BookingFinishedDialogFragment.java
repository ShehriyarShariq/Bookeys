package com.studio.millionares.barberbooker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class BookingFinishedDialogFragment extends DialogFragment {

    private CardView cancelBtn, goToBookingBtn;

    private Activity activity;
    private HashMap<String, Object> salonBasicDetails;

    public BookingFinishedDialogFragment() {
    }

    @SuppressLint("ValidFragment")
    public BookingFinishedDialogFragment(Activity activity, HashMap<String, Object> salonBasicDetails) {
        this.activity = activity;
        this.salonBasicDetails = salonBasicDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.booking_finished_dialog_box_layout, container, false);
        cancelBtn = view.findViewById(R.id.cancel_btn);
        goToBookingBtn = view.findViewById(R.id.go_to_booking_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Intent restartIntent = new Intent(activity, SalonBookingDetailsActivity.class);
                restartIntent.putExtra("id", salonBasicDetails.get("id").toString());
                restartIntent.putExtra("name", salonBasicDetails.get("name").toString());
                restartIntent.putExtra("rating", salonBasicDetails.get("rating").toString());
                restartIntent.putExtra("latitude", (Double) salonBasicDetails.get("latitude"));
                restartIntent.putExtra("longitude", (Double) salonBasicDetails.get("longitude"));
                activity.finish();
                startActivity(restartIntent);
            }
        });

        goToBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, MyAppointmentsActivity.class));
                getDialog().dismiss();
                activity.finish();
            }
        });


        return view;
    }
}