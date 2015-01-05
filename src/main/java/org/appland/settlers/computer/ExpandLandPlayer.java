/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Size;

/**
 *
 * @author johan
 */
public class ExpandLandPlayer implements ComputerPlayer {
    private final List<Building> placedBarracks;
    private final GameMap        map;
    private final Player         player;

    private Barracks    unfinishedBarracks;
    private Headquarter headquarter;
    private State       state;

    private enum State {
        INITIAL_STATE, 
        WAITING_FOR_CONSTRUCTION, 
        CONSTRUCTION_DONE
    }

    private final static int MIN_DISTANCE_BETWEEN_BARRACKS = 4;
    private final static int GOOD_DISTANCE_BETWEEN_BARRACKS = 7;
    private final static int MAX_DISTANCE_FROM_BORDER = 3;
    private final static int MIN_DISTANCE_TO_EDGE = 3;
    private final static int THRESHOLD_FOR_EVACUATION = 10;

    public ExpandLandPlayer(Player p, GameMap m) {
        player = p;
        map    = m;

        placedBarracks = new LinkedList<>();

        /* Set the initial state */
        state = State.INITIAL_STATE;
    }

    @Override
    public void turn() throws Exception {
        State stateBefore = state;

        /* Construct a barracks */
        if (state == State.INITIAL_STATE) {

            /* Find headquarter */
            headquarter = Utils.findHeadquarter(player);

            /* Find a site for the first barracks */
            Point site = findSpotForNextBarracks(player);

            /* Place barracks */
            unfinishedBarracks = map.placeBuilding(new Barracks(player), site);

            /* Connect the barracks with the headquarter */
            Road road = map.placeAutoSelectedRoad(player, unfinishedBarracks.getFlag(), headquarter.getFlag());

            /* Place flags where possible */
            Utils.fillRoadWithFlags(map, road);

            /* Change state to wait for the barracks to be ready and occupied */
            state = State.WAITING_FOR_CONSTRUCTION;
        } else if (state == State.WAITING_FOR_CONSTRUCTION) {

            /* Check if construction is done and the building is occupied */
            if (unfinishedBarracks.ready() && unfinishedBarracks.getHostedMilitary() > 0) {

                /* Save the barracks */
                placedBarracks.add(unfinishedBarracks);

                /* Evacuate any buildings far enough from the border */
                evacuateWherePossible(player);

                /* Change the state to construction done */
                state = State.CONSTRUCTION_DONE;
            }
        } else if (state == State.CONSTRUCTION_DONE) {

            /* Find the spot for the next barracks */
            Point site = findSpotForNextBarracks(player);

            /* Place barracks */
            unfinishedBarracks = map.placeBuilding(new Barracks(player), site);

            /* Connect the barracks with the headquarter */
            Road road = Utils.placeRoadToHeadquarterOrExistingRoad(player, map, unfinishedBarracks, headquarter);

            /* Place flags where possible */
            Utils.fillRoadWithFlags(map, road);

            /* Change state to wait for the barracks to be ready and occupied */
            state = State.WAITING_FOR_CONSTRUCTION;
        }

        /* Print the old and new state if the state changed */
        if (stateBefore != state) {
            System.out.println("Transition: " + stateBefore + " -> " + state);
        }
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }

    private Point findSpotForFirstBarracks(GameMap map, Player player) throws Exception {

        /* Go throug the border and find a suitable spot */
        for (Point point : player.getBorders().get(0)) {

            /* Get a suitable point for building a military building */
            for (Point availablePoint : getMilitaryBuildingsPointFromBorderPoint(point)) {

                /* Filter out points too close to the edge of the map */
                if (tooCloseToEdge(map, availablePoint)) {
                    continue;
                }

                /* Return any suitable point */
                return availablePoint;
            }
        }

        /* Return null if there were no available points */
        return null;
    }

    private boolean tooCloseToMilitaryBuilding(Player player, Point point, int limit) {

        for (Building b : player.getBuildings()) {
            if (!b.isMilitaryBuilding()) {
                continue;
            }

            if (point.distance(b.getPosition()) < limit) {
                return true;
            }
        }

        return false;
    }

