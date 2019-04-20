package com.studio.millionares.barberbooker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {

    RelativeLayout codeErrorLayout;
    View focusDeflectionView;
    CardView firstDigitInpCard, secondDigitInpCard, thirdDigitInpCard, fourthDigitInpCard, fifthDigitInpCard, sixthDigitInpCard;
    EditText firstDigitInp, secondDigitInp, thirdDigitInp, fourthDigitInp, fifthDigitInp, sixthDigitInp;
    TextView resendCodeBtn;

    String phoneNum;
    private String verificationId;
    private String smsCode;
    private boolean isFromSignUpFrag;

    private String userID, username, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        // Initialize Views
        codeErrorLayout = findViewById(R.id.code_error_layout);
        focusDeflectionView = findViewById(R.id.focus_deflection_view);

        resendCodeBtn = findViewById(R.id.resend_code_btn_txt);

        firstDigitInpCard = findViewById(R.id.code_inp_first_layout);
        secondDigitInpCard = findViewById(R.id.code_inp_second_layout);
        thirdDigitInpCard = findViewById(R.id.code_inp_third_layout);
        fourthDigitInpCard = findViewById(R.id.code_inp_fourth_layout);
        fifthDigitInpCard = findViewById(R.id.code_inp_fifth_layout);
        sixthDigitInpCard = findViewById(R.id.code_inp_sixth_layout);

        firstDigitInp = findViewById(R.id.first_digit_inp);
        secondDigitInp = findViewById(R.id.second_digit_inp);
        thirdDigitInp = findViewById(R.id.third_digit_inp);
        fourthDigitInp = findViewById(R.id.fourth_digit_inp);
        fifthDigitInp = findViewById(R.id.fifth_digit_inp);
        sixthDigitInp = findViewById(R.id.sixth_digit_inp);

        phoneNum = getIntent().getStringExtra("phoneNum");

        if(getIntent().getStringExtra("from").equals("signUp")){
            // Email and Password of customer
            isFromSignUpFrag = true;
            userID = getIntent().getStringExtra("userID");
            username = getIntent().getStringExtra("username");
            email = getIntent().getStringExtra("email");
            password = getIntent().getStringExtra("password");
        } else {
            isFromSignUpFrag = false;
        }

        // Send OTP at provided phone number
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                120,
                TimeUnit.SECONDS,
                PhoneVerificationActivity.this,
                mCallback
        );

        // For input views
        final EditText[] codeDigInputs = {firstDigitInp, secondDigitInp, thirdDigitInp, fourthDigitInp, fifthDigitInp, sixthDigitInp};

        final HashMap<EditText, CardView> inpToCardMap = new HashMap<>();
        inpToCardMap.put(firstDigitInp, firstDigitInpCard);
        inpToCardMap.put(secondDigitInp, secondDigitInpCard);
        inpToCardMap.put(thirdDigitInp, thirdDigitInpCard);
        inpToCardMap.put(fourthDigitInp, fourthDigitInpCard);
        inpToCardMap.put(fifthDigitInp, fifthDigitInpCard);
        inpToCardMap.put(sixthDigitInp, sixthDigitInpCard);

        // Original color of input box
        final ColorStateList cardOriginalColor = firstDigitInpCard.getCardBackgroundColor();

        // Change color of selected input box
        for(final EditText digInp : codeDigInputs){
            if(digInp.getOnFocusChangeListener() == null){
                digInp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            inpToCardMap.get(digInp).setCardBackgroundColor(Color.parseColor("#9C5AFF91"));
                        } else {
                            inpToCardMap.get(digInp).setCardBackgroundColor(cardOriginalColor);
                        }
                    }
                });
            }
        }

        // Automatically change focus of input box when filled
        for(int i = 0; i < codeDigInputs.length - 1; i++){
            final int finalI = i;

            codeDigInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    codeDigInputs[finalI].clearFocus();
                    codeDigInputs[finalI + 1].requestFocus();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String code = "";
                    for(EditText digInp : codeDigInputs){
                        if(!digInp.getText().toString().isEmpty()){
                            code += digInp.getText().toString().trim();
                        }
                    }

                    if(code.length() == 6){
                        Log.d("Code", code);
                        processCode(code);
                    }

                }
            });

        }

        // Last box
        codeDigInputs[codeDigInputs.length - 1].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove all focus
                codeDigInputs[codeDigInputs.length - 1].clearFocus();
                focusDeflectionView.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verify entered code
                String code = "";
                for(EditText digInp : codeDigInputs){
                    if(!digInp.getText().toString().isEmpty()){
                        code += digInp.getText().toString().trim();
                    }
                }

                if(code.length() == 6){
                    Log.d("Code", code);
                    processCode(code);
                }
            }
        });

        resendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNum,
                        120,
                        TimeUnit.SECONDS,
                        PhoneVerificationActivity.this,
                        mCallback
                );
            }
        });


    }

    private void processCode(String userInpCode){
        boolean isValidCode = isCodeValid(userInpCode);

        if(smsCode != null){
            codeErrorLayout.setVisibility(View.GONE);
            if(isValidCode){
                codeErrorLayout.setVisibility(View.GONE);

                if(userInpCode.equals(smsCode)){
                    codeErrorLayout.setVisibility(View.GONE);
                    verifyCode(smsCode);
                } else {
                    Toast.makeText(getApplicationContext(), "02Error", Toast.LENGTH_SHORT).show();
                }

            } else {
                codeErrorLayout.setVisibility(View.VISIBLE);
            }
        } else {
            codeErrorLayout.setVisibility(View.VISIBLE);
        }
    }

    private void verifyCode(final String code){
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code);
        if(isFromSignUpFrag){
            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseAuth.getInstance().signOut();
                        Intent returnSuccessIntent = new Intent();
                        returnSuccessIntent.putExtra("userID", userID);
                        returnSuccessIntent.putExtra("username", username);
                        returnSuccessIntent.putExtra("email", email);
                        returnSuccessIntent.putExtra("phoneNum", phoneNum);
                        returnSuccessIntent.putExtra("password", password);
                        setResult(Activity.RESULT_OK, returnSuccessIntent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Some error occurred! Try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() == null){
                    FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseAuth.getInstance().signOut();
                                Intent returnSuccessIntent = new Intent();
                                returnSuccessIntent.putExtra("verificationID", verificationId);
                                returnSuccessIntent.putExtra("code", code);
                                setResult(Activity.RESULT_OK, returnSuccessIntent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Some error occurred! Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    FirebaseAuth.getInstance().signOut();
                    Intent returnSuccessIntent = new Intent();
                    returnSuccessIntent.putExtra("verificationID", verificationId);
                    returnSuccessIntent.putExtra("code", code);
                    setResult(Activity.RESULT_OK, returnSuccessIntent);
                    finish();
                }
            } else {
                Intent returnSuccessIntent = new Intent();
                returnSuccessIntent.putExtra("verificationID", verificationId);
                returnSuccessIntent.putExtra("code", code);
                setResult(Activity.RESULT_OK, returnSuccessIntent);
                finish();
            }
        }

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            //super.onCodeSent(s, forceResendingToken);
            verificationId = s;

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            codeErrorLayout.setVisibility(View.GONE);
            smsCode = phoneAuthCredential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            codeErrorLayout.setVisibility(View.VISIBLE);
        }
    };

    private boolean isCodeValid(String code){
        char[] codeDigits = code.toCharArray();

        for(char digit : codeDigits){
            if((Integer.parseInt(String.valueOf(digit)) > 9) || (Integer.parseInt(String.valueOf(digit)) < 0)){
                return false;
            }
        }

        return true;
    }

}
