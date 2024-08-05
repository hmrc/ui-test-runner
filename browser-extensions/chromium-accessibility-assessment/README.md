# accessibility-assessment-extension

A web extension for automatic accessibility checks.

## Usage

### Run

Run as follows:

```bash
npm start
```

### Build

Build as follows:

```bash
npm run build
```

### Test

Run tests with
```bash
npm test
```

## Versioning

> [!WARNING]
> Please do not update the package.json version manually, 
> always use the correct npm version command as this will ensure that the manifest.json version is kept in sync.


When updating the accessibility-assessment-extension, you should also update the version number in package.json. Use one of the following commands based on the changes made to the code:

Patch Release: (e.g., 1.0.0 to 1.0.1)
```bash
npm version patch
```

Minor Release: (e.g., 1.0.0 to 1.1.0)
```bash
npm version minor
```

Major Release: (e.g., 1.0.0 to 2.0.0)
```bash
npm version major
```

### GitHub Releases 

The extension requires that the zip file of the extension be provided with the GitHub release. Currently, this cannot be done via CI/CD, so for now, after a new release version is built, we should manually upload the zip file to the GitHub release version.

To do this, visit the latest release version [here](https://github.com/hmrc/accessibility-assessment-extension/releases), 
edit the latest release, and then attach the zip file. The latest zip file version can be found at https://artefacts.tax.service.gov.uk/ui/native/hmrc-releases-local/uk/gov/hmrc/accessibility-assessment-extension/[VERSION]/accessibility-assessment-extension-[VERSION].zip. 
Replace [VERSION] with the latest version in GitHub releases.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
