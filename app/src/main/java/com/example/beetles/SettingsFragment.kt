package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_form, container, false)
        val prefs = requireContext().getSharedPreferences("beetles", Context.MODE_PRIVATE)

        bindSeekBar(view.findViewById(R.id.seekSpeed),
            view.findViewById(R.id.textSpeed), "Скорость", 1, " из 3",
            prefs, "speed")

        bindSeekBar(view.findViewById(R.id.seekMaxBeetles),
            view.findViewById(R.id.textMaxBeetles), "Тараканов", 1, " из 20",
            prefs, "maxBeetles")

        bindSeekBar(view.findViewById(R.id.seekBonusInterval),
            view.findViewById(R.id.textBonusInterval), "Интервал", 1, " сек",
            prefs, "bonusInterval")

        bindSeekBar(view.findViewById(R.id.seekRoundDuration),
            view.findViewById(R.id.textRoundDuration), "Раунд", 1, " сек",
            prefs, "roundDuration")

        return view
    }

    private fun bindSeekBar(
        seekBar: SeekBar,
        textView: TextView,
        label: String,
        offset: Int,
        suffix: String,
        prefs: android.content.SharedPreferences,
        key: String
    ) {
        val saved = prefs.getInt(key, offset) - offset
        seekBar.progress = saved.coerceIn(0, seekBar.max)
        textView.text = "$label: ${seekBar.progress + offset}$suffix"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "$label: ${progress + offset}$suffix"
                prefs.edit().putInt(key, progress + offset).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}