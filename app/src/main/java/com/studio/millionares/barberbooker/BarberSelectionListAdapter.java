package com.studio.millionares.barberbooker;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BarberSelectionListAdapter extends RecyclerView.Adapter<BarberSelectionListAdapter.BarberSelectionListViewHolder> {

    // Adapter is bound to the list which shows all the available barbers for the selected time slot

    private final BarberSelectionRecyclerViewListClickListener barberSelectionListener;

    ArrayList<Barber> allBarbers;

    public BarberSelectionListAdapter(ArrayList<Barber> allBarbers, BarberSelectionRecyclerViewListClickListener barberSelectionListener) {
        this.allBarbers = allBarbers;
        this.barberSelectionListener = barberSelectionListener;
    }

    @NonNull
    @Override
    public BarberSelectionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // Binds the view for the adapter
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.barber_selection_list_single_item_layout, parent, false);

        return new BarberSelectionListAdapter.BarberSelectionListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BarberSelectionListViewHolder barberSelectionListViewHolder, int i) {
        String barberNameStr = allBarbers.get(i).getBarberDetails().get("name").toString();
        String barberRatingStr = allBarbers.get(i).getBarberDetails().get("rating").toString();
        //String barberImageURLStr = allBarbers.get(i).getBarberDetails().get("imageURL");

        // Updating the UI
        if(barberNameStr.equals("none")){
            barberSelectionListViewHolder.noBarberLayout.setVisibility(View.VISIBLE);
            barberSelectionListViewHolder.barberLayout.setVisibility(View.GONE);
        } else {
            barberSelectionListViewHolder.noBarberLayout.setVisibility(View.GONE);
            barberSelectionListViewHolder.barberLayout.setVisibility(View.VISIBLE);
        }

        barberSelectionListViewHolder.barberName.setText(barberNameStr);
        barberSelectionListViewHolder.barberRating.setText(barberRatingStr);
    }

    @Override
    public int getItemCount() {
        return allBarbers.size();
    }

    public class BarberSelectionListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView barberLayout, noBarberLayout;
        TextView barberName, barberRating;
        ImageView barberImage;

        public BarberSelectionListViewHolder(@NonNull View itemView) {
            super(itemView);

            barberLayout = itemView.findViewById(R.id.barber_layout);
            noBarberLayout = itemView.findViewById(R.id.no_barber_layout);
            barberName = itemView.findViewById(R.id.barber_name);
            barberRating = itemView.findViewById(R.id.barber_rating);
            barberImage = itemView.findViewById(R.id.barber_image);

            // On item click listener for a barber item
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Triggers the interface event listener
            barberSelectionListener.barberSelectionRecyclerViewListClicked((allBarbers.get(getLayoutPosition())).getId());
        }
    }

    // Update the data in the list adapter
    public void refreshDataset(ArrayList<Barber> allBarbers){
        this.allBarbers = allBarbers;
        notifyDataSetChanged();
    }
}
