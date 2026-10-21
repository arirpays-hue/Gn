package com.example.calculator

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.View

object IosGlassTheme {

    // מסיר אימוג'ים משמות המחשבונים במצב iOS ומשאיר טקסט נקי בלבד
    fun cleanCategoryTitle(rawTitle: String, isIosMode: Boolean): String {
        if (!isIosMode) return rawTitle
        return rawTitle.substringAfter(" ").trim()
    }

    // יצירת זכוכית חלבית תלת-ממדית (3D Frosted Glass) לכפתורי המחשבון
    fun createGlassButtonDrawable(
        activity: MainActivity,
        symbol: String,
        isMem: Boolean
    ): Drawable {
        val radius = activity.dp(28f).toFloat()
        val isAccent = symbol == "="
        val isDanger = symbol == "AC"
        val isOp = symbol in listOf("⌫", "%", "÷", "×", "−", "+")

        val baseBottomColor = when {
            isDanger -> Color.argb(190, 255, 59, 48)
            isAccent -> Color.argb(215, Color.red(activity.primaryColor), Color.green(activity.primaryColor), Color.blue(activity.primaryColor))
            isOp -> Color.argb(120, Color.red(activity.primaryColor), Color.green(activity.primaryColor), Color.blue(activity.primaryColor))
            isMem -> Color.argb(55, 255, 255, 255)
            else -> Color.argb(75, 255, 255, 255)
        }

        val baseTopColor = when {
            isDanger -> Color.argb(230, 255, 110, 95)
            isAccent -> Color.argb(245,
                (Color.red(activity.primaryColor) + 45).coerceAtMost(255),
                (Color.green(activity.primaryColor) + 45).coerceAtMost(255),
                (Color.blue(activity.primaryColor) + 45).coerceAtMost(255)
            )
            isOp -> Color.argb(165,
                (Color.red(activity.primaryColor) + 60).coerceAtMost(255),
                (Color.green(activity.primaryColor) + 60).coerceAtMost(255),
                (Color.blue(activity.primaryColor) + 60).coerceAtMost(255)
            )
            else -> Color.argb(130, 255, 255, 255)
        }

        // שכבת עומק תחתונה (צל תלת-ממדי פנימי)
        val shadowLayer = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.argb(95, 0, 0, 0))
        }

        // גוף הזכוכית החלבית עם גרדיאנט ומסגרת קריסטלית
        val frostedBody = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(baseTopColor, baseBottomColor)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setStroke(activity.dp(1.3f), Color.argb(155, 255, 255, 255))
        }

        // הברקת זכוכית עליונה (3D Specular Highlight)
        val topGloss = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(120, 255, 255, 255), Color.argb(0, 255, 255, 255))
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(radius, radius, radius, radius, radius * 0.4f, radius * 0.4f, radius * 0.4f, radius * 0.4f)
        }

        val layers = LayerDrawable(arrayOf(shadowLayer, frostedBody, topGloss)).apply {
            setLayerInset(1, 0, 0, 0, activity.dp(2.5f))
            setLayerInset(2, activity.dp(2f), activity.dp(1.5f), activity.dp(2f), activity.dp(22f))
        }

        return RippleDrawable(ColorStateList.valueOf(Color.parseColor("#55FFFFFF")), layers, null)
    }

    // זכוכית חלבית תלת-ממדית לתצוגת המחשבון ולכרטיסים
    fun createGlassCardDrawable(activity: MainActivity, cornerDp: Float = 30f): Drawable {
        val radius = activity.dp(cornerDp).toFloat()
        val glass = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.argb(95, 255, 255, 255),
                Color.argb(45, 200, 220, 255),
                Color.argb(75, Color.red(activity.primaryColor), Color.green(activity.primaryColor), Color.blue(activity.primaryColor))
            )
        ).apply {
            cornerRadius = radius
            setStroke(activity.dp(1.5f), Color.argb(145, 255, 255, 255))
        }
        return glass
    }

    fun apply3DElevation(view: View, dpElevation: Float = 8f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = dpElevation * view.resources.displayMetrics.density
        }
    }
}
