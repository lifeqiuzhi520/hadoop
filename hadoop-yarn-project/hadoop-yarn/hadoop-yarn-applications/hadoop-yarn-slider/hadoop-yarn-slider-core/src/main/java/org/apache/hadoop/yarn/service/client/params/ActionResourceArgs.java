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
package org.apache.hadoop.yarn.service.client.params;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.hadoop.yarn.service.client.params.AbstractActionArgs;
import org.apache.hadoop.yarn.service.client.params.SliderActions;

@Parameters(commandNames = { SliderActions.ACTION_RESOURCE},
    commandDescription = SliderActions.DESCRIBE_ACTION_RESOURCE)

public class ActionResourceArgs  extends AbstractActionArgs {

  @Override
  public String getActionName() {
    return SliderActions.ACTION_RESOURCE;
  }

  @Parameter(names = {ARG_INSTALL},
      description = "Install the resource(s)")
  public boolean install;

  @Parameter(names = {ARG_DELETE},
      description = "Delete the file")
  public boolean delete;

  @Parameter(names = {ARG_LIST},
      description = "List of installed files")
  public boolean list;

  @Parameter(names = {ARG_RESOURCE},
      description = "Name of the file or directory")
  public String resource;

  @Parameter(names = {ARG_DESTDIR},
      description = "The name of the folder in which to store the resources")
  public String folder;

  @Parameter(names = {ARG_OVERWRITE}, description = "Overwrite existing resource(s)")
  public boolean overwrite = false;

  /**
   * Get the min #of params expected
   * @return the min number of params in the {@link #parameters} field
   */
  public int getMinParams() {
    return 0;
  }

  @Override
  public int getMaxParams() {
    return 3;
  }
}
