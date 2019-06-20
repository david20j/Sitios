package com.example.sitiosjunio.activities.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragmentlist = ArrayList<Fragment>()

    override fun getItem(p0: Int): Fragment =  fragmentlist.get(p0)

    override fun getCount(): Int =  fragmentlist.size

    fun addFragment(fragment: Fragment) = fragmentlist.add(fragment)

}