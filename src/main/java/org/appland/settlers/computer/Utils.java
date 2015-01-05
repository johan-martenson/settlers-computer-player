/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.EndPoint;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.InvalidRouteException;
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

    static void removeRoadWithoutAffectingOthers(GameMap map, Flag flag) throws Exception {

        /* Remove the road as long as it connects only in one direction and not to a building */
        while (map.getRoadsFromFlag(flag).size() == 1) {

            /* Get the connected road */
            Road road = map.getRoadsFromFlag(flag).get(0);

            /* Move the flag iterator to the other side */
            EndPoint otherSide = road.getOtherFlag(flag);

            /* Stop if the other side is a building */
            if (map.isBuildingAtPoint(otherSide.getPosition())) {
                break;
            }

            /* Get the flag at the position */
            Flag otherFlag = map.getFlagAtPoint(otherSide.getPosition());

            /* Remove the previous flag */
            map.removeFlag(flag);

            /* Move the flag iterator to the new flag */
            flag = otherFlag;
        }
    }

    public static double getDistanceToBorder(Point position, Player player0) {
        double distance = Double.MAX_VALUE;

        for (Collection<Point> border : player0.getBorders()) {
            for (Point borderPoint : border) {
                double tempDistance = position.distance(borderPoint);

                if (tempDistance < distance) {
                    distance = tempDistance;
                }
            }
        }

        return distance;
    }

    static Iterable<Point> getPointsBetweenRadiuses(GameMap map, Point point, int rMin, int rMax) {
        List<Point> result = new ArrayList<>();
    
        int x;
        int y;
        boolean rowFlip = false;
        
        for (y = point.y - rMax; y <= point.y + rMax; y++) {
            int startX = point.x - rMax;
            
            if (rowFlip) {
                startX++;
            }
            
            for (x = startX; x <= point.x + rMax; x += 2) {
                Point p = new Point(x, y);

                if (!map.isWithinMap(p)) {
                    continue;
                }
                
                double distance = point.distance(p);

                if (distance >= rMin && distance <= rMax) {
                    result.add(p);
                }
            }

            rowFlip = !rowFlip;
        }

        return result;
    }

    static Road placeRoadToHeadquarterOrExistingRoad(Player player, GameMap map, Building building1, Building building2) throws InvalidRouteException, Exception {
        Point start = building1.getFlag().getPosition();
        Point end   = building2.getFlag().getPosition();

        /* Look for close flag with connection to the headquarter */
        double distance = Double.MAX_VALUE;
        Point viaPoint = null;

        for (Point point : map.getPointsWithinRadius(start, 15)) {

            if (point.equals(end)) {
                continue;
            }

            if (map.findWayWithExistingRoads(point, end) == null) {
                continue;
            }

            if (map.findAutoSelectedRoad(player, start, point, null) == null) {
                continue;
            }

            double tempDistance = start.distance(point);

            if (tempDistance < distance) {
                distance = tempDistance;

                viaPoint = point;
            }
        }

        /* Connect via the nearby flag if it existed */
        if (viaPoint != null) {
            return map.placeAutoSelectedRoad(player, start, viaPoint);
        }

        /* Try to connect directly to the headquarter */
        if (map.findAutoSelectedRoad(player, start, end, null) != null) {
            return map.placeAutoSelectedRoad(player, start, end);
        }

        return null;
    }
}
