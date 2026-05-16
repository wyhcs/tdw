<template>
  <el-dialog
    :title="title"
    :visible.sync="innerVisible"
    width="860px"
    append-to-body
    class="ai-writing-dialog"
    @close="$emit('update:visible', false)"
  >
    <div class="ai-shell">
      <div class="ai-output-card" v-loading="streaming">
        <div class="ai-output-head">
          <strong>{{ currentNodeTitle }}</strong>
          <el-button type="primary" plain icon="el-icon-circle-check" @click="useContent">使用该内容</el-button>
        </div>
        <div class="ai-output-body">
          <pre v-if="generated">{{ generated }}</pre>
          <span v-else class="empty-text">AI生成内容将显示在这里，点击下方按钮开始生成。</span>
        </div>
      </div>

      <div class="ai-input-card">
        <el-input
          v-model="manualInstruction"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 6 }"
          :placeholder="placeholder"
        />
        <div class="actions">
          <el-dropdown trigger="click" @command="selectMode">
            <el-button plain>{{ selectedModeLabel }} <i class="el-icon-arrow-down el-icon--right" /></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="item in modeOptions" :key="item.value" :command="item.value">
                <div class="mode-item">
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.desc }}</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button type="primary" icon="el-icon-magic-stick" :loading="streaming" @click="generate">
            生成{{ shortTitle }}
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { streamPost } from '@/utils/aiStream'

export default {
  name: 'AiWritingDialog',
  props: {
    visible: { type: Boolean, default: false },
    type: { type: String, default: 'direction' },
    bidId: { type: [String, Number], required: true },
    node: { type: Object, default: null }
  },
  data() {
    return {
      mode: 'self',
      generated: '',
      manualInstruction: '',
      streaming: false
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
    title() {
      return this.type === 'direction' ? '编写方向目录' : '编写要求目录'
    },
    shortTitle() {
      return this.type === 'direction' ? '编写方向' : '编写要求'
    },
    placeholder() {
      return this.type === 'direction' ? '输入编写方向，AI可自动生成；也可直接输入后使用。' : '输入编写要求，AI可自动生成；也可直接输入后使用。'
    },
    currentNodeTitle() {
      return this.node ? this.node.title : '当前目录'
    },
    modeOptions() {
      if (this.type === 'direction') {
        return [
          { label: '自己写', value: 'self', desc: '' },
          { label: '四字标题', value: 'four_title', desc: '请将标题下的所有内容输出为四字格式（标题：内容）' },
          { label: '六字标题', value: 'six_title', desc: '请将标题下的所有内容输出为六字格式（标题：内容）' },
          { label: '自由发挥', value: 'free', desc: '请将标题下的每条内容，字数不限，自由决定长度。（标题：内容）' },
          { label: '动宾结合', value: 'verb_object', desc: '用“动词+具体交付物”描述每个工作项。' }
        ]
      }
      return [
        { label: '自己写', value: 'self', desc: '' },
        { label: '简单直观', value: 'simple', desc: '使用通俗、直接词语，避免复杂句式或抽象术语。' },
        { label: '结构输出', value: 'structure', desc: '使用数字序列组织内容，如 1、2、3。' },
        { label: '人称静止', value: 'no_pronoun', desc: '禁止使用“你、我、他、我们”等人称代词。' }
      ]
    },
    selectedModeLabel() {
      const selected = this.modeOptions.find(item => item.value === this.mode)
      return selected ? selected.label : '自己写'
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.mode = 'self'
        this.generated = ''
        this.manualInstruction = ''
      }
    }
  },
  methods: {
    selectMode(value) {
      this.mode = value
    },
    generate() {
      if (!this.node || !this.node.id) {
        this.$modal.msgWarning('请先选择目录节点')
        return
      }
      this.generated = ''
      this.streaming = true
      const suffix = this.type === 'direction' ? 'writing-direction' : 'writing-requirement'
      streamPost(`/tdw/plan/${this.bidId}/outline/nodes/${this.node.id}/${suffix}/ai`, {
        mode: this.mode,
        manualInstruction: this.manualInstruction
      }, {
        onMessage: chunk => {
          this.generated += chunk || ''
        },
        onError: error => {
          this.$modal.msgError(error || 'AI生成失败')
        }
      }).catch(error => {
        this.$modal.msgError(error.message || 'AI生成失败')
      }).finally(() => {
        this.streaming = false
      })
    },
    useContent() {
      const content = this.generated || this.manualInstruction
      if (!content) {
        this.$modal.msgWarning('暂无可使用内容')
        return
      }
      this.$emit('use', { content, mode: this.mode })
      this.innerVisible = false
    }
  }
}
</script>

<style scoped>
.ai-shell {
  display: grid;
  gap: 14px;
}
.ai-output-card,
.ai-input-card {
  border: 1px solid #dfe4ee;
  border-radius: 8px;
  background: #fff;
}
.ai-output-head {
  min-height: 52px;
  padding: 0 14px;
  border-bottom: 1px solid #eef1f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ai-output-body {
  min-height: 220px;
  max-height: 360px;
  overflow: auto;
  padding: 14px;
  color: #303133;
  line-height: 1.8;
}
.ai-output-body pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
}
.empty-text {
  color: #a8abb2;
}
.ai-input-card {
  padding: 14px;
}
.actions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mode-item {
  min-width: 320px;
  display: grid;
  gap: 4px;
}
.mode-item span {
  color: #8f98a8;
  font-size: 12px;
}
</style>
