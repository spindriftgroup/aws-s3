/**
 * Copyright (C) 2012-2016 Spindrift B.V. All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spindrift.gradle.aws.s3.client

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.spindrift.gradle.utilities.OSUtils

import java.text.SimpleDateFormat
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Provides simple AWS S3 upload and download facilities
 * @author hallatech
 *
 */
class S3SimpleClient {
  
  public static final MAX_BYTE_ARRAY_SIZE=8192
  public static final SMALL_SIZE_THRESHOLD_DEFAULT = 100000000L
  public static final LOG_INTERVAL_DEFAULT = 500000000L

  /**
   * Provides a map based parameter driven facility to get a file as there many string parameters.
   * This allows Groovy convention for specifying the map key by the caller.
   * @param properties
   * @return
   */
  public static download(Map properties) {
    download(properties.project,
      properties.overrideEnvironment,
      properties.region,
      properties.accessKeyId,
      properties.secretAccessKey,
      properties.bucket,
      properties.key,
      properties.destination)
  }

  public static upload(Map properties) {
    upload(properties.project,
      properties.overrideEnvironment,
      properties.region,
      properties.accessKeyId,
      properties.secretAccessKey,
      properties.bucket,
      properties.key,
      properties.fileName)
  }

  public static downloadModifiedVersion(Map properties) {
    downloadModifiedVersion(properties.project,
      properties.overrideEnvironment,
      properties.region,
      properties.accessKeyId,
      properties.secretAccessKey,
      properties.bucket,
      properties.key,
      properties.destination,
      properties.originalDate)
  }
  
  /**
   * Gets a file from a S3 bucket
   * @param project
   * @param bucketRegion
   * @param accessKeyId
   * @param secretAccessKey
   * @param bucket
   * @param key the file
   * @param destination the destination to save the retrieved file to
   * @return
   * @throws IOException
   */
  public static download(Project project,
                         boolean overrideEnvironment,
                         String bucketRegion,
                         String accessKeyId,
                         String secretAccessKey,
                         String bucket,
                         String key,
                         String destination) throws IOException {

    project.logger.debug "download($project, $overrideEnvironment, $bucketRegion, $accessKeyId, $secretAccessKey, $bucket, $key, $destination)"
    AmazonS3 s3Client = createClient(project, overrideEnvironment, bucketRegion, accessKeyId, secretAccessKey)
    s3Download(project, s3Client, bucket, key, destination)
  }

  /**
   * Uploads a file to an S3 bucket
   * @param project
   * @param overrideEnvironment
   * @param bucketRegion
   * @param accessKeyId
   * @param secretAccessKey
   * @param bucket
   * @param key
   * @param fileSource
   * @return
   * @throws IOException
   */
  public static upload(Project project,
                       boolean overrideEnvironment,
                       String bucketRegion,
                       String accessKeyId,
                       String secretAccessKey,
                       String bucket,
                       String key,
                       String fileSource) throws IOException {
    
    project.logger.debug "upload($project, $overrideEnvironment, $bucketRegion, $accessKeyId, $secretAccessKey, $bucket, $key, $fileSource)"
    AmazonS3 s3Client = createClient(project, overrideEnvironment, bucketRegion, accessKeyId, secretAccessKey)
    File fileToUpload = new File(fileSource)
    assert fileToUpload.exists(),"File $fileSource does not exist."
    s3Upload(project, s3Client, bucket, key, fileToUpload)
  }
    
  /**
   * Downloads a file if the timestamp is later
   * @param project
   * @param overrideEnvironment
   * @param bucketRegion
   * @param accessKeyId
   * @param secretAccessKey
   * @param bucket
   * @param key
   * @param destination
   * @return
   * @throws IOException
   */
  public static boolean downloadModifiedVersion(Project project,
                                                boolean overrideEnvironment,
                                                String bucketRegion,
                                                String accessKeyId,
                                                String secretAccessKey,
                                                String bucket,
                                                String key,
                                                String destination,
                                                Date originalDate) throws IOException {
    
      project.logger.debug "downloadModifiedVersion($project, $overrideEnvironment, $bucketRegion, $accessKeyId, $secretAccessKey, $bucket, $key, $destination)"
      AmazonS3 s3Client = createClient(project, overrideEnvironment, bucketRegion, accessKeyId, secretAccessKey)
      S3Object s3Object = getFileObject(project, s3Client, bucket, key)
      Date lastModifiedDate = s3Object.getObjectMetadata().getLastModified()
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
      if (lastModifiedDate.after(originalDate)) {
        project.logger.debug("File for $bucket, $key has been modified original date=[${sdf.format(originalDate)}], "
          + "modified date=[${sdf.format(lastModifiedDate)}], a new version will be downloaded.")
        s3Download(project, s3Client, bucket, key, destination)
        return true
      }
      else {
        project.logger.debug "File for $bucket, $key has not been modified, no download will be initiated."
        return false
      }
  }
    
