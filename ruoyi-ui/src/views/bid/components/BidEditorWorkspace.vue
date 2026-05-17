<template>
  <div class="app-container bid-editor-page">
    <el-page-header @back="$router.push(backPath)" :content="pageTitle" />

    <div class="editor-shell">
      <aside class="outline-column">
        <outline-tree
          :tree="outlineTree"
          :selected-id="selectedOutline && selectedOutline.id"
          @refresh="loadOutline"
          @select="handleSelectOutline"
          @add-root="openAddChapter"
          @add-child="openAddChild"
          @add-after="openAddAfter"
          @rename="openRename"
          @remove="removeOutlineNode"
          @move-up="moveOutlineNode($event, -1)"
          @move-down="moveOutlineNode($event, 1)"
        />
      </aside>

      <main class="content-column" v-loading="contentLoading">
        <content-block-editor
          ref="contentEditor"
          :bid-id="bidId"
          :selected-outline="selectedOutline"
          :blocks="contentBlocks"
          @save-rich="saveRichContentBlock"
        />
      </main>

      <aside class="ai-column">
        <ai-generate-panel
          :selected-outline="selectedOutline"
          :generating-outline="generatingOutline"
          :generating-content="generatingContent"
          :module-type="moduleType"
          :export-name="exportName"
          @generate-outline="openOutlineGenerateDialog"
          @generate-content="handleGenerateContent"
          @insert-table="addTableBlock"
          @insert-diagram="addDiagramBlock"
          @insert-gallery-image="openGalleryImageDialog"
          @export-plan="exportVisible = true"
        />
      </aside>
    </div>

    <el-dialog title="生成三级大纲" :visible.sync="outlineGenerateVisible" width="620px" append-to-body>
      <el-form label-width="112px">
        <el-form-item label="生成模式">
          <el-radio-group v-model="outlineGenerateForm.mode">
            <el-radio-button label="overwrite">覆盖生成</el-radio-button>
            <el-radio-button label="append">追加生成</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模板文件ID">
          <el-input v-model="outlineGenerateForm.templateFileIds" placeholder="多个ID用英文逗号分隔，可为空" />
        </el-form-item>
        <el-form-item label="知识库文件ID">
          <el-input v-model="outlineGenerateForm.knowledgeFileIds" placeholder="多个ID用英文逗号分隔，可为空" />
        </el-form-item>
        <el-form-item label="补充要求">
          <el-input v-model="outlineGenerateForm.requirement" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" :placeholder="outlinePlaceholder" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="outlineGenerateVisible = false">取消</el-button>
        <el-button type="primary" :loading="generatingOutline" @click="submitGenerateOutline">开始生成</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="outlineDialogTitle" :visible.sync="outlineDialogVisible" width="520px" append-to-body>
      <el-form ref="outlineForm" :model="outlineForm" :rules="outlineRules" label-width="92px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="outlineForm.title" maxlength="100" show-word-limit placeholder="请输入大纲标题" />
        </el-form-item>
        <el-form-item v-if="outlineDialogMode !== 'rename'" label="层级">
          <el-tag>{{ outlineLevelText(outlineForm.level) }}</el-tag>
        </el-form-item>
        <el-form-item v-if="outlineDialogMode !== 'rename'" label="字数限制">
          <el-input-number v-model="outlineForm.wordLimit" :min="0" :step="100" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="outlineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOutlineDialog">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="AI 生成建议" :visible.sync="suggestionVisible" width="720px" append-to-body>
      <pre class="suggestion-json">{{ suggestionText }}</pre>
      <div slot="footer">
        <el-button type="primary" @click="suggestionVisible = false">知道了</el-button>
      </div>
    </el-dialog>

    <export-dialog
      :visible.sync="exportVisible"
      :loading="exporting"
      :result="exportResult"
      :export-name="exportName"
      @submit="submitExport"
    />
    <gallery-image-select-dialog
      :visible.sync="galleryImageVisible"
      @select="insertGalleryImage"
    />
  </div>
</template>

<script>
import { exportPlanHtml, getBids } from '@/api/bid/bids'
import { deleteOutline, generateOutline, getOutlineTree, insertOutline, sortOutlines, updateOutlineTitle } from '@/api/bid/outlines'
import { addContent, deleteContent, generateContentBlocks, listContentsByOutline, saveRichContent, sortContents, updateContent } from '@/api/bid/contents'
import OutlineTree from './OutlineTree.vue'
import ContentBlockEditor from './RichContentEditor.vue'
import AiGeneratePanel from './AiGeneratePanel.vue'
import ExportDialog from './ExportDialog.vue'
import GalleryImageSelectDialog from '@/views/bid/tool/components/GalleryImageSelectDialog.vue'

