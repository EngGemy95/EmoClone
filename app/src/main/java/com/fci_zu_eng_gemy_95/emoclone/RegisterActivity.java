package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private String checker = "", phoneNumber = "";
    private ActivityRegisterBinding binding;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        binding.ccp.registerCarrierNumberEditText(binding.phoneText);

        binding.continueNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.continueNextButton.getText().equals("Submit") || checker.equals("CodeSent")) {
                    String verificationCode = binding.codeText.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(RegisterActivity.this, "Please write verification code first", Toast.LENGTH_SHORT).show();
                    }else {
                        loadingBar.setTitle("code Verification");
                        loadingBar.setMessage("Please wait , while we are verifying your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                } else {
                    phoneNumber = binding.ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait , while we are verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                60,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                RegisterActivity.this,               // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallbacks
                    } else {
                        Toast.makeText(RegisterActivity.this, "Please write valid phone number ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegisterActivity.this, "Invalid Phone Number...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                binding.layoutPhoneAuth.setVisibility(View.VISIBLE);

                binding.continueNextButton.setText("Continue");
                binding.codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken ;
                
                binding.layoutPhoneAuth.setVisibility(View.GONE);
                checker = "CodeSent";
                binding.continueNextButton.setText("Submit");
                binding.codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegisterActivity.this, "Code has been sent , please check ", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Congratulations, you are logged in successfully.", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error : " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null){
            startActivity(new Intent(RegisterActivity.this, ContactsActivity.class));
            finish();
        }
    }
}

