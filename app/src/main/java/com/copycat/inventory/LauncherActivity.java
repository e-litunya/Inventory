package com.copycat.inventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.copycat.inventory.databinding.ActivityLauncherBinding;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LauncherActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView animText;
    private CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.copycat.inventory.databinding.ActivityLauncherBinding binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        sharedPreferences = getSharedPreferences(Constants.LOCALDB, MODE_PRIVATE);
        animText = binding.getRoot().findViewById(R.id.animText);
        circleImageView = binding.getRoot().findViewById(R.id.logo);
        splashAnimations();
        Handler handler = new Handler();
        handler.postDelayed(this::launchActivity, 12000);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.

    }


    private void launchActivity() {
        boolean signUp = sharedPreferences.getBoolean(Constants.SIGNUP, false);
        Intent intent;
        if (!signUp) {
            intent = new Intent(this, RegisterActivity.class);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }
        startActivity(intent);
    }

    private void splashAnimations() {
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360f);
        long delay = 1000L;
        rotateAnimation.setDuration(delay);
        int count = 6;
        rotateAnimation.setRepeatCount(count);
        rotateAnimation.setRepeatMode(Animation.REVERSE);
        circleImageView.setAnimation(rotateAnimation);
        circleImageView.startAnimation(rotateAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(delay);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(count);
        animText.setAnimation(alphaAnimation);
        updateAnimText();

    }

    private void updateAnimText() {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {

                    switch (i) {
                        case 0:
                            animText.setText(R.string.agile);
                            break;
                        case 1:
                            animText.setText(R.string.bold);
                            break;
                        case 2:
                            animText.setText(R.string.creative);
                            break;
                        case 3:
                            animText.setText(R.string.dynamic);
                            break;
                        case 4:
                            animText.setText(R.string.moto);
                            break;
                        default:

                    }

                    Thread.sleep(2000);

                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }).start();
    }
}