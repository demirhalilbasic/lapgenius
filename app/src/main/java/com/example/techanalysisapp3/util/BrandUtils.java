package com.example.techanalysisapp3.util;

import com.example.techanalysisapp3.R;

public class BrandUtils {
    public static int getBrandLogoResource(String brandKey) {
        switch (brandKey) {
            case "acer": return R.drawable.acer;
            case "acer_predator": return R.drawable.acer_predator;
            case "acer_nitro": return R.drawable.acer_nitro;
            case "apple": return R.drawable.apple;
            case "asus": return R.drawable.asus;
            case "asus_rog": return R.drawable.asus_rog;
            case "asus_tuf_gaming": return R.drawable.asus_tuf_gaming;
            case "asus_zenbook": return R.drawable.asus_zenbook;
            case "dell": return R.drawable.dell;
            case "dell_xps": return R.drawable.dell_xps;
            case "dell_alienware": return R.drawable.dell_alienware;
            case "gigabyte": return R.drawable.gigabyte;
            case "gigabyte_aero": return R.drawable.gigabyte_aero;
            case "gigabyte_aorus": return R.drawable.gigabyte_aorus;
            case "hp": return R.drawable.hp;
            case "hp_omen": return R.drawable.hp_omen;
            case "hp_victus": return R.drawable.hp_victus;
            case "lenovo": return R.drawable.lenovo;
            case "lenovo_thinkpad": return R.drawable.lenovo_thinkpad;
            case "lenovo_yoga": return R.drawable.lenovo_yoga;
            case "lenovo_legion": return R.drawable.lenovo_legion;
            case "lg_gram": return R.drawable.lg_gram;
            case "microsoft_surface": return R.drawable.microsoft_surface;
            case "msi": return R.drawable.msi;
            case "msi_gaming": return R.drawable.msi_gaming;
            case "razer": return R.drawable.razer;
            case "samsung_galaxy_book": return R.drawable.samsung_galaxy_book;
            case "xmg": return R.drawable.xmg;
            default: return R.drawable.ic_laptop_placeholder;
        }
    }
}