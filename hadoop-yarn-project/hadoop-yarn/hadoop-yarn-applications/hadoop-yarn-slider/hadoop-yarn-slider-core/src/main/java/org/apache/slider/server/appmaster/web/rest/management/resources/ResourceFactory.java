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
package org.apache.slider.server.appmaster.web.rest.management.resources;

import org.apache.slider.core.conf.AggregateConf;
import org.apache.slider.core.conf.ConfTree;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.core.UriBuilder;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResourceFactory {

  public static AggregateConfResource createAggregateConfResource(AggregateConf conf,
                                                                  UriBuilder uriBuilder) {
    return new AggregateConfResource(conf, uriBuilder);
  }

  public static ConfTreeResource createConfTreeResource(ConfTree confTree,
                                                        UriBuilder uriBuilder) {
    return new ConfTreeResource(confTree, uriBuilder);
  }

  public static ComponentResource createComponentResource(String name,
                                                          Map<String, String> props,
                                                          UriBuilder uriBuilder,
                                                          Map<String, Object> pathElems) {
    return new ComponentResource(name, props, uriBuilder, pathElems);
  }
}
