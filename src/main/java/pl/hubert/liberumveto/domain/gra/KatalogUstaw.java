package pl.hubert.liberumveto.domain.gra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.TypUstawy;
import pl.hubert.liberumveto.domain.model.Ustawa;

/**
 * Pula projektów ustaw, z której w każdej turze losujemy kilka do wyboru.
 */
public class KatalogUstaw {

    private final List<Ustawa> pula;

    public KatalogUstaw(List<Ustawa> pula) {
        this.pula = List.copyOf(pula);
    }

    /** Domyślna pula ustaw. Liczby to wpływ na frakcje: królewską, magnacką, niezależną. */
    public static KatalogUstaw domyslny() {
        return new KatalogUstaw(List.of(
                ustawa(1, "Podatek na wojsko kwarciane", TypUstawy.PODATEK, 15, -25, -5),
                ustawa(2, "Pogłówne generalne", TypUstawy.PODATEK, 10, -15, -15),
                ustawa(3, "Pobór łanowy na wojnę z Moskwą", TypUstawy.POBOR_WOJSKA, 20, -20, 0),
                ustawa(4, "Powiększenie gwardii królewskiej", TypUstawy.POBOR_WOJSKA, 25, -30, -10),
                ustawa(5, "Potwierdzenie przywilejów szlacheckich", TypUstawy.PRZYWILEJ_SZLACHECKI, -10, 20, 15),
                ustawa(6, "Zniesienie ceł na spław zboża", TypUstawy.PRZYWILEJ_SZLACHECKI, 0, 15, 10),
                ustawa(7, "Reforma skarbu koronnego", TypUstawy.REFORMA_SKARBU, 20, -20, 5),
                ustawa(8, "Komisja menniczna", TypUstawy.REFORMA_SKARBU, 15, -10, 0)));
    }

    private static Ustawa ustawa(int id, String tytul, TypUstawy typ,
                                 int krolewska, int magnacka, int niezalezna) {
        return new Ustawa(id, tytul, typ, Map.of(
                Frakcja.KROLEWSKA, krolewska,
                Frakcja.MAGNACKA, magnacka,
                Frakcja.NIEZALEZNA, niezalezna));
    }

    /** Losuje {@code ile} różnych ustaw z puli. */
    public List<Ustawa> losuj(int ile, Random random) {
        if (ile > pula.size()) {
            throw new IllegalArgumentException("W puli jest tylko " + pula.size() + " ustaw");
        }
        List<Ustawa> kopia = new ArrayList<>(pula);
        Collections.shuffle(kopia, random);
        return List.copyOf(kopia.subList(0, ile));
    }

    public List<Ustawa> wszystkie() {
        return pula;
    }
}
