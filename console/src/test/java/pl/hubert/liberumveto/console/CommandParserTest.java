package pl.hubert.liberumveto.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;

class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void parsesBribeWithArguments() {
        assertThat(parser.parse("  przekup 7   30 ")).isEqualTo(new Command.Bribe(7, 30));
    }

    @ParameterizedTest
    @ValueSource(strings = {"posłowie", "poslowie", "POSŁOWIE", "p"})
    void acceptsPolishCharactersAndAnyCase(String text) {
        assertThat(parser.parse(text)).isInstanceOf(Command.ShowDeputies.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"głosuj", "glosuj", "Głosuj", "g"})
    void recognizesVoteCommand(String text) {
        assertThat(parser.parse(text)).isEqualTo(new Command.CallVote());
    }

    @Test
    void recognizesStatisticsAndSave() {
        assertThat(parser.parse("statystyki")).isEqualTo(new Command.ShowStatistics());
        assertThat(parser.parse("zapisz")).isEqualTo(new Command.Save());
    }

    @Test
    void recognizesHelpAndQuit() {
        assertThat(parser.parse("pomoc")).isEqualTo(new Command.Help());
        assertThat(parser.parse("wyjdź")).isEqualTo(new Command.Quit());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "taniec", "przekup", "przekup 7", "przekup x 30", "przekup 7 dużo", "glosuj teraz"})
    void rejectsInvalidInput(String text) {
        assertThatThrownBy(() -> parser.parse(text)).isInstanceOf(InvalidChoiceException.class);
    }

    @Test
    void numberErrorKeepsTheCause() {
        assertThatThrownBy(() -> parser.parseNumber("dwa"))
                .isInstanceOf(InvalidChoiceException.class)
                .hasCauseInstanceOf(NumberFormatException.class);
        assertThat(parser.parseNumber(" 2 ")).isEqualTo(2);
    }
}
