package com.studio.millionares.barberbooker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText complaintTitle, complaintContent;
    CardView sendBtn;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        toolbar = findViewById(R.id.toolbar);
        complaintTitle = findViewById(R.id.complaint_title);
        complaintContent = findViewById(R.id.complaint_content);
        sendBtn = findViewById(R.id.send_btn);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoaderDialog sendingDialog = new LoaderDialog(FeedbackActivity.this, "Send");
                sendingDialog.showDialog();

                HashMap<String, String> feedbackMap = new HashMap<>();
                feedbackMap.put("customerID", firebaseAuth.getCurrentUser().getUid());
                feedbackMap.put("title", complaintTitle.getText().toString());
                feedbackMap.put("content", complaintContent.getText().toString());

                firebaseDatabase.child("Feedback").push().setValue(feedbackMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendingDialog.hideDialog();
                        finish();
                    }
                });
            }
        });

    }
}
