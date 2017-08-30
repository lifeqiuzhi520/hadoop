/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.service.provider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.service.conf.YarnServiceConf;
import org.apache.slider.api.resource.Application;
import org.apache.slider.api.resource.Component;
import org.apache.hadoop.yarn.service.conf.SliderKeys;
import org.apache.slider.common.tools.SliderFileSystem;
import org.apache.slider.common.tools.SliderUtils;
import org.apache.slider.core.exceptions.SliderException;
import org.apache.slider.core.launch.AbstractLauncher;
import org.apache.slider.core.launch.CommandLineBuilder;
import org.apache.hadoop.yarn.service.compinstance.ComponentInstance;
import org.apache.hadoop.yarn.service.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.hadoop.yarn.service.conf.YarnServiceConf.CONTAINER_RETRY_INTERVAL;
import static org.apache.hadoop.yarn.service.conf.YarnServiceConf.CONTAINER_RETRY_MAX;
import static org.apache.hadoop.yarn.service.utils.ServiceApiUtil.$;

public abstract class AbstractProviderService implements ProviderService,
  SliderKeys {

  protected static final Logger log =
      LoggerFactory.getLogger(AbstractProviderService.class);

  public abstract void processArtifact(AbstractLauncher launcher,
      ComponentInstance compInstance, SliderFileSystem fileSystem,
      Application application)
      throws IOException;

  public void buildContainerLaunchContext(AbstractLauncher launcher,
      Application application, ComponentInstance instance,
      SliderFileSystem fileSystem, Configuration yarnConf)
      throws IOException, SliderException {
    Component component = instance.getComponent().getComponentSpec();;
    processArtifact(launcher, instance, fileSystem, application);

    ServiceContext context =
        instance.getComponent().getScheduler().getContext();
    // Generate tokens (key-value pair) for config substitution.
    // Get pre-defined tokens
    Map<String, String> globalTokens =
        instance.getComponent().getScheduler().globalTokens;
    Map<String, String> tokensForSubstitution = ProviderUtils
        .initCompTokensForSubstitute(instance);
    tokensForSubstitution.putAll(globalTokens);
    // Set the environment variables in launcher
    launcher.putEnv(SliderUtils
        .buildEnvMap(component.getConfiguration(), tokensForSubstitution));
    launcher.setEnv("WORK_DIR", ApplicationConstants.Environment.PWD.$());
    launcher.setEnv("LOG_DIR", ApplicationConstants.LOG_DIR_EXPANSION_VAR);
    if (System.getenv(HADOOP_USER_NAME) != null) {
      launcher.setEnv(HADOOP_USER_NAME, System.getenv(HADOOP_USER_NAME));
    }
    launcher.setEnv("LANG", "en_US.UTF-8");
    launcher.setEnv("LC_ALL", "en_US.UTF-8");
    launcher.setEnv("LANGUAGE", "en_US.UTF-8");

    for (Entry<String, String> entry : launcher.getEnv().entrySet()) {
      tokensForSubstitution.put($(entry.getKey()), entry.getValue());
    }
    //TODO add component host tokens?
//    ProviderUtils.addComponentHostTokens(tokensForSubstitution, amState);

    // create config file on hdfs and add local resource
    ProviderUtils.createConfigFileAndAddLocalResource(launcher, fileSystem,
        component, tokensForSubstitution, instance, context);

    // substitute launch command
    String launchCommand = ProviderUtils
        .substituteStrWithTokens(component.getLaunchCommand(),
            tokensForSubstitution);
    CommandLineBuilder operation = new CommandLineBuilder();
    operation.add(launchCommand);
    operation.addOutAndErrFiles(OUT_FILE, ERR_FILE);
    launcher.addCommand(operation.build());

    // By default retry forever every 30 seconds
    launcher.setRetryContext(YarnServiceConf
        .getInt(CONTAINER_RETRY_MAX, -1, application.getConfiguration(),
            yarnConf), YarnServiceConf
        .getInt(CONTAINER_RETRY_INTERVAL, 30000, application.getConfiguration(),
            yarnConf));
  }
}
