/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.CoalMine;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.GoldMine;
import org.appland.settlers.model.IronMine;
import static org.appland.settlers.model.Material.BEER;
import static org.appland.settlers.model.Material.BREAD;
import static org.appland.settlers.model.Material.COAL;
import static org.appland.settlers.model.Material.COIN;
import static org.appland.settlers.model.Material.FISH;
import static org.appland.settlers.model.Material.FLOUR;
import static org.appland.settlers.model.Material.GOLD;
import static org.appland.settlers.model.Material.IRON;
import static org.appland.settlers.model.Material.IRON_BAR;
import static org.appland.settlers.model.Material.MEAT;
import static org.appland.settlers.model.Material.PLANCK;
import static org.appland.settlers.model.Material.SHIELD;
import static org.appland.settlers.model.Material.STONE;
import static org.appland.settlers.model.Material.SWORD;
import static org.appland.settlers.model.Material.WATER;
import static org.appland.settlers.model.Material.WHEAT;
import static org.appland.settlers.model.Material.WOOD;
import org.appland.settlers.model.Player;

/**
 *
 * @author johan
 */
public class CompositePlayer implements ComputerPlayer {
    private final Player player;
    private final GameMap map;
    private final ConstructionPreparationPlayer constructionPlayer;
    private final SearchForMineralsPlayer mineralsPlayer;
    private final FoodProducer foodPlayer;
    private final CoinProducer coinPlayer;
    private final MiltaryProducer militaryProducer;
    private final ExpandLandPlayer expandingPlayer;
    private final AttackPlayer attackingPlayer;

    private ComputerPlayer previousPlayer;
    private int counter;
    private final static int PERIODIC_ENEMY_SCAN = 100;
    private final static int PERIODIC_PRIO_SCAN  = 100;
    private final static int COUNTER_MAX         = 1000;
    private final static int ATTACK_FOLLOW_UP    = 20;

    public CompositePlayer(Player player, GameMap map) {
        this.player = player;
        this.map    = map;
        counter     = 0;

        constructionPlayer = new ConstructionPreparationPlayer(player, map);
        mineralsPlayer     = new SearchForMineralsPlayer(player, map);
        foodPlayer         = new FoodProducer(player, map);
        coinPlayer         = new CoinProducer(player, map);
        militaryProducer   = new MiltaryProducer(player, map);
        expandingPlayer    = new ExpandLandPlayer(player, map);
        attackingPlayer    = new AttackPlayer(player, map);
    }

    @Override
    public void turn() throws Exception {

        if (counter > COUNTER_MAX) {
            counter = 0;
        } else {
            counter++;
        }

        /* Determine if there is a need for maintenance */
        if (!constructionPlayer.basicConstructionDone()) {
            constructionPlayer.turn();

            if (previousPlayer != constructionPlayer) {
                System.out.println(" -- Switched to construction player");
            }

            previousPlayer = constructionPlayer;

        } else if (!mineralsPlayer.allCurrentMineralsKnown()) {
            mineralsPlayer.turn();

            if (previousPlayer != mineralsPlayer) {
                System.out.println(" -- Switched to minerals player");
            }

            previousPlayer = mineralsPlayer;

        } else if (mineralsPlayer.hasMines() && !foodPlayer.basicFoodProductionDone()) {
            foodPlayer.turn();

            if (previousPlayer != foodPlayer) {
                System.out.println(" -- Switched to food player");
            }

            previousPlayer = foodPlayer;

        } else if (player.getInventory().get(GOLD) > 0 && !coinPlayer.coinProductionDone()) {
            coinPlayer.turn();

            if (previousPlayer != coinPlayer) {
                System.out.println(" -- Switched to coin player");
            }

            previousPlayer = coinPlayer;

        } else if (mineralsPlayer.hasCoalMine() &&
                   mineralsPlayer.hasIronMine() &&
                   !foodPlayer.fullFoodProductionDone()) {
            foodPlayer.turn();

            if (previousPlayer != foodPlayer) {
                System.out.println(" -- Switched to food player");
            }

            previousPlayer = foodPlayer;
        } else if (mineralsPlayer.hasCoalMine() && 
                   mineralsPlayer.hasIronMine() &&
                   !militaryProducer.productionDone()){

            /* Change transport priorities if needed */
            if (previousPlayer != expandingPlayer) {
                player.setTransportPriority(0, PLANCK);
                player.setTransportPriority(1, STONE);
                player.setTransportPriority(2, WOOD);
            }

            militaryProducer.turn();

            if (previousPlayer != militaryProducer) {
                System.out.println(" -- Switched to military player");
            }

            previousPlayer = militaryProducer;
        } else if (attackingPlayer.isAttacking() && counter % ATTACK_FOLLOW_UP == 0) {

            if (previousPlayer != attackingPlayer) {
                System.out.println(" -- Switched to attacking player");
            }

            attackingPlayer.turn();

            previousPlayer = attackingPlayer;
        } else if (attackingPlayer.hasWonBuildings()) {
            System.out.println("\nComposite player: Has won building\n");
            System.out.println("  " + attackingPlayer.getWonBuildings());

            /* Notify the expanding player about newly acquired enemy buildings */
            expandingPlayer.registerBuildings(attackingPlayer.getWonBuildings());
            attackingPlayer.clearWonBuildings();
        } else if (expandingPlayer.hasNewBuildings() || counter % PERIODIC_ENEMY_SCAN == 0) {

            expandingPlayer.clearNewBuildings();

            /* Look for enemies close by to attack */
            Building enemyBuilding = Utils.getCloseEnemyBuilding(player);

            if (enemyBuilding == null) {
                System.out.println("Composite player: No close enemy to attack");
                return;
            }

            /* Attack if possible */
            if (player.getAvailableAttackersForBuilding(enemyBuilding) > 0) {
                System.out.println("Composite player: Can attack");
                attackingPlayer.turn();

                if (previousPlayer != attackingPlayer) {
                    System.out.println(" -- Switched to attacking player");
                }
            } else {
                System.out.println("Composite player: Cannot attack enemy at " + enemyBuilding.getPosition());
            }

            previousPlayer = attackingPlayer;
        } else {

            player.setFoodQuota(CoalMine.class, 1);
            player.setFoodQuota(GoldMine.class, 1);
            player.setFoodQuota(IronMine.class, 1);

            if (player.getInventory().get(COAL) < 5) {
                player.setFoodQuota(CoalMine.class, 10);
            }

            if (player.getInventory().get(GOLD) < 5) {
                player.setFoodQuota(GoldMine.class, 10);
            }

            if (player.getInventory().get(IRON) < 5) {
                player.setFoodQuota(IronMine.class, 10);
            }

            /* Change transport priorities if needed */
            if (previousPlayer != expandingPlayer || counter % PERIODIC_PRIO_SCAN == 0) {
                tuneTransportPriorities();
            }

            expandingPlayer.turn();

            if (previousPlayer != expandingPlayer) {
                System.out.println(" -- Switched to expanding player");
            }

            previousPlayer = expandingPlayer;
        }
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }

