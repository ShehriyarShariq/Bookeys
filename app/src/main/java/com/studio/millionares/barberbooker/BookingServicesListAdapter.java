package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class BookingServicesListAdapter extends RecyclerView.Adapter<BookingServicesListAdapter.BookingServicesListViewHolder> {

    // Adapter is bound to the list which shows all the services available for booking

    ArrayList<Service> allServices; // Stores all the services

    ArrayList<Service> selectedServices; // Stores all the selected services

    public BookingServicesListAdapter(ArrayList<Service> allServices) {
        this.allServices = allServices;
        selectedServices = new ArrayList<>();
    }

    @NonNull
    @Override
    public BookingServicesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // Binds the view for the adapter
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.booking_services_selection_list_single_item_layout, parent, false);

        return new BookingServicesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookingServicesListViewHolder bookingServicesListViewHolder, final int i) {
        HashMap<String, Object> serviceDetails = allServices.get(i).getServiceDetails();
        String serviceNameStr = serviceDetails.get("name").toString();
        String serviceCostStr = serviceDetails.get("cost").toString();
        String serviceExpectedTimeStr = getTimeString(serviceDetails.get("expectedTime").toString());

        // Updating the UI
        bookingServicesListViewHolder.serviceName.setText(serviceNameStr);
        bookingServicesListViewHolder.serviceCost.setText(serviceCostStr + "/-");
        bookingServicesListViewHolder.serviceExpectedTime.setText("Expected Time: " + serviceExpectedTimeStr);

        // When clicked on a service item, adds or removes it from the selected services list
        bookingServicesListViewHolder.serviceItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bookingServicesListViewHolder.serviceSelectionCheckbox.isChecked()){ // Already selected
                    bookingServicesListViewHolder.serviceSelectionCheckbox.setChecked(false);
                    selectedServices.remove(allServices.get(i));
                } else { // Not selected
                    bookingServicesListViewHolder.serviceSelectionCheckbox.setChecked(true);
                    selectedServices.add(allServices.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return allServices.size();
    }

    public class BookingServicesListViewHolder extends RecyclerView.ViewHolder {

        CardView serviceItemLayout;
        CheckBox serviceSelectionCheckbox;
        TextView serviceName, serviceCost, serviceExpectedTime;

        public BookingServicesListViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceItemLayout = itemView.findViewById(R.id.list_item_layout);
            serviceSelectionCheckbox = itemView.findViewById(R.id.checkbox);
            serviceName = itemView.findViewById(R.id.description_txt);
            serviceCost = itemView.findViewById(R.id.price_txt);
            serviceExpectedTime = itemView.findViewById(R.id.expected_time_txt);
        }
    }

    private String getTimeString(String rawTimeString){
        // Converts timeString from xHyM to x Hr y Mins time format

        int hourCharInd = rawTimeString.indexOf('H');
        int minCharInd = rawTimeString.indexOf('M');

        String hours = rawTimeString.substring(0, hourCharInd);
        String minutes = rawTimeString.substring(hourCharInd + 1, minCharInd);

        // if 0 hours then hours not shown
        String timeString = Integer.parseInt(hours) > 0 ? hours + "Hr " + minutes + "Mins" : minutes + "Mins";

        return timeString;
    }

    public ArrayList<Service> getAllSelectedServices(){ // Returns the currently selected services list
        return selectedServices;
    }

    public void clearSelectedServices(){ // Clears the selected services list
        selectedServices = new ArrayList<>();
    }
}
