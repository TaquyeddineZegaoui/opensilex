<template>
  <div>
    <div class="row">
      <div class="col-md-6">
        <b-card>
          <b-button-group>
            <opensilex-CreateButton label="ExperimentScientificObjects.add-facilities"></opensilex-CreateButton>
            <opensilex-CreateButton label="ExperimentScientificObjects.import-scientific-objects"></opensilex-CreateButton>
          </b-button-group>
          <opensilex-TreeView :nodes.sync="nodes" @select="displayScientificObjectDetails">
            <template v-slot:node="{ node }">
              <span class="item-icon">
                <opensilex-Icon :icon="$opensilex.getRDFIcon(node.data.type)" />
              </span>&nbsp;
              <strong v-if="node.data.selected">{{ node.title }}</strong>
              <span v-if="!node.data.selected">{{ node.title }}</span>
            </template>

            <template v-slot:buttons="{ node }">
              <opensilex-AddChildButton
                v-if="user.hasCredential(credentials.CREDENTIAL_EXPERIMENT_MODIFICATION_ID)"
                @click="addExistingScientificObjectAsChild(node.data.uri)"
                label="ExperimentScientificObjects.add-facility-scientific-objects"
                :small="true"
              ></opensilex-AddChildButton>
              <opensilex-DeleteButton
                v-if="user.hasCredential(credentials.CREDENTIAL_EXPERIMENT_MODIFICATION_ID)"
                @click="deleteScientificObjectRelation(node.data.uri)"
                label="ExperimentScientificObjects.delete-facility-relation"
                :small="true"
              ></opensilex-DeleteButton>
            </template>
          </opensilex-TreeView>
        </b-card>
      </div>
      <div class="col-md-6">
        <b-card>SO details</b-card>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component } from "vue-property-decorator";
import Vue from "vue";
@Component
export default class ExperimentScientificObjects extends Vue {
  get user() {
    return this.$store.state.user;
  }

  public nodes = [];

  addExistingScientificObjectAsChild(facilityURI) {}

  deleteFacilityRelation(facilityURI) {}

  public displayScientificObjectDetails(node: any) {}
}
</script>

<style scoped lang="scss">
</style>