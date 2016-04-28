/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.ArrayList;
import java.util.List;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Player;

/**
 *
 * @author johan
 */
public class AttackPlayer implements ComputerPlayer {
    private final GameMap  map;
    private final Player   player;
    private State          state;
    private Headquarter    headquarter;
    private List<Building> recentlyWonBuildings;
    private Building       buildingUnderAttack;

    enum State {
        INITIAL_STATE, 
        LOOK_FOR_BUILDINGS_TO_ATTACK, 
        ATTACKING,
    }

    public AttackPlayer(Player p, GameMap m) {
        player = p;
        map    = m;

        state = State.INITIAL_STATE;

        buildingUnderAttack = null;
        recentlyWonBuildings = new ArrayList<>();
    }

    @Override
    public void turn() throws Exception {

        /* Record the state before the turn */
        State stateBefore = state;

        /* Start with finding the headquarte */
        if (state == State.INITIAL_STATE) {

            /* Find headquarter */
            headquarter = Utils.findHeadquarter(player);

            /* Change the state to ready to build */
            state = State.LOOK_FOR_BUILDINGS_TO_ATTACK;
        } else if (state == State.LOOK_FOR_BUILDINGS_TO_ATTACK) {

            /* Find opponents' buildings within field of view */
            List<Building> visibleOpponentBuildings = Utils.findVisibleOpponentBuildings(map, player);

            /* Find a building to attack */
            Building buildingToAttack = findBuildingToAttack(visibleOpponentBuildings);

            /* Keep looking for buildings to attack if there is no building now */
            if (buildingToAttack == null) {
                return;
            }

            /* Attack the identified building */
            player.attack(buildingToAttack, player.getAvailableAttackersForBuilding(buildingToAttack));

            /* Change state to attacking */
            state = State.ATTACKING;

            buildingUnderAttack = buildingToAttack;
        } else if (state == State.ATTACKING) {

            /* Check if the attack is finished */
            if (!buildingUnderAttack.isUnderAttack()) {
                state = State.LOOK_FOR_BUILDINGS_TO_ATTACK;

                if (buildingUnderAttack.getPlayer().equals(player)) {
                    recentlyWonBuildings.add(buildingUnderAttack);
                }
            }
        }

        /* Print the old and new state if the state changed */
        if (stateBefore != state) {
            System.out.println("Transition: " + stateBefore + " -> " + state);
        }
    }

    @Override
    public Player getControlledPlayer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Building findBuildingToAttack(List<Building> visibleOpponentBuildings) throws Exception {

        for (Building building : visibleOpponentBuildings) {

            /* Filter out non-military buildings */
            if (!building.isMilitaryBuilding()) {
                continue;
            }

            int availableAttackers = player.getAvailableAttackersForBuilding(building);

            /* Filter out buildings that cannot be attacked */
            if (availableAttackers == 0) {
                continue;
            }

            /* Choose the first building that can be attacked */
            return building;
        }

        return null;
    }


    boolean hasWonBuildings() {
        return !recentlyWonBuildings.isEmpty();
    }

    void clearWonBuildings() {
        recentlyWonBuildings.clear();
    }

    public List<Building> getWonBuildings() {
        return recentlyWonBuildings;
    }
}
