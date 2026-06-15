package com.example.fmoraga.dictuc.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fmoraga.dictuc.R
import com.example.fmoraga.dictuc.databinding.ActivityMainBinding
import com.google.firebase.appdistribution.FirebaseAppDistribution
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado ignorado — el worker notificará si tiene permiso */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.listaMuestraFragment, R.id.listaRetiroFragment)
        )
        setupActionBarWithNavController(navController, appBarConfig)

        FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
