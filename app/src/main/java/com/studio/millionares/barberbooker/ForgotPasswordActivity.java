package com.studio.millionares.barberbooker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    EditText emailInput;
    RelativeLayout nextBtnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Custom toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nextBtnLayout = findViewById(R.id.next_btn_layout);
        emailInput = findViewById(R.id.email_input);

        nextBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!emailInput.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailInput.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){ // Email sent
                                Toast.makeText(getApplicationContext(), "Email sent", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            } else { // Email not sent
                                Toast.makeText(getApplicationContext(), "Some error occurred! Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else { // No email entered
                    Toast.makeText(getApplicationContext(), "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
