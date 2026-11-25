package pl.hubert.liberumveto.console;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.console.zapis.ZapisGry;
import pl.hubert.liberumveto.domain.gra.Gra;
import pl.hubert.liberumveto.domain.gra.StanGry;
import pl.hubert.liberumveto.domain.gra.Statystyki;
import pl.hubert.liberumveto.domain.gra.WynikSejmu;
import pl.hubert.liberumveto.domain.gra.ZmianaPoparcia;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;
import pl.hubert.liberumveto.domain.wyjatki.BladZapisuGryException;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;
import pl.hubert.liberumveto.domain.wyjatki.NiewystarczajacyBudzetException;

/**
 * Interfejs tekstowy: czyta komendy, wywołuje logikę z {@link Gra} i drukuje wyniki.
 * Wejście i wyjście dostaje przez konstruktor, więc w testach podstawiamy tekst zamiast klawiatury.
 */
public class KonsolaUI {

    private static final String POMOC = """
            Dostępne komendy:
              przekup <id> <kwota>   przekup posła (podnosi jego poparcie dla ustawy)
              posłowie  (p)          tabela posłów z aktualnym poparciem
              statystyki (s)         frakcje, najbardziej przekupni, zagrożeni vetem
              głosuj    (g)          zakończ przekupstwa i przeprowadź głosowanie
              zapisz                 zapisz grę (zapis dzieje się też po każdym sejmie)
              pomoc     (?)          ta lista
              wyjdź                  przerwij grę
            Progi: poparcie >= 50 to głos ZA, poniżej 15 grozi VETO.""";

    private final Scanner wejscie;
    private final PrintStream wyjscie;
    private final ZapisGry zapis;
    private final ParserKomend parser = new ParserKomend();
    private Gra gra;

    public KonsolaUI(Scanner wejscie, PrintStream wyjscie, ZapisGry zapis) {
        this.wejscie = wejscie;
        this.wyjscie = wyjscie;
        this.zapis = zapis;
    }

    /**
     * Główna pętla gry. Sposób utworzenia gry dostajemy jako funkcje, więc konsola nie musi
     * wiedzieć, skąd brać posłów, katalog ustaw czy generator losowy.
     *
     * @param nowaGra tworzy nową grę
     * @param wznow   tworzy grę z wczytanego stanu
     */
    public void uruchom(Supplier<Gra> nowaGra, Function<StanGry, Gra> wznow) {
        wyjscie.println("Witaj, Najjaśniejszy Panie! Przeforsuj 3 z 5 ustaw, zanim magnaci zerwą sejmy.");
        Optional<Gra> start = wybierzStart(nowaGra, wznow);
        if (start.isEmpty()) {
            wyjscie.println("Bywaj, Miłościwy Panie!");
            return;
        }
        gra = start.get();
        wyjscie.println("Wpisz pomoc, aby zobaczyć komendy.");
        while (!gra.czyKoniec()) {
            if (!rozegrajSejm()) {
                wyjscie.println("Gra przerwana. Bywaj, Miłościwy Panie!");
                return;
            }
        }
        pokazKoniecGry();
    }

    /** F1: nowa gra albo wczytanie zapisu, jeśli istnieje. */
    private Optional<Gra> wybierzStart(Supplier<Gra> nowaGra, Function<StanGry, Gra> wznow) {
        if (!zapis.istnieje()) {
            return Optional.of(nowaGra.get());
        }
        while (true) {
            wyjscie.println("Znaleziono zapis gry. 1) Wczytaj zapis  2) Nowa gra");
            Optional<String> linia = czytajLinie();
            if (linia.isEmpty()) {
                return Optional.empty();
            }
            switch (linia.get().strip()) {
                case "1" -> {
                    try {
                        StanGry stan = zapis.wczytaj();
                        wyjscie.printf("Wczytano grę (sejm %d/%d).%n", stan.numerSejmu(), Gra.LICZBA_SEJMOW);
                        return Optional.of(wznow.apply(stan));
                    } catch (BladZapisuGryException e) {
                        wyjscie.println("! " + e.getMessage() + ". Zaczynamy nową grę.");
                        return Optional.of(nowaGra.get());
                    }
                }
                case "2" -> {
                    return Optional.of(nowaGra.get());
                }
                default -> wyjscie.println("! Wpisz 1 albo 2");
            }
        }
    }

