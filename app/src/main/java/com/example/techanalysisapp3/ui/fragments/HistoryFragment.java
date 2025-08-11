package com.example.techanalysisapp3.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.MainActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class HistoryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        Toolbar toolbar = view.findViewById(R.id.historyToolbar);
        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapseToolbar);
        ImageView imageView = view.findViewById(R.id.ivHistoryFull);
        TextView textView = view.findViewById(R.id.tvHistoryFullText);
        ImageButton githubButton = view.findViewById(R.id.btnGithub);
        ImageButton linkedInButton = view.findViewById(R.id.btnLinkedIn);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setTint(ContextCompat.getColor(requireContext(), R.color.white));
        toolbar.setNavigationContentDescription("Nazad");
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity) requireActivity()).showBottomNavigation();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        imageView.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Želite li posjetiti LapGenius stranicu?")
                .setMessage("Bićete preusmjereni na zvaničnu stranicu IPI Akademije.")
                .setPositiveButton("Da", (dialog, which) ->
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://ipi-akademija.ba/app"))))
                .setNegativeButton("Ne", null)
                .show());

        String storyText =
                "Sve je počelo mirnog popodneva, 22. aprila 2025. godine, kada je objavljen poziv za drugo IPIA App takmičenje. " +
                        "Jedan student treće godine, uz malo entuzijazma i mnogo kafe, odlučio je da napravi nešto korisno.\n\n" +

                        "Tako je rođen LapGenius – pomoćnik za svakoga ko traži polovan laptop, a nije siguran koji odabrati. " +
                        "Dovoljno je unijeti link sa OLX.ba i odabrati svrhu korištenja: škola, posao, gaming ili multimedija. " +
                        "Aplikacija zatim generiše kratak, razumljiv AI odgovor: isplati li se uređaj ili ne.\n\n" +

                        "U pozadini, LapGenius koristi OLX API da preuzme tehničke specifikacije, a zatim ih analizira uz pomoć AI modela. " +
                        "Razvijen je u Android Studio koristeći MVVM arhitekturu, Javu i Firebase servise.\n\n" +

                        "Od ideje do verzije 1.4.1, aplikacija je prošla više faza razvoja, uz 66 commitova. " +
                        "Danas je spremna da pomogne svima koji žele pametniju odluku bez tehničkih komplikacija.\n\n" +

                        "LapGenius je više od studentskog projekta. To je mali dokaz da dobra ideja, uz trud i volju, može postati alat koji zaista pomaže ljudima.";


        SpannableString styledStory = new SpannableString(storyText);

        textView.setText(styledStory);

        githubButton.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse("https://github.com/demirhalilbasic"))));

        linkedInButton.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/demir-halilbasic/"))));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Restore the bottom nav & pop back stack
                        ((MainActivity) requireActivity()).showBottomNavigation();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }
}
