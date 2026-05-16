<template>
  <span class="node-actions">
    <template v-if="tab === 'word'">
      <template v-if="Number(node.level) === 3">
        <span v-if="Number(node.contentWords || 0) > 0" class="word-metric">
          <b class="success">{{ Number(node.contentWords || 0) }}</b> / {{ Number(node.wordLimit || 300) }}
        </span>
        <el-select
          v-else
          v-model="node.wordLimit"
          size="mini"
          class="word-select"
          @change="$emit('single-word', node)"
        >
          <el-option v-for="item in wordOptions" :key="item" :label="item + '字'" :value="item" />
        </el-select>
      </template>
      <template v-else>
        <el-select
          v-if="hasEditableLeaf(node)"
          v-model="batchValue"
          size="mini"
          class="word-select"
          placeholder="批量修改"
          @change="$emit('batch-word', node, batchValue)"
        >
          <el-option v-for="item in wordOptions" :key="item" :label="item + '字'" :value="item" />
        </el-select>
        <span v-else class="word-metric">
          <b class="success">{{ summaryWords(node).generated }}</b> / {{ summaryWords(node).target }}
        </span>
      </template>
    </template>

    <template v-else-if="tab === 'add'">
      <el-dropdown trigger="click" @command="add">
        <el-button size="mini" circle icon="el-icon-plus" />
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="sibling">{{ Number(node.level) === 3 ? '新增单段' : '新增同级' }}</el-dropdown-item>
          <el-dropdown-item v-if="Number(node.level) < 3" command="child">新增子级</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </template>

    <template v-else-if="tab === 'delete'">
      <el-button size="mini" circle icon="el-icon-delete" @click="$emit('delete', node)" />
    </template>

    <template v-else-if="tab === 'sort'">
      <el-tooltip content="拖拽排序" placement="top">
        <span class="sort-tip">拖拽排序</span>
      </el-tooltip>
    </template>
  </span>
</template>

<script>
export default {
  name: 'EditNodeActions',
  props: {
    node: { type: Object, required: true },
    tab: { type: String, required: true },
    wordOptions: { type: Array, required: true }
  },
  data() {
    return {
      batchValue: undefined
    }
  },
  methods: {
    add(command) {
      this.$emit('add', this.node, command)
    },
    leafNodes(node) {
      if (!node) return []
      if (Number(node.level) === 3) return [node]
      return (node.children || []).reduce((all, child) => all.concat(this.leafNodes(child)), [])
    },
    hasEditableLeaf(node) {
      return this.leafNodes(node).some(item => Number(item.contentWords || 0) <= 0)
    },
    summaryWords(node) {
      const leaves = this.leafNodes(node)
      const generated = leaves.reduce((sum, item) => sum + Number(item.contentWords || 0), 0)
      const target = leaves.reduce((sum, item) => sum + (Number(item.wordLimit) > 0 ? Number(item.wordLimit) : 300), 0)
      return { generated, target }
    }
  }
}
</script>

<style scoped>
.node-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.word-select {
  width: 118px;
}
.word-metric {
  min-width: 110px;
  text-align: right;
  color: #909399;
  font-size: 14px;
}
.sort-tip {
  color: #909399;
  font-size: 12px;
}
.success {
  color: #2fbd55;
}
</style>

