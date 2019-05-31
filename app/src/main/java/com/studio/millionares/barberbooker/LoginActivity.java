package com.studio.millionares.barberbooker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // REQUEST CODES
    private final int SIGNUP_VERIFICATION_REQ = 104;
    private final int SIGNIN_VERIFICATION_REQ = 204;

    ViewPager loginPager;
    TabLayout tabLayout;
    CardView continueBtn;

    LoginPagerAdapter loginPagerAdapter;

    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        continueBtn = findViewById(R.id.continue_btn);

        // Setting the tabs for the viewpager
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("SIGN IN"));
        tabLayout.addTab(tabLayout.newTab().setText("SIGN UP"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.setTabTextColors(Color.parseColor("#ffffff"), Color.parseColor("#aaf2ff"));

        // Initialize firebase references
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        loginPager = findViewById(R.id.loginPager);
        loginPagerAdapter = new LoginPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        loginPager.setCurrentItem(0);
        loginPager.setSaveFromParentEnabled(false);
        loginPager.setOffscreenPageLimit(tabLayout.getTabCount() - 1);

        loginPager.setAdapter(loginPagerAdapter);
        loginPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // For first page initially
        if (loginPager.getCurrentItem() == 0) { // SIGN IN Page
            continueBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Initialize Views
                    final View view = loginPager.getChildAt(0);
                    final EditText email = view.findViewById(R.id.email_input);
//                    final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                    final EditText password = view.findViewById(R.id.password_input);
                    final RelativeLayout loginError = view.findViewById(R.id.login_error);
                    final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);

                    if (email.isFocusable()) { // && !phoneNum.isFocusable()) { // Email login
                        if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                            loginError.setVisibility(View.INVISIBLE);
                            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);

                            firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        firebaseDatabase.child("Customers").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                        if (user.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                                                            String phoneNum = "", isPhoneNumVerified = "false";

                                                            for (DataSnapshot details : user.getChildren()) {
                                                                if (details.getKey().equals("phoneNum")) {
                                                                    phoneNum = details.getValue().toString().trim();
                                                                } else if (details.getKey().equals("verified")) {
                                                                    for (DataSnapshot type : details.getChildren()) {
                                                                        if (type.getKey().equals("phoneNum")) {
                                                                            isPhoneNumVerified = type.getValue().toString().trim();
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            // Check if email verified
                                                            if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                                progressDialog.dismiss();

                                                                // Dialog for notification of email sent
                                                                AlertDialog.Builder emailVerificationBuilder = new AlertDialog.Builder(LoginActivity.this);
                                                                emailVerificationBuilder.setTitle("Email Not Verified!");
                                                                emailVerificationBuilder.setMessage("Verify your email to login. Would you like to receive another verification email?");
                                                                emailVerificationBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(final DialogInterface dialog, int which) {
                                                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                Toast.makeText(LoginActivity.this, "Verification email sent @ " + email.getText().toString(), Toast.LENGTH_SHORT).show();
                                                                                firebaseAuth.signOut();
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                emailVerificationBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                });
                                                                AlertDialog emailVerificationDialog = emailVerificationBuilder.create();
                                                                emailVerificationDialog.show();
                                                            } else {
                                                                progressDialog.dismiss();
                                                                loginError.setVisibility(View.INVISIBLE);

                                                                // Store email and password locally for future instant login
                                                                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                                                editor.putString("email", email.getText().toString());
                                                                editor.putString("password", password.getText().toString());
                                                                editor.commit();

                                                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                                finish();

//                                                                // Phone Number verification check
//                                                                if (isPhoneNumVerified.equals("false")) { // Phone number not validated
//                                                                    progressDialog.dismiss();
//                                                                    loginError.setVisibility(View.INVISIBLE);
//
//                                                                    // Opens phone number verification activity
//
//                                                                    Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
//                                                                    Bundle phoneNumberCarrier = new Bundle();
//                                                                    phoneNumberCarrier.putString("from", "signIn");
//                                                                    phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum);
//
//                                                                    verificationActivity.putExtras(phoneNumberCarrier);
//                                                                    startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);
//                                                                } else { // Phone number verified
//                                                                    progressDialog.dismiss();
//                                                                    loginError.setVisibility(View.INVISIBLE);
//
//                                                                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
//                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                                                                    editor.putString("email", email.getText().toString());
//                                                                    editor.putString("password", password.getText().toString());
//                                                                    editor.commit();
//
//                                                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                                                                    finish();
//                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    firebaseAuth.signOut();
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.VISIBLE);
                                                    loginErrorStatus.setText("Incorrect Email/ Password!");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else { // Email login failed
                                        progressDialog.dismiss();
                                        loginError.setVisibility(View.VISIBLE);
                                        loginErrorStatus.setText("Incorrect Email/ Password!");
                                    }
                                }
                            });

                        } else { // Email or password not provided
                            loginError.setVisibility(View.VISIBLE);
                            loginErrorStatus.setText("Fill required fields");
                        }
                    }
