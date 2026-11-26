package pl.hubert.liberumveto.console;

import java.text.Normalizer;
import java.util.Locale;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;

/**
 * Turns the text typed by the player into a {@link Command} object.
 * The command words are Polish (they are part of the user interface) and are accepted
 * with or without Polish diacritics ("posłowie" and "poslowie").
 */
public class CommandParser {

    public Command parse(String line) {
        String[] words = line.strip().split("\\s+");
        String name = normalize(words[0]);

        return switch (name) {
            case "przekup" -> parseBribe(words);
            case "poslowie", "p" -> withoutArguments(words, new Command.ShowDeputies());
            case "statystyki", "s" -> withoutArguments(words, new Command.ShowStatistics());
            case "glosuj", "g" -> withoutArguments(words, new Command.CallVote());
            case "zapisz" -> withoutArguments(words, new Command.Save());
            case "pomoc", "help", "?" -> withoutArguments(words, new Command.Help());
            case "wyjdz", "koniec" -> withoutArguments(words, new Command.Quit());
            case "" -> throw new InvalidChoiceException("Wpisz komendę (pomoc pokaże listę)");
            default -> throw new InvalidChoiceException(
                    "Nieznana komenda: " + words[0] + ". Wpisz pomoc, żeby zobaczyć listę");
        };
    }

    /** A bill number from the list (1, 2, 3...). */
    public int parseNumber(String line) {
        return number(line.strip(), "numer ustawy");
    }

    private Command parseBribe(String[] words) {
        if (words.length != 3) {
            throw new InvalidChoiceException("Użycie: przekup <id posła> <kwota>");
        }
        return new Command.Bribe(number(words[1], "id posła"), number(words[2], "kwota"));
    }

    private static Command withoutArguments(String[] words, Command command) {
        if (words.length > 1) {
            throw new InvalidChoiceException("Komenda " + words[0] + " nie przyjmuje argumentów");
        }
        return command;
    }

    /** @param whatItIs what the number means, in Polish, for the error message */
    private static int number(String text, String whatItIs) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // translate a technical exception into a domain one, keeping the original cause
            throw new InvalidChoiceException("To nie jest poprawna liczba (" + whatItIs + "): " + text, e);
        }
    }

    /** "Głosuj" → "glosuj": lower case and Polish diacritics removed. */
    static String normalize(String word) {
        String withoutAccents = Normalizer.normalize(word.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // "ł" does not decompose into a letter plus a combining mark, so we replace it by hand
        return withoutAccents.replace('ł', 'l');
    }
}
