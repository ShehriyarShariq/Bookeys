package com.studio.millionares.barberbooker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SignInFragment extends Fragment {

    RelativeLayout emailLayout, phoneNumLayout, passwordLayout;
    EditText email, phoneNum, password;
    TextView forgotPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        emailLayout = view.findViewById(R.id.email_layout);
        phoneNumLayout = view.findViewById(R.id.phone_number_layout);
        passwordLayout = view.findViewById(R.id.password_layout);
        email = view.findViewById(R.id.email_input);
        phoneNum = view.findViewById(R.id.phone_number_input);
        password = view.findViewById(R.id.password_input);
        forgotPassword = view.findViewById(R.id.forgot_password_txt_btn);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    phoneNumLayout.setAlpha((float) 0.3);
                    phoneNum.setFocusableInTouchMode(false);
                    phoneNum.setFocusable(false);
                } else if(s.toString().isEmpty() && password.getText().toString().isEmpty()) {
                    phoneNumLayout.setAlpha((float) 1.0);
                    phoneNum.setFocusableInTouchMode(true);
                    phoneNum.setFocusable(true);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    phoneNumLayout.setAlpha((float) 0.3);
                    phoneNum.setFocusableInTouchMode(false);
                    phoneNum.setFocusable(false);
                } else if(s.toString().isEmpty() && email.getText().toString().isEmpty()) {
                    phoneNumLayout.setAlpha((float) 1.0);
                    phoneNum.setFocusableInTouchMode(true);
                    phoneNum.setFocusable(true);
                }
            }
        });

        phoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    emailLayout.setAlpha((float) 0.3);
                    email.setFocusableInTouchMode(false);
                    email.setFocusable(false);

                    passwordLayout.setAlpha((float) 0.3);
                    password.setFocusableInTouchMode(false);
                    password.setFocusable(false);
                } else if(s.toString().isEmpty()) {
                    emailLayout.setAlpha((float) 1.0);
                    email.setFocusableInTouchMode(true);
                    email.setFocusable(true);

                    passwordLayout.setAlpha((float) 1.0);
                    password.setFocusableInTouchMode(true);
                    password.setFocusable(true);
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
            }
        });

        return view;
    }
}
