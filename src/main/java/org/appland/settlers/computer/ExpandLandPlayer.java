package org.appland.settlers.computer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.InvalidUserActionException;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;

/**
 *
 * @author johan
 */
public class ExpandLandPlayer implements ComputerPlayer {
    private static final int CLOSE_TO_ENEMY_WEIGHT = 2;
    private static final int TOO_CLOSE_TO_ENEMY_WEIGHT = 2;

    private final Collection<Building> placedBarracks;
    private final GameMap              map;
    private final Player               player;
    private final Set<Point>           impossibleSpots;

    private Building    unfinishedBarracks;
    private Headquarter headquarter;
    private State       state;
    private boolean     newBuildings;
    int                 counter;
    private boolean     preferEnemyDirection;
    private boolean     waitUntilOccupied;

    private enum State {
        INITIAL_STATE, 
        WAITING_FOR_CONSTRUCTION, 
        READY_FOR_CONSTRUCTION, 
        BUILDING_NOT_CONNECTED,
        WAITING_FOR_BUILDINGS_TO_GET_OCCUPIED
    }

    private final static int MAX_PERIOD = 1000;
    private final static int MAINTENANCE_PERIOD = 50;
    private final static int MIN_DISTANCE_BETWEEN_BARRACKS = 4;
    private final static int MAX_DISTANCE_FROM_BORDER = 3;
    private final static int MIN_DISTANCE_TO_EDGE = 3;
    private final static int THRESHOLD_FOR_EVACUATION = 6;
    private final static int ENEMY_CLOSE = 4;

    public ExpandLandPlayer(Player p, GameMap m) {
        player = p;
        map    = m;

        placedBarracks = new HashSet<>();

        /* Set the initial state */
        state = State.INITIAL_STATE;

        newBuildings = false;
        impossibleSpots = new HashSet<>();

        /* Set default configuration */
        preferEnemyDirection = false;
        waitUntilOccupied = false;
    }

