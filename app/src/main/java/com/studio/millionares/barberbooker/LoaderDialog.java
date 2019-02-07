package com.studio.millionares.barberbooker;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.TextView;

public class LoaderDialog {

    Activity activity;
    Dialog dialog;
    String type;

    public LoaderDialog(Activity activity, String type){
        this.activity = activity;
        this.type = type;
    }

    public void showDialog(){
        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loader_layout);

        TextView label = dialog.findViewById(R.id.label);
        switch (type){
            case "InfoLoader":
                label.setText("Loading...");
                break;
            case "BookingCompletion":
                label.setText("Processing...");
                break;
        }

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
