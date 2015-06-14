package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.Fishery;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Terrain;

/**
 *
 * @author johan
 */
public class FoodProducer implements ComputerPlayer {
    private final int RANGE_FISHERY_TO_WATER = 3;

    private final Player        controlledPlayer;
    private final GameMap       map;
    private final List<Fishery> fisheries;

    private State     state;
    private Building  headquarter;

    private enum State {
        INITIALIZING,
        NEEDS_FOOD,
        LOOKING_FOR_GEOLOGIST,
        FOUND_MINERALS, 
        WAITING_FOR_GEOLOGY_RESULTS, 
        BUILD_FISHERY, WAITING_FOR_FISHERY
    }
    
    public FoodProducer(Player player, GameMap m) {
        controlledPlayer = player;
        map              = m;

        fisheries = new ArrayList<>();

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
                state = State.NEEDS_FOOD;
            }
        } else if (state == State.NEEDS_FOOD) {

            /* Try to build a fishery if there isn't already one placed */
            if (fisheries.isEmpty()) {
                state = State.BUILD_FISHERY;
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
}
