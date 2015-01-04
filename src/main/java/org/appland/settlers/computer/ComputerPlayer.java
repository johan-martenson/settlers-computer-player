package org.appland.settlers.computer;

import org.appland.settlers.model.Player;

/**
 * Hello world!
 *
 */
public interface ComputerPlayer {

    public void turn() throws Exception;

    public Player getControlledPlayer();
}
