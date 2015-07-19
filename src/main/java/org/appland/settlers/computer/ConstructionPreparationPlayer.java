/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.LinkedList;
import java.util.List;
import org.appland.settlers.model.ForesterHut;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Quarry;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Sawmill;
import static org.appland.settlers.model.Size.MEDIUM;
import static org.appland.settlers.model.Size.SMALL;
import org.appland.settlers.model.Stone;
import org.appland.settlers.model.Woodcutter;

/**
 *
 * @author johan
 */
public class ConstructionPreparationPlayer implements ComputerPlayer {
    private ForesterHut foresterHut;
    private Woodcutter  woodcutter;
    private Headquarter headquarter;
    private Sawmill     sawmill;
    private Quarry      quarry;

    private enum State {
        NO_CONSTRUCTION,
        WAITING_FOR_FORESTER,
        FORESTER_CONSTRUCTED,
        WOODCUTTER_CONSTRUCTED,
        SAWMILL_CONSTRUCTED, 
        WAITING_FOR_WOODCUTTER, 
        WAITING_FOR_SAWMILL, 
        NO_STONE_WITHIN_BORDER, 
        WAITING_FOR_QUARRY, 
        IDLE, 
        NEED_STONE
    }

    private static final int QUARRY_STONE_DISTANCE = 4;

    private final GameMap map;
    private final Player  player;

    private State state;
    
    public ConstructionPreparationPlayer(Player p, GameMap m) {
        player = p;
        map    = m;

        /* Set the initial state */
        state = State.NO_CONSTRUCTION;
    }

    @Override
    public void turn() throws Exception {
        State stateBefore = state;

        /* Construct a forester */
        if (state == State.NO_CONSTRUCTION) {

            /* Find headquarter */
            headquarter = Utils.findHeadquarter(player);

            /* Find a site for the forester hut */
            Point site = findSpotForForesterHut();

            /* Place forester hut */
            foresterHut = map.placeBuilding(new ForesterHut(player), site);

            /* Connect the forester hut with the headquarter */
            Road road = Utils.connectPointToBuilding(player, map, 
                    foresterHut.getFlag().getPosition(), headquarter);

            /* Place flags where possible */
            if (road != null) {
                Utils.fillRoadWithFlags(map, road);
            }

            /* Change state to wait for the forester to be ready */
            state = State.WAITING_FOR_FORESTER;
        } else if (state == State.WAITING_FOR_FORESTER) {

            /* Check if the forester hut is constructed */
            if (foresterHut.ready()) {
                state = State.FORESTER_CONSTRUCTED;
            }
        } else if (state == State.FORESTER_CONSTRUCTED) {

            /* Find a site for the woodcutter close to the forester hut */
            Point site = findSpotForWoodcutterNextToForesterHut(foresterHut);

            /* Place the woodcutter */
            woodcutter = map.placeBuilding(new Woodcutter(player), site);

            /* Connect the forester hut with the headquarter */
            Road road = Utils.connectPointToBuilding(player, map, 
                    woodcutter.getFlag().getPosition(), headquarter);

            /* Place flags where possible */
            if (road != null) {
                Utils.fillRoadWithFlags(map, road);
            }

            /* Change state to wait for the woodcutter */
            state = State.WAITING_FOR_WOODCUTTER;
        } else if (state == State.WAITING_FOR_WOODCUTTER) {

            /* Check if the woodcutter is constructed */
            if (woodcutter.ready()) {
                state = State.WOODCUTTER_CONSTRUCTED;
            }
        } else if (state == State.WOODCUTTER_CONSTRUCTED) {

            /* Find a site for the sawmill close to the headquarter */
            Point site = findSpotForSawmill(headquarter);

            /* Place the sawmill */
            sawmill = map.placeBuilding(new Sawmill(player), site);

            /* Connect the sawmill with the headquarter */
            Road road = Utils.connectPointToBuilding(player, map, 
                    sawmill.getFlag().getPosition(), headquarter);

            /* Place flags where possible */
            if (road != null) {
                Utils.fillRoadWithFlags(map, road);
            }

            /* Change state to wait for the woodcutter */
            state = State.WAITING_FOR_SAWMILL;
        } else if (state == State.WAITING_FOR_SAWMILL) {

            /* Check if the sawmill is constructed */
            if (sawmill.ready()) {
                state = State.SAWMILL_CONSTRUCTED;
            }
        } else if (state == State.SAWMILL_CONSTRUCTED) {

            /* Look for stone within the border */
            Point stonePoint = findStoneWithinBorder();

            /* Write a warning and exit if no stone is found */
            if (stonePoint == null) {
                System.out.println("WARNING: No stone found within border");

                state = State.NO_STONE_WITHIN_BORDER;

                return;
            }

            /* Find spot close to stone to place quarry */
            Point site = findSpotForQuarry(stonePoint);

            /* Place the quarry */
            quarry = map.placeBuilding(new Quarry(player), site);

            /* Connect the quarry to the headquarter */
            Road road = Utils.connectPointToBuilding(player, map, quarry.getFlag().getPosition(), headquarter);

            /* Place flags on the road where possible */
            if (road != null) {
                Utils.fillRoadWithFlags(map, road);
            }

            /* Change state to wait for construction of the quarry */
            state = State.WAITING_FOR_QUARRY;
        } else if (state == State.WAITING_FOR_QUARRY) {

            /* Check if the quarry is ready */
            if (quarry.ready()) {
                state = State.IDLE;
            }
        } else if (state == State.IDLE) {

            /* Check if the quarry still has access to stone */
            List<Stone> stones = findStonesWithinReach(quarry.getPosition());

            /* Stay idle if there is still stone */
            if (!stones.isEmpty()) {
                return;
            }

            /* Destroy the quarry if it can't reach any stone */
            quarry.tearDown();

            /* Remove the part of the road that is used only by the quarry */
            Utils.removeRoadWithoutAffectingOthers(map, quarry.getFlag());

            /* Set state to needing stone again */
            state = State.NEED_STONE;
        }

        /* Print the old and new state if the state changed */
        if (stateBefore != state) {
            System.out.println("Transition: " + stateBefore + " -> " + state);
        }
    }

