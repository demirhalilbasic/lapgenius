package com.example.techanalysisapp3.ui.activities;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.techanalysisapp3.ui.adapters.ComparisonSwipeAdapter;
import com.example.techanalysisapp3.R;

import java.util.ArrayList;

public class ComparisonSwipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comparison);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        ViewPager2 vp = findViewById(R.id.comparisonViewPager);
        ArrayList<String> leftImages  = getIntent().getStringArrayListExtra("left_images");
        ArrayList<String> rightImages = getIntent().getStringArrayListExtra("right_images");
        ArrayList<String> sections = getIntent().getStringArrayListExtra("ai_comparison_sections");

        ComparisonSwipeAdapter adapter = new ComparisonSwipeAdapter(
                leftImages, rightImages, sections
        );
        vp.setAdapter(adapter);
    }
}
