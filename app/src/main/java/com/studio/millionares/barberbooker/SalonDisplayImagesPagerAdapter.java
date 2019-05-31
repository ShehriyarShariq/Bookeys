package com.studio.millionares.barberbooker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SalonDisplayImagesPagerAdapter extends PagerAdapter {

    Context context;
    String salonID;
    ArrayList<String> allDisplayImages;

    LayoutInflater inflater;

    private FirebaseStorage firebaseStorage;

    public SalonDisplayImagesPagerAdapter(Context context, String salonID, ArrayList<String> allDisplayImages) {
        this.context = context;
        this.salonID = salonID;
        this.allDisplayImages = allDisplayImages;

        inflater = LayoutInflater.from(context);

        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup parent, int position) {
        View view = inflater.inflate(R.layout.salon_display_images_pager_single_item_layout, parent, false);

        final ProgressBar progressLoader = view.findViewById(R.id.progress_loader);
        ImageView image = view.findViewById(R.id.image);

        StorageReference imgReference = firebaseStorage.getReference().child("Salons").child(salonID).child("displayImages").child(allDisplayImages.get(position));

        Glide.with(context)
             .load(imgReference)
             .addListener(new RequestListener<Drawable>() {
                 @Override
                 public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                     return false;
                 }

                 @Override
                 public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                     progressLoader.setVisibility(View.GONE);
                     return false;
                 }
             })
             .into(image);

        parent.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return allDisplayImages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
