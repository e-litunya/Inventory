package com.copycat.inventory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private TextView passwordReset;
    private Button signIn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences myPrefs;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationID;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mVerificationCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        myPrefs = getSharedPreferences(Constants.LOCALDB, MODE_PRIVATE);
        email = findViewById(R.id.sign_userEmail);
        password = findViewById(R.id.sign_userPassword);
        passwordReset = findViewById(R.id.pass_reset);
        signIn = findViewById(R.id.btn_SignON);
        signIn.setOnClickListener(this);
        passwordReset.setOnClickListener(this);
        Linkify.addLinks(passwordReset, Linkify.WEB_URLS);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.AppName);
        mVerificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInwithPhoneNumber(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mResendToken = forceResendingToken;
                mVerificationID = s;
            }
        };
    }

    @Override
    public void onClick(View v) {

        if (v == signIn) {
            if (TextUtils.isEmpty(password.getText()) || TextUtils.isEmpty(email.getText())) {
                Toast.makeText(this, getString(R.string.emailPassEmpty), Toast.LENGTH_LONG).show();
            } else {
                try {
                    signInWithPassword(email.getText().toString(), password.getText().toString());
                } catch (Exception s) {
                    s.printStackTrace();
                }
            }
        } else if (v == passwordReset) {
            if (TextUtils.isEmpty(email.getText())) {
                Toast.makeText(this, getString(R.string.emailEmpty), Toast.LENGTH_LONG).show();
            } else {
                resetPassword(email.getText().toString());
            }
        }

    }


    private void signInWithPassword(String userEmail, String userPassword) throws Exception {
        setProgressDialogMessage(getString(R.string.passwordSignIn), false);
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        } else {
            // proceed with signIn

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                setProgressDialogMessage(getString(R.string.logOncomplete), false);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        launchInventory(user);
                                    }
                                }, 3000);

                            } else {

                                String phoneNumber = myPrefs.getString(Constants.PHONE_NUMBER, Constants.DEF_PHONE);
                                //notify progress text
                                setProgressDialogMessage(getString(R.string.phoneSignIn), false);
                                verifyPhoneNumber(phoneNumber);
                            }

                        }

                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        setProgressDialogMessage(getString(R.string.invalidPassword), true);
                    } else if (e instanceof FirebaseAuthInvalidUserException) {
                        setProgressDialogMessage(getString(R.string.invalidEmail), true);
                    } else {
                        setProgressDialogMessage(e.getMessage(), true);
                    }
                }
            });
        }
    }

    private void launchInventory(FirebaseUser user) {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra(Constants.USER, user.getEmail());
        mainIntent.putExtra(Constants.USERID, user);
        startActivity(mainIntent);
    }

    private void signInwithPhoneNumber(PhoneAuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            setProgressDialogMessage(getString(R.string.logOncomplete), false);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    launchInventory(user);
                                }
                            }, 3000);
                        }
                    }
                });
    }

    private void verifyPhoneNumber(String phoneNumber) {
        PhoneAuthOptions authOptions =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mVerificationCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(authOptions);
    }

    private void resetPassword(String emailID) {
        setProgressDialogMessage(getString(R.string.resetPassword), false);
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        } else {
            firebaseAuth.sendPasswordResetEmail(emailID)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                setProgressDialogMessage(getString(R.string.checkEmail), true);

                            }
                        }
                    });
        }
    }

    private void setProgressDialogMessage(String message, boolean timed) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.show();

        if (timed) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 3000);
        }
    }


}