package com.group29.localtreasury.ui.settings

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    val profileImage = MutableLiveData<Bitmap>()
}