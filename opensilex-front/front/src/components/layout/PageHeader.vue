<template>
  <div class="page-header">
    <div class="row">
      <div class="col-lg-8">
        <div class="page-header-title">
          <div class="d-inline">
            <h5>
              <opensilex-Icon :icon="icon" class="title-icon" />
              <slot name="title">&nbsp;{{ $t(title) }}</slot>
            </h5>
            <span><slot name="description">{{ $t(description) }}</slot></span>
          </div>
        </div>
      </div>
      <div class="col-lg-4">
        <nav class="breadcrumb-container" aria-label="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <router-link :to="{path: '/dashboard'}" :title="$t('component.menu.backToDashboard')">
                <opensilex-Icon icon="ik#ik-grid" />
                
                {{ $t('component.menu.dashboard') }}
              </router-link>
            </li>
            <li class="breadcrumb-item active">
              <opensilex-Icon :icon="icon" />
              &nbsp;{{ $t(title) }}
            </li>
          </ol>
        </nav>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop } from "vue-property-decorator";
import Vue from "vue";

@Component
export default class PageHeader extends Vue {
  $opensilex: any;

  @Prop()
  icon;

  @Prop()
  title;

  @Prop()
  description;
}
</script>

<style scoped lang="scss">
.title-icon {
  float: left;
  width: 40px;
  height: 40px;
  border-radius: 5px;
  margin-right: 20px;
  vertical-align: middle;
  font-size: 22px;
  color: #fff;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 12px -3px rgba(0, 0, 0, 0.5);
  background-color: #00a38d;
}

h5 {
  margin-bottom: 0;
}
</style>