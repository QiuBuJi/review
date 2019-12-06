package com.example.review.New

import android.view.View

class KeyText {
    var text: String
    var isCom = false
    var isPressed = false
    var keyCode = -1
    var key = 1.toChar()
    var view: View? = null

    constructor(text: String, isCom: Boolean) {
        this.text = text
        this.isCom = isCom
    }

    constructor(text: String, isCom: Boolean, keyCode: Int) {
        this.text = text
        this.isCom = isCom
        this.keyCode = keyCode
    }

    constructor(text: String, isCom: Boolean, keyCode: Int, key: Char) {
        this.text = text
        this.isCom = isCom
        this.keyCode = keyCode
        this.key = key
    }

    constructor(text: String) {
        this.text = text
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("  isCom = $isCom")
        sb.append("  text = $text")
        sb.append("  key = $key")
        sb.append("  keyCode = $keyCode")
        return sb.toString()
    }
}