package pl.hubert.liberumveto.console;

import java.text.Normalizer;
import java.util.Locale;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;

/**
 * Zamienia tekst wpisany przez gracza na obiekt {@link Komenda}.
 * Akceptuje słowa z polskimi znakami i bez nich ("posłowie" i "poslowie").
 */
public class ParserKomend {

    public Komenda parsuj(String linia) {
        String[] slowa = linia.strip().split("\\s+");
        String nazwa = normalizuj(slowa[0]);

        return switch (nazwa) {
            case "przekup" -> parsujPrzekup(slowa);
            case "poslowie", "p" -> bezArgumentow(slowa, new Komenda.Poslowie());
            case "statystyki", "s" -> bezArgumentow(slowa, new Komenda.Statystyki());
            case "glosuj", "g" -> bezArgumentow(slowa, new Komenda.Glosuj());
            case "pomoc", "help", "?" -> bezArgumentow(slowa, new Komenda.Pomoc());
            case "wyjdz", "koniec" -> bezArgumentow(slowa, new Komenda.Wyjdz());
            case "" -> throw new NieprawidlowyWyborException("Wpisz komendę (pomoc pokaże listę)");
            default -> throw new NieprawidlowyWyborException(
                    "Nieznana komenda: " + slowa[0] + ". Wpisz pomoc, żeby zobaczyć listę");
        };
    }

    /** Numer ustawy z listy (1, 2, 3...). */
    public int parsujNumer(String linia) {
        return liczba(linia.strip(), "numer ustawy");
    }

    private Komenda parsujPrzekup(String[] slowa) {
        if (slowa.length != 3) {
            throw new NieprawidlowyWyborException("Użycie: przekup <id posła> <kwota>");
        }
        return new Komenda.Przekup(liczba(slowa[1], "id posła"), liczba(slowa[2], "kwota"));
    }

    private static Komenda bezArgumentow(String[] slowa, Komenda komenda) {
        if (slowa.length > 1) {
            throw new NieprawidlowyWyborException("Komenda " + slowa[0] + " nie przyjmuje argumentów");
        }
        return komenda;
    }

    private static int liczba(String tekst, String coTo) {
        try {
            return Integer.parseInt(tekst);
        } catch (NumberFormatException e) {
            // tłumaczymy techniczny wyjątek na wyjątek domenowy, zachowując przyczynę (cause)
            throw new NieprawidlowyWyborException("To nie jest poprawna liczba (" + coTo + "): " + tekst, e);
        }
    }

    /** "Głosuj" → "glosuj": małe litery i usunięcie polskich znaków diakrytycznych. */
    static String normalizuj(String slowo) {
        String bezAkcentow = Normalizer.normalize(slowo.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // "ł" nie rozkłada się na literę + znak diakrytyczny, więc podmieniamy ręcznie
        return bezAkcentow.replace('ł', 'l');
    }
}
