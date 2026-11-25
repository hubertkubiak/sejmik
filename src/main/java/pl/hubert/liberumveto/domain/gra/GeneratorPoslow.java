package pl.hubert.liberumveto.domain.gra;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;

/**
 * Tworzy posłów z losowymi atrybutami (F2).
 * Nie czyta plików: imiona i herby dostaje gotowe, więc domena nie zależy od I/O.
 */
public class GeneratorPoslow {

    private final List<String> imiona;
    private final List<String> herby;
    private final Random random;

    public GeneratorPoslow(List<String> imiona, List<String> herby, Random random) {
        if (imiona.isEmpty() || herby.isEmpty()) {
            throw new IllegalArgumentException("Potrzebne jest co najmniej jedno imię i jeden herb");
        }
        this.imiona = List.copyOf(imiona);
        this.herby = List.copyOf(herby);
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Posel> generuj(int liczba) {
        return IntStream.rangeClosed(1, liczba)
                .mapToObj(this::nowyPosel)
                .toList();
    }

    private Posel nowyPosel(int id) {
        Frakcja frakcja = losujFrakcje();
        // switch jako wyrażenie (Java 14+): każda gałąź zwraca wartość, a kompilator
        // sprawdza, czy obsłużyliśmy wszystkie stałe enuma
        int lojalnosc = switch (frakcja) {
            case KROLEWSKA -> losujZZakresu(55, 85);
            case NIEZALEZNA -> losujZZakresu(35, 65);
            case MAGNACKA -> losujZZakresu(20, 45);
        };
        double przekupnosc = Math.round(random.nextDouble(0.2, 1.0) * 100) / 100.0;
        return new Posel(id, losujZ(imiona), losujZ(herby), frakcja, lojalnosc, przekupnosc);
    }

    private Frakcja losujFrakcje() {
        int rzut = random.nextInt(100);
        if (rzut < 35) {
            return Frakcja.KROLEWSKA;
        }
        if (rzut < 65) {
            return Frakcja.NIEZALEZNA;
        }
        return Frakcja.MAGNACKA;
    }

    private int losujZZakresu(int odWlacznie, int doWlacznie) {
        return random.nextInt(odWlacznie, doWlacznie + 1);
    }

    private String losujZ(List<String> lista) {
        return lista.get(random.nextInt(lista.size()));
    }
}
