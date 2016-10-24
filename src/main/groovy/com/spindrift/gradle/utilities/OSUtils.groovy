/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.utilities

/**
 * Simple supporting OS utilities
 * @author hallatech
 */
class OSUtils {

/**
 * Gets the Operating System type
 * @return
 */
  static String getOSType() {
    def osType=''
    def os = System.getProperty("os.name").toLowerCase()
    if (os.contains("windows")) { osType="windows" }
    else if (os.contains("mac os")) { osType="mac" }
    else { osType="linux" } // assume Linux
    return osType
  }

/**
 * Confirms if running on a Windows based O/S
 * @return
 */
  static boolean isWindows() {
    return (getOSType().equals("windows")) ? true : false
  }
}
