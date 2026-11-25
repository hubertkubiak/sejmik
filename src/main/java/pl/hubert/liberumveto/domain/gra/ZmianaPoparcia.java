package pl.hubert.liberumveto.domain.gra;

import pl.hubert.liberumveto.domain.model.Posel;

/**
 * Efekt łapówki Króla, np. "Jan Zamoyski h. Jelita: poparcie 12 → 34".
 */
public record ZmianaPoparcia(Posel posel, int przed, int po) {
}
