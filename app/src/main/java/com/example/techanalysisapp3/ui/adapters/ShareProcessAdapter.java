package com.example.techanalysisapp3.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.techanalysisapp3.model.Listing;
import com.example.techanalysisapp3.ui.fragments.ConfirmAnalysisFragment;
import com.example.techanalysisapp3.ui.fragments.ConfirmListingFragment;
import com.example.techanalysisapp3.ui.fragments.GameSelectionFragment;
import com.example.techanalysisapp3.ui.fragments.PrimaryPurposeFragment;
import com.example.techanalysisapp3.ui.fragments.SecondaryFocusFragment;
import com.example.techanalysisapp3.ui.fragments.VisualAnalysisFragment;

public class ShareProcessAdapter extends FragmentStateAdapter {
    private final Listing listing;
    private final String[] titles = {
            "Potvrdi oglas",
            "Primarna namjena",
            "Sekundarni fokus",
            "Odabir igre",
            "Vizualna analiza",
            "Summary"
    };

    public ShareProcessAdapter(FragmentActivity fa, Listing listing) {
        super(fa);
        this.listing = listing;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return ConfirmListingFragment.newInstance(listing);
            case 1: return new PrimaryPurposeFragment();
            case 2: return new SecondaryFocusFragment();
            case 3: return new GameSelectionFragment();
            case 4: return new VisualAnalysisFragment();
            case 5: return new ConfirmAnalysisFragment();
            default: return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public String getPageTitle(int position) {
        return titles[position];
    }
}