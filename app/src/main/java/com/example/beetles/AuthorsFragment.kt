package com.example.beetles

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment() {

    private val authors = listOf(
        "Степаненко Мирон Ип314" to R.drawable.miron,
        "Николайчук Егор Ип314" to R.drawable.egor
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.authors_form, container, false)
        val list = view.findViewById<ListView>(R.id.listAuthors)

        list.adapter = object : BaseAdapter() {
            override fun getCount() = authors.size
            override fun getItem(p: Int) = authors[p]
            override fun getItemId(p: Int) = p.toLong()
            override fun getView(p: Int, convertView: View?, parent: ViewGroup?): View {
                val v = convertView ?: LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_author, parent, false)
                v.findViewById<TextView>(R.id.textAuthorName).text = authors[p].first
                v.findViewById<ImageView>(R.id.imgAuthor).setImageResource(authors[p].second)
                return v
            }
        }
        return view
    }
}