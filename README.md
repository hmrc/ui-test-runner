# ui-test-runner

A helper library for UI testing at HMRC.

## Usage

### Declare dependency

Declare the library as a project dependency as follows:

```scala
"uk.gov.hmrc" %% "ui-test-runner" % "x.x.x" % Test
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/project/Dependencies.scala).

### Configuration

- System property `browser` must be set in order to execute tests. Arguments `chrome`, `edge` and `firefox` are available.
- System property `environment` must be set in order to execute tests. Arguments `local`, `dev`, `qa` and `staging` are typically available, but will depend on your project configuration. See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/resources/application.conf).
- System property `accessibility.assessment` is available to enable or disable the accessibility assessment. Arguments `true` and `false` are available, the default is `true`.
- System property `security.assessment` is available to enable or disable the security assessment. Arguments `true` and `false` are available, the default is `false`.
- System property `browser.option.headless` is available to enable or disable headless browser mode. Arguments `true` and `false` are available, the default is `true`.

Set system properties when executing tests as follows:

```sbt
sbt -Dbrowser="<browser>" -Denvironment="<environment>" -Daccessibility.assessment="<accessibility.asessment>" -Dsecurity.assessment="<security.asessment>" -Dbrowser.option.headless=<browser.option.headless> "testOnly uk.gov.hmrc.ui.specs.*"
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/run-tests.sh).

### Browser

#### Start

Start a new browser session as follows:

```scala
startBrowser()
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

#### Stop

Stop an existing browser session as follows:

```scala
quitBrowser()
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

#### Driver instance

Starting a new browser session returns an instance of RemoteWebDriver as an object. Use as follows:

```scala
Driver.instance.<command>
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/pages/BasePage.scala).

#### Download directory

The default directory for file downloads is `target/browser-downloads`.

### Screenshot on failure

Enable screenshot on failure with `ScreenshotOnFailure` trait as follows:

```scala
with ScreenshotOnFailure
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

### Test environment configuration

Test environment configuration is available. A configuration file is required to use it. See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/resources/application.conf).

Create a url from a configuration file as follows:

```scala
val url: String = TestEnvironment.url("service") + "/path"
```

See an [example](https://github.com/hmrc/platform-test-example-ui-journey-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/pages/VATReturnPeriod.scala).

## Development

### Tests

Run tests as follows:

```bash
sbt clean test
```

### Scalafmt

Check all project files are formatted as expected as follows:

```bash
sbt scalafmtCheckAll scalafmtCheck
```

Format `*.sbt` and `project/*.scala` files as follows:

```bash
sbt scalafmtSbt
```

Format all project files as follows:

```bash
sbt scalafmtAll
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
