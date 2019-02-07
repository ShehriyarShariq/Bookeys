package com.studio.millionares.barberbooker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private final int SIGNUP_VERIFICATION_REQ = 104;
    private final int SIGNIN_VERIFICATION_REQ = 204;

    ViewPager loginPager;
    CardView continueBtn;
    TabLayout tabLayout;

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

        loginPager.setAdapter(loginPagerAdapter);
        loginPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // For first page initially
        if(loginPager.getCurrentItem() == 0){
            continueBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final View view = loginPager.getChildAt(0);
                    //final RelativeLayout emailLayout = view.findViewById(R.id.email_layout);
                    //final RelativeLayout phoneNumLayout = view.findViewById(R.id.phone_number_layout);
                    final EditText email = view.findViewById(R.id.email_input);
                    final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                    final EditText password = view.findViewById(R.id.password_input);
                    final RelativeLayout loginError = view.findViewById(R.id.login_error);
                    final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);
                    //final CheckBox rememberMe = view.findViewById(R.id.remember_me_checkbox);

                    if(email.isFocusable() && !phoneNum.isFocusable()){ // Email login
                        if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
                            loginError.setVisibility(View.INVISIBLE);
                            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                            //SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                            //final SharedPreferences.Editor editor = sharedPreferences.edit();

                            firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                // Before logging in, checks if phone number is validated

                                                String phoneNum = "", isPhoneNumVerified = "false";

                                                for(DataSnapshot customerDetails : dataSnapshot.getChildren()){
                                                    if(customerDetails.getKey().equals("phoneNum")){
                                                        phoneNum = customerDetails.getValue().toString().trim();
                                                    } else if(customerDetails.getKey().equals("verified")){
                                                        for(DataSnapshot type : customerDetails.getChildren()){
                                                            if(type.getKey().equals("phoneNum")){
                                                                isPhoneNumVerified = type.getValue().toString().trim();
                                                            }
                                                        }
                                                    }
                                                }

                                                if(isPhoneNumVerified.equals("false")){ // Phone number not validated
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.INVISIBLE);

                                                    // Opens phone number verification activity

                                                    Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                                                    Bundle phoneNumberCarrier = new Bundle();
                                                    phoneNumberCarrier.putString("from", "signIn");
                                                    phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum);

                                                    verificationActivity.putExtras(phoneNumberCarrier);
                                                    startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);
                                                } else { // Phone number verified
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                }                                            }

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
                    } else if (phoneNum.isFocusable() && !email.isFocusable()) { // Phone number login
                        Toast.makeText(getApplicationContext(), "Phone number login", Toast.LENGTH_SHORT).show();

                        // For phone number login
                        if(!phoneNum.getText().toString().isEmpty()){
                            loginError.setVisibility(View.INVISIBLE);

                            if(!phoneNumValidation(phoneNum.getText().toString().trim())){
                                loginError.setVisibility(View.VISIBLE);
                                loginErrorStatus.setText("Incorrect Phone #");
                            } else {
                                //final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                                //SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                                //final SharedPreferences.Editor editor = sharedPreferences.edit();

                                // Opens phone verification activity

                                Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                                Bundle phoneNumberCarrier = new Bundle();
                                phoneNumberCarrier.putString("from", "signIn");
                                phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());

                                verificationActivity.putExtras(phoneNumberCarrier);
                                startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);


                            }

                        } else { // Phone number not provided
                            loginError.setVisibility(View.VISIBLE);
                            loginErrorStatus.setText("Fill required fields");
                        }
                    } else {
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
                switch (position){
                    case 0: // Signin page
                        continueBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final View view = loginPager.getChildAt(0);
                                //final RelativeLayout emailLayout = view.findViewById(R.id.email_layout);
                                //final RelativeLayout phoneNumLayout = view.findViewById(R.id.phone_number_layout);
                                final EditText email = view.findViewById(R.id.email_input);
                                final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                                final EditText password = view.findViewById(R.id.password_input);
                                final RelativeLayout loginError = view.findViewById(R.id.login_error);
                                final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);
                                //final CheckBox rememberMe = view.findViewById(R.id.remember_me_checkbox);

                                if(email.isFocusable() && !phoneNum.isFocusable()){
                                    //Toast.makeText(getApplicationContext(), "email login", Toast.LENGTH_SHORT).show();
                                    if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
                                        loginError.setVisibility(View.INVISIBLE);
                                        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                                        //SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                                        //final SharedPreferences.Editor editor = sharedPreferences.edit();

                                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    progressDialog.dismiss();
                                                    loginError.setVisibility(View.VISIBLE);
                                                    loginErrorStatus.setText("Incorrect Email/ Password!");
                                                }
                                            }
                                        });

                                    } else {
                                        loginError.setVisibility(View.VISIBLE);
                                        loginErrorStatus.setText("All Fields must be provided!");
                                    }
                                } else if (phoneNum.isFocusable() && !email.isFocusable()) {
                                    Toast.makeText(getApplicationContext(), "Phone number login", Toast.LENGTH_SHORT).show();

                                    // For phone number login
                                    if(!phoneNum.getText().toString().isEmpty()){
                                        loginError.setVisibility(View.INVISIBLE);

                                        if(!phoneNumValidation(phoneNum.getText().toString().trim())){
                                            loginError.setVisibility(View.VISIBLE);
                                            loginErrorStatus.setText("Incorrect Phone #");
                                        } else {
                                            //final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                                            //SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                                            //final SharedPreferences.Editor editor = sharedPreferences.edit();

                                            Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                                            Bundle phoneNumberCarrier = new Bundle();
                                            phoneNumberCarrier.putString("from", "signIn");
                                            phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());

                                            verificationActivity.putExtras(phoneNumberCarrier);
                                            startActivityForResult(verificationActivity, SIGNIN_VERIFICATION_REQ);


                                        }

                                    } else {
                                        loginError.setVisibility(View.VISIBLE);
                                        loginErrorStatus.setText("All Fields must be provided!");
                                    }
                                } else {
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
                                final EditText username = view.findViewById(R.id.username_input);
                                final EditText email =  view.findViewById(R.id.email_input);
                                final EditText password = view.findViewById(R.id.password_input);
                                final EditText confirmPassword = view.findViewById(R.id.confirm_password_input);
                                final EditText phoneNum = view.findViewById(R.id.phone_number_input);
                                final RelativeLayout loginError = view.findViewById(R.id.login_error);
                                final TextView loginErrorStatus = view.findViewById(R.id.login_error_status);

                                SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
                                final SharedPreferences.Editor editor = sharedPreferences.edit();

                                if(!username.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !confirmPassword.getText().toString().isEmpty() && !phoneNum.getText().toString().isEmpty()){
                                    loginError.setVisibility(View.INVISIBLE);

                                    if(password.getText().toString().length() < 6){
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
                                                            progressDialog.dismiss();
                                                            loginError.setVisibility(View.INVISIBLE);

                                                            final String userID = firebaseAuth.getCurrentUser().getUid();

                                                            Customer customer = new Customer(username.getText().toString().trim(), email.getText().toString().trim(), phoneNum.getText().toString().trim(), "none");
                                                            firebaseDatabase.child("Customers").child(userID).setValue(customer.getCustomerMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    progressDialog.dismiss();
                                                                    if(task.isSuccessful()){
                                                                        Intent verificationActivity = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                                                                        Bundle phoneNumberCarrier = new Bundle();
                                                                        phoneNumberCarrier.putString("from", "signUp");
                                                                        phoneNumberCarrier.putString("userID", userID);
                                                                        phoneNumberCarrier.putString("username", username.getText().toString().trim());
                                                                        phoneNumberCarrier.putString("email", firebaseAuth.getCurrentUser().getEmail());
                                                                        phoneNumberCarrier.putString("password", password.getText().toString().trim());
                                                                        phoneNumberCarrier.putString("phoneNum", "+92" + phoneNum.getText().toString().trim());

                                                                        verificationActivity.putExtras(phoneNumberCarrier);
                                                                        startActivityForResult(verificationActivity, SIGNUP_VERIFICATION_REQ);

                                                                    } else {
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

        if(requestCode == SIGNUP_VERIFICATION_REQ){ // From signup phone verification
            if(resultCode == Activity.RESULT_OK){ // If code successfully entered
                loginPager.setCurrentItem(0, true);

                // Login through email and password

                final String userID = data.getStringExtra("userID");
                final String email = data.getStringExtra("email");
                String password = data.getStringExtra("password");

                final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            firebaseDatabase.child("Customers").child(userID).child("verified").child("phoneNum").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        } else if(requestCode == SIGNIN_VERIFICATION_REQ){ // Phone number verification on  signin
            if(resultCode == Activity.RESULT_OK){
                final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Signing In...", "Processing...", true);
                final String verificationID = data.getStringExtra("verificationID");
                final String code = data.getStringExtra("code");

                // Signin through phone credentials

                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, code);
                firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(firebaseAuth.getCurrentUser().getEmail() == null) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Invalid Number!", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                            } else {
                                if(firebaseAuth.getCurrentUser().getEmail().isEmpty()){
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Invalid Number!", Toast.LENGTH_SHORT).show();
                                    firebaseAuth.signOut();
                                } else {

                                    firebaseDatabase.child("Customers").child(firebaseAuth.getCurrentUser().getUid()).child("verified").child("phoneNum").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            if(task.isSuccessful()){
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
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

    private boolean phoneNumValidation(String phoneNum){
        boolean isValid = true;

        if(phoneNum.length() != 10){
            isValid = false;
        }

        for(int i = 0; i < phoneNum.length(); i++){
            if((phoneNum.charAt(i) > '9') && (phoneNum.charAt(i) < '0')){
                isValid = false;
            }
        }

        return isValid;
    }
}
