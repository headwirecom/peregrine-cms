<template>
  <ul class="tree-view">
    <admin-components-treeitem
        class="item"
        :item="data.demo"
        @add-item="addItem"/>
  </ul>
</template>

<script>
  export default {
    name: 'TreeView',
    data() {
      return {
        data: {
          demo: {
            name: "My Tree",
            children: [
              { name: "hello" },
              { name: "wat" },
              {
                name: "child folder",
                children: [
                  {
                    name: "child folder",
                    children: [{ name: "hello" }, { name: "wat" }]
                  },
                  { name: "hello" },
                  { name: "wat" },
                  {
                    name: "child folder",
                    children: [{ name: "hello" }, { name: "wat" }]
                  }
                ]
              }
            ]
          }
        }
      }
    },
    methods: {
      addItem(item) {
        item.children.push({
          name: "new stuff"
        })
      }
    }
  }
</script>

<style scoped>

</style>