/*-
 * #%L
 * admin base - UI Apps
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('editPreview').setLevelDebug()

import { set, get } from '../utils'
import { IgnoreContainers } from '../constants.js'

export default function(me, target) {

    log.fine(target);
    if(!target) target = 'preview';

    let view = me.getView();
    let currIgnoreContainers = get(view, '/state/tools/workspace/ignoreContainers', IgnoreContainers.DISABLED);
    const current = get(view, '/state/tools/workspace/preview', '');
    if(target === 'preview') {
        if(current === 'preview') {
            set(view, '/state/tools/workspace/preview', '');
            if (currIgnoreContainers === IgnoreContainers.ON_HOLD) {
                set(view, '/state/tools/workspace/ignoreContainers', IgnoreContainers.ENABLED);
                set(view, '/pageView/view', IgnoreContainers.ENABLED);
            } else {
                set(view, '/pageView/view', view.state.tools.workspace.view);
            }
        } else {
            set(view, '/state/tools/workspace/preview', target);
            set(view, '/pageView/view', target)
            if (currIgnoreContainers === IgnoreContainers.ENABLED) {
                set(view, '/state/tools/workspace/ignoreContainers', IgnoreContainers.ON_HOLD);
            }
        }
    } else if (target === IgnoreContainers.ENABLED){
        if (current !== 'preview') {
            if(currIgnoreContainers === IgnoreContainers.ENABLED) {
                set(view, '/state/tools/workspace/ignoreContainers', IgnoreContainers.DISABLED);
                set(view, '/pageView/view', view.state.tools.workspace.view);
            } else {
                set(view, '/state/tools/workspace/ignoreContainers', target);
                set(view, '/pageView/view', target);
            }
        }
    } else {
        set(view, '/state/tools/workspace/view', target);
    }
}
