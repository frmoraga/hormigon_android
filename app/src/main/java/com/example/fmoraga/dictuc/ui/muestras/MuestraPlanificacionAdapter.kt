package com.example.fmoraga.dictuc.ui.muestras

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fmoraga.dictuc.domain.model.MuestraPlanificacion
import com.example.fmoraga.dictuc.databinding.ItemMuestraPlanificacionBinding

class MuestraPlanificacionAdapter(
    private val onClick: (MuestraPlanificacion) -> Unit
) : ListAdapter<MuestraPlanificacion, MuestraPlanificacionAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemMuestraPlanificacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MuestraPlanificacion) {
            binding.txtCliente.text   = item.cardFName
            binding.txtDireccion.text = "${item.address}, ${item.county}"
            binding.txtMaterial.text  = item.nombreMaterial
            binding.txtProbeta.text   = "Probetas: ${item.totalProbeta} | Edad: ${item.edadProbeta}"
            binding.txtIdMuestra.text = "#${item.idMuestra}"

            when (item.estado) {
                2 -> {
                    // Tomada (servidor: id_estado=2)
                    val sincLabel = if (item.enviado) "  ·  Sincronizada ✓" else "  ·  Pendiente sincronización ↑"
                    binding.txtEstadoBarra.text            = "✓  MUESTRA TOMADA$sincLabel"
                    binding.layoutEstadoBarra.setBackgroundColor(
                        if (item.enviado) Color.parseColor("#2E7D32") else Color.parseColor("#558B2F")
                    )
                    binding.root.strokeColor               = Color.parseColor("#2E7D32")
                    binding.root.setCardBackgroundColor(Color.parseColor("#F1F8E9"))
                    binding.txtCliente.alpha               = 0.55f
                    binding.txtDireccion.alpha             = 0.55f
                    binding.txtMaterial.alpha              = 0.55f
                    binding.txtProbeta.alpha               = 0.55f
                }
                3 -> {
                    // Suspendida (servidor: id_estado=3)
                    binding.txtEstadoBarra.text            = "⚠  MUESTRA SUSPENDIDA"
                    binding.layoutEstadoBarra.setBackgroundColor(Color.parseColor("#E65100"))
                    binding.root.strokeColor               = Color.parseColor("#E65100")
                    binding.root.setCardBackgroundColor(Color.parseColor("#FFF8E1"))
                    binding.txtCliente.alpha               = 1f
                    binding.txtDireccion.alpha             = 1f
                    binding.txtMaterial.alpha              = 1f
                    binding.txtProbeta.alpha               = 1f
                }
                4 -> {
                    // Retirada (servidor: id_estado=4)
                    binding.txtEstadoBarra.text            = "◼  MUESTRA RETIRADA"
                    binding.layoutEstadoBarra.setBackgroundColor(Color.parseColor("#546E7A"))
                    binding.root.strokeColor               = Color.parseColor("#546E7A")
                    binding.root.setCardBackgroundColor(Color.parseColor("#ECEFF1"))
                    binding.txtCliente.alpha               = 0.55f
                    binding.txtDireccion.alpha             = 0.55f
                    binding.txtMaterial.alpha              = 0.55f
                    binding.txtProbeta.alpha               = 0.55f
                }
                else -> {
                    // Pendiente (servidor: id_estado=1 u otro)
                    binding.txtEstadoBarra.text            = "●  PENDIENTE"
                    binding.layoutEstadoBarra.setBackgroundColor(Color.parseColor("#1565C0"))
                    binding.root.strokeColor               = Color.parseColor("#1565C0")
                    binding.root.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    binding.txtCliente.alpha               = 1f
                    binding.txtDireccion.alpha             = 1f
                    binding.txtMaterial.alpha              = 1f
                    binding.txtProbeta.alpha               = 1f
                }
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMuestraPlanificacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MuestraPlanificacion>() {
            override fun areItemsTheSame(a: MuestraPlanificacion, b: MuestraPlanificacion) =
                a.idMuestra == b.idMuestra
            override fun areContentsTheSame(a: MuestraPlanificacion, b: MuestraPlanificacion) =
                a == b
        }
    }
}
