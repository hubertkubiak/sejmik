package pl.hubert.liberumveto.console.save;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import pl.hubert.liberumveto.domain.exception.SaveGameException;
import pl.hubert.liberumveto.domain.game.GameState;

/**
 * Saves and loads the game state as a JSON file (F1, F8).
 * Exception messages are shown to the player, so they are in Polish.
 */
public class SaveFile {

    private final Path path;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public SaveFile(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public boolean exists() {
        return Files.isRegularFile(path);
    }

    public Path path() {
        return path;
    }

    /**
     * Writes to a temporary file first and then swaps it in. If the program dies midway,
     * the previous save stays intact.
     */
    public void save(GameState state) throws SaveGameException {
        Path directory = path.toAbsolutePath().getParent();
        Path tempFile = null;
        try {
            Files.createDirectories(directory);
            tempFile = Files.createTempFile(directory, "save", ".tmp");
            mapper.writeValue(tempFile.toFile(), SaveMapper.toDto(state));
            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(tempFile);
            throw new SaveGameException("Nie udało się zapisać gry do " + path, e);
        }
    }

    public GameState load() throws SaveGameException {
        try {
            SavedGame dto = mapper.readValue(path.toFile(), SavedGame.class);
            return SaveMapper.fromDto(dto);
        } catch (JacksonException | IllegalArgumentException | NullPointerException e) {
            // corrupted or mismatched JSON
            throw new SaveGameException("Plik zapisu " + path + " jest uszkodzony", e);
        } catch (IOException e) {
            throw new SaveGameException("Nie udało się wczytać gry z " + path, e);
        }
    }

    private static void deleteQuietly(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
            // cleaning up is the best we can do; the save error is reported to the caller anyway
        }
    }
}
