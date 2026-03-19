package com.example.fmoraga.dictuc.ui.retiro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fmoraga.dictuc.databinding.ItemRetiroMuestraBinding
import com.example.fmoraga.dictuc.domain.model.RetiroMuestra

class RetiroMuestraAdapter(
    private val onClick: (RetiroMuestra) -> Unit
) : ListAdapter<RetiroMuestra, RetiroMuestraAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemRetiroMuestraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RetiroMuestra) {
            binding.txtCliente.text   = item.cardFName
            binding.txtDireccion.text = item.address
            binding.txtMaterial.text  = item.nombreMaterial
            binding.txtBoleta.text    = if (item.numBoleta.isNotEmpty()) "Boleta: ${item.numBoleta}" else ""
            binding.txtFecha.text     = item.fechaProgramada
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRetiroMuestraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RetiroMuestra>() {
            override fun areItemsTheSame(a: RetiroMuestra, b: RetiroMuestra) =
                a.idMuestra == b.idMuestra
            override fun areContentsTheSame(a: RetiroMuestra, b: RetiroMuestra) = a == b
        }
    }
}
