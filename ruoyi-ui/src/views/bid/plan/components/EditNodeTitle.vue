<template>
  <span class="title-editor">
    <template v-if="editing">
      <span class="node-prefix">{{ node.titlePrefix }}</span>
      <el-input v-model="value" size="small" @change="save" @blur="save" />
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
}
.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-prefix {
  flex: 0 0 auto;
  color: #606266;
}
</style>

