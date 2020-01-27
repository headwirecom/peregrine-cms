import {LoggerFactory} from '../logger'

const log = LoggerFactory.logger('deleteTemplate').setLevelDebug();

export default function (me, target) {

  log.fine(target);

  const api = me.getApi();

  me.getNodeFromView('/state/tools').template = undefined;

  return api.deleteTemplate(target);
};
