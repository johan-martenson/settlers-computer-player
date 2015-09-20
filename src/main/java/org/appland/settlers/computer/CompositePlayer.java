/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.GameMap;
import static org.appland.settlers.model.Material.BEER;
import static org.appland.settlers.model.Material.BREAD;
import static org.appland.settlers.model.Material.COAL;
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

    public CompositePlayer(Player player, GameMap map) {
        this.player = player;
        this.map    = map;

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

        /* Determine if there is a need for maintenance */
        if (!constructionPlayer.basicConstructionDone()) {
            constructionPlayer.turn();
        } else if (!mineralsPlayer.allCurrentMineralsKnown()) {
            mineralsPlayer.turn();
        } else if (mineralsPlayer.hasMines() && !foodPlayer.basicFoodProductionDone()) {
            foodPlayer.turn();
        } else if (player.getInventory().get(GOLD) > 0 && !coinPlayer.coinProductionDone()) {
            coinPlayer.turn();
        } else if (mineralsPlayer.hasCoalMine() &&
                   mineralsPlayer.hasIronMine() &&
                   !foodPlayer.fullFoodProductionDone()) {
            foodPlayer.turn();
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
            previousPlayer = militaryProducer;
        } else {

            /* Change transport priorities if needed */
            if (previousPlayer != expandingPlayer) {
                player.setTransportPriority(0, GOLD);
                player.setTransportPriority(1, SHIELD);
                player.setTransportPriority(2, SWORD);
                player.setTransportPriority(3, IRON_BAR);
                player.setTransportPriority(4, BEER);
                player.setTransportPriority(5, COAL);
                player.setTransportPriority(6, IRON);
                player.setTransportPriority(7, WHEAT);
                player.setTransportPriority(8, WATER);
                player.setTransportPriority(9, MEAT);
                player.setTransportPriority(10, BREAD);
                player.setTransportPriority(11, FISH);
                player.setTransportPriority(11, FLOUR);
                player.setTransportPriority(11, WATER);
            }

            expandingPlayer.turn();
            previousPlayer = expandingPlayer;
        }
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }
}
