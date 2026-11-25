package pl.hubert.liberumveto.domain.wyjatki;

import java.io.Serial;

/**
 * Nie udało się zapisać albo wczytać stanu gry (brak pliku, brak uprawnień, uszkodzony JSON).
 * Checked: problemy z plikami zdarzają się nawet w poprawnym programie, więc wywołujący
 * musi świadomie zdecydować, co wtedy zrobić.
 */
public class BladZapisuGryException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public BladZapisuGryException(String message, Throwable cause) {
        super(message, cause);
    }
}
