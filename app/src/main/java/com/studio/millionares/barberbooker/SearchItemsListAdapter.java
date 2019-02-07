package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchItemsListAdapter extends RecyclerView.Adapter<SearchItemsListAdapter.SearchItemsListViewHolder> {

    ArrayList<Salon> allSalons;

    public SearchItemsListAdapter(ArrayList<Salon> allSalons) {
        this.allSalons = allSalons;
    }

    @NonNull
    @Override
    public SearchItemsListAdapter.SearchItemsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_items_list_single_item_layout, parent, false);

        return new SearchItemsListAdapter.SearchItemsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchItemsListAdapter.SearchItemsListViewHolder searchItemsListViewHolder, int i) {
        HashMap<String, Object> salonDetails = allSalons.get(i).getSalonDetails();
        String salonName = salonDetails.get("name").toString();
        String city = salonDetails.get("city").toString();

        searchItemsListViewHolder.salonName.setText(salonName);
        searchItemsListViewHolder.city.setText(city);

    }

    @Override
    public int getItemCount() {
        return allSalons.size();
    }

    public void filterList(ArrayList<Salon> allSalons){
        this.allSalons = allSalons;
        notifyDataSetChanged();
    }

    public class SearchItemsListViewHolder extends RecyclerView.ViewHolder {

        TextView salonName, city;

        public SearchItemsListViewHolder(@NonNull View itemView) {
            super(itemView);

            salonName = itemView.findViewById(R.id.name);
            city = itemView.findViewById(R.id.city);
        }
    }
}
