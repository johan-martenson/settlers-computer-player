package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.List;
import org.appland.settlers.model.Bakery;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.Farm;
import org.appland.settlers.model.Fishery;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.HunterHut;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Mill;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Terrain;
import org.appland.settlers.model.Well;

/**
 *
 * @author johan
 */
public class FoodProducer implements ComputerPlayer {
    private final int RANGE_FISHERY_TO_WATER = 3;

    private final Player          controlledPlayer;
    private final GameMap         map;
    private final List<Fishery>   fisheries;
    private final List<HunterHut> hunterHuts;

    private State       state;
    private Headquarter headquarter;
    private Farm        farm;
    private Well        well;
    private Mill        mill;
    private Bakery      bakery;

    private enum State {
        INITIALIZING,
        NEEDS_FOOD,
        BUILD_FISHERY, 
        WAITING_FOR_FISHERY, 
        BUILD_HUNTER_HUT, 
        WAITING_FOR_HUNTER_HUT,
        NEEDS_BREAD
    }
    
    public FoodProducer(Player player, GameMap m) {
        controlledPlayer = player;
        map              = m;

        fisheries  = new ArrayList<>();
        hunterHuts = new ArrayList<>();
        farm       = null;
        well       = null;
        mill       = null;
        bakery     = null;

        state = State.INITIALIZING;
    }

    @Override
    public void turn() throws Exception {

        if (state == State.INITIALIZING) {

            for (Building building : controlledPlayer.getBuildings()) {
                if (building instanceof Headquarter) {
                    headquarter = (Headquarter) building;

                    break;
                }
            }

            if (headquarter != null) {
                state = State.NEEDS_FOOD;
            }
        } else if (state == State.NEEDS_FOOD) {

            /* Try to build a fishery if there isn't already one placed */
            if (fisheries.isEmpty()) {
                state = State.BUILD_FISHERY;
            } else if (hunterHuts.isEmpty()) {
                state = State.BUILD_HUNTER_HUT;
            }
        } else if (state == State.BUILD_FISHERY) {

            /* Find a spot to build a fishery on */
            Point pointForFishery = findPointForFishery();

            if (pointForFishery == null) {
                return;
            }

            /* Build the fishery */
            Fishery fishery = map.placeBuilding(new Fishery(controlledPlayer), pointForFishery);

            fisheries.add(fishery);

            /* Connect the fishery with the headquarter */
            Road road = Utils.connectPointToBuilding(controlledPlayer, map, fishery.getFlag().getPosition(), headquarter);

            /* Fill the road with flags */
            Utils.fillRoadWithFlags(map, road);

            state = State.WAITING_FOR_FISHERY;
        } else if (state == State.BUILD_HUNTER_HUT) {

            /* Find a spot to build a hunter hut on */
            Point pointForHunterHut = findPointForHunterHut();

            if (pointForHunterHut == null) {
                return;
            }

            /* Build the hunter hut */
            HunterHut hunterHut = map.placeBuilding(new HunterHut(controlledPlayer), pointForHunterHut);

            hunterHuts.add(hunterHut);

            /* Connect the hunter hut with the headquarter */
            Road road = Utils.connectPointToBuilding(controlledPlayer, map, hunterHut.getFlag().getPosition(), headquarter);

            /* Fill the road with flags */
            Utils.fillRoadWithFlags(map, road);

            state = State.WAITING_FOR_HUNTER_HUT;
        } else if (state == State.WAITING_FOR_FISHERY) {

            boolean buildingsDone = true;

            for (Fishery fishery : fisheries) {
                if (!fishery.ready()) {
                    buildingsDone = false;
                }
            }

            if (buildingsDone) {
                state = State.NEEDS_FOOD;
            }
        } else if (state == State.WAITING_FOR_HUNTER_HUT) {

            boolean buildingsDone = true;

            for (HunterHut hunterHut : hunterHuts) {
                if (!hunterHut.ready()) {
                    buildingsDone = false;
                }
            }

            if (buildingsDone) {
                state = State.NEEDS_BREAD;
            }
        } else if (state == State.NEEDS_BREAD) {
            if (!Utils.buildingInPlace(farm)) {

                /* Place a farm */
                farm = Utils.placeBuilding(controlledPlayer, headquarter, new Farm(controlledPlayer));
            } else if (Utils.buildingDone(farm) && !Utils.buildingInPlace(well)) {

                /* Place a well */
                well = Utils.placeBuilding(controlledPlayer, headquarter, new Well(controlledPlayer));
            } else if (Utils.buildingDone(well) && !Utils.buildingInPlace(mill)) {

                /* Place a mill */
                mill = Utils.placeBuilding(controlledPlayer, headquarter, new Mill(controlledPlayer));
            } else if (Utils.buildingDone(mill) && !Utils.buildingInPlace(bakery)) {

                /* Place bakery */
                bakery = Utils.placeBuilding(controlledPlayer, headquarter, new Bakery(controlledPlayer));
            }
        }
    }

    @Override
    public Player getControlledPlayer() {
        return controlledPlayer;
    }

    private Point findPointForFishery() throws Exception {

        Terrain terrain = map.getTerrain();

        /* Look for water */
        for (Land land : controlledPlayer.getLands()) {
            for (Point wp : land.getPointsInLand()) {

                /* Filter non-water points */
                if (!terrain.isInWater(wp)) {
                    continue;
                }

                /* Find point close by to build a fishery */
                for (Point p : map.getPointsWithinRadius(wp, RANGE_FISHERY_TO_WATER)) {

                    /* Filter points where it's not possible to build */
                    if (map.isAvailableHousePoint(controlledPlayer, p) == null) {
                        continue;
                    }

                    return p;
                }
            }
        }

        return null;
    }

    private Point findPointForHunterHut() throws Exception {

        /* Find a good point to build on, close to the headquarter */
        Point site = null;
        double distance = Double.MAX_VALUE;

        for (Land land : controlledPlayer.getLands()) {
            for (Point p : land.getPointsInLand()) {

                /* Filter out points where it's not possible to build */
                if (map.isAvailableHousePoint(controlledPlayer, p) == null) {
                    continue;
                }

                double tempDistance = p.distance(headquarter.getPosition());

                if (tempDistance < distance) {
                    site = p;
                    distance = tempDistance;
                }
            }
        }

        return site;
    }

    boolean basicFoodProductionDone() {

        return (Utils.listContainsAtLeastOneReadyBuilding(fisheries) &&
                Utils.listContainsAtLeastOneReadyBuilding(hunterHuts));
    }

    boolean fullFoodProductionDone() {
        return basicFoodProductionDone() &&
               Utils.buildingDone(farm)  && 
               Utils.buildingDone(mill)  && 
               Utils.buildingDone(well)  && 
               Utils.buildingDone(bakery);
    }
}
