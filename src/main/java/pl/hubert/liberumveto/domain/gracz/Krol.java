package pl.hubert.liberumveto.domain.gracz;

import pl.hubert.liberumveto.domain.model.Posel;

/**
 * Gracz sterowany przez użytkownika. Jego łapówki podnoszą poparcie dla ustawy.
 */
public class Krol extends Gracz {

    public Krol(String nazwa, int budzet) {
        super(nazwa, budzet);
    }

    @Override
    protected void przekazLapowke(Posel posel, int kwota) {
        posel.przyjmijLapowkeOdKrola(kwota);
    }
}
