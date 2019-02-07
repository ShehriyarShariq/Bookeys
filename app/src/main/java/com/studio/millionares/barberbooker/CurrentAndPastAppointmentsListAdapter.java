package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentAndPastAppointmentsListAdapter extends RecyclerView.Adapter<CurrentAndPastAppointmentsListAdapter.CurrentAndPastAppointmentsListViewHolder> {

    private String type;
    private ArrayList<HashMap<String, Object>> allAppointments;

    public CurrentAndPastAppointmentsListAdapter(String type, ArrayList<HashMap<String, Object>> allAppointments) {
        this.type = type;
        this.allAppointments = allAppointments;
    }

    @NonNull
    @Override
    public CurrentAndPastAppointmentsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.appointments_list_single_item_layout, parent, false);

        return new CurrentAndPastAppointmentsListAdapter.CurrentAndPastAppointmentsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentAndPastAppointmentsListViewHolder currentAndPastAppointmentsListViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return allAppointments.size();
    }

    public class CurrentAndPastAppointmentsListViewHolder extends RecyclerView.ViewHolder {
        public CurrentAndPastAppointmentsListViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
