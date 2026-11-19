import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Validators {
    private Validators() {
    }

    private static final Set<String> STATES;

    static {
        Set<String> states = new LinkedHashSet<>();
        Collections.addAll(states,
                "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC",
                "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
                "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
                "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH",
                "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT",
                "VT", "VA", "WA", "WV", "WI", "WY");
        STATES = Collections.unmodifiableSet(states);
    }

    public static boolean isValidNpi(Object npi) {
        String n = npi == null ? "" : String.valueOf(npi).trim();
        if (n.length() != 10) {
            return false;
        }
        for (char c : n.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }

        int sum = 24;
        char[] reversed = new StringBuilder(n).reverse().toString().toCharArray();
        for (int i = 0; i < reversed.length; i++) {
            int digit = Character.digit(reversed[i], 10);
            if (i % 2 == 0) {
                sum += digit;
            } else {
                int doubled = digit * 2;
                sum += doubled / 10 + doubled % 10;
            }
        }
        return sum % 10 == 0;
    }

    public static boolean isValidDateRange(String dateOfService, String dateSubmitted)
            throws DateTimeParseException {
        LocalDate service = LocalDate.parse(dateOfService);
        LocalDate submitted = LocalDate.parse(dateSubmitted);
        return !service.isBefore(submitted);
    }

    public static boolean isValidState(String state) {
        if (state == null) {
            return false;
        }
        String trimmed = state.trim();
        return !trimmed.isEmpty() && STATES.contains(trimmed.toUpperCase());
    }

    public static List<String> validateClaim(Map<String, ?> row) {
        List<String> problems = new ArrayList<>();

        Object npi = row.containsKey("provider_npi") ? row.get("provider_npi") : "";
        if (!isValidNpi(npi)) {
            problems.add("Provider NPI is invalid.");
        }

        try {
            boolean datesOk = isValidDateRange(
                    valueAsString(row.get("date_of_service")),
                    valueAsString(row.get("date_submitted")));
            if (!datesOk) {
                problems.add("Date of service cannot be after date submitted.");
            }
        } catch (DateTimeParseException | NullPointerException exception) {
            problems.add("Service and submission dates must use YYYY-MM-DD.");
        }

        if (!isValidState(valueAsString(row.get("state")))) {
            problems.add("State must be a two-letter US state code.");
        }

        return problems;
    }

    private static String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
