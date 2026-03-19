package com.example.fmoraga.dictuc.ui.muestras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fmoraga.dictuc.databinding.FragmentListaMuestraBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListaMuestraFragment : Fragment() {

    private var _binding: FragmentListaMuestraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListaMuestraViewModel by viewModels()
    private lateinit var adapter: MuestraPlanificacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaMuestraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MuestraPlanificacionAdapter { muestra ->
            val action = ListaMuestraFragmentDirections
                .actionListaMuestraToInfoMuestra(muestra.idMuestra)
            findNavController().navigate(action)
        }
        binding.recyclerMuestras.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.muestras.collect { list ->
                    adapter.submitList(list)
                    binding.txtEmpty.visibility =
                        if (list.isEmpty() && !viewModel.isLoading.value) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { loading ->
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                    if (!loading && viewModel.muestras.value.isEmpty()) {
                        binding.txtEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncError.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.clearSyncError()
                    }
                }
            }
        }

        binding.btnTerminarJornada.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Terminar Jornada")
                .setMessage("¿Desea terminar la jornada de hoy?")
                .setPositiveButton("Sí") { _, _ -> viewModel.terminarJornada() }
                .setNegativeButton("No", null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jornadaTerminada.collect { terminada ->
                    if (terminada) binding.btnTerminarJornada.isEnabled = false
                }
            }
        }

        // Sync inmediato al entrar al fragment
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
