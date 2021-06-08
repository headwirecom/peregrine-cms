import { LoggerFactory } from '../logger';
import { set } from '../utils';

const log = LoggerFactory.logger('unselectFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { path } = target;

  set($perAdminApp.getView(), path, null);
}
