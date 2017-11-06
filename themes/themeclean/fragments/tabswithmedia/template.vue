<template>
  <pcmsstock-components-block v-bind:model="model">
    <div>
      <div class="row">
        <h2 class="text-center col-12 pb-4" v-if="model.showtitle == 'true'"
        v-html="model.title"></h2>
        <h3 class="text-center col-12 pb-4" v-if="model.showsubtitle == 'true'"
        v-html="model.subtitle"></h3>
      </div>
      <div class="row">
        <pcmsstock-components-media :model="model"></pcmsstock-components-media>
        <div class="col-12 col-md">
          <!-- Tab Nav -->
          <div class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist"
          style="margin-left: 2em">
            <ul class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist">
              <li class="nav-item" v-for="(item,i) in model.tabs" :key="i">
                <a data-toggle="pill" role="tab" aria-expanded="true" v-bind:href="`#tab${_uid}${parseInt(i)+1}`"
                v-bind:class="i == 0 ? 'nav-link active' : 'nav-link'"
                v-bind:id="`tab-control-${_uid}${parseInt(i)+1}`" v-bind:aria-controls="`tab${_uid}${parseInt(i)+1}`"
                v-html="item.title"></a>
              </li>
            </ul>
          </div>
          <!-- Tab Content -->
          <div class="tab-content" id="myTabContent">
            <div role="tabpanel" v-for="(item,i) in model.tabs" :key="i" v-bind:id="`tab${_uid}${parseInt(i)+1}`"
            v-bind:aria-labelledby="`tablabel${_uid}${parseInt(i)+1}`" v-bind:class="i == 0 ? 'tab-pane fade show active' : 'tab-pane fade'">
              <div class="text-center py-5" v-html="item.text"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </pcmsstock-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>