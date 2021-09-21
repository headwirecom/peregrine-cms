import { SUFFIX_PARAM_SEPARATOR } from '../constants';
import { LoggerFactory } from '../logger';
import { parentPath as getParentPath } from '../utils';

let log = LoggerFactory.logger('renameFile').setLevelDebug();

export default function(me, target) {
  const api = me.getApi();
  const { path, to } = target;
  const file = path.split('/').pop();
  const page = path.split('/')[3];
  const dest = `${to}/${file}`;

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
        `/content/admin/pages/${page}.html/path${SUFFIX_PARAM_SEPARATOR}${to}`
      );
    })
    .then(() => ({ destination: dest }));
}
