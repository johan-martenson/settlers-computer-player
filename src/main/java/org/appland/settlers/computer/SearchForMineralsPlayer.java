/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.CoalMine;
import org.appland.settlers.model.Countdown;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Geologist;
import org.appland.settlers.model.GoldMine;
import org.appland.settlers.model.GraniteMine;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.IronMine;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Material;
import static org.appland.settlers.model.Material.COAL;
import static org.appland.settlers.model.Material.GOLD;
import static org.appland.settlers.model.Material.IRON;
import static org.appland.settlers.model.Material.STONE;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Sign;
import org.appland.settlers.model.Worker;

/**
 *
 * @author johan
 */
public class SearchForMineralsPlayer implements ComputerPlayer {
    private final int RANGE_BETWEEN_FLAG_AND_POINT = 5;
    private final int GEOLOGIST_WAIT_TIMEOUT = 200;

    private final Player                 controlledPlayer;
    private final GameMap                map;
    private final Set<Point>             concludedPoints;
    private final Set<Point>             pointsToInvestigate;
    private final Map<Point, Material>   foundMinerals;
    private final Map<Material, Integer> activeMines;
    private final Countdown              countdown;
    private final Set<Point>             unreachablePoints;

    private State     state;
    private Building  headquarter;
    private Flag      geologistFlag;
    private Geologist calledGeologist;

    private enum State {
        INITIALIZING,
        LOOKING_FOR_MINERALS,
        LOOKING_FOR_GEOLOGIST,
        FOUND_MINERALS,
        WAITING_FOR_GEOLOGY_RESULTS,
        ALL_CURRENTLY_CONCLUDED
    }

    public SearchForMineralsPlayer(Player player, GameMap m) {
        controlledPlayer = player;
        map              = m;

        concludedPoints     = new HashSet<>();
        pointsToInvestigate = new HashSet<>();
        unreachablePoints   = new HashSet<>();
        geologistFlag       = null;
        foundMinerals       = new HashMap<>();
        activeMines         = new HashMap<>();

        activeMines.put(GOLD, 0);
        activeMines.put(IRON, 0);
        activeMines.put(COAL, 0);
        activeMines.put(STONE, 0);

        countdown = new Countdown();

        state = State.INITIALIZING;
    }

    @Override
    public void turn() throws Exception {

        if (state == State.INITIALIZING) {

            for (Building building : controlledPlayer.getBuildings()) {
                if (building instanceof Headquarter) {
                    headquarter = building;

                    break;
                }
            }

            if (headquarter != null) {
                state = State.LOOKING_FOR_MINERALS;
            }
        } else if (state == State.LOOKING_FOR_MINERALS) {

            lookForNewPointsToHandle();

            /* Update points to investigate */
            List<Point> noLongerValid = new LinkedList<>();

            for (Point p : pointsToInvestigate) {
                if (!isAvailableForSign(p)) {
                    noLongerValid.add(p);
                }
            }

            pointsToInvestigate.removeAll(noLongerValid);

            if (pointsToInvestigate.isEmpty()) {
                System.out.println(" - Has investigated all available spots");

                state = State.ALL_CURRENTLY_CONCLUDED;
            } else {

                /* Send out geologists if needed and possible */
                for (Point p : pointsToInvestigate) {

                    /* Skip un-reachable points */
                    if (unreachablePoints.contains(p)) {
                        continue;
                    }

                    /* Temporarily skip points if needed */
                    if (map.isBuildingAtPoint(p)) {
                        continue;
                    }

                    if (map.isTreeAtPoint(p)) {
                        continue;
                    }

                    if (map.isFlagAtPoint(p)) {
                        continue;
                    }

                    /* Look for a suitable flag close to the point */
                    Flag flag = findFlagCloseBy(p);

                    if (flag == null) {

                        Point flagPoint = findPointForFlagCloseBy(p);

                        if (flagPoint != null) {
                            flag = map.placeFlag(controlledPlayer, flagPoint);

                            /* Build a road that connects with the headquarter */
                            Road road = Utils.connectPointToBuilding(controlledPlayer, map, flagPoint, headquarter);

                            /* Fill the road with flags */
                            Utils.fillRoadWithFlags(map, road);
                        } else {
                            unreachablePoints.add(p);
                        }
                    }

                    if (flag != null) {
                        state = State.LOOKING_FOR_GEOLOGIST;

                        geologistFlag = flag;

                        /* Call two geologist to speed up search */
                        flag.callGeologist();
                        flag.callGeologist();

                        /* Set a countdown for how long to wait for the geologist */
                        countdown.countFrom(GEOLOGIST_WAIT_TIMEOUT);

                        break;
                    }
                }
            }
        } else if (state == State.LOOKING_FOR_GEOLOGIST) {

            for (Worker w : map.getWorkers()) {

                if (! (w instanceof Geologist)) {
                    continue;
                }

                if (w.getTarget().equals(geologistFlag.getPosition())) {
                    calledGeologist = (Geologist)w;

                    state = State.WAITING_FOR_GEOLOGY_RESULTS;

                    break;
                }
            }

            if (countdown.reachedZero()) {

                /* Give up on waiting for the geologist if the timeout expired */
                state = State.LOOKING_FOR_MINERALS;
            } else {
                countdown.step();
            }
        } else if (state == State.WAITING_FOR_GEOLOGY_RESULTS) {

            List<Point> newlyInvestigatedPoints = new LinkedList<>();

            /* Find any new results */
            for (Point p : pointsToInvestigate) {

                if (!map.isSignAtPoint(p)) {
                    continue;
                }

                Sign sign = map.getSignAtPoint(p);

                if (sign.getType() == null) {
                    continue;
                }

                foundMinerals.put(p, sign.getType());

                newlyInvestigatedPoints.add(p);

                if (buildMineIfPossible(p, sign.getType())) {

                    /* Remove the flag as well from the list of points to investigate */
                    newlyInvestigatedPoints.add(p.downRight());
                }
            }

            concludedPoints.addAll(newlyInvestigatedPoints);

            pointsToInvestigate.removeAll(newlyInvestigatedPoints);

            if (calledGeologist.getTarget().equals(headquarter.getPosition())) {
                state = State.LOOKING_FOR_MINERALS;
            }
        }
    }

