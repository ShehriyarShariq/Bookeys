package com.studio.millionares.barberbooker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private final static int PENALTY = 50;

    TextView salonNameTxt, bookingDateTxt, bookingTimeTxt, bookingExpectedTimeTxt, barberNameTxt, netTotalTxt, totalCostTxt;
    RecyclerView servicesList;
    CardView cancelBookingButton;
    ImageView contactBtn;

    private String salonName, salonID, barberName, barberID, bookingDate, bookingStartTime, startTimeAmOrPm, bookingEndTime, status, bookingID;
    private String contactNumStr, amount;
    private int totalCost;
    private ArrayList<HashMap<String, String>> servicesAvailed;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    private Socket mSocket;
    private Boolean hasConnection = false;

    String currentDate, currentTime;

    BookingServicesListAdapter bookingServicesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        Intent appointmentIntent = getIntent();
        Appointment appointment = appointmentIntent.getParcelableExtra("appointment");
        HashMap<String, Object> appointmentDetails = appointment.getAppointmentDetails();

        salonNameTxt = findViewById(R.id.heading_label);
        bookingDateTxt = findViewById(R.id.booking_date);
        bookingTimeTxt = findViewById(R.id.booking_time);
        bookingExpectedTimeTxt = findViewById(R.id.expected_time);
        barberNameTxt = findViewById(R.id.selected_barber);
        servicesList = findViewById(R.id.services_list);
        cancelBookingButton = findViewById(R.id.cancel_booking_btn);
        contactBtn = findViewById(R.id.contact_btn);
        netTotalTxt = findViewById(R.id.net_total);
        totalCostTxt  = findViewById(R.id.total_cost);

        try {
            mSocket = IO.socket("https://bookies14.herokuapp.com/");
        } catch (URISyntaxException e) {
        }

        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if (!hasConnection) {

            mSocket.connect();
            mSocket.on("dateAndTime", getTrueDateTime);
        }

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        salonName = appointmentDetails.get("salonName").toString();
        totalCost = Integer.parseInt(appointmentDetails.get("amount").toString());

        //appointmentDetails.get("rating");
        //appointmentDetails.get("review");
        barberID = appointmentDetails.get("barberID").toString();
        barberName = appointmentDetails.get("barberName").toString();
        salonID = appointmentDetails.get("salonID").toString();
        status = appointmentDetails.get("status").toString();
        bookingID = appointmentDetails.get("bookingID").toString();
        servicesAvailed = (ArrayList<HashMap<String, String>>) appointmentDetails.get("servicesAvailed");
        contactNumStr = appointmentDetails.get("contactNum").toString();
        amount = appointmentDetails.get("amount").toString();

        // 88:88 PM on 25-03-2019
        bookingStartTime = getDateAndTime(appointmentDetails.get("dateAndTime").toString())[0];
        bookingDate = getDateAndTime(appointmentDetails.get("dateAndTime").toString())[1];
        startTimeAmOrPm = (appointmentDetails.get("dateAndTime").toString()).indexOf("AM") >= 0 ?
                (appointmentDetails.get("dateAndTime").toString()).substring((appointmentDetails.get("dateAndTime").toString()).indexOf("AM"), (appointmentDetails.get("dateAndTime").toString()).indexOf("AM") + 2) :
                (appointmentDetails.get("dateAndTime").toString()).substring((appointmentDetails.get("dateAndTime").toString()).indexOf("PM"), (appointmentDetails.get("dateAndTime").toString()).indexOf("PM") + 2)
        ;
        bookingEndTime = addTimeStrings(bookingStartTime, getAllServicesTotalTime(servicesAvailed));

        salonNameTxt.setText(salonName);
        bookingDateTxt.setText(bookingDate);
        bookingTimeTxt.setText(bookingStartTime + " to " + bookingEndTime);
        netTotalTxt.setText("R.s " + amount + "/-");
        totalCostTxt.setText("R.s " + amount + "/-");

        String[] servicesTotalTimeSplit = getAllServicesTotalTime(servicesAvailed).split(":");
        String expectedTimeFinal = Integer.parseInt(servicesTotalTimeSplit[0]) > 0 ? servicesTotalTimeSplit[0] + " hour " + servicesTotalTimeSplit[1] + " min" : servicesTotalTimeSplit[1] + " min";
        bookingExpectedTimeTxt.setText(expectedTimeFinal);

        barberNameTxt.setText(barberName);

        ArrayList<Service> allServices = new ArrayList<>();
        for(HashMap<String, String> service : servicesAvailed){
            allServices.add(new Service(service));
        }

        servicesList.setHasFixedSize(true);
        LinearLayoutManager servicesListLinearLayoutManager = new LinearLayoutManager(AppointmentDetailsActivity.this);
        servicesList.setLayoutManager(servicesListLinearLayoutManager);

        SelectedServicesListAdapter selectedServicesListAdapter = new SelectedServicesListAdapter(allServices);
        servicesList.setAdapter(selectedServicesListAdapter);

        LoaderDialog appointmentLoader = new LoaderDialog(AppointmentDetailsActivity.this, "InfoLoader");

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri phoneNumStrURI = Uri.parse("tel:+92" + contactNumStr);
                Intent phoneDialer = new Intent(Intent.ACTION_DIAL, phoneNumStrURI);
                startActivity(phoneDialer);
            }
        });

        cancelBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDate != null) {
                    if (bookingDate.equals(currentDate)) {
                        int currentHour = Integer.parseInt((currentTime.split(":"))[0]);
                        int bookingHour = Integer.parseInt((bookingStartTime.split(":"))[0]);

                        if ((bookingHour - currentHour) == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentDetailsActivity.this);
                            builder.setTitle("Are You Sure?");
                            builder.setMessage("Cancelling would result in a reduction of R.s.50 from your wallet. Press Ok to proceed.");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final LoaderDialog cancellationProcess = new LoaderDialog(AppointmentDetailsActivity.this, "Process");
                                    cancellationProcess.showDialog();

                                    firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("wallet").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int walletAmount = Integer.parseInt(dataSnapshot.getValue().toString());
                                            walletAmount -= PENALTY;
                                            final int finalWalletAmount = walletAmount;
                                            firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("wallet").setValue(walletAmount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("currentBooking").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                firebaseDatabase.child("Salons").child(salonID).child("bookedTimeSlots").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        firebaseDatabase.child("Salons").child(salonID).child("barbers").child(barberID).child("bookedTimeSlots").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    cancellationProcess.hideDialog();
                                                                                    startActivity(new Intent(AppointmentDetailsActivity.this, MyAppointmentsActivity.class));
                                                                                    finish();
                                                                                } else {
                                                                                    firebaseDatabase.child("Salons").child(salonID).child("barbers").child(barberID).child("bookedTimeSlots").child(bookingID).removeValue();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("wallet").setValue(finalWalletAmount + PENALTY).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                cancellationProcess.hideDialog();
                                                                Toast.makeText(AppointmentDetailsActivity.this, "Error Occured. Please try again later.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentDetailsActivity.this);
                        builder.setTitle("Are You Sure?");
                        builder.setMessage("Press Ok to proceed or press Cancel to go back.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final LoaderDialog cancellationProcess = new LoaderDialog(AppointmentDetailsActivity.this, "Process");
                                cancellationProcess.showDialog();

                                firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("currentBooking").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        firebaseDatabase.child("Salons").child(salonID).child("bookedTimeSlots").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                firebaseDatabase.child("Salons").child(salonID).child("barbers").child(barberID).child("bookedTimeSlots").child(bookingID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        cancellationProcess.hideDialog();
                                                        startActivity(new Intent(AppointmentDetailsActivity.this, MyAppointmentsActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, "Please try again in a few seconds...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    Emitter.Listener getTrueDateTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args != null) {
                        String[] fullTimeStampSplit = (args[0].toString()).split(" ");

                        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                        String month = (getIndexOf(months, fullTimeStampSplit[4]) + 1) < 10 ? "0" + (getIndexOf(months, fullTimeStampSplit[4]) + 1) : "" + (getIndexOf(months, fullTimeStampSplit[4]) + 1);
                        String date = (fullTimeStampSplit[5].length() != 2) ? ((Integer.parseInt(fullTimeStampSplit[5]) < 10) ? "0" + fullTimeStampSplit[5] : fullTimeStampSplit[5]) : fullTimeStampSplit[5];
                        String year = fullTimeStampSplit[6];

                        currentDate = date + "-" + month + "-" + year;

                        String[] timeSplit = fullTimeStampSplit[0].split(":");
                        currentTime = (Integer.parseInt(timeSplit[0]) + 5) + ":" + timeSplit[1];
                    }
                }
            });
        }
    };

    private int getIndexOf(String[] array, String str) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return -1;
    }

    private String[] getDateAndTime(String dateTimeStr) {
        // Sample: HH:mm PM on dd-MM-yyyy
        String spacesRemovedStr = dateTimeStr.replace(" ", "");
        String AmOrPmRemoved = (spacesRemovedStr.replace("AM", "")).equals(spacesRemovedStr) ? spacesRemovedStr.replace("PM", "") : spacesRemovedStr.replace("AM", "");
        String[] dateAndTimeSplit = AmOrPmRemoved.split("on");

        return dateAndTimeSplit;
    }

    private String addTimeStrings(String timeStamp1, String timeStamp2) {
        int timeStamp1Hours = Integer.parseInt((timeStamp1.split(":"))[0]);
        int timeStamp1Mins = Integer.parseInt((timeStamp1.split(":"))[1]);
        int timeStamp2Hours = Integer.parseInt((timeStamp2.split(":"))[0]);
        int timeStamp2Mins = Integer.parseInt((timeStamp2.split(":"))[1]);

        int finalHours = timeStamp1Hours + timeStamp2Hours;
        int finalMins = timeStamp1Mins + timeStamp2Mins;

        finalHours += finalMins / 60;
        finalMins = finalMins % 60;

        return fixTimeStamp(finalHours + ":" + finalMins);
    }

    private String getAllServicesTotalTime(ArrayList<HashMap<String, String>> servicesAvailed) {
        int minutes = 0;
        for (HashMap<String, String> service : servicesAvailed) {
            String expectedTime = service.get("expectedTime");
            int hours = 0, mins = 0;
            if (expectedTime.indexOf("H") >= 0) {
                hours = Integer.parseInt(expectedTime.substring(0, expectedTime.indexOf("H")));
                mins = Integer.parseInt(expectedTime.substring(expectedTime.indexOf("H") + 1, expectedTime.indexOf("M")));
            } else {
                mins = Integer.parseInt(expectedTime.substring(0, expectedTime.indexOf("M")));
            }

            minutes += (hours * 60) + mins;
        }

        int totalHours = minutes / 60;
        int totalMins = minutes % 60;

        return totalHours + ":" + totalMins;
    }

    private String fixTimeStamp(String timeStamp) {
        // Converts the timestamp from 24-hour format to 12-hour format

        String[] timeStampSplit = timeStamp.split(":");
        String hours = timeStampSplit[0];
        String suffix;

        if (Integer.parseInt(hours) > 12) { // If 13 hours or above in 24 hours
            timeStampSplit[0] = String.valueOf(Integer.parseInt(hours) % 12);
        }

        // At and after 12 in 24-hour clock, PM starts
        if (Integer.parseInt(hours) >= 12) {
            suffix = " PM";
        } else {
            suffix = " AM";
        }

        // Add 0 to single digits
        for (int i = 0; i < timeStampSplit.length; i++) {
            if (timeStampSplit[i].length() == 1) {
                timeStampSplit[i] = "0" + timeStampSplit[i];
            }
        }

        return timeStampSplit[0] + ":" + timeStampSplit[1] + suffix;
    }
}
