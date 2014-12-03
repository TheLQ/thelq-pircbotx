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

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;

/**
 *
 * @author Leon
 */
@Slf4j
public class CommandCallTest {
	@DataProvider
	public Object[][] splitDataProvider() {
		return new Object[][]{
			new Object[]{
				"did you \"see that\"'stupid idiot' who \"likes\"to'quote' everything",
				Arrays.asList("did", "you", "see that", "stupid idiot", "who", "likes", "to", "quote", "everything")
			}, new Object[]{
				"did you see that stupid idiot who likes to quote everything",
				Arrays.asList("did", "you", "see", "that", "stupid", "idiot", "who", "likes", "to", "quote", "everything")
			}, new Object[]{
				"\"did\" you 'see that' stupid idiot who \"likes to quote\" everything",
				Arrays.asList("did", "you", "see that", "stupid", "idiot", "who", "likes to quote", "everything")
			}, new Object[]{
				"'did you'   \"see that\"    'stupid''idiot'\"who\" likes   \" to quote everything\"",
				Arrays.asList("did you", "see that", "stupid", "idiot", "who", "likes", " to quote everything")
			}, new Object[]{
				"'did\" you'   \"see' that\"    'stu\"pid''idiot'\"who\" likes   \" to quote everything\"",
				Arrays.asList("did\" you", "see' that", "stu\"pid", "idiot", "who", "likes", " to quote everything")
			},};
	}

	@Test(dataProvider = "splitDataProvider")
	public void split(String text, List<String> expected) {
		List<String> result = CommandCall.splitCommandQuotable(text);
		log.debug("text     {}", text);
		log.debug("expected {}", StringUtils.join(expected, "|"));
		log.debug("result   {}", StringUtils.join(result, "|"));
		assertEquals(expected.size(), result.size(), "size diff");
		int errors = 0;
		for (int i = 0; i < expected.size(); i++) {
			String expectedPart = expected.get(i);
			String resultPart = result.get(i);
			if (!expectedPart.equals(resultPart)) {
				log.error("Expected {} but got {}", expectedPart, resultPart);
				errors++;
			}
		}

		assertEquals(errors, 0, "Parse errors occured (see log)");
	}
}
