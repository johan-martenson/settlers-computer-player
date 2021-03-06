package org.appland.settlers.test;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.Cargo;
import org.appland.settlers.model.Catapult;
import org.appland.settlers.model.Courier;
import org.appland.settlers.model.Crop;
import org.appland.settlers.model.Farmer;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Hunter;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Military;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Projectile;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.Stone;
import org.appland.settlers.model.Storehouse;
import org.appland.settlers.model.Tree;
import org.appland.settlers.model.WildAnimal;
import org.appland.settlers.model.Worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.appland.settlers.model.Crop.GrowthState.FULL_GROWN;
import static org.appland.settlers.model.Material.COAL;
import static org.appland.settlers.model.Material.COIN;
import static org.appland.settlers.model.Material.GENERAL;
import static org.appland.settlers.model.Material.GOLD;
import static org.appland.settlers.model.Material.IRON;
import static org.appland.settlers.model.Material.PLANK;
import static org.appland.settlers.model.Material.PRIVATE;
import static org.appland.settlers.model.Material.SERGEANT;
import static org.appland.settlers.model.Material.STONE;
import static org.appland.settlers.model.Size.LARGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Utils {

    public static void fastForward(int time, GameMap map) throws Exception {

        for (int i = 0; i < time; i++) {
            map.stepTime();
        }
    }

    public static List<Building> getNeedForMaterial(Material wood, List<Building> buildings) {

        List<Building> result = new ArrayList<>();

        for (Building building : buildings) {
            if (building.needsMaterial(wood)) {
                result.add(building);
            }
        }

        return result;
    }

    public static void fillUpInventory(Storehouse storehouse, Material material, int amount) throws Exception {
        Cargo cargo = new Cargo(material, null);

        for (int i = 0; i < amount; i++) {
            storehouse.putCargo(cargo);
        }
    }

    public static boolean materialIntMapIsEmpty(Map<Material, Integer> map) {
        boolean isEmpty = true;

        for (Material material : Material.values()) {
            if (map.get(material) != 0) {
                isEmpty = false;
            }
        }

        return isEmpty;
    }

    public static void assertNoStepDirectlyUpwards(List<Point> route) {
        Point previous = null;

        for (Point iterator : route) {
            if (previous == null) {
                previous = iterator;
                continue;
            }

            assertFalse(iterator.x == previous.x && abs(iterator.y - previous.y) == 2);

            previous = iterator;
        }
    }

    public static void fastForwardUntilWorkersReachTarget(GameMap map, Worker... workers) throws Exception {
        fastForwardUntilWorkersReachTarget(map, Arrays.asList(workers));
    }

    public static void fastForwardUntilWorkersReachTarget(GameMap map, List<Worker> workers) throws Exception {

        assertNotNull(map);
        assertFalse(workers.isEmpty());

        for (Worker worker : workers) {
            assertTrue(worker.isTraveling());
        }

        for (int i = 0; i < 1000; i++) {
            boolean allDone = true;

            for (Worker worker : workers) {
                if (!worker.isArrived()) {
                    allDone = false;
                }
            }

            if (allDone) {
                break;
            }

            map.stepTime();
        }
    }

    public static void fastForwardUntilWorkerReachesPoint(GameMap map, Worker worker, Point target) throws Exception {
        assertNotNull(target);
        assertNotNull(worker);
        assertNotNull(map);

        for (int i = 0; i < 1000; i++) {

            if (worker.isAt(target)) {
                break;
            }

            map.stepTime();
        }

        assertEquals(worker.getPosition(), target);
        assertTrue(worker.isAt(target));
    }

    public static <T extends Worker> T occupyBuilding(T worker, Building building) throws Exception {
        GameMap map = building.getMap();

        map.placeWorker(worker, building);
        building.assignWorker(worker);
        worker.enterBuilding(building);

        assertEquals(building.getWorker(), worker);

        return worker;
    }

    public static void fastForwardUntilTreeIsGrown(Tree tree, GameMap map) throws Exception {

        for (int i = 0; i < 500; i++) {
            map.stepTime();

            if (tree.getSize() == LARGE) {
                break;
            }
        }

        assertEquals(tree.getSize(), LARGE);
    }

    public static void fastForwardUntilCropIsGrown(Crop crop, GameMap map) throws Exception {

        for (int i = 0; i < 500; i++) {
            if (crop.getGrowthState() == FULL_GROWN) {
                break;
            }

            map.stepTime();
        }

        assertEquals(crop.getGrowthState(), FULL_GROWN);
    }

    public static void verifyListContainsWorkerOfType(List<Worker> workers, Class<? extends Worker> workerClass) {
        boolean found = false;

        for (Worker worker : workers) {
            if (worker.getClass().equals(workerClass)) {
                found = true;
                break;
            }
        }

        assertTrue(found);
    }

    public static void fastForwardUntilBuildingIsConstructed(Building building) throws Exception {
        GameMap map = building.getMap();

        for (int i = 0; i < 10000; i++) {
            if (building.isReady()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(building.isReady());
    }

    public static void fastForwardUntilBuildingIsOccupied(Building building) throws Exception {
        GameMap map = building.getMap();

        for (int i = 0; i < 1000; i++) {
            if (building.getWorker() != null) {
                break;
            }

            map.stepTime();
        }

        assertNotNull(building.getWorker());
    }

    public static void putGoldAtSurroundingTiles(Point point, Size size, GameMap map) {
        map.surroundPointWithMineral(point, GOLD, size);
    }

    public static void putIronAtSurroundingTiles(Point point, Size size, GameMap map) {
        map.surroundPointWithMineral(point, IRON, size);
    }

    public static void putCoalAtSurroundingTiles(Point point, Size size, GameMap map) {
        map.surroundPointWithMineral(point, COAL, size);
    }

    public static void putGraniteAtSurroundingTiles(Point point, Size size, GameMap map) {
        map.surroundPointWithMineral(point, STONE, size);
    }

    public static void putMineralWithinRadius(Material mineral, Point point1, int radius, GameMap map) {
        for (Point p : map.getPointsWithinRadius(point1, radius - 1)) {
            map.surroundPointWithMineral(p, mineral, LARGE);
        }
    }

    public static Courier occupyRoad(Road road, GameMap map) throws Exception {
        Courier courier = new Courier(road.getPlayer(), map);

        map.placeWorker(courier, road.getFlags()[0]);
        courier.assignToRoad(road);

        assertEquals(road.getCourier(), courier);

        return courier;
    }

    public static void adjustInventoryTo(Storehouse Storehouse, Material material, int amount) throws Exception {
        GameMap map = Storehouse.getMap();

        for (int i = 0; i < 1000; i++) {

            if (Storehouse.getAmount(material) == amount) {
                break;
            }

            if (Storehouse.getAmount(material) > amount) {

                if (material == PRIVATE || material == SERGEANT || material == GENERAL) {
                    Storehouse.retrieveMilitary(material);
                } else {
                    Storehouse.retrieve(material);
                }
            } else if (Storehouse.getAmount(material) < amount) {
                Storehouse.putCargo(new Cargo(material, map));
            }
        }

        assertEquals(Storehouse.getAmount(material), amount);
    }

    public static void constructHouse(Building building) throws Exception {
        GameMap map = building.getMap();

        assertTrue(building.isUnderConstruction());

        for (int i = 0; i < 20; i++) {
            if (building.needsMaterial(PLANK)) {
                try {
                    Cargo cargo = new Cargo(PLANK, map);

                    building.promiseDelivery(PLANK);
                    building.putCargo(cargo);
                } catch (Exception ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (building.needsMaterial(STONE)) {
                try {
                    Cargo cargo = new Cargo(STONE, map);

                    building.promiseDelivery(STONE);
                    building.putCargo(cargo);
                } catch (Exception ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        for (int i = 0; i < 500; i++) {
            if (building.isReady()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(building.isReady());
    }

    public static void fastForwardUntilWorkerCarriesCargo(GameMap map, Worker worker, Material material) throws Exception {

        for (int j = 0; j < 20000; j++) {
            if (worker.getCargo() != null && worker.getCargo().getMaterial().equals(material)) {
                break;
            }

            map.stepTime();
        }

        assertEquals(worker.getCargo().getMaterial(), material);
    }

    public static void fastForwardUntilWorkerCarriesCargo(GameMap map, Worker worker, Cargo cargo) throws Exception {
        for (int j = 0; j < 2000; j++) {
            if (cargo.equals(worker.getCargo())) {
                break;
            }

            map.stepTime();
        }

        assertEquals(worker.getCargo(), cargo);
    }

    public static void fastForwardUntilWorkerProducesCargo(GameMap map, Worker worker) throws Exception {

        for (int i = 0; i < 300; i++) {
            if (worker.getCargo() != null) {
                break;
            }

            map.stepTime();
        }

        assertNotNull(worker.getCargo());
    }

    public static void waitForMilitaryBuildingToGetPopulated(Building building, int nr) throws Exception {
        GameMap map = building.getMap();

        boolean populated = false;

        for (int i = 0; i < 1000; i++) {
            if (building.getNumberOfHostedMilitary() == nr) {
                populated = true;

                break;
            }

            map.stepTime();
        }

        assertTrue(populated);
        assertEquals(building.getNumberOfHostedMilitary(), nr);
    }

    public static void verifyPointIsWithinBorder(Player player, Point point) {
        boolean insideLand = false;

        for (Point landPoint : player.getLandInPoints()) {
            if (landPoint.equals(point)) {
                insideLand = true;

                break;
            }
        }

        assertFalse(player.getLandInPoints().contains(point));
        assertTrue(insideLand);
    }

    public static void verifyPointIsNotWithinBorder(Player player, Point point) {
        boolean insideLand = false;

        for (Point landPoint : player.getLandInPoints()) {
            if (landPoint.equals(point)) {
                insideLand = true;

                break;
            }
        }

        assertFalse(player.getLandInPoints().contains(point));
        assertFalse(insideLand);
    }

    public static void verifyDeliveryOfMaterial(GameMap map, Road road) throws Exception {
        Courier courier = road.getCourier();

        boolean delivery = false;

        for (int i = 0; i < 500; i++) {
            if (courier.getCargo() != null && courier.getCargo().getMaterial() == COIN) {
                delivery = true;

                break;
            }

            map.stepTime();
        }

        assertTrue(delivery);
    }

    public static void verifyNoDeliveryOfMaterial(GameMap map, Road road) throws Exception {
        Courier courier = road.getCourier();

        for (int i = 0; i < 500; i++) {
            if (courier.getCargo() != null && courier.getCargo().getMaterial() == COIN) {
                fail();
            }

            map.stepTime();
        }
    }

    public static void occupyMilitaryBuilding(Military.Rank rank, int amount, Building building, GameMap map) throws Exception {
        assertTrue(building.isReady());
        for (int i = 0; i < amount; i++) {
            occupyMilitaryBuilding(rank, building, map);
        }
    }

    public static Military occupyMilitaryBuilding(Military.Rank rank, Building building, GameMap map) throws Exception {
        Player player = building.getPlayer();

        Military military = new Military(player, rank, map);

        map.placeWorker(military, building);

        building.promiseMilitary(military);

        military.enterBuilding(building);

        return military;
    }

    public static Military findMilitaryOutsideBuilding(Player player, GameMap map) {
        Military attacker = null;
        for (Worker worker : map.getWorkers()) {
            if (worker instanceof Military && !worker.isInsideBuilding() && worker.getPlayer().equals(player)) {
                attacker = (Military)worker;
            }
        }

        return attacker;
    }

    public static List<Military> findMilitariesOutsideBuilding(Player player, GameMap map) {
        List<Military> result = new LinkedList<>();

        for (Worker worker : map.getWorkers()) {
            if (worker instanceof Military && !worker.isInsideBuilding() && worker.getPlayer().equals(player)) {
                result.add((Military)worker);
            }
        }

        return result;
    }

    public static void waitForWorkerToDisappear(Worker worker, GameMap map) throws Exception {
        for (int i = 0; i < 10000; i++) {
            if (!map.getWorkers().contains(worker)) {
                break;
            }

            map.stepTime();
        }

        assertFalse(map.getWorkers().contains(worker));
    }

    public static Military waitForMilitaryOutsideBuilding(Player player, GameMap map) throws Exception {
        for (int i = 0; i < 1000; i++) {
            Military military = findMilitaryOutsideBuilding(player, map);

            if (military != null) {
                assertEquals(military.getPlayer(), player);

                return military;
            }

            map.stepTime();
        }

        fail();

        return null;
    }

    public static <T extends Worker> List<T> findWorkersOfTypeOutsideForPlayer(Class<T> aClass, Player player, GameMap map) {
        List<T> workersFound = new LinkedList<>();

        for (Worker worker : map.getWorkers()) {
            if (worker.getClass().equals(aClass) && !worker.isInsideBuilding() && worker.getPlayer().equals(player)) {
                workersFound.add((T)worker);
            }
        }

        return workersFound;
    }

    public static <T extends Worker> List<T> waitForWorkersOutsideBuilding(Class<T> type, int nr, Player player, GameMap map) throws Exception {
        List<T> workers = new LinkedList<>();

        for (int i = 0; i < 1000; i++) {

            workers.clear();

            for (Worker worker : map.getWorkers()) {
                if (worker.getClass().equals(type) && !worker.isInsideBuilding() && player.equals(worker.getPlayer())) {
                    workers.add((T)worker);
                }
            }

            if (workers.size() == nr) {
                break;
            }

            map.stepTime();
        }

        assertEquals(workers.size(), nr);

        return workers;
    }

    public static <T extends Building> void waitForBuildingToDisappear(GameMap map, T building) throws Exception {
        assertTrue(building.isBurningDown() || building.isDestroyed());

        for (int i = 0; i < 1000; i++) {
            if (!map.getBuildings().contains(building)) {
                break;
            }

            map.stepTime();
        }

        assertFalse(map.getBuildings().contains(building));
    }

    static void waitForFightToStart(GameMap map, Military attacker, Military defender) throws Exception {

        for (int i = 0; i < 1000; i++) {
            if (attacker.isFighting() && defender.isFighting()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(defender.isFighting());
        assertTrue(attacker.isFighting());
    }

    static Military getMainAttacker(GameMap map, Building building, Collection<Military> attackers) throws Exception {
        Military firstAttacker = null;

        for (Military military : attackers) {
            if (military.getTarget().equals(building.getFlag().getPosition())) {
                firstAttacker = military;

                break;
            }

            map.stepTime();
        }

        assertNotNull(firstAttacker);

        return firstAttacker;
    }

    static Projectile waitForCatapultToThrowProjectile(Catapult catapult, GameMap map) throws Exception {
        Projectile projectile = null;

        assertTrue(map.getProjectiles().isEmpty());

        for (int i = 0; i < 1000; i++) {

            map.stepTime();

            if (!map.getProjectiles().isEmpty()) {

                projectile = map.getProjectiles().get(0);

                break;
            }
        }

        assertNotNull(projectile);

        return projectile;
    }

    static void waitForProjectileToReachTarget(Projectile projectile, GameMap map) throws Exception {

        for (int i = 0; i < 1000; i++) {

            if (projectile.isArrived()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(projectile.isArrived());
    }

    static WildAnimal waitForAnimalToAppear(GameMap map) throws Exception {

        for (int i = 0; i < 2000; i++) {

            if (!map.getWildAnimals().isEmpty()) {
                break;
            }

            map.stepTime();
        }

        assertFalse(map.getWildAnimals().isEmpty());

        return map.getWildAnimals().get(0);
    }

    static WildAnimal waitForWildAnimalCloseToPoint(Point point, GameMap map) throws Exception {
        WildAnimal animal = null;

        for (int i = 0; i < 5000; i++) {

            /* Check if there is a wild animal close to the hut */
            for (WildAnimal wa : map.getWildAnimals()) {
                if (wa.getPosition().distance(point) < 20) {
                    animal = wa;

                    break;
                }
            }

            /* Exit the loop if an animal was found */
            if (animal != null) {
                break;
            }

            map.stepTime();
        }

        assertNotNull(animal);
        assertTrue(animal.getPosition().distance(point) < 20);

        return animal;
    }

    static void waitForActorsToGetClose(Hunter hunter, WildAnimal animal, int distance, GameMap map) throws Exception {
        for (int i = 0; i < 5000; i++) {
            if (hunter.getPosition().distance(animal.getPosition()) <= distance) {
                break;
            }

            map.stepTime();
        }

        assertTrue(hunter.getPosition().distance(animal.getPosition()) <= distance);
    }

    static void fastForwardUntilWorkerCarriesNoCargo(GameMap map, Worker worker) throws Exception {

        for (int j = 0; j < 2000; j++) {
            if (worker.getCargo() == null) {
                break;
            }

            map.stepTime();
        }

        assertNull(worker.getCargo());
    }

    static void waitForCargoToReachTarget(GameMap map, Cargo cargo) throws Exception {

        for (int i = 0; i < 2000; i++) {

            if (cargo.getPosition().equals(cargo.getTarget().getPosition())) {
                break;
            }

            map.stepTime();
        }

        assertEquals(cargo.getPosition(), cargo.getTarget().getPosition());
    }

    static void waitUntilAmountIs(GameMap map, Building target, Material material, int amount) throws Exception {

        for (int i = 0; i < 2000; i++) {

            if (target.getAmount(material) == amount) {
                break;
            }

            map.stepTime();
        }

        assertEquals(target.getAmount(material), amount);
    }

    static void deliverCargo(Building building, Material material, GameMap map) throws Exception {
        Cargo cargo = new Cargo(material, map);

        building.promiseDelivery(material);

        building.putCargo(cargo);
    }

    static Cargo fastForwardUntilWorkerCarriesCargo(GameMap map, Worker worker) throws Exception {

        for (int i = 0; i < 2000; i++) {

            if (worker.getCargo() != null) {
                return worker.getCargo();
            }

            map.stepTime();
        }

        fail();

        return null;
    }

    static void waitForCropToGetReady(GameMap map, Crop crop) throws Exception {

        for (int i = 0; i < 1000; i++) {

            if (crop.getGrowthState() == Crop.GrowthState.FULL_GROWN) {
                break;
            }

            map.stepTime();
        }

        assertEquals(crop.getGrowthState(), Crop.GrowthState.FULL_GROWN);
    }

    static Crop waitForFarmerToPlantCrop(GameMap map, Farmer farmer0) throws Exception {

        waitForFarmerToStartPlanting(map, farmer0);

        Point position = farmer0.getPosition();

        assertFalse(map.isCropAtPoint(position));

        waitForFarmerToStopPlanting(map, farmer0);

        assertTrue(map.isCropAtPoint(position));

        return map.getCropAtPoint(position);
    }

    private static void waitForFarmerToStopPlanting(GameMap map, Farmer farmer) throws Exception {

        for (int i = 0; i < 10000; i++) {

            if (!farmer.isPlanting()) {
                break;
            }

            map.stepTime();
        }

        assertFalse(farmer.isPlanting());
    }

    private static void waitForFarmerToStartPlanting(GameMap map, Farmer farmer) throws Exception {

        for (int i = 0; i < 10000; i++) {

            if (farmer.isPlanting()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(farmer.isPlanting());
    }

    static void waitForCropToGetHarvested(GameMap map, Crop crop) throws Exception {

        for (int i = 0; i < 1000; i++) {

            if (crop.getGrowthState() == Crop.GrowthState.HARVESTED) {
                break;
            }

            map.stepTime();
        }

        assertEquals(crop.getGrowthState(), Crop.GrowthState.HARVESTED);
    }

    static int getAmountMilitary(Headquarter headquarter0) {
        return headquarter0.getAmount(PRIVATE)  +
               headquarter0.getAmount(SERGEANT) +
               headquarter0.getAmount(GENERAL);
    }

    static <T> int countNumberElementAppearsInList(List<T> transportPriorityList, T element) {
        int sum = 0;

        for (T type : transportPriorityList) {
            if (element == null) {
                if (type == null) {
                    sum++;
                }
            } else {
                if (element.equals(type)) {
                    sum++;
                }
            }
        }

        return sum;
    }

    static Building waitForBuildingToGetUpgraded(Building building) throws Exception {
        GameMap map = building.getMap();

        for (int i = 0; i < 10000; i++) {
            if (!map.getBuildingAtPoint(building.getPosition()).equals(building)) {
                break;
            }

            map.stepTime();
        }

        assertNotEquals(map.getBuildingAtPoint(building.getPosition()), building);

        return map.getBuildingAtPoint(building.getPosition());
    }

    static void waitForBuildingToBurnDown(Building building, GameMap map) throws Exception {
        for (int i = 0; i < 10000; i++) {

            if (building.isDestroyed()) {
                break;
            }

            map.stepTime();
        }

        assertTrue(building.isDestroyed());
    }

    static void removePiecesFromStoneUntil(Stone stone, int amountLeft) {
        for (int i = 0; i < 1000; i++) {
            if (stone.getAmount() > amountLeft) {
                stone.removeOnePart();
            }
        }

        assertEquals(stone.getAmount(), amountLeft);
    }

    static void waitForTreeToGetPlanted(GameMap map, Point point) throws Exception {
        for (int i = 0; i < 1000; i++) {
            if (map.isTreeAtPoint(point)) {
                break;
            }

            map.stepTime();
        }

        assertTrue(map.isTreeAtPoint(point));

    }

    public static void verifyWorkerStaysAtHome(Worker worker, GameMap map) throws Exception {
        for (int i = 0; i < 1000; i++) {
            assertEquals(worker.getHome().getPosition(), worker.getPosition());

            map.stepTime();
        }
    }

    public static void plantTreesOnPlayersLand(Player player) {

        for (Point point: player.getLandInPoints()) {
            try {
                player.getMap().placeTree(point);
            } catch (Exception e) {}
        }
    }

    public static void plantTreesOnPoints(List<Point> pointsWithinRadius, GameMap map) {
        for (Point point : pointsWithinRadius) {
            if (map.isTreeAtPoint(point)) {
                continue;
            }

            try {
                map.placeTree(point);
            } catch (Exception e) {}
        }
    }

    public static void putStonesOnPoints(List<Point> points, GameMap map) {
        for (Point point : points) {
            if (map.isStoneAtPoint(point)) {
                continue;
            }

            try {
                map.placeStone(point);
            } catch (Exception e) {}
        }
    }

    public static void putStoneOnOnePoint(List<Point> points, GameMap map) {
        for (Point point : points) {
            if (map.isStoneAtPoint(point)) {
                continue;
            }

            try {
                map.placeStone(point);
                break;
            } catch (Exception e) {}
        }
    }

    public static void putWildAnimalOnOnePoint(List<Point> points, GameMap map) {
        for (Point point : points) {
            try {
                map.placeWildAnimal(point);
                break;
            } catch (Exception e) {}
        }
    }
}