export default {
  name: 'BidEditorWorkspace',
  components: { OutlineTree, ContentBlockEditor, AiGeneratePanel, ExportDialog, GalleryImageSelectDialog },
  props: {
    bidId: { type: [Number, String], required: true },
    backPath: { type: String, default: '/bid/plan' },
    moduleType: { type: String, default: 'plan' },
    exportName: { type: String, default: '方案' },
    titleSuffix: { type: String, default: '三级大纲与内容编辑器' },
    tenderParseReportId: { type: [Number, String], default: undefined },
    focusOutlineId: { type: [Number, String], default: undefined },
    focusContentId: { type: [Number, String], default: undefined }
  },
  data() {
    return {
      bid: {},
      outlineTree: [],
      selectedOutline: null,
      selectedBlock: null,
      contentBlocks: [],
      contentLoading: false,
      generatingOutline: false,
      generatingContent: false,
      exporting: false,
      exportVisible: false,
      exportResult: {},
      outlineGenerateVisible: false,
      outlineGenerateForm: { mode: 'overwrite', templateFileIds: '', knowledgeFileIds: '', requirement: '' },
      outlineDialogVisible: false,
      outlineDialogMode: 'add-root',
      outlineForm: { id: undefined, bidId: undefined, parentId: undefined, afterId: undefined, level: 1, title: '', wordLimit: 500 },
      outlineRules: { title: [{ required: true, message: '请输入大纲标题', trigger: 'blur' }] },
      suggestionVisible: false,
      suggestionText: '',
      galleryImageVisible: false
    }
  },
  computed: {
    pageTitle() {
      return this.bid.title ? this.bid.title + ' - ' + this.titleSuffix : this.titleSuffix
    },
    outlineDialogTitle() {
      const titleMap = { 'add-root': '新增章', 'add-child': '新增子级', 'add-after': '同级后插入', rename: '修改标题' }
      return titleMap[this.outlineDialogMode] || '编辑大纲'
    },
    outlinePlaceholder() {
      return this.moduleType === 'tender'
        ? '例如：结合招标文件解析结果生成响应目录，覆盖评分项。'
        : '例如：围绕评分项组织目录，突出实施路线和质量保障。'
    }
  },
  created() {
    this.loadBid()
    this.loadOutline().then(() => this.applyInitialFocus())
  },
  methods: {
    loadBid() {
      getBids(this.bidId).then(res => { this.bid = res.data || {} })
    },
    loadOutline() {
      return getOutlineTree(this.bidId).then(res => {
        this.outlineTree = res.data || []
        if (this.selectedOutline) {
          const selected = this.findNodeById(this.outlineTree, this.selectedOutline.id)
          this.selectedOutline = selected || null
          if (!selected) {
            this.contentBlocks = []
            this.selectedBlock = null
          }
        }
      })
    },
    applyInitialFocus() {
      if (!this.focusOutlineId) return
      const node = this.findNodeById(this.outlineTree, Number(this.focusOutlineId))
      if (node) this.handleSelectOutline(node)
    },
    handleSelectOutline(node) {
      this.selectedOutline = node
      this.selectedBlock = null
      this.loadContents(node.id)
    },
    loadContents(outlineId) {
      this.contentLoading = true
      return listContentsByOutline(outlineId).then(res => {
        this.contentBlocks = res.data || []
        if (this.focusContentId) {
          this.selectedBlock = this.contentBlocks.find(item => String(item.id) === String(this.focusContentId)) || null
        }
      }).finally(() => { this.contentLoading = false })
    },
    openAddChapter() {
      this.openOutlineDialog('add-root', { bidId: this.bidId, level: 1, title: '', wordLimit: 800 })
    },
    openAddChild(parent) {
      if (parent.level >= 3) {
        this.$modal.msgWarning('内容标题下不能再新增子级')
        return
      }
      this.openOutlineDialog('add-child', { bidId: this.bidId, parentId: parent.id, level: parent.level + 1, title: '', wordLimit: parent.level === 1 ? 600 : 400 })
    },
    openAddAfter(node) {
      this.openOutlineDialog('add-after', { bidId: this.bidId, parentId: node.parentId, afterId: node.id, level: node.level, title: '', wordLimit: node.wordLimit || 500 })
    },
    openRename(node) {
      this.openOutlineDialog('rename', { id: node.id, title: node.title, level: node.level })
    },
    openOutlineDialog(mode, form) {
      this.outlineDialogMode = mode
      this.outlineForm = { ...form }
      this.outlineDialogVisible = true
      this.$nextTick(() => { this.$refs.outlineForm && this.$refs.outlineForm.clearValidate() })
    },
    submitOutlineDialog() {
      this.$refs.outlineForm.validate(valid => {
        if (!valid) return
        const action = this.outlineDialogMode === 'rename'
          ? updateOutlineTitle({ id: this.outlineForm.id, title: this.outlineForm.title })
          : insertOutline(this.outlineForm)
        action.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.outlineDialogVisible = false
          this.loadOutline()
        })
      })
    },
    removeOutlineNode(node) {
      this.$modal.confirm('删除该节点会同时删除子孙节点和关联内容块，确认删除吗？').then(() => deleteOutline(node.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.selectedOutline && this.selectedOutline.id === node.id) {
          this.selectedOutline = null
          this.selectedBlock = null
          this.contentBlocks = []
        }
        this.loadOutline()
      }).catch(() => {})
    },
    moveOutlineNode(node, direction) {
      const siblings = this.findSiblings(node)
      const index = siblings.findIndex(item => item.id === node.id)
      const targetIndex = index + direction
      if (index < 0 || targetIndex < 0 || targetIndex >= siblings.length) {
        this.$modal.msgWarning(direction < 0 ? '已经是同级第一个节点' : '已经是同级最后一个节点')
        return
      }
      const ids = siblings.map(item => item.id)
      const temp = ids[index]
      ids[index] = ids[targetIndex]
      ids[targetIndex] = temp
      sortOutlines({ bidId: this.bidId, parentId: node.parentId, outlineIds: ids }).then(() => {
        this.$modal.msgSuccess('排序已更新')
        this.loadOutline()
      })
    },
    addTextBlock() {
      this.addContentBlock(1, { text: '请输入正文内容', format: { fontSize: 14, bold: false }})
    },
    addTableBlock() {
      if (this.$refs.contentEditor && this.selectedOutline) {
        this.$refs.contentEditor.insertDefaultTable()
        return
      }
      this.addContentBlock(2, { title: '响应要点表', headers: ['序号', '内容', '说明'], rows: [['1', '总体响应', '围绕要求形成响应'], ['2', '实施保障', '明确任务、进度和质量控制']] })
    },
    addDiagramBlock() {
      this.addContentBlock(3, { title: '总体结构图', description: '展示模块关系', imageUrl: '', diagramType: 'architecture', mermaid: 'graph TD; A[输入资料] --> B[方案编写]; B --> C[成果输出];' })
    },
    openGalleryImageDialog() {
      if (!this.selectedOutline) {
        this.$modal.msgWarning('请先选择一个大纲节点')
        return
      }
      this.galleryImageVisible = true
    },
    insertGalleryImage(image) {
      if (!image) return
      if (this.$refs.contentEditor && this.selectedOutline) {
        this.$refs.contentEditor.insertGalleryImage(image)
        return
      }
      this.addContentBlock(3, {
        title: image.imageName,
        description: image.description || '',
        imageUrl: image.imageUrl,
        diagramType: 'gallery',
        mermaid: ''
      })
    },
    addContentBlock(contentType, content) {
      if (!this.selectedOutline) {
        this.$modal.msgWarning('请先选择一个大纲节点')
        return
      }
      addContent({ outlineId: this.selectedOutline.id, contentType, content: JSON.stringify(content), sortOrder: this.contentBlocks.length + 1 }).then(() => {
        this.$modal.msgSuccess('内容块已新增')
        this.loadContents(this.selectedOutline.id)
      })
    },
    updateContentBlock(block) {
      updateContent(block).then(() => {
        this.$modal.msgSuccess('内容块已保存')
        this.loadContents(this.selectedOutline.id)
      })
    },
    saveRichContentBlock(payload, done) {
      let saved = false
      saveRichContent(payload).then(res => {
        saved = true
        if (this.selectedOutline && String(this.selectedOutline.id) === String(payload.outlineId)) {
          this.contentBlocks = res.data ? [res.data] : []
        }
      }).catch(() => {
        this.$modal.msgError('富文本内容保存失败')
      }).finally(() => {
        if (typeof done === 'function') done(saved)
      })
    },
    removeContentBlock(block) {
      this.$modal.confirm('确认删除该内容块吗？').then(() => deleteContent(block.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.loadContents(this.selectedOutline.id)
      }).catch(() => {})
    },
    moveContentBlock(block, direction) {
      const index = this.contentBlocks.findIndex(item => item.id === block.id)
      const targetIndex = index + direction
      if (index < 0 || targetIndex < 0 || targetIndex >= this.contentBlocks.length) {
        this.$modal.msgWarning(direction < 0 ? '已经是第一个内容块' : '已经是最后一个内容块')
        return
      }
      const ids = this.contentBlocks.map(item => item.id)
      const temp = ids[index]
      ids[index] = ids[targetIndex]
      ids[targetIndex] = temp
      sortContents({ outlineId: this.selectedOutline.id, contentIds: ids }).then(() => {
        this.$modal.msgSuccess('内容块排序已更新')
        this.loadContents(this.selectedOutline.id)
      })
    },
    openOutlineGenerateDialog() {
      this.outlineGenerateVisible = true
    },
    submitGenerateOutline() {
      this.generatingOutline = true
      generateOutline({
        bidId: this.bidId,
        mode: this.outlineGenerateForm.mode,
        templateFileIds: this.parseIdList(this.outlineGenerateForm.templateFileIds),
        knowledgeFileIds: this.parseIdList(this.outlineGenerateForm.knowledgeFileIds),
        tenderParseReportId: this.tenderParseReportId,
        requirement: this.outlineGenerateForm.requirement
      }).then(() => {
        this.$modal.msgSuccess('三级大纲生成完成')
        this.outlineGenerateVisible = false
        this.selectedOutline = null
        this.selectedBlock = null
        this.contentBlocks = []
        this.loadOutline()
      }).finally(() => { this.generatingOutline = false })
    },
    handleGenerateContent(options) {
      if (options.scope !== 'full' && !this.selectedOutline) {
        this.$modal.msgWarning('请先选择要生成的章、节或内容标题')
        return
      }
      this.generatingContent = true
      generateContentBlocks({
        bidId: this.bidId,
        outlineId: options.scope === 'full' ? undefined : this.selectedOutline.id,
        scope: options.scope,
        mode: options.mode,
        requirement: options.requirement,
        writingStyle: options.writingStyle || 'general',
        includeTable: options.includeTable,
        includeDiagram: options.includeDiagram,
        tenderParseReportId: this.tenderParseReportId
      }).then(res => {
        if (options.mode === 'keep') {
          this.suggestionText = JSON.stringify(res.data || {}, null, 2)
          this.suggestionVisible = true
        } else {
          this.$modal.msgSuccess('内容生成完成')
          if (this.selectedOutline) this.loadContents(this.selectedOutline.id)
        }
      }).finally(() => { this.generatingContent = false })
    },
    submitExport(form) {
      this.exporting = true
      exportPlanHtml({ bidId: this.bidId, fileFormat: form.fileFormat, includeEmptyOutline: form.includeEmptyOutline }).then(res => {
        this.exportResult = res.data || {}
        this.exportVisible = false
      }).finally(() => { this.exporting = false })
    },
    outlineLevelText(level) {
      return level === 1 ? '章' : level === 2 ? '节' : '内容标题'
    },
    parseIdList(value) {
      if (!value) return []
      return value.split(',').map(item => item.trim()).filter(Boolean).map(item => Number(item)).filter(item => !isNaN(item))
    },
    findNodeById(nodes, id) {
      for (const node of nodes) {
        if (String(node.id) === String(id)) return node
        const child = this.findNodeById(node.children || [], id)
        if (child) return child
      }
      return null
    },
    findSiblings(node) {
      if (!node.parentId) return this.outlineTree
      const parent = this.findNodeById(this.outlineTree, node.parentId)
      return parent ? (parent.children || []) : []
    }
  }
}
</script>

<style scoped>
.bid-editor-page { min-height: calc(100vh - 84px); display: flex; flex-direction: column; }
.editor-shell { flex: 1; min-height: 620px; margin-top: 16px; display: grid; grid-template-columns: minmax(280px, 360px) minmax(460px, 1fr) minmax(260px, 300px); border: 1px solid #ebeef5; border-radius: 6px; background: #fff; overflow: hidden; }
.outline-column, .content-column, .ai-column { min-width: 0; min-height: 0; }
.outline-column, .content-column { border-right: 1px solid #ebeef5; }
.suggestion-json { max-height: 520px; overflow: auto; margin: 0; padding: 12px; border-radius: 4px; background: #f5f7fa; color: #303133; white-space: pre-wrap; }
@media (max-width: 1200px) {
  .editor-shell { grid-template-columns: 300px minmax(420px, 1fr); }
  .ai-column { grid-column: 1 / 3; border-top: 1px solid #ebeef5; }
}
</style>
