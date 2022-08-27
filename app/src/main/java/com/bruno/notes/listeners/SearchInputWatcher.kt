package com.bruno.notes.listeners

import android.text.Editable
import android.text.TextWatcher

class SearchInputWatcher(private val onInputChange: (String) -> Unit) : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

    override fun afterTextChanged(searchInput: Editable?) {
        onInputChange(searchInput.toString())
    }
}