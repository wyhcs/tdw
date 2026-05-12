<template>
  <section class="content-editor">
    <div class="content-toolbar">
      <div class="content-title">
        <span>{{ selectedOutline ? selectedOutline.title : '内容块编辑区' }}</span>
        <el-tag v-if="selectedOutline" size="mini" effect="plain">{{ levelText(selectedOutline.level) }}</el-tag>
      </div>
      <div>
        <el-button size="mini" icon="el-icon-document-add" :disabled="!selectedOutline" @click="$emit('add-text')">文本</el-button>
        <el-button size="mini" icon="el-icon-s-grid" :disabled="!selectedOutline" @click="$emit('add-table')">表格</el-button>
        <el-button size="mini" icon="el-icon-picture-outline" :disabled="!selectedOutline" @click="$emit('add-diagram')">结构图</el-button>
      </div>
    </div>
    <div v-if="!selectedOutline" class="empty-state">
      <i class="el-icon-document" />
      <p>请在左侧选择章、节或内容标题。</p>
    </div>
    <div v-else-if="!blocks.length" class="empty-state">
      <i class="el-icon-edit-outline" />
      <p>当前节点暂无内容块，可新增或使用右侧 AI 生成。</p>
    </div>
    <div v-else class="block-list">
      <el-card
        v-for="(block, index) in blocks"
        :key="block.id || index"
        class="content-block"
        shadow="never"
        :class="{ selected: block.id === selectedBlockId }"
        @click.native="$emit('select-block', block)"
      >
        <div slot="header" class="block-header">
          <div class="block-name">
            <el-tag size="mini" :type="blockTag(block.contentType)">{{ blockTypeText(block.contentType) }}</el-tag>
            <span>内容块 {{ index + 1 }}</span>
          </div>
          <div class="block-actions">
            <el-button size="mini" icon="el-icon-arrow-up" :disabled="index === 0" @click.stop="$emit('move-up', block)" />
            <el-button size="mini" icon="el-icon-arrow-down" :disabled="index === blocks.length - 1" @click.stop="$emit('move-down', block)" />
            <el-button size="mini" icon="el-icon-edit" @click.stop="openEdit(block)">编辑</el-button>
            <el-button size="mini" type="danger" icon="el-icon-delete" @click.stop="$emit('remove', block)">删除</el-button>
          </div>
        </div>
        <div v-if="block.contentType === 1" class="text-block">{{ parseContent(block).text || block.content }}</div>
        <div v-else-if="block.contentType === 2">
          <div class="block-subtitle">{{ parseContent(block).title || parseContent(block).name || '表格内容' }}</div>
          <el-table :data="tableRows(parseContent(block))" size="mini" border>
            <el-table-column v-for="(header, headerIndex) in tableHeaders(parseContent(block))" :key="headerIndex" :prop="'col' + headerIndex" :label="header" min-width="120" />
          </el-table>
        </div>
        <div v-else class="diagram-block">
          <div class="block-subtitle">{{ parseContent(block).title || parseContent(block).name || '结构图' }}</div>
          <p>{{ parseContent(block).description }}</p>
          <pre v-if="parseContent(block).mermaid">{{ parseContent(block).mermaid }}</pre>
          <el-image v-if="parseContent(block).imageUrl || parseContent(block).url" :src="parseContent(block).imageUrl || parseContent(block).url" fit="contain" />
        </div>
      </el-card>
    </div>

    <el-dialog :title="editForm.contentType === 1 ? '编辑文本块' : '编辑内容块 JSON'" :visible.sync="editVisible" width="680px" append-to-body>
      <el-input v-if="editForm.contentType === 1" v-model="editForm.text" class="json-editor" type="textarea" :autosize="{ minRows: 10, maxRows: 20 }" />
      <el-input v-else v-model="editForm.content" class="json-editor" type="textarea" :autosize="{ minRows: 12, maxRows: 22 }" />
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
export default {
  name: 'ContentBlockEditor',
  props: {
    selectedOutline: { type: Object, default: null },
    blocks: { type: Array, default: () => [] },
    selectedBlockId: { type: [Number, String], default: undefined }
  },
  data() {
    return { editVisible: false, editForm: {} }
  },
  methods: {
    levelText(level) {
      return level === 1 ? '章' : level === 2 ? '节' : '内容标题'
    },
    blockTypeText(type) {
      return type === 1 ? '文本' : type === 2 ? '表格' : '图示'
    },
    blockTag(type) {
      return type === 1 ? 'primary' : type === 2 ? 'success' : 'warning'
    },
    parseContent(block) {
      if (!block || !block.content) return {}
      if (typeof block.content === 'object') return block.content
      try {
        return JSON.parse(block.content)
      } catch (e) {
        return { text: block.content }
      }
    },
    tableHeaders(content) {
      return Array.isArray(content.headers) && content.headers.length ? content.headers : ['内容']
    },
    tableRows(content) {
      const headers = this.tableHeaders(content)
      const rows = Array.isArray(content.rows) ? content.rows : []
      return rows.map(row => {
        const item = {}
        headers.forEach((header, index) => {
          item['col' + index] = Array.isArray(row) ? row[index] : row[header]
        })
        return item
      })
    },
    openEdit(block) {
      const parsed = this.parseContent(block)
      this.editForm = {
        ...block,
        text: parsed.text || '',
        format: parsed.format || { fontSize: 14, bold: false },
        content: this.stringifyContent(block.content)
      }
      this.editVisible = true
    },
    stringifyContent(content) {
      if (!content) return '{}'
      if (typeof content === 'object') return JSON.stringify(content, null, 2)
      try {
        return JSON.stringify(JSON.parse(content), null, 2)
      } catch (e) {
        return content
      }
    },
    submitEdit() {
      if (this.editForm.contentType === 1) {
        this.$emit('update', {
          id: this.editForm.id,
          outlineId: this.editForm.outlineId,
          contentType: this.editForm.contentType,
          sortOrder: this.editForm.sortOrder,
          content: JSON.stringify({ text: this.editForm.text, format: this.editForm.format || { fontSize: 14, bold: false }})
        })
        this.editVisible = false
        return
      }
      try {
        const parsed = JSON.parse(this.editForm.content)
        this.$emit('update', {
          id: this.editForm.id,
          outlineId: this.editForm.outlineId,
          contentType: this.editForm.contentType,
          sortOrder: this.editForm.sortOrder,
          content: JSON.stringify(parsed)
        })
        this.editVisible = false
      } catch (e) {
        this.$modal.msgError('JSON 格式不正确，请检查后再保存')
      }
    }
  }
}
</script>

<style scoped>
.content-editor { height: 100%; display: flex; flex-direction: column; }
.content-toolbar { min-height: 48px; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 0 16px; border-bottom: 1px solid #ebeef5; }
.content-title { min-width: 0; display: flex; align-items: center; gap: 8px; font-weight: 600; color: #303133; }
.content-title span:first-child { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; }
.empty-state i { font-size: 36px; margin-bottom: 12px; }
.block-list { flex: 1; overflow: auto; padding: 14px 16px 24px; }
.content-block { margin-bottom: 12px; border-radius: 6px; }
.content-block.selected { border-color: #409eff; }
.block-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.block-name { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.block-actions { display: flex; align-items: center; gap: 6px; }
.text-block { white-space: pre-wrap; line-height: 1.8; color: #303133; }
.block-subtitle { margin-bottom: 8px; font-weight: 600; color: #303133; }
.diagram-block p { margin: 0 0 10px; color: #606266; }
.diagram-block pre { margin: 0; padding: 10px; border-radius: 4px; background: #f5f7fa; color: #303133; white-space: pre-wrap; }
.json-editor { margin-top: 12px; font-family: Consolas, Monaco, monospace; }
</style>
