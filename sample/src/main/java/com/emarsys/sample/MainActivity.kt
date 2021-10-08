package com.emarsys.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.emarsys.Emarsys
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.sample.databinding.ActivityMainBinding
import com.emarsys.sample.extensions.showSnackBar
import com.emarsys.sample.fragments.MobileEngageFragmentTracking
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject


class MainActivity : FragmentActivity(), EventHandler {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        setupBottomNavMenu(navHostFragment.navController)
        val view = binding.root

        setContentView(view)
    }

    fun checkLocationPermission(): Boolean {
        return if (
             ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission needed")
                .setMessage("Emarsys SDK collects location data to enable Geofencing feature even when the app is closed or not in use.")
                .setPositiveButton("Ok") { dialog, b ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        MobileEngageFragmentTracking.REQUEST_LOCATION_PERMISSIONS
                    )
                }.show()
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MobileEngageFragmentTracking.REQUEST_LOCATION_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Emarsys.geofence.enable {
                            binding.navHostFragment.showSnackBar("Geofence has been Enabled!")
                        }
                    }
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)!!
        fragment.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupBottomNavMenu(navController: NavController) {
        binding.bottomNavigationView.let {
            NavigationUI.setupWithNavController(it, navController)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navigated = NavigationUI.onNavDestinationSelected(item, navController)
        return navigated || super.onOptionsItemSelected(item)
    }

    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
    }

}
