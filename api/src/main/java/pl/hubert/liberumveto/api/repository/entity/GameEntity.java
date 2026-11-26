package pl.hubert.liberumveto.api.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One row in the {@code games} table. A JPA entity is a mutable class with a no-argument constructor,
 * because Hibernate creates the object first and fills the fields afterwards; that is why we do not
 * use the domain {@code Game} (or a record) here and map between the two instead.
 */
@Entity
@Table(name = "games")
public class GameEntity {

    /** We generate the UUID ourselves (it is the game id in URLs), so no {@code @GeneratedValue}. */
    @Id
    private UUID id;

    /**
     * Optimistic locking: every UPDATE runs "... WHERE version = ?" and increments the version.
     * If another request changed the game in the meantime, nothing matches and Spring throws
     * {@code ObjectOptimisticLockingFailureException} instead of silently overwriting the other change.
     */
    @Version
    private Long version;

    private int sejmNumber;
    private int kingBudget;
    private int magnateBudget;
    private Integer selectedBillId;

    /** A list of simple values in its own table: game_available_bills(game_id, position, bill_id). */
    @ElementCollection
    @CollectionTable(name = "game_available_bills", joinColumns = @JoinColumn(name = "game_id"))
    @OrderColumn(name = "position")
    @Column(name = "bill_id")
    private List<Integer> availableBillIds = new ArrayList<>();

    /**
     * One game, many deputies. {@code mappedBy}: the foreign key lives on the deputy side.
     * {@code cascade = ALL}: saving the game saves its deputies. {@code orphanRemoval}: a deputy removed
     * from the list is deleted from the database.
     */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("deputyId")
    private List<DeputyEntity> deputies = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number")
    private List<SejmResultEntity> history = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    /** Required by JPA; protected so application code uses the public constructor. */
    protected GameEntity() {
    }

    public GameEntity(UUID id) {
        this.id = id;
        this.createdAt = Instant.now();
    }

    /** Keeps both sides of the relationship in sync: the list here and the deputy's {@code game}. */
    public void addDeputy(DeputyEntity deputy) {
        deputy.setGame(this);
        deputies.add(deputy);
    }

    public void addSejmResult(SejmResultEntity result) {
        result.setGame(this);
        history.add(result);
    }

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public int getSejmNumber() {
        return sejmNumber;
    }

    public void setSejmNumber(int sejmNumber) {
        this.sejmNumber = sejmNumber;
    }

    public int getKingBudget() {
        return kingBudget;
    }

    public void setKingBudget(int kingBudget) {
        this.kingBudget = kingBudget;
    }

    public int getMagnateBudget() {
        return magnateBudget;
    }

    public void setMagnateBudget(int magnateBudget) {
        this.magnateBudget = magnateBudget;
    }

    public Integer getSelectedBillId() {
        return selectedBillId;
    }

    public void setSelectedBillId(Integer selectedBillId) {
        this.selectedBillId = selectedBillId;
    }

    public List<Integer> getAvailableBillIds() {
        return availableBillIds;
    }

    public void setAvailableBillIds(List<Integer> billIds) {
        // modify the existing collection: Hibernate tracks changes on the instance it gave us
        availableBillIds.clear();
        availableBillIds.addAll(billIds);
    }

    public List<DeputyEntity> getDeputies() {
        return deputies;
    }

    public List<SejmResultEntity> getHistory() {
        return history;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
