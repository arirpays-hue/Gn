package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView

object AuroraBorealisTheme {

    val EMERALD_NIGHT = Color.parseColor("#0A1C1A")
    val ARCTIC_BLUE = Color.parseColor("#07111E")
    val COSMIC_PURPLE = Color.parseColor("#1B0F2E")
    val AURORA_PRIMARY = Color.parseColor("#00F5D4") // Luminous Mint-Cyan
    val AURORA_SECONDARY = Color.parseColor("#10B981") // Ethereal Green
    val EXPRESSION_CYAN_GREY = Color.parseColor("#A0B2C6")

    var auroraOpacity: Int = 195
    var auroraBlurRadius: Float = 40f
    var auroraGlowIntensity: Int = 230
    var auroraCornerPt: Float = 24f

    fun loadAuroraPrefs(prefs: SharedPreferences?) {
        if (prefs == null) return
        auroraOpacity = prefs.getInt("aurora_opacity", 195)
        auroraBlurRadius = prefs.getFloat("aurora_blur", 40f)
        auroraGlowIntensity = prefs.getInt("aurora_glow", 230)
        auroraCornerPt = prefs.getFloat("aurora_corner", 24f)
    }

    fun saveAuroraPrefs(prefs: SharedPreferences?) {
        prefs?.edit()
            ?.putInt("aurora_opacity", auroraOpacity)
            ?.putFloat("aurora_blur", auroraBlurRadius)
            ?.putInt("aurora_glow", auroraGlowIntensity)
            ?.putFloat("aurora_corner", auroraCornerPt)
            ?.apply()
    }

    fun applyHeaderMetallicGradient(tv: TextView?, isAurora: Boolean, isLiquidGlass: Boolean) {
        if (tv == null) return
        tv.post {
            if (tv.height > 0) {
                val bottomCol = when {
                    isAurora -> Color.parseColor("#70C1B3")
                    isLiquidGlass -> Color.parseColor("#A0AAB8")
                    else -> Color.WHITE
                }
                if (isAurora || isLiquidGlass) {
                    tv.paint.shader = LinearGradient(
                        0f, 0f, 0f, tv.height.toFloat(),
                        Color.WHITE, bottomCol, Shader.TileMode.CLAMP
                    )
                    if (isAurora) {
                        tv.setShadowLayer(14f, 0f, 0f, Color.argb(120, 0, 245, 212))
                    } else {
                        tv.setShadowLayer(8f, 0f, 2f, Color.argb(90, 0, 0, 0))
                    }
                    tv.invalidate()
                } else {
                    tv.paint.shader = null
                    tv.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                    tv.invalidate()
                }
            }
        }
    }

