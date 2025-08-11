package com.example.techanalysisapp3.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String> sharedUrl = new MutableLiveData<>();
    private MutableLiveData<Boolean> showInstructions = new MutableLiveData<>(false);

    public void setSharedUrl(String url) {
        sharedUrl.setValue(url);
    }

    public LiveData<String> getSharedUrl() {
        return sharedUrl;
    }

    public void clearSharedUrl() {
        sharedUrl.setValue(null);
    }

    public LiveData<Boolean> getShowInstructions() {
        return showInstructions;
    }

    public void clearData() {
        sharedUrl.setValue(null);
        showInstructions.setValue(false);
    }
}