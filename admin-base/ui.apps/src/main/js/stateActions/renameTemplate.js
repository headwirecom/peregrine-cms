import {LoggerFactory} from '../logger'
import {SUFFIX_PARAM_SEPARATOR} from '../constants'

const log = LoggerFactory.logger('renameTemplate').setLevelDebug();

export default function (me, target) {

  log.fine(target);

  const api = me.getApi();

  api.renamePage(target.path, target.name).then(() => {
    let path = me.getNodeFromView('/state/tools/templates');
    me.loadContent(
        `/content/admin/pages/templates.html/path${SUFFIX_PARAM_SEPARATOR + path}`);
  });
};
