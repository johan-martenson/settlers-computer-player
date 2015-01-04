/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.test;

import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.ForesterHut;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Woodcutter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author johan
 */
public class MoreUtils {

    public static void verifyPlayersBuildingsContain(Player player0, Class<? extends Building> aClass) {
        boolean found = false;
        for (Building b : player0.getBuildings()) {
            if (b instanceof ForesterHut) {
                found = true;
            }
        }

        assertTrue(found);
    }

    public static <T> T waitForComputerPlayerToPlaceBuilding(ComputerPlayer computerPlayer, Class<T> aClass, GameMap map) throws Exception {
        T found = null;

        for (int i = 0; i < 1000; i++) {
            computerPlayer.turn();

            for (Building b : computerPlayer.getControlledPlayer().getBuildings()) {
                if (b.getClass().equals(aClass)) {
                    found = (T)b;

                    break;
                }
            }

            map.stepTime();
        }

        assertNotNull(found);

        return found;
    }

    public static <T extends Building> void waitForBuildingToGetConstructed(ComputerPlayer computerPlayer, GameMap map, T building) throws Exception {

        for (int i = 0; i < 1000; i++) {
            computerPlayer.turn();

            if (building.ready()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(building.ready());
    }

    public static <T extends Building> void verifyPlayerPlacesOnlyBuilding(ComputerPlayer computerPlayer, GameMap map, Class<T> aClass) throws Exception {
        Player player = computerPlayer.getControlledPlayer();
        int amount    = player.getBuildings().size();
        boolean found = false;

        for (int i = 0; i < 1000; i++) {
            computerPlayer.turn();

            for (Building b : player.getBuildings()) {
                if (b.getClass().equals(aClass)) {
                    found = true;

                    break;
                }
            }

            if (found) {
                break;
            }

            map.stepTime();
        }

        assertTrue(found);
        assertEquals(player.getBuildings().size(), amount);
    }
}
