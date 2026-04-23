package com.example.blueprintproapps

import android.app.Application
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.FontAwesomeModule
import com.joanzapata.iconify.fonts.MaterialModule

class BlueprintProApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Iconify with Material and FontAwesome fonts
        Iconify.with(MaterialModule())
               .with(FontAwesomeModule())
    }
}
