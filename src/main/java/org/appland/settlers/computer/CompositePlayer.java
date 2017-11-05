/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.CoalMine;
import org.appland.settlers.model.Countdown;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.GoldMine;
import org.appland.settlers.model.GraniteMine;
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
    private final SearchForMineralsPlayer       mineralsPlayer;
    private final FoodProducer                  foodPlayer;
    private final CoinProducer                  coinPlayer;
    private final MilitaryProducer              militaryProducer;
    private final ExpandLandPlayer              expandingPlayer;
    private final AttackPlayer                  attackingPlayer;
    private final Countdown                     countdown;

    private ComputerPlayer previousPlayer;
    private ComputerPlayer currentPlayer;
    private int counter;
    private final static int PERIODIC_ENEMY_SCAN = 100;
    private final static int PERIODIC_SCAN_FOR_NEW_MINERALS = 30;
    private final static int PERIODIC_TRANSPORT_PRIO_REVIEW = 200;
    private final static int COUNTER_MAX         = 1000;
    private final static int ATTACK_FOLLOW_UP    = 20;
    private final static int TIME_TO_WAIT_FOR_PROMOTED_SOLDIERS = 200;
    private final static int PERIODIC_LAKE_SCAN = 40;

    public CompositePlayer(Player player, GameMap map) {
        this.player = player;
        this.map    = map;
        counter     = 0;
        countdown   = new Countdown();

        constructionPlayer = new ConstructionPreparationPlayer(player, map);
        mineralsPlayer     = new SearchForMineralsPlayer(player, map);
        foodPlayer         = new FoodProducer(player, map);
        coinPlayer         = new CoinProducer(player, map);
        militaryProducer   = new MilitaryProducer(player, map);
        expandingPlayer    = new ExpandLandPlayer(player, map);
        attackingPlayer    = new AttackPlayer(player, map);

        /* Configure the players */
        expandingPlayer.setExpandTowardEnemies(true);
        expandingPlayer.waitForBuildingsToGetCompletelyOccupied(true);
    }

    @Override
    public void turn() throws Exception {

        /* Keep track of how many times the method is run to support periodic tasks */
        if (counter > COUNTER_MAX) {
            counter = 0;
        } else {
            counter++;
        }

        /* Remember the previous player to detect player changes */
        previousPlayer = currentPlayer;

        /* Tweak transport priority regularly */
        if (counter % PERIODIC_TRANSPORT_PRIO_REVIEW == 0) {

            player.setFoodQuota(CoalMine.class, 1);
            player.setFoodQuota(GoldMine.class, 1);
            player.setFoodQuota(IronMine.class, 1);
            player.setFoodQuota(GraniteMine.class, 1);

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
            tuneTransportPriorities();
        }

        /* Scan for new potential mines periodically */
        if (counter % PERIODIC_SCAN_FOR_NEW_MINERALS == 0 ) {
            mineralsPlayer.scanForNewMinerals();
        }

        /* Scan for lakes periodically */
        if (counter % PERIODIC_LAKE_SCAN == 0) {
            foodPlayer.scanForNewLakes();
        }

        /* Handle basic construction if it's not in place */
        if (!constructionPlayer.basicConstructionDone()) {
            constructionPlayer.turn();

            currentPlayer = constructionPlayer;

        /* Scan for minerals if there are unknown areas and re-scan periodically */
        } else if (!mineralsPlayer.allCurrentMineralsKnown()) {
            mineralsPlayer.turn();

            currentPlayer = mineralsPlayer;

        /* Build first level of food production if it's missing and there are mines needing it */
        } else if (mineralsPlayer.hasMines() && !foodPlayer.basicFoodProductionDone()) {
            foodPlayer.turn();

            currentPlayer = foodPlayer;

        /* Build up coin production if gold is available */
        } else if (player.getInventory().get(GOLD) > 0 && !coinPlayer.coinProductionDone()) {
            coinPlayer.turn();

            currentPlayer = coinPlayer;

        /* Build up full food production after the coin production is available */
        } else if (mineralsPlayer.hasCoalMine() &&
                   mineralsPlayer.hasIronMine() &&
                   !foodPlayer.fullFoodProductionDone()) {
            foodPlayer.turn();

            currentPlayer = foodPlayer;

        /* Build up military production when full food production is done */
        } else if (mineralsPlayer.hasCoalMine() &&
                   mineralsPlayer.hasIronMine() &&
                   !militaryProducer.productionDone()){

            militaryProducer.turn();

            currentPlayer = militaryProducer;

        /* Handle ongoing attacks */
        } else if (attackingPlayer.isAttacking() && counter % ATTACK_FOLLOW_UP == 0) {

            attackingPlayer.turn();

            currentPlayer = attackingPlayer;

        /* Handle the case where an ongoing attack has been won */
        } else if (attackingPlayer.hasWonBuildings()) {
            System.out.println("\nComposite player: Has won building\n");
            System.out.println("  " + attackingPlayer.getWonBuildings());

            /* Notify the expanding player about newly acquired enemy buildings */
            expandingPlayer.registerBuildings(attackingPlayer.getWonBuildings());
            attackingPlayer.clearWonBuildings();

        /* Look for enemies to attack */
        } else if (expandingPlayer.hasNewBuildings() || counter % PERIODIC_ENEMY_SCAN == 0) {

            expandingPlayer.clearNewBuildings();

            /* Wait with attack if there is gold available but not enough promotions yet */
            if (mineralsPlayer.hasGoldMine()) {

                /* Wait to get a chance to get promoted soldiers before attacking */
                if (countdown.isActive()) {
                    if (!countdown.reachedZero()) {
                        countdown.step();

                        return;
                    }
                } else {
                    countdown.countFrom(TIME_TO_WAIT_FOR_PROMOTED_SOLDIERS);

                    return;
                }
            }

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

                currentPlayer = attackingPlayer;
            } else {
                System.out.println("Composite player: Cannot attack enemy at " + enemyBuilding.getPosition());
            }

        /* Expand the land if there is nothing else to do */
        } else {

            expandingPlayer.turn();

            currentPlayer = expandingPlayer;
        }

        if (previousPlayer != currentPlayer) {
            System.out.println(" -- Switched to " + currentPlayer.getClass().getSimpleName() + " from " + previousPlayer);
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
        player.setTransportPriority(3, IRON_BAR);
        player.setTransportPriority(4, COAL);
        player.setTransportPriority(5, IRON);
        player.setTransportPriority(6, MEAT);
        player.setTransportPriority(7, BREAD);
        player.setTransportPriority(8, FISH);
        player.setTransportPriority(9, FLOUR);
        player.setTransportPriority(10, WHEAT);
        player.setTransportPriority(11, STONE);
        player.setTransportPriority(12, PLANCK);
        player.setTransportPriority(13, BEER);
        player.setTransportPriority(14, WOOD);
        player.setTransportPriority(15, WATER);

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

            if (player.getInventory().get(WATER) < 10) {
                player.setTransportPriority(1, WATER);
            }

            if (player.getInventory().get(WHEAT) < 10) {
                player.setTransportPriority(2, WHEAT);
            }
        }

        /* Then privates - handle weapons */
        if (player.getInventory().get(BEER) > 10) {
            player.setTransportPriority(0, SWORD);
            player.setTransportPriority(1, SHIELD);

            player.setTransportPriority(2, IRON_BAR);
            player.setTransportPriority(3, COAL);

            if (player.getInventory().get(IRON_BAR) < 5) {
                player.setTransportPriority(4, IRON);

                if (player.getInventory().get(COAL) < 5 ||
                    player.getInventory().get(IRON) < 5) {
                    player.setTransportPriority(5, BREAD);
                    player.setTransportPriority(6, FISH);
                    player.setTransportPriority(7, MEAT);

                    if (player.getInventory().get(BREAD) < 5) {
                        player.setTransportPriority(8, FLOUR);
                        player.setTransportPriority(9, WATER);

                        if (player.getInventory().get(FLOUR) < 5) {
                            player.setTransportPriority(9, WHEAT);
                        }
                    }
                }
            }
        }

        /* Then gold */
        if (player.getInventory().get(COIN) < 5) {
            player.setTransportPriority(0, COIN);
            player.setTransportPriority(1, COAL);
            player.setTransportPriority(2, GOLD);

            if (player.getInventory().get(COAL) < 5 ||
                player.getInventory().get(GOLD) < 5) {
                player.setTransportPriority(3, BREAD);
                player.setTransportPriority(4, FISH);
                player.setTransportPriority(5, MEAT);

                if (player.getInventory().get(BREAD) < 5) {
                    player.setTransportPriority(6, FLOUR);
                    player.setTransportPriority(7, WATER);

                    if (player.getInventory().get(FLOUR) < 5) {
                        player.setTransportPriority(7, WHEAT);
                    }
                }
            }
        }
    }
}
