package com.example.fmoraga.dictuc.ui.retiro

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
import com.example.fmoraga.dictuc.databinding.FragmentListaRetiroBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListaRetiroFragment : Fragment() {

    private var _binding: FragmentListaRetiroBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RetiroViewModel by viewModels()
    private lateinit var adapter: RetiroMuestraAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaRetiroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RetiroMuestraAdapter { retiro ->
            val action = ListaRetiroFragmentDirections
                .actionListaRetiroToRetirarMuestra(retiro.idMuestra)
            findNavController().navigate(action)
        }
        binding.recyclerRetiros.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.retiros.collect { list ->
                    adapter.submitList(list)
                    binding.txtEmpty.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
