package com.helwigdev.helpdesk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.crash.FirebaseCrash;

public class TabActivity extends AppCompatActivity {

    //set up method variables

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    ProgressDialog pd;
    SharedPreferences preferences;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up views
        setContentView(R.layout.activity_tab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //init ads
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5637328886369714~1187638383");
        mAdView = (AdView) findViewById(R.id.av_tickets_bottom);
        //if ads have not been removed
        if (!preferences.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("E498D046420E068963DD7607B804BA3D")
                    .build();

            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //email button (new ticket?)
        //disabled for the moment
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        pd = new ProgressDialog(this);


    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //re-set up preferences if the object has been lost
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        //remove ads if necessary
        if (preferences.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            mAdView.setVisibility(View.GONE);
        }

        //reload tickets when coming back to activity
        Boolean toRefresh = preferences.getBoolean("key_pref_bool_tick_auto_refresh", true);
        if (toRefresh) {
            refreshAndNotify();
        }

    }

    public boolean myTicketsRefreshed = true;
    public boolean groupTicketsRefreshed = true;

    public void dismissPd() {
        //only dismiss loading screen if both views have successfully refreshed
        if (myTicketsRefreshed && groupTicketsRefreshed) {
            try {
                pd.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshAndNotify() {
        //set up refresh framework
        myTicketsRefreshed = false;
        groupTicketsRefreshed = false;
        pd.setMessage(getResources().getString(R.string.loading));
        pd.show();
        //continue refresh cascade
        mSectionsPagerAdapter.refreshFragments();
        //Toast.makeText(getApplicationContext(),"Refreshing tickets...",Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ticketlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            //handle menu item clicks
            case R.id.menu_feedback:
                //send email
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:helwigdev@gmail.com?subject=Feedback for Web Help Desk app");
                intent.setData(data);
                startActivity(intent);
                return true;
            case R.id.menu_tab_refresh:
                //begin refresh cascade
                refreshAndNotify();
                return true;
            case R.id.action_settings:
                //it's pretty self explanatory
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //left in place until search fragment can be implemented
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        //controls tabs
        TicketListFragment myList;
        TicketGroupListFragment groupList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            myList = new TicketListFragment();
            groupList = new TicketGroupListFragment();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return myList;
            } else if (position == 1) {
                return groupList;
            }

            return PlaceholderFragment.newInstance(position + 1);
        }

        private void refreshFragments() {
            //continue refresh cascade
            myList.refresh();
            groupList.refresh();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.my_tickets);
                case 1:
                    return getResources().getString(R.string.group_tickets);
                case 2:
                    return getResources().getString(R.string.search);

            }
            return null;
        }
    }
}
