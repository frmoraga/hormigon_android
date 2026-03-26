package com.example.fmoraga.dictuc.ui.auth

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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fmoraga.dictuc.R
import com.example.fmoraga.dictuc.data.remote.sync.SyncWorker
import com.example.fmoraga.dictuc.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                runStr   = binding.txtRun.text.toString(),
                password = binding.txtPassword.text.toString()
            )
        }

        binding.btnForzarSync.setOnClickListener {
            WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                "${SyncWorker.WORK_NAME}_manual",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().build()
            )
            Snackbar.make(binding.root, "Sincronizando...", Snackbar.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is LoginState.Idle    -> setLoading(false)
                        is LoginState.Loading -> setLoading(true)
                        is LoginState.Success -> {
                            setLoading(false)
                            findNavController().navigate(R.id.action_login_to_tipoPerfil)
                            viewModel.resetState()
                        }
                        is LoginState.Error   -> {
                            setLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled     = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
