<template>
  <sl-vue-tree v-model="nodeList" @select="selectItem">
    <template slot="toggle" slot-scope="{ node }">
      <span v-if="!node.isLeaf">
        <opensilex-Icon v-if="node.isExpanded" icon="fa#chevron-down" />
        <opensilex-Icon v-if="!node.isExpanded" icon="fa#chevron-right" />
      </span>
    </template>

    <template slot="title" slot-scope="{ node }">
      <div v-if="node.isLeaf && node.data.parent" class="leaf-spacer"></div>

      <strong v-if="node.data.selected">
        <slot name="node" v-bind:node="node"></slot>
      </strong>
      <span v-if="!node.data.selected">
        <slot name="node" v-bind:node="node">></slot>
      </span>

      <b-button-group v-if="!noButtons" class="tree-button-group" size="sm">
        <slot name="buttons" v-bind:node="node"></slot>
      </b-button-group>
    </template>
  </sl-vue-tree>
</template>

<script lang="ts">
import { Component, Prop, PropSync } from "vue-property-decorator";
import copy from "copy-to-clipboard";
import Vue from "vue";

@Component
export default class TreeView extends Vue {
  @PropSync("nodes")
  nodeList: any;

  @Prop()
  noButtons: boolean;

  copy = copy;

  selectItem(nodes: any[]) {
    if (nodes.length > 0) {
      let node = nodes[nodes.length - 1];
      this.$emit("select", nodes[0]);
    }
  }
}
</script>

<style scoped lang="scss">
</style>

