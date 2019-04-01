package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class PastAppointmentsListAdapter extends RecyclerView.Adapter<PastAppointmentsListAdapter.CurrentAndPastAppointmentsListViewHolder> {

    private String type;
    private ArrayList<Appointment> allAppointments;

    public PastAppointmentsListAdapter(String type, ArrayList<Appointment> allAppointments) {
        this.type = type;
        this.allAppointments = allAppointments;
    }

    @NonNull
    @Override
    public CurrentAndPastAppointmentsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.past_appointments_list_single_item_layout, parent, false);

        return new PastAppointmentsListAdapter.CurrentAndPastAppointmentsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentAndPastAppointmentsListViewHolder currentAndPastAppointmentsListViewHolder, int i) {
        HashMap<String, Object> appointment = allAppointments.get(i).getAppointmentDetails();

        String salonNameTxt = appointment.get("salonName").toString();
        String bookingTimeAndDateTxt = appointment.get("dateAndTime").toString();
        String bookingAmountTxt = appointment.get("amount").toString();

        currentAndPastAppointmentsListViewHolder.salonName.setText(salonNameTxt);
        currentAndPastAppointmentsListViewHolder.bookingTimeAndDate.setText(bookingTimeAndDateTxt);
        currentAndPastAppointmentsListViewHolder.bookingAmount.setText("R.s.\n" + bookingAmountTxt);
    }

    @Override
    public int getItemCount() {
        return allAppointments.size();
    }

    public class CurrentAndPastAppointmentsListViewHolder extends RecyclerView.ViewHolder {

        TextView salonName, bookingTimeAndDate, bookingAmount;

        public CurrentAndPastAppointmentsListViewHolder(@NonNull View itemView) {
            super(itemView);

            salonName = itemView.findViewById(R.id.salon_name);
            bookingTimeAndDate = itemView.findViewById(R.id.date_and_time);
            bookingAmount = itemView.findViewById(R.id.price_txt);
        }
    }
}
