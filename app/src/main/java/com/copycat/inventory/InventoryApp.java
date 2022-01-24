package com.copycat.inventory;

import android.app.Application;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class InventoryApp extends Application {

    private FirebaseApp firebaseApp;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseApp=FirebaseApp.initializeApp(this);
        try {
            if (firebaseApp!=null)
            firebaseApp.setAutomaticResourceManagementEnabled(true);
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        Firebase.setAndroidContext(this);
        FirebaseDatabase.getInstance(firebaseApp).setPersistenceEnabled(true);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Firebase.goOffline();
    }


}
