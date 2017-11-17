/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.Headquarter;
import static org.appland.settlers.model.Material.PLANK;
import org.appland.settlers.model.Player;



/**
 *
 * @author johan
 */
public class Evaluator {
    public int evaluateMap(Player player) {
        int value = 0;

        /* Calculate value from inventory */
        Building headquarter = null;

        for (Building b : player.getBuildings()) {
            if (b instanceof Headquarter) {
                headquarter = b;

                break;
            }
        }

        if (headquarter != null) {
            int planks = headquarter.getAmount(PLANK);

            if (planks > 0 && planks < 11) {
                value += planks * 10;

            } else if (planks > 10 && planks < 21) {
                value += 10 * 10;
                planks -= 10;

                value += planks * 5;
            } else if (planks > 20) {
                value += 10 * 10;
                planks -= 10;

                value += 10 * 5;
                planks -= 10;

                value += planks;
            }
        }

        return value;
    }
}
