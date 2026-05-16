<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="innerVisible"
    width="640px"
    append-to-body
    @open="reset"
    @close="$emit('update:visible', false)"
  >
    <el-form ref="form" :model="form" :rules="rules" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="请输入标题" />
      </el-form-item>

      <div class="form-grid">
        <el-form-item v-if="showSectionCount" label="要求节数" prop="sectionCount">
          <el-input-number v-model="form.sectionCount" :min="1" :max="20" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="showParagraphCount" label="每节段数" prop="paragraphCount">
          <el-input-number v-model="form.paragraphCount" :min="1" :max="30" controls-position="right" />
        </el-form-item>
        <el-form-item label="单段字数" prop="wordLimit">
          <el-select v-model="form.wordLimit" filterable>
            <el-option v-for="item in wordOptions" :key="item" :label="item + '字'" :value="item" />
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="需求描述">
        <div class="desc-head">
          <span>选填，1000字以内</span>
          <el-button size="mini" type="primary" icon="el-icon-magic-stick" plain @click="mockHelp">AI帮写</el-button>
        </div>
        <el-input
          v-model="form.requirementDesc"
          type="textarea"
          maxlength="1000"
          show-word-limit
          :autosize="{ minRows: 6, maxRows: 10 }"
          placeholder="请输入1000字以内需求描述"
        />
      </el-form-item>
    </el-form>

    <span slot="footer">
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">新增</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'AddNodeDialog',
  props: {
    visible: { type: Boolean, default: false },
    node: { type: Object, default: null },
    mode: { type: String, default: 'sibling' },
    submitting: { type: Boolean, default: false }
  },
  data() {
    return {
      form: {
        title: '',
        sectionCount: 3,
        paragraphCount: 3,
        wordLimit: 1200,
        requirementDesc: ''
      },
      wordOptions: [300, 600, 900, 1200, 1800, 2700, 3600, 4500, 5400, 6300, 7200, 8100, 9000, 9900],
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        sectionCount: [{ required: true, message: '请输入要求节数', trigger: 'change' }],
        paragraphCount: [{ required: true, message: '请输入每节段数', trigger: 'change' }],
        wordLimit: [{ required: true, message: '请选择单段字数', trigger: 'change' }]
      }
    }
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    level() {
      return this.node ? Number(this.node.level) : 1
    },
    showSectionCount() {
      return this.mode === 'sibling' && this.level === 1
    },
    showParagraphCount() {
      return (this.mode === 'sibling' && this.level < 3) || (this.mode === 'child' && this.level === 1)
    },
    dialogTitle() {
      if (!this.node) return '新增节点'
      if (this.mode === 'child') return this.level === 1 ? '新增节' : '新增段'
      if (this.mode === 'paragraph' || this.level === 3) return '新增单段'
      return this.level === 1 ? '新增章' : '新增节'
    }
  },
  methods: {
    reset() {
      this.form = {
        title: '',
        sectionCount: 3,
        paragraphCount: 3,
        wordLimit: 1200,
        requirementDesc: ''
      }
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    mockHelp() {
      const title = this.form.title || (this.node && this.node.titleText) || '新增目录'
      this.form.requirementDesc = `围绕“${title}”说明实施目标、关键步骤、交付成果和验收依据。`
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const data = {
          title: this.form.title,
          wordLimit: this.form.wordLimit,
          requirementDesc: this.form.requirementDesc
        }
        if (this.showSectionCount) data.sectionCount = this.form.sectionCount
        if (this.showParagraphCount) data.paragraphCount = this.form.paragraphCount
        this.$emit('submit', data)
      })
    }
  }
}
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.form-grid ::v-deep .el-input-number,
.form-grid ::v-deep .el-select {
  width: 100%;
}
.desc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #909399;
}
@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
