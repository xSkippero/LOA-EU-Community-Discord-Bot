package de.Skippero.LOA.features.states;

public enum State {

    GOOD("Online"),
    BUSY("Busy"),
    FULL("Full"),
    MAINTENANCE("Maintenance"),
    UNKNOWN("Unknown");

    private final String displayName;

    State(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
