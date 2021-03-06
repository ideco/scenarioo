# Scenarioo Release Procedure

We use git flow (see [branching strategy](Branching-strategy.md)). Therefore the procedure is:

### Set Release Version & Release Branch

In `gradle.build` you have to adjust following version information:

* `scenariooReleaseBranch`: this is the relase branch for this release, that we are going to keep for references into the documentation of eactly this same version. It makes it possible to do versioned links into the documentation. We do not use a tag for this links, since we want to link to the newest documentation files (no need for a release if docu is improved) but of the right major version.
* `scenariooAggregatedDataFormatVersion`: set it to a new version in case the internal aggregated data format somehow changes, which nakes it necessary on an update, that all imported builds need to be reimported. Usually this should be the same as the Scenarioo version that you are going to release. If you are a 100% sure that the internal format did not change since the last release, you can keep the version from the previous release.

### Prepare release notes

* Create the release notes using GitHubs infrastructure: https://github.com/scenarioo/scenarioo/releases. Make sure you safe them as a draft and do not publish them yet.

### Create release branch and make last fixes

* Create a release branch, e.g. execute `git flow release start 1.0.1` while you have checked out the `develop` branch.
* Test it thoroughly.
* Fix the bugs you find and commit them to the release branch.
* Push the release branch and make sure it builds on our [build server](Build-Server).

### Finalize release
* Finalize the release, e.g. using `git flow release finish 1.0.1`. Add the correct tag when git flow asks you for the tag (we only use version numbers for tags, without a leading "v"). Git flow will merge the branch into `master` and `develop`. Therefore you have to push these two branches.
* Push the tag to the master branch using `git push --tags`.
* DO NOT delete the release branch. We will keep the release branch to provide links to the right documentation version and we can even reuse the release branch as a branch to do and test hotfixes on it.

### Build master branch

* Make sure the master branch builds on the build server. As the build uses the git tag as a version number,
 it's important to distribute a build artifact that was build after the tag was pushed!

### Create and push Docker image

* Create a new Docker image for this release according to [this manual](Building-the-Docker-Image)
* Do not forget to update the usage instructions for the Docker Image:     
    [Scenarioo Viewer Doker Image](../setup/Scenarioo-Viewer-Docker-Image.md) 
    (**!! change the version number in the example!**)

### Finish release notes and create links
* Attach the binary of the release (WAR-file) to the release notes (see area "Attach binaries by dropping them here ...").
* Publish the release notes.


### Upload to maven central
1. You will need to specify the following properties in your gradle.properties located in your gradle home directory:

```
signing.keyId=BDCAAE60
signing.password=#private key goes here#
signing.secretKeyRingFile=#secret key file goes here#
ossrhUsername=scenarioo
ossrhPassword=#sonatype password goes here#
```

2. Change the version appropriately in the build.gradle
3. `gradlew clean uploadArchives`
4. Promote build to maven central:
http://central.sonatype.org/pages/releasing-the-deployment.html
