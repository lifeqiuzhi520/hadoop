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

import org.apache.slider.api.resource.Artifact;
import org.apache.slider.api.resource.Artifact.TypeEnum;
import org.apache.slider.providers.docker.DockerClientProvider;
import org.apache.slider.providers.docker.DockerProviderFactory;
import org.apache.slider.providers.docker.DockerProviderService;
import org.apache.slider.providers.tarball.TarballClientProvider;
import org.apache.slider.providers.tarball.TarballProviderFactory;
import org.apache.slider.providers.tarball.TarballProviderService;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test provider factories.
 */
public class TestProviderFactory {
  @Test
  public void testDockerFactory() throws Throwable {
    SliderProviderFactory factory = SliderProviderFactory
        .createSliderProviderFactory(new Artifact().type(TypeEnum.DOCKER));
    assertTrue(factory instanceof DockerProviderFactory);
    assertTrue(factory.createClientProvider() instanceof DockerClientProvider);
    assertTrue(factory.createServerProvider() instanceof DockerProviderService);
    assertTrue(SliderProviderFactory.getProviderService(new Artifact()
        .type(TypeEnum.DOCKER)) instanceof DockerProviderService);
  }

  @Test
  public void testTarballFactory() throws Throwable {
    SliderProviderFactory factory = SliderProviderFactory
        .createSliderProviderFactory(new Artifact().type(TypeEnum.TARBALL));
    assertTrue(factory instanceof TarballProviderFactory);
    assertTrue(factory.createClientProvider() instanceof TarballClientProvider);
    assertTrue(factory.createServerProvider() instanceof
        TarballProviderService);
    assertTrue(SliderProviderFactory.getProviderService(new Artifact()
        .type(TypeEnum.TARBALL)) instanceof TarballProviderService);
  }

  @Test
  public void testDefaultFactory() throws Throwable {
    SliderProviderFactory factory = SliderProviderFactory
        .createSliderProviderFactory(null);
    assertTrue(factory instanceof DefaultProviderFactory);
    assertTrue(factory.createClientProvider() instanceof DefaultClientProvider);
    assertTrue(factory.createServerProvider() instanceof
        DefaultProviderService);
    assertTrue(SliderProviderFactory.getProviderService(null) instanceof
        DefaultProviderService);
  }

}
