package com.example.usage

import android.graphics.drawable.Drawable
import java.io.Serializable

data class MyData (var apptime:String, var apppackname:String, var appicon: Drawable):Serializable{
}