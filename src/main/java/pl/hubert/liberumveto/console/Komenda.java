package pl.hubert.liberumveto.console;

/**
 * Komenda wpisana przez gracza w trakcie sejmu.
 *
 * <p>{@code sealed} + {@code permits}: lista implementacji jest zamknięta i znana kompilatorowi.
 * Dzięki temu {@code switch} po komendzie nie potrzebuje gałęzi {@code default}, a dodanie
 * nowej komendy bez jej obsłużenia kończy się błędem kompilacji.
 */
public sealed interface Komenda
        permits Komenda.Przekup, Komenda.Poslowie, Komenda.Glosuj, Komenda.Pomoc, Komenda.Wyjdz {

    /** {@code przekup <id> <kwota>} */
    record Przekup(int poselId, int kwota) implements Komenda {
    }

    /** {@code poslowie}: tabela posłów z aktualnym poparciem. */
    record Poslowie() implements Komenda {
    }

    /** {@code glosuj}: koniec przekupstw, sejm głosuje. */
    record Glosuj() implements Komenda {
    }

    /** {@code pomoc}: lista komend. */
    record Pomoc() implements Komenda {
    }

    /** {@code wyjdz}: przerwanie gry. */
    record Wyjdz() implements Komenda {
    }
}
