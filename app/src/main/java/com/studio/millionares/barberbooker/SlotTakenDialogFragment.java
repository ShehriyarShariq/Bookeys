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

public class SlotTakenDialogFragment extends DialogFragment {

    /*
        CUSTOM DIALOG TO INDICATE INVALID SELECTED TIME
    */

    private CardView okBtn;

    private Activity activity;
    private HashMap<String, Object> salonBasicDetails;

    public SlotTakenDialogFragment() {
    }

    @SuppressLint("ValidFragment")
    public SlotTakenDialogFragment(Activity activity, HashMap<String, Object> salonBasicDetails) {
        this.activity = activity;
        this.salonBasicDetails = salonBasicDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slot_taken_dialog_box_layout, container, false);
        okBtn = view.findViewById(R.id.ok_btn);

        okBtn.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }
}