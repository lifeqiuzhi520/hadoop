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
package org.apache.hadoop.yarn.service.client;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.service.conf.ExampleAppJson;
import org.apache.slider.api.resource.Component;
import org.apache.hadoop.yarn.service.client.params.ClientArgs;
import org.apache.slider.common.tools.SliderFileSystem;
import org.apache.hadoop.yarn.service.utils.ServiceApiUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.hadoop.yarn.service.client.params.Arguments.ARG_APPDEF;
import static org.apache.hadoop.yarn.service.conf.SliderXmlConfKeys.KEY_SLIDER_BASE_PATH;

/**
 * Test for building / resolving components of type APPLICATION.
 */
public class TestBuildExternalComponents {

  protected Configuration conf = new YarnConfiguration();
  private File basedir;

  // Check component names match with expected
  private static void checkComponentNames(List<Component> components,
      Set<String> expectedComponents) {
    Assert.assertEquals(expectedComponents.size(), components.size());
    for (Component comp : components) {
      Assert.assertTrue(expectedComponents.contains(comp.getName()));
    }
  }

  // 1. Build the appDef and store on fs
  // 2. check component names
  private void buildAndCheckComponents(String appName, String appDef,
      SliderFileSystem sfs, Set<String> names) throws Throwable {
    String[] args =
        { "build", appName, ARG_APPDEF, ExampleAppJson.resourceName(appDef) };
    ClientArgs clientArgs = new ClientArgs(args);
    clientArgs.parse();
    ServiceCLI cli = new ServiceCLI() {
      @Override protected void createServiceClient() {
        client = new ServiceClient();
        client.init(conf);
        client.start();
      }
    };
    cli.exec(clientArgs);

    // verify generated conf
    List<Component> components =
        ServiceApiUtil.getApplicationComponents(sfs, appName);
    checkComponentNames(components, names);
  }

  @Before
  public void setup() throws IOException {
    basedir = new File("target", "apps");
    if (basedir.exists()) {
      FileUtils.deleteDirectory(basedir);
    } else {
      basedir.mkdirs();
    }
    conf.set(KEY_SLIDER_BASE_PATH, basedir.getAbsolutePath());
  }

  @After
  public void tearDown() throws IOException {
    if (basedir != null) {
      FileUtils.deleteDirectory(basedir);
    }
  }

  // Test applications defining external components(APPLICATION type)
  // can be resolved correctly
  @Test
  public void testExternalComponentBuild() throws Throwable {
    SliderFileSystem sfs = new SliderFileSystem(conf);

    Set<String> nameSet = new HashSet<>();
    nameSet.add("simple");
    nameSet.add("master");
    nameSet.add("worker");

    // app-1 has 3 components: simple, master, worker
    buildAndCheckComponents("app-1", ExampleAppJson.APP_JSON, sfs, nameSet);
    buildAndCheckComponents("external-0", ExampleAppJson.EXTERNAL_JSON_0, sfs,
        nameSet);

    nameSet.add("other");

    // external1 has 3 components: simple(APPLICATION - app1), master and other
    buildAndCheckComponents("external-1", ExampleAppJson.EXTERNAL_JSON_1, sfs,
        nameSet);

    nameSet.add("another");

    // external2 has 2 components: ext(APPLICATION - external1), another
    buildAndCheckComponents("external-2", ExampleAppJson.EXTERNAL_JSON_2, sfs,
        nameSet);
  }
}
