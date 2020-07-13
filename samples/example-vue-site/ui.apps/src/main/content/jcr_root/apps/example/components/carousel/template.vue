<!--
  #%L
  example site - UI Apps
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
  <div
    class="carousel slide"
    v-bind:id              ="name"
    v-bind:data-interval   ="model.interval"
    v-bind:data-pause      ="model.pause"
    v-bind:data-ride       ="model.ride"
    v-bind:data-indicators ="model.indicators"
    v-bind:data-controls   ="model.controls"
    v-bind:data-wrap       ="model.wrap"
    v-bind:data-keyboard   ="model.keyboard"
    v-bind:data-per-path   ="model.path"
    style="min-height: 300px;background-color:#eceff1;">
    <ol v-if="model.indicators" class="carousel-indicators">
      <li
        v-for="(slide, index) in model.children"
        :data-target="`#${name}`"
        :data-slide-to="index"></li>
    </ol>
    <div class="carousel-inner" role="listbox">
      <div v-for="(slide, index) in model.children" :class="`carousel-item ${index === 0 ? 'active' : ''}`">
        <img v-if="slide.imagePath" :src="slide.imagePath" :alt="slide.alt" />
        <div v-if="slide.heading || slide.text" class="carousel-caption">
          <h3 v-if="slide.heading" :data-per-inline="`model.children.${index}.heading`">
            {{slide.heading}}
          </h3>
          <p v-if="slide.text"
             v-html="slide.text"
             :data-per-inline="`model.children.${index}.text`"></p>
        </div>
      </div>
    </div>
    <template v-if="model.controls">
      <a class="carousel-control-prev" :href="`#${name}`" role="button" data-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </a>
      <a class="carousel-control-next" :href="`#${name}`" role="button" data-slide="next">
        <span class="carousel-control-next-icon"></span>
      </a>
    </template>
  </div>
</template>

<script>
  export default {
    props: ['model'],
    computed: {
      name() {
          return this.model.path.split('/').slice(1).join('-').slice(4)
      }
    },
    methods: {
      // beforeSave(data) {
      //   while(data.children.length > 0) {
      //     let name = data.children[0].name
      //     if(data.children[0].path) {
      //       name = data.children[0].path.split('/').pop()
      //     }
      //     delete data.children[0].path
      //     delete data.children[0].component
      //     data.children[0]['sling:resourceType'] = 'example/components/carouselItem'
      //     data[name] = data.children[0]
      //     data.children = data.children.slice(1)
      //   }
      //   return data
      // }
    }
  }
</script>
