/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of TheLQ-PircBotX.
 *
 * TheLQ-PircBotX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * TheLQ-PircBotX is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * TheLQ-PircBotX. If not, see <http://www.gnu.org/licenses/>.
 */
package org.thelq.pircbotx.commands.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.UserListEvent;
import org.pircbotx.hooks.events.WhoisEvent;
import org.pircbotx.hooks.types.GenericEvent;

/**
 *
 * @author Leon
 */
@Slf4j
public class AdminsCommand extends SimpleCommand {
	public static final long IDENTIFY_WAIT_MSEC = 1000 * 5;
	public final Object stateLock = new Object();
	public final Map<String, CountDownLatch> adminInactivePings = new HashMap<>();
	public final ImmutableList<String> adminNicks;
	public final List<String> adminsActive = new ArrayList<>();

	public AdminsCommand(List<String> adminNicks) {
		super("admins", "get admins");
		this.adminNicks = ImmutableList.copyOf(adminNicks);
	}

	/**
	 * Send whois to all admins
	 */
	@Override
	public void onConnect(ConnectEvent event) throws Exception {
		//Send all
		sendWhois(event, adminsInactive());
	}

	/**
	 * Send whois to all admins from servers given WHO list
	 */
	@Override
	public void onUserList(UserListEvent event) throws Exception {
		sendWhois(event, event.getUsers().stream().map(u -> u.getNick()).collect(Collectors.toList()));
	}

	/**
	 * Send whois to a user who just changed to an admin nick
	 */
	@Override
	public void onNickChange(NickChangeEvent event) throws Exception {
		sendWhois(event, ImmutableList.of(event.getNewNick()));
	}

	@Synchronized("stateLock")
	protected void sendWhois(GenericEvent event, Iterable<String> nicks) {
		List<String> inactiveAdmins = adminsInactive();
		for (String nick : nicks)
			if (inactiveAdmins.contains(nick))
				event.getBot().sendIRC().whois(nick);
	}

	/**
	 * On WhoisEvent's for admin nicks that exist, check if registered with
	 * nickserv. If they exist but aren't registered, send WHOIS again after
	 * {@link #IDENTIFY_WAIT_MSEC}
	 */
	@Override
	public void onWhois(WhoisEvent event) throws Exception {
		synchronized (stateLock) {
			List<String> channels = event.getBot().getUserChannelDao().getAllChannels()
					.stream()
					.map((c) -> c.getName())
					.collect(Collectors.toList());
			String nick = event.getNick();
			if (!adminNicks.contains(nick) || adminsActive.contains(nick))
				return;
			if (!event.isExists() || !channels.removeAll(event.getChannels())) {
				log.debug("Going to wait for user {} to join or message us", event.getNick());
				return;
			}
			if (event.isRegistered()) {
				log.info("Nick " + event.getNick() + " is registered");
				event.getBot().getUserChannelDao().getUser(event.getNick()).send().notice("Logged in as admin");
				adminsActive.add(event.getNick());
				return;
			}
		}

		//Have to re-whois the user
		log.info("User " + event.getNick() + " exists but is not registered, sending WHOIS again in " + IDENTIFY_WAIT_MSEC + "msec");
		Thread.sleep(IDENTIFY_WAIT_MSEC);
		event.getBot().sendIRC().whois(event.getNick());
	}

	public boolean isAdmin(User user) throws InterruptedException {
		if (!adminsActive.contains(user.getNick())) {
			String message = "Unknown user " + user.getNick() + " tried doing an admin action";
			log.warn(message);
			adminNotice(user.getBot(), message);
			return false;
		}
		return true;
	}

	public void adminNotice(PircBotX bot, String message) {
		for (String curAdmin : adminsActive)
			bot.sendIRC().notice(curAdmin, message);
	}

	public ImmutableList<String> adminsInactive() {
		List<String> admins = Lists.newArrayList(adminNicks);
		admins.removeAll(adminsActive);
		return ImmutableList.copyOf(admins);
	}

	@Override
	public void onCommand(GenericEvent event, Channel channel, User user, ImmutableList<String> args) {
		List<String> adminsActiveNick = Lists.newArrayList();
		event.respond("Active: " + adminsActiveNick.toString());
	}
}
