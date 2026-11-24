package hu.framingham.risk.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** One saved assessment of the logged-in user.
 * {@code probability} is in [0, 1]; {@code contributions} are log-odds per input feature. */
public record JournalEntryResponse(
        UUID id,
        Instant createdAt,
        String model,
        String modelVersion,
        Double probability,
        Map<String, Double> contributions,
        RiskValuesResponse values
) {}
