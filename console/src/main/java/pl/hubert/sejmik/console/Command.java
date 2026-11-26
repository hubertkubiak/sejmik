package pl.hubert.sejmik.console;

/**
 * A command typed by the player during a Sejm.
 *
 * <p>{@code sealed} + {@code permits}: the set of implementations is closed and known to the compiler.
 * Thanks to that, a {@code switch} over a command needs no {@code default} branch, and adding a new
 * command without handling it is a compile error.
 */
public sealed interface Command
        permits Command.Bribe, Command.ShowDeputies, Command.ShowStatistics, Command.CallVote,
                Command.Save, Command.Help, Command.Quit {

    /** {@code przekup <id> <kwota>} */
    record Bribe(int deputyId, int amount) implements Command {
    }

    /** {@code posłowie}: a table of deputies with their current support. */
    record ShowDeputies() implements Command {
    }

    /** {@code statystyki}: factions, the most corruptible deputies and those at risk of a veto. */
    record ShowStatistics() implements Command {
    }

    /** {@code głosuj}: bribing is over, the Sejm votes. */
    record CallVote() implements Command {
    }

    /** {@code zapisz}: save the game state to a file. */
    record Save() implements Command {
    }

    /** {@code pomoc}: list of commands. */
    record Help() implements Command {
    }

    /** {@code wyjdź}: quit the game. */
    record Quit() implements Command {
    }
}
