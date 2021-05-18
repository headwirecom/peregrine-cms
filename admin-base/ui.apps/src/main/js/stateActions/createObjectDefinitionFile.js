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
import { Toast } from '../constants';
import { LoggerFactory } from '../logger';

let log = LoggerFactory.logger('createObjectDefinitionFile').setLevelDebug();

export default function(me, target) {
  log.fine(target);

  const { parent: path, name, format } = target;

  const api = me.getApi();
  let fileOptions = { type: 'text/plain' };

  if (format === '.json') {
    fileOptions = { type: 'application/json' };
  }

  const payload = {
    '*': new File([new Blob(['{}'], fileOptions)], name),
    '@TypeHint': 'nt:file',
  };

  return api
    ._postFormData(path, payload)
    .then((data) => {
      me.loadContent(
        `/content/admin/pages/object-definitions.html/path:${path}`
      );
      return data;
    })
    .then((data) => {
      console.log(me.toast);
      me.toast(`File "${name}" created.`, Toast.Level.SUCCESS);
      return data;
    });
}