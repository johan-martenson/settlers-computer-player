/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.Headquarter;
import static org.appland.settlers.model.Material.PLANCK;
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
                headquarter = (Headquarter) b;

                break;
            }
        }
        
        if (headquarter != null) {
            int plancks = headquarter.getAmount(PLANCK);
            
            if (plancks > 0 && plancks < 11) {
                value += plancks * 10;

            } else if (plancks > 10 && plancks < 21) {
                value += 10 * 10;
                plancks -= 10;
                
                value += plancks * 5;
            } else if (plancks > 20) {
                value += 10 * 10;
                plancks -= 10;
                
                value += 10 * 5;
                plancks -= 10;
                
                value += plancks;
            }
        }
        
        return value;
    }
}
