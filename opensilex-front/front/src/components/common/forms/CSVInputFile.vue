<template>
  <div>
    <b-button @click="importCsv" class="mb-2 mr-2" variant="outline-success">{{$t(buttonLabel)}}</b-button>
    <slot name="error">
      <span v-if="errors" class="error-message alert alert-danger mb-2 mr-2">{{this.errors[0]}}</span>
    </slot>
    <b-form-file
      ref="inputFile"
      accept="text/csv, .csv"
      @change="fileUpdated"
      hidden
      style="display:none"
    ></b-form-file>
  </div>
</template>

<script lang="ts">
import {
  Component,
  Prop,
  Ref,
  Model,
  Provide,
  PropSync
} from "vue-property-decorator";
import Vue from "vue";
import VuePapaParse from "vue-papa-parse";
Vue.use(VuePapaParse);

@Component
export default class CSVInputFile extends Vue {
  $opensilex: any;
  $papa: any;
  $store: any;

  @PropSync("csvData")
  data: any[];

  @Prop()
  headersToCheck: string[];

  @Prop({ default: true })
  header: boolean;

  @Prop()
  config: any;

  errors: any = [];

  @Prop({
    default: "component.common.import-files.csv-file"
  })
  buttonLabel: string;

  get lang() {
    return this.$store.getters.language;
  }

  @Prop()
  delimiterOption: string;

  delimiter: string;

  @Ref("inputFile") readonly inputFile!: any;

  importCsv() {
    this.inputFile.$el.childNodes[0].click();
  }
  fileUpdated() {
    this.$opensilex.showLoader();
    this.$nextTick(() => {
      let file = this.inputFile.selectedFile;
      this.readUploadedFileAsText(file).then(text => {
        console.debug("text", text);
        // delimiter per local
        if (this.delimiterOption == null) {
          switch (this.lang) {
            case "en":
              this.delimiter = ",";
              break;
            case "fr":
              this.delimiter = ";";
              break;
            default:
              this.delimiter = ",";
          }
        }
        let result = this.$papa.parse(text, {
          header: true,
          delimiter: this.delimiter
        });

        console.debug("result.data", result.data);
        console.debug("result.errors", result.errors);
        console.debug("result.meta", result.meta);
        this.errors = [];
        if (result.data == null || result.data.length == 0) {
          this.errors.push(
            "Unable to parse csv, delimiter used : '" + this.delimiter + "'"
          );
        } else {
          if (this.headersToCheck != null && this.headersToCheck.length > 0) {
            let objectToCheck = result.data[0];

            console.log(
              CSVInputFile.equalArrays(
                Object.keys(objectToCheck),
                this.headersToCheck
              ),
              Object.keys(objectToCheck),
              this.headersToCheck
            );
            if (
              !CSVInputFile.equalArrays(
                Object.keys(objectToCheck),
                this.headersToCheck
              )
            ) {
              this.errors.push(
                "Bad data : [" +
                  this.headersToCheck.toString() +
                  "] is excepted. [" +
                  Object.keys(objectToCheck) +
                  "] has been found."
              );
            }
          }
        }
        if(this.errors.length ==0){
            this.data = result.data;
            this.$emit("updated", result.data);
        } 
        this.$opensilex.hideLoader();
      });
    });
  }

  static equalArrays(arr1, arr2): boolean {
    const containsAll = (arr1, arr2) =>
      arr2.every(arr2Item => arr1.includes(arr2Item));

    return containsAll(arr1, arr2) && containsAll(arr2, arr1);
  }

  async readUploadedFileAsText(inputFile) {
    
    const temporaryFileReader = new FileReader();

    return new Promise((resolve, reject) => {
      temporaryFileReader.onerror = () => {
        temporaryFileReader.abort();
        reject(new DOMException("Problem parsing input file."));
      };

      temporaryFileReader.onload = () => {
        resolve(temporaryFileReader.result);
      };
      temporaryFileReader.readAsText(inputFile);
    });
    
  }
}
</script>

<style scoped lang="scss">
</style>

