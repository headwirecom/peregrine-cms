import { LoggerFactory } from '../logger';
import { set } from '../utils';

const log = LoggerFactory.logger('selectFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  set($perAdminApp.getView(), `/state/tools/file`, target);
}
