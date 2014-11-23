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

import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashMap;
import java.util.Map;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;

/**
 *
 * @author Leon
 */
public class StatsMultiBotManager extends MultiBotManager {
	protected Map<Integer, Stats> statsMap = new HashMap<Integer, Stats>();

	public Stats getStats(int botNum) {
		return statsMap.get(botNum);
	}

	@Override
	protected ListenableFuture<Void> startBot(PircBotX bot) {
		statsMap.put(bot.getBotId(), new Stats());
		return super.startBot(bot);
	}
	
}
