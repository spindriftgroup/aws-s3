/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.aws.s3.auth

/**
 * Contains AWS credential configuration
 * @author hallatech
 *
 */
class Credentials {

  /**
   * The AWS access key identifier
   * @param accessKeyId
   * @return
   */
  String accessKeyId=''
  String accessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId
  }
  
  /**
   * The AWS secret access key
   * @param secretAccessKey
   * @return
   */
  String secretAccessKey=''
  String secretAccessKey(String secretAccessKey) {
    this.secretAccessKey = secretAccessKey
  }
  
  /**
   * Override the AWS credential file (~/.aws/crendentials) look by using configured values
   * @param overrideEnvCredentials
   * @return
   */
  boolean overrideEnvCredentials=true
  boolean overrideEnvCredentials(boolean overrideEnvCredentials) {
    this.overrideEnvCredentials = overrideEnvCredentials
  }
  
  /**
   * @inheritDoc
   */
  public Map getPropertyValues() {
    def props=[:]
    props << ['accessKeyId':accessKeyId]
    props << ['secretAccessKey':secretAccessKey]
    props << ['overrideEnvCredentials':overrideEnvCredentials]
    props
  }
}
