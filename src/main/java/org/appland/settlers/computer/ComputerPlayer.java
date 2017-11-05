package org.appland.settlers.computer;

import org.appland.settlers.model.Player;

/**
 * Hello world!
 *
 */
public interface ComputerPlayer {

    void turn() throws Exception;

    Player getControlledPlayer();
}
