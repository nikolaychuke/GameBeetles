package com.example.beetles

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

        bindSeekBar(
            view.findViewById(R.id.seekSpeed),
            view.findViewById(R.id.textSpeed),
            label = "Скорость", offset = 1, suffix = " из 3"
        )
        bindSeekBar(
            view.findViewById(R.id.seekMaxBeetles),
            view.findViewById(R.id.textMaxBeetles),
            label = "Тараканов", offset = 1, suffix = " из 20"
        )
        bindSeekBar(
            view.findViewById(R.id.seekBonusInterval),
            view.findViewById(R.id.textBonusInterval),
            label = "Интервал", offset = 1, suffix = " сек"
        )
        bindSeekBar(
            view.findViewById(R.id.seekRoundDuration),
            view.findViewById(R.id.textRoundDuration),
            label = "Раунд", offset = 1, suffix = " сек"
        )

        return view
    }

    private fun bindSeekBar(
        seekBar: SeekBar,
        textView: TextView,
        label: String,
        offset: Int,
        suffix: String
    ) {
        textView.text = "$label: ${seekBar.progress + offset}$suffix"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "$label: ${progress + offset}$suffix"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}