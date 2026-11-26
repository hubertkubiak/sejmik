package pl.hubert.sejmik.api.dto;

/**
 * Response to a bill selection. Who exactly the magnates bribed stays secret, only how many.
 */
public record BillSelectionDto(BillDto bill, int bribedDeputies) {
}
