/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thelq.pircbotx.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Leon
 */
@Slf4j
public class PingServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("Received keepalive GET - " + request.getPathInfo());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Received keepalive POST - " + request.getPathInfo());
	}

}
