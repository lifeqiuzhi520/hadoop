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

package org.apache.slider.providers;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.registry.client.api.RegistryOperations;
import org.apache.slider.common.tools.SliderFileSystem;
import org.apache.slider.common.tools.SliderUtils;
import org.apache.slider.core.exceptions.SliderException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractClientProvider extends Configured {
  private static final Logger log =
    LoggerFactory.getLogger(AbstractClientProvider.class);

  public AbstractClientProvider(Configuration conf) {
    super(conf);
  }

  public abstract String getName();

  public abstract List<ProviderRole> getRoles();

  /**
   * Generates a fixed format of application tags given one or more of
   * application name, version and description. This allows subsequent query for
   * an application with a name only, version only or description only or any
   * combination of those as filters.
   *
   * @param appName name of the application
   * @param appVersion version of the application
   * @param appDescription brief description of the application
   * @return
   */
  public static final Set<String> createApplicationTags(String appName,
      String appVersion, String appDescription) {
    Set<String> tags = new HashSet<>();
    tags.add(SliderUtils.createNameTag(appName));
    if (appVersion != null) {
      tags.add(SliderUtils.createVersionTag(appVersion));
    }
    if (appDescription != null) {
      tags.add(SliderUtils.createDescriptionTag(appDescription));
    }
    return tags;
  }

  /**
   * Process client operations for applications such as install, configure
   * @param fileSystem
   * @param registryOperations
   * @param configuration
   * @param operation
   * @param clientInstallPath
   * @param clientPackage
   * @param clientConfig
   * @param name
   * @throws SliderException
   */
  public void processClientOperation(SliderFileSystem fileSystem,
                                     RegistryOperations registryOperations,
                                     Configuration configuration,
                                     String operation,
                                     File clientInstallPath,
                                     File clientPackage,
                                     JSONObject clientConfig,
                                     String name)
      throws SliderException {
    throw new SliderException("Provider does not support client operations.");
  }

}
