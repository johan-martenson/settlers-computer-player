/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.computer;

import java.util.LinkedList;
import java.util.List;
import static org.appland.settlers.computer.Filter.Limit.ALL;
import static org.appland.settlers.computer.Filter.SearchFor.BUILDING_OF_TYPE;

/**
 *
 * @author johan
 */
public class Filter {
    private final List<Criteria>criterias;

    public enum SearchFor {
        BUILDING_OF_TYPE
    }

    enum Limit {
        ALL
    }

    public Filter() {
        criterias = new LinkedList<>();
    }

    public boolean matches(Object o) {
        for (Criteria criteria : criterias) {
            if (!criteria.matches(o)) {
                return false;
            }
        }

        return true;
    }

    public void filterOnBuildingType(Class type) {
        criterias.add(new BuildingOfTypeCriteria(type, ALL));
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public interface Criteria {
        public SearchFor getSearchType();
        public boolean matches(Object o);
    }

    private class BuildingOfTypeCriteria implements Criteria {
        private final Class type;

        public BuildingOfTypeCriteria(Class type, Limit limit) {
            this.type = type;
        }

        @Override
        public SearchFor getSearchType() {
            return BUILDING_OF_TYPE;
        }

        @Override
        public boolean matches(Object o) {
            return o.getClass().equals(type);
        }
    }
}
