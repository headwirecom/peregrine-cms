import { SUFFIX_PARAM_SEPARATOR } from '../constants';
import { LoggerFactory } from '../logger';
import { set } from '../utils';

let log = LoggerFactory.logger('editFile').setLevelDebug();

export default function(me, { path, resourceType }) {
    log.fine({ path, resourceType });
    debugger

    const view = me.getView();

    set(view, '/state/tools/file', path);
    set(view, '/state/tools/explorerpreview/resourceType', resourceType);

    return new Promise((resolve) => {
        me.loadContent(
            `/content/admin/pages/objects/edit.html/path${SUFFIX_PARAM_SEPARATOR}${path}`
        );
        resolve();
    });
}
