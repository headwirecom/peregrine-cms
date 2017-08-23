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
    <div v-bind:data-per-path="model.path">
        <div v-if="edit">edit tour</div>
        <div v-if="enabled" v-bind:class="tourClass" v-bind:data-per-path="model.path">
            <div class="__pcms_tour_overlay tour-left" ref="left" v-bind:style="leftStyle"></div>
            <div class="__pcms_tour_overlay tour-right" ref="right" v-bind:style="rightStyle"></div>
            <div class="__pcms_tour_overlay tour-top" ref="top" v-bind:style="topStyle"></div>
            <div class="__pcms_tour_overlay tour-bot" ref="bottom" v-bind:style="bottomStyle"></div>
            <div class="__pcms_tour_highlite" ref="highlite" v-bind:style="highliteStyle"></div>
            <div class="__pcms_tour_info card" ref="info" v-bind:style="infoStyle">
                <button v-on:click="enabled = false" class="btn-flat btn-close"><i class="material-icons">close</i></button>
                <div ref="tourText" v-html="text" class="card-content">
                </div>
                <div class="card-action">
                    <button v-on:click="onPrevious" class="btn">prev</button>
                    <button v-on:click="onNext" class="btn">next</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        data() {
            return { 
                enabled: false , left: 10, width: 100, height: 10, top: 10, text: '', index: 0,
                info: {width: null, height: null}
            }
        },
        computed: {
            edit() {
                return window.parent !== window && window.parent.$perAdminApp !== undefined
            },
            bottom() { return this.top + this.height },
            right() { return this.left + this.width },
            tourClass() {
                return '__pcms_tour'
            },
            tourItemStyle() {
                return 'top: 0; left: 0;'
            },
            leftStyle() {
                return { top: `${ this.top }px`, left: '0px', height: `${ this.height }px`, width: `${ this.left }px`}
            },
            rightStyle() {
                return { top: `${ this.top }px`, left: `${ this.right }px`, height: `${ this.height }px`, width: `${window.innerWidth - this.right}px`}
            },
            bottomStyle() {
                return { top: `${ this.bottom }px`, left: '0px', width: '100%', height: `${window.innerHeight - this.bottom}px`}
            },
            topStyle() {
                return { top: '0px', left: '0px', width: '100%', height: `${ this.top }px`}
            },
            highliteStyle() {
                return { top: `${this.top}px`, left: `${this.left}px`, width: `${this.width}px`, height: `${this.height}px`}
            },
            infoStyle() {
                const placeLeft  = {left: `${this.left - this.info.width - 20}px`}
                const placeRight = {left: `${this.right + 10}px`}
                const placeAbove = {top : `${this.top - this.info.height - 20}px`}
                const placeBelow = {top : `${this.bottom + 10}px`}

                //Use anchor if supplied
                const anchor = this.model.children[this.index].anchor;
                if (anchor) {
                    let vertical, horizontal = {}
                    if(anchor.indexOf('top') > -1) vertical     = {top : 0}
                    if(anchor.indexOf('bottom') > -1) vertical  = {top : `${window.innerHeight - this.info.height}px`}
                    if(anchor.indexOf('left') > -1) horizontal  = {left: 0}
                    if(anchor.indexOf('right') > -1) horizontal = {left: `${window.innerWidth - this.info.width}px`}
                    return Object.assign( vertical, horizontal);
                }

                const spaceLeft  = this.left;
                const spaceRight = window.innerWidth - this.right;
                const spaceAbove = this.top;
                const spaceBelow = window.innerHeight - this.bottom;

                const horizontalStyle = spaceLeft > spaceRight ? 
                    placeLeft : placeRight;
                const verticalStyle = spaceAbove > spaceBelow ? 
                    placeAbove : placeBelow;

                if ( spaceBelow > (this.info.height + 40) || spaceAbove > (this.info.height + 40)) {
                    return Object.assign( verticalStyle, {left: `${this.left}px`});
                }
                if ( spaceLeft > (this.info.width + 40) || spaceRight > (this.info.width + 40)) {
                    return Object.assign( horizontalStyle, {top: `${this.top}px`});
                }
                return {
                    top: `${this.bottom - this.info.height}px`,
                    left:`${this.right - this.info.width}px`
                }
            }
        },

        methods: {
            findElement(node, path) {
                if(node) {
                    if(node.model && node.model.path === path) return node.$el
                    for(let i = 0; i < node.$children.length; i++) {
                        const ret = this.findElement(node.$children[i], path)
                        if(ret !== null) {
                            return ret
                        }
                    }
                }
                return null
            },
            showTour(me, target) {
                me.enabled = true
                me.showTourItem()
            },
            showTourItem() {
                const root = this.findElement(this.$root, this.model.children[this.index].locator)
                const el = this.model.children[this.index].selector ? 
                    root.querySelector(this.model.children[this.index].selector) : root;
                this.enabled = false
                this.text = ''
                if(el !== null) {
                    const rect = el.getBoundingClientRect()
                    this.left = rect.left
                    this.top = rect.top
                    this.width = rect.width
                    this.height = rect.height
                    this.text = this.model.children[this.index].text
                    this.enabled = true
                }
            },
            onNext() {
                this.index++
                if(this.index === this.model.children.length) this.index = 0
                this.showTourItem()
            },
            onPrevious() {
                this.index--
                if(this.index === -1) this.index = this.model.children.length -1
                this.showTourItem()
            },
            windowResize() {
                if(this.enabled) {
                    this.showTourItem()
                }
            }
        },
        mounted() {
            this.index = 0
            window.addEventListener('resize', this.windowResize)
        },
        updated() {
            this.info.width = this.$refs.info ? this.$refs.info.offsetWidth : 0;
            this.info.height = this.$refs.info ?this.$refs.info.offsetHeight : 0;
        },
        beforeDestroy() {
            window.removeEventListener('resize', this.windowResize)
        }
    }
</script>

<style>
    .__pcms_tour {
        position: fixed;
        z-index: 1003;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
    }
    .__pcms_tour_overlay {
        position: fixed;
        opacity: 0.75;
        background: black;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
    }
    .__pcms_tour_highlite {
        position: fixed;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
    }

    .__pcms_tour_info {
        margin: 0;
        min-width: 400px;
        position: fixed;
        max-width: 400px;
        transition: bottom 0.25s, top 0.25s, left 0.25s, right 0.25s;
    }

    .__pcms_tour_info .btn-close{
        float: right;
    }

    .__pcms_tour_info .card-action{
        display: flex;
        justify-content: space-between;
    }

    .tour-left  { transition: top 0.25s, width 0.25s, height 0.25s }
    .tour-right { transition: top 0.25s, left 0.25s, width 0.25s, height 0.25s }
    .tour-top   { transition: height 0.25s }
    .tour-bot   { transition: top 0.25s, height 0.25s }

</style>
