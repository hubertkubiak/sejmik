package pl.hubert.sejmik.domain.player;

import pl.hubert.sejmik.domain.model.Deputy;

/**
 * The player controlled by the user. Their bribes raise support for the bill.
 */
public class King extends Player {

    public King(String name, int budget) {
        super(name, budget);
    }

    @Override
    protected void deliverBribe(Deputy deputy, int amount) {
        deputy.acceptKingBribe(amount);
    }
}
