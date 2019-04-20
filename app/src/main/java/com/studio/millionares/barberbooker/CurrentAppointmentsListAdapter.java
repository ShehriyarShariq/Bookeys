package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentAppointmentsListAdapter extends RecyclerView.Adapter<CurrentAppointmentsListAdapter.CurrentAppointmentsListViewHolder> {

    /*
        RECYCLER VIEW ADAPTER FOR CurrentAppointmentsFragment
    */

    private String type;
    private ArrayList<Appointment> allAppointments;
    private AppointmentsRecyclerViewListClickListener appointmentsRecyclerViewListClickListener;

    public CurrentAppointmentsListAdapter(String type, ArrayList<Appointment> allAppointments, AppointmentsRecyclerViewListClickListener appointmentsRecyclerViewListClickListener) {
        this.type = type;
        this.allAppointments = allAppointments;
        this.appointmentsRecyclerViewListClickListener = appointmentsRecyclerViewListClickListener;
    }

    @NonNull
    @Override
    public CurrentAppointmentsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.current_appointments_list_single_item_layout, parent, false);

        return new CurrentAppointmentsListAdapter.CurrentAppointmentsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentAppointmentsListViewHolder currentAppointmentsListViewHolder, int i) {
        HashMap<String, Object> appointment = allAppointments.get(i).getAppointmentDetails();

        String salonNameTxt = appointment.get("salonName").toString();
        String bookingTimeAndDateTxt = appointment.get("dateAndTime").toString();
        String bookingAmountTxt = appointment.get("amount").toString();

        currentAppointmentsListViewHolder.salonName.setText(salonNameTxt);
        currentAppointmentsListViewHolder.bookingTimeAndDate.setText(bookingTimeAndDateTxt);
        currentAppointmentsListViewHolder.bookingAmount.setText("R.s.\n" + bookingAmountTxt + "/-");
    }

    @Override
    public int getItemCount() {
        return allAppointments.size();
    }

    public class CurrentAppointmentsListViewHolder extends RecyclerView.ViewHolder {

        TextView salonName, bookingTimeAndDate, bookingAmount;

        public CurrentAppointmentsListViewHolder(@NonNull View itemView) {
            super(itemView);

            salonName = itemView.findViewById(R.id.salon_name);
            bookingTimeAndDate = itemView.findViewById(R.id.date_and_time);
            bookingAmount = itemView.findViewById(R.id.price_txt);

            // Get index of appointment clicked in the allAppointments list
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appointmentsRecyclerViewListClickListener.AppointmentListOnClick(allAppointments.get(getLayoutPosition()));
                }
            });
        }
    }
}
