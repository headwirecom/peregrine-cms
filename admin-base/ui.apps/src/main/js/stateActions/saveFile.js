import { LoggerFactory } from '../logger';
import { set } from '../utils';

let log = LoggerFactory.logger('saveFile').setLevelDebug();

export default function(me, { path, content, extension }) {
  log.fine({ path, content, extension });

  const options = {
    headers: {
      'Content-Type': 'text/plain',
    },
  };

  return axios.put(path, content, options).then((data) => {
    set(me.getView(), '/state/tools/file', path);
    return data;
  });
}
