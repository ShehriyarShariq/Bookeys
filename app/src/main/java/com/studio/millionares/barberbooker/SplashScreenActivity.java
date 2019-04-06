package com.studio.millionares.barberbooker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreenActivity extends AppCompatActivity {

    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        String email = sharedPreferences.getString("email", "");
        String password = sharedPreferences.getString("password", "");

        if (email.equals("")) {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                            /*firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("salonUpdates").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean isUpdateAvailable = false;

                                    for(DataSnapshot salonID : dataSnapshot.getChildren()){
                                        if(salonID.getKey().equals("salonID")){
                                            continue;
                                        }

                                        isUpdateAvailable = true;
                                    }

                                    if(isUpdateAvailable){
                                        firebaseDatabase.child("Salons").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });*/

                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            });
        }
    }
}
