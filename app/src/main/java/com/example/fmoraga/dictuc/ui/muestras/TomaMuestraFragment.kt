package com.example.fmoraga.dictuc.ui.muestras

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.example.fmoraga.dictuc.R
import com.example.fmoraga.dictuc.databinding.FragmentTomaMuestraBinding
import com.example.fmoraga.dictuc.domain.model.MuestraTomada
import com.example.fmoraga.dictuc.ui.common.SignatureView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TomaMuestraFragment : Fragment() {

    private var _binding: FragmentTomaMuestraBinding? = null
    private val binding get() = _binding!!
    private val args: TomaMuestraFragmentArgs by navArgs()
    private val viewModel: TomaMuestraViewModel by viewModels()

    // Fotos
    private var activePhotoSlot = 1
    private var tempPhotoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            when (activePhotoSlot) {
                1 -> { viewModel.photoUri1 = tempPhotoUri; binding.imgFoto1.setImageURI(tempPhotoUri) }
                2 -> { viewModel.photoUri2 = tempPhotoUri; binding.imgFoto2.setImageURI(tempPhotoUri) }
                3 -> { viewModel.photoUri3 = tempPhotoUri; binding.imgFoto3.setImageURI(tempPhotoUri) }
                4 -> { viewModel.photoUri4 = tempPhotoUri; binding.imgFoto4.setImageURI(tempPhotoUri) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTomaMuestraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupTimePickers()
        setupPhotos()
        setupSignatures()
        setupSubmit()
        setupSuspender()
        setupConditionalVisibility()
        observeState()

        // Mostrar datos de la muestra en el encabezado
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.muestra.collect { m ->
                m?.let {
                    binding.txtClienteHeader.text   = it.cardFName
                    binding.txtDireccionHeader.text = "${it.address}, ${it.county}"
                    binding.txtMaterialHeader.text  = it.nombreMaterial
                    binding.spinnerTipoProbeta.setSelection(it.idProbeta.toInt())
                }
            }
        }
    }

    // ─── Fecha ─────────────────────────────────────────────────────────────

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        binding.txtFechaElaboracion.setText(
            String.format("%02d-%02d-%04d", cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        )
        binding.btnCalendario.setOnClickListener {
            DatePickerDialog(requireContext(),
                { _, y, m, d -> binding.txtFechaElaboracion.setText(
                    String.format("%02d-%02d-%04d", d, m + 1, y)) },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // ─── Time pickers (Material) ───────────────────────────────────────────

    private fun setupTimePickers() {
        fun showTimePicker(title: String, onSet: (String) -> Unit) {
            val cal = Calendar.getInstance()
            val fm = childFragmentManager
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .setTitleText(title)
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        onSet(String.format("%02d:%02d", this.hour, this.minute))
                    }
                    show(fm, title)
                }
        }

        binding.btnHoraLlegada.setOnClickListener {
            showTimePicker("Hora llegada") { binding.txtHoraLlegada.setText(it) }
        }
        binding.btnHoraMuestreo.setOnClickListener {
            showTimePicker("Hora muestreo") { binding.txtHoraMuestreo.setText(it) }
        }
        binding.btnHoraSalida.setOnClickListener {
            showTimePicker("Hora salida planta") { binding.txtHoraSalida.setText(it) }
        }
        binding.btnHoraSalidaObra.setOnClickListener {
            showTimePicker("Hora salida obra") { binding.txtHoraSalidaObra.setText(it) }
        }
    }

    // ─── Fotos ─────────────────────────────────────────────────────────────

    private fun setupPhotos() {
        fun launchCamera(slot: Int) {
            activePhotoSlot = slot
            val photoFile = createImageFile()
            tempPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(tempPhotoUri)
        }
        binding.imgFoto1.setOnClickListener { launchCamera(1) }
        binding.imgFoto2.setOnClickListener { launchCamera(2) }
        binding.imgFoto3.setOnClickListener { launchCamera(3) }
        binding.imgFoto4.setOnClickListener { launchCamera(4) }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("DICTUC_$timestamp", ".jpg", dir)
    }

    // ─── Firmas ────────────────────────────────────────────────────────────

    private fun setupSignatures() {
        binding.btnFirmar1.setOnClickListener { showSignatureDialog(1) }
        binding.btnFirmar2.setOnClickListener { showSignatureDialog(2) }
    }

    private fun showSignatureDialog(slot: Int) {
        val signatureView = SignatureView(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Firma ${if (slot == 1) "Muestreador" else "Mixero"}")
            .setView(signatureView)
            .setPositiveButton("Guardar") { _, _ ->
                if (!signatureView.isEmpty) {
                    val dir  = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                    val file = "firma_${slot}_${System.currentTimeMillis()}.png"
                    val path = signatureView.saveToFile(dir, file)
                    if (slot == 1) {
                        viewModel.signaturePath1 = path
                        binding.txtFirma1.setText(path)
                    } else {
                        viewModel.signaturePath2 = path
                        binding.txtFirma2.setText(path)
                    }
                }
            }
            .setNegativeButton("Limpiar") { _, _ -> signatureView.clear() }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    // ─── Guardar muestra ───────────────────────────────────────────────────

    private fun setupSubmit() {
        binding.btnTomarMuestra.setOnClickListener {
            val b = binding
            val mt = MuestraTomada(
                idMuestra             = args.idMuestra,
                runMuestreador        = 0L, // inyectado por el VM desde DataStore
                fechaElaboracion      = b.txtFechaElaboracion.text.toString(),
                tipoHormigon          = (b.spinnerTipoHormigon.selectedItemPosition - 1).toLong(),
                otraOpcion            = b.txtOtraOpcion.text.toString(),
                idMaterial            = (b.spinnerPreparadoObra.selectedItemPosition - 1).toLong(),
                marcaPremezclado      = b.txtMarcaPremezclado.text.toString(),
                numCamion             = b.txtNumCamion.text.toString().toLongOrNull() ?: 0,
                numGuia               = b.txtNumGuia.text.toString().toLongOrNull() ?: 0,
                idPreparadoObra       = 0L,
                planta                = b.txtPlanta.text.toString(),
                dosificacionKgM       = b.txtDosificacion.text.toString().toDoubleOrNull() ?: 0.0,
                marcaPObra            = b.txtMarcaPObra.text.toString(),
                tipoPObra             = b.txtTipoPObra.text.toString(),
                procedenciaArena      = b.txtArena.text.toString(),
                procedenciaGravilla   = b.txtGravilla.text.toString(),
                procedenciaGrava      = b.txtGrava.text.toString(),
                aditivoObra           = if (b.checkAditivo.isChecked) 1L else 0L,
                marcaAditivoObra      = b.txtMarcaAditivo.text.toString(),
                tipoAditivoObra       = b.txtTipoAditivo.text.toString(),
                cantidadAditivoObra   = b.txtCantidadAditivo.text.toString().toDoubleOrNull() ?: 0.0,
                idEmpleadoEn          = b.spinnerElemento.selectedItemPosition.toLong(),
                idEjesEmpleadoEn      = 0L,
                ejeX                  = b.txtEjeX.text.toString(),
                ejeY                  = b.txtEjeY.text.toString(),
                ejeZ                  = b.txtEjeZ.text.toString(),
                ejeA                  = b.txtEjeA.text.toString(),
                ejeB                  = b.txtEjeB.text.toString(),
                idCompactacion        = b.spinnerCompactacion.selectedItemPosition.toLong(),
                idColocacion          = b.spinnerColocacion.selectedItemPosition.toLong(),
                idCompactacionProbeta = (b.spinnerCompactacionProbeta.selectedItemPosition - 1).toLong(),
                numVibrador           = b.txtVibrador.text.toString().toLongOrNull() ?: 0,
                idEnsayo              = buildEnsayo(),
                numCono               = b.txtNumeroCono.text.toString().toDoubleOrNull() ?: 0.0,
                nombreEquipoCono      = b.txtEquipoCono.text.toString().toLongOrNull() ?: 0,
                idAspecto             = b.radioGrpAspecto.indexOfChild(b.root.findViewById(b.radioGrpAspecto.checkedRadioButtonId)).toLong(),
                idTextura             = b.radioGrpTextura.indexOfChild(b.root.findViewById(b.radioGrpTextura.checkedRadioButtonId)).toLong(),
                cAmbiente             = b.txtTempAmbiente.text.toString().toDoubleOrNull() ?: 0.0,
                nombreEquipoT         = b.txtEquipoTemp.text.toString().toLongOrNull() ?: 0,
                hormigonC             = b.txtTempHormigon.text.toString().toDoubleOrNull() ?: 0.0,
                densidadKgM           = b.txtDensidadKg.text.toString().toDoubleOrNull() ?: 0.0,
                volRecipienteL        = b.txtVolRecipiente.text.toString().toDoubleOrNull() ?: 0.0,
                pesoRecipienteMaterialKg = b.txtPesoRecMat.text.toString().toDoubleOrNull() ?: 0.0,
                numEquipoDensidad     = b.txtEquipoDensidad.text.toString().toLongOrNull() ?: 0,
                pesoRecipienteKg      = b.txtPesoRec.text.toString().toDoubleOrNull() ?: 0.0,
                pesoMaterialKg        = b.txtPesoMat.text.toString().toDoubleOrNull() ?: 0.0,
                contenidoAire         = b.txtContenidoAire.text.toString().toDoubleOrNull() ?: 0.0,
                nombreEquipoContAire  = b.txtEquipoAire.text.toString(),
                pathFoto              = viewModel.photoUri1?.path ?: "",
                pathFoto2             = viewModel.photoUri2?.path ?: "",
                pathFoto3             = viewModel.photoUri3?.path ?: "",
                pathFoto4             = viewModel.photoUri4?.path ?: "",
                pathFirma             = viewModel.signaturePath1,
                nombreMixero          = b.txtNombreMixero1.text.toString(),
                mailMixero            = b.txtEmailMixero1.text.toString(),
                pathFirma2            = viewModel.signaturePath2,
                nombreMixero2         = b.txtNombreMixero2.text.toString(),
                mailMixero2           = b.txtEmailMixero2.text.toString(),
                observacion           = b.txtObservacion.text.toString(),
                numBoleta             = b.txtNumBoleta.text.toString(),
                horaLlegada           = b.txtHoraLlegada.text.toString(),
                horaMuestreo          = b.txtHoraMuestreo.text.toString(),
                horaSalida            = b.txtHoraSalida.text.toString(),
                horaSalidaObra        = b.txtHoraSalidaObra.text.toString(),
                metrosCubicos         = b.txtMetrosCubicos.text.toString().toDoubleOrNull() ?: 0.0,
                numPatente            = b.txtPatente.text.toString(),
                tipoCurado            = b.spinnerCurado.selectedItemPosition.toLong(),
                designacion           = b.txtDesignacion.text.toString(),
                tipoProbeta           = b.spinnerTipoProbeta.selectedItemPosition.toLong()
            )
            // Validate before saving
            val errors = mutableListOf<String>()
            if (b.txtFechaElaboracion.text.isNullOrBlank())
                errors.add("• Fecha de elaboración")
            if (b.spinnerTipoHormigon.selectedItemPosition == 0)
                errors.add("• Tipo de hormigón")
            if (b.spinnerPreparadoObra.selectedItemPosition == 0)
                errors.add("• Preparado en (planta/obra)")
            if (b.spinnerPreparadoObra.selectedItemPosition == 1) {
                if (b.txtMarcaPremezclado.text.isNullOrBlank()) errors.add("• Marca premezclado")
                if (b.txtNumCamion.text.isNullOrBlank()) errors.add("• Número de camión")
                if (b.txtNumGuia.text.isNullOrBlank()) errors.add("• Número de guía")
                if (b.txtPlanta.text.isNullOrBlank()) errors.add("• Planta")
            }
            if (b.spinnerElemento.selectedItemPosition == 0)
                errors.add("• Posición de colocación (elemento)")
            if (b.spinnerCompactacion.selectedItemPosition == 0)
                errors.add("• Tipo de compactación")
            if (b.spinnerColocacion.selectedItemPosition == 0)
                errors.add("• Tipo de colocación")
            if (b.spinnerCompactacionProbeta.selectedItemPosition == 0)
                errors.add("• Compactación de probeta")
            if (b.spinnerCurado.selectedItemPosition == 0)
                errors.add("• Tipo de curado")
            if (b.spinnerTipoProbeta.selectedItemPosition == 0)
                errors.add("• Tipo de probeta")
            if (b.txtMetrosCubicos.text.isNullOrBlank())
                errors.add("• Metros cúbicos")
            if (b.txtPatente.text.isNullOrBlank())
                errors.add("• Patente del vehículo")
            if (b.txtNumeroCono.text.isNullOrBlank())
                errors.add("• Número de cono")
            if (b.txtTempAmbiente.text.isNullOrBlank())
                errors.add("• Temperatura ambiente")
            if (b.txtTempHormigon.text.isNullOrBlank())
                errors.add("• Temperatura del hormigón")
            if (errors.isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Campos requeridos")
                    .setMessage(errors.joinToString("\n"))
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }
            viewModel.saveMuestra(mt)
        }
    }

    private fun buildEnsayo(): String {
        val ensayos = mutableListOf<String>()
        if (binding.checkCompactacion.isChecked)   ensayos.add("compactacion")
        if (binding.checkFlexotraccion.isChecked)  ensayos.add("flexotraccion")
        if (binding.checkHendimiento.isChecked)    ensayos.add("hendimiento")
        return ensayos.joinToString(",")
    }

    private fun setupConditionalVisibility() {
        // ── Hormigón "Otro" ────────────────────────────────────────────────
        binding.layoutOtraOpcion.visibility = View.GONE
        binding.spinnerTipoHormigon.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                binding.layoutOtraOpcion.visibility = if (pos == 5) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // ── Preparado en / material section ───────────────────────────────
        binding.layoutPremezclado.visibility = View.GONE
        binding.layoutPreparadoObra.visibility = View.GONE
        binding.spinnerPreparadoObra.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                binding.layoutPremezclado.visibility = if (pos == 1) View.VISIBLE else View.GONE
                binding.layoutPreparadoObra.visibility = if (pos == 2) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // ── Aditivo ────────────────────────────────────────────────────────
        binding.layoutAditivoObra.visibility = View.GONE
        binding.checkAditivo.setOnCheckedChangeListener { _, checked ->
            binding.layoutAditivoObra.visibility = if (checked) View.VISIBLE else View.GONE
        }

        // ── Ejes ───────────────────────────────────────────────────────────
        binding.layoutEjesXYZ.visibility = View.GONE
        binding.layoutEjesAB.visibility = View.GONE
        binding.spinnerElemento.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                val showEjes = pos >= 1 && pos <= 6
                binding.layoutEjesXYZ.visibility = if (showEjes) View.VISIBLE else View.GONE
                // Show A/B only for elements that need them: Losa(2), Muro(4), Cimiento desc 5
                val showAB = pos == 2 || pos == 4 || pos == 1
                binding.layoutEjesAB.visibility = if (showEjes && showAB) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // ── Vibrador ───────────────────────────────────────────────────────
        binding.layoutVibrador.visibility = View.GONE
        binding.spinnerCompactacionProbeta.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                binding.layoutVibrador.visibility = if (pos == 2 || pos == 3) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupSuspender() {
        binding.btnSuspender.setOnClickListener {
            val input = android.widget.EditText(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Suspender Muestra")
                .setMessage("Indique el motivo:")
                .setView(input)
                .setPositiveButton("Suspender") { _, _ ->
                    viewModel.suspenderMuestra(input.text.toString())
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // ─── Observar estado ──────────────────────────────────────────────────

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is TomaMuestraState.Saving -> binding.btnTomarMuestra.isEnabled = false
                        is TomaMuestraState.Saved  -> {
                            Snackbar.make(binding.root, "Muestra guardada", Snackbar.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                            viewModel.resetState()
                        }
                        is TomaMuestraState.Error  -> {
                            binding.btnTomarMuestra.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        else -> binding.btnTomarMuestra.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