    private void tuneTransportPriorities() {

        /* Create a baseline for materials that tend to overflow */

        player.setTransportPriority(0, GOLD);
        player.setTransportPriority(1, SHIELD);
        player.setTransportPriority(2, SWORD);
        player.setTransportPriority(4, PLANCK);
        player.setTransportPriority(3, IRON_BAR);
        player.setTransportPriority(5, COAL);
        player.setTransportPriority(6, STONE);
        player.setTransportPriority(7, IRON);
        player.setTransportPriority(8, MEAT);
        player.setTransportPriority(9, BREAD);
        player.setTransportPriority(10, FISH);
        player.setTransportPriority(11, FLOUR);
        player.setTransportPriority(12, WHEAT);
        player.setTransportPriority(13, PLANCK);
        player.setTransportPriority(14, BEER);
        player.setTransportPriority(15, WOOD);
        player.setTransportPriority(16, WATER);

        /* Main priority: GOLD, PRIVATE, PLANCKS, STONES
           Handle backwards to get the priority right 
        */

        /* First stones */
        if (player.getInventory().get(STONE) < 20) {
            player.setTransportPriority(0, STONE);
        }

        /* Then plancks */
        if (player.getInventory().get(PLANCK) < 20) {
            player.setTransportPriority(0, PLANCK);
            player.setTransportPriority(1, WOOD);
        }

        /* Then privates - handle beer */
        if (player.getInventory().get(BEER) < 5) {
            player.setTransportPriority(0, BEER);
            player.setTransportPriority(1, WATER);
            player.setTransportPriority(2, WHEAT);
        }

        /* Then privates - handle weapons */
        if (player.getInventory().get(BEER) > 10) {
            player.setTransportPriority(0, SWORD);
            player.setTransportPriority(1, SHIELD);

            player.setTransportPriority(2, IRON_BAR);

            if (player.getInventory().get(IRON_BAR) < 5) {
                player.setTransportPriority(3, COAL);
                player.setTransportPriority(4, IRON);

                player.setTransportPriority(5, BREAD);
                player.setTransportPriority(6, FISH);
                player.setTransportPriority(7, MEAT);

                if (player.getInventory().get(BREAD) < 5) {
                    player.setTransportPriority(8, FLOUR);
                    player.setTransportPriority(9, WATER);

                    if (player.getInventory().get(FLOUR) < 5) {
                        player.setTransportPriority(10, WHEAT);
                    }
                }
            }
        }

        /* Then gold */
        if (player.getInventory().get(COIN) < 5) {
            player.setTransportPriority(0, COAL);
            player.setTransportPriority(1, GOLD);
        }
    }
}
