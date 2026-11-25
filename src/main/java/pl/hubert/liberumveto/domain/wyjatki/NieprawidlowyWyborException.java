package pl.hubert.liberumveto.domain.wyjatki;

import java.io.Serial;

/**
 * Błędne dane od użytkownika: nieznana komenda, nieistniejący poseł, zły numer ustawy.
 */
public class NieprawidlowyWyborException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NieprawidlowyWyborException(String message) {
        super(message);
    }

    public NieprawidlowyWyborException(String message, Throwable cause) {
        super(message, cause);
    }
}
