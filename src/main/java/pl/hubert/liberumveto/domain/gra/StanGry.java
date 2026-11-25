package pl.hubert.liberumveto.domain.gra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pl.hubert.liberumveto.domain.gracz.Krol;
import pl.hubert.liberumveto.domain.gracz.Magnat;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;

/**
 * Cały stan rozgrywki w jednym miejscu, bez logiki reguł (tę ma {@link Gra}).
 * Wszystko, co tu jest, musi trafić do pliku zapisu, żeby gra wczytała się bez utraty stanu.
 */
public class StanGry {

    private int numerSejmu;
    private final List<Posel> poslowie;
    private final Krol krol;
    private final Magnat magnat;
    private final List<WynikSejmu> historia;
    private List<Ustawa> ustawyDoWyboru;
    private Ustawa wybranaUstawa;

    public StanGry(int numerSejmu, List<Posel> poslowie, Krol krol, Magnat magnat,
                   List<WynikSejmu> historia, List<Ustawa> ustawyDoWyboru, Ustawa wybranaUstawa) {
        if (numerSejmu < 1) {
            throw new IllegalArgumentException("Numer sejmu zaczyna się od 1: " + numerSejmu);
        }
        this.numerSejmu = numerSejmu;
        // posłowie są mutowalni (łapówki), ale sama lista posłów się nie zmienia
        this.poslowie = List.copyOf(poslowie);
        this.krol = Objects.requireNonNull(krol, "krol");
        this.magnat = Objects.requireNonNull(magnat, "magnat");
        // historia rośnie w trakcie gry, więc trzymamy własną, mutowalną kopię
        this.historia = new ArrayList<>(historia);
        this.ustawyDoWyboru = List.copyOf(ustawyDoWyboru);
        this.wybranaUstawa = wybranaUstawa;
    }

    public int numerSejmu() {
        return numerSejmu;
    }

    public List<Posel> poslowie() {
        return poslowie;
    }

    public Posel posel(int id) {
        return poslowie.stream()
                .filter(posel -> posel.id() == id)
                .findFirst()
                .orElseThrow(() -> new NieprawidlowyWyborException("Nie ma posła o id " + id));
    }

    public Krol krol() {
        return krol;
    }

    public Magnat magnat() {
        return magnat;
    }

    /** Kopia tylko do odczytu: z zewnątrz nie da się dopisać fałszywego wyniku. */
    public List<WynikSejmu> historia() {
        return List.copyOf(historia);
    }

    public List<Ustawa> ustawyDoWyboru() {
        return ustawyDoWyboru;
    }

    public Optional<Ustawa> wybranaUstawa() {
        return Optional.ofNullable(wybranaUstawa);
    }

    // Mutatory są package-private: stan może zmieniać tylko Gra (ten sam pakiet).

    void dodajDoHistorii(WynikSejmu wynik) {
        historia.add(Objects.requireNonNull(wynik));
    }

    void ustawUstawyDoWyboru(List<Ustawa> ustawy) {
        this.ustawyDoWyboru = List.copyOf(ustawy);
    }

    void ustawWybranaUstawe(Ustawa ustawa) {
        this.wybranaUstawa = ustawa;
    }

    void nastepnySejm() {
        numerSejmu++;
    }
}
