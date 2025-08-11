package com.example.techanalysisapp3.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.techanalysisapp3.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TriviaRepository {
    private static final String PREFS_NAME = "TriviaPrefs";
    private static final String KEY_LAST_INDEX = "last_index";
    private static final String KEY_LAST_DATE = "last_date";

    private final Context context;
    private final SharedPreferences prefs;
    private final List<String> triviaList;
    private final Random random = new Random();

    public TriviaRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.triviaList = new ArrayList<>();
        initializeTriviaList();
    }

    private void initializeTriviaList() {
        triviaList.add("💼 Dell XPS modeli imaju šasiju od karbona – lagani kao Formula 1.");
        triviaList.add("💡 ThinkPad laptopi su korišteni na svemirskoj stanici. Istraži Lenovo sada!");
        triviaList.add("🎮 Alienware ima BIOS overklok! Pogledaj najjače gejming mašine ove sedmice.");
        triviaList.add("🧠 Razer Blade – tanak kao MacBook, ali gejming. Pogledaj ga u aplikaciji.");
        triviaList.add("⚡ MSI ima 6 toplinskih cijevi u hlađenju! Pogledaj koji model dominira.");
        triviaList.add("🔋 LG Gram 17 teži manje od 1kg! Pogledaj najlakše laptope sada.");
        triviaList.add("🖋️ Surface Pro ima 4000+ nivoa pritiska! Pogledaj šta još nudi.");
        triviaList.add("🦾 Acer Predator je budžetski gejming kralj. Provjeri OLX ponude.");
        triviaList.add("📸 Gigabyte Aero koristi 4K AMOLED ekran. Istraži kreatorske modele!");
        triviaList.add("💻 Lenovo Yoga rotira 360°! Savršen za rad i zabavu. Istraži ga sada.");
        triviaList.add("🔊 HP Omen ima Bang & Olufsen zvučnike! Isprobaj zvuk bez kompromisa.");
        triviaList.add("🎯 ASUS TUF ima vojni certifikat izdržljivosti! Pogledaj model sedmice.");
        triviaList.add("📐 ZenBook je tanji od magazina. Provjeri ponudu u aplikaciji.");
        triviaList.add("🧊 XMG laptopi se mogu potpuno prilagoditi. LEGO za odrasle, pogledaj sada!");
        triviaList.add("🌌 Samsung Galaxy Book koristi AMOLED ekran. Pogledaj šta još nudi.");
        triviaList.add("🔎 Lenovo Legion ima tiho ‘Coldfront’ hlađenje. Idealno za gejming!");
        triviaList.add("🧪 ThinkPad je preživio pad s 3 metra. Pouzdanost zauvijek.");
        triviaList.add("🔥 Najgledaniji laptop ove sedmice je... otvori LapGenius i saznaj!");
        triviaList.add("💼 Victus laptopi balansiraju posao i gejming. Istraži modele sada!");
        triviaList.add("🎥 Acer Nitro koriste YouTuberi širom svijeta. Istraži zašto u aplikaciji.");
        triviaList.add("🔋 LG Gram ima bateriju koja traje 28 sati. Pogledaj ponudu sada!");
        triviaList.add("💡 MSI tipke se mogu customizirati. Gejming po tvojoj mjeri!");
        triviaList.add("🚨 1 klik do najbolje OLX ponude! Otvori aplikaciju sada.");
        triviaList.add("🎉 Koji je najtraženiji laptop ove sedmice? Pogledaj odmah!");
        triviaList.add("🕹️ Legion vs Aero – koji je tvoj tim? Istraži brendove!");
        triviaList.add("✨ Dnevna preporuka: Razer Blade 14. Pogledaj zašto dominira.");
        triviaList.add("🎯 Ne znaš koji laptop? AI ti pomaže – otvori LapGenius!");
        triviaList.add("💼 Brend dana: Dell. Pogledaj modele s najboljim ocjenama.");
        triviaList.add("🔍 Upoznaj priču svakog brenda u sekciji 'O brendu'. Saznaj više!");
        triviaList.add("📣 Najtraženiji laptop danas je u aplikaciji. Možda je to tvoj!");
        triviaList.add("💡 ASUS ROG laptopi dolaze sa liquid metal hlađenjem. Pogledaj gejming elitu!");
        triviaList.add("🎮 Legion 5 Pro ima ekran od 165Hz! Pogledaj modele koji prate tvoju brzinu.");
        triviaList.add("🔋 Dell Latitude modeli traju i do 20h na punjenju. Pouzdanost bez punjača.");
        triviaList.add("🖋️ Surface Laptop Studio ima ekran koji lebdi! Provjeri ga u LapGenius aplikaciji.");
        triviaList.add("📸 Aero laptopi su optimizovani za Adobe softver. Pogledaj kreatorske alate!");
        triviaList.add("🎯 Victus laptopi su najtraženiji kod studenata. Pogledaj modele za učenje i igru.");
        triviaList.add("🎧 Razer Blade 16 podržava dual-mode ekran! Istraži tehnologiju budućnosti.");
        triviaList.add("🌌 OLED ekran? Samsung Galaxy Book nudi ga u ultra tankom kućištu.");
        triviaList.add("💼 HP EliteBook je čest izbor u korporacijama. Saznaj zašto u aplikaciji.");
        triviaList.add("🕹️ MSI Titan GT koristi desktop RTX grafiku! Pogledaj gejmerski monstrum.");
        triviaList.add("📐 ZenBook ima military-grade certifikat. Tanak, ali izdržljiv. Pogledaj sada.");
        triviaList.add("🧊 XMG podržava do 64GB RAM-a. Prava snaga za multitasking i više.");
        triviaList.add("🔥 Acer Nitro 5 je među najprodavanijim gejming laptopima u regiji!");
        triviaList.add("💻 Lenovo Yoga podržava digitalne olovke. Savršeno za kreativce!");
        triviaList.add("🎮 Legion 7i koristi mini-LED ekran. Pogledaj gejming u novom svjetlu.");
        triviaList.add("🧠 Razer Chroma tastatura može prikazati 16 miliona boja. Pogledaj efekt uživo.");
        triviaList.add("🔋 Gram 17 ima najlakše 17\" kućište na svijetu. Provjeri nevjerovatnu težinu.");
        triviaList.add("📊 Dell Precision serija je savršena za CAD i analitiku. Provjeri radne stanice.");
        triviaList.add("🌐 Victus dolazi s Wi-Fi 6E podrškom. Brzina bez kompromisa. Saznaj više!");
        triviaList.add("📷 Surface uređaji imaju najbolje kamere među Windows laptopima.");
        triviaList.add("📣 XPS ekran ima gotovo nevidljive ivice! Istraži maksimalni prikaz.");
        triviaList.add("🎓 Najtraženiji laptop među studentima je upravo objavljen! Saznaj koji.");
        triviaList.add("🔊 Bang & Olufsen zvuk u HP modelima. Probaj ga virtualno – klikni sad.");
        triviaList.add("🕵️ Da li znaš koji laptop koriste FBI agenti? Otvori aplikaciju i saznaj.");
        triviaList.add("🛡️ ASUS TUF ima tastaturu otpornu na prosipanje! Pogledaj test izdržljivosti.");
        triviaList.add("💥 Legion koristi AI tuning za igre! Pogledaj automatsko podešavanje FPS-a.");
        triviaList.add("🎯 Danas je top brend... otvori LapGenius i otkrij koji vodi!");
        triviaList.add("✨ Razer je omiljen među streamerima. Saznaj zašto u sekciji Brendovi.");
        triviaList.add("📦 Na OLX-u je dodan novi MSI model. Prvi ga pogledaj u aplikaciji.");
        triviaList.add("📣 Pogledaj preporučene laptope za ovu sedmicu! Tvoje idealno čeka.");
        triviaList.add("🧊 XMG se pravi ručno u Njemačkoj! Pogledaj vrhunski hardver bez kompromisa.");
        triviaList.add("🛸 ThinkPad je sletio na Mjesec... skoro! Koristi se i u NASA projektima.");
        triviaList.add("🕶️ Razer Blade je zvan 'MacBook za gejmere'. Elegancija + moć = ljubav.");
        triviaList.add("🔥 Legion 5i – najviše pregleda ove sedmice. Saznaj šta ga čini hitom!");
        triviaList.add("🖥️ Da li znaš koji laptop koristi RTX 4090? Provjeri u našoj Top kategoriji!");
        triviaList.add("😎 MSI Raider je kao sportski auto među laptopima. Pogledaj performanse!");
        triviaList.add("🪶 LG Gram 17 je lakši od iPada Pro. I da, ima veći ekran!");
        triviaList.add("🧠 ZenBook ima AI Noise Cancelation! Savršen za online predavanja i pozive.");
        triviaList.add("🎨 Aero laptopi imaju Pantone-validaciju. Profesionalni kolori, bez greške.");
        triviaList.add("💼 Surface Laptop je tanak, lagan i elegantan. Pogledaj ga u sekciji Poslovni.");
        triviaList.add("🎮 ROG Zephyrus ima ekran koji rotira! Gejming iz svakog ugla. Provjeri sad.");
        triviaList.add("🔋 Gram baterija traje duže nego neka putovanja. Isprobaj ultralako čudo.");
        triviaList.add("📊 Dell Precision podržava 8K video editing. Snaga za pro korisnike!");
        triviaList.add("🧃 ASUS TUF testiran je na prolijevanje kafe. Pravi student friendly izbor. ☕");
        triviaList.add("🌈 Razer koristi Chroma RGB. Tastatura koja pleše – pogledaj efekte!");
        triviaList.add("🔍 Surface Pro je i tablet i laptop. Pogledaj kako koristiš dva u jednom.");
        triviaList.add("📣 Novi Predator model upravo stigao na OLX! Budi među prvima koji ga vide.");
        triviaList.add("🌐 ZenBook ima Wi-Fi 6E. Leti kroz mrežu bez zastoja. Istraži brže opcije!");
        triviaList.add("🧠 Lenovo koristi AI cooling tehnologiju. Tiše, pametnije, efikasnije.");
        triviaList.add("📱 Samsung Galaxy Book savršeno radi sa Galaxy telefonom. Sinkronizuj sad!");
        triviaList.add("🎥 XPS OLED ekran oduzima dah. Savršen za filmove i serije u 4K.");
        triviaList.add("🕹️ Alienware je ime iz 1996. godine. I dalje simbol gejming prestiža.");
        triviaList.add("🎯 Da li znaš koji laptop dominira OLX-om ovaj mjesec? Saznaj unutar aplikacije!");
        triviaList.add("🧪 Victus laptopi testirani su za 20.000 otvaranja poklopca. Dugotrajni izbor!");
        triviaList.add("🛡️ TUF modeli imaju military-grade standarde. Robustnost u svakom pogledu.");
        triviaList.add("🖋️ Surface stylus podržava nagib! Digitalno crtanje nikad nije bilo prirodnije.");
        triviaList.add("💬 Korisnici ocijenili Lenovo kao najpouzdaniji brend. Pogledaj njihove recenzije.");
        triviaList.add("🎉 Novi XMG modeli dodani na OLX! Pogledaj prilagodljive gejming opcije.");
        triviaList.add("🔔 LapGenius ti svakog dana donosi top model! Ne propusti ponudu dana.");
        triviaList.add("🧊 XMG se pravi ručno u Njemačkoj! Pogledaj vrhunski hardver bez kompromisa.");
        triviaList.add("🛸 ThinkPad je sletio na Mjesec... skoro! Koristi se i u NASA projektima.");
        triviaList.add("🕶️ Razer Blade je zvan 'MacBook za gejmere'. Elegancija + moć = ljubav.");
        triviaList.add("🔥 Legion 5i – najviše pregleda ove sedmice. Saznaj šta ga čini hitom!");
        triviaList.add("🖥️ Da li znaš koji laptop koristi RTX 4090? Provjeri u našoj Top kategoriji!");
        triviaList.add("😎 MSI Raider je kao sportski auto među laptopima. Pogledaj performanse!");
        triviaList.add("🪶 LG Gram 17 je lakši od iPada Pro. I da, ima veći ekran!");
        triviaList.add("🧠 ZenBook ima AI Noise Cancelation! Savršen za online predavanja i pozive.");
        triviaList.add("🎨 Aero laptopi imaju Pantone-validaciju. Profesionalni kolori, bez greške.");
        triviaList.add("💼 Surface Laptop je tanak, lagan i elegantan. Pogledaj ga u sekciji Poslovni.");
        triviaList.add("🎮 ROG Zephyrus ima ekran koji rotira! Gejming iz svakog ugla. Provjeri sad.");
        triviaList.add("🔋 Gram baterija traje duže nego neka putovanja. Isprobaj ultralako čudo.");
        triviaList.add("📊 Dell Precision podržava 8K video editing. Snaga za pro korisnike!");
        triviaList.add("🧃 ASUS TUF testiran je na prolijevanje kafe. Pravi student friendly izbor. ☕");
        triviaList.add("🌈 Razer koristi Chroma RGB. Tastatura koja pleše – pogledaj efekte!");
        triviaList.add("🔍 Surface Pro je i tablet i laptop. Pogledaj kako koristiš dva u jednom.");
        triviaList.add("📣 Novi Predator model upravo stigao na OLX! Budi među prvima koji ga vide.");
        triviaList.add("🌐 ZenBook ima Wi-Fi 6E. Leti kroz mrežu bez zastoja. Istraži brže opcije!");
        triviaList.add("🧠 Lenovo koristi AI cooling tehnologiju. Tiše, pametnije, efikasnije.");
        triviaList.add("📱 Samsung Galaxy Book savršeno radi sa Galaxy telefonom. Sinkronizuj sad!");
        triviaList.add("🎥 XPS OLED ekran oduzima dah. Savršen za filmove i serije u 4K.");
        triviaList.add("🕹️ Alienware je ime iz 1996. godine. I dalje simbol gejming prestiža.");
        triviaList.add("🎯 Da li znaš koji laptop dominira OLX-om ovaj mjesec? Saznaj unutar aplikacije!");
        triviaList.add("🧪 Victus laptopi testirani su za 20.000 otvaranja poklopca. Dugotrajni izbor!");
        triviaList.add("🛡️ TUF modeli imaju military-grade standarde. Robustnost u svakom pogledu.");
        triviaList.add("🖋️ Surface stylus podržava nagib! Digitalno crtanje nikad nije bilo prirodnije.");
        triviaList.add("💬 Korisnici ocijenili Lenovo kao najpouzdaniji brend. Pogledaj njihove recenzije.");
        triviaList.add("🎉 Novi XMG modeli dodani na OLX! Pogledaj prilagodljive gejming opcije.");
        triviaList.add("🧠 Koji je najpametniji laptop 2025? Otvori aplikaciju i pogledaj AI preporuku.");
        triviaList.add("🔔 LapGenius ti svakog dana donosi top model! Ne propusti ponudu dana.");
        triviaList.add("📅 Prvi laptop s touchpadom bio je Apple PowerBook 500... još 1994. godine!");
        triviaList.add("🖥️ Dell je osnovan 1984. iz studentske sobe! Danas globalni lider u hardveru.");
        triviaList.add("💡 ThinkPad dizajniran je po japanskim bento kutijama. Pogledaj zašto izgleda klasično.");
        triviaList.add("🧠 Acer je započeo kao distribucijska firma za mikročipove! Danas pravi Predator seriju.");
        triviaList.add("🛸 Prvi laptop ikad? Osborne 1 iz 1981. – težio je skoro 11kg!");
        triviaList.add("💾 Lenovo je kupio IBM-ovu PC diviziju 2005. i preuzeo ThinkPad seriju.");
        triviaList.add("🎮 Alienware je osnovan od strane dvoje gejmera 1996. Naziv je inspirisan serijom X-Files!");
        triviaList.add("📐 ZenBook serija inspirisana je japanskom filozofijom minimalizma. Pogledaj modele.");
        triviaList.add("🧪 MSI je ušao u gejming tek 2008. – danas je lider u high-end laptopima.");
        triviaList.add("🖋️ Surface linija redefinisala je 2-u-1 uređaje kad je lansirana 2012. godine.");
        triviaList.add("🔍 HP je osnovan u garaži 1939. godine. I danas – lider u poslovnim laptopima.");
        triviaList.add("🌍 Samsung je proizvodio nudle, šećer i tkanine prije nego je ušao u elektroniku!");
        triviaList.add("📺 Razer Blade prvi je laptop sa 120Hz ekranom za gejmere još 2017. godine.");
        triviaList.add("🔋 LG Gram prvi je probio granicu od 1kg za 15'' modele. Ultra lako, ultra moćno.");
        triviaList.add("🎓 Victus serija HP-a je lansirana tek 2021, a već je među najtraženijim laptopima!");
        triviaList.add("📦 Prvi MacBook Pro sa Retina ekranom izašao je 2012. – promijenio industriju ekrana.");
        triviaList.add("🧠 ASUS je originalno pravio matične ploče! Danas pravi neke od top gejming laptopa.");
        triviaList.add("🎯 Alienware je prvi laptop s RGB tastaturom. Revolucija u gejming dizajnu!");
        triviaList.add("🎞️ Aero laptopi prvi su nudili Pantone-certifikovane ekrane za editore.");
        triviaList.add("💡 Prvi laptop sa SSD-om kao standard? MacBook Air 2008. – tada vizija, danas norma.");
        triviaList.add("🖥️ Toshiba je proizvela prvi komercijalni laptop u svijetu – T1100 iz 1985.");
        triviaList.add("🧠 IBM ThinkPad 701C ima 'butterfly' tastaturu. Inženjersko čudo devedesetih!");
        triviaList.add("🎮 ROG je stvoren 2006. kao ASUS-ov gejming ogranak. Danas poznat u cijelom svijetu.");
        triviaList.add("🧪 XMG je underground brend poznat po custom laptopima. Njemačka preciznost u hardveru.");
        triviaList.add("🧬 Lenovo je prvi kineski brend koji je postao #1 proizvođač laptopa globalno.");
        triviaList.add("🔋 Najduži testirani vijek baterije bio je 31 sat – ostvaren na LG Gram modelu.");
        triviaList.add("🧠 MSI Titan GT77 sadrži desktop-class procesor. Doslovno desktop u laptopu.");
        triviaList.add("🚀 ThinkPad je jedini laptop certificiran za upotrebu u ruskom i američkom svemirskom programu.");
        triviaList.add("📣 Sve ovo i još mnogo zanimljivosti čeka te u aplikaciji. Otvori LapGenius!");
        triviaList.add("🕰️ 40 godina laptop evolucije stalo je u tvoju šaku. Pogledaj koji je za tebe!");
        triviaList.add("🤯 Legion 5 Pro za ovu cijenu? Provjeri da li je još dostupan!");
        triviaList.add("⚠️ Predator za manje od 1.500KM? Ovo se ne viđa svaki dan.");
        triviaList.add("🛒 Laptopi na akciji? Pogledaj šta smo pronašli samo za danas!");
        triviaList.add("🎓 Koji je idealan laptop za fax? Otvori LapGenius i saznaj odmah.");
        triviaList.add("🔥 Novi modeli upravo dodani! Budi među prvima koji ih vide.");
        triviaList.add("📉 Pad cijena! Provjeri koji modeli su sad ispod 1.000KM.");
        triviaList.add("🧃 TUF Gaming preživio prosutu kafu. Doslovno. Istraži izdržljive laptope!");
        triviaList.add("👀 Kad vidiš Legion 7 ispod 2K... otvori app prije nego nestane.");
        triviaList.add("😂 Razer: 'macbook, but for gamers'. Provjeri da li je hype opravdan.");
        triviaList.add("👑 XPS + OLED = wow. Isprobaj najluksuzniji Dell u klasi.");
        triviaList.add("🧠 Ne znaš šta da kupiš? Naš AI zna. Otvori aplikaciju sada!");
        triviaList.add("💸 Olx ponude + LapGenius filter = laptopi za svaki budžet.");
        triviaList.add("🕶️ Acer Nitro i dalje dominira mid-range gejming klasom. Provjeri modele.");
        triviaList.add("🪄 'Najtraženiji laptop' se mijenja svaki dan. Saznaj koji je danas!");
        triviaList.add("🧊 Laptop ti se pregrijava? Pogledaj modele s najboljim hlađenjem.");
        triviaList.add("📺 Gledaš filmove? Pogledaj koji laptopi imaju najbolji ekran.");
        triviaList.add("🎧 Zvuk je bitan! Pogledaj laptope s najboljim zvučnicima.");
        triviaList.add("🔋 Umoran od punjača? Pogledaj laptope sa baterijom od 20+ sati.");
        triviaList.add("💡 Savjet: SSD + 16GB RAM je idealan za većinu korisnika.");
        triviaList.add("😅 Tvoj laptop se budi sporije nego ti? Vrijeme je za zamjenu.");
        triviaList.add("🛡️ Kupuješ polovan laptop? Istraži sekciju savjeta u LapGenius!");
        triviaList.add("⚙️ Da li znaš razliku između Ryzen 5 i i5? Saznaj u aplikaciji.");
        triviaList.add("🤓 DDR4 ili DDR5? Koji RAM ti treba? Pogledaj vodič u appu.");
        triviaList.add("⏳ Laptop ti se vuče? Saznaj koji su najbrži za tu cijenu.");
        triviaList.add("💭 Više portova = manje problema. Provjeri koji modeli to nude.");
        triviaList.add("🔍 Danas izdvajamo: najviše klikova ima ASUS TUF. Pogledaj zašto.");
        triviaList.add("📣 Novi model na OLX-u već prikupio 500 pregleda! Saznaj koji.");
        triviaList.add("🧠 AI u laptopima? Da, to je stvar. Pogledaj nove trendove.");
        triviaList.add("🎮 FPS boost bez dodatnog hardvera? Pogledaj laptop sa MUX switchom!");
        triviaList.add("🎯 Šta se najviše traži danas? Provjeri top pretrage u aplikaciji.");
    }

    public String getRandomTrivia() {
        if (triviaList.isEmpty()) {
            return context.getString(R.string.default_trivia);
        }

        // Check if we already sent notification today
        long lastDate = prefs.getLong(KEY_LAST_DATE, 0);
        if (DateUtils.isToday(lastDate)) {
            return null; // Already sent today
        }

        int lastIndex = prefs.getInt(KEY_LAST_INDEX, -1);
        int newIndex;

        // Ensure we don't repeat the same trivia two days in a row
        if (triviaList.size() > 1) {
            do {
                newIndex = random.nextInt(triviaList.size());
            } while (newIndex == lastIndex);
        } else {
            newIndex = 0;
        }

        // Save new index and date
        prefs.edit()
                .putInt(KEY_LAST_INDEX, newIndex)
                .putLong(KEY_LAST_DATE, System.currentTimeMillis())
                .apply();

        return triviaList.get(newIndex);
    }

    public String getRandomTestTrivia() {
        if (triviaList.isEmpty()) {
            return context.getString(R.string.default_trivia);
        }

        Random random = new Random();
        return triviaList.get(random.nextInt(triviaList.size()));
    }
}