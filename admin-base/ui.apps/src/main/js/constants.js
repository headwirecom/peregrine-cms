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

export const DATA_EXTENSION = '.data.json'

export const COMPONENT_PREFIX = 'cmp'

export const SUFFIX_PARAM_SEPARATOR = ':'

export const EditorTypes = {
  TEMPLATE: 'template-editor',
  PAGE: 'page-editor'
}

export const PathBrowser = {
  Type: {
    PAGE: 'page',
    ASSET: 'asset',
    IMAGE: 'image',
    OBJECT: 'object'
  }
}
export const IgnoreContainers = {
  ENABLED: 'ignore-containers',
  ON_HOLD: 'on-hold',
  DISABLED: ''
}
export const Field = {
  SWITCH: 'materialswitch',
  SELECT: 'material-select',
  MULTI_SELECT: 'material-multiselect'
}
export const Icon = {
  LABEL: 'label',
  SETTINGS: 'settings',
  TEXT_FORMAT: 'text_format',
  COMPARE_ARROWS: 'compare_arrows',
  DELETE: 'delete',
  INFO: 'info',
  EDIT: 'edit',
  LIST: 'list',
  CHECK: 'check',
  CANCEL: 'close',
  MORE_VERT: 'more_vert'
}
export const NodeType = {
  PAGE: 'page',
  TEMPLATE: 'template',
  ASSET: 'asset',
  OBJECT: 'object'
}
export const MimeType = {
  Image: {
    PNG: 'image/png',
    JPG: 'image/jpg',
    JPEG: 'image/jpeg',
    GIF: 'image/gif',
    TIFF: 'image/tiff',
    SVG: 'image/svg+xml'
  }
}

export const Admin = {
  Page: {
    EDIT: '/content/admin/pages/pages/edit.html',
    PAGES: '/content/admin/pages/pages.html'
  }
}

export const NodeTree ={
  SUPPORTED_RESOURCE_TYPES: [
      'per:Page'
  ]
}