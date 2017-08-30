/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.service.webapp;

import com.google.inject.Singleton;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.VersionInfo;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.service.api.records.Service;
import org.apache.hadoop.yarn.service.api.records.ServiceState;
import org.apache.hadoop.yarn.service.api.records.ServiceStatus;
import org.apache.hadoop.yarn.service.client.ServiceClient;
import org.apache.hadoop.yarn.service.api.records.Component;
import org.apache.hadoop.yarn.service.utils.SliderUtils;
import org.apache.hadoop.yarn.service.utils.ServiceApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.apache.hadoop.yarn.service.conf.RestApiConstants.*;

/**
 * The rest API endpoints for users to manage services on YARN.
 */
@Singleton
@Path(CONTEXT_ROOT)
public class ApiServer {
  private static final Logger LOG =
      LoggerFactory.getLogger(ApiServer.class);
  private static Configuration YARN_CONFIG = new YarnConfiguration();
  private static ServiceClient SERVICE_CLIENT;

  static {
    init();
  }

  // initialize all the common resources - order is important
  private static void init() {
    SERVICE_CLIENT = new ServiceClient();
    SERVICE_CLIENT.init(YARN_CONFIG);
    SERVICE_CLIENT.start();
  }

  @GET
  @Path(VERSION)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
  public Response getVersion() {
    String version = VersionInfo.getBuildVersion();
    LOG.info(version);
    return Response.ok(version).build();
  }

