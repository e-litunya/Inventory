package com.copycat.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email,password,retype_password,phone;
    private Button register;
    private FirebaseAuth firebaseAuth;
    private String userEmail,userPassword;
    private CountryCodePicker countryCodePicker;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    private static final String PASSWORD_PATTERN="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sharedPreferences=getSharedPreferences(Constants.LOCALDB,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        firebaseAuth=FirebaseAuth.getInstance();
        countryCodePicker=findViewById(R.id.country_code);
        phone=findViewById(R.id.telephone);
        email=findViewById(R.id.userEmail);
        password=findViewById(R.id.userPassword);
        retype_password=findViewById(R.id.userRetypePassword);
        register=findViewById(R.id.btn_Register);
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.AppName);
        progressDialog.setIcon(R.mipmap.ic_launcher);
        countryCodePicker.registerCarrierNumberEditText(phone);
        register.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser!=null)
        {
            reload();
        }
    }

    private void reload() {
    }

    private boolean validatePasswordComplexity(String userPassword)
    {
        Matcher matcher=PATTERN.matcher(userPassword);

        return matcher.matches();
    }

    private boolean passwordMatch(String userPassword,String passwordRetype)
    {

        return TextUtils.equals(userPassword,passwordRetype);
    }

    @Override
    public void onClick(View v) {

        if (!checkEmptyFields())
        {
            //check for matching password
            if (passwordMatch(password.getText().toString(),retype_password.getText().toString())){
               //check complexity
                if (validatePasswordComplexity(password.getText().toString()))
                {
                    //proceed with registration

                    userEmail=email.getText().toString();
                    userPassword=retype_password.getText().toString();
                    String phoneNumber=countryCodePicker.getFullNumberWithPlus();

                    registerUser(userEmail,userPassword);
                    editor.putString(Constants.PHONE_NUMBER,phoneNumber);
                    editor.apply();
                }
                else
                {
                   makeNotifications(getResources().getString(R.string.complexPassword));
                }
            }
            else
            {
                makeNotifications(getResources().getString(R.string.mismatch));
            }
        }

    }

    private boolean checkEmptyFields()
    {
        boolean isEmpty;
        if(TextUtils.isEmpty(email.getText().toString())){
            makeNotifications(getResources().getString(R.string.emailEmpty));
            isEmpty=true;
        }
        else if(TextUtils.isEmpty(password.getText().toString()))
        {
            makeNotifications(getResources().getString(R.string.passwordEmpty));
            isEmpty=true;
        }
        else if(TextUtils.isEmpty(retype_password.getText().toString()))
        {
            makeNotifications(getResources().getString(R.string.passwordEmpty));
            isEmpty=true;
        }
        else if(TextUtils.isEmpty(phone.getText().toString()))
        {
            makeNotifications(getResources().getString(R.string.phoneEmpty));
            isEmpty=true;
        }
        else {
            isEmpty=false;
        }


        return isEmpty;
    }

    private  void makeNotifications(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void registerUser(String accountEmail,String accountPassword)
    {

        setProgressDialogMessage(getString(R.string.signup));
        firebaseAuth.createUserWithEmailAndPassword(accountEmail,accountPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            //send verification email
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            if(user!=null)
                            {

                                sendEmailVerification(user);
                            }

                        }
                    }
                });
    }

    private  void sendEmailVerification(FirebaseUser newUser)
    {


        newUser.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            setProgressDialogMessage(getString(R.string.accountSuccess));
                            editor.putBoolean(Constants.SIGNUP,true);
                            editor.apply();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            },2500);

                        }
                    }
                });
    }

    private void setProgressDialogMessage(String message)
    {
        if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }

        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
}