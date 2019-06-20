package com.example.sitiosjunio.activities


import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.mylibrarytoolbar.ToolbarActivity
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.adapters.PagerAdapter
import com.example.sitiosjunio.activities.fragments.FavoritosFragment
import com.example.sitiosjunio.activities.fragments.LugaresFragment
import com.example.sitiosjunio.activities.fragments.PerfilFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ToolbarActivity() {

    private var prevBottomSelected: MenuItem?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbarToLoad(toolbarView as Toolbar)

        setUpViewPager(getPagerAdapter())
        setUpBottonNavigationBar()

    }

    private fun getPagerAdapter(): PagerAdapter{
        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFragment(LugaresFragment())
        adapter.addFragment(FavoritosFragment())
        adapter.addFragment(PerfilFragment())
        return adapter
    }

    private fun setUpViewPager(adapter: PagerAdapter){
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = adapter.count
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(p0: Int) {
                if(prevBottomSelected == null)
                    bottomNavigation.menu.getItem(0).isChecked = false
                else
                    prevBottomSelected!!.isChecked = false
                bottomNavigation.menu.getItem(p0).isChecked = true
                prevBottomSelected = bottomNavigation.menu.getItem(p0)
            }

        })
    }

    private fun setUpBottonNavigationBar(){
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.bottomSitios -> {
                    viewPager.currentItem = 0; true
                }
                R.id.bottomFavoritos -> {
                    viewPager.currentItem = 1; true
                }
                R.id.bottomPerfil -> {
                    viewPager.currentItem = 2; true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_crear_sitio -> {
                goToActivity<AddSiteActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
