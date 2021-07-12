import { LoggerFactory } from '../logger';
import { set, parentPath as getParentPath } from '../utils';
import { SUFFIX_PARAM_SEPARATOR } from '../constants';

let log = LoggerFactory.logger('copyFile').setLevelDebug();

export default function(me, { from, to }) {
  log.fine({ from, to });

  const page = to.split('/')[3];
  const file = from.split('/').pop();
  const fileSplit = file.split('.');
  const extension = fileSplit.pop();
  const filename = `${fileSplit.join('.')}-copy.${extension}`;
  const options = {
    headers: {
      'Content-Type': 'text/plain',
    },
  };

  return axios
    .get(from, options)
    .then(({ data: content }) => {
      return axios.put(`${to}/${filename}`, content, options);
    })
    .then((response) => {
      set(me.getView(), '/state/tools/file', to);

      return response;
    })
    .then(() =>
      $perAdminApp.loadContent(
        `/content/admin/pages/${page}.html/path${SUFFIX_PARAM_SEPARATOR}${to}`
      )
    );
}
