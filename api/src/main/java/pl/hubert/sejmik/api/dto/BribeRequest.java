package pl.hubert.sejmik.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Body of {@code POST /api/games/{id}/bribes}.
 *
 * <p>Bean Validation annotations describe the rules declaratively; {@code @Valid} in the controller
 * makes Spring check them before the method runs. The messages reach the player, so they are Polish.
 * Wrapper types ({@code Integer}) let us tell "missing" (null) apart from 0.
 */
public record BribeRequest(
        @NotNull(message = "Podaj id posła") @Positive(message = "Id posła musi być dodatnie") Integer deputyId,
        @NotNull(message = "Podaj kwotę łapówki") @Positive(message = "Kwota łapówki musi być dodatnia") Integer amount) {
}
