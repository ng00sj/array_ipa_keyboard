package com.example.arraykeyboard

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.inputmethodservice.InputMethodService
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import com.example.arraykeyboard.DictManager.loadDict

class MyInputMethodService : InputMethodService() {
    private var viewKeyboard: View? = null

    private val composingBuffer = StringBuilder()
    private var composingCodes = StringBuilder()
    private var candidateWords = mutableListOf<String>()

    private var color = 0

    data class KeyConfig(
        val code: String,
        val buffer: String,
        val text: String,
        val isNumber: Boolean,
        val is8up: Boolean,
        val eng: String,
        val engShift: String,
        val mark: String
    )

    private val numMapColor = mapOf(
        0 to Pair(R.color.gra_0,R.color.gra_1),
        1 to Pair(R.color.red_0,R.color.red_1),
        2 to Pair(R.color.ora_0,R.color.ora_1),
        3 to Pair(R.color.yel_0,R.color.yel_1),
        4 to Pair(R.color.gre_0,R.color.gre_1),
        5 to Pair(R.color.sky_0,R.color.sky_1),
        6 to Pair(R.color.blu_0,R.color.blu_1),
        7 to Pair(R.color.blw_0,R.color.blw_1),
        8 to Pair(R.color.purple_200,R.color.purple_500),
        9 to Pair(R.color.pur_0,R.color.pur_1),
    )

    private val keyMapArrayNumber = mapOf(
        R.id.btn_01 to 0,
        R.id.btn_02 to 1,
        R.id.btn_03 to 2,
        R.id.btn_04 to 3,
        R.id.btn_05 to 4,
        R.id.btn_06 to 5,
        R.id.btn_07 to 6,
        R.id.btn_08 to 7,
        R.id.btn_09 to 8,
        R.id.btn_00 to 9,
    )
    private val keyMap = mapOf(
        R.id.btn_01 to KeyConfig("1", "1=", "1", true, false, "1", "!","1"),
        R.id.btn_02 to KeyConfig("2", "2=", "2", true, false, "2", "@","2"),
        R.id.btn_03 to KeyConfig("3", "3=", "3", true, false, "3", "#","3"),
        R.id.btn_04 to KeyConfig("4", "4=", "4", true, false, "4", "$","4"),
        R.id.btn_05 to KeyConfig("5", "5=", "5", true, false, "5", "%","5"),
        R.id.btn_06 to KeyConfig("6", "6=", "6", true, false, "6", "^","6"),
        R.id.btn_07 to KeyConfig("7", "7=", "7", true, false, "7", "&","7"),
        R.id.btn_08 to KeyConfig("8", "8=", "8", true, false, "8", "*","8"),
        R.id.btn_09 to KeyConfig("9", "9=", "9", true, false, "9", "(","9"),
        R.id.btn_00 to KeyConfig("0", "0=", "0", true, false, "0", ")","0"),
        R.id.btn_11 to KeyConfig("q", "1^", "1^", false, false, "q", "Q","`"),
        R.id.btn_12 to KeyConfig("w", "2^", "2^", false, false, "w", "W","~"),
        R.id.btn_13 to KeyConfig("e", "3^", "3^", false, false, "e", "E","-"),
        R.id.btn_14 to KeyConfig("r", "4^", "4^", false, false, "r", "R","_"),
        R.id.btn_15 to KeyConfig("t", "5^", "5^", false, false, "t", "T","+"),
        R.id.btn_16 to KeyConfig("y", "6^", "6^", false, false, "y", "Y","+"),
        R.id.btn_17 to KeyConfig("u", "7^", "7^", false, false, "u", "U","["),
        R.id.btn_18 to KeyConfig("i", "8^", "8^", false, true, "i", "I","]"),
        R.id.btn_19 to KeyConfig("o", "9^", "9^", false, false, "o", "O","{"),
        R.id.btn_10 to KeyConfig("p", "0^", "0^", false, false, "p", "P","}"),
        R.id.btn_21 to KeyConfig("a", "1-", "1-", false, false, "a", "A","\\"),
        R.id.btn_22 to KeyConfig("s", "2-", "2-", false, false, "s", "S","|"),
        R.id.btn_23 to KeyConfig("d", "3-", "3-", false, false, "d", "D","'"),
        R.id.btn_24 to KeyConfig("f", "4-", "4-", false, false, "f", "F","\""),
        R.id.btn_25 to KeyConfig("g", "5-", "5-", false, false, "g", "G","°"),
        R.id.btn_26 to KeyConfig("h", "6-", "6-", false, false, "h", "H","√"),
        R.id.btn_27 to KeyConfig("j", "7-", "7-", false, false, "j", "J","≲"),
        R.id.btn_28 to KeyConfig("k", "8-", "8-", false, false, "k", "K","≳"),
        R.id.btn_29 to KeyConfig("l", "9-", "9-", false, false, "l", "L","⊂"),
        R.id.btn_20 to KeyConfig(";", "0-", "0-", false, false, ";", ":","⊃"),
        R.id.btn_31 to KeyConfig("z", "1v", "1v", false, false, "z", "Z","⊆"),
        R.id.btn_32 to KeyConfig("x", "2v", "2v", false, false, "x", "X","⊇"),
        R.id.btn_33 to KeyConfig("c", "3v", "3v", false, false, "c", "C","≠"),
        R.id.btn_34 to KeyConfig("v", "4v", "4v", false, false, "v", "V","∅"),
        R.id.btn_35 to KeyConfig("b", "5v", "5v", false, false, "b", "B","≤"),
        R.id.btn_36 to KeyConfig("n", "6v", "6v", false, false, "n", "N","≥"),
        R.id.btn_37 to KeyConfig("m", "7v", "7v", false, false, "m", "M","≪"),
        R.id.btn_38 to KeyConfig(",", "8v", "8v", false, false, ",", "<","≫"),
        R.id.btn_39 to KeyConfig(".", "9v", "9v", false, false, ".", ">","≈"),
        R.id.btn_30 to KeyConfig("/", "0v", "0v", false, false, "/", "?","∓"),
    )

