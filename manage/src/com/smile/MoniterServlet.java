package com.smile;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import manage.Managable;
import manage.Moniter;

/**
 * Servlet implementation class MoniterServlet
 */
public class MoniterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public MoniterServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Managable manager = (Managable) request;
		Moniter moniter = manager.getMoniter();
		int qps = moniter.getQPS();
		int concurrency = moniter.getConcurrency();
		long runtime = moniter.getRuntime();
		long totalCount = moniter.getTotalCount();
		
		StringBuilder info = new StringBuilder();
		info.append("{\"qps\":").append(qps);
		info.append(",\"concurrency\":").append(concurrency);
		info.append(",\"runtime\":").append(runtime);
		info.append(",\"totalCount\":").append(totalCount);
		info.append("}");
		response.setContentType("application/json");
		response.getWriter().write(info.toString());
	}

}
