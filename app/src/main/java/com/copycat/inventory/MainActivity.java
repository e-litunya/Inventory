package com.copycat.inventory;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.copycat.inventory.databinding.ActivityMainBinding;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static String userID;
    public static String[] listedCustomers;
    private FirebaseAuth firebaseAuth;
    private static final int STORAGE_CODE=181;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.copycat.inventory.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = (BottomNavigationView) findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_entry, R.id.navigation_search, R.id.navigation_reports)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        Intent intent = getIntent();
        userID = intent.getStringExtra(Constants.USER);
        listedCustomers = intent.getStringArrayExtra(Constants.CUSTOMER_lABEL);


        if (!checkCameraPermission()) {
            askCameraPermission();
        }

       if (!checkStoragePermission())
       {
           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_CODE);
       }
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.signOut();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private boolean checkCameraPermission() {
        boolean result=false;
        result=ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;


        return result;
    }

    private void askCameraPermission() {
        int CAMERA_STORAGE_PER_CODE = 150;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_STORAGE_PER_CODE);


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = null;
                if (result != null) {
                    resultUri = result.getUri();
                }
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    recognizeText(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void recognizeText(Bitmap bitmapImage) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();
        if (!textRecognizer.isOperational()) {
            Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmapImage).build();
            SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int index = 0; index < textBlockSparseArray.size(); index++) {
                TextBlock textBlock = textBlockSparseArray.valueAt(index);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");

            }
            copyToClipboard(stringBuilder.toString());

        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager;
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data", text);
        clipboardManager.setPrimaryClip(clipData);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


           if(requestCode==STORAGE_CODE) {
               if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                   SharedPreferences sharedPreferences = getSharedPreferences(Constants.LOCALDB, MODE_PRIVATE);
                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putBoolean(Constants.FOLDER, true);
                   editor.apply();
               }

           }




    }

    private boolean checkStoragePermission()
    {
        boolean status=getSharedPreferences(Constants.LOCALDB,MODE_PRIVATE).getBoolean(Constants.FOLDER,false);

        return status;
    }


}