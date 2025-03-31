package de.Skippero.LOA.features.states;

import lombok.Getter;

@Getter
public class Server {
    private String name;
    private State state;
    private int updateThreshold;
    private State lastState;

    public Server(String name, State state) {
        this.name = name;
        this.state = state;
        this.updateThreshold = 5;
        this.lastState = State.UNKNOWN;
    }

    public void Update(String newName, State newState) {
        if(state.equals(newState)) {
            updateThreshold--;
        }else{
            updateThreshold = 5;
        }
        name = newName;
        state = newState;
    }

    public void UpdateLastState(State newState) {
        this.lastState = newState;
    }

    public boolean IsValidStateUpdate() {
        return updateThreshold <= 0 && lastState != state;
    }

}