    private val letterMapIPA = mapOf(
        "a" to listOf<String>("æ","ɐ","ɑ","ɒ","ä","á","à","â","ã","å","ă"),
        "b" to listOf<String>("β","ɓ"),
        "c" to listOf<String>("ç","č","ƈ","ʗ"),
        "d" to listOf<String>("ɖ","ɗ","ᶑ","ȡ","ð","ʣ","ʤ","ʥ","ȸ"),
        "e" to listOf<String>("ɘ","ɵ","ə","ɚ","ɛ","ɜ","ɝ","ɞ","ʚ","ë","é","è","ê","ᵊ"),
        "f" to listOf<String>("ʩ"),
        "g" to listOf<String>("ğ","ɠ","ɣ","ˠ"),
        "h" to listOf<String>("ħ","ɦ","ɧ","ɥ","ʮ","ʯ","ʰ","ʱ"),
        "i" to listOf<String>("ɨ","ɿ","ʅ","ï","í","ì","î"),
        "j" to listOf<String>("ɟ","ʝ","ʲ","ʄ"),
        "k" to listOf<String>("ƙ","ʞ"),
        "l" to listOf<String>("ɬ","ɫ","ɮ","ɭ","ꞎ","ʎ","ʪ","ʫ","ˡ"),
        "m" to listOf<String>("ɱ","ɰ","ɯ"),
        "n" to listOf<String>("ɳ","ɲ","ŋ","ȵ","ñ","ⁿ"),
        "o" to listOf<String>("ø","œ","ɔ","ö","ó","ò","ô","õ"),
        "p" to listOf<String>("ɸ","ƥ"),
        "q" to listOf<String>("ʠ","ȹ"),
        "r" to listOf<String>("ɹ","ɻ","ɾ","ɽ","ʳ","ʴ","ʵ"),
        "s" to listOf<String>("ʂ","ɕ","š","ʃ","ʆ","ˢ"),
        "t" to listOf<String>("ʈ","ƭ","ȶ","ʇ","ʦ","ʧ","ʨ","θ","ᶿ"),
        "u" to listOf<String>("ʉ","ü","ú","ù","û"),
        "v" to listOf<String>("ʋ","ⱱ","ʌ"),
        "w" to listOf<String>("ʍ","ʷ","ʬ"),
        "x" to listOf<String>("χ","ˣ"),
        "y" to listOf<String>("ÿ","ý","ʸ"),
        "z" to listOf<String>("ʐ","ʑ","ʒ","ʓ","ž"),
        "A" to listOf<String>("ᴀ"),
        "B" to listOf<String>("ʙ"),
        "C" to listOf<String>(),
        "D" to listOf<String>(),
        "E" to listOf<String>("ᴇ"),
        "F" to listOf<String>(),
        "G" to listOf<String>("ɢ","ʛ"),
        "H" to listOf<String>("ʜ"),
        "I" to listOf<String>("ɪ"),
        "J" to listOf<String>(),
        "K" to listOf<String>(),
        "L" to listOf<String>("ʟ"),
        "M" to listOf<String>(),
        "N" to listOf<String>("ɴ"),
        "O" to listOf<String>("ʊ","ɶ","ɷ"),
        "P" to listOf<String>(),
        "Q" to listOf<String>(),
        "R" to listOf<String>("ʁ","ʀ","ʶ"),
        "S" to listOf<String>(),
        "T" to listOf<String>(),
        "U" to listOf<String>(),
        "V" to listOf<String>(),
        "W" to listOf<String>(),
        "X" to listOf<String>(),
        "Y" to listOf<String>("ʏ"),
        "Z" to listOf<String>(),
        ";" to listOf<String>("ɤ","ʘ","ǀ","ǁ","ǃ","\uD837\uDF0A","ǂ","ʭ"),
        "," to listOf<String>("ʔ","ʡ","ʕ","ʢ","ʖ","ˀ","ˁ","ˤ"),
        "." to listOf<String>(),
        "/" to listOf<String>(),
        "<" to listOf<String>(),
        ">" to listOf<String>(),
        "?" to listOf<String>(),
        "1" to listOf<String>("̀","́","̍","̎","̊","̃","̂","̌","̑","̆","̇","̈","̄","̅","̿","̚","̏","̋","︠","︡","︢","︣","⃰","̒","̓","̔","̕","̉","̐","̽","̾","͊","͋","͌"),
        "2" to listOf<String>("̖","̗","̩","͈","̥","̰","̬","̭","̮","̯","̣","̤","̱","̲","̳","̝","̞","̘","̙","͉","̪","̺","͙","̟","̠","͇","̧","̨","̜","̹","̡","̢","̫","̼","̦","̻","͚"),
        "3" to listOf<String>("̴","̵","̶","̷","̸"),
        "4" to listOf<String>("↗","↘","|","‖"),
        "5" to listOf<String>(),
        "6" to listOf<String>("ꜛ","ꜜ","ʹ","ʺ","ʻ","ʼ","ʽ","ʾ","ʿ","˂","˃","˄","˅","ˆ","ˇ","ˈ","ˉ","ˊ","ˋ","˘","˙","˚","˜","˝","˟","˭","ˮ","˹","˺"),
        "7" to listOf<String>("ˌ","ˍ","ˎ","ˏ","˒","˓","˔","˕","˖","˛","ˬ","˯","˰","˱","˲","˳","˷","˻","˼","˽","˾","˿","‿"),
        "8" to listOf<String>("ː","ˑ","˞","˴","˵","˶","˸"),
        "9" to listOf<String>("˩","˨","˧","˦","˥"),
        "0" to listOf<String>("¹","²","³","⁴","⁵","⁶","⁷","⁸","⁹","⁰"),
        "!" to listOf<String>("̛","͂","̓","̈́","͗","͘","͛"),
        "@" to listOf<String>("ͅ","͓","͔","͕","͖"),
        "#" to listOf<String>("ͣ","ͤ","ͥ","ͦ","ͧ","ͨ","ͩ","ͪ","ͫ","ͬ","ͭ","ͮ","ͯ")
    )

