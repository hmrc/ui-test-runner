# What has been worked on?

## accessibility-assessment-extension

The build job for the accessibility-assessment-extension has been updated 
to tag a release version whenever a PR is merged. Additionally, 
the accessibility-assessment-extension utilises npm for versioning, 
ensuring that the package.json and manifest.json versions are updated 
and synchronised when changes are made.

There was an issue encountered when updating the build job. 
Specifically, we were unable to automatically upload a zip file of 
the extension's distributable files when the build job is executed. 
It appears there is a permission issue with the GITHUB_TOKEN used in 
the build jobs, which does not permit uploads to a release tag.
This can be discussed with [build and deploy](https://hmrcdigital.slack.com/archives/C518C88QN/p1721987781265859?thread_ts=1721986805.663359&cid=C518C88QN) if we require a token with the correct permissions.

Here is the upload zip code that was written and used in the build job as reference

```groovy
static def addZipToReleasedVersion(String repo) {
    return '''#!/usr/bin/env bash
        |set -o errexit #abort if any command fails
        |
        |GITHUB_OWNER="hmrc"
        |GITHUB_REPO="'''.stripMargin() + repo + '''"
        |RELEASE_VERSION="$(cat package.json | sed -n 's/.*"version": "\\([0-9.]*\\)".*/\\1/p\')"
        |echo "RELEASE_VERSION IS '$RELEASE_VERSION'"
        |cd dist
        |zip -r ../$ARTIFACT_NAME-$RELEASE_VERSION.zip .
        |cd ..
        |
        |# Set the file you want to upload
        |FILE_PATH="$ARTIFACT_NAME-$RELEASE_VERSION.zip"
        |FILE_NAME=$(basename "$FILE_PATH")
        |echo "FILE_PATH IS '$FILE_PATH'"
        |echo "FILE_NAME IS '$FILE_NAME'"
        |
        |RELEASE_ID=$(curl -s -H "Authorization: Bearer $GITHUB_TOKEN" \\
        |               "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/tags/v$RELEASE_VERSION" | \\
        |               jq -r '.id')
        |echo "RELEASE_ID IS '$RELEASE_ID'"
        |
        |if [ "$RELEASE_ID" = "" ]; then
        |  echo "Error: Unable to find release with tag v$RELEASE_VERSION"
        |  exit 1
        |fi
        |
        |# Get the upload URL for the release
        |UPLOAD_URL="https://uploads.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/$RELEASE_ID/assets?name=$FILE_NAME"
        |echo "UPLOAD_URL IS '$UPLOAD_URL'"
        | 
        |# Upload the file
        |curl -H "Authorization: Bearer $GITHUB_TOKEN" -H "Content-Type: application/octet-stream" --data-binary @"$FILE_NAME" "$UPLOAD_URL"
        |
        |echo "Binary uploaded successfully to the release with tag v$RELEASE_VERSION"
        |
    '''.stripMargin()
}
```

## ui-test-runner

There is an open [PR](https://github.com/hmrc/ui-test-runner/pull/49/files) 
for the ui-test-runner that includes several updates. Firstly, 
a new version field named `accessibilityAssessmentExtensionVersion` 
has been added to the Dependencies.scala file. This version can be 
updated when a new version of the accessibility-assessment-extension 
is released. Additionally, an extraction mechanism has been introduced 
for when the ui-test-runner is used as a dependency in frontend 
repositories. This mechanism enables the extension to be unpacked when 
accessibility tests are executed within those frontend services.