package pl.hubert.liberumveto.domain.gra;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import pl.hubert.liberumveto.domain.gracz.Krol;
import pl.hubert.liberumveto.domain.gracz.Magnat;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.sejm.Sejm;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;
import pl.hubert.liberumveto.domain.wyjatki.SejmZerwanyException;

/**
 * Reguły rozgrywki: przebieg tury i warunki końca gry.
 * Nie wie nic o konsoli, więc ta sama klasa posłuży później jako backend REST API.
 *
 * <p>Tura: {@link #wybierzUstawe(int)} (magnaci od razu przekupują) → dowolnie wiele razy
 * {@link #przekup(int, int)} → {@link #glosuj()}.
 */
public class Gra {

    public static final int LICZBA_SEJMOW = 5;
    public static final int LICZBA_POSLOW = 20;
    public static final int USTAW_DO_WYBORU = 3;
    public static final int PROG_ZWYCIESTWA = 3;

    static final int BUDZET_KROLA = 100;
    static final int BONUS_KROLA_ZA_USTAWE = 20;
    static final int BUDZET_MAGNATA = 160;
    static final int BONUS_MAGNATA_ZA_ZERWANIE = 20;

    private final StanGry stan;
    private final KatalogUstaw katalog;
    private final Random random;

    /** Wznowienie gry z istniejącego stanu (np. wczytanego z pliku). */
    public Gra(StanGry stan, KatalogUstaw katalog, Random random) {
        this.stan = Objects.requireNonNull(stan, "stan");
        this.katalog = Objects.requireNonNull(katalog, "katalog");
        this.random = Objects.requireNonNull(random, "random");
    }

    /** Metoda fabryczna: zupełnie nowa gra z wylosowanymi posłami. */
    public static Gra nowa(GeneratorPoslow generator, KatalogUstaw katalog, Random random) {
        var stan = new StanGry(
                1,
                generator.generuj(LICZBA_POSLOW),
                new Krol("Król", BUDZET_KROLA),
                new Magnat("Magnaci", BUDZET_MAGNATA),
                List.of(),
                katalog.losuj(USTAW_DO_WYBORU, random),
                null);
        return new Gra(stan, katalog, random);
    }

    public StanGry stan() {
        return stan;
    }

    public List<Ustawa> ustawyDoWyboru() {
        return stan.ustawyDoWyboru();
    }

    public Optional<Ustawa> wybranaUstawa() {
        return stan.wybranaUstawa();
    }

    /**
     * Król wybiera ustawę (numer z listy, od 1), po czym magnaci rozdają łapówki przeciw niej.
     *
     * @return łapówki rozdane przez magnatów
     */
    public Map<Posel, Integer> wybierzUstawe(int numerNaLiscie) {
        wymagajTrwajacejGry();
        if (stan.wybranaUstawa().isPresent()) {
            throw new IllegalStateException("Ustawa na ten sejm została już wybrana");
        }
        List<Ustawa> ustawy = stan.ustawyDoWyboru();
        if (numerNaLiscie < 1 || numerNaLiscie > ustawy.size()) {
            throw new NieprawidlowyWyborException("Wybierz ustawę od 1 do " + ustawy.size());
        }
        Ustawa ustawa = ustawy.get(numerNaLiscie - 1);
        stan.ustawWybranaUstawe(ustawa);
        return stan.magnat().rozdzielLapowki(stan.poslowie(), ustawa);
    }

    /** Król przekupuje posła; zwraca poparcie przed i po łapówce. */
    public ZmianaPoparcia przekup(int poselId, int kwota) {
        Ustawa ustawa = wymagajWybranejUstawy();
        Posel posel = stan.posel(poselId);
        int przed = posel.obliczPoparcie(ustawa);
        stan.krol().przekup(posel, kwota);
        return new ZmianaPoparcia(posel, przed, posel.obliczPoparcie(ustawa));
    }

    /** Głosowanie nad wybraną ustawą. Po nim gra przechodzi do kolejnego sejmu. */
    public WynikSejmu glosuj() {
        Ustawa ustawa = wymagajWybranejUstawy();
        WynikGlosowania wynik;
        try {
            wynik = new Sejm(stan.poslowie(), random).obraduj(ustawa);
        } catch (SejmZerwanyException e) {
            // zerwanie to legalny wynik tury, więc nie przepuszczamy wyjątku wyżej, tylko go zapisujemy
            wynik = e.wynik();
        }
        var wynikSejmu = new WynikSejmu(stan.numerSejmu(), ustawa, wynik);
        stan.dodajDoHistorii(wynikSejmu);
        if (!czyKoniec()) {
            przygotujNastepnySejm();
        } else {
            stan.ustawWybranaUstawe(null);
        }
        return wynikSejmu;
    }

    private void przygotujNastepnySejm() {
        stan.nastepnySejm();
        stan.poslowie().forEach(Posel::wyczyscLapowki);
        stan.krol().ustawBudzet(BUDZET_KROLA + BONUS_KROLA_ZA_USTAWE * (int) liczbaPrzyjetych());
        stan.magnat().ustawBudzet(BUDZET_MAGNATA + BONUS_MAGNATA_ZA_ZERWANIE * (int) liczbaZerwanych());
        stan.ustawUstawyDoWyboru(katalog.losuj(USTAW_DO_WYBORU, random));
        stan.ustawWybranaUstawe(null);
    }

    public boolean czyKoniec() {
        return stan.historia().size() >= LICZBA_SEJMOW;
    }

    public long liczbaPrzyjetych() {
        return stan.historia().stream().filter(w -> w.wynik().przyjeta()).count();
    }

    public long liczbaZerwanych() {
        return stan.historia().stream().filter(w -> w.wynik().zerwany()).count();
    }

    /** Rozstrzygnięcie gry; dostępne dopiero po ostatnim sejmie. */
    public WynikGry wynik() {
        if (!czyKoniec()) {
            throw new IllegalStateException("Gra jeszcze trwa");
        }
        if (liczbaPrzyjetych() >= PROG_ZWYCIESTWA) {
            return WynikGry.WYGRANA_KROLA;
        }
        if (liczbaZerwanych() >= PROG_ZWYCIESTWA) {
            return WynikGry.WYGRANA_MAGNATOW;
        }
        return WynikGry.REMIS;
    }

    private void wymagajTrwajacejGry() {
        if (czyKoniec()) {
            throw new IllegalStateException("Gra się skończyła");
        }
    }

    private Ustawa wymagajWybranejUstawy() {
        wymagajTrwajacejGry();
        return stan.wybranaUstawa()
                .orElseThrow(() -> new NieprawidlowyWyborException("Najpierw wybierz ustawę"));
    }
}
