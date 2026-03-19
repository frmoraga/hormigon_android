package com.example.fmoraga.dictuc.ui.retiro

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fmoraga.dictuc.databinding.FragmentRetirarMuestraBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class RetirarMuestraFragment : Fragment() {

    private var _binding: FragmentRetirarMuestraBinding? = null
    private val binding get() = _binding!!
    private val args: RetirarMuestraFragmentArgs by navArgs()
    private val viewModel: RetiroViewModel by viewModels()

    private var photoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) binding.imgFoto.setImageURI(photoUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetirarMuestraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgFoto.setOnClickListener { launchCamera() }

        binding.btnConfirmarRetiro.setOnClickListener {
            if (photoUri == null) {
                Snackbar.make(binding.root, "Debe tomar una foto antes de confirmar el retiro", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.retirarMuestra(
                idMuestra    = args.idMuestra,
                pathFoto     = photoUri?.path ?: "",
                observacion  = binding.txtObservacion.text.toString()
            )
        }

        viewModel.checkYaRetirada(args.idMuestra)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.yaRetirada.collect { yaRetirada ->
                        if (yaRetirada) {
                            binding.btnConfirmarRetiro.isEnabled = false
                            binding.btnConfirmarRetiro.text = "Muestra ya retirada"
                            Snackbar.make(binding.root, "Esta muestra ya fue retirada", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                launch {
                    viewModel.retirado.collect { ok ->
                        if (ok) {
                            Snackbar.make(binding.root, "Muestra retirada", Snackbar.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                            viewModel.resetRetiro()
                        }
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        val ts  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val f   = File.createTempFile("RETIRO_$ts", ".jpg", dir)
        photoUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", f
        )
        takePictureLauncher.launch(photoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
