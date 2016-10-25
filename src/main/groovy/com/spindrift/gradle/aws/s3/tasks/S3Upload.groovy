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
 * Task for uploading to S3
 * @author hallatech
 */

class S3Upload extends DefaultTask {

  static final String TASK_DESCRIPTION = "Uploads a file to AWS S3."
  static final String TASK_GROUP = "AWS"
  static final String DEFAULT_REGION = "EU_WEST_1"

  String bucket
  String key
  String region
  String fileName
  boolean overrideEnvironmentCredentials
  String accessKeyId
  String secretAccessKey

  S3Upload() {
    description = TASK_DESCRIPTION
    group = TASK_GROUP
  }

  @TaskAction
  public void executeUpload() {
    project.logger.lifecycle("Uploading $fileName to $bucket/$key ....")
    if (!region) region = DEFAULT_REGION
    S3SimpleClient.upload(project, overrideEnvironmentCredentials, region, accessKeyId, secretAccessKey, bucket, key, fileName)
  }

  //Utility methods for DSL style configuration
  public bucket(String bucket) {
    this.bucket = bucket
  }

  public key(String key) {
    this.key = key
  }
  public to(String key) {
    this.key = key
  }

  public region(String region) {
    this.region = region
  }

  public fileName(String fileName) {
    this.fileName = fileName
  }
  public from(String fileName) {
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
