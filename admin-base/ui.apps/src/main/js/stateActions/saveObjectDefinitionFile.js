import { LoggerFactory } from '../logger';

let log = LoggerFactory.logger('saveObjectDefinitionFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { path, content, format } = target;

  const api = me.getApi();
  const pathArr = path.split('/');
  const name = pathArr.pop();
  const parentPath = pathArr.join('/');
  let fileOptions = { type: 'text/plain' };

  if (format === '.json') {
    fileOptions = { type: 'application/json' };
  }

  const deletePayload = {
    ':operation': 'delete',
  };

  const payload = {
    '*': new File([new Blob([content], fileOptions)], name),
    '@TypeHint': 'nt:file',
  };

  /* eslint-disable no-underscore-dangle */
  return api
    ._postFormData(parentPath, deletePayload)
    .then(() => api._postFormData(parentPath, payload))
    .then((data) => {
      me.loadContent(
        `/content/admin/pages/object-definitions.html/path:${parentPath}`
      );

      return data;
    });
}
