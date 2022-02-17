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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private TextView passwordReset;
    private Button signIn, phoneSignIn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences myPrefs;
    private ArrayList<String> customers;
    private String mVerificationID;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mVerificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            setProgressDialogMessage(getString(R.string.verificationCompleted), true);

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID, code);
                signInwithPhoneNumber(credential);
            }


        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                setProgressDialogMessage(e.getMessage(), true);
            } else if (e instanceof FirebaseTooManyRequestsException) {
                setProgressDialogMessage(e.getMessage(), true);
            } else {
                setProgressDialogMessage(e.getMessage(), true);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            setProgressDialogMessage(getString(R.string.codeSent), true);
            mVerificationID = s;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        myPrefs = getSharedPreferences(Constants.LOCALDB, MODE_PRIVATE);
        email = findViewById(R.id.sign_userEmail);
        password = findViewById(R.id.sign_userPassword);
        passwordReset = findViewById(R.id.pass_reset);
        signIn = findViewById(R.id.btn_SignON);
        phoneSignIn = findViewById(R.id.btn_PhoneSign);
        signIn.setOnClickListener(this);
        phoneSignIn.setOnClickListener(this);
        passwordReset.setOnClickListener(this);
        Linkify.addLinks(passwordReset, Linkify.WEB_URLS);
        customers = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setIcon(R.mipmap.ic_ccllogo);
        progressDialog.setTitle(R.string.AppName);


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
        } else if (v == phoneSignIn) {
            setProgressDialogMessage(getString(R.string.phoneSign), true);

            String phoneNumber = myPrefs.getString(Constants.PHONE_NUMBER, Constants.DEF_PHONE);
            //notify progress text
            startPhoneNumberVerification(phoneNumber);


        }

    }


    private void signInWithPassword(String userEmail, String userPassword) {
        setProgressDialogMessage(getString(R.string.passwordSignIn), false);
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        } else {
            // proceed with signIn

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            customers = getCustomers();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            setProgressDialogMessage(getString(R.string.logOncomplete), false);
                            Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                progressDialog.dismiss();
                                launchInventory(user, null);
                            }, 3000);

                        }

                    }).addOnFailureListener(this, e -> {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            setProgressDialogMessage(getString(R.string.invalidPassword), true);
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            setProgressDialogMessage(getString(R.string.invalidEmail), true);
                        } else {
                            setProgressDialogMessage(e.getMessage(), true);
                        }
                    });
        }
    }

    private void launchInventory(FirebaseUser user, @Nullable String emailID) {

        String[] customerNames = new String[]{};
        String[] customerNamesDistinct;
        Intent mainIntent = new Intent(this, MainActivity.class);
        if (emailID == null) {
            mainIntent.putExtra(Constants.USER, user.getEmail());
        } else {
            mainIntent.putExtra(Constants.USER, emailID);
        }
        mainIntent.putExtra(Constants.USERID, user);

        if (!customers.isEmpty()) {
            customerNames = customers.toArray(new String[customers.size()]);

        }
        customerNamesDistinct = Arrays.stream(customerNames).distinct().toArray(String[]::new);
        mainIntent.putExtra(Constants.CUSTOMER_lABEL, customerNamesDistinct);
        startActivity(mainIntent);
    }

    private void signInwithPhoneNumber(PhoneAuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        customers = getCustomers();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        String myEmail = getSharedPreferences(Constants.LOCALDB, MODE_PRIVATE).getString(Constants.USER_EMAIL, getString(R.string.emptyString));
                        setProgressDialogMessage(getString(R.string.logOncomplete), false);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            progressDialog.dismiss();
                            launchInventory(user, myEmail);
                        }, 3000);
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
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
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            setProgressDialogMessage(getString(R.string.checkEmail), true);

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
            handler.postDelayed(() -> progressDialog.dismiss(), 3000);
        }
    }

    private ArrayList<String> getCustomers() {
        ArrayList<String> customers = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query customerQuery = databaseReference.orderByKey();

        customerQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SystemInventory systemInventory = dataSnapshot.getValue(SystemInventory.class);
                        if (systemInventory != null) {
                            customers.add(systemInventory.getCustomerName());
                        }
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "No Data", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return customers;
    }


}