    private val numSup = listOf<String>("⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹")
    private val numSub = listOf<String>("₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉")

    private var nowLang = 1 //0: English, 1: Array, 2: Symbol

    private val orderMapLang by lazy {
        mapOf(
            0 to getString(R.string.lang_0),
            1 to getString(R.string.lang_1),
            2 to getString(R.string.lang_2),
        )
    }
    private var candidatesPage = 0

    private var isCap = false
    private var isIPA = false
    private var latinIPA = ""


    // Long Click (Delete)
    private val handler = Handler(Looper.getMainLooper())
    private var isDel = false

    @SuppressLint("ClickableViewAccessibility")
    private fun setContinuousDel(btn: Button) {
        val deleteRunnable = object : Runnable {
            override fun run() {
                if (isDel) {
                    onDeleteInput()
                    handler.postDelayed(this, 100)
                }
            }
        }
        btn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDel = true
                    handler.postDelayed(deleteRunnable, 300)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDel = false
                    handler.removeCallbacks(deleteRunnable)
                    v.performClick()
                }
            }
            true
        }
    }

    private fun resetInLatin() {
        isCap = false
        isIPA = false
        latinIPA = ""
        candidatesPage = 0
        btnTextToLatin()
        resetBtnDone()
        resetBtnDel()
    }


    private fun resetIPA() {
        isIPA = false
        latinIPA = ""
        candidatesPage = 0
        btnTextToNumber()
        resetBtnDone()
        resetBtnDel()
    }
    private fun resetInArray() {
        val ic = currentInputConnection ?: return
        composingBuffer.clear()
        ic.setComposingText(composingBuffer.toString(),1)
        composingCodes.clear()
        candidatesPage = 0
        btnTextToNumber()
        resetBtnDone()
    }

    private fun clearComposingText() {
        val ic = currentInputConnection ?: return
        composingBuffer.clear()
        ic.setComposingText(composingBuffer.toString(),1)
        composingCodes.clear()
    }

    private fun resetBtnDone() {
        val btnEnter = viewKeyboard!!.findViewById<Button>(R.id.btn_done)
        val actionId = (currentInputEditorInfo?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        btnEnter.text = when (actionId) {
            EditorInfo.IME_ACTION_SEARCH -> "✔"
            EditorInfo.IME_ACTION_SEND -> "▶"
            EditorInfo.IME_ACTION_GO -> "✔"
            EditorInfo.IME_ACTION_NEXT -> "↵"
            EditorInfo.IME_ACTION_DONE -> "✔"
            else -> "↵"
        }
    }


    private fun resetBtnDel() {
        val btnDel = viewKeyboard!!.findViewById<Button>(R.id.btn_del)
        btnDel.text = "⌫"
    }

    private fun btnTextToCandidatesArray() {
        keyMapArrayNumber.forEach { (id, number) ->
            val button = viewKeyboard?.findViewById<Button>(id)
            button?.text = candidateWords.getOrNull(number+candidatesPage*10) ?: getString(R.string.noWord)
        }
    }

    private fun btnTextToCandidatesIPA() {
        keyMapArrayNumber.forEach { (id, number) ->
            val button = viewKeyboard?.findViewById<Button>(id)
            if (latinIPA in listOf("1","2","3","!","@")) {
                button?.text = candidateWords.getOrNull(number + candidatesPage * 10)?.let{getString(R.string.blankIPA)+it} ?: getString(R.string.noWord)
            } else {
                button?.text = candidateWords.getOrNull(number + candidatesPage * 10) ?: getString(R.string.noWord)
            }
        }
    }
    private fun btnTextToNumber() {
        keyMapArrayNumber.forEach { (id, _) ->
            val button = viewKeyboard!!.findViewById<Button>(id)
            button.text = keyMap[id]?.text ?: "N"
        }
    }

    private fun btnTextToLatin() {
        keyMap.forEach { (id, config) ->
            val button = viewKeyboard!!.findViewById<Button>(id)
            button.text = config.eng
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBtnDonePage() {
        val btnDone = viewKeyboard!!.findViewById<Button>(R.id.btn_done)
        btnDone.text = numSup[candidatesPage+1]+"/"+numSub[(candidateWords.size+9)/10]
    }


    private fun setShowCandidates() {
        candidateWords = DictManager.trie.search(composingCodes.toString())?.toMutableList() ?: mutableListOf()
        if (candidateWords.size>10) {
            setBtnDonePage()
        }
        btnTextToCandidatesArray()
    }
    private fun onKeyInput(config: KeyConfig) {
        val ic = currentInputConnection ?: return
        if (composingCodes.length>4) {
            return
        } else if ((composingCodes.length>3) and !(config.is8up)) {
            return
        }
        composingCodes.append(config.code)
        composingBuffer.append(config.buffer)
        ic.setComposingText(composingBuffer.toString(),1)//+(arrayCandidateWords.getOrNull(0)?.let{"[${it}]"}?:"")
        setShowCandidates()
    }

    private fun onDeleteInput() {
        val ic = currentInputConnection ?: return
        if (nowLang==1 && composingCodes.isNotEmpty()) {
            composingCodes.deleteAt(composingCodes.length - 1)
            composingBuffer.deleteCharAt(composingBuffer.length - 1)
            composingBuffer.deleteCharAt(composingBuffer.length - 1)
            ic.setComposingText(composingBuffer.toString(),1)
            if (composingCodes.isEmpty()) {
                resetInArray()
            } else {
                setShowCandidates()
            }
        } else if (nowLang==0 && isIPA) {
            resetIPA()
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun onSpaceInput() {
        //val ic = currentInputConnection ?: return
        if (nowLang==1 && composingCodes.isNotEmpty()) {
            onCommitCandidate(0)
        } else if (nowLang==0 && isIPA) {
            onCommitCandidate(0)
        } else {
            onCommitDirect(" ")
        }
    }

    private fun onCommitCandidate(number: Int) {
        val ic = currentInputConnection ?: return
        ic.commitText(candidateWords.getOrNull(number+candidatesPage*10)?:"",1)
        when (nowLang) {
            0 -> {
                resetInLatin()
            }
            1 -> {
                resetInArray()
            }
        }
    }

    private fun onCommitDirect(s: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(s,1)
    }

    private fun setLang() {
        val view = viewKeyboard!!
        val btnSpace = view.findViewById<Button>(R.id.btn_space)
        btnSpace.text = orderMapLang[nowLang]
        clearComposingText()
        resetBtnDel()
        resetBtnDone()
        when (nowLang) {
            0 -> {
                resetInLatin()
                //no need to map the key since it's already in resetLatin()
            }
            1 -> {
                resetInArray()
                keyMap.forEach { (id, config) ->
                    val button = view.findViewById<Button>(id)
                    button.text = config.text
                }
            }
            2 -> {
                keyMap.forEach { (id, config) ->
                    val button = view.findViewById<Button>(id)
                    button.text = config.mark
                }
            }
        }
    }

    private fun changeColor() {
        color = (color+1) % numMapColor.size
        val c0Res = numMapColor[color]?.first ?: R.color.gra_0
        val c1Res = numMapColor[color]?.second ?: R.color.gra_1
        val c0 = resources.getColor(c0Res, null)
        val c1 = resources.getColor(c1Res, null)
        val white = resources.getColor(R.color.white, null)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf()
        )
        val colorsA = intArrayOf(c0, c1)
        val colors = intArrayOf(white, c0)
        val sca = ColorStateList(states, colorsA)
        val sc = ColorStateList(states, colors)
        keyMap.forEach { (id, config) ->
            val button = viewKeyboard!!.findViewById<Button>(id)
            if (config.isNumber) {
                button.background?.let {drawable ->
                    val wrappedDrawable = DrawableCompat.wrap(drawable)
                    DrawableCompat.setTintList(wrappedDrawable, sca)
                    button.background = wrappedDrawable
                }
            } else {
                button.background?.let {drawable ->
                    val wrappedDrawable = DrawableCompat.wrap(drawable)
                    DrawableCompat.setTintList(wrappedDrawable, sc)
                    button.background = wrappedDrawable
                }
            }
        }
        listOf(R.id.btn_set,R.id.btn_lang,R.id.btn_space,R.id.btn_done,R.id.btn_del).forEach {
            val button = viewKeyboard!!.findViewById<Button>(it)
            button.background?.let {drawable ->
                val wrappedDrawable = DrawableCompat.wrap(drawable)
                DrawableCompat.setTintList(wrappedDrawable, sca)
                button.background = wrappedDrawable
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Thread {
            applicationContext.loadDict()
        }.start()
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

        if (newSelStart==newSelEnd) {
            if (composingBuffer.isNotEmpty() && (newSelStart !in candidatesStart..candidatesEnd)) {
                resetInArray()
            }
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        when (nowLang) {
            0 -> {
                resetInLatin()
            }
            1 -> {
                resetInArray()
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View {
        val contextThemeWrapper = ContextThemeWrapper(this, R.style.Theme_ArrayKeyboard)
        val layoutInflater = layoutInflater.cloneInContext(contextThemeWrapper)
        viewKeyboard = layoutInflater.inflate(R.layout.keyboard_layout, null)
        val view = viewKeyboard!!

        for ((id, config) in keyMap) {
            bindKey(view, id, config)
        }

        val btnDel = view.findViewById<Button>(R.id.btn_del)
        btnDel.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            when (nowLang) {
                0 -> {
                    if (isIPA) {
                        if (isCap) {
                            resetInLatin()
                        } else {
                            resetIPA()
                        }
                        return@setOnClickListener
                    }
                    if (isCap) {
                        resetInLatin()
                    }
                    onDeleteInput()
                }
                1,2 -> {
                    onDeleteInput()
                }
            }
        }
        setContinuousDel(btnDel)
        val btnSpace = view.findViewById<Button>(R.id.btn_space)
        btnSpace.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onSpaceInput()
        }
        val btnLang = view.findViewById<Button>(R.id.btn_lang)
        btnLang.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            nowLang = (nowLang + 1) % orderMapLang.size
            setLang()
        }
        val btnColor = view.findViewById<Button>(R.id.btn_set)
        btnColor.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            changeColor()
        }
        val btnDone = view.findViewById<Button>(R.id.btn_done)
        btnDone?.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (nowLang==1 && composingCodes.isNotEmpty()) {
                val candidatesSize = candidateWords.size
                if (candidatesSize>10) {
                    if ((candidatesPage + 1) * 10 >= candidatesSize) {
                        candidatesPage = 0
                    } else {
                        candidatesPage += 1
                    }
                    btnTextToCandidatesArray()
                    setBtnDonePage()
                } else {
                    onCommitCandidate(0)
                }
            }else if (nowLang==0 && isIPA) {
                val candidatesSize = candidateWords.size
                if (candidatesSize>10) {
                    if ((candidatesPage + 1) * 10 >= candidatesSize) {
                        candidatesPage = 0
                    } else {
                        candidatesPage += 1
                    }
                    btnTextToCandidatesIPA()
                    setBtnDonePage()
                } else {
                    onCommitCandidate(0)
                }
            } else {
                val ic = currentInputConnection ?: return@setOnClickListener
                val editorInfo = currentInputEditorInfo ?: return@setOnClickListener
                when (val actionId = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
                    EditorInfo.IME_ACTION_SEARCH,
                    EditorInfo.IME_ACTION_SEND,
                    EditorInfo.IME_ACTION_GO,
                    EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_DONE -> {
                        // 觸發輸入框指定的特殊動作
                        ic.performEditorAction(actionId)
                    }

                    else -> {
                        // 若沒有指定特殊動作（例如純文字檔），則執行預設的「換行 (\n)」
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                    }
                }
            }
        }
        btnDone?.setOnLongClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            when (nowLang) {
                0 -> {
                    if (isIPA) {
                        resetIPA()
                    }
                    if (isCap) {
                        resetInLatin()
                    } else {
                        isCap = true
                        keyMap.forEach { (id, config) ->
                            val button = view.findViewById<Button>(id)
                            button.text = config.engShift
                        }
                    }
                }
            }
            true
        }
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        when (nowLang) {
            0 -> {
                resetInLatin()
            }
            1 -> {
                resetInArray()
            }
        }
    }

    override fun onCurrentInputMethodSubtypeChanged(subtype: android.view.inputmethod.InputMethodSubtype) {
        super.onCurrentInputMethodSubtypeChanged(subtype)
        val langTag = subtype.languageTag

        when (langTag) {
            "en-US" -> {
                nowLang = 0
                setLang()
            }
            "zh-TW", "zh-CN" -> {
                nowLang = 1
                setLang()
            }
        }
    }

    private fun bindKey(parent: View, id: Int, config: KeyConfig) {
        val button = parent.findViewById<Button>(id)
        button.text = config.text
        button.setOnClickListener { v->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            when (nowLang) {
                0 -> {
                    if (isIPA) {
                        if (config.isNumber) {
                            onCommitCandidate(keyMapArrayNumber.getValue(id))
                        } else {
                            if (isCap) {
                                onCommitDirect(config.engShift)
                            } else {
                                onCommitDirect(config.eng)
                            }
                        }
                        if (isCap) {
                            resetInLatin()
                        } else {
                            resetIPA()
                        }
                    } else if (isCap) {
                        onCommitDirect(config.engShift)
                        resetInLatin()
                    } else {
                        onCommitDirect(config.eng)
                    }
                }

                1 -> {
                    if (config.isNumber) {
                        if (composingCodes.isNotEmpty()) {
                            if (composingCodes.toString()=="w" || composingCodes.toString()=="hg") {
                                onKeyInput(config)
                            } else {
                                onCommitCandidate(keyMapArrayNumber.getValue(id))
                            }
                        } else {
                            onCommitDirect(config.code)
                        }
                    } else {
                        onKeyInput(config)
                    }
                }

                2 -> {
                    onCommitDirect(config.mark)
                }
            }
        }
        button.setOnLongClickListener { v ->
            when (nowLang) {
                0 -> {
                    if (isIPA && config.isNumber) {
                        return@setOnLongClickListener false
                    }
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    resetIPA()
                    isIPA = true
                    latinIPA = if (isCap) {
                        config.engShift
                    } else {
                        config.eng
                    }
                    candidateWords = letterMapIPA[latinIPA]?.toMutableList() ?: mutableListOf()
                    val view = viewKeyboard!!
                    val btnDel = view.findViewById<Button>(R.id.btn_del)
                    btnDel.text = "✘"
                    btnTextToCandidatesIPA()
                    if (candidateWords.size>10) {
                        setBtnDonePage()
                    }
                }
            }
            true
        }
    }
}