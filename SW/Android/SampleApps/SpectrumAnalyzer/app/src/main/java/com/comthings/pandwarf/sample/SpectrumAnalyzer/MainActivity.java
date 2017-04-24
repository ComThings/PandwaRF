package com.comthings.pandwarf.sample.SpectrumAnalyzer;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.pandwarf.sample.SpectrumAnalyzer.fragment.ScanDevicesFragment;
import com.comthings.pandwarf.sample.SpectrumAnalyzer.fragment.SpecAnalyzerFragment;
import com.comthings.pandwarf.specAn.R;
import com.sdsmdg.tastytoast.TastyToast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new ScanDevicesFragment()).addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            // Handle the camera action
            fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new ScanDevicesFragment()).addToBackStack(null);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_specAn) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new SpecAnalyzerFragment()).addToBackStack(null);
            fragmentTransaction.commit();
        } else if (id == R.id.disconnect) {
            GollumDongle.getInstance(this).closeDevice();
            TastyToast.makeText(this, "disconected", TastyToast.LENGTH_SHORT, TastyToast.INFO);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
