/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer.test;

import java.util.List;
import org.appland.settlers.computer.Filter;
import org.appland.settlers.computer.Filter.Criteria;
import org.appland.settlers.model.ForesterHut;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Woodcutter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author johan
 */
public class TestFiltering {

    @Test
    public void testCreateFilterToObserveWoodcutters() {

        /* Create a filter to observe woodcutters */
        Filter filter = new Filter();
        
        filter.filterOnBuildingType(Woodcutter.class);
    }

    @Test
    public void testWoodcutterFilterHasOneCriteria() {

        /* Create a filter to observe woodcutters */
        Filter filter = new Filter();
        
        filter.filterOnBuildingType(Woodcutter.class);
    
        /* Verify that the filter has one criteria */
        List<Criteria> criterias = filter.getCriterias();
        
        assertNotNull(criterias);
        assertEquals(criterias.size(), 1);
    }

    @Test
    public void testBuildingTypeFilterMatchesCorrectly() {

        /* Create a filter to observe woodcutters */
        Filter filter = new Filter();
        
        filter.filterOnBuildingType(Woodcutter.class);

        /* Verify that the filter matches woodcutters */
        assertTrue(filter.matches(new Woodcutter(new Player("Player 0", java.awt.Color.BLUE))));
    }

    @Test
    public void testForesterHutFilterDoesNotMatchOtherBuilding() {

        /* Create a filter to observe woodcutters */
        Filter filter = new Filter();
        
        filter.filterOnBuildingType(ForesterHut.class);

        /* Verify that the filter matches woodcutters */
        assertFalse(filter.matches(new Woodcutter(new Player("Player 0", java.awt.Color.BLUE))));
    }
}