  /**
   * Creates a S3 client for a given region
   * @param bucketRegion the region of the S3 bucket
   * @param accessKeyId s3 credential access key
   * @param secretAccessKey s3 credential secret key
   * @return the s3 client
   */
  private static AmazonS3 createClient(Project project,
                                       boolean overrideEnvironment,
                                       String bucketRegion,
                                       String accessKeyId,
                                       String secretAccessKey) {

    project.logger.debug "createClient($project, $overrideEnvironment, $bucketRegion, $accessKeyId, $secretAccessKey)"
    
    //First attempt to get the credentials from the environment
    AWSCredentials credentials
    AmazonS3 s3Client
    if (!overrideEnvironment) {
      try {
        credentials = new ProfileCredentialsProvider().getCredentials();
      } catch (Exception e) {
        throw new GradleException(
          "Cannot load the credentials from the credential profiles file. " +
          "Please make sure that your credentials file is at the correct " +
          "location (~/.aws/credentials), and is in valid format.",
          e);
      }
      s3Client = new AmazonS3Client(credentials);
    }
    else {
      validateCredentials(bucketRegion, accessKeyId, secretAccessKey)
      //Uses basic credentials via gradle configuration
      BasicAWSCredentials basicAWSCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey)
      s3Client = new AmazonS3Client(basicAWSCreds)
    }

