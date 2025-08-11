package com.example.techanalysisapp3.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.ShareProcessActivity;
import com.google.android.material.checkbox.MaterialCheckBox;

public class VisualAnalysisFragment extends Fragment {
    private MaterialCheckBox checkboxVisual;
    private TextView tvCost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visual_analysis, container, false);

        checkboxVisual = view.findViewById(R.id.checkboxVisual);
        tvCost = view.findViewById(R.id.tvCost);

        checkboxVisual.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCostDisplay();
            ((ShareProcessActivity) requireActivity()).setVisualAnalysisEnabled(isChecked);
        });

        return view;
    }

    private void updateCostDisplay() {
        int cost = checkboxVisual.isChecked() ? 2 : 1;
        tvCost.setText("Ukupna cijena: " + cost + " kredita");
    }

    public boolean isVisualAnalysisEnabled() {
        return checkboxVisual != null && checkboxVisual.isChecked();
    }
}