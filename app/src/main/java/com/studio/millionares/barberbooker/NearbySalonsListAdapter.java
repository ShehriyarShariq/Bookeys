package com.studio.millionares.barberbooker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NearbySalonsListAdapter extends RecyclerView.Adapter<NearbySalonsListAdapter.NearbySalonsListViewHolder> {

    /*RECYCLER VIEW ADAPTER FOR HomeActivity NearbySalonsList*/

    ArrayList<Salon> allNearbySalons;
    Context context;

    private FirebaseStorage firebaseStorage;

    public NearbySalonsListAdapter(Context context, ArrayList<Salon> allNearbySalons) {
        this.context = context;
        this.allNearbySalons = allNearbySalons;

        firebaseStorage = FirebaseStorage.getInstance();
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
        String salonID = allNearbySalons.get(position).getSalonDetails().get("id").toString();
        final String rating = allNearbySalons.get(position).getSalonDetails().get("rating").toString();
        final String salonName = allNearbySalons.get(position).getSalonDetails().get("name").toString();
        String salonAddress = allNearbySalons.get(position).getSalonDetails().get("address").toString();
        String profileImageName = allNearbySalons.get(position).getSalonDetails().get("imageURL").toString();

        nearbySalonsListViewHolder.salonName.setText(salonName);
        nearbySalonsListViewHolder.ratingTxt.setText(rating);
        nearbySalonsListViewHolder.salonAddress.setText(salonAddress);

        if(!profileImageName.equals("none")){
            StorageReference imgReference = firebaseStorage.getReference().child("Salons").child(salonID).child(profileImageName);

            Glide.with(context)
                    .load(imgReference)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            nearbySalonsListViewHolder.salonImg.setBackgroundColor(Color.TRANSPARENT);
                            return false;
                        }
                    })
                    .into(nearbySalonsListViewHolder.salonImg);
        }

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
        ImageView salonImg;
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
