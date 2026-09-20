package com.example.beetles

import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.fragment.app.Fragment

class RulesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rules_form, container, false)
        val webView = view.findViewById<WebView>(R.id.webViewRules)
        webView.loadUrl("file:///android_res/raw/rules.html")
        return view
    }
}