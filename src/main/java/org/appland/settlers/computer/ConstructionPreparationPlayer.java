/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

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

        /* Construct a forester */
        if (noForester()) {

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
        } else if (foresterDone() && noWoodcutter()) {

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
        } else if (woodcutterDone() && noSawmill()) {

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
        } else if (sawmillDone() && noQuarry()) {

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
        } else if (quarryDone() && quarry.outOfNaturalResources()) {

            /* Destroy the quarry if it can't reach any stone */
            quarry.tearDown();

            /* Remove the part of the road that is used only by the quarry */
            Utils.removeRoadWithoutAffectingOthers(map, quarry.getFlag());
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

    boolean basicConstructionDone() {

        return (foresterDone()   &&
                woodcutterDone() &&
                sawmillDone()    &&
                ((quarryDone() && !quarry.outOfNaturalResources()) ||
                 (noQuarry() && !Utils.hasStoneWithinArea(map, player))));
    }

    private boolean foresterDone() {
        return Utils.buildingDone(foresterHut);
    }

    private boolean noForester() {
        return !Utils.buildingInPlace(foresterHut);
    }

    private boolean noWoodcutter() {
        return !Utils.buildingInPlace(woodcutter);
    }

    private boolean woodcutterDone() {
        return Utils.buildingDone(woodcutter);
    }

    private boolean noSawmill() {
        return !Utils.buildingInPlace(sawmill);
    }

    private boolean sawmillDone() {
        return Utils.buildingDone(sawmill);
    }

    private boolean noQuarry() {
        return !Utils.buildingInPlace(quarry);
    }

    private boolean quarryDone() {
        return Utils.buildingDone(quarry);
    }
}
