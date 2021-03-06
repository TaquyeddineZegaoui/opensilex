<template>
    <div>
        <ValidationObserver ref="validatorRef">
            <b-form>
                <p>
                    {{$t('component.skos.link-external')}} :
                    <em>
                        <strong class="text-primary">{{this.skosReferences.uri}}</strong>
                    </em>
                </p>
                <b-card bg-variant="light">
                    <div class="row">
                        <div class="col">
                            <b-form-group
                                    label="component.skos.ontologies-references-label"
                                    label-size="lg"
                                    label-class="font-weight-bold pt-0"
                                    class="mb-0"
                            >
                                <template v-slot:label>{{$t('component.skos.ontologies-references-label') }}</template>
                            </b-form-group>
                            <b-card-text>
                                <ul>
                                    <li
                                            v-for="externalOntologyRef in externalOntologiesRefs"
                                            :key="externalOntologyRef.label"
                                    >
                                        <a
                                                target="_blank"
                                                v-bind:title="externalOntologyRef.label"
                                                v-bind:href="externalOntologyRef.link"
                                                v-b-tooltip.v-info.hover.left="externalOntologyRef.description"
                                        >{{ externalOntologyRef.label }}</a>
                                    </li>
                                </ul>
                            </b-card-text>
                        </div>
                        <div class="col">
                            <b-form-group>
                                <opensilex-FormInputLabelHelper
                                        label="component.skos.relation"
                                        helpMessage="component.skos.relation-help"
                                ></opensilex-FormInputLabelHelper>
                                <ValidationProvider
                                        :name="$t('component.skos.relation')"
                                        :rules="{
                  required: true
                }"
                                        v-slot="{ errors }"
                                >
                                    <b-form-select
                                            required
                                            v-model="currentRelation"
                                            :placeholder="$t('component.skos.relation-placeholder')"
                                    >
                                        <b-form-select-option
                                                v-for="option in options"
                                                v-bind:key="option.value"
                                                v-bind:value="option.value"
                                        >{{ $t(option.text) }}
                                        </b-form-select-option>
                                    </b-form-select>
                                    <div class="mt-3">
                                        {{ $t('component.skos.current-relation')}} :
                                        <strong>{{ $t((currentRelation == "") ? 'component.skos.no-current-relation' :
                                            currentRelation) }}</strong>
                                    </div>
                                    <div class="error-message alert alert-danger">{{ errors[0] }}</div>
                                </ValidationProvider>
                            </b-form-group>
                            <!-- URI -->
                            <b-form-group>
                                <opensilex-FormInputLabelHelper
                                        label="component.skos.uri"
                                        helpMessage="component.skos.-help"
                                ></opensilex-FormInputLabelHelper>
                                <ValidationProvider
                                        :name="$t('component.skos.uri')"
                                        :rules="{
                    required: true,
                    regex: /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/
                  }"
                                        v-slot="{ errors }"
                                >
                    <span
                            class="error-message alert alert-danger"
                            v-if="isIncludedInRelations()"
                    >{{$t('component.skos.external-already-existing')}}</span>
                                    <b-form-input
                                            id="externalUri"
                                            v-model.trim="currentExternalUri"
                                            type="text"
                                            required
                                            :placeholder="$t('component.skos.uri-placeholder')"
                                            debounce="300"
                                    ></b-form-input>

                                    <div class="error-message alert alert-danger">{{ errors[0] }}</div>
                                </ValidationProvider>
                            </b-form-group>
                            <b-form-group label-align-sm="right">
                                <b-button
                                        @click="addRelationsToSkosReferences"
                                        variant="success"
                                >{{$t('component.skos.add')}}
                                </b-button>
                            </b-form-group>
                        </div>
                    </div>
                </b-card>

                <b-form-group v-if="displayInsertButton" label-align-sm="right">
                    <b-button
                            class="float-right"
                            @click="update"
                            variant="primary"
                    >{{$t("component.skos.update")}}
                    </b-button>
                </b-form-group>
            </b-form>
        </ValidationObserver>
        <div>
            <b-table v-if="relations.length !== 0" striped hover :items="relations" :fields="fields">
                <template v-slot:head(relation)="data">{{$t(data.label)}}</template>
                <template v-slot:cell(relation)="data">{{$t(data.value)}}</template>
                <template v-slot:head(relationURI)="data">{{$t(data.label)}}</template>
                <template v-slot:cell(relationURI)="data">
                    <a :href="data.value" target="_blank">{{$t(data.value)}}</a>
                </template>
                <template v-slot:head(actions)="data">{{$t(data.label)}}</template>
                <template v-slot:cell(actions)="data">
                    <b-button-group size="sm">
                        <b-button
                                size="sm"
                                @click="removeRelationsToSkosReferences(data.item)"
                                variant="danger"
                        >
                            <font-awesome-icon icon="trash-alt" size="sm"/>
                        </b-button>
                    </b-button-group>
                </template>
            </b-table>
            <p v-else>
                <strong>{{$t('component.skos.no-external-links-provided')}}</strong>
            </p>
        </div>
    </div>
