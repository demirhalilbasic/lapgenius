package com.example.techanalysisapp3.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.ShareProcessActivity;

public class ConfirmAnalysisFragment extends Fragment {
    private TextView tvCostSummary;
    private boolean isVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_analysis, container, false);
        tvCostSummary = view.findViewById(R.id.tvCostSummary);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        updateCostSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
    }

    private void updateCostSummary() {
        ShareProcessActivity activity = (ShareProcessActivity) getActivity();
        if (activity != null && isVisible) {
            boolean visualAnalysis = activity.visualAnalysisEnabled;
            int cost = visualAnalysis ? 2 : 1;

            String costText;
            if (activity.freeUsed < 3) {
                costText = "Ova analiza biti će u potpunosti BESPLATNA!";
            } else {
                costText = String.format("Ova analiza koštati će ukupno %d kredita", cost);
            }

            tvCostSummary.setText(costText);
        }
    }
}