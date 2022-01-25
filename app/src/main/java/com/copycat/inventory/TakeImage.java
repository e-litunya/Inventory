package com.copycat.inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TakeImage extends ActivityResultContract<String, Uri> {


    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String input) {
        return new Intent(input);
    }

    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode!= Activity.RESULT_OK || intent==null)
        {
            return null;
        }
        return intent.getData();
    }
}
