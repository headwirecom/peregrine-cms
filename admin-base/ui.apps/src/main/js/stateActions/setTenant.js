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

let log = LoggerFactory.logger('setTenant').setLevelDebug()

export default function(me, tenant) {

  log.fine(tenant)

  let view = me.getView()
  let eventBus = me.eventBus
  const list = view.admin.tenants

  return new Promise( (resolve, reject) => {
    if (!list) resolve()

    let next = list.filter((item) => (item.name === tenant.name))
    if (next.length <= 0) {
      throw 'tenant not found'
    }

    // prepopulate tree viewers
    set(me.getView(), '/state/tools', {
      pages: `/content/${tenant.name}/pages`,
      assets: `/content/${tenant.name}/assets`,
      objects: `/content/${tenant.name}/objects`,
      templates: `/content/${tenant.name}/templates`
    })

    next = next[0]
    set(me.getView(), '/state/tenant', next)
    eventBus.$emit('tenants-update', {
      current: next
    })
    resolve()

  }).catch(err => {
    log.error(err)
  })
}
