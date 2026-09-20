package com.example.beetles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.Calendar

data class Player(
    val fio: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiac: String
)

class PlayerFormFragment : Fragment() {

    private var selectedDay = 0
    private var selectedMonth = 0
    private var selectedYear = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.player_form, container, false)

        val editFio = view.findViewById<EditText>(R.id.editFio)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupGender)
        val spinnerCourse = view.findViewById<Spinner>(R.id.spinnerCourse)
        val seekBar = view.findViewById<SeekBar>(R.id.seekDifficulty)
        val textDifficulty = view.findViewById<TextView>(R.id.textDifficulty)
        val calendar = view.findViewById<CalendarView>(R.id.calendarBirth)
        val btnShow = view.findViewById<Button>(R.id.btnShow)
        val textResult = view.findViewById<TextView>(R.id.textResult)
        val imageZodiac = view.findViewById<ImageView>(R.id.imageZodiac)

        val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
        spinnerCourse.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, courses
        )

        val today = Calendar.getInstance()
        selectedDay = today.get(Calendar.DAY_OF_MONTH)
        selectedMonth = today.get(Calendar.MONTH) + 1
        selectedYear = today.get(Calendar.YEAR)

        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDay = dayOfMonth
            selectedMonth = month + 1
            selectedYear = year
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                textDifficulty.text = "Уровень: ${progress + 1} из 10"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnShow.setOnClickListener {
            val fio = editFio.text.toString().trim()
            if (fio.isEmpty()) {
                Toast.makeText(requireContext(), "Введите ФИО", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = when (radioGroup.checkedRadioButtonId) {
                R.id.radioMale -> "Мужской"
                R.id.radioFemale -> "Женский"
                else -> "Не указан"
            }
            val course = spinnerCourse.selectedItem.toString()
            val difficulty = seekBar.progress + 1

            val birthDate = "%02d.%02d.%d".format(selectedDay, selectedMonth, selectedYear)
            val zodiac = getZodiac(selectedDay, selectedMonth)

            val player = Player(fio, gender, course, difficulty, birthDate, zodiac)

            textResult.text = """
                ФИО: ${player.fio}
                Пол: ${player.gender}
                Курс: ${player.course}
                Уровень сложности: ${player.difficulty} из 10
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiac}
            """.trimIndent()

            imageZodiac.setImageResource(getZodiacImage(player.zodiac))
            imageZodiac.visibility = View.VISIBLE
        }

        return view
    }

    private fun getZodiac(day: Int, month: Int): String = when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
        (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
        (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
        (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
        (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
        (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
        (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
        (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
        (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
        (month == 2 && day >= 19) || (month == 3 && day <= 20) -> "Рыбы"
        else -> "Не определён"
    }

    private fun getZodiacImage(zodiac: String): Int = when (zodiac) {
        "Овен" -> R.drawable.zodiac_aries
        "Телец" -> R.drawable.zodiac_taurus
        "Близнецы" -> R.drawable.zodiac_gemini
        "Рак" -> R.drawable.zodiac_cancer
        "Лев" -> R.drawable.zodiac_leo
        "Дева" -> R.drawable.zodiac_virgo
        "Весы" -> R.drawable.zodiac_libra
        "Скорпион" -> R.drawable.zodiac_scorpio
        "Стрелец" -> R.drawable.zodiac_sagittarius
        "Козерог" -> R.drawable.zodiac_capricorn
        "Водолей" -> R.drawable.zodiac_aquarius
        "Рыбы" -> R.drawable.zodiac_pisces
        else -> android.R.drawable.ic_menu_help
    }
}