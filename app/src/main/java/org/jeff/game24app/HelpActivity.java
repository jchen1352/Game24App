package org.jeff.game24app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Displays the help dialog. (For some reason using a regular dialog wasn't working)
 */
public class HelpActivity extends AppCompatActivity {

    public static final int NUM_PAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_help);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
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
                return inflater.inflate(R.layout.dialog_help_page1, container, false);
            }
            return null;
        }
    }
}
