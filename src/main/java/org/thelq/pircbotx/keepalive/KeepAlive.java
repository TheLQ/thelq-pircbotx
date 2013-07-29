/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
