package edu.kit.iti.formal.automation.testtables.model;

/*-
 * #%L
 * geteta
 * %%
 * Copyright (C) 2016 Alexander Weigl
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexander Weigl
 * @vesion 1 (10.12.16)
 */
public class Region extends State {
    private List<State> children = new ArrayList<>();

    public Region(int id) {
        super(id);
    }

    public List<State> getStates() {
        return children;
    }

    /**
     * @return
     */
    @Override
    public int count() {
        return children.stream().mapToInt(State::count).sum();
    }

    /**
     *
     * @return
     */
    public List<State> flat() {
        return children.stream()
                .flatMap((a) -> a.flat().stream())
                .collect(Collectors.toList());
    }
}
