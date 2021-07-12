import { SUFFIX_PARAM_SEPARATOR } from '../constants';
import { LoggerFactory } from '../logger';
import { parentPath as getParentPath } from '../utils';

let log = LoggerFactory.logger('renameFile').setLevelDebug();

export default function(me, target) {
  const { path, name } = target;
  const extension = path.split('.').pop();
  const { parentPath } = getParentPath(path);
  const api = me.getApi();
  const page = path.split('/')[3];
  const dest = `${parentPath}/${name}.${extension}`;

  const payload = {
    ':operation': 'move',
    ':dest': dest,
  };

  log.fine('target.', target, '\npayload:', payload);

  /* eslint-disable no-underscore-dangle */
  return api
    ._postFormData(path, payload)
    .then(() => {
      return me.loadContent(
        `/content/admin/pages/${page}.html/path${SUFFIX_PARAM_SEPARATOR}${parentPath}`
      );
    })
    .then(() => ({ destination: dest }));
}
