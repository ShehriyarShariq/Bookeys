package com.studio.millionares.barberbooker;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.TextView;

public class LoaderDialog {

    /*
        MULTI-PURPOSE MESSAGE DIALOG BOX
    */

    Activity activity;
    Dialog dialog;
    String type; // Dialog type

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
            case "InfoLoader": // Data Loader Dialog
                label.setText("Loading...");
                break;
            case "Process": // Data Processing Dialog
                label.setText("Processing...");
                break;
            case "Send": // Data Sending Dialog
                label.setText("Sending...");
                break;
        }

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
