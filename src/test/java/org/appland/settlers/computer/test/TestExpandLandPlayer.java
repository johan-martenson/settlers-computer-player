/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.computer.ExpandLandPlayer;
import static org.appland.settlers.computer.Utils.getDistanceToBorder;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import static org.appland.settlers.model.Material.PLANCK;
import static org.appland.settlers.model.Material.PRIVATE;
import static org.appland.settlers.model.Material.STONE;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.test.MoreUtils;
import org.appland.settlers.test.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

        /* Wait for the player to with place a barracks */
        Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Verify that the barracks is close to the border */
        double distance = getDistanceToBorder(barracks.getPosition(), player0);

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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

        /* Verify that the player does not place barracks too close to the edges */

        for (int i = 0; i < 15; i++) {
            /* Wait for the player to with place a barracks */
            Barracks barracks = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Verify that it's not close to the edge and un-necessary */
            Point p = barracks.getPosition();

            boolean foundBorderPointNotAtEdge = false;

            /* Check that the barracks is close to at least one border point
               that is not close to the edge */
            for (Collection<Point> border : player0.getBorders()) {
                for (Point bp : border) {
                    if (p.distance(bp) < 20) {
                        if (bp.x > 3 && bp.x < map.getWidth() - 3 && bp.y > 3 && bp.y < map.getHeight() - 3) {
                            foundBorderPointNotAtEdge = true;

                            break;
                        }
                    }
                }

                if (foundBorderPointNotAtEdge) {
                    break;
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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

        /* Wait for the player to place a barracks that is close to the edge */
        for (int i = 0; i < 20; i++) {
            System.out.println("i: " + i);
            /* Wait for the player to with place a barracks */
            Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

            /* Check if the barracks is close enough to the edge of the map */
            Point point = barracks0.getPosition();

            if (point.x < 4 || point.x > map.getWidth() - 4 || point.y < 4 || point.y > map.getHeight() - 4) {
                break;
            }
        }
            
        /* Verify that the player barracks again */
        MoreUtils.verifyPlayerPlacesOnlyBuilding(computerPlayer, map, Barracks.class);
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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

        /* Wait for the player to place the first barracks */
        Barracks barracks0 = MoreUtils.waitForComputerPlayerToPlaceBuilding(computerPlayer, Barracks.class, map);

        /* Wait for the border to get extended so that the first barracks is far
           from the border */
        for (int i = 0; i < 20000; i++) {

            computerPlayer.turn();

            /* Check if the barracks is still close to the border */
            boolean borderClose = false;

            for (Collection<Point> border : player0.getBorders()) {
                for (Point point : border) {

                    /* Filter points too close to the edges of the map */
                    if (point.x < 3 || point.x > map.getWidth() - 3 ||
                        point.y < 3 || point.y > map.getHeight() - 3) {
                        continue;
                    }

                    if (point.distance(barracks0.getPosition()) < 10) {
                        borderClose = true;

                        break;
                    }
                }

                if (borderClose) {
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
        Utils.adjustInventoryTo(headquarter, PLANCK, 40, map);
        Utils.adjustInventoryTo(headquarter, STONE, 40, map);
        Utils.adjustInventoryTo(headquarter, PRIVATE, 40, map);

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
// TEST CAN BUILD 20 (MANY) BARRACKS
}