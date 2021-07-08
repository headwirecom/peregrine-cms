const path = require('path');
const notifier = require('node-notifier');

notifier.notify({
  title: 'peregrine-cms (STARTED)',
  message: 'started building & deploying /ui.apps',
  icon: path.join(
    __dirname,
    '../../../../pagerenderer/server/ui.apps/src/main/content/jcr_root/content/pagerenderserver/assets/peregrine-logo.png'
  ),
  timeout: 2,
});
