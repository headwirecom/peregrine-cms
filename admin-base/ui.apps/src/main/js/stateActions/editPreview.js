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
 * "License") you may not use this file except in compliance
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
import {get, set} from '../utils'
import {IgnoreContainers as IgContainers} from '../constants.js'

let log = LoggerFactory.logger('editPreview').setLevelDebug()

export default function (me, target) {

  log.fine(target)
  target = target || 'preview'

  const state = {
    preview: '/state/tools/workspace/preview',
    igContainers: '/state/tools/workspace/ignoreContainers',
    pageViewView: '/pageView/view',
    view: '/state/tools/workspace/view'
  }
  let view = me.getView()
  let currIgContainers = get(view, state.igContainers, IgContainers.DISABLED)
  const current = get(view, state.preview, '')

  return new Promise((resolve, reject) => {
    if (target === 'preview') {
      if (current === 'preview') {
        set(view, state.preview, '')
        if (currIgContainers === IgContainers.ON_HOLD) {
          set(view, state.igContainers, IgContainers.ENABLED)
          set(view, state.pageViewView, IgContainers.ENABLED)
        } else {
          set(view, state.pageViewView, view.state.tools.workspace.view)
        }
      } else {
        set(view, state.preview, target)
        set(view, state.pageViewView, target)
        if (currIgContainers === IgContainers.ENABLED) {
          set(view, state.igContainers, IgContainers.ON_HOLD)
        }
      }
    } else if (target === IgContainers.ENABLED) {
      if (current !== 'preview') {
        if (currIgContainers === IgContainers.ENABLED) {
          set(view, state.igContainers, IgContainers.DISABLED)
          set(view, state.pageViewView, view.state.tools.workspace.view)
        } else {
          set(view, state.igContainers, target)
          set(view, state.pageViewView, target)
        }
      }
    } else {
      set(view, state.view, target)
    }
    me.eventBus.$emit('edit-preview', get(view, state.preview, ''))
    resolve()
  })
}
