package edu.kit.iti.formal.automation.datatypes.values;

/*-
 * #%L
 * iec61131lang
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

import lombok.*;
import lombok.experimental.Tolerate;

/**
 * <p>TimeOfDayValue class.</p>
 *
 * @author weigl
 * @version $Id: $Id
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class TimeOfDayValue {
    private int hours, minutes, seconds, millieseconds;

    public TimeOfDayValue(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public TimeOfDayValue(int hour, int min, int sec, int ms) {
        this(hour, min, sec);
        millieseconds = ms;
    }
}
