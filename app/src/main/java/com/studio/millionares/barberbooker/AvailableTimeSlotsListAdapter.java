package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AvailableTimeSlotsListAdapter extends RecyclerView.Adapter<AvailableTimeSlotsListAdapter.AvailableTimeSlotsListViewHolder> {

    // Adapter is bound to the list which shows all the available time slots

    ArrayList<HashMap<String, String>> availableTimeSlots;

    public AvailableTimeSlotsListAdapter(ArrayList<HashMap<String, String>> availableTimeSlots) {
        this.availableTimeSlots = availableTimeSlots;
    }

    @NonNull
    @Override
    public AvailableTimeSlotsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // Binds the view for the adapter
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.available_time_slots_list_single_item_layout, parent, false);

        return new AvailableTimeSlotsListAdapter.AvailableTimeSlotsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableTimeSlotsListViewHolder availableTimeSlotsListViewHolder, int i) {
        String startTime = fixTimeStamp(availableTimeSlots.get(i).get("startTime"));
        String endTime = fixTimeStamp(availableTimeSlots.get(i).get("endTime"));

        // Updating the UI
        availableTimeSlotsListViewHolder.startTimeTxt.setText(startTime);
        availableTimeSlotsListViewHolder.endTImeTxt.setText(endTime);
    }

    @Override
    public int getItemCount() {
        return availableTimeSlots.size();
    }

    public class AvailableTimeSlotsListViewHolder extends RecyclerView.ViewHolder {

        TextView startTimeTxt, endTImeTxt;

        public AvailableTimeSlotsListViewHolder(@NonNull View itemView) {
            super(itemView);

            startTimeTxt = itemView.findViewById(R.id.start_time_txt);
            endTImeTxt = itemView.findViewById(R.id.end_time_txt);
        }
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

    // Update the data in the list adapter
    public void refreshDataset(ArrayList<HashMap<String, String>> availableTimeSlots){
        this.availableTimeSlots = availableTimeSlots;
        notifyDataSetChanged();
    }
}
