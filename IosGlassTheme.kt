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

    // מסיר אימוג'ים משמות המחשבונים כשהעיצוב של אפל (iOS) פעיל
    fun formatCategoryTitle(rawTitle: String, isIosMode: Boolean): String {
        if (!isIosMode) return rawTitle
        val stripped = rawTitle.substringAfter(" ").trim()
        return if (stripped.isNotEmpty()) stripped else rawTitle
    }

    // זכוכית חלבית תלת-ממדית (3D Frosted Glass) לכרטיס התצוגה ולתוצאות
    fun createFrostedCardDrawable(activity: MainActivity, bgColor: Int, primaryColor: Int, radiusDp: Float = 28f): Drawable {
        val r = activity.dp(radiusDp).toFloat()

        // שכבה 1: צל תחתון עדין ליצירת ריחוף תלת-ממדי
        val shadowLayer = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(30, 0, 0, 0),
                Color.argb(120, 0, 0, 0)
            )
        ).apply { cornerRadius = r }

        // שכבה 2: גוף הזכוכית החלבית (Frosted Translucent Body)
        val glassBase = Utils.lightenColor(bgColor, 0.16f)
        val glassTop = Color.argb(
            215,
            (Color.red(glassBase) + 38).coerceAtMost(255),
            (Color.green(glassBase) + 40).coerceAtMost(255),
            (Color.blue(glassBase) + 48).coerceAtMost(255)
        )
        val glassBottom = Color.argb(
            180,
            Color.red(glassBase),
            Color.green(glassBase),
            Color.blue(glassBase)
        )
        val bodyLayer = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(glassTop, glassBottom)
        ).apply {
            cornerRadius = r
            setStroke(
                activity.dp(1.4f),
                Color.argb(135, 255, 255, 255)
            )
        }

        // שכבה 3: השתקפות אור עליונה (3D Specular Glass Sheen)
        val sheenLayer = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(95, 255, 255, 255),
                Color.argb(22, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)),
                Color.TRANSPARENT
            )
        ).apply { cornerRadius = r }

        return LayerDrawable(arrayOf(shadowLayer, bodyLayer, sheenLayer)).apply {
            setLayerInset(0, 0, activity.dp(3f), 0, 0)
            setLayerInset(1, 0, 0, 0, activity.dp(2f))
            setLayerInset(2, activity.dp(1.5f), activity.dp(1.5f), activity.dp(1.5f), activity.dp(12f))
        }
    }
        // כפתור זכוכית חלבית תלת-ממדית של iOS למקלדת המחשבון
    fun createFrostedKeyButtonBackground(
        activity: MainActivity,
        symbol: String,
        bgColor: Int,
        primaryColor: Int,
        cornerDp: Float
    ): Drawable {
        val r = activity.dp(cornerDp).toFloat()
        val isMem = symbol in listOf("MC", "MR", "M+", "M-")
        val isAccentOp = symbol in listOf("÷", "×", "−", "+", "=")
        val isTopFunc = symbol in listOf("AC", "⌫", "%")

        val baseColor = when {
            symbol == "=" -> primaryColor
            symbol == "AC" -> Color.parseColor("#FF453A") // iOS System Red
            isAccentOp -> Color.argb(215, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
            isTopFunc || isMem -> Utils.lightenColor(bgColor, 0.28f)
            else -> Utils.lightenColor(bgColor, 0.17f)
        }

        val shadow3D = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = r
            setColor(Color.argb(115, 0, 0, 0))
        }

        val topFrost = Color.argb(
            235,
            (Color.red(baseColor) + 45).coerceAtMost(255),
            (Color.green(baseColor) + 45).coerceAtMost(255),
            (Color.blue(baseColor) + 50).coerceAtMost(255)
        )
        val bottomFrost = Color.argb(
            200,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )

        val glassSurface = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(topFrost, bottomFrost)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = r
            setStroke(activity.dp(1.2f), Color.argb(130, 255, 255, 255))
        }

        val highlightGloss = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(110, 255, 255, 255), Color.TRANSPARENT)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = r
        }

        val layered = LayerDrawable(arrayOf(shadow3D, glassSurface, highlightGloss)).apply {
            setLayerInset(0, 0, activity.dp(3f), 0, 0)
            setLayerInset(1, 0, 0, 0, activity.dp(2.5f))
            setLayerInset(2, activity.dp(1.5f), activity.dp(1.5f), activity.dp(1.5f), activity.dp(14f))
        }

        return RippleDrawable(ColorStateList.valueOf(Color.parseColor("#55FFFFFF")), layered, null)
    }

    // כפתור פעולה / שדה קלט בזכוכית חלבית של iOS
    fun createFrostedActionDrawable(activity: MainActivity, bgColor: Int, primaryColor: Int, isPrimary: Boolean, cornerDp: Float): Drawable {
        val sym = if (isPrimary) "=" else "0"
        return createFrostedKeyButtonBackground(activity, sym, bgColor, primaryColor, cornerDp)
    }

    fun apply3DElevation(view: View, activity: MainActivity, isIosMode: Boolean, dpElev: Float = 8f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = if (isIosMode) activity.dp(dpElev).toFloat() else 0f
        }
    }
}
