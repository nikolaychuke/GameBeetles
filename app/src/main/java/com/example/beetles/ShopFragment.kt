package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ShopFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.shop_form, container, false)

        val container = view.findViewById<LinearLayout>(R.id.shopContainer)

        viewModel.selectedAbilities.observe(viewLifecycleOwner) {
            rebuildList(container)
        }

        return view
    }

    private fun rebuildList(container: LinearLayout) {
        container.removeAllViews()

        val selected = viewModel.selectedAbilities.value ?: emptySet()

        for (type in AbilityType.values()) {
            val isSelected = type in selected
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_ability, container, false)

            itemView.findViewById<TextView>(R.id.textAbilityName).text = type.displayName
            itemView.findViewById<TextView>(R.id.textAbilityDesc).text = type.description
            itemView.findViewById<TextView>(R.id.textAbilityCharges).text =
                if (isSelected) "Выбрано" else "Не выбрано"

            val btn = itemView.findViewById<Button>(R.id.btnBuy)
            btn.isEnabled = isSelected || viewModel.canSelectMore()
            btn.text = if (isSelected) "Убрать" else "Выбрать"
            btn.setOnClickListener {
                if (isSelected) {
                    viewModel.toggleAbilitySelection(type)
                } else if (viewModel.canSelectMore()) {
                    viewModel.toggleAbilitySelection(type)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Можно выбрать только 2 способности",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            container.addView(itemView)
        }
    }
}