    /** @return false, jeśli gracz chce wyjść */
    private boolean rozegrajSejm() {
        var stan = gra.stan();
        wyjscie.printf("%n=== SEJM %d/%d | Budżet króla: %d dukatów ===%n",
                stan.numerSejmu(), Gra.LICZBA_SEJMOW, stan.krol().budzet());

        if (gra.wybranaUstawa().isEmpty() && !wybierzUstawe()) {
            return false;
        }
        Ustawa ustawa = gra.wybranaUstawa().orElseThrow();
        wyjscie.println("Obrady nad ustawą: " + ustawa.tytul());

        while (true) {
            wyjscie.println("Komenda (przekup <id> <kwota> | posłowie | statystyki | głosuj | zapisz | pomoc):");
            Optional<String> linia = czytajLinie();
            if (linia.isEmpty()) {
                return false;
            }
            try {
                Komenda komenda = parser.parsuj(linia.get());
                // pattern matching w switch (Java 21) z dekonstrukcją rekordu Przekup
                switch (komenda) {
                    case Komenda.Przekup(int poselId, int kwota) -> przekup(poselId, kwota);
                    case Komenda.Poslowie() -> pokazPoslow(ustawa);
                    case Komenda.Statystyki() -> pokazStatystyki(ustawa);
                    case Komenda.Zapisz() -> zapisz();
                    case Komenda.Pomoc() -> wyjscie.println(POMOC);
                    case Komenda.Glosuj() -> {
                        glosuj();
                        return true;
                    }
                    case Komenda.Wyjdz() -> {
                        return false;
                    }
                }
            } catch (NieprawidlowyWyborException | NiewystarczajacyBudzetException | IllegalArgumentException e) {
                // multi-catch: błędy gracza pokazujemy i pytamy dalej, gra się nie wywraca
                wyjscie.println("! " + e.getMessage());
            }
        }
    }

    private boolean wybierzUstawe() {
        List<Ustawa> ustawy = gra.ustawyDoWyboru();
        while (true) {
            wyjscie.println("Wybierz ustawę:");
            for (int i = 0; i < ustawy.size(); i++) {
                wyjscie.printf(" %d) %-42s %s%n", i + 1, ustawy.get(i).tytul(), opisWplywu(ustawy.get(i)));
            }
            Optional<String> linia = czytajLinie();
            if (linia.isEmpty() || ParserKomend.normalizuj(linia.get().strip()).equals("wyjdz")) {
                return false;
            }
            try {
                Map<Posel, Integer> lapowki = gra.wybierzUstawe(parser.parsujNumer(linia.get()));
                wyjscie.printf("Magnaci rozdają łapówki... (przekupionych posłów: %d)%n", lapowki.size());
                return true;
            } catch (NieprawidlowyWyborException e) {
                wyjscie.println("! " + e.getMessage());
            }
        }
    }

