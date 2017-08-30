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

package org.apache.slider.api;

/**
 * Enumeration of state values.
 */
public class StateValues {

  private StateValues() {}

  /**
   * Specification is incomplete & cannot
   * be used: {@value}.
   */
  public static final int STATE_INCOMPLETE = 0;

  /**
   * Spec has been submitted: {@value}
   */
  public static final int STATE_SUBMITTED = 1;
  /**
   * Cluster created: {@value}
   */
  public static final int STATE_CREATED = 2;
  /**
   * Live: {@value}
   */
  public static final int STATE_LIVE = 3;
  /**
   * Not ready.
   */
  public static final int STATE_NOT_READY = 4;
  /**
   * Ready.
   */
  public static final int STATE_READY = 5;
  /**
   * Stopped.
   */
  public static final int STATE_STOPPED = 99;
  /**
   * Destroyed.
   */
  public static final int STATE_DESTROYED = 100;

}
