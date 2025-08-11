package com.example.techanalysisapp3.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.Listing;

public class ConfirmListingFragment extends Fragment {
    private Listing listing;

    public static ConfirmListingFragment newInstance(Listing listing) {
        ConfirmListingFragment fragment = new ConfirmListingFragment();
        Bundle args = new Bundle();
        args.putSerializable("listing", listing);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_listing, container, false);

        listing = (Listing) getArguments().getSerializable("listing");
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (listing.images != null && !listing.images.isEmpty()) {
            Glide.with(this).load(listing.images.get(0)).into(imageView);
        }

        tvTitle.setText(listing.title);
        btnCancel.setOnClickListener(v -> requireActivity().finish());

        return view;
    }
}