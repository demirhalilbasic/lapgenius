package com.example.techanalysisapp3.model;

import com.example.techanalysisapp3.util.Additional;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Listing implements Serializable {
    public String title;
    public String display_price;
    public String state;

    @SerializedName("additional")
    public Additional additional;

    @SerializedName("images")
    public List<String> images;

    @SerializedName("attributes")
    public List<Attribute> attributes;

    @SerializedName("category_id")
    public int category_id;

    @SerializedName("user")
    public User user;

    // User and Locations classes
    public static class User {
        @SerializedName("location")
        public Location location;
    }

    public static class Location {
        @SerializedName("name")
        public String name;

        @SerializedName("location")
        public Coordinates coordinates;
    }

    public static class Coordinates {
        @SerializedName("lat")
        public String latitude;

        @SerializedName("lon")
        public String longitude;
    }

    // Fulfilled fields for review
    public String displaySize;
    public String resolution;
    public String operatingSystem;
    public String processorBrand;
    public String processorModel;
    public Double processorSpeedGHz;
    public Integer processorCores;
    public String ramSize;
    public Integer ssdCapacityGB;
    public Integer hddCapacityGB;
    public String cdRom;
    public Integer usbPortCount;
    public String gpuType;
    public String gpuModel;
    public String gpuVendor;
    public String batteryDuration;
    public Double weightKg;
    public String warrantyMonths;
    public String productionYear;

    // Additional
    public Boolean bluetoothEnabled;
    public Boolean cardReader;
    public Boolean fingerprintSensor;
    public Boolean hasHdmi;
    public Boolean hasMicrophone;
    public Boolean keyboardLocal;
    public Boolean hasTouchscreen;
    public Boolean hasWebcam;
    public Boolean hasWireless;

    /**
     * Call after deserialization to fulfill specific fields
     * */
    public void mapAttributes() {
        if (attributes == null) return;
        for (Attribute attr : attributes) {
            String n = attr.name;
            String v = attr.value;

            try {
                switch (n) {
                    case "Display (incha)": displaySize = v + "\""; break;
                    case "Rezolucija": resolution = v; break;
                    case "Operativni sistem": operatingSystem = v; break;
                    case "Procesor": processorBrand = v; break;
                    case "Model procesora": processorModel = v; break;
                    case "Brzina procesora (GHz)": processorSpeedGHz = Double.parseDouble(v); break;
                    case "Broj fizičkih jezgri": processorCores = Integer.parseInt(v); break;
                    case "RAM": ramSize = v; break;
                    case "SSD kapacitet (GB)": ssdCapacityGB = Integer.parseInt(v); break;
                    case "HDD - Hard disk (GB)": hddCapacityGB = Integer.parseInt(v); break;
                    case "CD - ROM": cdRom = v; break;
                    case "Broj USB portova": usbPortCount = Integer.parseInt(v); break;
                    case "Vrsta grafičke": gpuType = v; break;
                    case "Grafička karta (model)": gpuModel = v; break;
                    case "Proizvođač graf. kartice": gpuVendor = v; break;
                    case "Baterija (trajanje)": batteryDuration = v; break;
                    case "Masa (kg)": weightKg = Double.parseDouble(v); break;
                    case "Garancija (mjeseci)": warrantyMonths = v; break;
                    case "Godina proizvodnje": productionYear = v; break;
                    case "Bluetooth": bluetoothEnabled = Boolean.parseBoolean(v); break;
                    case "Card reader (čitač kartica)": cardReader = Boolean.parseBoolean(v); break;
                    case "Fingerprint": fingerprintSensor = Boolean.parseBoolean(v); break;
                    case "HDMI": hasHdmi = Boolean.parseBoolean(v); break;
                    case "Mikrofon": hasMicrophone = Boolean.parseBoolean(v); break;
                    case "Tastatura (naša slova)": keyboardLocal = Boolean.parseBoolean(v); break;
                    case "Touchscreen": hasTouchscreen = Boolean.parseBoolean(v); break;
                    case "Webcam": hasWebcam = Boolean.parseBoolean(v); break;
                    case "Wireless": hasWireless = Boolean.parseBoolean(v); break;
                    default: break;
                }
            } catch (NumberFormatException e) {
                // discard useless fields
            }
        }
    }
}