import { SUFFIX_PARAM_SEPARATOR } from '../constants';
import { LoggerFactory } from '../logger';
import { set } from '../utils';

let log = LoggerFactory.logger('editObjectDefinitionFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const view = me.getView();

  set(view, '/state/tools/objectdefinitioneditor', target);
  set(view, '/state/tools/file', target);

  return new Promise((resolve) => {
    me.loadContent(
      `/content/admin/pages/object-definitions/edit.html/path${SUFFIX_PARAM_SEPARATOR}${target}`
    );
    resolve();
  });
}
