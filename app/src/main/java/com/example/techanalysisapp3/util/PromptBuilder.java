package com.example.techanalysisapp3.util;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.techanalysisapp3.model.Listing;

import java.util.Calendar;

public class PromptBuilder {
    private static final String[] mjeseci = {"januar", "februar", "mart", "april", "maj", "juni",
            "juli", "avgust", "septembar", "oktobar", "novembar", "decembar"};

    public static String buildPrompt(
            Listing listing,
            String cleanedDescription,
            String purpose,
            String secondary,
            String visualAnalysisEN,
            boolean visualAnalysisEnabled,
            int selectedGameId,
            String selectedGameName,
            String selectedMinReq,
            String selectedRecReq,
            String gender,
            String firstName,
            String favoriteBrand
    ) {
        // Ensure attributes are mapped
        listing.mapAttributes();

        // Build current date string
        Calendar calendar = Calendar.getInstance();
        int mjesec = calendar.get(Calendar.MONTH);
        int godina = calendar.get(Calendar.YEAR);
        String datumString = mjeseci[mjesec] + " " + godina + ". godine";

        StringBuilder inputBuilder = new StringBuilder();

        // PERSONALIZED INTRODUCTION
        if (gender != null && firstName != null) {
            StringBuilder introBuilder = new StringBuilder("Ja sam ").append(firstName)
                    .append(". Potrebna mi je tvoja stručna analiza pri kupovini laptopa.");

            if (favoriteBrand != null && !"default".equals(favoriteBrand)) {
                introBuilder.append(" Moj omiljeni brend laptopa je ").append(favoriteBrand).append(".");
            }

            if ("female".equals(gender)) {
                introBuilder.append(" Obraćaj mi se u ženskom rodu.");
            } else {
                introBuilder.append(" Obraćaj mi se u muškom rodu.");
            }

            inputBuilder.append(introBuilder).append("\n\n");
        }

        // INSTRUCTIONS
        inputBuilder.append("Ti si stručnjak za analizu laptopa sa 5+ godina iskustva. Analizu vrši u sljedećem redoslijedu:\n")
                .append("1. Detaljno pročitaj cijeli opis oglasa\n")
                .append("2. Izvuci sve tehničke specifikacije iz opisa\n")
                .append("3. Obraćaj se meni u prvom licu prilikom odgovora, potencijalni sam kupac\n\n");

        // DESCRIPTION
        inputBuilder.append("📝 Cjelokupni opis oglasa:\n")
                .append(cleanedDescription).append("\n\n");

        // STRUCTURED DATA
        inputBuilder.append("📊 Dostupni podaci u navedenom oglasu:\n")
                .append("• Naslov: ").append(listing.title).append("\n")
                .append("• Cijena: ").append(listing.display_price).append(" (BAM)\n")
                .append("• Stanje: ").append(listing.state != null ? listing.state : "Nepoznato").append("\n")
                .append("• Ekran: ").append(listing.displaySize != null ? listing.displaySize : "Nepoznato")
                .append(listing.resolution != null ? " (" + listing.resolution + ")" : "").append("\n")
                .append("• OS: ").append(listing.operatingSystem != null ? listing.operatingSystem : "Nepoznato").append("\n")
                .append("• Procesor: ")
                .append(listing.processorBrand != null ? listing.processorBrand : "Nepoznato")
                .append(listing.processorModel != null ? " " + listing.processorModel : "")
                .append(listing.processorSpeedGHz != null ? " @ " + listing.processorSpeedGHz + "GHz" : "")
                .append(listing.processorCores != null ? ", " + listing.processorCores + " jezgra" : "").append("\n")
                .append("• RAM: ").append(listing.ramSize != null ? listing.ramSize : "Nepoznato").append("\n")
                .append("• SSD: ").append(listing.ssdCapacityGB != null ? listing.ssdCapacityGB + "GB" : "Nepoznato").append("\n")
                .append("• HDD: ").append(listing.hddCapacityGB != null ? listing.hddCapacityGB + "GB" : "Nepoznato").append("\n")
                .append("• Grafika: ")
                .append(listing.gpuType != null ? listing.gpuType : "Nepoznato")
                .append(listing.gpuModel != null ? " (" + listing.gpuModel + ")" : "")
                .append(listing.gpuVendor != null ? " od " + listing.gpuVendor : "").append("\n")
                .append("• Baterija: ").append(listing.batteryDuration != null ? listing.batteryDuration : "Nepoznato").append("\n")
                .append("• Težina: ").append(listing.weightKg != null ? listing.weightKg + "kg" : "Nepoznato").append("\n")
                .append("• USB portovi: ").append(listing.usbPortCount != null ? listing.usbPortCount : "Nepoznato").append("\n")
                .append("• CD-ROM: ").append(listing.cdRom != null ? listing.cdRom : "Nepoznato").append("\n")
                .append("• Garancija: ").append(listing.warrantyMonths != null ? listing.warrantyMonths + " mjeseci" : "Nepoznato").append("\n")
                .append("• Godina proizvodnje: ").append(listing.productionYear != null ? listing.productionYear : "Nepoznato").append("\n\n");

        if (listing.state != null && listing.state.equalsIgnoreCase("new")) {
            inputBuilder.append("🔋 Napomena o bateriji: Laptop je označen kao NOV, što garantuje da je baterija u potpunosti ispravna i ima 100% kapaciteta\n\n");
        }

        if (visualAnalysisEnabled && visualAnalysisEN != null && !visualAnalysisEN.isEmpty()) {
            inputBuilder.append("📸 RAW VISUAL ANALYSIS:\n")
                    .append(visualAnalysisEN)
                    .append("\n\n")
                    .append("🔍 INSTRUCTIONS FOR VISUAL ANALYSIS:\n")
                    .append("1. Prevedi analizu na bosanski jezik\n")
                    .append("2. Sažmi ključne informacije\n")
                    .append("3. Integriši sa tehničkim specifikacijama\n")
                    .append("4. UZMI SA DOZOM REZERVE: Vizuelni prikaz nije uvijek 100% tačan\n")
                    .append("5. Integriši unutar '👀 Vizuelna Analiza' sekcije\n\n");
        }

        // USER REQUIREMENTS
        inputBuilder.append("🎯 Zahtjevi:\n")
                .append("• Primarna namjena laptopa: ").append(purpose).append("\n")
                .append("• Sekundarni fokus za koji ga koristim: ").append(secondary).append("\n\n");

        if (selectedGameId != -1) {
            inputBuilder.append("• Odabrana igra: ").append(selectedGameName).append("\n");
            inputBuilder.append("• Sistemski zahtjevi za igru (min/rec): ").append(selectedMinReq).append(" / ").append(selectedRecReq).append("\n\n");
        }

        // ANALYSIS INSTRUCTIONS
        inputBuilder.append("🔍 Obavezno analiziraj:\n")
                .append("1. Kompatibilnost specifikacija sa namjenom (").append(purpose).append(")\n")
                .append("2. Performanse ključnih komponenti (CPU/GPU/RAM)\n")
                .append("3. Identificiraj sve nedostatke u specifikacijama\n")
                .append(listing.state != null && listing.state.equalsIgnoreCase("new")
                        ? "4. Posebno naglasi prednosti novog uređaja\n"
                        : "4. Preporuči optimizacije/alternative ako postoji nesklad\n")
                .append("\n");

        // RESPONSE FORMAT
        inputBuilder.append("⚠️ NAPOMENE:\n")
                .append("1. Koristi emoji + naslov za svaku cjelinu\n")
                .append("2. Biti što detaljniji, ako nešto nije navedeno u oglasu pretpostaviti, istražiti\n")
                .append("3. Napraviti adekvatnu procjenu, ako previše detalja fali ili je uređaj star, dati znatno lošiju ocjenu\n")
                .append("4. Odgovor piši na bosanskom jeziku\n")
                .append("5. Koristi komparativne primjere sa sličnim modelima\n")
                .append("6. Kombiniraj tehničke specifikacije i praktične primjene\n")
                .append("7. Ne služiti se '**' bold i drugim elementima (samo plain text)\n")
                .append("8. Ukoliko se pozivaš na vremenski okvir, trenutno je ").append(datumString).append("\n")
                .append("9. Ukupnu ocjenu bazirati na: Performanse (40% težine), Vrijednost za novac (30%), Buduća proof-of-concept (20%), Garancija i podrška (10%)\n")
                .append("10. Vrijednost 'X' u sekciji 'Ukupna ocjena' zamijeni sa cijelim (npr. '8') ili decimalnim (npr. '8.2') brojem\n\n")
                .append("📝 STROGO KORISTI FORMAT ODGOVORA ISPOD:\n")
                .append("[emoji] [naslov]: [tekst u pasusu]\n\n")
                .append("🔍 Kompatibilnost s namjenom: [Analiza]\n")
                .append("💪 Performanse: [Detalji o CPU/GPU/RAM]\n");

        if (visualAnalysisEnabled) {
            inputBuilder.append("👀 Vizuelna analiza: [Stanje laptopa]\n");
        }

        inputBuilder.append("💾 Pohrana i brzina: [SSD/HDD analiza]\n")
                .append("💻 Ekran i korisničko iskustvo: [Veličina, rezolucija]\n")
                .append("🔋 Baterija i mobilnost: [Trajanje baterije, težina]\n")
                .append("🔌 Konektivitet: [USB/HDMI/WiFi/Bluetooth]\n")
                .append("🛡️ Sigurnost i pouzdanost: [Garancija]\n")
                .append("⚖️ Vrijednost za novac: [Cijena vs konkurencija]\n")
                .append("💡 Preporuke: [Optimizacije]\n")
                .append("🎯 Ukupna ocjena (1-10): X/10 [Obrazložena ocjena]\n\n");

        String finalPrompt = inputBuilder.toString();

        // Output in console (for debugging)
        // logLongPrompt(finalPrompt);

        return finalPrompt;
    }

    private static void logLongPrompt(String prompt) {
        int maxLogSize = 4000; // Android's max log entry size
        for (int i = 0; i <= prompt.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = Math.min(end, prompt.length());
            Log.d(TAG, "PROMPT (part " + (i + 1) + "):\n" + prompt.substring(start, end));
        }
    }
}