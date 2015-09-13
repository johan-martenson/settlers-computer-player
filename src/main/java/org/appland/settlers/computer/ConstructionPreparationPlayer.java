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

    private final GameMap map;
    private final Player  player;

    public ConstructionPreparationPlayer(Player p, GameMap m) {
        player = p;
        map    = m;
    }

    @Override
    public void turn() throws Exception {

        /* Find the headquarter if needed */
        if (headquarter == null) {
            headquarter = Utils.findHeadquarter(player);
        }

        /* Construct a forester */
        if (noForester()) {

            /* Place a forester hut */
            foresterHut = Utils.placeBuilding(player, headquarter, new ForesterHut(player));
        } else if (foresterDone() && noWoodcutter()) {

            /* Place the woodcutter */
            woodcutter = Utils.placeBuilding(player, headquarter, new Woodcutter(player));
        } else if (woodcutterDone() && noSawmill()) {

            /* Place the sawmill */
            sawmill = Utils.placeBuilding(player, headquarter, new Sawmill(player));
        } else if (sawmillDone() && noQuarry()) {

            /* Look for stone within the border */
            Point stonePoint = findStoneWithinBorder();

            /* Write a warning and exit if no stone is found */
            if (stonePoint == null) {
                System.out.println("WARNING: No stone found within border");

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
