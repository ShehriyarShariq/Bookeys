package com.studio.millionares.barberbooker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NearbySalonsListAdapter extends RecyclerView.Adapter<NearbySalonsListAdapter.NearbySalonsListViewHolder> {

    /*RECYCLER VIEW ADAPTER FOR HomeActivity NearbySalonsList*/

    ArrayList<Salon> allNearbySalons;
    Context context;

    public NearbySalonsListAdapter(Context context, ArrayList<Salon> allNearbySalons) {
        this.context = context;
        this.allNearbySalons = allNearbySalons;
    }

    @NonNull
    @Override
    public NearbySalonsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.closest_barbers_list_single_item_layout, parent, false);

        return new NearbySalonsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NearbySalonsListViewHolder nearbySalonsListViewHolder, final int position) {

        //String salonImg = allNearbySalons.get(position).getSimpleDetails().get("imageURL").toString();
        final String rating = allNearbySalons.get(position).getSalonDetails().get("rating").toString();
        final String salonName = allNearbySalons.get(position).getSalonDetails().get("name").toString();
        String salonAddress = allNearbySalons.get(position).getSalonDetails().get("address").toString();

        nearbySalonsListViewHolder.salonName.setText(salonName);
        nearbySalonsListViewHolder.ratingTxt.setText(rating);
        nearbySalonsListViewHolder.salonAddress.setText(salonAddress);

        nearbySalonsListViewHolder.salonThumbnailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent salonIntent = new Intent(context, SalonBookingDetailsActivity.class);
                salonIntent.putExtra("id", allNearbySalons.get(position).getSalonDetails().get("id").toString());
                salonIntent.putExtra("name", salonName);
                salonIntent.putExtra("rating", rating);
                salonIntent.putExtra("latitude", ((LatLng) allNearbySalons.get(position).getSalonDetails().get("location")).latitude);
                salonIntent.putExtra("longitude", ((LatLng) allNearbySalons.get(position).getSalonDetails().get("location")).longitude);

                context.startActivity(salonIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allNearbySalons.size();
    }

    public void filterList(ArrayList<Salon> allSalons){
        this.allNearbySalons = allSalons;
        notifyDataSetChanged();
    }

    public class NearbySalonsListViewHolder extends RecyclerView.ViewHolder {

        CardView salonThumbnailLayout;
        CircleImageView salonImg;
        TextView salonName, ratingTxt, salonAddress;

        public NearbySalonsListViewHolder(@NonNull View itemView) {
            super(itemView);

            salonThumbnailLayout = itemView.findViewById(R.id.salon_layout);
            salonImg = itemView.findViewById(R.id.salon_image);
            ratingTxt = itemView.findViewById(R.id.rating_txt);
            salonName = itemView.findViewById(R.id.salon_name);
            salonAddress = itemView.findViewById(R.id.address_txt);
        }
    }
}
