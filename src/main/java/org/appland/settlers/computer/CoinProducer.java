package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.List;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Mint;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Size;

import static org.appland.settlers.model.Material.GOLD;
import static org.appland.settlers.model.Size.SMALL;

/**
 *
 * @author johan
 */
public class CoinProducer implements ComputerPlayer {
    private final Player     controlledPlayer;
    private final GameMap    map;
    private final List<Mint> mints;

    private State     state;
    private Building  headquarter;

    private enum State {
        INITIALIZING,
        NEEDS_GOLD,
        MINT_NEEDED,
        WAITING_FOR_MINT,
        DONE
    }
    
    public CoinProducer(Player player, GameMap m) {
        controlledPlayer = player;
        map              = m;

        mints = new ArrayList<>();

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
                state = State.NEEDS_GOLD;
            }
        } else if (state == State.NEEDS_GOLD) {

        	/* Start building a mint if there is gold in storage */
        	if (headquarter.getAmount(GOLD) > 0 && mints.isEmpty()) {
        	    state = State.MINT_NEEDED;
        	}
       	
        /* Try to build a mint if there isn't already one placed */
        } else if (state == State.MINT_NEEDED) {

            /* Find a spot to build a mint on */
            Point pointForMint = findPointForMint();

            if (pointForMint == null) {
                return;
            }

            /* Build the mint */
            Mint mint = map.placeBuilding(new Mint(controlledPlayer), pointForMint);

            mints.add(mint);

            /* Connect the fishery with the headquarter */
            Road road = Utils.connectPointToBuilding(controlledPlayer, map, mint.getFlag().getPosition(), headquarter);

            /* Fill the road with flags */
            Utils.fillRoadWithFlags(map, road);

            state = State.WAITING_FOR_MINT;
        } else if (state == State.WAITING_FOR_MINT) {

            boolean buildingsDone = true;

            for (Mint mint : mints) {
                if (!mint.ready()) {
                    buildingsDone = false;
                }
            }

            if (buildingsDone) {
                state = State.DONE;
            }
        }
    }

    @Override
    public Player getControlledPlayer() {
        return controlledPlayer;
    }

    private Point findPointForMint() throws Exception {

        /* Find a good point to build on, close to the headquarter */
        Point site = null;
        double distance = Double.MAX_VALUE;

        for (Land land : controlledPlayer.getLands()) {
            for (Point p : land.getPointsInLand()) {

                /* Filter out points where it's not possible to build */
                Size size = map.isAvailableHousePoint(controlledPlayer, p); 
                if (size == null || size == SMALL) {
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

    boolean coinProductionDone() {
        return Utils.listContainsAtLeastOneReadyBuilding(mints);
    }
}
