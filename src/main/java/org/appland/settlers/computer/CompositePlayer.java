/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.GameMap;
import static org.appland.settlers.model.Material.GOLD;
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
            militaryProducer.turn();
        } else {
            expandingPlayer.turn();
        }
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }
}
