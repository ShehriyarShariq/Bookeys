package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectedServicesListAdapter extends RecyclerView.Adapter<SelectedServicesListAdapter.SelectedServicesListViewHolder> {

    ArrayList<Service> allSelectedServices;

    public SelectedServicesListAdapter(ArrayList<Service> allSelectedServices) {
        this.allSelectedServices = allSelectedServices;
    }

    @NonNull
    @Override
    public SelectedServicesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.receipt_services_list_single_item_layout, parent, false);

        return new SelectedServicesListAdapter.SelectedServicesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedServicesListViewHolder selectedServicesListViewHolder, int i) {
        String position = String.valueOf(i + 1);
        String serviceName = allSelectedServices.get(i).getServiceSimpleDetails().get("name");
        String serviceCost = "R.s " + allSelectedServices.get(i).getServiceSimpleDetails().get("cost") + "/-";

        selectedServicesListViewHolder.positionNumberTxt.setText(position + ".");
        selectedServicesListViewHolder.serviceNameTxt.setText(serviceName);
        selectedServicesListViewHolder.serviceCostTxt.setText(serviceCost);
    }

    @Override
    public int getItemCount() {
        return allSelectedServices.size();
    }

    public class SelectedServicesListViewHolder extends RecyclerView.ViewHolder {

        TextView positionNumberTxt, serviceNameTxt, serviceCostTxt;

        public SelectedServicesListViewHolder(@NonNull View itemView) {
            super(itemView);

            positionNumberTxt = itemView.findViewById(R.id.number_txt);
            serviceNameTxt = itemView.findViewById(R.id.service_name_txt);
            serviceCostTxt = itemView.findViewById(R.id.service_cost_txt);
        }
    }
}
