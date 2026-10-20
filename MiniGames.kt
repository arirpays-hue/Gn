package com.example.calculator

import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.widget.Toast
import kotlin.math.sin
import kotlin.random.Random

object MiniGameEngine {
    var isActive = false
    private var currentGame = ""
    private val handler = Handler(Looper.getMainLooper())
    private var gameLoop: Runnable? = null

    // משתנים לאיקס עיגול חכם (1337)
    private val tttBoard = Array(9) { "" }

    // משתנים לפולשי החשבון (8080)
    private var invaderScore = 0
    private var invaderTrack = mutableListOf<String>()
    private val invaderSymbols = listOf("+", "−", "×", "÷", "sin", "cos", "tan", "√")

    // משתנים למירוץ מכוניות (5555) - 3 נתיבים כמו החללית
    private var carLane = 1 // 0 = נתיב עליון (8), 1 = אמצעי, 2 = תחתון (2)
    private var racerScore = 0
    private val racerTracks = Array(3) { MutableList(5) { "." } }

    // משתנים לנטרול פצצה (1010)
    private var bombTimeLeft = 15
    private var bombScore = 0
    private var expectedOp = "×"

    // משתנים למסיבת חיות מצפצפות (7777)
    var isAnimalParty = false
    private val savedKeyLabels = mutableMapOf<String, CharSequence>()
    private val animalEmojis = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
        "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔"
    )

    fun stopGame(activity: MainActivity, message: String = "יצאת מהמשחק 🎮") {
        isActive = false
        currentGame = ""
        isAnimalParty = false
        gameLoop?.let { handler.removeCallbacks(it) }
        restoreAnimalLabels(activity)
        activity.currentInput = ""
        activity.justCalculated = false
        activity.applyTheme() // משחזר את כפתורי המקלדת והתצוגה
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    // צליל צפצפת גומי סינתטי למסיבת החיות
    fun playSqueakSound() {
        Thread {
            try {
                val sampleRate = 22050
                val durationMs = 140
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)
                var phase = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.45) 1100.0 + (progress / 0.45) * 1600.0 else 2700.0 - ((progress - 0.45) / 0.55) * 1300.0
                    val envelope = sin(progress * Math.PI)
                    phase += 2.0 * Math.PI * freq / sampleRate
                    val sample = (sin(phase) * 0.8 + sin(phase * 2.0) * 0.2) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(buffer.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
                track.write(buffer, 0, buffer.size); track.play(); Thread.sleep(durationMs.toLong() + 25); track.release()
            } catch (_: Exception) {}
        }.start()
    }

    fun startAnimalParty(activity: MainActivity) {
        stopPreviousLoop()
        isActive = true
        currentGame = "ANIMALS"
        isAnimalParty = true
        savedKeyLabels.clear()
        val shuffled = animalEmojis.shuffled()
        var idx = 0
        for ((k, btn) in activity.keypadButtons) {
            savedKeyLabels[k] = btn.text
            if (k != "AC") { btn.text = shuffled[idx % shuffled.size]; idx++ }
        }
        activity.memoryIndicator?.text = "🐾 מסיבת חיות! לחץ לצפצוף | AC ליציאה"
        activity.expressionView?.text = "חוזר למצב רגיל בעוד 15 שניות..."
        activity.currentView?.text = "🐶 🐱 🐸 🦊 🐼"
        playSqueakSound()
        var left = 15
        gameLoop = object : Runnable {
            override fun run() {
                if (!isActive || currentGame != "ANIMALS") return
                left--
                if (left <= 0) { stopGame(activity, "🐾 החיות חזרו לגן החיות!"); return }
                activity.expressionView?.text = "חוזר למצב רגיל בעוד $left שניות..."
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(gameLoop!!, 1000)
    }

    private fun restoreAnimalLabels(activity: MainActivity) {
        for ((k, text) in savedKeyLabels) activity.keypadButtons[k]?.text = text
        savedKeyLabels.clear()
    }
        // --- משחק 1: איקס עיגול חכם על מקלדת המחשבון (קוד 1337) ---
    fun startTicTacToe(activity: MainActivity) {
        stopPreviousLoop()
        isActive = true
        currentGame = "TTT"
        for (i in 0..8) tttBoard[i] = (i + 1).toString()
        updateTicTacToeUI(activity, "תורך! לחץ על 1-9 להצבת × (AC ליציאה)")
    }

    private fun updateTicTacToeUI(activity: MainActivity, status: String) {
        activity.memoryIndicator?.text = "🎮 איקס עיגול (× נגד 0) | AC ליציאה"
        activity.expressionView?.text = status
        val row1 = "[ ${tttBoard[6]} | ${tttBoard[7]} | ${tttBoard[8]} ]"
        val row2 = "[ ${tttBoard[3]} | ${tttBoard[4]} | ${tttBoard[5]} ]"
        val row3 = "[ ${tttBoard[0]} | ${tttBoard[1]} | ${tttBoard[2]} ]"
        activity.currentView?.text = "$row1\n$row2\n$row3"

        for (i in 0..8) {
            val btn = activity.keypadButtons[(i + 1).toString()]
            val mark = tttBoard[i]
            btn?.text = mark
            when (mark) {
                "×" -> btn?.setTextColor(Color.GREEN)
                "0" -> btn?.setTextColor(Color.RED)
                else -> btn?.setTextColor(Color.WHITE)
            }
        }
    }

    private fun checkWinner(): String? {
        val wins = arrayOf(
            intArrayOf(0,1,2), intArrayOf(3,4,5), intArrayOf(6,7,8),
            intArrayOf(0,3,6), intArrayOf(1,4,7), intArrayOf(2,5,8),
            intArrayOf(0,4,8), intArrayOf(2,4,6)
        )
        for (w in wins) {
            if (tttBoard[w[0]] == tttBoard[w[1]] && tttBoard[w[1]] == tttBoard[w[2]]) {
                return tttBoard[w[0]]
            }
        }
        if (tttBoard.none { it in "1".."9" }) return "TIE"
        return null
    }

    private fun getSmartComputerMove(): Int {
        val free = (0..8).filter { tttBoard[it] !in listOf("×", "0") }
        if (free.isEmpty()) return -1

        for (i in free) {
            val orig = tttBoard[i]; tttBoard[i] = "0"
            if (checkWinner() == "0") { tttBoard[i] = orig; return i }
            tttBoard[i] = orig
        }
        for (i in free) {
            val orig = tttBoard[i]; tttBoard[i] = "×"
            if (checkWinner() == "×") { tttBoard[i] = orig; return i }
            tttBoard[i] = orig
        }
        if (Random.nextFloat() < 0.82f) {
            var bestScore = Int.MIN_VALUE
            val bestMoves = mutableListOf<Int>()
            for (i in free) {
                val orig = tttBoard[i]; tttBoard[i] = "0"
                val score = minimax(0, false)
                tttBoard[i] = orig
                if (score > bestScore) { bestScore = score; bestMoves.clear(); bestMoves.add(i) }
                else if (score == bestScore) bestMoves.add(i)
            }
            if (bestMoves.isNotEmpty()) return bestMoves.random()
        }
        return free.random()
    }

    private fun minimax(depth: Int, isMaximizing: Boolean): Int {
        when (checkWinner()) {
            "0" -> return 10 - depth
            "×" -> return depth - 10
            "TIE" -> return 0
        }
        val free = (0..8).filter { tttBoard[it] !in listOf("×", "0") }
        if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in free) {
                val orig = tttBoard[i]; tttBoard[i] = "0"
                best = maxOf(best, minimax(depth + 1, false))
                tttBoard[i] = orig
            }
            return best
        } else {
            var best = Int.MAX_VALUE
            for (i in free) {
                val orig = tttBoard[i]; tttBoard[i] = "×"
                best = minOf(best, minimax(depth + 1, true))
                tttBoard[i] = orig
            }
            return best
        }
    }
        // --- משחק 2: פולשי החשבון במחשבון המדעי בלבד (קוד 8080) ---
    fun startCalcInvaders(activity: MainActivity) {
        if (activity.currentTabIndex != 1) {
            Toast.makeText(activity, "🔬 משחק זה זמין במחשבון המדעי בלבד!", Toast.LENGTH_LONG).show()
            activity.currentInput = ""
            activity.currentView?.text = "0"
            return
        }
        stopPreviousLoop()
        isActive = true
        currentGame = "INVADERS"
        invaderScore = 0
        invaderTrack = MutableList(6) { "." }
        activity.memoryIndicator?.text = "👾 פולשי החשבון | לחץ על הסימן כדי ליירט! | AC ליציאה"

        gameLoop = object : Runnable {
            override fun run() {
                if (!isActive || currentGame != "INVADERS") return
                val reachedShip = invaderTrack.removeAt(0)
                if (reachedShip != ".") {
                    activity.shakeScreen()
                    stopGame(activity, "💥 החללית נפגעה! ניקוד סופי: $invaderScore")
                    return
                }
                val nextSpawn = if (Random.nextFloat() < 0.65f) invaderSymbols.random() else "."
                invaderTrack.add(nextSpawn)
                renderInvaders(activity)
                val speed = (950L - (invaderScore * 25L)).coerceAtLeast(350L)
                handler.postDelayed(this, speed)
            }
        }
        renderInvaders(activity)
        handler.postDelayed(gameLoop!!, 900)
    }

    private fun renderInvaders(activity: MainActivity) {
        activity.expressionView?.text = "ניקוד: $invaderScore ⭐ | יירט את הסימן הקרוב ל-🚀"
        activity.currentView?.text = "🚀 ⬅ " + invaderTrack.joinToString("  ")
    }

    // --- משחק 3: מירוץ מכוניות לאורך המסך - המכונית בצד ימין ופונה שמאלה (קוד 5555) ---
    fun startMathRacer(activity: MainActivity) {
        stopPreviousLoop()
        isActive = true
        currentGame = "RACER"
        carLane = 1
        racerScore = 0
        for (lane in 0..2) {
            racerTracks[lane] = MutableList(5) { "." }
        }
        savedKeyLabels.clear()
        for (k in listOf("8", "2", "5", "6")) {
            activity.keypadButtons[k]?.let { savedKeyLabels[k] = it.text }
        }
        activity.keypadButtons["8"]?.text = "8⬆️"
        activity.keypadButtons["2"]?.text = "2⬇️"
        activity.keypadButtons["5"]?.text = "5💥"
        activity.keypadButtons["6"]?.text = "6⚡"

        activity.memoryIndicator?.text = "🏎️ מירוץ: 8=למעלה, 2=למטה, 5/×=פוצץ מכשול, 6/+=טורבו"

        gameLoop = object : Runnable {
            override fun run() {
                if (!isActive || currentGame != "RACER") return
                advanceRacerTracks(activity)
                if (!isActive) return
                renderRacer(activity)
                val speed = (750L - (racerScore * 12L)).coerceAtLeast(280L)
                handler.postDelayed(this, speed)
            }
        }
        renderRacer(activity)
        handler.postDelayed(gameLoop!!, 750)
    }

    private fun advanceRacerTracks(activity: MainActivity) {
        // המכונית בצד ימין (סוף הרשימה), והמכשולים נכנסים משמאל (אינדקס 0) ומתקדמים ימינה
        for (lane in 0..2) {
            val reachedCar = racerTracks[lane].removeAt(racerTracks[lane].lastIndex)
            if (reachedCar != ".") {
                if (lane == carLane) {
                    if (reachedCar == "🚧") {
                        activity.shakeScreen()
                        stopGame(activity, "💥 התנגשת במחסום! ניקוד: $racerScore")
                        return
                    } else if (reachedCar == "⭐") {
                        racerScore += 5
                    }
                } else {
                    racerScore++
                }
            }
        }
        val recentlySpawned = (0..2).any { racerTracks[it].firstOrNull() != "." }
        val spawnLane = if (!recentlySpawned && Random.nextFloat() < 0.7f) Random.nextInt(3) else -1
        for (lane in 0..2) {
            if (lane == spawnLane) {
                racerTracks[lane].add(0, if (Random.nextFloat() < 0.72f) "🚧" else "⭐")
            } else {
                racerTracks[lane].add(0, ".")
            }
        }
    }

    private fun renderRacer(activity: MainActivity) {
        activity.expressionView?.text = "🏆 ניקוד: $racerScore | 8=⬆️ 2=⬇️ 5/×=💥 6/+=⚡"
        val rows = (0..2).map { lane ->
            val carIcon = if (lane == carLane) " ➡ 🏎️" else " ➡ ▫️"
            racerTracks[lane].joinToString("  ") + carIcon
        }
        activity.currentView?.text = rows.joinToString("\n")
    }
        // --- משחק 4: נטרול פצצה מתמטית (קוד 1010) ---
    fun startBombDefusal(activity: MainActivity) {
        stopPreviousLoop()
        isActive = true
        currentGame = "BOMB"
        bombTimeLeft = 15
        bombScore = 0
        activity.memoryIndicator?.text = "💣 נטרל את הפצצה! לחץ על +, −, × או ÷ | AC ליציאה"
        generateBombQuestion(activity)

        gameLoop = object : Runnable {
            override fun run() {
                if (!isActive || currentGame != "BOMB") return
                bombTimeLeft--
                if (bombTimeLeft <= 0) {
                    activity.shakeScreen()
                    stopGame(activity, "💥 בום! הזמן נגמר! פצצות שנוטרלו: $bombScore")
                    return
                }
                activity.expressionView?.text = "⏳ זמן: ${bombTimeLeft}ש' | נוטרלו: $bombScore 💣"
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(gameLoop!!, 1000)
    }

    private fun generateBombQuestion(activity: MainActivity) {
        val a = Random.nextInt(2, 12)
        val b = Random.nextInt(2, 10)
        val ops = listOf("+", "−", "×", "÷")
        expectedOp = ops.random()
        val displayEq = when (expectedOp) {
            "+" -> "$a [?] $b = ${a + b}"
            "−" -> "${a + b} [?] $b = $a"
            "×" -> "$a [?] $b = ${a * b}"
            else -> "${a * b} [?] $b = $a"
        }
        activity.expressionView?.text = "⏳ זמן: ${bombTimeLeft}ש' | נוטרלו: $bombScore 💣"
        activity.currentView?.text = displayEq
    }

    // --- קליטת לחיצות מהמקלדת בזמן משחק ---
    fun onKeyPress(activity: MainActivity, key: String): Boolean {
        if (!isActive) return false
        if (key == "AC") {
            stopGame(activity)
            return true
        }

        when (currentGame) {
            "ANIMALS" -> {
                playSqueakSound()
                activity.currentView?.text = List(5) { animalEmojis.random() }.joinToString(" ")
                return true
            }
            "TTT" -> {
                val idx = key.toIntOrNull()?.minus(1) ?: return true
                if (idx in 0..8 && tttBoard[idx] !in listOf("×", "0")) {
                    tttBoard[idx] = "×"
                    var w = checkWinner()
                    if (w != null) { finishTicTacToe(activity, w); return true }
                    val comp = getSmartComputerMove()
                    if (comp != -1) tttBoard[comp] = "0"
                    w = checkWinner()
                    if (w != null) finishTicTacToe(activity, w) else updateTicTacToeUI(activity, "תורך! בחר משבצת פנויה (1-9)")
                }
                return true
            }
            "INVADERS" -> {
                val targetIndex = invaderTrack.indexOfFirst { it != "." }
                if (targetIndex != -1 && invaderTrack[targetIndex] == key) {
                    invaderTrack[targetIndex] = "."
                    invaderScore += 10
                    if (activity.useHaptic) activity.mainContent?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    renderInvaders(activity)
                } else {
                    invaderScore = (invaderScore - 2).coerceAtLeast(0)
                    renderInvaders(activity)
                }
                return true
            }
            "RACER" -> {
                when (key) {
                    "8", "8⬆️" -> carLane = (carLane - 1).coerceAtLeast(0)
                    "2", "2⬇️" -> carLane = (carLane + 1).coerceAtMost(2)
                    "5", "5💥", "×" -> {
                        // פוגע במחסום הקרוב ביותר לחזית המכונית (הימני ביותר בנתיב)
                        val idx = racerTracks[carLane].indexOfLast { it == "🚧" }
                        if (idx != -1) {
                            racerTracks[carLane][idx] = "⭐"
                            racerScore += 2
                        }
                    }
                    "6", "6⚡", "+" -> {
                        racerScore++
                        advanceRacerTracks(activity)
                    }
                }
                if (isActive) renderRacer(activity)
                return true
            }
            "BOMB" -> {
                if (key in listOf("+", "−", "×", "÷")) {
                    if (key == expectedOp) {
                        bombScore++
                        bombTimeLeft = (bombTimeLeft + 3).coerceAtMost(20)
                        generateBombQuestion(activity)
                    } else {
                        bombTimeLeft = (bombTimeLeft - 3).coerceAtLeast(1)
                        activity.shakeScreen()
                    }
                }
                return true
            }
        }
        return true
    }

    private fun finishTicTacToe(activity: MainActivity, winner: String) {
        val msg = when (winner) {
            "×" -> "🏆 ניצחת! × ניצח את 0! (AC ליציאה)"
            "0" -> "🤖 המחשב (0) ניצח! נסה שוב"
            else -> "🤝 תיקו! משחק צמוד"
        }
        updateTicTacToeUI(activity, msg)
        handler.postDelayed({ if (isActive && currentGame == "TTT") startTicTacToe(activity) }, 2200)
    }

    private fun stopPreviousLoop() {
        gameLoop?.let { handler.removeCallbacks(it) }
    }
}
