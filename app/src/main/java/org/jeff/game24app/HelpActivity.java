package org.jeff.game24app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Displays the help dialog. (For some reason using a regular dialog wasn't working)
 */
public class HelpActivity extends AppCompatActivity {

    public static final int NUM_PAGES = 3;

    protected static int f0, f1, t0, t1, m0, m1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_help);
        ViewPager pager = findViewById(R.id.pager);
        PagerAdapter adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tab_dots);
        tabLayout.setupWithViewPager(pager, true);

        SharedPreferences prefs = getSharedPreferences(BaseActivity.PREFS, 0);
        f0 = prefs.getInt(BaseActivity.CLASSIC_SOLVED_PREF, 0);
        f1 = prefs.getInt(BaseActivity.FRAC_SOLVED_PREF, 0);
        t0 = prefs.getInt(BaseActivity.CLASSIC_SCORE_PREF, 0);
        t1 = prefs.getInt(BaseActivity.FRAC_SCORE_PREF, 0);
        m0 = prefs.getInt(BaseActivity.NUM_WON_PREF, 0);
        m1 = prefs.getInt(BaseActivity.NUM_PLAYED_PREF, 0);
    }

    public static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return HelpFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static class HelpFragment extends Fragment {

        public static HelpFragment newInstance(int page) {
            HelpFragment f = new HelpFragment();
            Bundle args = new Bundle();
            args.putInt("page", page);
            f.setArguments(args);
            return f;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            int page = getArguments() != null ? getArguments().getInt("page") : 0;
            if (page == 0) {
                return inflater.inflate(R.layout.dialog_help_page0, container, false);
            }
            if (page == 1) {
                View v = inflater.inflate(R.layout.dialog_help_page1, container, false);
                TextView tv = v.findViewById(R.id.free_play_stats);
                tv.setText(getString(R.string.stats_free_play, f0, f1));
                tv = v.findViewById(R.id.timed_play_stats);
                tv.setText(getString(R.string.stats_timed_play, t0, t1));
                tv = v.findViewById(R.id.multiplayer_stats);
                tv.setText(getString(R.string.stats_multiplayer, m0, m1));
                return v;
            }
            if (page == 2) {
                View v = inflater.inflate(R.layout.dialog_help_page2, container, false);
                TextView tv = v.findViewById(R.id.version);
                tv.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
                return v;
            }
            return null;
        }
    }
}
