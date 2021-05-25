import { LoggerFactory } from '../logger';

let log = LoggerFactory.logger('saveObjectDefinitionFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { path, content, format } = target;

  const api = me.getApi();
  let fileOptions = { type: 'text/plain' };

  if (format === '.json') {
    fileOptions = { type: 'application/json' };
  }

  const payload = {
    '*': new File([new Blob([content], fileOptions)], name),
    '@TypeHint': 'nt:file',
  };

  /* eslint-disable no-underscore-dangle */
  return api._postFormData(path, payload).then((data) => {
    me.loadContent(`/content/admin/pages/object-definitions.html/path:${path}`);
    return data;
  });
}
