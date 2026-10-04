package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beetles.data.GameRepository
import com.example.beetles.data.ScoreWithPlayer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordsFragment : Fragment() {

    private lateinit var repository: GameRepository
    private lateinit var listView: ListView
    private lateinit var textEmpty: TextView

    private val records = mutableListOf<ScoreWithPlayer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.records_form, container, false)
        repository = GameRepository(requireContext())

        listView = view.findViewById(R.id.listRecords)
        textEmpty = view.findViewById(R.id.textEmpty)

        listView.adapter = object : BaseAdapter() {
            override fun getCount() = records.size
            override fun getItem(position: Int) = records[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val v = convertView ?: LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_record, parent, false)

                val rec = records[position]

                v.findViewById<TextView>(R.id.textRecordName).text =
                    "${position + 1}. ${rec.player.fio}"

                v.findViewById<TextView>(R.id.textRecordScore).text =
                    "Очки: ${rec.score.score} | Точность: ${(rec.score.accuracy * 100).toInt()}%"

                v.findViewById<TextView>(R.id.textRecordDetails).text =
                    "Сложность: ${rec.score.difficulty} | " +
                            "Попаданий: ${rec.score.hits} | Промахов: ${rec.score.misses} | " +
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                .format(Date(rec.score.timestamp))

                return v
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val list = repository.getAllScores()
            records.clear()
            records.addAll(list)
            (listView.adapter as BaseAdapter).notifyDataSetChanged()

            textEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
            listView.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}