package com.copycat.inventory.ui.entry;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EntryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EntryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is entry fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}