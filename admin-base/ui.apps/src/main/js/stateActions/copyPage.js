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
import { LoggerFactory } from '../logger';

let log = LoggerFactory.logger('copyPage').setLevelDebug();

export default function(me, copy) {
  log.fine(copy);
  const api = me.getApi();
  let fileName = null;
  const { srcPath, targetPath, name, title, resourceType, mimeType } = copy;

  // change name for assets.
  // Not really sure why this is used for assets, but copyFile() behaves wildly different and causes many unwanted sideeffects when copying assets.
  if (resourceType === 'per:Asset') {
    const file = srcPath.split('/').pop();
    fileName = file;
    let extension = "";
    const fileSplit = file.split('.');
    if (fileSplit.length > 1) {
      extension = "." + fileSplit.pop();
    }
    fileName = `${fileSplit.join('.')}-copy${extension}`;

    let existingNode = me.findNodeFromPath(
      me.getView().admin.nodes,
      `${targetPath}/${fileName}`
    );

    if (existingNode) {
      let counter = 2;

      while (existingNode) {
        fileName = `${fileSplit.join('.')}-copy-${counter}${extension}`;
        existingNode = me.findNodeFromPath(
          me.getView().admin.nodes,
          `${targetPath}/${fileName}`
        );
        counter++;
      }
    }
  }

  api.copyPage(srcPath, targetPath, name || fileName || null, {title, resourceType, mimeType}).then(() => {
    log.fine(`copy from ${srcPath} to ${targetPath} complete`);
  });
}
