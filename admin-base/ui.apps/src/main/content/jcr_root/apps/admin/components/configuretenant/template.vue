<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
<div>
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'editRootTemplate',
                title: 'configure website root template',
                target: '/content/admin/pages/templates/edit',
                classes: 'btn'
            }">
        </admin-components-action>
        <p>
            The root template references all your domain names this website
            is exposed at as well as yout js/css includes, prefetch domains and
            your brand slug 
        </p>
    </div>
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'tenantSetupReplication',
                title: 'site setup replication',
                target: '/content/admin/pages/templates/edit',
                classes: 'btn'
            }">
        </admin-components-action>
        <p>
            Use the 'site setup replication' button to go live with your whole
            site and push all the necessary information out to your web server.
            Once you're live you can easily push any new pages or page changes
            out in the admin console.   
        </p>
    </div>
    <h2>backup and restore</h2>
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'backupTenant',
                title: 'backup site',
                target: '/content/admin/pages/templates/edit',
                tooltipTitle: 'Create Backup of this Site',
                classes: 'btn'
            }">
        </admin-components-action>
    </div>
    <div>
        <a v-bind:href="downloadUrl" title="Download the latest Backup" target="_blank" class='btn'>download site backup</a>
    </div>
    <div>
        <label class='btn' title="Upload a Site Backup to this Server">
            Upload Site Backup File
            <input type="file" ref="file_upload" style="display:none" v-on:change="addFiles">
            <i class="material-icons">file_upload</i>
        </label>
    </div>
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'restoreTenant',
                title: 'restore site',
                target: '/content/admin/pages/templates/edit',
                tooltipTitle: 'Restore the latest Site Backup',
                classes: 'btn'
            }">
        </admin-components-action>
    </div>
    <div>
        <p>Latest Site Backup</p>
        <p><b>Date:</b>&nbsp;{{backupDate}}</p>
        <p><b>Result:</b>&nbsp;{{backupState}}</p>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
            editRootTemplate(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('editTemplate', `/content/${tenant}/templates` );
            },
            tenantSetupReplication(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('tenantSetupReplication', `/content/${tenant}`, 'true');
            },
            backupTenant(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('backupTenant', `/content/${tenant}`);
            },
            uploadBackupTenant(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('uploadBackupTenant', `/content/${tenant}`, 'test-file.zip');
            },
            restoreTenant(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('restoreTenant', `/content/${tenant}`);
            },
            addFiles (ev) {
                this.uploadFile(ev.target.files)
            },
            uploadFile(files) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction(
                    'uploadBackupTenant',
                    {
                        path: `/content/${tenant}`,
                        files: files,
                        cb: this.fileUploadComplete
                    }
                );
            },
            fileUploadComplete(percentCompleted) {
                return
            }
        },
        computed: {
            // Build up the Download URL with the tenant name
            downloadUrl() {
                const tenant = $perAdminApp.getView().state.tenant;
                return tenant ? '/perapi/admin/downloadBackupTenant.zip/content/' + tenant.name : '';
            },
            backupInfo() {
                return $perAdminApp.getView().state.tools.backup
            },
            backupDate() {
                const backup = $perAdminApp.getView().state.tools.backup;
                return backup ? backup.last : "No Backup Info for Date";
            },
            backupState() {
                const backup = $perAdminApp.getView().state.tools.backup;
                let state = backup ? backup.state : '';
                if(state === '') {
                   state = 'No Backup Info for State';
                } else if(state === 'SUCCEEDED') {
                    state = 'Successfully Built';
                } else {
                    state = 'Failed Build';
                }
                return state;
            }
        }
    }
</script>
