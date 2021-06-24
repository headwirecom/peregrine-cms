import { LoggerFactory } from '../logger';
import { set } from '../utils';

let log = LoggerFactory.logger('saveFile').setLevelDebug();

export default function(me, { path, content, extension }) {
  log.fine({ path, content, extension });

  const pathArr = path.split('/');
  const page = pathArr[3];
  const parentPath = pathArr.slice(0, -1).join('/');
  const options = {
    headers: {
      'Content-Type': 'text/plain',
    },
  };

  return axios.put(path, content, options).then((data) => {
    set(me.getView(), '/state/tools/file', path);
    me.loadContent(`/content/admin/pages/${page}.html/path:${parentPath}`);

    return data;
  });
}
