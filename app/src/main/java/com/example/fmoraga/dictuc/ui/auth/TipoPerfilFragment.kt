package com.example.fmoraga.dictuc.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fmoraga.dictuc.R
import com.example.fmoraga.dictuc.databinding.FragmentTipoPerfilBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipoPerfilFragment : Fragment() {

    private var _binding: FragmentTipoPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipoPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMuestreador.setOnClickListener {
            findNavController().navigate(R.id.action_tipoPerfil_to_listaMuestra)
        }
        binding.btnRetiro.setOnClickListener {
            findNavController().navigate(R.id.action_tipoPerfil_to_listaRetiro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
