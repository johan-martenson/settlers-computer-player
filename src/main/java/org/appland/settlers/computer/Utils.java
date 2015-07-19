/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.EndPoint;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.InvalidRouteException;
import org.appland.settlers.model.IronSmelter;
import org.appland.settlers.model.Land;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.Well;

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

        if (road == null) {
            return;
        }

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

    static Road connectPointToBuilding(Player player, GameMap map, Point start, Building building2) throws InvalidRouteException, Exception {
        Point end = building2.getFlag().getPosition();

        /* Return directly if they are already connected */
        if (map.findWayWithExistingRoads(start, end) != null) {
            return null;
        }

        /* Look for the closest flag with connection to the headquarter within a reasonable range */
        double distance = Double.MAX_VALUE;
        Point viaPoint = null;

        for (Point point : map.getPointsWithinRadius(start, 15)) {

            /* Avoid the source point itself */
            if (point.equals(end)) {
                continue;
            }

            /* Filter non-flag points */
            if (!map.isFlagAtPoint(point)) {
                continue;
            }

            /* Filter points that are not connected to the headquarter */
            List<Point> pathViaPointToHeadquarter = map.findWayWithExistingRoads(point, end);
            if (pathViaPointToHeadquarter == null) {
                continue;
            }

            /* Filter points that cannot be reached */
            List<Point> wayToViaPoint = map.findAutoSelectedRoad(player, start, point, null);
            if (wayToViaPoint == null) {
                continue;
            }

            double tempDistance = wayToViaPoint.size();

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

    static Point findConnectionToDestionationOrExistingRoad(Player player, GameMap map, Point start, Point end) throws InvalidRouteException, Exception {

        /* Look for the closest flag with connection to the headquarter within a reasonable range */
        double distance = Double.MAX_VALUE;
        Point viaPoint = null;

        for (Point point : map.getPointsWithinRadius(start, 15)) {

            /* Avoid the source point itself */
            if (point.equals(end)) {
                continue;
            }

            /* Filter non-flag points */
            if (!map.isFlagAtPoint(point)) {
                continue;
            }

            /* Filter points that are not connected to the headquarter */
            List<Point> pathViaPointToHeadquarter = map.findWayWithExistingRoads(point, end);
            if (pathViaPointToHeadquarter == null) {
                continue;
            }

            /* Filter points that cannot be reached */
            List<Point> wayToViaPoint = map.findAutoSelectedRoad(player, start, point, null);
            if (wayToViaPoint == null) {
                continue;
            }

            double tempDistance = wayToViaPoint.size();

            if (tempDistance < distance) {
                distance = tempDistance;

                viaPoint = point;
            }
        }

        /* Connect via the nearby flag if it existed */
        if (viaPoint != null) {
            return viaPoint;
        } else {
            return end;
        }
    }

    static List<Building> findVisibleOpponentBuildings(GameMap map, Player player) {

        /* Get the discovered land that is not owned by the player */
        Set<Point> allLand = new HashSet<>(player.getDiscoveredLand());

        /* Subtract the owned land */
        for (Land land : player.getLands()) {
            allLand.removeAll(land.getPointsInLand());
        }

        /* Collect all buildings in the remaining land */
        List<Building> visibleOpponentBuildings = new LinkedList<>();

        for (Building b : map.getBuildings()) {
            if (allLand.contains(b.getPosition())) {
                visibleOpponentBuildings.add(b);
            }
        }

        return visibleOpponentBuildings;
    }

    public static void repairConnection(GameMap map, Player player, Flag from, Flag to) throws Exception {

        System.out.println("Repairing connection");
        /* Get the connected flags on each side */
        Set<Flag> fromFlags = findConnectedFlags(map, from);
        Set<Flag> toFlags   = findConnectedFlags(map, to);

        /* Find a good-enough or any road that connects them */
        Flag flag1 = null;
        Flag flag2 = null;

        int distance = Integer.MAX_VALUE;

        /* Try all pairs of flags from the to and from set */
        for (Flag fromFlag : fromFlags) {

            for (Flag toFlag : toFlags) {

                int total = 0;

                /* Add the distance from the start flag to the current flag */
                if (!fromFlag.equals(from)) {
                    List<Point> path1 = map.findWayWithExistingRoads(from.getPosition(), fromFlag.getPosition());

                    if (path1 != null) {
                        total += path1.size();
                    }
                }

                /* Add the distance from the goal flag to the current flag */
                if (!toFlag.equals(to)) {
                    List<Point> path2 = map.findWayWithExistingRoads(to.getPosition(), toFlag.getPosition());

                    if (path2 != null) {
                        total += path2.size();
                    }
                }

                /* Determine if it's possible to connect the flags */
                List<Point> path = map.findAutoSelectedRoad(player, fromFlag.getPosition(), toFlag.getPosition(), null);

                /* Go to next pair if it's not possible to build a road between them */
                if (path == null) {
                    continue;
                }

                total = total + path.size();

                /* Look for the pair with the shortest total distance */
                if (total < distance) {
                    distance = total;

                    flag1 = fromFlag;
                    flag2 = toFlag;
                }
            }
        }

        /* Leave without placing road if no good flag pair was found */
        if (flag1 == null || flag2 == null) {
            return;
        }

        /* Place a new road to repair the connection */
        Road road = map.placeAutoSelectedRoad(player, flag1, flag2);

        /* Fill the new road with flags */
        fillRoadWithFlags(map, road);
    }

    public static Set<Flag> findConnectedFlags(GameMap map, Flag from) throws Exception {
        Set<Flag>  fromFlags     = new HashSet<>();
        List<Flag> flagsToSearch = new LinkedList<>();
        Set<Road>  searchedRoads = new HashSet<>();

        /* Set the root to search from */
        flagsToSearch.add(from);

        /* Find existing flags in the "to" network */
        while (!flagsToSearch.isEmpty()) {

            Flag flag = flagsToSearch.remove(0);
            fromFlags.add(flag);

            for (Road road : map.getRoadsFromFlag(flag)) {

                /* Skip already searched roads */
                if (searchedRoads.contains(road)) {
                    continue;
                }

                searchedRoads.add(road);

                /* Get the other end */
                EndPoint ep = road.getOtherFlag(flag);

                /* Filter buildings */
                if (map.isBuildingAtPoint(ep.getPosition())) {
                    continue;
                }

                /* Add the flag */
                flagsToSearch.add(map.getFlagAtPoint(ep.getPosition()));
            }
        }

        return fromFlags;
    }

	public static <T extends Building> boolean buildingsAreReady(List<T> buildings) {

	    for (T b : buildings) {
	        if (!b.ready()) {
	            return false;
	        }
	    }

	    return true;
	}

	public static <T> boolean buildingTypeExists(List<Building> buildings, Class<T> class1) {

	    for (Building b : buildings) {
	        if (b.getClass().equals(class1)) {
	            return true;
	        }
	    }

	    return false;
	}

	public static <T> List<T> getBuildingsOfType(List<Building> buildings, Class<T> class1) {
		List<T> result = new ArrayList<>();

		for (Building b : buildings) {

		    if (b.getClass().equals(class1)) {
		    	result.add((T)b);
		    }
		}

	    return result;
	}

	public static Point findPointForBuildingCloseToPoint(Point point, Size neededSize, Player controlledPlayer, GameMap map) throws Exception {

        /* Find a good point to build on, close to the given point */
        Point site = null;
        double distance = Double.MAX_VALUE;

        for (Land land : controlledPlayer.getLands()) {
            for (Point p : land.getPointsInLand()) {

                /* Filter out points where it's not possible to build */
                Size availableSize = map.isAvailableHousePoint(controlledPlayer, p);

                if (!Size.contains(availableSize, neededSize)) {
                    continue;
                }

                double tempDistance = p.distance(point);

                if (tempDistance < distance) {
                    site = p;
                    distance = tempDistance;
                }
            }
        }

        return site;
	}

    static <T extends Building> boolean listContainsAtLeastOneReadyBuilding(List<T> wells) {
        for (T b : wells) {
            if (b.ready()) {
                return true;
            }
        }

        return false;
    }
}
