package pl.hubert.liberumveto.console;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.domain.gra.Gra;
import pl.hubert.liberumveto.domain.gra.WynikSejmu;
import pl.hubert.liberumveto.domain.gra.ZmianaPoparcia;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;
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
              głosuj    (g)          zakończ przekupstwa i przeprowadź głosowanie
              pomoc     (?)          ta lista
              wyjdź                  przerwij grę
            Progi: poparcie >= 50 to głos ZA, poniżej 15 grozi VETO.""";

    private final Scanner wejscie;
    private final PrintStream wyjscie;
    private final Gra gra;
    private final ParserKomend parser = new ParserKomend();

    public KonsolaUI(Scanner wejscie, PrintStream wyjscie, Gra gra) {
        this.wejscie = wejscie;
        this.wyjscie = wyjscie;
        this.gra = gra;
    }

    /** Główna pętla gry. */
    public void uruchom() {
        wyjscie.println("Witaj, Najjaśniejszy Panie! Przeforsuj 3 z 5 ustaw, zanim magnaci zerwą sejmy.");
        wyjscie.println("Wpisz pomoc, aby zobaczyć komendy.");
        while (!gra.czyKoniec()) {
            if (!rozegrajSejm()) {
                wyjscie.println("Gra przerwana. Bywaj, Miłościwy Panie!");
                return;
            }
        }
        pokazKoniecGry();
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
            wyjscie.println("Komenda (przekup <id> <kwota> | posłowie | głosuj | pomoc):");
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
