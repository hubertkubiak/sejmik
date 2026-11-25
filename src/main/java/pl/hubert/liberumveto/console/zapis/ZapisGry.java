package pl.hubert.liberumveto.console.zapis;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import pl.hubert.liberumveto.domain.gra.StanGry;
import pl.hubert.liberumveto.domain.wyjatki.BladZapisuGryException;

/**
 * Zapis i odczyt stanu gry w pliku JSON (F1, F8).
 */
public class ZapisGry {

    private final Path plik;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public ZapisGry(Path plik) {
        this.plik = Objects.requireNonNull(plik, "plik");
    }

    public boolean istnieje() {
        return Files.isRegularFile(plik);
    }

    public Path plik() {
        return plik;
    }

    /**
     * Zapis najpierw do pliku tymczasowego, a potem podmiana. Jeśli program padnie w trakcie,
     * poprzedni zapis zostaje nienaruszony.
     */
    public void zapisz(StanGry stan) throws BladZapisuGryException {
        Path katalog = plik.toAbsolutePath().getParent();
        Path tymczasowy = null;
        try {
            Files.createDirectories(katalog);
            tymczasowy = Files.createTempFile(katalog, "save", ".tmp");
            mapper.writeValue(tymczasowy.toFile(), MapperZapisu.doDto(stan));
            Files.move(tymczasowy, plik, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            usunPoCichu(tymczasowy);
            throw new BladZapisuGryException("Nie udało się zapisać gry do " + plik, e);
        }
    }

    public StanGry wczytaj() throws BladZapisuGryException {
        try {
            ZapisGryDto dto = mapper.readValue(plik.toFile(), ZapisGryDto.class);
            return MapperZapisu.zDto(dto);
        } catch (JacksonException | IllegalArgumentException | NullPointerException e) {
            // uszkodzony lub niepasujący JSON
            throw new BladZapisuGryException("Plik zapisu " + plik + " jest uszkodzony", e);
        } catch (IOException e) {
            throw new BladZapisuGryException("Nie udało się wczytać gry z " + plik, e);
        }
    }

    private static void usunPoCichu(Path tymczasowy) {
        if (tymczasowy == null) {
            return;
        }
        try {
            Files.deleteIfExists(tymczasowy);
        } catch (IOException ignored) {
            // sprzątanie jest najlepszym, co możemy zrobić; błąd zapisu i tak zgłaszamy wyżej
        }
    }
}
