package com.example.arraykeyboard

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var candidates: List<String>? = null
}

class Trie {
    private val root = TrieNode()

    fun insert(code: String, words: List<String>) {
        var current: TrieNode = root
        for (char in code) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.candidates = words
    }

    fun search(code: String): List<String>? {
        var current = root
        for (char in code) {
            current = current.children[char] ?: return null
        }
        return current.candidates
    }
}

object DictManager {
    val trie = Trie()
    var isLoad = false

    fun Context.loadDict() {
        if (isLoad) {
            return
        }
        try {
            val fileName = getString(R.string.arrayTxtFileNameBig)
            val inputStream = this.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

            val tempMap = HashMap<String, MutableList<String>>()

            reader.forEachLine { line ->
                if (line.isBlank() || line.startsWith("#")) return@forEachLine

                val parts = line.split("\t")
                if (parts.size >= 2) {
                    val code = parts[0].trim()
                    val word = parts[1].trim()
                    tempMap.getOrPut(code) { mutableListOf() }.add(word)
                }
            }
            reader.close()

            for ((code, words) in tempMap) {
                trie.insert(code, words)
            }

            isLoad = true
            Log.d("DictManager", "Dictionary loaded successfully! Total unique codes: ${tempMap.size}")

            // 測試搜尋一個常見代碼（您可以將 "8^" 換成您的字典裡實際有的代碼來測試）
            // val testResult = trie.search("8^")
            // Log.d("DictManager", "Test search result: $testResult")

        } catch (e: Exception) {
            Log.e("DictManager", "Failed to load dictionary file!", e)
        }
        //val testResult = trie.search("o..s")
        //Log.d("DictManager", "測試查字結果: $testResult")
    }
}