    private void lookForNewPointsToHandle() {
        /* Look for any new points to handle */
        for (Land land : controlledPlayer.getLands()) {
            for (Point p : land.getPointsInLand()) {

                if (concludedPoints.contains(p)) {
                    continue;
                }

                if (!map.getTerrain().isOnMountain(p)) {
                    concludedPoints.add(p);

                    continue;
                }

                pointsToInvestigate.add(p);
            }
        }
    }

    @Override
    public Player getControlledPlayer() {
        return controlledPlayer;
    }

    private Point findPointForFlagCloseBy(Point point) throws Exception {

        for (Point p : map.getPointsWithinRadius(point, RANGE_BETWEEN_FLAG_AND_POINT)) {

            if (!map.isAvailableFlagPoint(controlledPlayer, p)) {
                continue;
            }

            Point hqFlagPoint = headquarter.getFlag().getPosition();
            Point connectPoint = Utils.findConnectionToDestionationOrExistingRoad(controlledPlayer, map, p, hqFlagPoint);

            if (connectPoint != null) {
                return p;
            }
        }

        return null;
    }

    private Flag findFlagCloseBy(Point point) throws Exception {

        for (Point p : map.getPointsWithinRadius(point, RANGE_BETWEEN_FLAG_AND_POINT)) {

            if (!map.isFlagAtPoint(p)) {
                continue;
            }

            if (!map.arePointsConnectedByRoads(p, headquarter.getFlag().getPosition())) {
                continue;
            }

            return map.getFlagAtPoint(p);
        }

        return null;
    }

    private boolean buildMineIfPossible(Point p, Material type) throws Exception {

        if (map.isAvailableMinePoint(controlledPlayer, p)) {

            Building mine = null;

            switch (type) {
            case GOLD:
                mine = map.placeBuilding(new GoldMine(controlledPlayer), p);
                break;
            case IRON:
                mine = map.placeBuilding(new IronMine(controlledPlayer), p);
                break;
            case COAL:
                mine = map.placeBuilding(new CoalMine(controlledPlayer), p);
                break;
            case STONE:
                mine = map.placeBuilding(new GraniteMine(controlledPlayer), p);
                break;
            default:
                throw new Exception("Cannot create mine to get " + type);
            }

            if (activeMines.get(type) == 0) {
                Road road = Utils.connectPointToBuilding(controlledPlayer, map, p.downRight(), headquarter);

                Utils.fillRoadWithFlags(map, road);

                activeMines.put(type, 1);
            }

            return true;
        }

        return false;
    }

    boolean allCurrentMineralsKnown() {
        return state == State.ALL_CURRENTLY_CONCLUDED;
    }

    boolean hasCoalMine() {
        return activeMines.containsKey(COAL) && activeMines.get(COAL) > 0;
    }

    boolean hasIronMine() {
        return activeMines.containsKey(IRON) && activeMines.get(IRON) > 0;
    }

    boolean hasGoldMine() {
        return activeMines.containsKey(GOLD) && activeMines.get(GOLD) > 0;
    }

    boolean hasGraniteMine() {
        return activeMines.containsKey(STONE) && activeMines.get(STONE) > 0;
    }

    boolean hasMines() {
        return hasCoalMine() || hasIronMine() || hasGoldMine() || hasGraniteMine();
    }

    private boolean isAvailableForSign(Point p) {

        if (!map.getTerrain().isOnMountain(p)) {
            return false;
        }

        if (map.isBuildingAtPoint(p)) {
            return false;
        }

        if (map.isCropAtPoint(p)) {
            return false;
        }

        if (map.isFlagAtPoint(p)) {
            return false;
        }

        if (map.isSignAtPoint(p)) {
            return false;
        }

        if (map.isStoneAtPoint(p)) {
            return false;
        }

        if (map.isTreeAtPoint(p)) {
            return false;
        }

        return true;
    }

    void scanForNewMinerals() {

        if (state != State.INITIALIZING) {
            state = State.LOOKING_FOR_MINERALS;
        }
    }
}
