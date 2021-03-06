/*
 *    Copyright 2019-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package apollo.spi;


import apollo.Config;
import apollo.ConfigFile;
import framework.apollo.core.enums.ConfigFileFormat;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactory {
  /**
   * Create the config instance for the appNamespace.
   *
   * @param namespace the appNamespace
   * @return the newly created config instance
   */
  public Config create(String namespace);

  /**
   * Create the config file instance for the appNamespace
   * @param namespace the appNamespace
   * @return the newly created config file instance
   */
  public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat);
}