    fun createAuroraAtmosphericBackground(): Drawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(EMERALD_NIGHT, ARCTIC_BLUE, COSMIC_PURPLE)
        )
    }

    fun styleAuroraSymbol(tv: TextView, symbol: String, isPressed: Boolean = false) {
        val isOperator = symbol in listOf("÷", "×", "−", "+", "=")
        val isCommand = symbol in listOf("AC", "⌫", "%", "MC", "MR", "M+", "M-")

        val textColor = when {
            isPressed -> Color.WHITE
            isOperator -> EMERALD_NIGHT
            isCommand -> AURORA_PRIMARY
            else -> Color.WHITE
        }
        tv.setTextColor(textColor)

        val displaySym = if (symbol == "-") "−" else symbol
        if (displaySym in listOf("sin", "cos", "tan", "log", "ln", "x²", "x³", "x^y", "e")) {
            tv.text = SpannableString(displaySym).apply {
                setSpan(StyleSpan(Typeface.ITALIC), 0, displaySym.length, 0)
            }
        } else {
            tv.text = displaySym
        }

        if (isPressed) {
            tv.setShadowLayer(20f, 0f, 0f, Color.argb(235, 0, 245, 212))
        } else {
            if (isOperator) {
                tv.setShadowLayer(4f, 0f, 1f, Color.argb(120, 255, 255, 255))
            } else {
                tv.setShadowLayer(8f, 0f, -1.2f, Color.argb(150, 0, 245, 212))
            }
        }
    }

    fun createAuroraButtonDrawable(
        activity: MainActivity,
        symbol: String,
        isMem: Boolean,
        isPressed: Boolean = false
    ): Drawable {
        val radius = activity.dp(auroraCornerPt).toFloat()
        val isOperator = symbol in listOf("÷", "×", "−", "+", "=")
        val isCommand = symbol in listOf("AC", "⌫", "%") || isMem
        val boost = if (isPressed) 50 else 0

        val (topColor, bottomColor) = when {
            isOperator -> {
                Color.argb((205 + boost).coerceAtMost(255), 0, 245, 212) to
                        Color.argb((160 + boost).coerceAtMost(255), 16, 185, 129)
            }
            isCommand -> {
                Color.argb((85 + boost).coerceAtMost(255), 200, 220, 230) to
                        Color.argb((55 + boost).coerceAtMost(255), 150, 180, 195)
            }
            else -> {
                Color.argb((165 + boost).coerceAtMost(255), 24, 42, 54) to
                        Color.argb((135 + boost).coerceAtMost(255), 15, 28, 36)
            }
        }

        val ambientShadow = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.argb(if (isPressed) 50 else 125, 0, 0, 0))
        }

        val glassBody = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(topColor, bottomColor)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            // ללא מסגרת לבנה חיצונית במצב זוהר צפוני
        }

        val radialPulse = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = radius * 1.5f
            val alpha = if (isPressed) 165 else 25
            colors = intArrayOf(Color.argb(alpha, 0, 245, 212), Color.TRANSPARENT)
        }

        val layers = LayerDrawable(arrayOf(ambientShadow, glassBody, radialPulse)).apply {
            setLayerInset(1, 0, 0, 0, if (isPressed) activity.dp(1f) else activity.dp(2.5f))
            setLayerInset(2, activity.dp(1f), activity.dp(1f), activity.dp(1f), activity.dp(19f))
        }

        return RippleDrawable(ColorStateList.valueOf(Color.parseColor("#6600F5D4")), layers, null)
    }

    // ללא מסגרת לבנה חיצונית בכרטיסים, דיאלוגים ובתפריט הצד
    fun createAuroraCardDrawable(activity: MainActivity, cornerDp: Float = 28f, isDrawer: Boolean = false): Drawable {
        val radius = if (isDrawer) 0f else activity.dp(cornerDp).toFloat()
        val glass = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.argb(if (isDrawer) 215 else 190, 15, 28, 36),
                Color.argb(if (isDrawer) 205 else 175, 10, 20, 26),
                Color.argb(if (isDrawer) 195 else 160, 18, 32, 40)
            )
        ).apply {
            cornerRadius = radius
        }
        return glass
    }

    fun createActiveDrawerCapsule(activity: MainActivity, isAurora: Boolean): Drawable {
        val radius = activity.dp(if (isAurora) 14f else 12f).toFloat()
        val glowCol = if (isAurora) AURORA_PRIMARY else activity.primaryColor
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.argb(75, Color.red(glowCol), Color.green(glowCol), Color.blue(glowCol)),
                Color.argb(35, Color.red(glowCol), Color.green(glowCol), Color.blue(glowCol))
            )
        ).apply {
            cornerRadius = radius
            setStroke(activity.dp(1f), Color.argb(155, Color.red(glowCol), Color.green(glowCol), Color.blue(glowCol)))
        }
    }

    // אייקונים וקטוריים מיוחדים ל-Aurora Borealis
    fun createAuroraVectorIcon(
        context: Context,
        index: Int,
        tintColor: Int = AURORA_PRIMARY,
        sizeDp: Float = 22f
    ): Drawable {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt().coerceAtLeast(24)
        return object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tintColor
                style = Paint.Style.STROKE
                strokeWidth = 1.8f * context.resources.displayMetrics.density
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                setShadowLayer(4f * context.resources.displayMetrics.density, 0f, 0f, Color.argb(160, Color.red(tintColor), Color.green(tintColor), Color.blue(tintColor)))
            }
            private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(45, Color.red(tintColor), Color.green(tintColor), Color.blue(tintColor))
                style = Paint.Style.FILL
            }

            override fun getIntrinsicWidth(): Int = sizePx
            override fun getIntrinsicHeight(): Int = sizePx

            override fun draw(canvas: Canvas) {
                val b = bounds
                val cx = b.exactCenterX()
                val cy = b.exactCenterY()
                val r = Math.min(b.width(), b.height()) * 0.38f
                canvas.drawRoundRect(cx - r * 1.15f, cy - r * 1.15f, cx + r * 1.15f, cy + r * 1.15f, r * 0.4f, r * 0.4f, fillPaint)

                when (index) {
                    0 -> { // מחשבון רגיל - גריד נקודות פריזמה
                        canvas.drawCircle(cx - r * 0.4f, cy - r * 0.4f, r * 0.2f, paint)
                        canvas.drawCircle(cx + r * 0.4f, cy - r * 0.4f, r * 0.2f, paint)
                        canvas.drawCircle(cx - r * 0.4f, cy + r * 0.4f, r * 0.2f, paint)
                        canvas.drawCircle(cx + r * 0.4f, cy + r * 0.4f, r * 0.2f, paint)
                    }
                    1 -> { // מחשבון מדעי - גל אופקי מואר
                        val p = Path().apply {
                            moveTo(cx - r * 0.8f, cy)
                            cubicTo(cx - r * 0.3f, cy - r * 0.9f, cx + r * 0.3f, cy + r * 0.9f, cx + r * 0.8f, cy)
                        }
                        canvas.drawPath(p, paint)
                    }
                    2 -> { // מע"מ - תג מחיר ארקטי
                        canvas.drawRoundRect(cx - r * 0.65f, cy - r * 0.75f, cx + r * 0.65f, cy + r * 0.75f, r * 0.2f, r * 0.2f, paint)
                        canvas.drawCircle(cx, cy - r * 0.25f, r * 0.2f, paint)
                        canvas.drawLine(cx - r * 0.35f, cy + r * 0.3f, cx + r * 0.35f, cy + r * 0.3f, paint)
                    }
                    3 -> { // הלוואה - בניין בנק קריסטלי
                        canvas.drawRect(cx - r * 0.7f, cy - r * 0.2f, cx + r * 0.7f, cy + r * 0.7f, paint)
                        val p = Path().apply { moveTo(cx - r * 0.85f, cy - r * 0.2f); lineTo(cx, cy - r * 0.8f); lineTo(cx + r * 0.85f, cy - r * 0.2f); close() }
                        canvas.drawPath(p, paint)
                    }
                    4 -> { // המרת מרחקים - מצפן זוהר
                        canvas.drawCircle(cx, cy, r * 0.75f, paint)
                        canvas.drawLine(cx, cy - r * 0.5f, cx, cy + r * 0.5f, paint)
                        canvas.drawLine(cx - r * 0.5f, cy, cx + r * 0.5f, cy, paint)
                    }
                    5 -> { // המרת משקלים - מאזניים ארקטיים
                        canvas.drawLine(cx, cy - r * 0.7f, cx, cy + r * 0.7f, paint)
                        canvas.drawLine(cx - r * 0.75f, cy - r * 0.3f, cx + r * 0.75f, cy - r * 0.3f, paint)
                        canvas.drawArc(cx - r * 0.7f, cy, cx - r * 0.2f, cy + r * 0.5f, 0f, 180f, false, paint)
                        canvas.drawArc(cx + r * 0.2f, cy, cx + r * 0.7f, cy + r * 0.5f, 0f, 180f, false, paint)
                    }
                    6 -> { // המרת טמפ' - מדחום קריסטל
                        canvas.drawArc(cx - r * 0.35f, cy + r * 0.1f, cx + r * 0.35f, cy + r * 0.8f, -45f, 270f, false, paint)
                        canvas.drawLine(cx - r * 0.18f, cy - r * 0.75f, cx - r * 0.18f, cy + r * 0.2f, paint)
                        canvas.drawLine(cx + r * 0.18f, cy - r * 0.75f, cx + r * 0.18f, cy + r * 0.2f, paint)
                    }
                    7 -> { // מחשבון מתכנתים - סוגריים בינאריים
                        canvas.drawLine(cx - r * 0.4f, cy - r * 0.6f, cx - r * 0.8f, cy, paint)
                        canvas.drawLine(cx - r * 0.8f, cy, cx - r * 0.4f, cy + r * 0.6f, paint)
                        canvas.drawLine(cx + r * 0.4f, cy - r * 0.6f, cx + r * 0.8f, cy, paint)
                        canvas.drawLine(cx + r * 0.8f, cy, cx + r * 0.4f, cy + r * 0.6f, paint)
                    }
                    8 -> { // חישוב אחוזים - סמל אחוז זוהר
                        canvas.drawLine(cx - r * 0.6f, cy + r * 0.6f, cx + r * 0.6f, cy - r * 0.6f, paint)
                        canvas.drawCircle(cx - r * 0.4f, cy - r * 0.4f, r * 0.2f, paint)
                        canvas.drawCircle(cx + r * 0.4f, cy + r * 0.4f, r * 0.2f, paint)
                    }
                    9 -> { // חישוב הנחות - תג הנחה קריסטלי
                        val p = Path().apply { moveTo(cx - r * 0.7f, cy - r * 0.7f); lineTo(cx, cy - r * 0.7f); lineTo(cx + r * 0.7f, cy); lineTo(cx, cy + r * 0.7f); lineTo(cx - r * 0.7f, cy); close() }
                        canvas.drawPath(p, paint)
                        canvas.drawCircle(cx - r * 0.35f, cy - r * 0.35f, r * 0.12f, paint)
                    }
                    10 -> { // חלוקת טיפים - כוסות זוהרות
                        canvas.drawCircle(cx - r * 0.25f, cy, r * 0.5f, paint)
                        canvas.drawCircle(cx + r * 0.3f, cy + r * 0.15f, r * 0.4f, paint)
                    }
                    11 -> { // מדד BMI - דופק לב ארקטי
                        val p = Path().apply { moveTo(cx - r * 0.8f, cy); lineTo(cx - r * 0.4f, cy); lineTo(cx - r * 0.15f, cy - r * 0.6f); lineTo(cx + r * 0.15f, cy + r * 0.6f); lineTo(cx + r * 0.4f, cy); lineTo(cx + r * 0.8f, cy) }
                        canvas.drawPath(p, paint)
                    }
                    12 -> { // חישוב גיל - לוח שנה קריסטלי
                        canvas.drawRoundRect(cx - r * 0.75f, cy - r * 0.65f, cx + r * 0.75f, cy + r * 0.75f, r * 0.2f, r * 0.2f, paint)
                        canvas.drawLine(cx - r * 0.75f, cy - r * 0.2f, cx + r * 0.75f, cy - r * 0.2f, paint)
                    }
                    13 -> { // מטבעות לייב - מטבע זוהר
                        canvas.drawCircle(cx, cy, r * 0.7f, paint)
                        canvas.drawLine(cx, cy - r * 0.45f, cx, cy + r * 0.45f, paint)
                    }
                    14 -> { // מחשבון BMR - להבת אנרגיה
                        val p = Path().apply { moveTo(cx, cy - r * 0.8f); cubicTo(cx + r * 0.7f, cy, cx + r * 0.6f, cy + r * 0.8f, cx, cy + r * 0.8f); cubicTo(cx - r * 0.6f, cy + r * 0.8f, cx - r * 0.7f, cy, cx, cy - r * 0.8f) }
                        canvas.drawPath(p, paint)
                    }
                    15 -> { // משוואות ריבועיות - פרבולה זוהרת
                        canvas.drawLine(cx - r * 0.8f, cy + r * 0.5f, cx + r * 0.8f, cy + r * 0.5f, paint)
                        val p = Path().apply { moveTo(cx - r * 0.6f, cy - r * 0.6f); quadTo(cx, cy + r * 1.2f, cx + r * 0.6f, cy - r * 0.6f) }
                        canvas.drawPath(p, paint)
                    }
                    16 -> { // מחשבון שברים - קו שבר ארקטי
                        canvas.drawLine(cx - r * 0.7f, cy + r * 0.6f, cx + r * 0.7f, cy - r * 0.6f, paint)
                        canvas.drawRect(cx - r * 0.5f, cy - r * 0.6f, cx - r * 0.2f, cy - r * 0.2f, paint)
                        canvas.drawRect(cx + r * 0.2f, cy + r * 0.2f, cx + r * 0.5f, cy + r * 0.6f, paint)
                    }
                    17 -> { // ריבית דריבית - גרף צמיחה עולה
                        val p = Path().apply { moveTo(cx - r * 0.75f, cy + r * 0.6f); lineTo(cx - r * 0.2f, cy + r * 0.1f); lineTo(cx + r * 0.15f, cy + r * 0.3f); lineTo(cx + r * 0.75f, cy - r * 0.5f) }
                        canvas.drawPath(p, paint)
                    }
                    18 -> { // המרת מהירות - מד מהירות ארקטי
                        canvas.drawArc(cx - r * 0.8f, cy - r * 0.6f, cx + r * 0.8f, cy + r * 0.8f, 170f, 200f, false, paint)
                        canvas.drawLine(cx, cy + r * 0.2f, cx + r * 0.4f, cy - r * 0.3f, paint)
                    }
                    19 -> { // הגדרות - גלגלי שיניים זוהרים
                        canvas.drawCircle(cx, cy, r * 0.5f, paint)
                        canvas.drawLine(cx - r * 0.8f, cy, cx + r * 0.8f, cy, paint)
                        canvas.drawLine(cx, cy - r * 0.8f, cx, cy + r * 0.8f, paint)
                    }
                    100 -> { // PiP (חלון צף)
                        canvas.drawRoundRect(cx - r * 0.8f, cy - r * 0.6f, cx + r * 0.8f, cy + r * 0.6f, r * 0.2f, r * 0.2f, paint)
                    }
                    101 -> { // History (היסטוריה)
                        canvas.drawCircle(cx, cy, r * 0.7f, paint)
                        canvas.drawLine(cx, cy - r * 0.35f, cx, cy, paint)
                        canvas.drawLine(cx, cy, cx + r * 0.3f, cy, paint)
                    }
                    200 -> { // חיפוש
                        canvas.drawCircle(cx - r * 0.15f, cy - r * 0.15f, r * 0.45f, paint)
                        canvas.drawLine(cx + r * 0.2f, cy + r * 0.2f, cx + r * 0.7f, cy + r * 0.7f, paint)
                    }
                    201 -> { // צליל
                        canvas.drawRect(cx - r * 0.5f, cy - r * 0.3f, cx - r * 0.1f, cy + r * 0.3f, paint)
                        val p = Path().apply { moveTo(cx - r * 0.1f, cy - r * 0.3f); lineTo(cx + r * 0.5f, cy - r * 0.7f); lineTo(cx + r * 0.5f, cy + r * 0.7f); lineTo(cx - r * 0.1f, cy + r * 0.3f); close() }
                        canvas.drawPath(p, paint)
                    }
                    202 -> { // בהירות
                        canvas.drawCircle(cx, cy, r * 0.35f, paint)
                    }
                    203 -> { // שקיפות
                        canvas.drawCircle(cx, cy, r * 0.5f, paint)
                    }
                }
            }

            override fun setAlpha(alpha: Int) { paint.alpha = alpha }
            override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }.apply {
            setBounds(0, 0, sizePx, sizePx)
        }
    }
}
 
