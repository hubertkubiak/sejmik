package pl.hubert.liberumveto.console.zapis;

import java.util.List;
import java.util.Optional;
import pl.hubert.liberumveto.console.zapis.ZapisGryDto.GraczDto;
import pl.hubert.liberumveto.console.zapis.ZapisGryDto.PoselDto;
import pl.hubert.liberumveto.console.zapis.ZapisGryDto.UstawaDto;
import pl.hubert.liberumveto.console.zapis.ZapisGryDto.WynikSejmuDto;
import pl.hubert.liberumveto.domain.gra.StanGry;
import pl.hubert.liberumveto.domain.gra.WynikSejmu;
import pl.hubert.liberumveto.domain.gracz.Krol;
import pl.hubert.liberumveto.domain.gracz.Magnat;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;

/**
 * Tłumaczy model domenowy na DTO zapisu i z powrotem.
 */
final class MapperZapisu {

    private MapperZapisu() {
    }

    static ZapisGryDto doDto(StanGry stan) {
        return new ZapisGryDto(
                ZapisGryDto.AKTUALNA_WERSJA,
                stan.numerSejmu(),
                stan.poslowie().stream().map(MapperZapisu::doDto).toList(),
                new GraczDto(stan.krol().nazwa(), stan.krol().budzet()),
                new GraczDto(stan.magnat().nazwa(), stan.magnat().budzet()),
                stan.historia().stream().map(MapperZapisu::doDto).toList(),
                stan.ustawyDoWyboru().stream().map(MapperZapisu::doDto).toList(),
                stan.wybranaUstawa().map(MapperZapisu::doDto).orElse(null));
    }

    static StanGry zDto(ZapisGryDto dto) {
        if (dto.wersja() != ZapisGryDto.AKTUALNA_WERSJA) {
            throw new IllegalArgumentException("Nieobsługiwana wersja zapisu: " + dto.wersja());
        }
        List<Posel> poslowie = dto.poslowie().stream().map(MapperZapisu::zDto).toList();
        return new StanGry(
                dto.numerSejmu(),
                poslowie,
                new Krol(dto.krol().nazwa(), dto.krol().budzet()),
                new Magnat(dto.magnat().nazwa(), dto.magnat().budzet()),
                dto.historia().stream().map(wynik -> zDto(wynik, poslowie)).toList(),
                dto.ustawyDoWyboru().stream().map(MapperZapisu::zDto).toList(),
                dto.wybranaUstawa() == null ? null : zDto(dto.wybranaUstawa()));
    }

    private static PoselDto doDto(Posel p) {
        return new PoselDto(p.id(), p.imie(), p.herb(), p.frakcja(), p.lojalnoscWobecKrola(),
                p.przekupnosc(), p.lapowkiKrola(), p.lapowkiMagnatow());
    }

    private static Posel zDto(PoselDto p) {
        return new Posel(p.id(), p.imie(), p.herb(), p.frakcja(), p.lojalnoscWobecKrola(),
                p.przekupnosc(), p.lapowkiKrola(), p.lapowkiMagnatow());
    }

    private static UstawaDto doDto(Ustawa u) {
        return new UstawaDto(u.id(), u.tytul(), u.typ(), u.wplyw());
    }

    private static Ustawa zDto(UstawaDto u) {
        return new Ustawa(u.id(), u.tytul(), u.typ(), u.wplyw());
    }

    private static WynikSejmuDto doDto(WynikSejmu w) {
        WynikGlosowania wynik = w.wynik();
        return new WynikSejmuDto(w.numer(), doDto(w.ustawa()), wynik.liczbaGlosow(), wynik.przyjeta(),
                wynik.zrywajacy().map(Posel::id).orElse(null));
    }

    /** Zrywającego szukamy wśród wczytanych posłów, żeby historia wskazywała na te same obiekty. */
    private static WynikSejmu zDto(WynikSejmuDto w, List<Posel> poslowie) {
        Optional<Posel> zrywajacy = Optional.ofNullable(w.zrywajacyId())
                .map(id -> poslowie.stream()
                        .filter(p -> p.id() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Nieznany zrywający: " + id)));
        return new WynikSejmu(w.numer(), zDto(w.ustawa()),
                new WynikGlosowania(w.liczbaGlosow(), w.przyjeta(), zrywajacy));
    }
}
