/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.aws.s3.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.spindrift.gradle.aws.s3.client.S3SimpleClient

/**
 * Task for downloading from S3
 * @author hallatech
 */

class S3Download extends DefaultTask {

  static final String TASK_DESCRIPTION = "Downloads a file from AWS S3."
  static final String TASK_GROUP = "AWS"
  static final String DEFAULT_REGION = "EU_WEST_1"

  String bucket
  String key
  String region
  String fileName
  boolean overrideEnvironmentCredentials = false
  String accessKeyId
  String secretAccessKey

  S3Download() {
    description = TASK_DESCRIPTION
    group = TASK_GROUP
  }

  @TaskAction
  public void executedownload() {
    project.logger.lifecycle("Downloading $fileName from $bucket/$key ....")
    if (!region) region = DEFAULT_REGION
    S3SimpleClient.download(project, overrideEnvironmentCredentials, region, accessKeyId, secretAccessKey, bucket, key, fileName)
  }

  //Utility methods for DSL style configuration
  public bucket(String bucket) {
    this.bucket = bucket
  }

  public key(String key) {
    this.key = key
  }
  public from(String key) {
    this.key = key
  }

  public region(String region) {
    this.region = region
  }

  public fileName(String fileName) {
    this.fileName = fileName
  }
  public to(String fileName) {
    this.fileName = fileName
  }

  public overrideEnvironmentCredentials(boolean overrideEnvironmentCredentials) {
    this.overrideEnvironmentCredentials = overrideEnvironmentCredentials
  }

  public accessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId
  }

  public secretAccessKey(String secretAccessKey) {
    this.secretAccessKey = secretAccessKey
  }

}
