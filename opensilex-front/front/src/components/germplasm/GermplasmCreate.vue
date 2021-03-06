<template>
  <div class="container-fluid">
    <opensilex-PageHeader
      icon="fa#sun"
      title="GermplasmCreate.title"
      description="GermplasmCreate.description"
    ></opensilex-PageHeader>

    <opensilex-PageActions>
      <template v-slot>
        <b-nav pills>
          <router-link
            to="/germplasm/"
            class="btn btn-outline-primary back-button"
            :title="$t('GermplasmDetails.backToList')"
          >
            <i class="ik ik-corner-up-left"></i>
          </router-link>
        </b-nav>
      </template>
    </opensilex-PageActions>

    <opensilex-PageContent>
      <b-input-group class="mt-3 mb-3" size="sm">
        <b-form-select
          ref="germplasmType"
          v-model="selected"
          :options="germplasmTypes"
          @change="refreshTable"
        >
          <template v-slot:first>
            <b-form-select-option :value="null">{{$t('GermplasmCreate.select')}}</b-form-select-option>
          </template>
        </b-form-select>
      </b-input-group>
      <opensilex-GermplasmTable v-if="this.selected" ref="germplasmTable" :germplasmType="selected"></opensilex-GermplasmTable>
    </opensilex-PageContent>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import {
  GermplasmCreationDTO,
  OntologyService,
  ResourceTreeDTO,
} from "opensilex-core/index";
import Oeso from "../../ontologies/Oeso";
import HttpResponse, { OpenSilexResponse } from "../../lib/HttpResponse";

@Component
export default class GermplasmCreate extends Vue {
  service: OntologyService;
  $opensilex: any;
  germplasmTypes: any = [];
  selected: string = null;

  get user() {
    return this.$store.state.user;
  }

  get credentials() {
    return this.$store.state.credentials;
  }

  created() {
    this.loadGermplasmTypes();
  }

  mounted() {
    this.$store.watch(
      () => this.$store.getters.language,
      (lang) => {
        this.loadGermplasmTypes();
      }
    );
  }

  loadGermplasmTypes() {
    this.germplasmTypes = [];
    let ontoService: OntologyService = this.$opensilex.getService(
      "opensilex.OntologyService"
    );

    ontoService
      .getSubClassesOf(Oeso.GERMPLASM_TYPE_URI, true)
      .then((http: HttpResponse<OpenSilexResponse<Array<ResourceTreeDTO>>>) => {
        console.log(http.response.result);
        for (let i = 0; i < http.response.result.length; i++) {
          let resourceDTO = http.response.result[i];
          if (resourceDTO.uri.endsWith("PlantMaterialLot")) {
            //retrieve plantMaterialLot children
            let children = resourceDTO.children;
            for (let j = 0; j < children.length; j++) {
              this.germplasmTypes.push({
                value: children[j].uri,
                text: children[j].name,
              });
            }
          } else {
            this.germplasmTypes.push({
              value: resourceDTO.uri,
              text: resourceDTO.name,
            });
          }
        }
      })
      .catch(this.$opensilex.errorHandler);
  }

  refreshTable() {
    let germplasmTable: any = this.$refs.germplasmTable;
    germplasmTable.updateColumns();
  }
}
</script>

<style scoped lang="scss">
</style>

<i18n>

en:
  GermplasmCreate:
    title: Declare Germplasm
    description: Add species, varieties, accessions ...
    backToList: Return to germplasm list
    select: Please select a type
fr:
  GermplasmCreate:
    title: Déclarer des ressources génétiques
    description: Créer des espèces, des variétiés, des accessions ...
    backToList: Revenir à la liste des germplasm
    select: Veuillez sélectionner un type de germplasm
  
</i18n>