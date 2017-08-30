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

package org.apache.slider.core.persist;

import org.apache.slider.core.exceptions.SliderException;

import java.io.IOException;

/**
 * Optional action to add while the lock is held; this is needed to execute
 * some other persistent operations within the scope at the same lock
 * without inserting too much code into the persister
 */
public interface LockHeldAction {

  /**
   * Execute the action
   * @throws IOException on any failure
   */
  public void execute() throws IOException, SliderException;
  
}
