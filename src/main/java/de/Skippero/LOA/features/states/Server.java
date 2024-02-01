package de.Skippero.LOA.features.states;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Server {
    private final String name;
    private final State state;

    public Server(String name, State state) {
        this.name = name;
        this.state = state;
    }

    public String getStateName() {
        return this.state.getDisplayName();
    }
}

