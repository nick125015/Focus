package com.example.focus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class MyMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mymenu)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        auth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WeatherFragment())
                .commit()
            navigationView.setCheckedItem(R.id.nav_home)
            currentFragment = WeatherFragment()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_home -> WeatherFragment()
            R.id.nav_timerfragment -> {
                if (currentFragment is TimerFragment) {
                    // Do nothing if already in TimerFragment
                    return true
                } else {
                    TimerFragment()
                }
            }
            R.id.nav_recordsfragment -> RecordsFragment()
            R.id.nav_calendarfragment -> CalendarFragment()
            R.id.nav_about -> AboutFragment()
            R.id.nav_dailyfragment -> DailyFragment()
            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
                return true
            }
            else -> return false
        }

        if (currentFragment is TimerFragment && selectedFragment != currentFragment) {
            showLeaveConfirmationDialog(selectedFragment)
        } else {
            switchToFragment(selectedFragment)
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("確定要登出嗎？")
        alertDialogBuilder.setPositiveButton("确定") { dialog: DialogInterface, _: Int ->
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("取消") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showLeaveConfirmationDialog(selectedFragment: Fragment) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("確定要離開計時器嗎？")
        alertDialogBuilder.setPositiveButton("确定") { dialog: DialogInterface, _: Int ->
            switchToFragment(selectedFragment)
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("取消") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun switchToFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}