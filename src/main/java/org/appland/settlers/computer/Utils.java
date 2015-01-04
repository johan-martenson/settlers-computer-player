/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class Utils {

    static Point findAvailableSpotForBuilding(GameMap map, Player player) throws Exception {
        Map<Point, Size> spots = map.getAvailableHousePoints(player);

        return spots.keySet().iterator().next();
    }

    static List<Point> findAvailableHousePointsWithinRadius(GameMap map, Player player, Point position, Size size, int radius) throws Exception {
        List<Point> availableHousePointsWithinRadius = new LinkedList<>();

        /* Collect the available house points within the radius */
        for (Point point : map.getPointsWithinRadius(position, radius)) {

            Size availableSize = map.isAvailableHousePoint(player, point);

            if (availableSize != null && Size.contains(availableSize, size)) {
                availableHousePointsWithinRadius.add(point);
            }
        }

        return availableHousePointsWithinRadius;
    }

    static Headquarter findHeadquarter(Player player) {

        for (Building b : player.getBuildings()) {
            if (b instanceof Headquarter) {
                return (Headquarter)b;
            }
        }

        return null;
    }

    static void fillRoadWithFlags(GameMap map, Road road) throws Exception {
        Player player = road.getPlayer();

        for (Point point : road.getWayPoints()) {
            if (map.isAvailableFlagPoint(road.getPlayer(), point)) {
                map.placeFlag(player, point);
            }
        }
    }
}
