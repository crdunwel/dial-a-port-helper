public enum PortZone {
    NORTH_KARANA("NK"),
    WEST_COMMONLANDS("WC"),
    TOXXULIA_FOREST("Tox"),
    BUTCHER_BLOCK("BB"),
    MISTY_THICKET("Misty"),
    LAVASTORM("LS"),
    FEERROTT("Feerrott"),
    SOUTHERN_RO("Sro"),
    NORTHERN_RO("Nro"),
    EAST_KARANA("EK"),
    WEST_KARANA("WK"),
    SUREFALL_GLADE("SFG"),
    STEAMFONT_MOUNTAINS("SF"),
    NEKTULOS_FOREST("Nek"),
    GREATER_FAYDARK("GF"),
    CAZIC_THULE("CT");

    private String abbreviation;

    PortZone(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return this.abbreviation;
    }
}