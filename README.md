# ui-test-runner

A helper library for UI testing at HMRC.

## Usage

### Declare dependency

Declare the library as a project dependency as follows:

```scala
"uk.gov.hmrc" %% "ui-test-runner" % "x.x.x" % Test
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/project/Dependencies.scala).

### Configuration

- System property `browser` must be set to execute tests. Arguments `chrome`, `edge` and `firefox` are available.
- System property `environment` must be set to execute tests. Arguments `local`, `dev`, `qa` and `staging` are typically available, but will depend on your project configuration. See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/resources/application.conf).
- System property `accessibility.assessment` is available to enable or disable the accessibility assessment. Arguments `true` and `false` are available, the default is `true`.
- System property `security.assessment` is available to enable or disable the security assessment. Arguments `true` and `false` are available, the default is `false`.
- System property `browser.option.headless` is available to enable or disable headless browser mode. Arguments `true` and `false` are available, the default is `true`.
- System property `browser.logging` is available to enable browser console logs when using Chrome. Arguments `true` and `false` are available, the default is `false`.
- System property `browser.bidi` is available to enable the WebSocket connection for bidirectional communication with browser. Arguments `true` and `false` are available, the default is `false`.
- System property `browser.usePreviousVersion` is available to enable the use of previous version of Chrome (v128). Arguments `true` and `false` are available, the default is `false`.

#### Browser Logging

Browser logging is **disabled by default** for all log types. You can selectively enable or configure specific log types using system properties.

**Enable/Disable Properties:**
- `browser.logging` - Browser console logs (default: `false`)
- `driver.logging` - WebDriver command logs (default: `false`)
- `performance.logging` - Network/performance logs (default: `false`)

**Log Level Properties:**
- `browser.logging.level` - Browser log level (default: `ALL`)
- `driver.logging.level` - Driver log level (default: `ALL`)
- `performance.logging.level` - Performance log level (default: `ALL`)

Valid log levels: `ALL`, `INFO`, `WARNING`, `SEVERE`

**Browser Support:**

`Chrome`, `Microsft Edge` fully supported `Browser`, `Driver` and `Performance` logs

`Firefox` partially supported `Browser` and `Driver` logs while `Performance` logs are not supported


**Log Output:**

Logs are saved to files in `target/test-reports/browser-logs/` with minimal console output showing only the file path.

Set system properties when executing tests as follows:

**Default (all logging enabled):**

```sbt
sbt -Dbrowser="<browser>" -Denvironment="<environment>" -Daccessibility.assessment="<accessibility.asessment>" -Dsecurity.assessment="<security.asessment>" -Dbrowser.option.headless=<browser.option.headless> "testOnly uk.gov.hmrc.ui.specs.*"
```
See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/run-tests.sh).

**With selective logging (only browser console):**

```sbt
sbt -Dbrowser="<browser>" -Denvironment="<environment>" -Ddriver.logging=false -Dperformance.logging=false "testOnly uk.gov.hmrc.ui.specs.*"
```

**With custom log levels:**

```sbt
sbt -Dbrowser="<browser>" -Denvironment="<environment>" -Dbrowser.logging.level=INFO -Ddriver.logging.level=WARNING "testOnly uk.gov.hmrc.ui.specs.*"
```

**Disable all logging:**

```sbt
sbt -Dbrowser="<browser>" -Denvironment="<environment>" -Dbrowser.logging=false -Ddriver.logging=false -Dperformance.logging=false "testOnly uk.gov.hmrc.ui.specs.*"
```

**Common Use Cases:**

1. **Default behavior** (all logging enabled at ALL level):
   ```bash
   sbt -Dbrowser=chrome -Denvironment=local test
   ```

2. **Only browser console logs** (for JavaScript debugging):
   ```bash
   sbt -Dbrowser=chrome -Denvironment=local -Ddriver.logging=false -Dperformance.logging=false test
   ```

3. **Only WebDriver commands** (for element location debugging):
   ```bash
   sbt -Dbrowser=chrome -Denvironment=local -Dbrowser.logging=false -Dperformance.logging=false test
   ```

4. **Custom log levels** (reduce verbosity):
   ```bash
   sbt -Dbrowser=chrome -Denvironment=local -Dbrowser.logging.level=WARNING test
   ```


### Browser

#### Start

Start a new browser session as follows:

```scala
startBrowser()
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

#### Stop

Stop an existing browser session as follows:

```scala
quitBrowser()
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

#### Driver instance

Starting a new browser session returns an instance of RemoteWebDriver as an object. Use as follows:

```scala
Driver.instance.<command>
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/pages/BasePage.scala).

#### Download directory

The default directory for file downloads is `target/browser-downloads`.

### Screenshot on failure

Enable screenshot on failure with `ScreenshotOnFailure` trait as follows:

```scala
with ScreenshotOnFailure
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/specs/BaseSpec.scala).

### Test environment configuration

Test environment configuration is available. A configuration file is required to use it. See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/resources/application.conf).

Create a url from a configuration file as follows:

```scala
val url: String = TestEnvironment.url("service") + "/path"
```

See an [example](https://github.com/hmrc/platform-test-example-ui-tests/blob/main/src/test/scala/uk/gov/hmrc/ui/pages/VATReturnPeriod.scala).

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

