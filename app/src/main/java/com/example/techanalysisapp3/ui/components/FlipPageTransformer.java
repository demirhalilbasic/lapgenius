package com.example.techanalysisapp3.ui.components;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FlipPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        page.setCameraDistance(20000);

        if (position < -1 || position > 1) {
            page.setAlpha(0f);
        } else {
            page.setAlpha(1f);
            page.setRotationY(-180f * position);
        }
    }
}
