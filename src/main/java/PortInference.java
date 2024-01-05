import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortInference {
    // from $1 to $2
    public static final Pattern FROM_TO_PATTERN = Pattern.compile("(?i)from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    // from $1
    public static final Pattern FROM_PATTERN = Pattern.compile("(?i)from\\s+((?:\\w+\\s)?\\w+)");
    // port from $1
    public static final Pattern PORT_FROM_PATTERN = Pattern.compile("(?i)port\\s+from\\s+((?:\\w+\\s)?\\w+)");
    // port to $2
    public static final Pattern PORT_TO_PATTERN = Pattern.compile("(?i)port\\s+to\\s+((?:\\w+\\s)?\\w+)");
    // $1 > $2
    public static final Pattern GREATER_THAN_PATTERN = Pattern.compile("(?i)(\\b\\w+(?:\\s\\w+)?\\b)\\s*>\\s*(\\b\\w+(?:\\s\\w+)?\\b)");
    public static final Pattern RIDE_TO_PATTERN = Pattern.compile("(?i)ride\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern RIDE_FROM_TO_PATTERN = Pattern.compile("(?i)ride\\s+from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern PICKUP_AT_TO_PATTERN = Pattern.compile("(?i)pickup\\s+at\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern PICKUP_FROM_TO_PATTERN = Pattern.compile("(?i)pickup\\s+from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    // $1 to $2
    public static final Pattern TO_PATTERN = Pattern.compile("(?i)((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");

    public static final Set<Pattern> FROM_TO_PATTERNS = new HashSet<>(Arrays.asList(
            FROM_TO_PATTERN,
            GREATER_THAN_PATTERN,
            RIDE_FROM_TO_PATTERN,
            PICKUP_AT_TO_PATTERN,
            PICKUP_FROM_TO_PATTERN,
            TO_PATTERN
    ));

    public static final Set<Pattern> FROM_PATTERNS = new HashSet<>(Arrays.asList(
            FROM_PATTERN,
            PORT_FROM_PATTERN
    ));

    public static final Set<Pattern> TO_PATTERNS = new HashSet<>(Arrays.asList(
            PORT_TO_PATTERN,
            RIDE_TO_PATTERN
    ));

    public static final List<Pattern> ALL_PATTERNS = new ArrayList<>();
    static {
        ALL_PATTERNS.addAll(FROM_TO_PATTERNS);
        ALL_PATTERNS.addAll(FROM_PATTERNS);
        ALL_PATTERNS.addAll(TO_PATTERNS);
    }

    public static Map<PortZone, Set<String>> zones = new HashMap<>();
    public static Map<String, PortZone> invertedFlattenedZones = new HashMap<>();

    static {
        // Initializing the 'zones' map
        zones.put(PortZone.NORTH_KARANA, new HashSet<>(Arrays.asList("nk", "north karana", "karana", "nkarana", "karanas")));
        zones.put(PortZone.WEST_COMMONLANDS, new HashSet<>(Arrays.asList("wc", "common", "commons", "wcommon", "wcommons", "wcom")));
        zones.put(PortZone.TOXXULIA_FOREST, new HashSet<>(Arrays.asList("tox", "toxx", "toxxulia", "toxulia")));
        zones.put(PortZone.BUTCHER_BLOCK, new HashSet<>(Arrays.asList("bb", "butcher", "bblock", "butcherblock", "butcher block")));
        zones.put(PortZone.MISTY_THICKET, new HashSet<>(Arrays.asList("misty", "misty thicket", "mt")));
        zones.put(PortZone.LAVASTORM, new HashSet<>(Arrays.asList("ls", "lava", "lavastorm", "lstorm")));
        zones.put(PortZone.FEERROTT, new HashSet<>(Arrays.asList("feerrott", "ferot", "feerott", "feerrot", "ferrot", "ferrott", "feerot")));
        zones.put(PortZone.SOUTHERN_RO, new HashSet<>(Arrays.asList("sro", "ro", "south ro", "southern ro")));
        zones.put(PortZone.SUREFALL_GLADE, new HashSet<>(Arrays.asList("sfg", "surefall", "surefall glade", "sfall")));
        zones.put(PortZone.STEAMFONT_MOUNTAINS, new HashSet<>(Arrays.asList("sf", "steam", "steamfont", "steamfont mountains", "steamfront", "stm mtn", "steam mountains")));
        zones.put(PortZone.EAST_KARANA, new HashSet<>(Arrays.asList("ek", "east karana", "ekarana")));
        zones.put(PortZone.NEKTULOS_FOREST, new HashSet<>(Arrays.asList("nek", "nek forest")));
        zones.put(PortZone.WEST_KARANA, new HashSet<>(Arrays.asList("wk", "west karana", "wkarana")));
        zones.put(PortZone.CAZIC_THULE, new HashSet<>(Arrays.asList("ct", "cazic thule", "cazic")));
        zones.put(PortZone.NORTHERN_RO, new HashSet<>(Arrays.asList("nro", "north ro", "northern ro")));
        zones.put(PortZone.GREATER_FAYDARK, new HashSet<>(Arrays.asList("gf", "greater faydark", "faydark", "gfay")));

        // Creating the inverted flattened map
        for (Map.Entry<PortZone, Set<String>> entry : zones.entrySet()) {
            PortZone key = entry.getKey();
            for (String alias : entry.getValue()) {
                invertedFlattenedZones.put(alias.toLowerCase(), key); // Using lower case for case-insensitivity
            }
        }
    }

    public static class PortLocationTuple {
        private PortZone from;
        private PortZone to;

        private PortLocationTuple(PortZone from, PortZone to) {
            this.from = from;
            this.to = to;
        }

        public String toString() {
            String fromString = (from != null) ? from.toString() : "";
            String toString = (to != null) ? to.toString() : "";
            return fromString + " > " + toString;
        }

        public static PortLocationTuple of(PortZone from, PortZone to) {
            return new PortLocationTuple(from, to);
        }

    }

    private static PortZone findZone(String[] parts) {
        // Try combined first
        PortZone zone = invertedFlattenedZones.get(String.join(" ", parts));
        if (zone != null) return zone;

        // Then try individual parts
        for (String part : parts) {
            zone = invertedFlattenedZones.get(part);
            if (zone != null) return zone;
        }

        return null;
    }

    public static PortLocationTuple findLocation(String line) {
        for (Pattern pattern : ALL_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                PortZone fromZone = null;
                PortZone toZone = null;

                if (matcher.groupCount() == 2) {
                    String[] fromParts = matcher.group(1).toLowerCase().trim().split("\\s+");
                    String[] toParts = matcher.group(2).toLowerCase().trim().split("\\s+");
                    fromZone = findZone(fromParts);
                    toZone = findZone(toParts);
                } else if (matcher.groupCount() == 1) {
                    String[] location = matcher.group(1).toLowerCase().trim().split("\\s+");
                    if (FROM_PATTERNS.contains(pattern)) {
                        fromZone = findZone(location);
                    } else if (TO_PATTERNS.contains(pattern)) {
                        toZone = findZone(location);
                    }
                }

                // Return the tuple if at least one of the zones is found
                if (fromZone != null || toZone != null) {
                    return PortLocationTuple.of(fromZone, toZone);
                }
            }
        }
        return null; // Return null if no match is found
    }

}
