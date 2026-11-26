package pl.hubert.sejmik.domain.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.BillType;
import pl.hubert.sejmik.domain.model.Faction;

/**
 * The pool of bills from which a few are drawn every turn.
 */
public class BillCatalog {

    private final List<Bill> pool;

    public BillCatalog(List<Bill> pool) {
        this.pool = List.copyOf(pool);
    }

    /** The default pool. The numbers are the impact on the royal, magnate and independent factions. */
    public static BillCatalog defaultCatalog() {
        return new BillCatalog(List.of(
                bill(1, "Podatek na wojsko kwarciane", BillType.TAX, 15, -25, -5),
                bill(2, "Pogłówne generalne", BillType.TAX, 10, -15, -15),
                bill(3, "Pobór łanowy na wojnę z Moskwą", BillType.CONSCRIPTION, 20, -20, 0),
                bill(4, "Powiększenie gwardii królewskiej", BillType.CONSCRIPTION, 25, -30, -10),
                bill(5, "Potwierdzenie przywilejów szlacheckich", BillType.NOBLE_PRIVILEGE, -10, 20, 15),
                bill(6, "Zniesienie ceł na spław zboża", BillType.NOBLE_PRIVILEGE, 0, 15, 10),
                bill(7, "Reforma skarbu koronnego", BillType.TREASURY_REFORM, 20, -20, 5),
                bill(8, "Komisja menniczna", BillType.TREASURY_REFORM, 15, -10, 0)));
    }

    private static Bill bill(int id, String title, BillType type, int royal, int magnate, int independent) {
        return new Bill(id, title, type, Map.of(
                Faction.ROYAL, royal,
                Faction.MAGNATE, magnate,
                Faction.INDEPENDENT, independent));
    }

    /** Draws {@code count} distinct bills from the pool. */
    public List<Bill> draw(int count, Random random) {
        if (count > pool.size()) {
            throw new IllegalArgumentException("The pool only has " + pool.size() + " bills");
        }
        List<Bill> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);
        return List.copyOf(copy.subList(0, count));
    }

    public List<Bill> all() {
        return pool;
    }
}
