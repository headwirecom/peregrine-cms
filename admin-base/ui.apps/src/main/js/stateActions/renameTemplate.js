import {LoggerFactory} from '../logger'
import {SUFFIX_PARAM_SEPARATOR} from '../constants'

const log = LoggerFactory.logger('renameTemplate').setLevelDebug();

export default function (me, target) {

  log.fine(target);

  const api = me.getApi();

  return api.renamePage(target.path, target.name, target.title).then(() => {
    let path = me.getNodeFromView('/state/tools/templates');
    me.loadContent(`/content/admin/pages/templates.html/path${SUFFIX_PARAM_SEPARATOR + path}`);
  });
};
