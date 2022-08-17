package de.Skippero.LOA.utils;

public enum State {

    GOOD("Online"),
    BUSY("Busy"),
    FULL("Full"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    State(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
