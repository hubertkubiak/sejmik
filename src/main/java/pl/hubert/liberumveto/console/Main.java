package pl.hubert.liberumveto.console;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import pl.hubert.liberumveto.console.zapis.ZapisGry;
import pl.hubert.liberumveto.domain.gra.GeneratorPoslow;
import pl.hubert.liberumveto.domain.gra.Gra;
import pl.hubert.liberumveto.domain.gra.KatalogUstaw;

/**
 * Punkt wejścia. Tutaj, i tylko tutaj, tworzymy zależności i składamy z nich aplikację
 * ("composition root"). Pozostałe klasy dostają wszystko przez konstruktory.
 */
public final class Main {

    private static final Path PLIK_ZAPISU = Path.of("save.json");

    private Main() {
    }

    public static void main(String[] args) {
        var random = new Random();
        var katalog = KatalogUstaw.domyslny();
        var generator = new GeneratorPoslow(
                ZasobyTekstowe.wczytajLinie("imiona.txt"),
                ZasobyTekstowe.wczytajLinie("herby.txt"),
                random);

        var wejscie = new Scanner(System.in, kodowanieKonsoli());
        var konsola = new KonsolaUI(wejscie, System.out, new ZapisGry(PLIK_ZAPISU));

        // lambdy jako "przepisy" na grę: konsola sama zdecyduje, którego użyć
        konsola.uruchom(
                () -> Gra.nowa(generator, katalog, random),
                stan -> new Gra(stan, katalog, random));
    }

    /** W Windows konsola często nie używa UTF-8, więc pytamy ją o jej kodowanie. */
    private static Charset kodowanieKonsoli() {
        Console konsola = System.console();
        return konsola != null ? konsola.charset() : Charset.defaultCharset();
    }
}
