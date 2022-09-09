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
import {LoggerFactory} from '../logger'
import {set} from '../utils'

let log = LoggerFactory.logger('showPageInfo').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    const { selected, resourceType } = target;
    let view = me.getView()
    const tenant = view.state.tenant

    console.log(`showPageInfo.js, target: ${JSON.stringify(target)}`)
    set(view, '/state/tools/explorerpreview/resourceType', resourceType);

    return new Promise( (resolve, reject) => {
        return me
          .getApi()
          .populateExplorerDialog(selected)
          .then(() => {
            if (selected.startsWith(`/content/${tenant.name}/pages`)) {
              return me
                .getApi()
                .populateReferencedBy(selected)
                .then(() => {
                  set(view, '/state/tools/page', selected);
                  resolve();
                })
                .catch((error) => reject(error));
            } else if (
              selected.startsWith(`/content/${tenant.name}/templates`)
            ) {
              return me
                .getApi()
                .populateReferencedBy(selected)
                .then(() => {
                  set(view, '/state/tools/template', selected);
                  resolve();
                })
                .catch((error) => reject(error));
            } else if (
              selected.startsWith(`/content/${tenant.name}/objects`)
            ) {
              return me
                .getApi()
                .populateReferencedBy(selected)
                .then(() => {
                  set(view, '/state/tools/objects', selected);
                  resolve();
                })
                .catch((error) => reject(error));
            // } else if (
            //   selected.startsWith(`/content/${tenant.name}/folder`)
            // ) {
            //   return me
            //     .getApi()
            //     .populateReferencedBy(selected)
            //     .then(() => {
            //       set(view, '/state/tools/objects', selected);
            //       resolve();
            //     })
            //     .catch((error) => reject(error));
            }
          })
          .catch((error) => reject(error));
    })
}
