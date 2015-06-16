package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.List;

import org.appland.settlers.model.Armory;
import org.appland.settlers.model.Brewery;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.IronSmelter;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;

import static org.appland.settlers.model.Size.MEDIUM;

/**
 *
 * @author johan
 */
public class MiltaryProducer implements ComputerPlayer {

    private final Player            controlledPlayer;
    private final GameMap           map;
    private final List<IronSmelter> ironSmelters;
    private final List<Armory>      armories;
    private final List<Brewery>     breweries;

    private State    state;
    private Building headquarter;

    private enum State {
        INITIALIZING,
        NEEDS_FOOD, 
        WAITING_FOR_ARMORY, 
        NEEDS_BREWERY, 
        WAITING_FOR_IRON_SMELTER, 
        NEEDS_ARMORY, 
        NEEDS_IRON_SMELTER, 
        WAITING_FOR_BREWERY, 
        DONE,
    }

    public MiltaryProducer(Player player, GameMap m) {
        controlledPlayer = player;
        map              = m;

        ironSmelters = new ArrayList<>();
        armories     = new ArrayList<>();
        breweries    = new ArrayList<>();

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
                state = State.NEEDS_IRON_SMELTER;
            }
        } else if (state == State.NEEDS_IRON_SMELTER) {

            /* Determine if there already are iron smelters built */
            if (Utils.buildingTypeExists(controlledPlayer.getBuildings(), IronSmelter.class)) {
        	    ironSmelters.addAll(Utils.getBuildingsOfType(controlledPlayer.getBuildings(), IronSmelter.class));

        	    state = State.WAITING_FOR_IRON_SMELTER;
            } else { 

                /* Find a spot for the iron smelter */
            	Point ironSmelterPoint = Utils.findPointForBuildingCloseToPoint(headquarter.getPosition(), MEDIUM, controlledPlayer, map);

            	if (ironSmelterPoint == null) {
            		return;
            	}

            	/* Build the iron smelter */
            	IronSmelter ironSmelter = map.placeBuilding(new IronSmelter(controlledPlayer), ironSmelterPoint);

            	ironSmelters.add(ironSmelter);

            	/* Connect the iron smelter with the headquarter */
                Road road = Utils.connectPointToBuilding(controlledPlayer, map, ironSmelter.getFlag().getPosition(), headquarter);

                /* Fill the road with flags */
                Utils.fillRoadWithFlags(map, road);

                state = State.WAITING_FOR_IRON_SMELTER;
            }
        } else if (state == State.WAITING_FOR_IRON_SMELTER) {

        	if (Utils.buildingsAreReady(ironSmelters)) {
        		state = State.NEEDS_ARMORY;
        	}
        } else if (state == State.NEEDS_ARMORY) {

            /* Determine if there already are armories built */
            if (Utils.buildingTypeExists(controlledPlayer.getBuildings(), Armory.class)) {
        	    armories.addAll(Utils.getBuildingsOfType(controlledPlayer.getBuildings(), Armory.class));

        	    state = State.WAITING_FOR_IRON_SMELTER;
            } else { 

                /* Find a spot for the armory */
            	Point armoryPoint = Utils.findPointForBuildingCloseToPoint(headquarter.getPosition(), MEDIUM, controlledPlayer, map);

            	if (armoryPoint == null) {
            		return;
            	}

            	/* Build the armory */
            	Armory armory = map.placeBuilding(new Armory(controlledPlayer), armoryPoint);

            	armories.add(armory);

            	/* Connect the armory with the headquarter */
                Road road = Utils.connectPointToBuilding(controlledPlayer, map, armory.getFlag().getPosition(), headquarter);

                /* Fill the road with flags */
                Utils.fillRoadWithFlags(map, road);

                state = State.WAITING_FOR_ARMORY;
            }
        } else if (state == State.WAITING_FOR_ARMORY) {

        	if (Utils.buildingsAreReady(armories)) {
        		state = State.NEEDS_BREWERY;
        	}
        } else if (state == State.NEEDS_BREWERY) {

            /* Determine if there already are breweries built */
            if (Utils.buildingTypeExists(controlledPlayer.getBuildings(), Brewery.class)) {
        	    breweries.addAll(Utils.getBuildingsOfType(controlledPlayer.getBuildings(), Brewery.class));

        	    state = State.WAITING_FOR_BREWERY;
            } else { 

                /* Find a spot for the brewery */
            	Point breweryPoint = Utils.findPointForBuildingCloseToPoint(headquarter.getPosition(), MEDIUM, controlledPlayer, map);

            	if (breweryPoint == null) {
            		return;
            	}

            	/* Build the brewery */
            	Brewery brewery = map.placeBuilding(new Brewery(controlledPlayer), breweryPoint);

            	breweries.add(brewery);

            	/* Connect the brewery with the headquarter */
                Road road = Utils.connectPointToBuilding(controlledPlayer, map, brewery.getFlag().getPosition(), headquarter);

                /* Fill the road with flags */
                Utils.fillRoadWithFlags(map, road);

                state = State.WAITING_FOR_BREWERY;
            }
        } else if (state == State.WAITING_FOR_BREWERY) {
            if (Utils.buildingsAreReady(breweries)) {
            	state = State.DONE;
            }
        }
    }

    @Override
    public Player getControlledPlayer() {
        return controlledPlayer;
    }
}
