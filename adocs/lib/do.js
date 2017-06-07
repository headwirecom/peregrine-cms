const CDP = require('chrome-remote-interface');
const file = require('fs');

module.exports = function(url, fileName, width, height) {

    // CLI Args
    const format = 'png';
    const viewportWidth = width;
    const viewportHeight = height;
    const userAgent = null;
    const fullPage = false

    console.log('capturing',url)

    // Start the Chrome Debugging Protocol
    CDP(async function(client) {
        // Extract used DevTools domains.
        const {DOM, Emulation, Network, Page, Runtime, Input} = client;

        // Enable events on domains we are interested in.
        await Page.enable();
        await DOM.enable();
        await Network.enable();
  //      await Input.enable();

        // If user agent override was specified, pass to Network domain
        if (userAgent) {
            await Network.setUserAgentOverride({userAgent});
        }

        // Set up viewport resolution, etc.
        const deviceMetrics = {
            width: viewportWidth,
            height: viewportHeight,
            deviceScaleFactor: 0,
            mobile: false,
            fitWindow: true,
        };
        await Emulation.setDeviceMetricsOverride(deviceMetrics);
        await Emulation.setVisibleSize({width: viewportWidth, height: viewportHeight});

        // Navigate to target page
        await Page.navigate({url});
        // Wait for page load event to take screenshot
        Page.loadEventFired(async () => {
            // If the `full` CLI option was passed, we need to measure the height of
            // the rendered page and use Emulation.setVisibleSize
            if (fullPage) {
                const {root: {nodeId: documentNodeId}} = await DOM.getDocument();
                const {nodeId: bodyNodeId} = await DOM.querySelector({
                    selector: 'body',
                    nodeId: documentNodeId,
                });
                const {model: {height}} = await DOM.getBoxModel({nodeId: bodyNodeId});

                await Emulation.setVisibleSize({width: viewportWidth, height: height});
                // This forceViewport call ensures that content outside the viewport is
                // rendered, otherwise it shows up as grey. Possibly a bug?
                await Emulation.forceViewport({x: 0, y: 0, scale: 1});
            }
            setTimeout( async function() {
                await Input.dispatchMouseEvent('mousePressed', 830, 470);
                const screenshot = await Page.captureScreenshot({format});
                const buffer = new Buffer(screenshot.data, 'base64');
                file.writeFile(fileName, buffer, 'base64', function(err) {
                    if (err) {
                        console.error(err);
                    } else {
                        console.log('Screenshot saved');
                    }
                    client.close();
                });
            },1000)
        });
    }).on('error', err => {
        console.error('Cannot connect to browser:', err);
    });

}
