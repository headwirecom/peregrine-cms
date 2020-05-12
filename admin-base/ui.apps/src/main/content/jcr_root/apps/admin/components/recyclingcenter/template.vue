<!--
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
  -->
<template>
<div class="recyclingcenter" >
    <div class="">
        <div class="row">
            <div class="col s12">
                <h1><b>{{getTenant().title}}</b>{{$i18n(' Recycle Bin')}}</h1>
                <ul class="collection">


                </ul>
           </div>
        </div>
    </div>
</div>
</template>

<script>
    import {set} from '../../../../../../js/utils';

    export default {
        props: ['model'],
        data: function() {
            return {
                hello: "vvb",
                treeData: { name: "My Tree",
                    children: [ ]
                }
            }
        },
        methods: {
            getTenant() {
              return $perAdminApp.getView().state.tenant || {name: 'example'}
            },
            fetchRecyclables() {
                const recyclePath = "/var/recyclebin/content/" + this.siteName + ".-1.json";
                var self = this;
                return axios.get(recyclePath).then( function(result) {
                    self.treeData = result.data
                    console.log(self.treeData)
                });
            }
        },
        computed: {
            siteName: function(){
                return this.getTenant().name
            },
            showNavigateToParent() {
                return this.path.split('/').length > 4
            },
            path: function() {
                var dataFrom = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                return node
            }
        },
        created: function() {
            this.fetchRecyclables()
        }
    }
</script>

<style>
    h1 {
        font-size: 2.5em;
    }

</style>