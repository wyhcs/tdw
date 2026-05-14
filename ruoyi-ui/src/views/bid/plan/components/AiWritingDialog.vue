<template>
  <el-dialog
    :title="title"
    :visible.sync="innerVisible"
    width="86vw"
    append-to-body
    class="ai-writing-dialog"
    @close="$emit('update:visible', false)"
  >
    <div class="ai-shell">
      <div class="ai-card">
        <div class="ai-card-head">
          <span class="avatar">AI</span>
          <strong>{{ currentNodeTitle }}</strong>
          <el-button type="primary" icon="el-icon-circle-check" @click="useContent">使用该内容</el-button>
        </div>
        <div class="ai-output" v-loading="streaming">
          <pre v-if="generated">{{ generated }}</pre>
          <div v-else class="ai-empty">选择撰写方式后，点击生成按钮。</div>
        </div>
      </div>

      <div class="ai-input">
        <el-input
          v-model="manualInstruction"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 6 }"
          :placeholder="placeholder"
        />
        <div class="ai-input-actions">
          <el-dropdown trigger="click" @command="selectMode">
            <el-button plain>
              {{ selectedModeLabel }} <i class="el-icon-arrow-up el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="item in modeOptions"
                :key="item.value"
                :command="item.value"
                :disabled="item.disabled"
              >
                <div class="mode-item">
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.desc }}</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button type="primary" plain icon="el-icon-magic-stick" :loading="streaming" @click="generate">
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
    visible: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: 'direction'
    },
    bidId: {
      type: [String, Number],
      required: true
    },
    node: {
      type: Object,
      default: null
    }
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
      return this.type === 'direction' ? '可手动输入编写方向，AI可自动为您生成。' : '可手动输入编写要求，AI可自动为您生成。'
    },
    currentNodeTitle() {
      return this.node ? this.node.title : '当前目录'
    },
    modeOptions() {
      if (this.type === 'direction') {
        return [
          { label: '自己写', value: 'self', desc: '手动编写指令' },
          { label: '四字标题', value: 'four_title', desc: '标题内容四字格式化' },
          { label: '六字标题', value: 'six_title', desc: '标题内容六字格式化' },
          { label: '自由发挥', value: 'free', desc: '标题内容自由发挥' },
          { label: '动宾结合', value: 'verb_object', desc: '编写方向动宾结合' }
        ]
      }
      return [
        { label: '自己写', value: 'self', desc: '手动编写要求' },
        { label: '简单直观', value: 'simple', desc: '内容简单化' },
        { label: '结构输出', value: 'structure', desc: '内容结构化' },
        { label: '人称静止', value: 'no_pronoun', desc: '禁止特定名称' }
      ]
    },
    selectedModeLabel() {
      const item = this.modeOptions.find(item => item.value === this.mode)
      return item ? item.label : '自己写'
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.generated = ''
        this.manualInstruction = ''
        this.mode = 'self'
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
      this.$emit('use', {
        content,
        mode: this.mode
      })
      this.innerVisible = false
    }
  }
}
</script>

<style scoped>
.ai-writing-dialog ::v-deep .el-dialog__body {
  background: #f5f7fb;
  padding: 20px 24px;
}
.ai-shell {
  border: 1px solid #d7dde8;
  border-radius: 6px;
  background: #fff;
  padding: 18px;
}
.ai-card {
  min-height: 420px;
  border: 1px solid #dfe4ee;
  border-radius: 6px;
  overflow: hidden;
}
.ai-card-head {
  height: 72px;
  padding: 0 18px;
  border-bottom: 1px solid #dfe4ee;
  display: flex;
  align-items: center;
  gap: 14px;
}
.ai-card-head .el-button {
  margin-left: auto;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #4b7bec, #8e63ff);
  font-weight: 700;
}
.ai-output {
  min-height: 330px;
  padding: 24px;
  font-size: 16px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
}
.ai-output pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
}
.ai-empty {
  color: #a8abb2;
}
.ai-input {
  margin-top: 18px;
  border: 1px solid #dfe4ee;
  border-radius: 6px;
  padding: 16px;
}
.ai-input-actions {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
}
.mode-item {
  min-width: 180px;
  display: flex;
  flex-direction: column;
}
.mode-item span {
  color: #a8abb2;
  font-size: 12px;
}
</style>
