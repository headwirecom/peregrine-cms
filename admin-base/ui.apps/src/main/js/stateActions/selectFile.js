import { LoggerFactory } from '../logger';
import { set } from '../utils';

const log = LoggerFactory.logger('selectFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { path, resourceType } = target;
  const view = me.getView();
  const { name: tenantName } = view.state.tenant;

  set(view, `/state/tools/file`, path);
  set(view, `/state/tools/explorerpreview/resourceType`, resourceType);

  if (path.startsWith(`/content/${tenantName}/page`)) {
    set(view, '/state/tools/page', path);
  }
}
