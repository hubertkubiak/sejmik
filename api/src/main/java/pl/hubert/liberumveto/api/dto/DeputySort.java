package pl.hubert.liberumveto.api.dto;

/**
 * Sort order for the deputy list ({@code ?sort=support}). Spring Boot converts the query parameter
 * to the enum case-insensitively, so "support" and "SUPPORT" both work.
 */
public enum DeputySort {
    ID,
    SUPPORT,
    CORRUPTIBILITY
}
