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
    <component :is="tag" v-if="visible">
        <a 
            v-if                    = "!model.type"
            v-bind:href             = "targetHtml"
            v-bind:title            = "title"
            v-on:click.stop.prevent = "onClick"
            v-bind:class            = "model.classes">
            {{model.title}}
            <slot></slot>
        </a>
        <a 
            v-if                    = "model.type === 'icon'" 
            v-bind:title            = "title"
            v-bind:href             = "target" 
            v-on:click.stop.prevent = "action" 
            class                   = "btn-floating waves-effect waves-light" 
            v-bind:class            = "model.classes">
            <i class="material-icons" v-bind:class="isSelected ? 'actionSelected' : ''">
                {{model.icon ? model.icon : model.title}}
                <slot></slot>
            </i>
        </a>
    </component>
</template>

<script>
    /**
     * admin-components-action can be used to trigger a UI action in the app. This component
     * renders either a basic link or an icon. The component supports rendering of a default slot
     *
     * on click of this component triggers a $perAdminApp.action() and tries to find a vue component
     * in the parents that defines a method with the name of the command. If not found in the parent
     * hierarchy then the complete vue tree will be searched for the action
     *
     * @example <caption>simple use case</caption>
     * <admin-components-action :model="{ action: 'save', target: item, title: 'save' }"></admin-components-action>
     *
     * @example <caption>icon use case</caption>
     * <admin-components-action :model="{ action: 'save', target: item, type: 'icon', title: 'open' }"></admin-components-action>
     *
     * @example <caption>slot</caption>
     * <admin-components-action :model="{ action: 'save', target: item }">
     *     <div>hello world</div>
     * </admin-components-action>
     *
     * @module admin/components/action
     * @param {Object} model - the model for this component
     * @param {string} model.action - the name of the action
     * @param {Object} model.target - an object containing all the information for this action
     * @param {string} model.tooltipTitle - used for tooltip/hover
     * @param {string} model.title - the title to be displayed
     * @param {string} model.type - if type === icon the action will be rendered as an icon
     * @param {string} model.classes - additional classes to be added to the action
     *
     */
    export default {
    props: {
        model: Object,
        tag: {
            type: String,
            default: 'span'
        }
    },
    data: function() {
        return {
            clickCount: 0,
            clickTimer: null,
            dblClickDelay: 200
        };
    },
    computed: {

        /**
         *
         * checks if this action is currently selected using the model.stateFrom and model.stateFromDefault
         * properties of the action
         *
         * @return {boolean}
         *
         */
        isSelected() {
            if(!this.model.stateFrom) return false
            let currentState = $perAdminApp.getNodeFromViewOrNull(this.model.stateFrom)
            if(!currentState) {
                if(this.model.stateFromDefault) {
                    return true
                }
            } else if(currentState === this.model.target) {
                return true
            }
            return false
        },

        /**
         *
         * returns the display title for this action. Logs an error if no title and no tooltipTitle
         * is defined in the model
         *
         * @method computed:title
         * @return {string} - the display title
         *
         */
        title() {
            if(this.model.tooltipTitle) {
                if (this.model.experiences) {
                    return this.$exp(this.model, 'tooltipTitle', this.model.tooltipTitle);
                }
                return this.model.tooltipTitle;
            }
            if(this.model.title) {
                if (this.model.experiences) {
                    return this.$exp(this.model, 'title', this.model.title);
                }
                return this.model.title
            }
            /* eslint-disable no-console */
            console.error('missing alt', this.model.command, this.model.path)
            /* eslint-enable no-console */
            return this.model.command
        },
        target() {
            if(this.model.target && typeof this.model.target === 'string') {
                return this.model.target
            }
            return '#'
        },
        targetHtml() {
            return this.target !== '#' ? this.target + '.html' : '#'
        },
        visible() {
            if(this.model.visibility) {
                return exprEval.Parser.evaluate( this.model.visibility, $perAdminApp.getView() );
            } else {
                return true;
            }
        }
    },
    methods: {

        /**
         *  triggers the action specified by the model
         *
         *  @method methods:action
         * @param {event} e - event
         */
        action: function(e) {
            $perAdminApp.action(this, this.model.command, this.model.target)
        },
        dblClickAction: function(e) {
            $perAdminApp.action(this, this.model.dblClickCommand, this.model.dblClickTarget)
        },
        onClick: function(e) {
            if(!this.model.dblClickCommand) {
                this.action(e);
            } else {
                this.clickCount++;
                if(this.clickCount === 1) {
                    this.timer = setTimeout(() => {
                        this.clickCount = 0;
                        this.action(e);
                    }, this.dblClickDelay);
                } else {
                    clearTimeout(this.timer);
                    this.dblClickAction(e);
                    this.clickCount = 0;
                }
            }
        }
    }
}
</script>

<style>
    .actionSelected {
        background-color: white !important;
        color: #37474f !important;
    }
</style>
