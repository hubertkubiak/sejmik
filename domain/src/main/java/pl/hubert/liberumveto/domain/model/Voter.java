package pl.hubert.liberumveto.domain.model;

import java.util.Random;

/**
 * Anyone who can cast a vote on a bill.
 * The interface describes "what" (the contract); the implementing class decides "how".
 */
public interface Voter {

    Vote vote(Bill bill, Random random);
}
