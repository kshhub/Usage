package com.example.usage.UserPatternAnalysis

import android.graphics.drawable.Drawable
import java.io.Serializable

data class UserData (var apptime:String, var apppackname:String, var appicon: Drawable):Serializable{
}