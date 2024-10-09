import de.Skippero.LOA.features.states.Server;
import de.Skippero.LOA.features.states.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerManagerTest {

    @Test
    void testServerThresholdDecrementOnSameState() {
        Server server = new Server("TestServer", State.GOOD);

        // Simulate multiple updates with the same state
        server.Update("TestServer", State.GOOD);
        server.Update("TestServer", State.GOOD);

        assertEquals(3, server.getUpdateThreshold());  // Initial threshold 5, decremented twice
        assertFalse(server.IsValidStateUpdate());  // Not valid yet

        // After 5 times, it should be valid
        server.Update("TestServer", State.GOOD);
        server.Update("TestServer", State.GOOD);
        server.Update("TestServer", State.GOOD);
        assertTrue(server.IsValidStateUpdate());
    }

    @Test
    void testServerThresholdResetOnStateChange() {
        Server server = new Server("TestServer", State.GOOD);

        // Simulate a state change
        server.Update("TestServer", State.BUSY);

        assertEquals(5, server.getUpdateThreshold());  // Threshold should reset to 5
        assertFalse(server.IsValidStateUpdate());  // Still invalid after one change
    }
}
