package com.example.techanalysisapp3.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.ShareProcessActivity;
import com.google.android.material.button.MaterialButton;

public class PrimaryPurposeFragment extends Fragment {
    private int selectedPosition = -1;
    private PurposeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purpose_selection, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        String[] purposes = getResources().getStringArray(R.array.purpose_options);
        adapter = new PurposeAdapter(purposes, position -> {
            selectedPosition = position;
            ((ShareProcessActivity) requireActivity()).goToNext();
        });

        adapter = new PurposeAdapter(purposes, position -> {
            selectedPosition = position;
            String purpose = getSelectedPurpose();
            ((ShareProcessActivity) requireActivity()).setSelectedPurpose(purpose);
            ((ShareProcessActivity) requireActivity()).goToNext();
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    public String getSelectedPurpose() {
        return (selectedPosition != -1) ?
                getResources().getStringArray(R.array.purpose_options)[selectedPosition] : "🧰 Svakodnevna upotreba";
    }

    private static class PurposeAdapter extends RecyclerView.Adapter<PurposeAdapter.ViewHolder> {
        private final String[] items;
        private final OnItemClickListener listener;

        public PurposeAdapter(String[] items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_purpose, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items[position], listener);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialButton button;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.btnPurpose);
            }

            public void bind(String text, OnItemClickListener listener) {
                button.setText(text);
                button.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            }
        }

        interface OnItemClickListener {
            void onItemClick(int position);
        }
    }
}