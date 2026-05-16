<template>
  <span class="title-editor">
    <template v-if="editing">
      <span class="node-prefix">{{ node.titlePrefix }}</span>
      <el-input v-model="value" size="small" @keyup.enter.native="save" />
      <el-button class="title-save" size="mini" @click.stop="save">保存</el-button>
    </template>
    <template v-else>
      <span class="title-text" :title="displayTitle(node)">{{ displayTitle(node) }}</span>
    </template>
  </span>
</template>

<script>
export default {
  name: 'EditNodeTitle',
  props: {
    node: { type: Object, required: true },
    editing: { type: Boolean, default: false }
  },
  data() {
    return {
      value: ''
    }
  },
  watch: {
    node: {
      immediate: true,
      handler(node) {
        this.value = node.titleText || this.cleanTitle(node)
      }
    }
  },
  methods: {
    displayTitle(node) {
      if (!node) return ''
      const title = node.title == null ? '' : String(node.title).trim()
      if (title.length) return title
      const prefix = node.titlePrefix == null ? '' : String(node.titlePrefix).trim()
      const text = node.titleText == null ? this.cleanTitle(node) : String(node.titleText).trim()
      return [prefix, text].filter(Boolean).join(' ').trim()
    },
    cleanTitle(node) {
      const title = node && node.title ? String(node.title) : ''
      if (Number(node.level) === 1) return title.replace(/^第[一二三四五六七八九十百千万零〇两]+章\s*/, '')
      if (Number(node.level) === 2) return title.replace(/^第[一二三四五六七八九十百千万零〇两]+节\s*/, '')
      return title.replace(/^[（(][一二三四五六七八九十百千万零〇两]+[）)]\s*/, '')
    },
    save() {
      this.$emit('save', this.node, this.value)
    }
  }
}
</script>

<style scoped>
.title-editor {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  font-size: 14px;
  line-height: 1.35;
}
.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
}
.node-prefix {
  flex: 0 0 auto;
  color: #303133;
}
.title-editor ::v-deep .el-input {
  flex: 1;
  min-width: 0;
}
.title-editor ::v-deep .el-input__inner {
  height: 32px;
  line-height: 32px;
  color: #303133;
  font-size: 14px;
}
.title-save {
  flex: 0 0 auto;
}
</style>