    Region region = Region.getRegion(Regions."${bucketRegion}")
    s3Client.setRegion(region)
    return s3Client
  }
  
  /**
   * Validates that credential information is configured
   * @param bucketRegion
   * @param accessKeyId
   * @param secretAccessKey
   * @return
   */
  private static validateCredentials(String bucketRegion, String accessKeyId, String secretAccessKey) {
    if (!bucketRegion) {
      throw new GradleException("AWS S3 region [${bucketRegion}] for bucket has not been configured. Configure property aws.region with a valid region format, e.g. EU_WEST_1")
    }
    if (!accessKeyId) {
      throw new GradleException("AWS access key has not been configured. Configure property aws.accessKeyId with a valid access key identifier.")
    }
    if (!secretAccessKey) {
      throw new GradleException("AWS secret access key has not been configured. Configure property aws.secretAccessKey with a valid secret access key.")
    }
  }
    
  /**
   * Gets the file object from S3.
   * Used for metadata processing
   * @param project
   * @param client
   * @param bucket
   * @param key
   * @return
   */
  private static getFileObject(Project project, AmazonS3 client, String bucket, String key) {
    project.logger.debug "getFileMetaData($project, $client, $bucket, $key)"
    S3Object object = null
    try {
      project.logger.debug("Retrieving $bucket/$key ...")
      object = client.getObject(new GetObjectRequest(bucket, key))
      project.logger.debug("Content-Type: "  + object.getObjectMetadata().getContentType())
    }
    catch (AmazonServiceException ase) {
      StringBuilder errorMessage = new StringBuilder("\nCaught an AmazonServiceException, which means your request made it "
        + "to Amazon S3, but was rejected with an error response for some reason.")
      errorMessage.append("\nError Message:    " + ase.getMessage())
      errorMessage.append("\nHTTP Status Code: " + ase.getStatusCode())
      errorMessage.append("\nAWS Error Code:   " + ase.getErrorCode())
      errorMessage.append("\nError Type:       " + ase.getErrorType())
      errorMessage.append("\nRequest ID:       " + ase.getRequestId())
      errorMessage.append("\nDownload failed. See the Amazon exception details above.")
      throw new GradleException(errorMessage.toString())
    }
    catch (AmazonClientException ace) {
      StringBuilder errorMessage = new StringBuilder("\nCaught an AmazonClientException, which means the client encountered "
        + "a serious internal problem while trying to communicate with S3, "
        + "such as not being able to access the network.")
      errorMessage.append("Error Message: " + ace.getMessage())
      errorMessage.append("Download failed. See the Amazon exception details above.")
      throw new GradleException(errorMessage.toString())
    }
    object
  }

  /**
   * Downloads a file from S3
   * @param project
   * @param client the client pre-initialised with credentials
   * @param bucket the S3 source bucket
   * @param filePath the S3 full path to file including folders from root of bucket
   * @param destination the file location to save the file to
   * @return
   */
  private static s3Download(Project project, AmazonS3 client, String bucket, String key, String destination) {
    project.logger.debug "downloadFile($project, $client, $bucket, $key, $destination)"
    S3Object s3Object = getFileObject(project, client, bucket, key)
    saveFile(project, s3Object, destination)
  }
  
  /**
   * Saves the downloaded file to a given location
   * @param project
   * @param file
   * @param destination the location to save the file to
   */
  private static saveFile(Project project, S3Object file, String destination) {
    project.logger.debug "saveFile($project, $file, $destination)"
    def tokenizer = (OSUtils.windows) ? "\\" : "/"
    project.logger.debug "tokenizer=$tokenizer"
    def tokens=destination.tokenize(tokenizer)
    project.logger.debug "tokens=${tokens}"
    project.logger.debug "tokens=${tokens[0..-2]}"
    if (tokens.size() > 1) {
      File dirPath=new File("${(destination.startsWith(tokenizer)) ? tokenizer : ''}" +tokens[0..-2].join(tokenizer).toString())
      project.logger.debug "dirPath=${dirPath}"
      dirPath.mkdirs()
      assert dirPath.exists(),"Could not create ${dirPath} directory to download file to."
    }
    
    long fileLength = file.getObjectMetadata().contentLength
    project.logger.debug "File length for ${file}=${fileLength}"
    long smallSizeThreshold = (project.getAt('aws').s3.smallFileWriteThreshold) ?: SMALL_SIZE_THRESHOLD_DEFAULT
    long logInterval = (project.getAt('aws').s3.bytesWrittenDisplayInterval) ?: LOG_INTERVAL_DEFAULT
    if (fileLength < smallSizeThreshold) {
      //Write smaller files under 100MB
      new File(destination).withOutputStream{ os->
        os << (file.getObjectContent().getBytes() as byte[])
      }
    }
    else {
      //Handle large files
      project.logger.debug "Writing big file size=${fileLength} ..."
      InputStream inputStream = file.getObjectContent()
      int read = 0;
      long bytesWritten=0L;
      try {
        project.logger.quiet "This is a large file (${fileLength} bytes), please be patient ..."
        int stagedBytesWritten=0
        new File(destination).withOutputStream { os->
          byte[] bytes = new byte[MAX_BYTE_ARRAY_SIZE]
          while ((read = inputStream.read(bytes)) != -1) {
            os.write(bytes, 0, read)
            //Calculate and display progress
            bytesWritten = ((fileLength - bytesWritten) > MAX_BYTE_ARRAY_SIZE) ? bytesWritten + MAX_BYTE_ARRAY_SIZE : bytesWritten + (fileLength - bytesWritten)
            if (project.logger.isDebugEnabled()) {
              project.logger.debug "Bytes written: ${bytesWritten}"
            }
            else {
              if ((bytesWritten / logInterval).intValue() > stagedBytesWritten) {
                stagedBytesWritten+=1
                project.logger.quiet "Bytes written: ${bytesWritten}"
              }
            }
          }
        }
      }
      catch(IOException ioe) {
        throw new GradleException("Encountered IOException writing large file ${file}, with size=${fileLength}, after writing ${bytesWritten} bytes.\n${ioe}")
      }
      finally {
        inputStream.close()
      }
      
    }
    project.logger.info "File written to $destination"
  }
  
  /**
   * Uploads a file to an AWS S3 bucket
   * @param project
   * @param client the client credentials
   * @param bucket the bucket to load
   * @param key the path to the file
   * @param file the file to upload
   */
  private static s3Upload(Project project, AmazonS3 client, String bucket, String key, File file) {
    project.logger.debug "uploadFile($project, $client, $bucket, $key, $file)"
    try {
      client.putObject(new PutObjectRequest(bucket, key, file))
    }
    catch (AmazonServiceException ase) {
      StringBuilder errorMessage = new StringBuilder("\nCaught an AmazonServiceException, which means your request made it "
        + "to Amazon S3, but was rejected with an error response for some reason.")
      errorMessage.append("\nError Message:    " + ase.getMessage())
      errorMessage.append("\nHTTP Status Code: " + ase.getStatusCode())
      errorMessage.append("\nAWS Error Code:   " + ase.getErrorCode())
      errorMessage.append("\nError Type:       " + ase.getErrorType())
      errorMessage.append("\nRequest ID:       " + ase.getRequestId())
      errorMessage.append("\nDownload failed. See the Amazon exception details above.")
      throw new GradleException(errorMessage.toString())
    }
    catch (AmazonClientException ace) {
      StringBuilder errorMessage = new StringBuilder("\nCaught an AmazonClientException, which means the client encountered "
        + "a serious internal problem while trying to communicate with S3, "
        + "such as not being able to access the network.")
      errorMessage.append("Error Message: " + ace.getMessage())
      errorMessage.append("Download failed. See the Amazon exception details above.")
      throw new GradleException(errorMessage.toString())
    }
  }
    
  /**
   * Method sourced from Amazon SDK /samples/AmazonS3/S3Sample.java
   * Displays the contents of the specified input stream as text.
   * @param input The input stream to display as text.
   * @throws IOException
   */
  private static void displayTextInputStream(Project project, InputStream input) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    while (true) {
      String line = reader.readLine();
      if (line == null) break;
      project.logger.lifecycle("    " + line);
    }
    project.logger.lifecycle("");
  }
}
