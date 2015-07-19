/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;

/**
 *
 * @author johan
 */
public class CompositePlayer implements ComputerPlayer {
    private final Player player;
    private final GameMap map;
    private State state;
    private final ConstructionPreparationPlayer constructionPlayer;
    private final SearchForMineralsPlayer mineralsPlayer;
    private final FoodProducer foodPlayer;
    private final CoinProducer coinPlayer;
    private final MiltaryProducer militaryProducer;
    private final ExpandLandPlayer expandingPlayer;
    private final AttackPlayer attackingPlayer;

    private enum State {
        JUST_STARTED,
        ESTABLISHING_BASIC_CONSTRUCTION,
        LOOKING_FOR_MINERALS,
        BUILDING_FOOD_SUPPLY,
        STARTING_COIN_PRODUCTION,
        ESTABLISHING_MILITARY_PRODUCTION,
        EXPANDING_COUNTRY,
        ATTACKING, 
        STEADY_STATE
    }

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

        state = State.JUST_STARTED;
    }

    @Override
    public void turn() throws Exception {

        if (state == State.JUST_STARTED) {

            if (constructionPlayer.basicConstructionDone()) {
                state = State.LOOKING_FOR_MINERALS;
            } else {
                constructionPlayer.turn();
            }
        } else if (state == State.LOOKING_FOR_MINERALS) {

            if (mineralsPlayer.allCurrentMineralsKnown()) {
                state = State.STEADY_STATE;
            } else {
                mineralsPlayer.turn();
            }
        } else if (state == State.STEADY_STATE) {

            if ((mineralsPlayer.hasCoalMine() ||
                 mineralsPlayer.hasGoldMine() ||
                 mineralsPlayer.hasIronMine()) &&
                !foodPlayer.basicFoodProductionDone()) {
                state = State.BUILDING_FOOD_SUPPLY;
            } else if (mineralsPlayer.hasGoldMine() && 
                    mineralsPlayer.hasCoalMine() &&
                    !coinPlayer.coinProductionDone()) {
                System.out.println("GOING TO COIN PRODUCTION");
                    state = State.STARTING_COIN_PRODUCTION;
            } else if (mineralsPlayer.hasCoalMine() &&
                       mineralsPlayer.hasIronMine() &&
                       !militaryProducer.productionDone()){
                System.out.println("GOING TO MILITARY PRODUCTION");
                state = State.ESTABLISHING_MILITARY_PRODUCTION;
            } else {
                state = State.EXPANDING_COUNTRY;
            }
        } else if (state == State.BUILDING_FOOD_SUPPLY) {

            if (foodPlayer.basicFoodProductionDone()) {
                System.out.println("FOOD PRODUCTION DONE, GOING TO STEADY STATE");
                state = State.STEADY_STATE;
            } else {
                foodPlayer.turn();
            }
        } else if (state == State.STARTING_COIN_PRODUCTION) {

            if (coinPlayer.coinProductionDone()) {
                System.out.println("COIN PRODUCTION DONE, GOING TO STEADY STATE");
                state = State.STEADY_STATE;
            } else {
                coinPlayer.turn();
            }
        } else if (state == State.ESTABLISHING_MILITARY_PRODUCTION) {

            if (militaryProducer.productionDone()) {
                System.out.println("MILITARY PRODUCTION DONE, GOING TO STEADY STATE");
                state = State.STEADY_STATE;
            } else {
                militaryProducer.turn();
            }
        } else if (state == State.EXPANDING_COUNTRY) {
            expandingPlayer.turn();
        }
    }

    @Override
    public Player getControlledPlayer() {
        return player;
    }
}
