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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.types.GenericEvent;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
@Slf4j
@ToString
@EqualsAndHashCode
public class CommandCall {
	//TODO: If moving to PircBotX, this should be a config option
	public static final String PREFIX = Main.PRODUCTION ? "?" : "!";
	/**
	 * The command the use is executing
	 */
	@NonNull
	public final String command;
	/**
	 *
	 */
	@NonNull
	public final ImmutableList<String> args;

	/**
	 * Checks if message starts with {@link #PREFIX} or various permutations of
	 * bots current nick
	 *
	 * @param event
	 * @param message
	 * @param command
	 * @return
	 */
	@Nullable
	public static CommandCall parse(@NonNull GenericEvent event, @NonNull String message, @NonNull String command) {
		String botNick = event.getBot().getNick();

		List<String> parts = trySplitCommandNoPrefix(message, PREFIX, command);
		if (parts == null)
			parts = trySplitCommandNoPrefix(message, botNick + " ", command);
		if (parts == null)
			parts = trySplitCommandNoPrefix(message, botNick + ": ", command);
		if (parts == null)
			return null;

		return new CommandCall(parts.remove(0), ImmutableList.copyOf(parts));
	}

	protected static List<String> trySplitCommandNoPrefix(String input, String prefix, String command) {
		if (StringUtils.startsWithIgnoreCase(input, prefix + command))
			return splitCommandQuotable(input.substring(prefix.length()));
		return null;
	}

	/**
	 * Split string, combining text in single or double quotes into one argument
	 * @param input
	 * @return 
	 */
	protected static List<String> splitCommandQuotable(String input) {
		//This isn't exactly fast but it works
		ArrayList<String> stringParts = new ArrayList<String>();
		if (input == null || input.length() == 0)
			return stringParts;

		//This probably doesn't have the best performance but its significantly easier to
		//do in your head than Utils.tokenizeLine()'s index-based approach. 
		//This also doesn't get called nearly as much
		String trimmedInput = CharMatcher.WHITESPACE.trimFrom(input);
		StringBuilder arg = new StringBuilder();
		Character splitChar = null;
		for (char lineChar : trimmedInput.toCharArray()) {
			if (lineChar == '\'' && (splitChar == null || splitChar == '\'')) {
				moveIfValue(arg, stringParts);
				if (splitChar == null) {
					splitChar = '\'';
				} else {
					splitChar = null;
				}
			} else if (lineChar == '"' && (splitChar == null || splitChar == '"')) {
				moveIfValue(arg, stringParts);
				if (splitChar == null) {
					splitChar = '"';
				} else {
					splitChar = null;
				}
			} else if (lineChar == ' ' && splitChar == null) {
				moveIfValue(arg, stringParts);
			} else {
				arg.append(lineChar);
			}
		}

		//No more spaces, add last part of line
		if (arg.length() > 0)
			stringParts.add(arg.toString());
		return stringParts;
	}

	protected static void moveIfValue(StringBuilder builder, List<String> args) {
		if (!StringUtils.isBlank(builder)) {
			args.add(builder.toString());
		}
		builder.setLength(0);
	}
}
