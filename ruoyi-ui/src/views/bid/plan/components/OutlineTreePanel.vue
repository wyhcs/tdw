<template>
  <section class="outline-panel">
    <div class="panel-toolbar">
      <div class="panel-title">三级大纲</div>
      <div>
        <el-button size="mini" type="primary" icon="el-icon-plus" @click="$emit('add-root')">章</el-button>
        <el-button size="mini" icon="el-icon-refresh" @click="$emit('refresh')" />
      </div>
    </div>

    <el-alert
      v-if="!tree.length"
      class="outline-empty"
      title="暂无大纲，请先生成三级大纲或新增章。"
      type="info"
      show-icon
      :closable="false"
    />

    <el-tree
      v-else
      ref="tree"
      class="outline-tree"
      :data="tree"
      node-key="id"
      default-expand-all
      highlight-current
      :expand-on-click-node="false"
      :props="{ label: 'title', children: 'children' }"
      @node-click="node => $emit('select', node)"
    >
      <div slot-scope="{ node, data }" class="outline-node" :class="{ active: data.id === selectedId }">
        <span class="outline-label">
          <el-tag size="mini" :type="levelTagType(data.level)">{{ levelLabel(data.level) }}</el-tag>
          <span class="outline-title" :title="data.title">{{ data.title }}</span>
        </span>
        <span class="outline-actions">
          <el-tooltip content="新增子级" placement="top">
            <el-button
              type="text"
              icon="el-icon-plus"
              :disabled="data.level >= 3"
              @click.stop="$emit('add-child', data)"
            />
          </el-tooltip>
          <el-tooltip content="同级后插入" placement="top">
            <el-button type="text" icon="el-icon-circle-plus-outline" @click.stop="$emit('add-after', data)" />
          </el-tooltip>
          <el-tooltip content="修改标题" placement="top">
            <el-button type="text" icon="el-icon-edit" @click.stop="$emit('rename', data)" />
          </el-tooltip>
          <el-tooltip content="上移" placement="top">
            <el-button type="text" icon="el-icon-arrow-up" @click.stop="$emit('move-up', data)" />
          </el-tooltip>
          <el-tooltip content="下移" placement="top">
            <el-button type="text" icon="el-icon-arrow-down" @click.stop="$emit('move-down', data)" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button type="text" class="danger-action" icon="el-icon-delete" @click.stop="$emit('remove', data)" />
          </el-tooltip>
        </span>
      </div>
    </el-tree>
  </section>
</template>

<script>
export default {
  name: 'OutlineTreePanel',
  props: {
    tree: {
      type: Array,
      default: () => []
    },
    selectedId: {
      type: [Number, String],
      default: undefined
    }
  },
  methods: {
    levelLabel(level) {
      return level === 1 ? '章' : level === 2 ? '节' : '目'
    },
    levelTagType(level) {
      return level === 1 ? 'primary' : level === 2 ? 'success' : 'info'
    }
  }
}
</script>

<style scoped>
.outline-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.panel-toolbar {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 12px;
}
.panel-title {
  font-weight: 600;
  color: #303133;
}
.outline-empty {
  margin: 12px;
}
.outline-tree {
  flex: 1;
  overflow: auto;
  padding: 8px 6px 12px;
}
.outline-node {
  width: 100%;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.outline-label {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}
.outline-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.outline-actions {
  display: none;
  flex-shrink: 0;
}
.outline-node:hover .outline-actions,
.outline-node.active .outline-actions {
  display: inline-flex;
}
.outline-actions .el-button {
  padding: 0 2px;
}
.danger-action {
  color: #f56c6c;
}
</style>
