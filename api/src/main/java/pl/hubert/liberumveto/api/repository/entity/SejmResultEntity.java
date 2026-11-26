package pl.hubert.liberumveto.api.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One finished Sejm of one game ({@code sejm_results} table). Bills live in the code
 * ({@code BillCatalog}), so only the bill id is stored.
 */
@Entity
@Table(name = "sejm_results")
public class SejmResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    private int number;
    private int billId;
    private long votesFor;
    private long votesAgainst;
    private long vetoes;
    private boolean passed;
    private Integer vetoerDeputyId;

    protected SejmResultEntity() {
    }

    public SejmResultEntity(int number, int billId, long votesFor, long votesAgainst, long vetoes,
                            boolean passed, Integer vetoerDeputyId) {
        this.number = number;
        this.billId = billId;
        this.votesFor = votesFor;
        this.votesAgainst = votesAgainst;
        this.vetoes = vetoes;
        this.passed = passed;
        this.vetoerDeputyId = vetoerDeputyId;
    }

    void setGame(GameEntity game) {
        this.game = game;
    }

    public int getNumber() {
        return number;
    }

    public int getBillId() {
        return billId;
    }

    public long getVotesFor() {
        return votesFor;
    }

    public long getVotesAgainst() {
        return votesAgainst;
    }

    public long getVetoes() {
        return vetoes;
    }

    public boolean isPassed() {
        return passed;
    }

    public Integer getVetoerDeputyId() {
        return vetoerDeputyId;
    }
}
