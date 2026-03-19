package com.example.fmoraga.dictuc.ui.muestras

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fmoraga.dictuc.databinding.FragmentInfoMuestraBinding
import com.example.fmoraga.dictuc.domain.model.Muestra
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InfoMuestraFragment : Fragment() {

    private var _binding: FragmentInfoMuestraBinding? = null
    private val binding get() = _binding!!
    private val args: InfoMuestraFragmentArgs by navArgs()

    @Inject lateinit var repository: DictucRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoMuestraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val muestra = repository.getMuestra(args.idMuestra)
            val tomada  = repository.getMuestraTomada(args.idMuestra)

            muestra?.let { bind(it) }

            if (tomada != null) {
                // Muestra ya tomada — mostrar banner y bloquear botón
                val sincDetalle = if (tomada.enviado)
                    "Sincronizada con el servidor ✓"
                else
                    "Pendiente de sincronización con el servidor ↑"
                binding.cardEstado.visibility = View.VISIBLE
                binding.cardEstado.setCardBackgroundColor(Color.parseColor("#F1F8E9"))
                binding.txtEstadoTitulo.text = "✓  Muestra ya tomada"
                binding.txtEstadoTitulo.setTextColor(Color.parseColor("#2E7D32"))
                binding.txtEstadoDetalle.text =
                    "Registrada el ${tomada.fechaElaboracion}.\n$sincDetalle\n\nNo es posible tomarla nuevamente."
                binding.btnTomarMuestra.isEnabled = false
                binding.btnTomarMuestra.text = "Muestra ya tomada"
            } else {
                binding.btnTomarMuestra.setOnClickListener {
                    findNavController().navigate(
                        InfoMuestraFragmentDirections.actionInfoMuestraToTomaMuestra(args.idMuestra)
                    )
                }
            }
        }
    }

    private fun bind(m: Muestra) {
        binding.txtCliente.text   = m.cardFName
        binding.txtDireccion.text = "${m.address}, ${m.county}"
        binding.txtMaterial.text  = m.nombreMaterial
        binding.txtMolde.text     = m.nombreMolde
        binding.txtProbetas.text  = "Probetas: ${m.totalProbeta} · Edad: ${m.edadProbeta}"
        binding.txtDesc.text      = m.prodDesc
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
