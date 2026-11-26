package pl.hubert.liberumveto.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Body of {@code PUT /api/games/{id}/selected-bill}.
 */
public record SelectBillRequest(
        @NotNull(message = "Podaj id ustawy") @Positive(message = "Id ustawy musi być dodatnie") Integer billId) {
}
