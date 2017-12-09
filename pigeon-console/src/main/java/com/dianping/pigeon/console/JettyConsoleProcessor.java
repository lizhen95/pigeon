/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console;

import java.net.URL;

import com.dianping.pigeon.console.servlet.*;
import com.dianping.pigeon.console.servlet.json.*;
import com.dianping.pigeon.log.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.http.provider.JettyHttpServerProcessor;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class JettyConsoleProcessor implements JettyHttpServerProcessor {

    protected final Logger logger = LoggerLoader.getLogger(this.getClass());

    @Override
    public void preStart(ServerConfig serverConfig, Server server, Context context) {
        int port = server.getConnectors()[0].getPort();
        context.addServlet(new ServletHolder(new ServiceServlet(serverConfig, port)), "/services");
        context.addServlet(new ServletHolder(new ServicePublishServlet(serverConfig, port)), "/services.publish");
        context.addServlet(new ServletHolder(new ServiceUnpublishServlet(serverConfig, port)), "/services.unpublish");
        context.addServlet(new ServletHolder(new ServiceOnlineServlet(serverConfig, port)), "/services.online");
        context.addServlet(new ServletHolder(new ServiceOfflineServlet(serverConfig, port)), "/services.offline");

        context.addServlet(new ServletHolder(new RegionStatusServlet(serverConfig, port)), "/region");
        context.addServlet(new ServletHolder(new RequestQualityServlet(serverConfig, port)), "/requestQuality");

        ServiceJsonServlet serviceJsonServlet = new ServiceJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(serviceJsonServlet), "/services.json");
        context.addServlet(new ServletHolder(serviceJsonServlet), "/meta");

        InvokeJsonServlet invokeJsonServlet = new InvokeJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(invokeJsonServlet), "/invoke.json");
        context.addServlet(new ServletHolder(invokeJsonServlet), "/invoke");

        DependencyJsonServlet dependencyJsonServlet = new DependencyJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(dependencyJsonServlet), "/dependencies.json");
        context.addServlet(new ServletHolder(dependencyJsonServlet), "/dependencies");

        StatisticsJsonServlet statisticsJsonServlet = new StatisticsJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(statisticsJsonServlet), "/stats.json");
        context.addServlet(new ServletHolder(statisticsJsonServlet), "/stats");

        ServiceStatusJsonServlet serviceStatusJsonServlet = new ServiceStatusJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(serviceStatusJsonServlet), "/services.status");
        context.addServlet(new ServletHolder(serviceStatusJsonServlet), "/status");

        ProviderOnlineStatusServlet providerOnlineStatusServlet = new ProviderOnlineStatusServlet();
        context.addServlet(new ServletHolder(providerOnlineStatusServlet), "/onlineStatus");

        JarJsonServlet jarJsonServlet = new JarJsonServlet(serverConfig, port);
        context.addServlet(new ServletHolder(jarJsonServlet), "/jars");

        TraceStatsJsonServlet statsJsonServlet = new TraceStatsJsonServlet();
        context.addServlet(new ServletHolder(statsJsonServlet), "/trace");

        GroupInfoServlet groupInfoServlet = new GroupInfoServlet(serverConfig, port);
        context.addServlet(new ServletHolder(groupInfoServlet), "/group");

        ServletHolder holder = new ServletHolder(new DefaultServlet());
        URL url = JettyConsoleProcessor.class.getClassLoader().getResource("statics");
        if (url == null) {
            logger.error("can't find console static files!");
            return;
        }
        String staticsDir = url.toExternalForm();
        holder.setInitParameter("resourceBase", staticsDir);
        holder.setInitParameter("gzip", "false");
        // context.addServlet(holder, "/jquery/*");
        // context.addServlet(holder, "/ztree/*");
        // context.addServlet(holder, "/bootstrap/*");
        context.addServlet(holder, "/bootstrap/css/bootstrap-responsive.min.css");
        context.addServlet(holder, "/bootstrap/css/bootstrap.min.css");
        context.addServlet(holder, "/bootstrap/img/glyphicons-halflings-white.png");
        context.addServlet(holder, "/bootstrap/img/glyphicons-halflings.png");
        context.addServlet(holder, "/bootstrap/js/bootstrap.min.js");
        context.addServlet(holder, "/jquery/jquery-1.7.2.min.js");
        context.addServlet(holder, "/jquery/jquery-ui.css");
        context.addServlet(holder, "/jquery/jquery-ui.js");
        context.addServlet(holder, "/jquery/jquery.easy-confirm-dialog.js");
        context.addServlet(holder, "/ztree/img/diy/1_close.png");
        context.addServlet(holder, "/ztree/img/diy/1_open.png");
        context.addServlet(holder, "/ztree/img/diy/2.png");
        context.addServlet(holder, "/ztree/img/diy/3.png");
        context.addServlet(holder, "/ztree/img/diy/4.png");
        context.addServlet(holder, "/ztree/img/diy/5.png");
        context.addServlet(holder, "/ztree/img/diy/6.png");
        context.addServlet(holder, "/ztree/img/diy/7.png");
        context.addServlet(holder, "/ztree/img/diy/8.png");
        context.addServlet(holder, "/ztree/img/diy/9.png");
        context.addServlet(holder, "/ztree/img/line_conn.gif");
        context.addServlet(holder, "/ztree/img/loading.gif");
        context.addServlet(holder, "/ztree/img/zTreeStandard.gif");
        context.addServlet(holder, "/ztree/img/zTreeStandard.png");
        context.addServlet(holder, "/ztree/jquery.ztree.core-3.3.min.js");
        context.addServlet(holder, "/ztree/ztree.css");
    }

}
