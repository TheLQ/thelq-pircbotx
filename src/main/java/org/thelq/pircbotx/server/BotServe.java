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
package org.thelq.pircbotx.server;

import Acme.Serve.Serve;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon
 */
public class BotServe extends Serve {
	protected final String rootPath;

	public BotServe(int port) {
		super(ImmutableMap.builder()
				.put(ARG_PORT, port)
				.put(ARG_NOHUP, "nohup")
				//.put(ARG_WORK_DIRECTORY, "c:\\users")
				.build(), System.out);

		File classesFolder = new File("src\\main\\resources");
		if (classesFolder.exists())
			rootPath = classesFolder.getAbsolutePath();
		else
			rootPath = new File(".").getAbsolutePath();

		VelocityViewServlet velocityServlet = new BotVelocityServlet();
		addServlet("/", velocityServlet);
		addServlet("/myServe", new HttpServlet() {
			@Override
			protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().write(getServletContext().getClass().toString());
			}
		});
	}

	@Override
	public String getRealPath(String path) {
		return rootPath + path;
	}
	
	

	@Slf4j
	@RequiredArgsConstructor
	protected static class BotVelocityServlet extends VelocityViewServlet {

		@Override
		protected void fillContext(Context context, HttpServletRequest request) {
			log.debug(getVelocityProperty("webapp.resource.loader.cache", "none at all"));
			context.put("manager", Main.MANAGER);
			String botIdRaw = request.getParameter("bot");
			if(StringUtils.isNotBlank(botIdRaw))
				context.put("bot", Main.MANAGER.getBotById(Integer.parseInt(botIdRaw)));
		}
	}
}
