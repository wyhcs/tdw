<template>
  <span>
    <el-dialog title="导出配置" :visible.sync="innerVisible" width="520px" append-to-body @close="$emit('update:visible', false)">
      <el-form label-width="112px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="form.fileFormat">
            <el-radio-button label="html">HTML</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="空标题">
          <el-switch v-model="form.includeEmptyOutline" active-text="保留" inactive-text="跳过" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="innerVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="$emit('submit', form)">导出</el-button>
      </div>
    </el-dialog>
    <el-dialog title="导出完成" :visible.sync="resultVisible" width="560px" append-to-body>
      <el-result icon="success" :title="exportName + '已导出'">
        <template slot="subTitle">
          <div class="export-result">
            <p>{{ result.downloadName }}</p>
            <el-link v-if="result.fileUrl" type="primary" :href="resourceUrl(result.fileUrl)" target="_blank">打开导出文件</el-link>
          </div>
        </template>
      </el-result>
      <div slot="footer">
        <el-button type="primary" @click="resultVisible = false">知道了</el-button>
      </div>
    </el-dialog>
  </span>
</template>

<script>
export default {
  name: 'ExportDialog',
  props: {
    visible: { type: Boolean, default: false },
    loading: { type: Boolean, default: false },
    result: { type: Object, default: () => ({}) },
    exportName: { type: String, default: '方案' }
  },
  data() {
    return {
      innerVisible: false,
      resultVisible: false,
      form: { fileFormat: 'html', includeEmptyOutline: true }
    }
  },
  watch: {
    visible(value) {
      this.innerVisible = value
    },
    innerVisible(value) {
      this.$emit('update:visible', value)
    },
    result(value) {
      if (value && value.fileUrl) {
        this.resultVisible = true
      }
    }
  },
  methods: {
    resourceUrl(url) {
      if (!url) return ''
      if (/^https?:\/\//.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    }
  }
}
</script>

<style scoped>
.export-result p { margin: 0 0 8px; color: #606266; }
</style>