//                    else if (!email.isFocusable() && phoneNum.isFocusable()) { // Phone number login
//                        Toast.makeText(getApplicationContext(), "Phone number login", Toast.LENGTH_SHORT).show();
//
//                        // For phone number login
//                        if (!phoneNum.getText().toString().isEmpty()) {
//                            loginError.setVisibility(View.INVISIBLE);
//
//                            if (!phoneNumValidation(phoneNum.getText().toString().trim())) {
//                                loginError.setVisibility(View.VISIBLE);
//                                loginErrorStatus.setText("Incorrect Phone #");
//                            } else {
//                                Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
//                                Bundle phoneNumberCarrier = new Bundle();
//                                phoneNumberCarrier.putString("from", "signIn");
//                                phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());
//
//                                verificationActivity.putExtras(phoneNumberCarrier);
//                                startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);
//
//
//                            }
//
//                        } else { // Phone number not provided
//                            loginError.setVisibility(View.VISIBLE);
//                            loginErrorStatus.setText("Fill required fields");
//                        }
//                    }
                    else {
                        // Everything is empty
                        loginError.setVisibility(View.VISIBLE);
                        loginErrorStatus.setText("Fill required fields");
                    }
                }
            });
        }

        // For when page is changed
        loginPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                switch (position) {
                    case 0: // Signin page
                        continueBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Initialize Views
                                final View view = loginPager.getChildAt(0);
                                final EditText email = view.findViewById(R.id.email_input);
//                                final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                                final EditText password = view.findViewById(R.id.password_input);
                                final RelativeLayout loginError = view.findViewById(R.id.login_error);
                                final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);

                                if (email.isFocusable()) {// && !phoneNum.isFocusable()) { // Email login
                                    if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                                        loginError.setVisibility(View.INVISIBLE);
                                        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);

                                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    firebaseDatabase.child("Customers").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                                                for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                                    if (user.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                                                                        String phoneNum = "", isPhoneNumVerified = "false";

                                                                        for (DataSnapshot details : user.getChildren()) {
                                                                            if (details.getKey().equals("phoneNum")) {
                                                                                phoneNum = details.getValue().toString().trim();
                                                                            } else if (details.getKey().equals("verified")) {
                                                                                for (DataSnapshot type : details.getChildren()) {
                                                                                    if (type.getKey().equals("phoneNum")) {
                                                                                        isPhoneNumVerified = type.getValue().toString().trim();
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        // Check if email verified
                                                                        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                                            progressDialog.dismiss();
                                                                            AlertDialog.Builder emailVerificationBuilder = new AlertDialog.Builder(LoginActivity.this);
                                                                            emailVerificationBuilder.setTitle("Email Not Verified!");
                                                                            emailVerificationBuilder.setMessage("Verify your email to login. Would you like to receive another verification email?");
                                                                            emailVerificationBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(final DialogInterface dialog, int which) {
                                                                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                                            Toast.makeText(LoginActivity.this, "Verification email sent @ " + email.getText().toString(), Toast.LENGTH_SHORT).show();
                                                                                            firebaseAuth.signOut();
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                            emailVerificationBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    firebaseAuth.signOut();
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                            AlertDialog emailVerificationDialog = emailVerificationBuilder.create();
                                                                            emailVerificationDialog.show();
                                                                        } else {
                                                                            progressDialog.dismiss();
                                                                            loginError.setVisibility(View.INVISIBLE);

                                                                            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                                                            editor.putString("email", email.getText().toString());
                                                                            editor.putString("password", password.getText().toString());
                                                                            editor.commit();

                                                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                                            finish();

//                                                                            // Phone Number verification
//                                                                            if (isPhoneNumVerified.equals("false")) { // Phone number not validated
//                                                                                progressDialog.dismiss();
//                                                                                loginError.setVisibility(View.INVISIBLE);
//
//                                                                                // Opens phone number verification activity
//
//                                                                                Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
//                                                                                Bundle phoneNumberCarrier = new Bundle();
//                                                                                phoneNumberCarrier.putString("from", "signIn");
//                                                                                phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum);
//
//                                                                                verificationActivity.putExtras(phoneNumberCarrier);
//                                                                                startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);
//                                                                            } else { // Phone number verified
//                                                                                progressDialog.dismiss();
//                                                                                loginError.setVisibility(View.INVISIBLE);
//
//                                                                                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
//                                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                                                                                editor.putString("email", email.getText().toString());
//                                                                                editor.putString("password", password.getText().toString());
//                                                                                editor.commit();
//
//                                                                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                                                                                finish();
//                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                firebaseAuth.signOut();
                                                                progressDialog.dismiss();
                                                                loginError.setVisibility(View.VISIBLE);
                                                                loginErrorStatus.setText("Incorrect Email/ Password!");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else { // Email login failed
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.VISIBLE);
                                                    loginErrorStatus.setText("Incorrect Email/ Password!");
                                                }
                                            }
                                        });

                                    } else { // Email or password not provided
                                        loginError.setVisibility(View.VISIBLE);
                                        loginErrorStatus.setText("Fill required fields");
                                    }
                                }
