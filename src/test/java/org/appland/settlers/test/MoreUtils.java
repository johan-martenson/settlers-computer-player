/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.test;

import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Stone;

import java.util.HashSet;
import java.util.Set;

import static org.appland.settlers.model.Military.Rank.PRIVATE_RANK;
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
                break;
            }
        }

        assertTrue(found);
    }

    public static <T extends Building>
    T waitForComputerPlayerToPlaceBuilding(ComputerPlayer computerPlayer, Class<T> aClass, GameMap map) throws Exception {
        Player player = computerPlayer.getControlledPlayer();
        T found       = null;

        Set<Building> buildingsBefore = new HashSet<>(player.getBuildings());

        for (int i = 0; i < 10000; i++) {
            for (Building b : player.getBuildings()) {
                if (b.getClass().equals(aClass) && !buildingsBefore.contains(b)) {
                    found = (T)b;

                    break;
                }
            }

            if (found != null) {
                break;
            }

            computerPlayer.turn();

            map.stepTime();
        }

        assertNotNull(found);

        return found;
    }

    public static <T extends Building> void waitForBuildingToGetConstructed(ComputerPlayer computerPlayer, GameMap map, T building) throws Exception {

        for (int i = 0; i < 1000; i++) {
            if (building.isReady()) {
                break;
            }

            computerPlayer.turn();

            map.stepTime();
        }

        assertTrue(building.isReady());
    }

    public static <T extends Building> T verifyPlayerPlacesOnlyBuilding(ComputerPlayer computerPlayer, GameMap map, Class<T> aClass) throws Exception {
        Player player = computerPlayer.getControlledPlayer();
        int amount    = player.getBuildings().size();

        T building = waitForComputerPlayerToPlaceBuilding(computerPlayer, aClass, map);

        assertEquals(player.getBuildings().size(), amount + 1);

        return building;
    }

    public static void waitForStoneToRunOut(ComputerPlayer computerPlayer, GameMap map, Stone stone) throws Exception {
        for (int i = 0; i < 20000; i++) {

            computerPlayer.turn();

            if (!map.isStoneAtPoint(stone.getPosition())) {
                break;
            }

            map.stepTime();
        }

        assertFalse(map.isStoneAtPoint(stone.getPosition()));
    }

    public static <T extends Building> void waitForBuildingToGetTornDown(ComputerPlayer computerPlayer, GameMap map, T quarry) throws Exception {
        for (int i = 0; i < 1000; i++) {

            computerPlayer.turn();

            if (quarry.isBurningDown()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(quarry.isBurningDown());
    }

    public static Barracks placeAndOccupyBarracks(GameMap map, Player player, Point point2) throws Exception {

        /* Place barracks */
        Barracks barracks0 = map.placeBuilding(new Barracks(player), point2);

        /* Finish construction of barracks */
        Utils.constructHouse(barracks0);

        /* Occupy the barracks */
        Utils.occupyMilitaryBuilding(PRIVATE_RANK, 2, barracks0, map);

        return barracks0;
    }

    public static void waitForBuildingToGetCapturedByPlayer(Building building, Player player, GameMap map) throws Exception {

        for (int i = 0; i < 5000; i++) {

            if (building.getPlayer().equals(player)) {
                break;
            }

            map.stepTime();
        }

        assertEquals(building.getPlayer(), player);
    }

    public static double distanceToKnownBorder(Barracks barracks, Player player1) {

        /* Check how close the barracks is to the enemy's border */
        double distance = Double.MAX_VALUE;

        for (Point p : player1.getBorderPoints()) {
            double tmpDistance = barracks.getPosition().distance(p);

            if (barracks.getPlayer().getDiscoveredLand().contains(p) &&
                tmpDistance < distance) {
                distance = tmpDistance;
            }
        }

        return distance;
    }

    public static void waitForBuildingToGetOccupied(ComputerPlayer computerPlayer, GameMap map, Building building) throws Exception {

        for (int i = 0; i < 10000; i++) {
            if (building.isOccupied()) {
                break;
            }

            computerPlayer.turn();

            map.stepTime();
        }

        assertTrue(building.isOccupied());
    }
}
