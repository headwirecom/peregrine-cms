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
let log = LoggerFactory.logger('savePageProperties').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    const view = me.getView()
    const nodeData = {}

    nodeData.path = '/jcr:content'
    nodeData.component = target.component

    const component = target.component
    const schema = view.admin.componentDefinitions[component].model
    const ogTagSchema = view.admin.componentDefinitions[component].ogTags

    for(let i = 0; i < schema.fields.length; i++) {
        if(!schema.fields[i].readonly) {
            const srcName = schema.fields[i].model
            const dstName = schema.fields[i]['x-model'] ? schema.fields[i]['x-model'] : srcName
            nodeData[dstName] = target[srcName]
        }
    }
    for(let i = 0; i < ogTagSchema.fields.length; i++) {
        if(!ogTagSchema.fields[i].readonly) {
            const srcName = ogTagSchema.fields[i].model
            const dstName = ogTagSchema.fields[i]['x-model'] ? ogTagSchema.fields[i]['x-model'] : srcName
            nodeData[dstName] = target[srcName]
        }
    }

    log.fine(nodeData)
    return new Promise( (resolve, reject) => {
        me.getApi().savePageEdit(target.path, nodeData).then( () => {
            resolve()
        })
    })
}
