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

package org.apache.slider.providers.agent;

import org.apache.slider.common.tools.SliderUtils;
import org.apache.slider.core.conf.ConfTreeOperations;
import org.apache.slider.providers.agent.application.metadata.CommandOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.apache.slider.api.RoleKeys.ROLE_PREFIX;

/**
 * Stores the command dependency order for all components in a service. <commandOrder>
 * <command>SUPERVISOR-START</command> <requires>NIMBUS-STARTED</requires> </commandOrder> Means, SUPERVISOR START
 * requires NIMBUS to be STARTED
 */
public class ComponentCommandOrder {
  public static final Logger log =
      LoggerFactory.getLogger(ComponentCommandOrder.class);
  private static char SPLIT_CHAR = '-';
  Map<Command, Map<String, List<ComponentState>>> dependencies =
      new HashMap<Command, Map<String, List<ComponentState>>>();
  Map<String, Collection<String>> prefixRoleMap = new HashMap<>();
  Map<String, String> rolePrefixMap = new HashMap<>();

  public ComponentCommandOrder() {}

  public ComponentCommandOrder(List<CommandOrder> commandOrders,
      ConfTreeOperations resources) {
    mergeCommandOrders(commandOrders, resources);
  }

  void mergeCommandOrders(List<CommandOrder> commandOrders,
      ConfTreeOperations resources) {
    for (String component : resources.getComponentNames()) {
      String prefix = SliderUtils.trimPrefix(
          resources.getComponentOpt(component, ROLE_PREFIX, null));
      if (prefix != null) {
        rolePrefixMap.put(component, prefix);
        if (!prefixRoleMap.containsKey(prefix)) {
          prefixRoleMap.put(prefix, new HashSet<String>());
        }
        prefixRoleMap.get(prefix).add(component);
      }
    }
    if (commandOrders != null && commandOrders.size() > 0) {
      for (CommandOrder commandOrder : commandOrders) {
        ComponentCommand componentCmd = getComponentCommand(
            commandOrder.getCommand(), resources);
        String requires = commandOrder.getRequires();
        List<ComponentState> requiredStates = parseRequiredStates(requires,
            resources);
        if (requiredStates.size() > 0) {
          Map<String, List<ComponentState>> compDep = dependencies.get(componentCmd.command);
          if (compDep == null) {
            compDep = new HashMap<>();
            dependencies.put(componentCmd.command, compDep);
          }

          List<ComponentState> requirements = compDep.get(componentCmd.componentName);
          if (requirements == null) {
            requirements = new ArrayList<>();
            compDep.put(componentCmd.componentName, requirements);
          }

          requirements.addAll(requiredStates);
        }
      }
    }
  }

  private List<ComponentState> parseRequiredStates(String requires,
      ConfTreeOperations resources) {
    if (requires == null || requires.length() < 2) {
      throw new IllegalArgumentException("Input cannot be null and must contain component and state.");
    }

    String[] componentStates = requires.split(",");
    List<ComponentState> retList = new ArrayList<ComponentState>();
    for (String componentStateStr : componentStates) {
      retList.add(getComponentState(componentStateStr, resources));
    }

    return retList;
  }

  private ComponentCommand getComponentCommand(String compCmdStr,
      ConfTreeOperations resources) {
    if (compCmdStr == null || compCmdStr.trim().length() < 2) {
      throw new IllegalArgumentException("Input cannot be null and must contain component and command.");
    }

    compCmdStr = compCmdStr.trim();
    int splitIndex = compCmdStr.lastIndexOf(SPLIT_CHAR);
    if (splitIndex == -1 || splitIndex == 0 || splitIndex == compCmdStr.length() - 1) {
      throw new IllegalArgumentException("Input does not appear to be well-formed.");
    }
    String compStr = compCmdStr.substring(0, splitIndex);
    String cmdStr = compCmdStr.substring(splitIndex + 1);

    if (resources.getComponent(compStr) == null && !prefixRoleMap.containsKey(compStr)) {
      throw new IllegalArgumentException("Component " + compStr + " specified" +
          " in command order does not exist");
    }

    Command cmd = Command.valueOf(cmdStr);

    if (cmd != Command.START) {
      throw new IllegalArgumentException("Dependency order can only be specified for START.");
    }
    return new ComponentCommand(compStr, cmd);
  }

  private ComponentState getComponentState(String compStStr,
      ConfTreeOperations resources) {
    if (compStStr == null || compStStr.trim().length() < 2) {
      throw new IllegalArgumentException("Input cannot be null.");
    }

    compStStr = compStStr.trim();
    int splitIndex = compStStr.lastIndexOf(SPLIT_CHAR);
    if (splitIndex == -1 || splitIndex == 0 || splitIndex == compStStr.length() - 1) {
      throw new IllegalArgumentException("Input does not appear to be well-formed.");
    }
    String compStr = compStStr.substring(0, splitIndex);
    String stateStr = compStStr.substring(splitIndex + 1);

    if (resources.getComponent(compStr) == null && !prefixRoleMap.containsKey(compStr)) {
      throw new IllegalArgumentException("Component " + compStr + " specified" +
          " in command order does not exist");
    }

    State state = State.valueOf(stateStr);
    if (state != State.STARTED && state != State.INSTALLED) {
      throw new IllegalArgumentException("Dependency order can only be specified against STARTED/INSTALLED.");
    }
    return new ComponentState(compStr, state);
  }

  // dependency is still on component level, but not package level
  // so use component name to check dependency, not component-package
  public boolean canExecute(String component, Command command, Collection<ComponentInstanceState> currentStates) {
    if (!dependencies.containsKey(command)) {
      return true;
    }
    List<ComponentState> required = new ArrayList<>();
    if (dependencies.get(command).containsKey(component)) {
      required.addAll(dependencies.get(command).get(component));
    }
    String prefix = rolePrefixMap.get(component);
    if (prefix != null && dependencies.get(command).containsKey(prefix)) {
      required.addAll(dependencies.get(command).get(prefix));
    }

    for (ComponentState stateToMatch : required) {
      for (ComponentInstanceState currState : currentStates) {
        log.debug("Checking schedule {} {} against dependency {} is {}",
            component, command, currState.getComponentName(), currState.getState());
        if (currState.getComponentName().equals(stateToMatch.componentName) ||
            (prefixRoleMap.containsKey(stateToMatch.componentName) &&
                prefixRoleMap.get(stateToMatch.componentName).contains(currState.getComponentName()))) {
          if (currState.getState() != stateToMatch.state) {
            if (stateToMatch.state == State.STARTED) {
              log.info("Cannot schedule {} {} as dependency {} is {}",
                  component, command, currState.getComponentName(), currState.getState());
              return false;
            } else {
              //state is INSTALLED
              if (currState.getState() != State.STARTING && currState.getState() != State.STARTED) {
                log.info("Cannot schedule {} {} as dependency {} is {}",
                    component, command, currState.getComponentName(), currState.getState());
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  static class ComponentState {
    public String componentName;
    public State state;

    public ComponentState(String componentName, State state) {
      this.componentName = componentName;
      this.state = state;
    }
  }

  static class ComponentCommand {
    public String componentName;
    public Command command;

    public ComponentCommand(String componentName, Command command) {
      this.componentName = componentName;
      this.command = command;
    }
  }
}
