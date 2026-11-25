package pl.hubert.liberumveto.console.zapis;

import java.util.List;
import java.util.Map;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.TypUstawy;

/**
 * Format pliku zapisu. Płaskie rekordy bez logiki (DTO: Data Transfer Object), oddzielone od domeny:
 * zmiana klas domenowych nie psuje starych zapisów, a domena nie zależy od Jacksona.
 *
 * @param wersja wersja formatu, żeby w przyszłości móc migrować stare pliki
 */
public record ZapisGryDto(
        int wersja,
        int numerSejmu,
        List<PoselDto> poslowie,
        GraczDto krol,
        GraczDto magnat,
        List<WynikSejmuDto> historia,
        List<UstawaDto> ustawyDoWyboru,
        UstawaDto wybranaUstawa) {

    public static final int AKTUALNA_WERSJA = 1;

    public record PoselDto(int id, String imie, String herb, Frakcja frakcja, int lojalnoscWobecKrola,
                           double przekupnosc, int lapowkiKrola, int lapowkiMagnatow) {
    }

    public record GraczDto(String nazwa, int budzet) {
    }

    public record UstawaDto(int id, String tytul, TypUstawy typ, Map<Frakcja, Integer> wplyw) {
    }

    /** @param zrywajacyId id posła, który zerwał sejm, albo null */
    public record WynikSejmuDto(int numer, UstawaDto ustawa, Map<Glos, Long> liczbaGlosow,
                                boolean przyjeta, Integer zrywajacyId) {
    }
}
