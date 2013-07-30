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
package org.thelq.pircbotx.keepalive;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.LazyInitializer;

/**
 *
 * @author Leon
 */
public class KeepAlive {
	protected static final ConcurrentInitializer<ScheduledExecutorService> executor = new AtomicSafeInitializer<ScheduledExecutorService>() {
		@Override
		protected ScheduledExecutorService initialize() {
			return Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder()
					.daemon(true)
					.namingPattern("keepalive-%d")
					.build());
		}
	};

	public static ScheduledExecutorService getExecutor() {
		try {
			return executor.get();
		} catch (ConcurrentException ex) {
			throw new RuntimeException("Cannot get executor", ex);
		}
	}
}