    private Point findSpotForForesterHut() throws Exception {
        return Utils.findAvailableSpotForBuilding(map, player);
    }

    private Point findSpotForWoodcutterNextToForesterHut(ForesterHut foresterHut) throws Exception {

        /* Find available spots close to the forester */
        List<Point> spots = Utils.findAvailableHousePointsWithinRadius(map, player, foresterHut.getPosition(), SMALL, 4);

        /* Return null if there are no available spots */
        if (spots.isEmpty()) {
            return null;
        }

        /* Return any point from the available ones */
        return spots.get(0);
    }

    private Point findSpotForSawmill(Headquarter headquarter) throws Exception {

        /* Find available spots close to the forester */
        List<Point> spots = Utils.findAvailableHousePointsWithinRadius(map, player, headquarter.getPosition(), MEDIUM, 4);

        /* Return null if there are no available spots */
        if (spots.isEmpty()) {
            return null;
        }

        /* Return any point from the available ones */
        return spots.get(0);
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }

    private Point findStoneWithinBorder() {
        for (Land land : player.getLands()) {
            for (Point point : land.getPointsInLand()) {
                if (map.isStoneAtPoint(point)) {
                    return point;
                }
            }
        }

        return null;
    }

    private Point findSpotForQuarry(Point stonePoint) throws Exception {

        /* Get points with available space for houses close to the stone point */
        List<Point> points = Utils.findAvailableHousePointsWithinRadius(map, player, stonePoint, SMALL, 3);

        /* Return null if there are no available places */
        if (points.isEmpty()) {
            return null;
        }

        /* Return any point from the available places */
        return points.get(0);
    }

    private List<Stone> findStonesWithinReach(Point position) {
        List<Stone> stones = new LinkedList<>();

        /* Get points the quarry can reach */
        List<Point> points = map.getPointsWithinRadius(position, QUARRY_STONE_DISTANCE);

        /* Find stones within the reachable area */
        for (Point point : points) {
            if (map.isStoneAtPoint(point)) {
                stones.add(map.getStoneAtPoint(point));
            }
        }

        return stones;
    }

    boolean basicConstructionDone() {

        return (foresterHut != null && foresterHut.ready() &&
                woodcutter  != null && woodcutter.ready()  &&
                sawmill     != null && sawmill.ready()     &&
                quarry      != null && quarry.ready());
    }
}
