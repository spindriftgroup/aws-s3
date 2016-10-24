aws-s3
======
This plugins provides simple S3 upload and download tasks.

Notes and Licensing
===================
This plugin does not create any tasks on installation but provides a simple upload and download task definition.
This plugin is operational for *nix environments, is enabled for Windows but has not been tested on Windows.

Environmental Requirements
==========================
Without AWS secret and access keys it assumes a default AWS profile configuration at ~/.aws/credentials or by setting env vars as documented by AWS.

Usage
=====
To use the plugin, include it in your build script:
```
buildscript {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
    dependencies {
        classpath 'gradle.plugin.com.spindrift.gradle:aws-s3:0.1.0'
    }
}

apply plugin: 'com.spindrift.aws-s3'
```

Custom Task Types
=================
- S3Download - Downloads a file fom AWS S3
- S3Upload - Uploads a file to AWS S3

Extension Properties
====================
For downloads:

| Property | Required | Type   | Default  | Details |
| -------- | -------  | ----   | -------- | ------- |
| bucket   | yes      | String | -- | AWS bucket | 
| region   | yes      | String | EU_WEST_1 | As per com.amazonaws.regions.Regions enum format | 
| key      | optional* | String | -- | As defined by AWS path + filename |
| from     | optional* | String -- | Alternative to key |
| fileName | optional* | String | Path to download location including file name |
| to       | optional* | String | -- | Alternative to fileName |
| overrideEnvironmentCredentials | optional | boolean | false | If true can override default profile with secret and access keys |
| accessKeyId | optional | String | -- | Valid AWS access key |
| secretAccessKey | optional | String | -- | Valid AWS secret access key |


Example Configuration
=====================
See integration-test/build.gradle for more examples

Build Notes
===========

The default task is build plus a local maven install so that the integration test can immediately be executed.
```
./gradlew build - builds the code base
./gradlew install - installs to Maven local
./gradlew  - builds and installs to Maven local
```

The integration tests are executed separately by running the alternative build file in the integration-test directory:
```
./gradlew -b integration-test/build.gradle download
```

To publish to the gradle plugin portal:

Ensure versions are correct
Ensure README notes versions match publishing versions
Ensure integration test versions matches publishing versions
Run `gradle clean build publishPlugins -Prelease=true` to publish to the gradle plugin portal. If you don't add `-Prelease=true` a SNAPSHOT version will be released