//                                else if (phoneNum.isFocusable() && !email.isFocusable()) { // Phone number login
//                                    Toast.makeText(getApplicationContext(), "Phone number login", Toast.LENGTH_SHORT).show();
//
//                                    // For phone number login
//                                    if (!phoneNum.getText().toString().isEmpty()) {
//                                        loginError.setVisibility(View.INVISIBLE);
//
//                                        if (!phoneNumValidation(phoneNum.getText().toString().trim())) {
//                                            loginError.setVisibility(View.VISIBLE);
//                                            loginErrorStatus.setText("Incorrect Phone #");
//                                        } else {
//                                            Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
//                                            Bundle phoneNumberCarrier = new Bundle();
//                                            phoneNumberCarrier.putString("from", "signIn");
//                                            phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());
//
//                                            verificationActivity.putExtras(phoneNumberCarrier);
//                                            startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);
//
//
//                                        }
//
//                                    } else { // Phone number not provided
//                                        loginError.setVisibility(View.VISIBLE);
//                                        loginErrorStatus.setText("Fill required fields");
//                                    }
//                                }
                                else {
                                    // Everything is empty
                                    loginError.setVisibility(View.VISIBLE);
                                    loginErrorStatus.setText("Fill required fields");
                                }
                            }
                        });
                        break;
                    case 1: // Signup page
                        continueBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final View view = loginPager.getChildAt(position);
                                final ImageView profileImg = view.findViewById(R.id.img_profile);
                                final EditText username = view.findViewById(R.id.username_input);
                                final EditText email = view.findViewById(R.id.email_input);
                                final EditText password = view.findViewById(R.id.password_input);
                                final EditText confirmPassword = view.findViewById(R.id.confirm_password_input);
                                final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                                final RelativeLayout loginError = view.findViewById(R.id.login_error);
                                final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);

                                SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                                final SharedPreferences.Editor editor = sharedPreferences.edit();

                                if (!username.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !confirmPassword.getText().toString().isEmpty() && !phoneNum.getText().toString().isEmpty()) {
                                    loginError.setVisibility(View.INVISIBLE);

                                    if (password.getText().toString().length() < 6) {
                                        loginError.setVisibility(View.VISIBLE);
                                        loginErrorStatus.setText("Password must be at least 6 characters.");
                                    } else {
                                        loginError.setVisibility(View.INVISIBLE);

                                        if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                                            loginError.setVisibility(View.INVISIBLE);

                                            if (!phoneNumValidation(phoneNum.getText().toString().trim())) {
                                                loginError.setVisibility(View.VISIBLE);
                                                loginErrorStatus.setText("Incorrect Phone #");
                                            } else {
                                                final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing Up...", "Processing...", true);

                                                firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            loginError.setVisibility(View.INVISIBLE);

                                                            final String userID = firebaseAuth.getCurrentUser().getUid();

                                                            Customer customer = new Customer(username.getText().toString().trim(), email.getText().toString().trim(), password.getText().toString().trim(), phoneNum.getText().toString().trim(), "none");
                                                            firebaseDatabase.child("Customers").child(userID).setValue(customer.getCustomerMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
//                                                                        // Phone Number verification activity
//                                                                        Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
//                                                                        Bundle phoneNumberCarrier = new Bundle();
//                                                                        phoneNumberCarrier.putString("from", "signUp");
//                                                                        phoneNumberCarrier.putString("userID", userID);
//                                                                        phoneNumberCarrier.putString("username", username.getText().toString().trim());
//                                                                        phoneNumberCarrier.putString("email", firebaseAuth.getCurrentUser().getEmail());
//                                                                        phoneNumberCarrier.putString("password", password.getText().toString().trim());
//                                                                        phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());
//
//                                                                        verificationActivity.putExtras(phoneNumberCarrier);
//                                                                        startActivityForResult(verificationActivity, SIGNUP_VERIFICATION_REQ);

                                                                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                                                                .setDisplayName(username.getText().toString())
                                                                                .build();

                                                                        firebaseAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Bitmap bitmap = ((BitmapDrawable) profileImg.getDrawable()).getBitmap();

                                                                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                                                                byte[] imageByteData = byteArrayOutputStream.toByteArray();

                                                                                StorageReference profileImgRef = FirebaseStorage.getInstance().getReference().child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileImg.jpg");

                                                                                UploadTask uploadTask = profileImgRef.putBytes(imageByteData);
                                                                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                progressDialog.dismiss();
                                                                                                AlertDialog.Builder emailVerificationBuilder = new AlertDialog.Builder(LoginActivity.this);
                                                                                                emailVerificationBuilder.setTitle("Verification Email Sent!");
                                                                                                emailVerificationBuilder.setMessage("Email Sent. Verify to login.");
                                                                                                emailVerificationBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(final DialogInterface dialog, int which) {
                                                                                                        firebaseAuth.signOut();
                                                                                                        dialog.dismiss();
                                                                                                    }
                                                                                                });
                                                                                                AlertDialog emailVerificationDialog = emailVerificationBuilder.create();
                                                                                                emailVerificationDialog.show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        });


                                                                    } else {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(getApplicationContext(), "Some error occurred! Try again!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });


                                                        } else {
                                                            progressDialog.dismiss();
                                                            loginError.setVisibility(View.VISIBLE);
                                                            loginErrorStatus.setText("Invalid Email");
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            loginError.setVisibility(View.VISIBLE);
                                            loginErrorStatus.setText("Passwords don't match!");
                                        }
                                    }
                                } else {
                                    loginError.setVisibility(View.VISIBLE);
                                    loginErrorStatus.setText("All fields must be provided!");
                                }
                            }
                        });

                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loginPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // For expected results from the phone verification activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGNUP_VERIFICATION_REQ) { // From signup phone verification
            if (resultCode == Activity.RESULT_OK) { // If code successfully entered
                loginPager.setCurrentItem(0, true);

                // Login through email and password

                final String userID = data.getStringExtra("userID");
                final String email = data.getStringExtra("email");
                final String password = data.getStringExtra("password");

                final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseDatabase.child("Customers").child(userID).child("verified").child("phoneNum").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();

                                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString("email", email);
                                    editor.putString("password", password);
                                    editor.commit();

                                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        finish();
                                    } else {
                                        AlertDialog.Builder emailVerificationBuilder = new AlertDialog.Builder(LoginActivity.this);
                                        emailVerificationBuilder.setTitle("Verification Email Sent!");
                                        emailVerificationBuilder.setMessage("Email Sent. Verify to login.");
                                        emailVerificationBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {
                                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                        Toast.makeText(LoginActivity.this, "Verification email sent @ " + email.getText().toString(), Toast.LENGTH_SHORT).show();
                                                        firebaseAuth.signOut();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                        AlertDialog emailVerificationDialog = emailVerificationBuilder.create();
                                        emailVerificationDialog.show();
                                    }


                                }
                            });
                        }
                    }
                });
            }
        } else if (requestCode == SIGNIN_VERIFICATION_REQ) { // Phone number verification on  signin
            if (resultCode == Activity.RESULT_OK) {
                final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                final String verificationID = data.getStringExtra("verificationID");
                final String code = data.getStringExtra("code");

                // Signin through phone credentials

                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, code);
                firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (firebaseAuth.getCurrentUser().getEmail() == null) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Invalid Number!", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                            } else {
                                if (firebaseAuth.getCurrentUser().getEmail().isEmpty()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Invalid Number!", Toast.LENGTH_SHORT).show();
                                    firebaseAuth.signOut();
                                } else {

                                    firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("verified").child("phoneNum").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String email = firebaseAuth.getCurrentUser().getEmail();
                                                        String password = dataSnapshot.getValue().toString();

                                                        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                                        editor.putString("email", email);
                                                        editor.putString("password", password);
                                                        editor.commit();

                                                        if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                            finish();
                                                        } else {
                                                            AlertDialog.Builder emailVerificationBuilder = new AlertDialog.Builder(LoginActivity.this);
                                                            emailVerificationBuilder.setTitle("Email Not Verified!");
                                                            emailVerificationBuilder.setMessage("Verify your email to login. Would you like to receive another verification email?");
                                                            emailVerificationBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(final DialogInterface dialog, int which) {
                                                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                            Toast.makeText(LoginActivity.this, "Verification email sent @ " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                                                            firebaseAuth.signOut();
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                            emailVerificationBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                            AlertDialog emailVerificationDialog = emailVerificationBuilder.create();
                                                            emailVerificationDialog.show();
                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Some error occurred! Try again!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), verificationID + ", " + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    // Validating provided phone number
    private boolean phoneNumValidation(String phoneNum) {
        boolean isValid = true;

        if (phoneNum.length() != 10) {
            isValid = false;
        }

        for (int i = 0; i < phoneNum.length(); i++) {
            if ((phoneNum.charAt(i) > '9') && (phoneNum.charAt(i) < '0')) {
                isValid = false;
            }
        }

        return isValid;
    }
}