  @POST
  @Path(SERVICE_ROOT_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createService(Service service) {
    LOG.info("POST: createService = {}", service);
    ServiceStatus serviceStatus = new ServiceStatus();
    try {
      ApplicationId applicationId = SERVICE_CLIENT.actionCreate(service);
      LOG.info("Successfully created service " + service.getName()
          + " applicationId = " + applicationId);
      serviceStatus.setState(ServiceState.ACCEPTED);
      serviceStatus.setUri(
          CONTEXT_ROOT + SERVICE_ROOT_PATH + "/" + service
              .getName());
      return Response.status(Status.CREATED).entity(serviceStatus).build();
    } catch (IllegalArgumentException e) {
      serviceStatus.setDiagnostics(e.getMessage());
      return Response.status(Status.BAD_REQUEST).entity(serviceStatus)
          .build();
    } catch (Exception e) {
      String message = "Failed to create service " + service.getName();
      LOG.error(message, e);
      serviceStatus.setDiagnostics(message + ": " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(serviceStatus).build();
    }
  }

  @GET
  @Path(SERVICE_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getService(@PathParam(SERVICE_NAME) String appName) {
    LOG.info("GET: getService for appName = {}", appName);
    ServiceStatus serviceStatus = new ServiceStatus();
    try {
      Service app = SERVICE_CLIENT.getStatus(appName);
      return Response.ok(app).build();
    } catch (IllegalArgumentException e) {
      serviceStatus.setDiagnostics(e.getMessage());
      serviceStatus.setCode(ERROR_CODE_APP_NAME_INVALID);
      return Response.status(Status.NOT_FOUND).entity(serviceStatus)
          .build();
    } catch (Exception e) {
      LOG.error("Get service failed", e);
      serviceStatus
          .setDiagnostics("Failed to retrieve service: " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(serviceStatus).build();
    }
  }

  @DELETE
  @Path(SERVICE_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response deleteService(@PathParam(SERVICE_NAME) String appName) {
    LOG.info("DELETE: deleteService for appName = {}", appName);
    return stopService(appName, true);
  }

  private Response stopService(String appName, boolean destroy) {
    try {
      SERVICE_CLIENT.actionStop(appName, destroy);
      if (destroy) {
        SERVICE_CLIENT.actionDestroy(appName);
        LOG.info("Successfully deleted service {}", appName);
      } else {
        LOG.info("Successfully stopped service {}", appName);
      }
      return Response.status(Status.NO_CONTENT).build();
    } catch (ApplicationNotFoundException e) {
      ServiceStatus serviceStatus = new ServiceStatus();
      serviceStatus.setDiagnostics(
          "Service " + appName + " not found " + e.getMessage());
      return Response.status(Status.NOT_FOUND).entity(serviceStatus)
          .build();
    } catch (Exception e) {
      ServiceStatus serviceStatus = new ServiceStatus();
      serviceStatus.setDiagnostics(e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(serviceStatus).build();
    }
  }

  @PUT
  @Path(COMPONENT_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN  })
  public Response updateComponent(@PathParam(SERVICE_NAME) String appName,
      @PathParam(COMPONENT_NAME) String componentName, Component component) {

    if (component.getNumberOfContainers() < 0) {
      return Response.status(Status.BAD_REQUEST).entity(
          "Service = " + appName + ", Component = " + component.getName()
              + ": Invalid number of containers specified " + component
              .getNumberOfContainers()).build();
    }
    try {
      Map<String, Long> original = SERVICE_CLIENT.flexByRestService(appName,
          Collections.singletonMap(component.getName(),
              component.getNumberOfContainers()));
      return Response.ok().entity(
          "Updating component " + componentName + " size from " + original
              .get(componentName) + " to " + component.getNumberOfContainers())
          .build();
    } catch (YarnException | IOException e) {
      ServiceStatus status = new ServiceStatus();
      status.setDiagnostics(e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status)
          .build();
    }
  }

  @PUT
  @Path(SERVICE_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response updateService(@PathParam(SERVICE_NAME) String appName,
      Service updateServiceData) {
    LOG.info("PUT: updateService for app = {} with data = {}", appName,
        updateServiceData);

    // Ignore the app name provided in updateServiceData and always use appName
    // path param
    updateServiceData.setName(appName);

    // For STOP the app should be running. If already stopped then this
    // operation will be a no-op. For START it should be in stopped state.
    // If already running then this operation will be a no-op.
    if (updateServiceData.getState() != null
        && updateServiceData.getState() == ServiceState.STOPPED) {
      return stopService(appName, false);
    }

    // If a START is requested
    if (updateServiceData.getState() != null
        && updateServiceData.getState() == ServiceState.STARTED) {
      return startService(appName);
    }

    // If new lifetime value specified then update it
    if (updateServiceData.getLifetime() != null
        && updateServiceData.getLifetime() > 0) {
      return updateLifetime(appName, updateServiceData);
    }

    // flex a single component app
    if (updateServiceData.getNumberOfContainers() != null && !ServiceApiUtil
        .hasComponent(updateServiceData)) {
      Component defaultComp = ServiceApiUtil.createDefaultComponent(updateServiceData);
      return updateComponent(updateServiceData.getName(), defaultComp.getName(),
          defaultComp);
    }

    // If nothing happens consider it a no-op
    return Response.status(Status.NO_CONTENT).build();
  }

  private Response updateLifetime(String appName, Service updateAppData) {
    try {
      String newLifeTime =
          SERVICE_CLIENT.updateLifetime(appName, updateAppData.getLifetime());
      return Response.ok("Service " + appName + " lifeTime is successfully updated to "
          + updateAppData.getLifetime() + " seconds from now: " + newLifeTime).build();
    } catch (Exception e) {
      String message =
          "Failed to update service (" + appName + ") lifetime ("
              + updateAppData.getLifetime() + ")";
      LOG.error(message, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(message + " : " + e.getMessage()).build();
    }
  }

  private Response startService(String appName) {
    try {
      SERVICE_CLIENT.actionStart(appName);
      LOG.info("Successfully started service " + appName);
      return Response.ok("Service " + appName + " is successfully started").build();
    } catch (Exception e) {
      String message = "Failed to start service " + appName;
      LOG.info(message, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(message + ": " + e.getMessage()).build();
    }
  }
}
