/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer.test;

import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.computer.ExpandLandPlayer;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Military;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.test.MoreUtils;
import org.appland.settlers.test.Utils;
import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.appland.settlers.computer.Utils.getDistanceToOwnBorder;
import static org.appland.settlers.model.Material.PLANK;
import static org.appland.settlers.model.Material.PRIVATE;
import static org.appland.settlers.model.Material.STONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author johan
 */
public class TestExpandLandPlayer {

    @Test
    public void testCreateExpandLandPlayer() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 10, 10);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);
    }

    @Test
    public void testPlayerPlacesFirstBarracks() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Verify that the player starts with placing a barracks */
        MoreUtils.verifyPlayerPlacesOnlyBuilding(computerPlayer, map, Barracks.class);

        assertEquals(player0.getBuildings().size(), 2);

        MoreUtils.verifyPlayersBuildingsContain(player0, Barracks.class);
    }

    @Test
    public void testPlayerPlacesFirstBarracksCloseToBorder() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to with place a barracks */
        Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Verify that the barracks is close to the border */
        double distance = getDistanceToOwnBorder(barracks.getPosition(), player0);

        assertTrue(distance < 3);
    }

    @Test
    public void testPlayerPlacesDoesNotPlaceUnnecessaryBarracksCloseToEdgeOfMap() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Verify that the player does not place barracks too close to the edges */

        for (int i = 0; i < 15; i++) {
            /* Wait for the player to with place a barracks */
            Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Verify that it's not close to the edge and un-necessary */
            Point p = barracks.getPosition();

            boolean foundBorderPointNotAtEdge = false;

            /* Check that the barracks is close to at least one border point
               that is not close to the edge */
            for (Point bp : player0.getBorderPoints()) {
                if (p.distance(bp) < 20) {
                    if (bp.x > 3 && bp.x < map.getWidth() - 3 && bp.y > 3 && bp.y < map.getHeight() - 3) {
                        foundBorderPointNotAtEdge = true;

                        break;
                    }
                }
            }

            assertTrue(foundBorderPointNotAtEdge);
        }
    }

    @Test
    public void testPlayerPlacesPlacesSecondBarracks() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to with place a barracks */
        Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Verify that the player places a second barracks */
        MoreUtils.verifyPlayerPlacesOnlyBuilding(computerPlayer, map, Barracks.class);
    }

    @Test
    public void testPlayerPlacesPlacesSecondBarracksAtCorrectDistanceFromFirstBarracks() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to with place a barracks */
        Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Wait for the player to place the second barracks */
        Barracks barracks1 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Verify that the player placed the second barracks at the right distance
           from the first barracks (at least 7 away) */
        double distance = barracks0.getPosition().distance(barracks1.getPosition());

        assertTrue(distance > 7);
    }

    @Test
    public void testPlayerKeepsPlacingBarracksWhenEdgeIsReached() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(5, 5);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 400);
        Utils.adjustInventoryTo(headquarter, STONE, 400);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 400);

        /* Wait for the player to place a barracks that is close to the edge */
        for (int i = 0; i < 20; i++) {

            /* Wait for the player to with place a barracks */
            Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Check if the barracks is close enough to the edge of the map */
            Point point = barracks0.getPosition();

            if (point.x < 4 || point.x > map.getWidth() - 4 || point.y < 4 || point.y > map.getHeight() - 4) {
                break;
            }
        }

        /* Verify that the player places barracks again */
        MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);
    }

    @Test
    public void testPlayerEvacuatesBarracksWhenItIsFarFromBorder() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(5, 5);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to place the first barracks */
        Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Wait for the border to get extended so that the first barracks is far
           from the border */
        for (int i = 0; i < 20000; i++) {

            computerPlayer.turn();

            /* Check if the barracks is still close to the border */
            boolean borderClose = false;

            for (Point point : player0.getBorderPoints()) {

                /* Filter points too close to the edges of the map */
                if (point.x < 3 || point.x > map.getWidth() - 3 ||
                    point.y < 3 || point.y > map.getHeight() - 3) {
                    continue;
                }

                if (point.distance(barracks0.getPosition()) < 6) {
                    borderClose = true;

                    break;
                }
            }

            map.stepTime();

            if (!borderClose) {
                break;
            }
        }

        assertTrue(barracks0.isEvacuated());

        /* Verify that the player barracks again */
        MoreUtils.verifyPlayerPlacesOnlyBuilding(computerPlayer, map, Barracks.class);
    }

    @Test
    public void testPlayerPlacesDoesNotPlaceBarracksTooCloseToEdgeOfMap() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(5, 5);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Verify that the player does not place barracks too close to the edges */

        for (int i = 0; i < 15; i++) {

            /* Wait for the player to with place a barracks */
            Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Verify that it's not too close to the edge */
            Point p = barracks.getPosition();

            assertTrue(p.x > 2);
            assertTrue(p.x < map.getWidth() - 2);
            assertTrue(p.y > 2);
            assertTrue(p.y < map.getHeight()- 2);
        }
    }

    @Test
    public void testPlayerBuildsNewBarracksIfCurrentBarracksIsDestroyed() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to with place a barracks */
        Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Wait for the player to place a second barracks */
        Barracks barracks1 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Destroy the second barracks */
        barracks1.tearDown();

        /* Verify that the player builds a new barracks */
        Barracks barracks2 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        assertEquals(map.getBuildingAtPoint(barracks2.getPosition()), barracks2);
        assertNotEquals(barracks2.getPosition(), barracks1.getPosition());
    }

    @Test
    public void testPlayerRestoresRoadIfNeeded() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to with place a barracks */
        Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        assertFalse(barracks0.isReady());

        /* Remove a road */
        List<Road> roads = map.getRoadsFromFlag(barracks0.getFlag());

        roads.remove(map.getRoad(barracks0.getPosition(), barracks0.getFlag().getPosition()));

        map.removeRoad(roads.get(0));

        assertFalse(map.areFlagsOrBuildingsConnectedViaRoads(barracks0, headquarter));

        /* Verify that the player builds a road to connect the barracks again */
        computerPlayer.turn();
        computerPlayer.turn();

        assertTrue(map.areFlagsOrBuildingsConnectedViaRoads(barracks0, headquarter));
    }

    @Test
    public void testPlayerDoesNotRestoreRoadToCapturedBarracks() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", Color.BLUE);
        Player player1 = new Player("Player 1", Color.RED);
        List<Player> players = new ArrayList<>();
        players.add(player0);
        players.add(player1);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place player 0's headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter0 = map.placeBuilding(new Headquarter(player0), point0);

        /* Place player 1's headquarter */
        Point point1 = new Point(40, 10);
        Headquarter headquarter1 = map.placeBuilding(new Headquarter(player1), point1);

        /* Place player 1's barracks */
        Point point2 = new Point(30, 10);
        Barracks barracks1 = map.placeBuilding(new Barracks(player1), point2);

        /* Finish player 1's barracks */
        Utils.constructHouse(barracks1);

        /* Occupy player 1's barracks */
        Utils.occupyMilitaryBuilding(Military.Rank.GENERAL_RANK, 2, barracks1, map);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter0, PLANK, 60);
        Utils.adjustInventoryTo(headquarter0, STONE, 60);
        Utils.adjustInventoryTo(headquarter0, PRIVATE, 10);

        /* Wait for player 0 to place a barracks close to player 1's barracks */
        Building barracksToAttack = null;

        for (int i = 0; i < 10000; i++) {

            for (Point p : map.getPointsWithinRadius(barracks1.getPosition(), 12)) {

                /* Filter points without buildings */
                if (!map.isBuildingAtPoint(p)) {
                    continue;
                }

                Building building = map.getBuildingAtPoint(p);

                /* Filter points with own buildings */
                if (building.getPlayer().equals(player1)) {
                    continue;
                }

                /* Filter non-military buildings */
                if (!building.isMilitaryBuilding()) {
                    continue;
                }

                /* Filter unfinished buildings */
                if (!building.isReady()) {
                    continue;
                }

                barracksToAttack = building;

                break;
            }

            map.stepTime();

            computerPlayer.turn();
        }

        assertNotNull(barracksToAttack);

        /* Let player 1 capture the barracks */
        player1.attack(barracksToAttack, 1);

        /* Wait for player 1 to capture the barracks */
        MoreUtils.waitForBuildingToGetCapturedByPlayer(barracksToAttack, player1, map);

        /* Verify that player 0 doesn't build a road to the captured barracks */
        for (int i = 0; i < 200; i++) {

            map.stepTime();

            computerPlayer.turn();

            /* Verify that there is only one road from the barracks' flag */
            assertEquals(map.getRoadsFromFlag(barracksToAttack.getFlag()).size(), 1);
        }
    }

    @Test
    public void testPlayerStopsPromotionsInBarracksNotCloseToAnEnemy() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(player0);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(10, 10);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 40);
        Utils.adjustInventoryTo(headquarter, STONE, 40);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40);

        /* Wait for the player to place barracks */
        Barracks barracks0 = MoreUtils.verifyPlayerPlacesOnlyBuilding(computerPlayer, map, Barracks.class);

        assertEquals(player0.getBuildings().size(), 2);

        MoreUtils.verifyPlayersBuildingsContain(player0, Barracks.class);

        /* Wait for the barracks to get constructed */
        Utils.fastForwardUntilBuildingIsConstructed(barracks0);

        /* Verify that the player stops promotions because there is no enemy in the map */
        computerPlayer.turn();

        assertFalse(barracks0.isPromotionEnabled());
    }

    @Test
    public void testPlayerEnablesPromotionsForBarracksCloseToEnemy() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        Player player1 = new Player("Player 1", java.awt.Color.RED);
        List<Player> players = new ArrayList<>();
        players.add(player0);
        players.add(player1);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 30);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(5, 5);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Place the enemy's headquarter */
        Point point1 = new Point(50, 10);
        Headquarter headquarter1 = map.placeBuilding(new Headquarter(player1), point1);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 500);
        Utils.adjustInventoryTo(headquarter, STONE, 500);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 500);

        /* Verify that the player enables promotions in barracks close to the
           enemy
        */
        Barracks latestBarracks = null;

        for (int i = 0; i < 100; i++) {

            /* Wait for the player to with place a barracks */
            Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Wait for the barracks to be occupied */
            MoreUtils.waitForBuildingToGetOccupied(computerPlayer, map, barracks);

            assertTrue(barracks.getNumberOfHostedMilitary() > 0);

            /* Check how close the barracks is to the enemy's border */
            if (MoreUtils.distanceToKnownBorder(barracks, player1) < 8) {
                latestBarracks = barracks;

                break;
            }
        }

        assertNotNull(latestBarracks);
        assertTrue(MoreUtils.distanceToKnownBorder(latestBarracks, player1) < 8);
        assertTrue(latestBarracks.isPromotionEnabled());

        /* Verify that promotions stay enabled */
        MoreUtils.waitForBuildingToGetConstructed(computerPlayer, map, latestBarracks);

        MoreUtils.waitForBuildingToGetOccupied(computerPlayer, map, latestBarracks);

        Utils.fastForward(100, map);

        assertTrue(latestBarracks.isPromotionEnabled());
    }

    @Test
    public void testPlayerUpgradesBarracksCloseToEnemy() throws Exception {

        /* Create players */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        Player player1 = new Player("Player 1", java.awt.Color.RED);
        List<Player> players = new ArrayList<>();
        players.add(player0);
        players.add(player1);

        /* Create game map */
        GameMap map = new GameMap(players, 100, 100);

        /* Create the computer player */
        ComputerPlayer computerPlayer = new ExpandLandPlayer(player0, map);

        /* Place headquarter */
        Point point0 = new Point(5, 5);
        Headquarter headquarter = map.placeBuilding(new Headquarter(player0), point0);

        /* Place the enemy's headquarter */
        Point point1 = new Point(40, 10);
        Headquarter headquarter1 = map.placeBuilding(new Headquarter(player1), point1);

        /* Give the player extra building materials and militaries */
        Utils.adjustInventoryTo(headquarter, PLANK, 500);
        Utils.adjustInventoryTo(headquarter, STONE, 500);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 500);

        /* Verify that the player upgrades barracks close to the enemy */
        Barracks latestBarracks = null;

        for (int i = 0; i < 100; i++) {

            /* Wait for the player to with place a barracks */
            Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            MoreUtils.waitForBuildingToGetConstructed(computerPlayer, map, barracks);

            /* Check how close the barracks is to the enemy's border */
            if (MoreUtils.distanceToKnownBorder(barracks, player1) < 8) {
                latestBarracks = barracks;
                break;
            }

            assertTrue(map.areFlagsOrBuildingsConnectedViaRoads(headquarter, barracks));
            assertTrue(barracks.needsMilitaryManning());
            assertTrue(headquarter.getAmount(Material.PRIVATE) > 10);

            /* Wait for the barracks to be occupied */
            MoreUtils.waitForBuildingToGetOccupied(computerPlayer, map, barracks);

            assertTrue(barracks.getNumberOfHostedMilitary() > 0);
        }

        map.stepTime();
        computerPlayer.turn();

        assertNotNull(latestBarracks);
        assertTrue(latestBarracks.isUpgrading());
        assertNotNull(latestBarracks);
        assertTrue(MoreUtils.distanceToKnownBorder(latestBarracks, player1) < 8);
        assertTrue(latestBarracks.isPromotionEnabled());

        /* Verify that promotions stay enabled */
        MoreUtils.waitForBuildingToGetConstructed(computerPlayer, map, latestBarracks);

        MoreUtils.waitForBuildingToGetOccupied(computerPlayer, map, latestBarracks);

        Utils.fastForward(100, map);

        assertTrue(latestBarracks.isPromotionEnabled());
    }
}