    /** Np. "[królewska +20, magnacka -30, niezależna +0]". */
    private static String opisWplywu(Ustawa ustawa) {
        return Arrays.stream(Frakcja.values())
                .map(frakcja -> "%s %+d".formatted(frakcja.nazwa(), ustawa.wplywNa(frakcja)))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private void przekup(int poselId, int kwota) {
        ZmianaPoparcia zmiana = gra.przekup(poselId, kwota);
        wyjscie.printf("%s: poparcie %d → %d (zostało %d dukatów)%n",
                zmiana.posel().pelneImie(), zmiana.przed(), zmiana.po(), gra.stan().krol().budzet());
    }

    private void pokazPoslow(Ustawa ustawa) {
        wyjscie.printf("%3s  %-36s %-11s %5s %5s %8s  %s%n",
                "ID", "Poseł", "Frakcja", "Lojal", "Przek", "Poparcie", "Przewidywany głos");
        gra.stan().poslowie().stream()
                .sorted(Comparator.comparingInt((Posel p) -> p.obliczPoparcie(ustawa)))
                .forEach(posel -> {
                    int poparcie = posel.obliczPoparcie(ustawa);
                    wyjscie.printf("%3d  %-36s %-11s %5d %5.2f %8d  %s%n",
                            posel.id(), posel.pelneImie(), posel.frakcja().nazwa(),
                            posel.lojalnoscWobecKrola(), posel.przekupnosc(), poparcie, opisGlosu(poparcie));
                });
    }

    private void pokazStatystyki(Ustawa ustawa) {
        var statystyki = new Statystyki(gra.stan().poslowie());
        Map<Frakcja, Long> liczebnosc = statystyki.liczebnoscFrakcji();

        wyjscie.println("Średnia lojalność wobec Króla:");
        statystyki.sredniaLojalnoscPerFrakcja().forEach((frakcja, srednia) ->
                wyjscie.printf("  %-11s %5.1f  (posłów: %d)%n", frakcja.nazwa(), srednia, liczebnosc.get(frakcja)));

        wyjscie.printf("Średnie poparcie dla ustawy: %.1f%n", statystyki.sredniePoparcie(ustawa));

        wyjscie.println("Top 3 najbardziej przekupnych:");
        statystyki.najbardziejPrzekupni(3).forEach(posel ->
                wyjscie.printf("  [%d] %s (%.2f)%n", posel.id(), posel.pelneImie(), posel.przekupnosc()));

        List<Posel> zagrozeni = statystyki.zagrozeniVetem(ustawa);
        wyjscie.printf("Zagrożeni vetem (%d):%n", zagrozeni.size());
        zagrozeni.forEach(posel ->
                wyjscie.printf("  [%d] %s: poparcie %d%n", posel.id(), posel.pelneImie(), posel.obliczPoparcie(ustawa)));
    }

    private static String opisGlosu(int poparcie) {
        if (poparcie >= Posel.PROG_ZA) {
            return "ZA";
        }
        if (poparcie >= Posel.PROG_VETA) {
            return "PRZECIW";
        }
        int szansa = (Posel.PROG_VETA - poparcie) * 100 / Posel.PROG_VETA;
        return "!!! VETO (" + szansa + "%)";
    }

    private void glosuj() {
        WynikSejmu wynikSejmu = gra.glosuj();
        WynikGlosowania wynik = wynikSejmu.wynik();
        wyjscie.printf("ZA: %d | PRZECIW: %d | VETO: %d%n",
                wynik.glosy(Glos.ZA), wynik.glosy(Glos.PRZECIW), wynik.glosy(Glos.VETO));
        wyjscie.println(opisWyniku(wynik));
        zapisz(); // F8: autozapis po każdej turze
    }

    private void zapisz() {
        try {
            zapis.zapisz(gra.stan());
            wyjscie.println("(gra zapisana w " + zapis.plik() + ")");
        } catch (BladZapisuGryException e) {
            // błąd zapisu nie powinien kończyć gry; informujemy i gramy dalej
            wyjscie.println("! " + e.getMessage() + ": " + e.getCause().getMessage());
        }
    }

    private static String opisWyniku(WynikGlosowania wynik) {
        return wynik.zrywajacy()
                .map(posel -> "!!! Sejm zerwany przez: " + posel.pelneImie())
                .orElse(wynik.przyjeta() ? "Vivat! Ustawa przyjęta." : "Ustawa odrzucona większością głosów.");
    }

    private void pokazKoniecGry() {
        wyjscie.println();
        wyjscie.println("=== KONIEC GRY ===");
        for (WynikSejmu w : gra.stan().historia()) {
            wyjscie.printf("Sejm %d: %-40s %s%n", w.numer(), w.ustawa().tytul(), krotkiOpis(w.wynik()));
        }
        wyjscie.printf("Przyjęte: %d | Zerwane: %d%n", gra.liczbaPrzyjetych(), gra.liczbaZerwanych());
        wyjscie.println(gra.wynik().opis());
    }

    private static String krotkiOpis(WynikGlosowania wynik) {
        if (wynik.zerwany()) {
            return "ZERWANY (" + wynik.zrywajacy().orElseThrow().pelneImie() + ")";
        }
        return wynik.przyjeta() ? "PRZYJĘTA" : "ODRZUCONA";
    }

    /** Pusta wartość oznacza koniec wejścia (np. Ctrl+D lub koniec pliku w testach). */
    private Optional<String> czytajLinie() {
        wyjscie.print("> ");
        wyjscie.flush();
        return wejscie.hasNextLine() ? Optional.of(wejscie.nextLine()) : Optional.empty();
    }
}
