package com.dmesh.prototype.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dmesh.prototype.MeshController
import com.dmesh.prototype.ui.MeshViewModel

class MeshViewModelFactory(private val controller: MeshController) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeshViewModel::class.java)) {
            return MeshViewModel(controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
