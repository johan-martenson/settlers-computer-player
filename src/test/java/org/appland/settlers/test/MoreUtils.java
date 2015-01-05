/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.test;

import java.util.HashSet;
import java.util.Set;
import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Stone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
            if (b.getClass().equals(aClass)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    public static <T> T waitForComputerPlayerToPlaceBuilding(ComputerPlayer computerPlayer, Class<T> aClass, GameMap map) throws Exception {
        Player player = computerPlayer.getControlledPlayer();
        T found       = null;
        Set<Building> buildingsBefore = new HashSet<>();

        buildingsBefore.addAll(player.getBuildings());

        for (int i = 0; i < 10000; i++) {
            computerPlayer.turn();

            for (Building b : player.getBuildings()) {
                if (b.getClass().equals(aClass) && !buildingsBefore.contains(b)) {
                    found = (T)b;

                    break;
                }
            }

            if (found != null) {
                break;
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

        T building = waitForComputerPlayerToPlaceBuilding(computerPlayer, aClass, map);

        assertEquals(player.getBuildings().size(), amount + 1);
    }

    public static void waitForStoneToRunOut(ComputerPlayer computerPlayer, GameMap map, Stone stone) throws Exception {
        for (int i = 0; i < 10000; i++) {

            computerPlayer.turn();

            if (!map.isStoneAtPoint(stone.getPosition())) {
                break;
            }

            map.stepTime();
        }

        assertFalse(map.isStoneAtPoint(stone.getPosition()));
    }

    public static <T extends Building> void waitForBuildingToGetTornDown(ComputerPlayer computerPlayer, GameMap map, T quarry) throws Exception {
        for (int i = 0; i < 100; i++) {

            computerPlayer.turn();

            if (quarry.burningDown()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(quarry.burningDown());
    }
}