</template>

<script lang="ts">
    import {Component, Prop, PropSync, Ref} from "vue-property-decorator";
    import Vue from "vue";
    import {Skos} from "../../../models/Skos";
    import {ExternalOntologies} from "../../../models/ExternalOntologies";

    @Component
    export default class ExternalReferencesForm extends Vue {
        $opensilex: any;
        $store: any;
        $t: any;
        $i18n: any;

        currentRelation: string = "";
        currentExternalUri: string = "";

        @PropSync("references")
        skosReferences: any;

        @Ref("validatorRef") readonly validatorRef!: any;

        @Prop({default: true})
        displayInsertButton: boolean;

        @Prop({default: (() => [])})
        ontologiesToSelect: string[];

        externalOntologiesRefs: any[] = ExternalOntologies.getExternalOntologiesReferences(this.ontologiesToSelect);

        relationsInternal: any[] = [];

        skosRelationsMap: Map<string, string> = Skos.getSkosRelationsMap();

        options: any[] = [
            {
                value: "",
                text: "component.skos.no-relation",
                disabled: true
            }
        ];

        created() {
            for (let [key, value] of this.skosRelationsMap) {
                this.$set(this.options, this.options.length, {
                    value: key,
                    text: value
                });
            }
        }

        resetForm() {
            this.currentRelation = "";
            this.currentExternalUri = "";
        }

        resetExternalUriForm() {
            this.currentExternalUri = "";
            this.$nextTick(() => this.validatorRef.reset());
        }

        fields = [
            {
                key: "relation",
                label: "component.skos.relation",
                sortable: true
            },
            {
                key: "relationURI",
                label: "component.skos.uri",
                sortable: false
            },
            {
                key: "actions",
                label: "component.common.actions"
            }
        ];

        get relations() {
            this.relationsInternal = [];
            if (this.skosReferences !== undefined) {
                for (let [key, value] of this.skosRelationsMap) {
                    this.updateRelations(key, this.skosReferences[key]);
                }
            }
            return this.relationsInternal;
        }

        updateRelations(relation: string, references: string[]) {
            for (let index = 0; index < references.length; index++) {
                const element = references[index];
                this.addRelation(relation, element);
            }
        }

        addRelation(relation: string, externalUri: string) {
            this.$set(this.relationsInternal, this.relationsInternal.length, {
                relation: this.skosRelationsMap.get(relation),
                relationURI: externalUri
            });
        }

        validateForm() {
            let validatorRef: any = this.$refs.validatorRef;
            return validatorRef.validate();
        }

        addRelationsToSkosReferences() {
            this.validateForm().then(isValid => {
                if (isValid) {
                    this.addRelationToSkosReferences();
                }
            });
        }

        addRelationToSkosReferences() {
            let isIncludedInRelations = this.isIncludedInRelations();
            if (!isIncludedInRelations) {
                this.skosReferences[this.currentRelation].push(this.currentExternalUri);
                this.resetExternalUriForm();
            }
        }

        isIncludedInRelations(): boolean {
            if (
                this.currentExternalUri == undefined ||
                this.currentExternalUri == "" ||
                this.currentExternalUri.length == 0
            ) {
                return false;
            }
            let includedInRelations = false;
            for (let [key, value] of this.skosRelationsMap) {
                if (this.skosReferences[key].includes(this.currentExternalUri)) {
                    includedInRelations = true;
                    break;
                }
            }
            return includedInRelations;
        }

        removeRelationsToSkosReferences(row: any) {
            for (let [key, value] of this.skosRelationsMap) {
                this.skosReferences[key] = this.skosReferences[key].filter(function (
                    value,
                    index,
                    arr
                ) {
                    return value != row.relationURI;
                });
            }
        }

        async update() {
            return new Promise((resolve, reject) => {
                this.$emit("onUpdate", this.skosReferences, result => {
                    if (result instanceof Promise) {
                        result.then(resolve).catch(reject);
                    } else {
                        resolve(result);
                    }
                });
            });
        }
    }
</script>

<style scoped lang="scss">
    a {
        color: #007bff;
    }
</style>