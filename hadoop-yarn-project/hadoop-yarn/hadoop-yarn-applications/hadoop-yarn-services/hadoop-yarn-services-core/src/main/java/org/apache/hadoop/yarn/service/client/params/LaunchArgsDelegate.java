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

import java.io.File;

/**
 * Any launch-time args
 */
public class LaunchArgsDelegate extends WaitArgsDelegate implements
                                                         LaunchArgsAccessor {


  //TODO: do we need this?
  @Parameter(names = ARG_RESOURCE_MANAGER,
             description = "Resource manager hostname:port ",
             required = false)
  private String rmAddress;

  @Override
  public String getRmAddress() {
    return rmAddress;
  }

  @Parameter(names = {ARG_OUTPUT, ARG_OUTPUT_SHORT},
      description = "output file for any service report")
  public File outputFile;

  @Override
  public File getOutputFile() {
    return outputFile;
  }
}
