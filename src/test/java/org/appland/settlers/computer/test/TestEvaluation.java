package org.appland.settlers.computer.test;

import org.appland.settlers.computer.Evaluator;
import java.util.LinkedList;
import java.util.List;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.Cargo;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import static org.appland.settlers.model.Material.PLANCK;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Storage;
import org.appland.settlers.test.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class TestEvaluation {

    @Test
    public void testEvaluateMap() throws Exception {

        /* Create player list */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);

        List<Player> players = new LinkedList<>();
        
        players.add(player0);
        
        /* Create initial game map */
        GameMap map = new GameMap(players, 40, 40);
        
        /* Create evaluator */
        Evaluator evaluator = new Evaluator();
        
        /* Evaluate map */
        evaluator.evaluateMap(player0);
    }

    @Test
    public void testValueOfInitialMapIsPositive() throws Exception {

        /* Create player list */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        
        List<Player> players = new LinkedList<>();
        
        players.add(player0);
        
        /* Create initial game map */
        GameMap map = new GameMap(players, 40, 40);
        
        Point point0 = new Point(5, 5);
        Building headquarter0 = map.placeBuilding(new Headquarter(player0), point0);
        
        /* Create evaluator */
        Evaluator evaluator = new Evaluator();
        
        /* Verify that the value of an initial map is positive */
        assertTrue(evaluator.evaluateMap(player0) > 0);
    }

    @Test
    public void testPlancksUnderTenAreWorthTen() throws Exception {

        /* Create player list */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        
        List<Player> players = new LinkedList<>();
        
        players.add(player0);
        
        /* Create initial game map */
        GameMap map = new GameMap(players, 40, 40);
        
        Point point0 = new Point(5, 5);
        Storage headquarter0 = (Storage)map.placeBuilding(new Headquarter(player0), point0);
        
        /* Create evaluator */
        Evaluator evaluator = new Evaluator();
        
        /* Remove all plancks in the headquarter */
        Utils.adjustInventoryTo(headquarter0, PLANCK, 0, map);

        assertEquals(headquarter0.getAmount(PLANCK), 0);
        
        /* Verify that plancks below ten are worth ten */
        for (int i = 0; i < 10; i++) {
            int value = evaluator.evaluateMap(player0);

            Cargo cargo = new Cargo(PLANCK, map);

            headquarter0.putCargo(cargo);
            
            assertEquals(evaluator.evaluateMap(player0), (i + 1)*10);
        }
    }

    @Test
    public void testPlancksBetweenTenAndTwentyAreWorthFive() throws Exception {

        /* Create player list */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        
        List<Player> players = new LinkedList<>();
        
        players.add(player0);
        
        /* Create initial game map */
        GameMap map = new GameMap(players, 40, 40);
        
        Point point0 = new Point(5, 5);
        Storage headquarter0 = (Storage)map.placeBuilding(new Headquarter(player0), point0);
        
        /* Create evaluator */
        Evaluator evaluator = new Evaluator();
        
        /* Set the amount of plancks in the headquarter to 10 */
        Utils.adjustInventoryTo(headquarter0, PLANCK, 10, map);

        int worth = evaluator.evaluateMap(player0);
        
        assertEquals(headquarter0.getAmount(PLANCK), 10);
        
        /* Verify that plancks between ten and twenty are worth five */
        for (int i = 0; i < 10; i++) {
            int value = evaluator.evaluateMap(player0);

            Cargo cargo = new Cargo(PLANCK, map);

            headquarter0.putCargo(cargo);
            
            assertEquals(evaluator.evaluateMap(player0), (i + 1)*5 + worth);
        }
    }

    @Test
    public void testPlancksOverTwentyAreWorthOne() throws Exception {

        /* Create player list */
        Player player0 = new Player("Player 0", java.awt.Color.BLUE);
        
        List<Player> players = new LinkedList<>();
        
        players.add(player0);
        
        /* Create initial game map */
        GameMap map = new GameMap(players, 40, 40);
        
        Point point0 = new Point(5, 5);
        Storage headquarter0 = (Storage)map.placeBuilding(new Headquarter(player0), point0);
        
        /* Create evaluator */
        Evaluator evaluator = new Evaluator();
        
        /* Set the amount of plancks in the headquarter to 20 */
        Utils.adjustInventoryTo(headquarter0, PLANCK, 20, map);

        int worth = evaluator.evaluateMap(player0);
        
        assertEquals(headquarter0.getAmount(PLANCK), 20);
        
        /* Verify that plancks over twenty are worth one */
        for (int i = 0; i < 10; i++) {
            int value = evaluator.evaluateMap(player0);

            Cargo cargo = new Cargo(PLANCK, map);

            headquarter0.putCargo(cargo);
            
            assertEquals(evaluator.evaluateMap(player0), (i + 1) + worth);
        }
    }
}