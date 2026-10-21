package com.example.calculator

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View

object IosGlassTheme {

    // מסיר אימוג'ים משמות הקטגוריות במצב עיצוב iOS של אפל
    fun formatCategoryTitle(rawTitle: String, isIosMode: Boolean): String {
        if (!isIosMode) return rawTitle
        return rawTitle.substringAfter(" ").trim()
    }

    private fun dp(context: Context, v: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, context.resources.displayMetrics).toInt()

    // כפתור זכוכית חלבית תלת-ממדית (Apple iOS 3D Frosted Glass)
    fun create3DGlassButtonDrawable(
        context: Context,
        baseColor: Int,
        primaryColor: Int,
        cornerDp: Float,
        isAccent: Boolean = false,
        isDanger: Boolean = false,
        isOperator: Boolean = false
    ): Drawable {
        val radius = dp(context, cornerDp).toFloat()

        // 1. שכבת צל תחתונה ליצירת עומק תלת-ממדי (3D Depth Rim)
        val shadowLayer = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.argb(135, 0, 0, 0))
        }

        // 2. גוף הזכוכית החלבית השקופה למחצה (Frosted Translucent Body)
        val topTint = when {
            isDanger -> Color.argb(215, 255, 69, 58)
            isAccent -> Color.argb(225, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
            isOperator -> Color.argb(155, (Color.red(primaryColor) + 90).coerceAtMost(255), (Color.green(primaryColor) + 90).coerceAtMost(255), (Color.blue(primaryColor) + 90).coerceAtMost(255))
            else -> Color.argb(95, (Color.red(baseColor) + 95).coerceAtMost(255), (Color.green(baseColor) + 100).coerceAtMost(255), (Color.blue(baseColor) + 115).coerceAtMost(255))
        }
        val bottomTint = when {
            isDanger -> Color.argb(180, 190, 30, 25)
            isAccent -> Color.argb(185, (Color.red(primaryColor) * 0.72).toInt(), (Color.green(primaryColor) * 0.72).toInt(), (Color.blue(primaryColor) * 0.72).toInt())
            isOperator -> Color.argb(110, (Color.red(primaryColor) * 0.5).toInt(), (Color.green(primaryColor) * 0.5).toInt(), (Color.blue(primaryColor) * 0.5).toInt())
            else -> Color.argb(60, (Color.red(baseColor) + 40).coerceAtMost(255), (Color.green(baseColor) + 45).coerceAtMost(255), (Color.blue(baseColor) + 55).coerceAtMost(255))
        }
        val frostedBody = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(topTint, bottomTint)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setStroke(dp(context, 1.3f), Color.argb(135, 255, 255, 255))
        }

        // 3. הברקת זכוכית עליונה (Top 3D Specular Glass Highlight)
        val glossHighlight = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(115, 255, 255, 255), Color.argb(0, 255, 255, 255))
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(radius, radius, radius, radius, radius * 0.35f, radius * 0.35f, radius * 0.35f, radius * 0.35f)
        }

        val layers = LayerDrawable(arrayOf(shadowLayer, frostedBody, glossHighlight)).apply {
            setLayerInset(0, dp(context, 1f), dp(context, 3f), dp(context, 1f), 0)
            setLayerInset(1, 0, 0, 0, dp(context, 2.5f))
            setLayerInset(2, dp(context, 1.5f), dp(context, 1.5f), dp(context, 1.5f), dp(context, 18f))
        }
        return RippleDrawable(ColorStateList.valueOf(Color.parseColor("#55FFFFFF")), layers, null)
    }

    // כרטיס תצוגה ותיבות קלט בזכוכית חלבית תלת-ממדית
    fun create3DGlassCardDrawable(context: Context, bgColor: Int, primaryColor: Int, cornerDp: Float): Drawable {
        val radius = dp(context, cornerDp).toFloat()
        val glassSurface = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.argb(120, (Color.red(bgColor) + 85).coerceAtMost(255), (Color.green(bgColor) + 90).coerceAtMost(255), (Color.blue(bgColor) + 110).coerceAtMost(255)),
                Color.argb(75, (Color.red(bgColor) + 35).coerceAtMost(255), (Color.green(bgColor) + 40).coerceAtMost(255), (Color.blue(bgColor) + 55).coerceAtMost(255))
            )
        ).apply {
            cornerRadius = radius
            setStroke(dp(context, 1.6f), Color.argb(145, 255, 255, 255))
        }
        val innerGlow = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(75, 255, 255, 255), Color.argb(15, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)))
        ).apply { cornerRadius = radius }
        return LayerDrawable(arrayOf(glassSurface, innerGlow))
    }

    fun apply3DElevation(view: View, context: Context, elevationDp: Float = 8f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = dp(context, elevationDp).toFloat()
        }
    }
}
