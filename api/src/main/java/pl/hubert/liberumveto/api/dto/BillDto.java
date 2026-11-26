package pl.hubert.liberumveto.api.dto;

import java.util.Map;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.BillType;
import pl.hubert.liberumveto.domain.model.Faction;

/**
 * A bill as seen by API clients.
 */
public record BillDto(int id, String title, BillType type, Map<Faction, Integer> impact) {

    public static BillDto from(Bill bill) {
        return new BillDto(bill.id(), bill.title(), bill.type(), bill.impact());
    }
}
