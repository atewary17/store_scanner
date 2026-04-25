package com.storescanner

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.storescanner.data.api.ApiClient
import com.storescanner.databinding.ActivityMainBinding
import com.storescanner.viewmodel.ScannerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ApiClient.init(viewModel.tokenManager)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        viewModel.isLoggedIn.observe(this) { loggedIn ->
            val graph = navController.navInflater.inflate(R.navigation.nav_graph)
            graph.setStartDestination(if (loggedIn) R.id.dashboardFragment else R.id.loginFragment)
            navController.graph = graph
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