    @Override
    public void turn() throws Exception {
        State stateBefore = state;

        if (counter % MAINTENANCE_PERIOD == 0) {
            System.out.println(" - Evacuating where possible");
            evacuateWherePossible(player);
        }

        if (counter == MAX_PERIOD) {
            counter = 0;
        } else {
            counter++;
        }

        if (unfinishedBarracks != null) {
            unfinishedBarracks = (Building)map.getBuildingAtPoint(unfinishedBarracks.getPosition());
        }

        /* Start with finding the headquarter */
        if (state == State.INITIAL_STATE) {

            /* Find headquarter */
            headquarter = Utils.findHeadquarter(player);

            /* Change the state to ready to build */
            state = State.READY_FOR_CONSTRUCTION;
        } else if (state == State.READY_FOR_CONSTRUCTION) {

            if (waitUntilOccupied && !militaryBuildingsFullyOccupied(player)) {
                state = State.WAITING_FOR_BUILDINGS_TO_GET_OCCUPIED;

                return;
            }

            /* Find the spot for the next barracks */
            Point site = findSpotForNextBarracks(player, impossibleSpots);

            /* Stay in the ready to build state if there is no suitable site to build at */
            if (site == null) {
                return;
            }

            /* Place barracks */
            unfinishedBarracks = map.placeBuilding(new Barracks(player), site);

            System.out.println("\n\nPlaced barracks at: " + site);

            /* Connect the barracks with the headquarter */
            Road road = Utils.connectPointToBuilding(player, map, unfinishedBarracks.getFlag().getPosition(), headquarter);

            if (!map.isValidRouteViaRoads(road.getWayPoints())) {
                System.out.println("\nBarracks at " + site + " is not connected!");
            }

            /* Place flags where possible */
            Utils.fillRoadWithFlags(map, road);

            if (!map.isValidRouteViaRoads(road.getWayPoints())) {
                System.out.println("\nBarracks at " + site + " is not connected after filling with flags!");
            }

            /* Change state to wait for the barracks to be ready and occupied */
            state = State.WAITING_FOR_CONSTRUCTION;
        } else if (state == State.WAITING_FOR_CONSTRUCTION) {

            /* Build a new barracks if this barracks was destroyed */
            if (unfinishedBarracks.burningDown() || unfinishedBarracks.destroyed()) {

                /* Set state to build new barracks */
                state = State.READY_FOR_CONSTRUCTION;

            /* Disable promotions directly when the barracks is ready */
            } else if (unfinishedBarracks.ready() && unfinishedBarracks.getNumberOfHostedMilitary() == 0) {

                /* Disable promotions if the barracks is not close to the enemy */
                if (unfinishedBarracks.isPromotionEnabled() && 
                    Utils.distanceToKnownEnemiesWithinRange(unfinishedBarracks, 20) > 9) {

                    if (unfinishedBarracks.isPromotionEnabled()) {
                        unfinishedBarracks.disablePromotions();
                    }

                } else {

                    /* Upgrade barracks close to the enemy */
                    if (unfinishedBarracks instanceof Barracks && !unfinishedBarracks.isUpgrading()) {
                        unfinishedBarracks.upgrade();
                    }
                }

            /* Check if construction is done and the building is occupied */
            } else if (unfinishedBarracks.ready() && unfinishedBarracks.getNumberOfHostedMilitary() > 0) {

                /* Save the barracks */
                placedBarracks.add(unfinishedBarracks);

                /* Evacuate any buildings far enough from the border */
                evacuateWherePossible(player);

                /* Change the state to construction done */
                state = State.READY_FOR_CONSTRUCTION;

                /* Signal that there is at least one new building in place */
                newBuildings = true;

                /* Verify that the barracks under construction is still reachable from the headquarter */
            } else if (!map.areFlagsOrBuildingsConnectedViaRoads(headquarter, unfinishedBarracks)) {

                /* Try to repair the connection */
                state = State.BUILDING_NOT_CONNECTED;
            }
        } else if (state == State.BUILDING_NOT_CONNECTED) {
            System.out.println("\n - Repairing: " + unfinishedBarracks.getFlag().getPosition() + " to " +
                    headquarter.getFlag().getPosition());
            System.out.println("   - On map: " + map.getBuildingAtPoint(unfinishedBarracks.getPosition()));
            System.out.println("   - On map: " + map.getBuildingAtPoint(headquarter.getPosition()));
            System.out.println("   - Connection now: " + map.findWayWithExistingRoads(headquarter.getPosition(), unfinishedBarracks.getPosition()));

            /* Try to repair the connection */
            Utils.repairConnection(map, player, unfinishedBarracks.getFlag(), headquarter.getFlag());

            /* Wait for the building to get constructed if the repair worked */
            if (map.areFlagsOrBuildingsConnectedViaRoads(headquarter, unfinishedBarracks)) {

                /* Wait for construction */
                state = State.WAITING_FOR_CONSTRUCTION;

            /* Destroy the building if the repair failed */
            } else {

                /* Destroy the building */
                unfinishedBarracks.tearDown();

                /* Remember that this spot didn't work out */
                impossibleSpots.add(unfinishedBarracks.getPosition());

                /* Construct a new building */
                state = State.READY_FOR_CONSTRUCTION;
            }
        } else if (state == State.WAITING_FOR_BUILDINGS_TO_GET_OCCUPIED) {
            if (militaryBuildingsFullyOccupied(player)) {
                state = State.READY_FOR_CONSTRUCTION;
            }
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

    private Point findSpotForNextBarracks(Player player, Set<Point> ignore) throws Exception {
        Set<Point> candidates = new HashSet<>();
        Set<Point> investigated = new HashSet<>();

        /* First collect all possible points to build on */
        for (Point borderPoint : player.getBorders().get(0)) {

            /* Filter border points that are too close to the edge of the map */
            if (borderPoint.x < 3 || borderPoint.x > map.getWidth() - 3 &&
                borderPoint.y < 3 || borderPoint.y > map.getHeight() - 3) {
                continue;
            }

            /* Go through points for construction close to the border point */
            for (Point point : map.getPointsWithinRadius(borderPoint, MAX_DISTANCE_FROM_BORDER)) {

                /* Don't re-examine already added candidates */
                if (investigated.contains(point)) {
                    continue;
                }

                /* Make sure this point will not be investigated again */
                investigated.add(point);

                /* Filter out points that cannot be built on */
                if (map.isAvailableHousePoint(player, point) == null) {
                    continue;
                }

                /* Filter out spots we have tried before and failed at */
                if (ignore.contains(point)) {
                    continue;
                }

                /* Filter out impossible points */
                if (impossibleSpots.contains(point)) {
                    continue;
                }

                /* Filter out border too close to the edge of the map */
                if (point.x < MIN_DISTANCE_TO_EDGE                  ||
                    point.x > map.getWidth() - MIN_DISTANCE_TO_EDGE ||
                    point.y < MIN_DISTANCE_TO_EDGE                  ||
                    point.y > map.getHeight() - MIN_DISTANCE_TO_EDGE) {
                    continue;
                }

                /* Filter points that cannot be connected to the headquarter */
                if (!map.arePointsConnectedByRoads(point.downRight(), headquarter.getPosition()) &&
                    Utils.pointToConnectViaToGetToBuilding(player, map, point.downRight(), headquarter) == null) {
                    continue;
                }

                /* Add the point as a candidate if it passed the filters */
                candidates.add(point);
            }
        }

        /* Score the candidates and pick the one with the best score */
        int bestScore = 0;
        Point bestPoint = null;
        for (Point point : candidates) {
            int score = 0;

            /* Determine if this point is close to an enemy */
            if (preferEnemyDirection &&
                Utils.distanceToKnownEnemiesWithinRange(map, player, point, 6) < ENEMY_CLOSE) {
                score = score + CLOSE_TO_ENEMY_WEIGHT;
            }

            /* Punish points that are too close to existing military buildings */
            if (tooCloseToMilitaryBuilding(player, point, MIN_DISTANCE_BETWEEN_BARRACKS)) {
                score = score - TOO_CLOSE_TO_ENEMY_WEIGHT;
            }

            /* Reward points that are far from military buildings */
            double distanceToClosestMilitaryBuilding = Double.MAX_VALUE;
            for (int i = 0; i < player.getBuildings().size(); i++) {
                Building existingBuilding = player.getBuildings().get(i);

                /* Don't investigate non-military buildings */
                if (!existingBuilding.isMilitaryBuilding()) {
                    continue;
                }

                double tempDistance = point.distance(existingBuilding.getPosition());

                if (tempDistance < distanceToClosestMilitaryBuilding) {
                    distanceToClosestMilitaryBuilding = tempDistance;
                }
            }

            score = score + (int)distanceToClosestMilitaryBuilding;

            if (score > bestScore) {
                bestScore = score;

                bestPoint = point;
            }
        }

        /* Return the least bad alternative if no good point was found */
        return bestPoint;
    }

    private void evacuateWherePossible(Player player) throws Exception {

        /* Go through the buildings and evacuate where possible */
        for (Building storedBuilding : placedBarracks) {

            /* Cater for upgrades */
            Building building = map.getBuildingAtPoint(storedBuilding.getPosition());

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

    void clearNewBuildings() {
        newBuildings = false;
    }

    boolean hasNewBuildings() {
        return newBuildings;
    }

    void registerBuildings(List<Building> wonBuildings) throws InvalidUserActionException {

        for (Building building : wonBuildings) {

            /* Connect the building to the headquarter if it's not already done */
            try {
                if (!map.areFlagsOrBuildingsConnectedViaRoads(headquarter, building)) {
                    Road road = Utils.connectPointToBuilding(player, map, building.getFlag().getPosition(), headquarter);

                    if (road != null) {
                        Utils.fillRoadWithFlags(map, road);
                    } else {
                        System.out.println("Could not place road for newly registered barracks at " + building.getPosition());
                    }
                }
            } catch (Exception e) {

            }

            /* Disable promotions if the barracks is not close to the enemy */
            if (Utils.distanceToKnownEnemiesWithinRange(building, 20) > 9) {

                if (building.isPromotionEnabled()) {
                    building.disablePromotions();
                }

            } else {

                /* Upgrade barracks close to the enemy */
                if (!building.isUpgrading() && building instanceof Barracks) {
                    building.upgrade();
                }
            }

            /* Treat these as regular buildings placed by the expand land player */
            if (!placedBarracks.contains(building)) {
                placedBarracks.add(building);
            }
        }
    }

    void setExpandTowardEnemies(boolean b) {
        preferEnemyDirection = b;
    }

    void waitForBuildingsToGetCompletelyOccupied(boolean b) {
        waitUntilOccupied = b;
    }

    private boolean militaryBuildingsFullyOccupied(Player player) {
        for (Building building : player.getBuildings()) {

            /* Filter non-military buildings */
            if (!building.isMilitaryBuilding()) {
                continue;
            }

            /* Filter evacuated buildings */
            if (building.isEvacuated()) {
                continue;
            }

            /* Filter not constructed buildings */
            if (!building.ready()) {
                continue;
            }

            /* Check if the building is fully occupied */
            if (building.getNumberOfHostedMilitary() < building.getMaxHostedMilitary()) {
                return false;
            }
        }

        return true;
    }
}
