/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of TheLQ-PircBotX.
 *
 * TheLQ-PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TheLQ-PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TheLQ-PircBotX. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 *
 * @author Leon
 */
@Data
public class Stats {
	protected final PeriodFormatter periodFormatter = PeriodFormat.getDefault();
	protected final DateTime startTime = DateTime.now();
	protected AtomicInteger receivedMessages = new AtomicInteger(0);
	protected AtomicInteger receivedCommands = new AtomicInteger(0);

	public String getUptime() {
		return periodFormatter.print(new Duration(startTime, DateTime.now()).toPeriod());
	}
	
}
