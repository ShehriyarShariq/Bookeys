package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SalonServicesListAdapter extends RecyclerView.Adapter<SalonServicesListAdapter.SalonServicesListViewHolder> {

    ArrayList<Service> allServices;

    public SalonServicesListAdapter(ArrayList<Service> allServices) {
        this.allServices = allServices;
    }

    @NonNull
    @Override
    public SalonServicesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.salon_services_list_single_item_layout, parent, false);

        return new SalonServicesListAdapter.SalonServicesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonServicesListViewHolder salonServicesListViewHolder, int i) {
        HashMap<String, Object> serviceDetails = allServices.get(i).getServiceDetails();
        String serviceNameStr = serviceDetails.get("name").toString();
        String serviceCostStr = serviceDetails.get("cost").toString();
        String serviceExpectedTimeStr = getTimeString(serviceDetails.get("expectedTime").toString());

        salonServicesListViewHolder.serviceName.setText(serviceNameStr);
        salonServicesListViewHolder.serviceCost.setText(serviceCostStr + "/-");
        salonServicesListViewHolder.serviceExpectedTime.setText("Expected Time: " + serviceExpectedTimeStr);
    }

    @Override
    public int getItemCount() {
        return allServices.size();
    }

    public class SalonServicesListViewHolder extends RecyclerView.ViewHolder {

        TextView serviceName, serviceExpectedTime, serviceCost;

        public SalonServicesListViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceName = itemView.findViewById(R.id.service_name_txt);
            serviceExpectedTime = itemView.findViewById(R.id.expected_time_txt);
            serviceCost = itemView.findViewById(R.id.cost_txt);
        }
    }

    private String getTimeString(String rawTimeString){
        int hourCharInd = rawTimeString.indexOf('H');
        int minCharInd = rawTimeString.indexOf('M');

        String hours = rawTimeString.substring(0, hourCharInd);
        String minutes = rawTimeString.substring(hourCharInd + 1, minCharInd);
        String timeString = Integer.parseInt(hours) > 0 ? hours + "Hr " + minutes + "Mins" : minutes + "Mins";

        return timeString;
    }
}
