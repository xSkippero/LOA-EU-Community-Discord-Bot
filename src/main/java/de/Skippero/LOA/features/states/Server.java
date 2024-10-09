package de.Skippero.LOA.features.states;

import lombok.Getter;

@Getter
public class Server {
    private String name;
    private State state;
    private int updateThreshold;

    public Server(String name, State state) {
        this.name = name;
        this.state = state;
        this.updateThreshold = 5;
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

    public boolean IsValidStateUpdate() {
        return updateThreshold <= 0;
    }

}

