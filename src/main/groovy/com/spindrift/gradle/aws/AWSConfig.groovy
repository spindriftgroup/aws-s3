/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.aws

import com.spindrift.gradle.aws.s3.auth.Credentials
import org.gradle.util.ConfigureUtil

/**
 * Contains AWS access configuration
 * @author hallatech
 *
 */
class AWSConfig {

  private static String DEFAULT_REGION='EU_WEST_1'
  private static String DEFAULT_RELEASE_URL='releases.spindriftsoftware.com'
  
  /**
   * The AWS region.
   * Defaults to a specific region
   * @param region
   * @return the region
   */
  String region=DEFAULT_REGION
  String region(String region) {
    this.region = region
  }

  /**
   * The AWS S3 bucket name.
   * This excludes any sub-folders.
   * @param bucket
   * @return the bucket name
   */
  String bucket=DEFAULT_RELEASE_URL
  String bucket(String bucket) {
    this.bucket = bucket
  }
  
  /**
   * Controls the small file save size limit.
   * Large files are saved to disk in byte array chunks to reduce out of memory errors
   * @param smallFileWriteThreshold
   * @return the threshold size for switching from smaller to larger files
   */
  long smallFileWriteThreshold=100000000L
  long smallFileWriteThreshold(long smallFileWriteThreshold) {
    this.smallFileWriteThreshold = smallFileWriteThreshold
  }

  /**
   * Controls the intervals at which a bytes written log is output for large files.
   * For files larger than the @link<smallFileWriteThreshold> a log is displayed
   * when not logging in debug mode. Ignored when logging in debug mode
   * @param bytesWrittenDisplayInterval
   * @return the interval at which to log a progress message
   */
  long bytesWrittenDisplayInterval=50000000L
  long bytesWrittenDisplayInterval(long bytesWrittenDisplayInterval) {
    this.bytesWrittenDisplayInterval = bytesWrittenDisplayInterval
  }
  
  /**
   * AWS credentials for non-public access
   * @param closure
   */
  Credentials credentials = new Credentials()
  void credentials(Closure closure) {
    ConfigureUtil.configure(closure, credentials)
  }
  
  /**
   * @inheritDoc
   */
  public Map getPropertyValues() {
    def props=[:]
    props << ['region':region]
    props << ['bucket':bucket]
    credentials.getPropertyValues().each {k,v->
      props << ["credentials.${k}":v]
    }
    props
  }

}
