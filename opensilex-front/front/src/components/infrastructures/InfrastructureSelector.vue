<template>
  <opensilex-SelectForm
    :label="label"
    :selected.sync="infrastructuresURI"
    :multiple="multiple"
    :options="infrastructuresOptions"
    placeholder="component.infrastructure.filter-placeholder"
    @select="select"
    @deselect="deselect"
  ></opensilex-SelectForm>
</template>

<script lang="ts">
import { Component, Prop, PropSync } from "vue-property-decorator";
import Vue, { PropOptions } from "vue";
import HttpResponse, { OpenSilexResponse } from "opensilex-core/HttpResponse";
import { InfrastructureGetDTO, ResourceTreeDTO } from "opensilex-core/index";

@Component
export default class InfrastructureSelector extends Vue {
  $opensilex: any;

  @PropSync("infrastructures")
  infrastructuresURI;

  @Prop()
  label;

  @Prop()
  multiple;

  infrastructuresOptions = [];
  mounted() {
    this.$opensilex
      .getService("opensilex-core.InfrastructuresService")
      .searchInfrastructuresTree()
      .then((http: HttpResponse<OpenSilexResponse<Array<ResourceTreeDTO>>>) => {
        this.infrastructuresOptions = this.$opensilex.buildTreeListOptions(
          http.response.result
        );
      })
      .catch(this.$opensilex.errorHandler);
  }

  select(value) {
    this.$emit("select", value);
  }

  deselect(value) {
    this.$emit("deselect", value);
  }
}
</script>

<style scoped lang="scss">
</style>
