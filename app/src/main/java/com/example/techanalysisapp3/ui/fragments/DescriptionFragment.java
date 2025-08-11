package com.example.techanalysisapp3.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.MainActivity;

public class DescriptionFragment extends Fragment {

    private TextView descriptionText;
    private TextView titleText;
    private SwitchCompat langSwitch;
    private boolean isEnglish = false;

    public DescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_description, container, false);

        titleText = view.findViewById(R.id.tvDescriptionTitle);
        descriptionText = view.findViewById(R.id.tvDescriptionText);
        langSwitch = view.findViewById(R.id.switchLanguage);

        titleText.setTextColor(getResources().getColor(R.color.primary, requireActivity().getTheme()));

        updateText(isEnglish);

        langSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEnglish = isChecked;
            updateText(isEnglish);
        });

        return view;
    }

    private void updateText(boolean english) {
        if (english) {
            titleText.setText("Terms of Service & Disclaimer");
            descriptionText.setText("This application uses external artificial intelligence models to generate recommendations and analyses based on the provided technical specifications of laptop devices. All insights, suggestions, and evaluations are the result of automatically processed data via third-party language models, without direct human oversight or editorial control.\n\nIt is important to emphasize that the displayed recommendations are for informational purposes only and should not be interpreted as final, professional, technical, or financial advice. The app does not guarantee the accuracy, completeness, or timeliness of the generated content. While every effort is made to rely on reliable sources and technically valid input, each output depends solely on the available data and the behavior of the AI model, over which the app creators have no direct influence.\n\nLapGenius assumes no liability for any technical, financial, or other consequences that users may experience by relying on the provided information. The final decision regarding purchasing, replacing, or assessing the value of a device rests entirely with the user. By using the application, the user agrees to these terms and acknowledges that the tool is intended solely as an assistant—not a replacement for professional expertise or independent technical evaluation.\n\nFurthermore, LapGenius does not guarantee the market value, long-term reliability, or future performance of any evaluated device. All insights are automatically generated and do not necessarily reflect the views, opinions, or endorsements of the development team behind the application.");
        } else {
            titleText.setText("Uslovi korištenja & Odricanje od odgovornosti");
            descriptionText.setText("Ova aplikacija koristi vanjske modele vještačke inteligencije za generisanje preporuka i analiza na osnovu tehničkih specifikacija unesenih laptop uređaja. Svi uvidi, sugestije i ocjene rezultat su automatski obrađenih podataka putem modela trećih strana, bez direktnog ljudskog nadzora ili uređivanja sadržaja.\n\nVažno je naglasiti da prikazane preporuke služe isključivo u informativne svrhe i ne trebaju se tumačiti kao konačan, profesionalan, tehnički ili finansijski savjet. Aplikacija ne garantuje tačnost, potpunost niti ažurnost generisanog sadržaja. Iako se nastoji oslanjati na pouzdane izvore i tehnički validne ulazne podatke, svaki rezultat zavisi isključivo od dostupnih informacija i ponašanja AI modela, na koje autori aplikacije nemaju direktan uticaj.\n\nLapGenius ne preuzima odgovornost za bilo kakve tehničke, finansijske ili druge posljedice koje korisnici mogu iskusiti oslanjajući se na prikazane informacije. Konačna odluka o kupovini, zamjeni ili procjeni vrijednosti uređaja ostaje u potpunosti na korisniku. Korištenjem aplikacije, korisnik prihvata ove uslove i potvrđuje da je alat namijenjen isključivo kao pomoć, a ne kao zamjena za stručno mišljenje ili nezavisnu tehničku evaluaciju.\n\nTakođer, LapGenius ne garantuje tržišnu vrijednost, dugoročnu pouzdanost niti buduće performanse bilo kojeg ocijenjenog uređaja. Svi prikazani podaci su automatski generisani i ne odražavaju nužno stavove, mišljenja niti preporuke razvojnog tima aplikacije.");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((MainActivity) requireActivity()).showBottomNavigation();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }
}