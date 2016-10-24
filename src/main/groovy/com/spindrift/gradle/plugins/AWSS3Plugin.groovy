/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Manages ATG modules within the ATG installation
 * @author hallatech
 *
 */
class AWSS3Plugin implements Plugin<Project> {

  static final String PLUGIN_EXTENSION_NAME="aws"
  
  @Override
  public void apply(Project project) {

    project.extensions."${PLUGIN_EXTENSION_NAME}" = new AWSS3PluginExtension()

  }

}


