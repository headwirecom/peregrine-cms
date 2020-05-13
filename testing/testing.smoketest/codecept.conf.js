const { setHeadlessWhen } = require('@codeceptjs/configure');

// turn on headless mode when running with HEADLESS=true environment variable
// HEADLESS=true npx codecept run
setHeadlessWhen(process.env.HEADLESS);

exports.config = {
  tests: './tests/*_test.js',
  output: './output',
  helpers: {
    REST: {
      endpoint: 'http://localhost:8080/perapi'
    },
    Puppeteer: {
      basicAuth: {username: 'admin', password: 'admin'},
      url: 'http://localhost:8080',
      show: true,
      waitForNavigation: ["networkidle2", "domcontentloaded"],
      windowSize: '1200x1024',
      "chrome": {
        "defaultViewport": {
          "width": 1200,
          "height": 1024
        },
        args: ['--no-sandbox', '--window-size=1200,1024'],
      }
    }
  },
  include: {
    I: './steps_file.js'
  },
  bootstrap: null,
  mocha: {},
  name: 'peregrine-e2e-test',
  plugins: {
    retryFailedStep: {
      enabled: true
    },
    screenshotOnFail: {
      enabled: true
    }
  }
}