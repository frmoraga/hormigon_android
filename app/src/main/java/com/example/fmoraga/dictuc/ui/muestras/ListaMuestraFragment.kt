package com.example.fmoraga.dictuc.ui.muestras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fmoraga.dictuc.data.remote.sync.SyncWorker
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

        // Fix 4: mostrar banner cuando hay muestras sin sincronizar
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pendingCount.collect { count ->
                    if (count > 0) {
                        binding.txtPendingSync.text =
                            "⚠ $count muestra${if (count > 1) "s" else ""} pendiente${if (count > 1) "s" else ""} de sincronizar"
                        binding.txtPendingSync.visibility = android.view.View.VISIBLE
                    } else {
                        binding.txtPendingSync.visibility = android.view.View.GONE
                    }
                }
            }
        }

        // Empujar los FABs por encima de la barra de navegación del sistema
        val density = resources.displayMetrics.density
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnTerminarJornada) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navBarHeight + (16 * density).toInt()
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnForzarSync) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // 56dp (altura ExtendedFAB) + 16dp margen inferior + 16dp separación + navBar
                bottomMargin = navBarHeight + (88 * density).toInt()
            }
            insets
        }

        // Botón de sync manual
        binding.btnForzarSync.setOnClickListener {
            WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                "${SyncWorker.WORK_NAME}_manual",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().build()
            )
            Snackbar.make(binding.root, "Sincronizando...", Snackbar.LENGTH_SHORT).show()
        }

        // Sync inmediato al entrar al fragment
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
