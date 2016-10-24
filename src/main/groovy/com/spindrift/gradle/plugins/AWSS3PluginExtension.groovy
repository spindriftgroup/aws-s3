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

import org.gradle.util.ConfigureUtil

import com.spindrift.gradle.aws.AWSConfig

/**
 * Provides a named DSL configuration container
 * @author hallatech
 *
 */
class AWSS3PluginExtension {

  /** For internal use only */
  String name
  def name(String name) {
    this.name = name
  }

  /**
   * AWS configuration
   * @param closure
   */
  AWSConfig s3 = new AWSConfig()
  void s3(Closure closure) {
    ConfigureUtil.configure(closure, s3)
  }

}
