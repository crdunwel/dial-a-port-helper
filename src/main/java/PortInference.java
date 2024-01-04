import java.util.*;
import java.util.regex.Pattern;

public class PortInference {

    // Regex for "from $1 to $2"
    public static final Pattern FROM_TO_PATTERN = Pattern.compile("(?i)from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    // Regex for "from $1"
    public static final Pattern FROM_PATTERN = Pattern.compile("(?i)from\\s+((?:\\w+\\s)?\\w+)");
    // Regex for "port from $1"
    public static final Pattern PORT_FROM_PATTERN = Pattern.compile("(?i)port\\s+from\\s+((?:\\w+\\s)?\\w+)");
    // Regex for "port to $2"
    public static final Pattern PORT_TO_PATTERN = Pattern.compile("(?i)port\\s+to\\s+((?:\\w+\\s)?\\w+)");
    // Regex for "$1 > $2"
    public static final Pattern GREATER_THAN_PATTERN = Pattern.compile("(?i)((?:\\w+\\s)?\\w+)\\s*>\\s*((?:\\w+\\s)?\\w+)");
    public static final Pattern RIDE_TO_PATTERN = Pattern.compile("(?i)ride\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern RIDE_FROM_TO_PATTERN = Pattern.compile("(?i)ride\\s+from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern PICKUP_AT_TO_PATTERN = Pattern.compile("(?i)pickup\\s+at\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");
    public static final Pattern PICKUP_FROM_TO_PATTERN = Pattern.compile("(?i)pickup\\s+from\\s+((?:\\w+\\s)?\\w+)\\s+to\\s+((?:\\w+\\s)?\\w+)");

    // List of all regex patterns
    public static final List<Pattern> ALL_PATTERNS = Arrays.asList(
            FROM_TO_PATTERN,
            FROM_PATTERN,
            PORT_FROM_PATTERN,
            PORT_TO_PATTERN,
            GREATER_THAN_PATTERN,
            RIDE_TO_PATTERN,
            RIDE_FROM_TO_PATTERN,
            PICKUP_AT_TO_PATTERN,
            PICKUP_FROM_TO_PATTERN
    );

    public static Map<String, Set<String>> zones = new HashMap<>();
    public static Map<String, String> invertedFlattenedZones = new HashMap<>();

    static {
        // Initializing the 'zones' map
        zones.put("NK", new HashSet<>(Arrays.asList("nk", "north karana", "karana", "nkarana", "karanas")));
        zones.put("WC", new HashSet<>(Arrays.asList("wc", "common", "commons", "wcommon", "wcommons", "wcom")));
        zones.put("Tox", new HashSet<>(Arrays.asList("tox", "toxx", "toxxulia", "toxulia")));
        zones.put("BB", new HashSet<>(Arrays.asList("bb", "butcher", "bblock", "butcherblock", "butcher block")));
        zones.put("Misty", new HashSet<>(Arrays.asList("misty", "misty thicket", "mt")));
        zones.put("LS", new HashSet<>(Arrays.asList("ls", "lava", "lavastorm")));
        zones.put("Feerrott", new HashSet<>(Arrays.asList("feerrott", "ferot", "feerott", "feerrot", "ferrot")));
        zones.put("Sro", new HashSet<>(Arrays.asList("sro", "ro", "south ro", "southern ro")));
        zones.put("Surefall", new HashSet<>(Arrays.asList("surefall", "surefall glade", "sfall")));
        zones.put("SF", new HashSet<>(Arrays.asList("sf", "steam", "steamfont", "steamfont mountains", "steamfront", "stm mtn")));

        // Creating the inverted flattened map
        for (Map.Entry<String, Set<String>> entry : zones.entrySet()) {
            String key = entry.getKey();
            for (String alias : entry.getValue()) {
                invertedFlattenedZones.put(alias.toLowerCase(), key); // Using lower case for case-insensitivity
            }
        }
    }

}
