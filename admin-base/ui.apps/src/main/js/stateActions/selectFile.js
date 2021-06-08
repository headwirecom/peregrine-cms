import { LoggerFactory } from '../logger';
import { set } from '../utils';

const log = LoggerFactory.logger('selectFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { selected, path } = target;

  set($perAdminApp.getView(), path, selected);
}
