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
import org.apache.velocity.tools.view.VelocityViewServlet;

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

		File classesFolder = new File("target/classes");
		if (classesFolder.exists())
			rootPath = classesFolder.getAbsolutePath();
		else
			rootPath = new File(".").getAbsolutePath();
		addServlet("/", new VelocityViewServlet());
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
}
