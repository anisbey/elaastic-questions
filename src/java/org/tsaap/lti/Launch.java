/*
 * Copyright (C) 2013-2016 Université Toulouse 3 Paul Sabatier
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tsaap.lti;

import grails.plugins.springsecurity.BCryptPasswordEncoder;
import grails.plugins.springsecurity.SpringSecurityService;
import groovy.sql.Sql;
import org.apache.log4j.Logger;
import org.tsaap.directory.UserProvisionAccountService;
import org.tsaap.lti.tp.Callback;
import org.tsaap.lti.tp.DataConnector;
import org.tsaap.lti.tp.ToolProvider;
import org.tsaap.lti.tp.dataconnector.JDBC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Servlet dedicated to the activity launch through LTI protocol
 * @author DROL
 * @author FSIL
 */
public class Launch extends HttpServlet implements Callback {

    private static final long serialVersionUID = 7955577706944298060L;
    private Db db;
    private LmsUserService lmsUserService;
    private LmsContextService lmsContextService;
    private static Logger logger = Logger.getLogger(Launch.class);


    /**
     * Process the http post request for launching the tool provider activity
     * @param request the request
     * @param response the response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        startNewSession(request);
        DataConnector dc = getDataConnector(request);
        ToolProvider tp = getToolProvider(request, response, dc);
        tp.execute();
    }

    /**
     * Execute the launch of the tool provider activity
     * @param toolProvider the tool provider
     * @return true if the launch is OK
     */
    @Override
    public boolean execute(ToolProvider toolProvider) {
        if (!isAnAuthorizedUserRole(toolProvider)) {
            toolProvider.setReason("Invalid role.");
            return false;
        }
        try {
            initializeUserSession(toolProvider);

            initialiseLmsUserService();
            Sql sql = getSql();
            LmsUser lmsUser = getLmsUser(sql, toolProvider);

            initialiseLmsContextService();
            LmsContext lmsContext = getLmsContext(sql, toolProvider, lmsUser);

            String serverUrl = getServerUrl(toolProvider, lmsUser, lmsContext) ;
            toolProvider.setRedirectUrl(serverUrl);
        } finally {
            closeConnection();
        }
        return true;
    }

    private LmsContext getLmsContext(Sql sql, ToolProvider toolProvider, LmsUser lmsUser) {
        LmsContext lmsContext = new LmsContext(toolProvider.getResourceLink().getLtiContextId(),
                toolProvider.getResourceLink().getId(),toolProvider.getConsumer().getKey(),
                toolProvider.getConsumer().getConsumerName(),toolProvider.getResourceLink().getTitle(),
                lmsUser) ;
        lmsContextService.findOrCreateContext(sql, lmsContext);
        return lmsContext;
    }

    private LmsUser getLmsUser(Sql sql, ToolProvider toolProvider) {
        LmsUser lmsUser = new LmsUser(toolProvider.getUser().getIdForDefaultScope(),toolProvider.getConsumer().getKey(),
                toolProvider.getUser().getFirstname(), toolProvider.getUser().getLastname(),
                toolProvider.getUser().getEmail(), toolProvider.getUser().isLearner()) ;
        lmsUser =  lmsUserService.findOrCreateUser(sql, lmsUser);
        return lmsUser;
    }

    private String getServerUrl(ToolProvider toolProvider, LmsUser lmsUser, LmsContext lmsContext) {
        String serverUrlFromTP = toolProvider.getRequest().getRequestURL().toString() ;
        String serverUrlRoot = serverUrlFromTP.substring(0, serverUrlFromTP.lastIndexOf("/")) ;
        String result = "&contextName=" + lmsContext.getContextTitle() + "&contextId=" + lmsContext.getContextId() ;
        if (lmsUser.isIsEnabled()) {
            result = serverUrlRoot + "/questions/index?displaysAll=on" + result ;
        } else {
            result = serverUrlRoot + "/lti/terms?username=" + lmsUser.getUsername() + result;
        }
        return result ;
    }

    private void closeConnection() {
        try {
            db.closeConnection();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private Sql getSql() {
        Connection connection = db.getConnection();
        return new Sql(connection);
    }

    private boolean isAnAuthorizedUserRole(ToolProvider toolProvider) {
        return toolProvider.getUser().isLearner() || toolProvider.getUser().isStaff();
    }

    private ToolProvider getToolProvider(HttpServletRequest request, HttpServletResponse response, DataConnector dc) {
        ToolProvider tp = new ToolProvider(request, response, this, dc);
        tp.setParameterConstraint("oauth_consumer_key", true, 50);
        tp.setParameterConstraint("resource_link_id", true, 50);
        tp.setParameterConstraint("user_id", true, 50);
        tp.setParameterConstraint("roles", true, null);
        return tp;
    }

    private DataConnector getDataConnector(HttpServletRequest request) {
        db = Utils.initialise(request.getSession(), false);
        DataConnector dc = null;
        if (db != null) {
            dc = new JDBC(Config.DB_TABLENAME_PREFIX, db.getConnection());
        }
        return dc;
    }

    private void startNewSession(HttpServletRequest request) throws UnsupportedEncodingException {
        request.getSession().invalidate();
        request.getSession(true);
        request.setCharacterEncoding("UTF-8");
    }


    private void initialiseLmsUserService() {
        lmsUserService = new LmsUserService();
        LmsUserHelper lmsUserHelper = new LmsUserHelper();
        UserProvisionAccountService userProvisionAccountService = new UserProvisionAccountService();
        SpringSecurityService springSecurityService = new SpringSecurityService();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        springSecurityService.setPasswordEncoder(bCryptPasswordEncoder);
        lmsUserService.setLmsUserHelper(lmsUserHelper);
        lmsUserService.setSpringSecurityService(springSecurityService);
        lmsUserService.setUserProvisionAccountService(userProvisionAccountService);
        lmsUserService.getUserProvisionAccountService().setLmsUserHelper(lmsUserHelper);
        lmsUserService.getUserProvisionAccountService().setSpringSecurityService(springSecurityService);

    }

    private void initialiseLmsContextService() {
        lmsContextService = new LmsContextService();
        LmsContextHelper lmsContextHelper = new LmsContextHelper();
        lmsContextService.setLmsContextHelper(lmsContextHelper);
        LmsUserHelper lmsUserHelper = new LmsUserHelper();
        lmsContextService.setLmsUserHelper(lmsUserHelper);
    }

    private void initializeUserSession(ToolProvider toolProvider) {
        toolProvider.getRequest().getSession().setAttribute("consumer_key", toolProvider.getConsumer().getKey());
        toolProvider.getRequest().getSession().setAttribute("resource_id", toolProvider.getResourceLink().getId());
        toolProvider.getRequest().getSession().setAttribute("user_consumer_key",
                toolProvider.getUser().getResourceLink().getConsumer().getKey());
        toolProvider.getRequest().getSession().setAttribute("user_id", toolProvider.getUser().getIdForDefaultScope());
        toolProvider.getRequest().getSession().setAttribute("isStudent", toolProvider.getUser().isLearner());
        toolProvider.getRequest().getSession().setAttribute("lti_context_id", toolProvider.getResourceLink().getLtiContextId());
    }


}
