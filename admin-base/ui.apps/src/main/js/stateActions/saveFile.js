import { LoggerFactory } from '../logger';
import { set } from '../utils';

let log = LoggerFactory.logger('saveFile').setLevelDebug();

export default function(me, { path, content, extension }) {
  log.fine({ path, content, extension });

  const api = me.getApi();
  const pathArr = path.split('/');
  const page = pathArr[3];
  const filename = pathArr.pop();
  const parentPath = pathArr.join('/');
  const fileOptions = { type: 'text/plain' };

  if (extension === '.json') {
    fileOptions.type = 'application/json';
  } else if (extension === '.xml') {
    fileOptions.type = 'application/xml';
  }

  const deletePayload = {
    ':operation': 'delete',
  };

  const payload = {
    '*': new File([new Blob([content], fileOptions)], filename),
    '@TypeHint': 'nt:file',
  };

  return api
    ._postFormData(path, deletePayload)
    .then(() => api._postFormData(parentPath, payload))
    .then((data) => {
      set(me.getView(), '/state/tools/file', path);
      me.loadContent(`/content/admin/pages/${page}.html/path:${parentPath}`);

      return data;
    });
}
