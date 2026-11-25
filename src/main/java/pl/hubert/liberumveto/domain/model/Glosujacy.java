package pl.hubert.liberumveto.domain.model;

import java.util.Random;

/**
 * Każdy, kto potrafi oddać głos nad ustawą.
 * Interfejs opisuje "co" (kontrakt), a klasa implementująca decyduje "jak".
 */
public interface Glosujacy {

    Glos zaglosuj(Ustawa ustawa, Random random);
}