    private List<Point> getMilitaryBuildingsPointFromBorderPoint(Point point) throws Exception {
        /* Avoid points without available spots to build houses close by */
        List<Point> sites = Utils.findAvailableHousePointsWithinRadius(map, player, point, Size.SMALL, 3);

        /* Return null if there are no available sites */
        if (sites.isEmpty()) {
            return null;
        }
        
        /* Choose the point if it passed all criterias */
        return sites;
    }

    private boolean tooCloseToEdge(GameMap map, Point point) {
        if (point.x < 3 || point.x > map.getWidth() - 3 || point.y < 3 || point.y > map.getHeight() - 3) {
            return true;
        }

        return false;
    }

    private Point findSpotForNextBarracks(Player player) throws Exception {
        double qualityOfLeastBad = Double.MAX_VALUE;

        Set<Point> alreadyTried = new HashSet<>();
        Point      leastBad     = null;

        /* Find the next one clockwise, if possible with a good distance to the previous one */
        for (Point borderPoint : player.getBorders().get(0)) {

            /* Filter out border too close to the edge of the map */
            if (borderPoint.x < MIN_DISTANCE_TO_EDGE                  ||
                borderPoint.x > map.getWidth() - MIN_DISTANCE_TO_EDGE ||
                borderPoint.y < MIN_DISTANCE_TO_EDGE                  ||
                borderPoint.y > map.getHeight() - MIN_DISTANCE_TO_EDGE) {
                continue;
            }

            /* Go through points for construction close to the border point */
            for (Point point : map.getPointsWithinRadius(borderPoint, MAX_DISTANCE_FROM_BORDER)) {

                /* Filter out already tried points */
                if (alreadyTried.contains(point)) {
                    continue;
                }

                /* Don't try the point again */
                alreadyTried.add(point);

                /* Filter out points that cannot be built on */
                if (map.isAvailableHousePoint(player, point) == null) {
                    continue;
                }

                /* Filter out points that are too close to existing military buildings */
                if (tooCloseToMilitaryBuilding(player, point, MIN_DISTANCE_BETWEEN_BARRACKS)) {
                    continue;
                }

                /* Check if this building close to any already built military buildings */
                boolean tooClose = false;

                for (int i = 0; i < player.getBuildings().size(); i++) {
                    Building existingBuilding = player.getBuildings().get(i);

                    /* Don't investigate non-military buildings */
                    if (!existingBuilding.isMilitaryBuilding()) {
                        continue;
                    }

                    double tempDistance = point.distance(existingBuilding.getPosition());

                    if (tempDistance >= GOOD_DISTANCE_BETWEEN_BARRACKS) {
                        continue;
                    }

                    tooClose = true;

                    if (i < qualityOfLeastBad) {
                        qualityOfLeastBad = i;

                        leastBad = point;
                    }
                }

                /* Filter the point if it's close to other military buildings */
                if (tooClose) {
                    continue;
                }

                /* Select the point if it passed all the filters */
                return point;
            }
        }

        /* Return the least bad alternative if no good point was found */
        return leastBad;
    }

    private void evacuateWherePossible(Player player) throws Exception {

        /* Go through the buildings and evacuate where possible */
        for (Building building : placedBarracks) {

            /* Only investigate military buildings */
            if (!building.isMilitaryBuilding()) {
                continue;
            }

            /* Skip buildings that are already evacuated */
            if (building.isEvacuated()) {
                continue;
            }

            /* Check if the building is far enough from the border */
            boolean borderClose = false;

            for (Collection<Point> border : player.getBorders()) {
                for (Point borderPoint : border) {

                    /* Filter points beyond the evacuation threshold */
                    if (borderPoint.distance(building.getPosition()) >= THRESHOLD_FOR_EVACUATION) {
                        continue;
                    }

                    /* Filter points at the edge of the map since no attack can come that way */
                    if (borderPoint.x < 3 || borderPoint.x > map.getWidth() - 3 || 
                        borderPoint.y < 3 || borderPoint.y > map.getHeight() - 3) {
                        continue;
                    }

                    /* The border is close if we made it here */
                    borderClose = true;

                    break;
                }

                if (borderClose) {
                    break;
                }
            }

            /* Evacuate the building if it's not close to the border */
            if (!borderClose) {
                building.evacuate();
            }
        }
    }
}
