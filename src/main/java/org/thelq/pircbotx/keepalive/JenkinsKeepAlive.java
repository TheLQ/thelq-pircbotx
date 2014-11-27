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
/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.keepalive;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.PircBotX;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon
 */
@Slf4j
public class JenkinsKeepAlive {
	protected static boolean created = false;

	public static void create() {
		Preconditions.checkState(!created, "Already created");
		created = true;
		ImmutableList.Builder<String> jenkinsBotsBuilder = ImmutableList.builder();
		for (int i = 1;; i++) {
			
			String value = "";
			if (value == null)
				break;
			jenkinsBotsBuilder.add(value);
		}

		ImmutableList<String> jenkinsBots = jenkinsBotsBuilder.build();
		if (jenkinsBots.size() == 0)
			throw new RuntimeException("No jenkins bots setup!");
		log.info("Created jenkins keep alive for " + jenkinsBots.toString());
		KeepAlive.getExecutor().scheduleAtFixedRate(new JenkinsRunner(jenkinsBots), 0, 15, TimeUnit.MINUTES);
	}

	@RequiredArgsConstructor
	@Slf4j
	public static class JenkinsRunner implements Runnable {
		protected final ImmutableList<String> jenkinsBots;

		@Override
		public void run() {
			log.info("Running jenkins keepalive");
			for (PircBotX bot : Main.MANAGER.getBots())
				for (String curJenkinsBot : jenkinsBots) {
					if (!bot.getUserChannelDao().containsUser(curJenkinsBot))
						continue;
					bot.sendIRC().message(curJenkinsBot, curJenkinsBot + ": jobs");
				}
		}
	}
}
