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

package org.apache.hadoop.yarn.services.utils;

public interface RestApiErrorMessages {
  String ERROR_APPLICATION_NAME_INVALID =
      "Application name is either empty or not provided";
  String ERROR_APPLICATION_NAME_INVALID_FORMAT =
      "Application name is not valid - only lower case letters, digits,"
          + " underscore and hyphen are allowed";

  String ERROR_APPLICATION_NOT_RUNNING = "Application not running";
  String ERROR_APPLICATION_DOES_NOT_EXIST = "Application not found";

  String ERROR_SUFFIX_FOR_COMPONENT =
      " for component %s (nor at the global level)";
  String ERROR_ARTIFACT_INVALID = "Artifact is not provided";
  String ERROR_ARTIFACT_FOR_COMP_INVALID =
      ERROR_ARTIFACT_INVALID + ERROR_SUFFIX_FOR_COMPONENT;
  String ERROR_ARTIFACT_ID_INVALID =
      "Artifact id (like docker image name) is either empty or not provided";
  String ERROR_ARTIFACT_ID_FOR_COMP_INVALID =
      ERROR_ARTIFACT_ID_INVALID + ERROR_SUFFIX_FOR_COMPONENT;

  String ERROR_RESOURCE_INVALID = "Resource is not provided";
  String ERROR_RESOURCE_FOR_COMP_INVALID =
      ERROR_RESOURCE_INVALID + ERROR_SUFFIX_FOR_COMPONENT;
  String ERROR_RESOURCE_MEMORY_INVALID =
      "Application resource or memory not provided";
  String ERROR_RESOURCE_CPUS_INVALID =
      "Application resource or cpus not provided";
  String ERROR_RESOURCE_CPUS_INVALID_RANGE =
      "Unacceptable no of cpus specified, either zero or negative";
  String ERROR_RESOURCE_MEMORY_FOR_COMP_INVALID =
      ERROR_RESOURCE_MEMORY_INVALID + ERROR_SUFFIX_FOR_COMPONENT;
  String ERROR_RESOURCE_CPUS_FOR_COMP_INVALID =
      ERROR_RESOURCE_CPUS_INVALID + ERROR_SUFFIX_FOR_COMPONENT;
  String ERROR_RESOURCE_CPUS_FOR_COMP_INVALID_RANGE =
      ERROR_RESOURCE_CPUS_INVALID_RANGE
          + " for component %s (or at the global level)";
  String ERROR_CONTAINERS_COUNT_INVALID =
      "Required no of containers not specified";
  String ERROR_CONTAINERS_COUNT_FOR_COMP_INVALID =
      ERROR_CONTAINERS_COUNT_INVALID + ERROR_SUFFIX_FOR_COMPONENT;

  String ERROR_RESOURCE_PROFILE_MULTIPLE_VALUES_NOT_SUPPORTED =
      "Cannot specify" + " cpus/memory along with profile";
  String ERROR_RESOURCE_PROFILE_MULTIPLE_VALUES_FOR_COMP_NOT_SUPPORTED =
      ERROR_RESOURCE_PROFILE_MULTIPLE_VALUES_NOT_SUPPORTED
          + " for component %s";
  String ERROR_RESOURCE_PROFILE_NOT_SUPPORTED_YET =
      "Resource profile is not " + "supported yet. Please specify cpus/memory.";

  String ERROR_APPLICATION_IN_USE = "Application name is already in use";
  String ERROR_NULL_ARTIFACT_ID =
      "Artifact Id can not be null if artifact type is none";
  String ERROR_ABSENT_NUM_OF_INSTANCE =
      "Num of instances should appear either globally or per component";
  String ERROR_ABSENT_LAUNCH_COMMAND =
      "launch command should appear if type is slider-zip or none";

  String ERROR_QUICKLINKS_FOR_COMP_INVALID =
      "Quicklinks specified at component level, needs corresponding values set at application level";
}
