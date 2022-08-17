package de.Skippero.LOA.utils;

public class Server
{
    private final String name;
    private final State state;

    public Server(String name, State state)
    {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public State getState() {
        return this.state;
    }

    public String getStateName() {
        return this.state.getDisplayName();
    }
}

