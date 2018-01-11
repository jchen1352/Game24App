package org.jeff.game24app.game;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.jeff.game24app.BaseApplication;
import org.jeff.game24app.R;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;

/**
 * The concrete activity that handles free play mode.
 * Features of free play mode: Automatic puzzle saving and loading, watch ad for hints.
 */
public class FreePlayGameActivity extends HintGameActivity implements RewardedVideoAdListener {

    private RewardedVideoAd ad;

    /**
     * Shared preference key for saved classic puzzle
     */
    private static final String CLASSIC_PREF = "puzzle_classic_pref";
    /**
     * Shared preference key for saved fractional puzzle
     */
    private static final String FRAC_PREF = "puzzle_frac_pref";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.score).setVisibility(View.GONE);
        findViewById(R.id.time).setVisibility(View.GONE);

        //Get saved puzzle if one exists
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        int puzzleEncoded = preferences.getInt(fracMode ? FRAC_PREF : CLASSIC_PREF, 0);
        if (puzzleEncoded != 0) {
            nextPuzzle = Game24Generator.reverseHash(puzzleEncoded);
        } else {
            nextPuzzle = getNewPuzzle();
        }

        ad = MobileAds.getRewardedVideoAdInstance(this);
        ad.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    @Override
    protected void onStop() {
        super.onStop();
        int puzzleEncoded = Game24Generator.hashToInt(nextPuzzle);
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(fracMode ? FRAC_PREF : CLASSIC_PREF, puzzleEncoded);
        editor.apply();
    }

    /**
     * Get saved puzzle if it exists.
     *
     * @return The saved puzzle, or a randomly generated one if none exists.
     */
    @Override
    public Rational[] getInitialPuzzle() {
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        int puzzleEncoded = preferences.getInt(fracMode ? FRAC_PREF : CLASSIC_PREF, 0);
        if (puzzleEncoded != 0) {
            return Game24Generator.reverseHash(puzzleEncoded);
        }
        return getNewPuzzle();
    }

    @Override
    protected void onHintClicked() {
        Log.d("FreePlayGameActivity", "hintClicked");
        showHintDialog();
    }

    @Override
    protected void onMoreHintsClicked() {
        if (ad.isLoaded()) {
            ad.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ad.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ad.resume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //There is currently a bug where ad.destroy makes it not work when activity restarts
        //ad.destroy(this);
    }

    private void loadRewardedVideoAd() {
        if (!ad.isLoaded()) {
            ad.loadAd(BaseApplication.AD_UNIT_ID, new AdRequest.Builder().build());
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d("GameActivity", "ad loaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d("GameActivity", "ad opened");
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d("GameActivity", "ad started");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d("GameActivity", "ad closed");
        //preload next ad
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        numHints++;
        onNumHintsChanged();
        Log.d("GameActivity", "ad rewarded");
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d("GameActivity", "ad left app");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d("GameActivity", "ad failed to load");
    }
}
