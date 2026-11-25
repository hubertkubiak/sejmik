package pl.hubert.liberumveto.domain.model;

import java.util.Objects;
import java.util.Random;

/**
 * Poseł na sejm. Jego poparcie zmienia się pod wpływem ustawy i łapówek, dlatego to klasa,
 * a nie rekord: ma tożsamość (id) i stan, który żyje w czasie.
 */
public class Posel implements Glosujacy {

    public static final int PROG_ZA = 50;
    public static final int PROG_VETA = 15;
    public static final int MIN_POPARCIE = 0;
    public static final int MAX_POPARCIE = 100;

    private final int id;
    private final String imie;
    private final String herb;
    private final Frakcja frakcja;
    private final int lojalnoscWobecKrola;
    private final double przekupnosc;
    private int lapowkiKrola;
    private int lapowkiMagnatow;

    public Posel(int id, String imie, String herb, Frakcja frakcja,
                 int lojalnoscWobecKrola, double przekupnosc) {
        this(id, imie, herb, frakcja, lojalnoscWobecKrola, przekupnosc, 0, 0);
    }

    /** Pełny konstruktor, przydatny np. przy wczytywaniu zapisanej gry. */
    public Posel(int id, String imie, String herb, Frakcja frakcja,
                 int lojalnoscWobecKrola, double przekupnosc,
                 int lapowkiKrola, int lapowkiMagnatow) {
        if (lojalnoscWobecKrola < MIN_POPARCIE || lojalnoscWobecKrola > MAX_POPARCIE) {
            throw new IllegalArgumentException("Lojalność musi być w zakresie 0-100: " + lojalnoscWobecKrola);
        }
        if (przekupnosc < 0.0 || przekupnosc > 1.0) {
            throw new IllegalArgumentException("Przekupność musi być w zakresie 0.0-1.0: " + przekupnosc);
        }
        if (lapowkiKrola < 0 || lapowkiMagnatow < 0) {
            throw new IllegalArgumentException("Łapówki nie mogą być ujemne");
        }
        this.id = id;
        this.imie = Objects.requireNonNull(imie, "imie");
        this.herb = Objects.requireNonNull(herb, "herb");
        this.frakcja = Objects.requireNonNull(frakcja, "frakcja");
        this.lojalnoscWobecKrola = lojalnoscWobecKrola;
        this.przekupnosc = przekupnosc;
        this.lapowkiKrola = lapowkiKrola;
        this.lapowkiMagnatow = lapowkiMagnatow;
    }

    /**
     * Algorytm ze specyfikacji (3.3): lojalność + wpływ ustawy + łapówki króla - łapówki magnatów,
     * przy czym łapówki są mnożone przez przekupność. Wynik obcinamy do 0-100.
     */
    public int obliczPoparcie(Ustawa ustawa) {
        double surowe = lojalnoscWobecKrola
                + ustawa.wplywNa(frakcja)
                + lapowkiKrola * przekupnosc
                - lapowkiMagnatow * przekupnosc;
        long zaokraglone = Math.round(surowe);
        return Math.clamp(zaokraglone, MIN_POPARCIE, MAX_POPARCIE);
    }

    @Override
    public Glos zaglosuj(Ustawa ustawa, Random random) {
        int poparcie = obliczPoparcie(ustawa);
        if (poparcie >= PROG_ZA) {
            return Glos.ZA;
        }
        if (poparcie >= PROG_VETA) {
            return Glos.PRZECIW;
        }
        double szansaNaVeto = (PROG_VETA - poparcie) / (double) PROG_VETA;
        return random.nextDouble() < szansaNaVeto ? Glos.VETO : Glos.PRZECIW;
    }

    public void przyjmijLapowkeOdKrola(int kwota) {
        lapowkiKrola += wymagajDodatniej(kwota);
    }

    public void przyjmijLapowkeOdMagnatow(int kwota) {
        lapowkiMagnatow += wymagajDodatniej(kwota);
    }

    /** Łapówki dotyczą jednego sejmu. Przed kolejnym "zapominamy" o nich. */
    public void wyczyscLapowki() {
        lapowkiKrola = 0;
        lapowkiMagnatow = 0;
    }

    private static int wymagajDodatniej(int kwota) {
        if (kwota <= 0) {
            throw new IllegalArgumentException("Kwota łapówki musi być dodatnia: " + kwota);
        }
        return kwota;
    }

    public int id() {
        return id;
    }

    public String imie() {
        return imie;
    }

    public String herb() {
        return herb;
    }

    public Frakcja frakcja() {
        return frakcja;
    }

    public int lojalnoscWobecKrola() {
        return lojalnoscWobecKrola;
    }

    public double przekupnosc() {
        return przekupnosc;
    }

    public int lapowkiKrola() {
        return lapowkiKrola;
    }

    public int lapowkiMagnatow() {
        return lapowkiMagnatow;
    }

    /** Np. "Jan Zamoyski h. Jelita". */
    public String pelneImie() {
        return imie + " h. " + herb;
    }

    /** Tożsamość posła to jego id; stan (łapówki) się zmienia, a poseł pozostaje tym samym posłem. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Posel inny && id == inny.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Posel[" + id + ", " + pelneImie() + ", " + frakcja + "]";
    }
}
