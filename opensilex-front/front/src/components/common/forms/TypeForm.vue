<template>
  <opensilex-FormField
    :rules="rules"
    :required="required"
    label="component.common.type"
  >
    <!-- helpMessage="component.common.type.help-message" -->
    <template v-slot:field="field">
      <treeselect
        :id="field.id"
        :options="typesOptions"
        :load-options="initTypes"
        :placeholder="$t(placeholder)"
        :disabled="disabled"
        v-model="typeURI"
        @select="field.validator && field.validator.validate()"
        @close="field.validator && field.validator.validate()"
      />
    </template>
  </opensilex-FormField>
</template>

<script lang="ts">
import {
  Component,
  Prop,
  Model,
  Provide,
  PropSync,
  Ref
} from "vue-property-decorator";
import Vue from "vue";
import { OntologyService, ResourceTreeDTO } from "opensilex-core/index";
import HttpResponse, { OpenSilexResponse } from "opensilex-core/HttpResponse";

@Component
export default class TypeForm extends Vue {
  $opensilex: any;
  service: OntologyService;

  @PropSync("type")
  typeURI: string;

  @Prop()
  baseType: string;

  @Prop()
  placeholder: string;

  @Prop()
  required: boolean;

  @Prop()
  disabled: boolean;

  @Prop()
  rules: string | Function;

  typesOptions = null;

  id: string;

  created() {
    this.service = this.$opensilex.getService(
      "opensilex-security.OntologyService"
    );
    this.id = this.$opensilex.generateID();
  }

  mounted() {
    this.$store.watch(
      () => this.$store.getters.language,
      lang => {
        this.loadTypes();
      }
    );
  }

  initTypes({ action, parentNode, callback }) {
    this.loadTypes(callback);
  }

  loadTypes(callback?) {
    this.service
      .getSubClassesOf(this.baseType, true)
      .then((http: HttpResponse<OpenSilexResponse<Array<ResourceTreeDTO>>>) => {
        this.typesOptions = this.$opensilex.buildTreeListOptions(
          http.response.result
        );
        if (callback) {
          callback();
        }
      })
      .catch(this.$opensilex.errorHandler);
  }
}
</script>

<style scoped lang="scss">
</style>

