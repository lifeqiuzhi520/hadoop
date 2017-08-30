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

package org.apache.slider.common.params;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(commandNames = { SliderActions.ACTION_UPGRADE },
            commandDescription = SliderActions.DESCRIBE_ACTION_UPGRADE)
public class ActionUpgradeArgs extends AbstractClusterBuildingActionArgs
    implements WaitTimeAccessor, LaunchArgsAccessor {

  @Override
  public String getActionName() {
    return SliderActions.ACTION_UPGRADE;
  }

  @ParametersDelegate
  LaunchArgsDelegate launchArgs = new LaunchArgsDelegate();

  @Override
  public File getOutputFile() {
    return launchArgs.getOutputFile();
  }

  @Override
  public String getRmAddress() {
    return launchArgs.getRmAddress();
  }

  @Override
  public int getWaittime() {
    return launchArgs.getWaittime();
  }

  @Override
  public void setWaittime(int waittime) {
    launchArgs.setWaittime(waittime);
  }

  @Parameter(names={ARG_CONTAINERS}, variableArity = true,
             description = "stop specific containers")
  public List<String> containers = new ArrayList<>(0);

  @Parameter(names={ARG_COMPONENTS}, variableArity = true,
      description = "stop all containers of specific components")
  public List<String> components = new ArrayList<>(0);

  @Parameter(names = {ARG_FORCE},
      description = "force spec upgrade operation")
  public boolean force;
}
