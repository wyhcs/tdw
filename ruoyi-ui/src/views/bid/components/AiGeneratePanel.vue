<template>
  <section class="ai-panel">
    <div class="panel-title">AI 操作</div>
    <el-button class="wide-button" type="primary" icon="el-icon-magic-stick" :loading="generatingOutline" @click="$emit('generate-outline')">
      生成三级大纲
    </el-button>
    <el-divider />
    <el-form label-position="top" size="small">
      <el-form-item label="写入模式">
        <el-radio-group v-model="form.mode">
          <el-radio-button label="append">追加</el-radio-button>
          <el-radio-button label="overwrite">覆盖</el-radio-button>
          <el-radio-button label="keep">建议</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="正文风格">
        <el-radio-group v-model="form.writingStyle">
          <el-radio-button label="general">通用型</el-radio-button>
          <el-radio-button label="data">数据型</el-radio-button>
          <el-radio-button label="concise">简练型</el-radio-button>
          <el-radio-button label="practical">实用型</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="补充要求">
        <el-input v-model="form.requirement" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" :placeholder="placeholder" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.includeTable">生成表格块</el-checkbox>
        <el-checkbox v-model="form.includeDiagram">生成结构图块</el-checkbox>
      </el-form-item>
    </el-form>
    <el-button class="wide-button" icon="el-icon-document-copy" :loading="generatingContent" @click="generate('full')">生成全文内容</el-button>
    <el-button class="wide-button" icon="el-icon-edit-outline" :disabled="!selectedOutline" :loading="generatingContent" @click="generate('selected')">生成当前{{ selectedLevelText }}</el-button>
    <el-button class="wide-button" icon="el-icon-refresh" :loading="generatingContent" @click="rewrite('full')">重编全文</el-button>
    <el-button class="wide-button" icon="el-icon-refresh" :disabled="!selectedOutline" :loading="generatingContent" @click="rewrite('selected')">重编当前{{ selectedLevelText }}</el-button>
    <el-divider />
    <el-button class="wide-button" icon="el-icon-s-grid" :disabled="!selectedOutline" @click="$emit('insert-table')">插入表格</el-button>
    <el-button class="wide-button" icon="el-icon-picture-outline" :disabled="!selectedOutline" @click="$emit('insert-diagram')">插入结构图</el-button>
    <el-button class="wide-button" icon="el-icon-picture" :disabled="!selectedOutline" @click="$emit('insert-gallery-image')">插入图库图片</el-button>
    <el-button class="wide-button" type="success" icon="el-icon-download" @click="$emit('export-plan')">导出{{ exportName }}</el-button>
  </section>
</template>

<script>
export default {
  name: 'AiGeneratePanel',
  props: {
    selectedOutline: { type: Object, default: null },
    generatingOutline: { type: Boolean, default: false },
    generatingContent: { type: Boolean, default: false },
    moduleType: { type: String, default: 'plan' },
    exportName: { type: String, default: '方案' }
  },
  data() {
    return {
      form: { mode: 'append', writingStyle: 'general', requirement: '', includeTable: true, includeDiagram: true }
    }
  },
  computed: {
    selectedLevelText() {
      if (!this.selectedOutline) return '节点'
      return this.selectedOutline.level === 1 ? '章' : this.selectedOutline.level === 2 ? '节' : '内容标题'
    },
    placeholder() {
      return this.moduleType === 'tender' ? '例如：严格响应招标文件评分项，突出技术方案和服务承诺' : '例如：突出技术路线，包含实施计划和风险控制'
    }
  },
  methods: {
    generate(scope) {
      this.$emit('generate-content', { ...this.form, scope })
    },
    rewrite(scope) {
      this.$emit('generate-content', {
        ...this.form,
        scope,
        requirement: this.form.requirement || '请对当前范围内容进行重编，保持专业、正式、符合投标文件写作规范。'
      })
    }
  }
}
</script>

<style scoped>
.ai-panel { height: 100%; overflow: auto; padding: 12px; }
.panel-title { margin-bottom: 12px; font-weight: 600; color: #303133; }
.wide-button { width: 100%; margin: 0 0 10px 0; }
.ai-panel ::v-deep .el-radio-button__inner { padding-left: 12px; padding-right: 12px; }
</style>
