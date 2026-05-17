<template>
  <div class="plan-reader-page">
    <aside class="reader-sidebar">
      <div class="side-title">≡ 我的方案</div>
      <el-input
        v-model="planQuery"
        size="small"
        clearable
        prefix-icon="el-icon-search"
        placeholder="搜索方案"
        @keyup.enter.native="loadPlans"
        @clear="loadPlans"
      />
      <el-button class="new-button" type="primary" @click="$router.push('/bid/plan/create')">新建方案</el-button>

      <div class="plan-list">
        <button
          v-for="item in planList"
          :key="item.id"
          class="plan-card"
          :class="{ active: String(item.id) === String(bidId) }"
          @click="switchPlan(item)"
        >
          <span class="plan-name">{{ item.title }}</span>
          <span class="plan-meta">{{ item.category || '普通' }} · {{ parseTime(item.createdTime, '{y}-{m}-{d}') }}</span>
        </button>
      </div>
    </aside>

    <section class="outline-area">
      <div class="project-head">
        <div>
          <h2>{{ bid.title || 'AI方案' }}</h2>
          <p>
            目标字数：<b class="danger">{{ targetWords }}</b> 字
            <span>生成字数：<b class="success">{{ generatedWords }}</b> 字</span>
          </p>
          <p>
            预估页数：<b class="danger">{{ estimatePages }}</b> 页
            <span>当前页数：<b class="success">{{ currentPages }}</b></span>
          </p>
        </div>
        <el-button size="mini" icon="el-icon-edit" @click="openRenameProject">编辑</el-button>
      </div>

      <div class="outline-toolbar">
        <span>大纲</span>
        <div>
          <el-button size="mini" icon="el-icon-plus" @click="openAddRoot" />
          <el-button size="mini" icon="el-icon-refresh" @click="loadOutline" />
        </div>
      </div>

      <div v-if="!outlineRows.length" class="outline-empty">
        <i class="el-icon-document" />
        <p>暂无目录，请先在新建页生成目录。</p>
      </div>
      <div v-else class="outline-list">
        <button
          v-for="row in outlineRows"
          :key="row.id"
          class="outline-node"
          :class="['level-' + row.level, { active: selectedOutline && row.id === selectedOutline.id }]"
          @click="selectOutline(row)"
        >
          <span class="node-title">
            <i v-if="row.level === 1" class="el-icon-caret-right" />
            <i v-else class="dot" />
            {{ row.title }}
          </span>
          <span class="node-words" v-if="row.level === 3">{{ outlineWordCount(row.id) }} / {{ row.wordLimit || 300 }}</span>
          <span class="node-actions">
            <i class="el-icon-plus" @click.stop="openAddChild(row)" />
            <i class="el-icon-edit" @click.stop="openRenameOutline(row)" />
            <i class="el-icon-arrow-up" @click.stop="moveOutline(row, -1)" />
            <i class="el-icon-arrow-down" @click.stop="moveOutline(row, 1)" />
            <i class="el-icon-delete" @click.stop="removeOutline(row)" />
          </span>
        </button>
      </div>

      <div class="bottom-actions">
        <el-button type="primary" class="export-button" icon="el-icon-download" :loading="exporting" @click="exportFull">导出全文</el-button>
        <el-upload
          action="#"
          :auto-upload="false"
          :limit="1"
          :show-file-list="false"
          :on-change="handlePromptChange"
        >
          <el-button icon="el-icon-upload2" />
        </el-upload>
        <el-button icon="el-icon-refresh" :loading="generatingContent" @click="generateFull" />
      </div>
    </section>

    <main class="document-area" v-loading="contentLoading">
      <template v-if="currentSection">
        <header class="doc-section-bar">
          <div class="section-title-wrap">
            <span class="section-rail"></span>
            <h1>{{ currentSection.title }}</h1>
          </div>
          <div class="section-actions">
            <el-button size="mini" type="primary" plain :loading="generatingContent" @click="regenerateNode(currentSection)">重编本节</el-button>
            <el-button size="mini" type="primary" plain :loading="exporting" @click="exportSection">导出本节</el-button>
          </div>
        </header>

        <section class="doc-paragraph-bar">
          <div class="paragraph-title">{{ currentParagraph ? currentParagraph.title : '请选择段级标题' }}</div>
          <el-button size="mini" type="primary" plain :disabled="!currentParagraph" :loading="generatingContent" @click="regenerateNode(currentParagraph)">重编本段</el-button>
          <div class="editor-toolbox">
            <el-tooltip content="插入文本" placement="bottom">
              <el-button size="mini" icon="el-icon-edit-outline" :disabled="!currentParagraph" @click="runEditorTool('text')" />
            </el-tooltip>
            <el-tooltip content="插入图片" placement="bottom">
              <el-button size="mini" icon="el-icon-picture-outline" :disabled="!currentParagraph" @click="runEditorTool('image')" />
            </el-tooltip>
            <el-tooltip content="插入表格" placement="bottom">
              <el-button size="mini" icon="el-icon-s-grid" :disabled="!currentParagraph" @click="runEditorTool('table')" />
            </el-tooltip>
            <el-tooltip content="清除图表" placement="bottom">
              <el-button size="mini" icon="el-icon-delete" :disabled="!currentParagraph" @click="runEditorTool('clear')" />
            </el-tooltip>
            <el-tooltip content="查看知识库引用" placement="bottom">
              <el-button size="mini" icon="el-icon-collection" :disabled="!currentParagraph" @click="runEditorTool('refs')" />
            </el-tooltip>
            <el-tooltip content="切换全屏" placement="bottom">
              <el-button size="mini" icon="el-icon-full-screen" :disabled="!currentParagraph" @click="runEditorTool('fullscreen')" />
            </el-tooltip>
          </div>
        </section>

        <section class="editor-paper">
          <div class="heading-gutter">
            <div class="heading-row h4-row">
              <span>H4</span>
              <strong>{{ currentSection.title }}</strong>
            </div>
            <div class="heading-row h5-row">
              <span>H5</span>
              <strong>{{ currentParagraph ? currentParagraph.title : '段级标题' }}</strong>
            </div>
          </div>
          <rich-content-editor
            ref="richEditor"
            :bid-id="bidId"
            :selected-outline="currentParagraph"
            :blocks="currentParagraphBlocks"
            :saving-external="contentSaving"
            :show-header="false"
            :show-toolbar="false"
            @save-rich="saveRichContentBlock"
          />
        </section>
      </template>
      <div v-else class="doc-empty">
        <i class="el-icon-document" />
        <p>请选择左侧节级标题查看正文</p>
      </div>
    </main>

    <el-dialog :title="outlineDialogTitle" :visible.sync="outlineDialogVisible" width="520px" append-to-body>
      <el-form ref="outlineForm" :model="outlineForm" :rules="outlineRules" label-width="86px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="outlineForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item v-if="outlineDialogMode !== 'rename'" label="层级">
          <el-tag>{{ levelText(outlineForm.level) }}</el-tag>
        </el-form-item>
        <el-form-item v-if="outlineDialogMode !== 'rename'" label="字数">
          <el-input-number v-model="outlineForm.wordLimit" :min="0" :step="100" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="outlineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOutlineDialog">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑正文" :visible.sync="contentDialogVisible" width="760px" append-to-body>
      <el-input v-model="contentEditText" type="textarea" :autosize="{ minRows: 12, maxRows: 24 }" />
      <div slot="footer">
        <el-button @click="contentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveContentEdit">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="导出完成" :visible.sync="exportDialogVisible" width="520px" append-to-body>
      <el-result icon="success" title="AI方案已导出">
        <template slot="subTitle">
          <el-link v-if="exportResult.fileUrl" type="primary" :href="resourceUrl(exportResult.fileUrl)" target="_blank">{{ exportResult.downloadName || '打开导出文件' }}</el-link>
        </template>
      </el-result>
      <div slot="footer">
        <el-button type="primary" @click="exportDialogVisible = false">知道了</el-button>
      </div>
    </el-dialog>

    <el-dialog title="修改方案名称" :visible.sync="projectDialogVisible" width="520px" append-to-body>
      <el-input v-model="projectTitle" maxlength="100" show-word-limit />
      <div slot="footer">
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProjectTitle">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addContent, generateContentBlocks, listContentsByOutlines, saveRichContent, updateContent } from '@/api/bid/contents'
import { exportPlanHtml, getBids, getLatestPlanReport, listBids, parsePlanMaterial, uploadPlanMaterial, updateBids } from '@/api/bid/bids'
import { deleteOutline, getOutlineTree, insertOutline, sortOutlines, updateOutlineTitle } from '@/api/bid/outlines'
import RichContentEditor from '@/views/bid/components/RichContentEditor.vue'

export default {
  name: 'BidPlanEditor',
  components: { RichContentEditor },
  data() {
    return {
      bidId: undefined,
      bid: {},
      planQuery: '',
      planList: [],
      outlineTree: [],
      selectedOutline: null,
      currentRootId: undefined,
      contentMap: {},
      loadedRootMap: {},
      contentLoading: false,
      contentSaving: false,
      generatingContent: false,
      exporting: false,
      exportDialogVisible: false,
      exportResult: {},
      tenderReportId: undefined,
      outlineDialogVisible: false,
      outlineDialogMode: 'add-root',
      outlineForm: { title: '', wordLimit: 300 },
      outlineRules: { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] },
      contentDialogVisible: false,
      contentEditBlock: null,
      contentEditText: '',
      projectDialogVisible: false,
      projectTitle: ''
    }
  },
  computed: {
    outlineRows() {
      return this.flattenOutlines(this.outlineTree)
    },
    currentRoot() {
      if (!this.currentRootId && this.outlineTree.length) {
        return this.outlineTree[0]
      }
      return this.findNodeById(this.outlineTree, this.currentRootId)
    },
    currentSection() {
      if (!this.selectedOutline || !this.currentRoot) {
        return this.firstSection(this.currentRoot)
      }
      const selected = this.findNodeById(this.outlineTree, this.selectedOutline.id) || this.selectedOutline
      if (Number(selected.level) === 2) return selected
      if (Number(selected.level) === 3) {
        return this.findNodeById(this.outlineTree, selected.parentId) || this.firstSection(this.currentRoot)
      }
      return this.firstSection(selected) || this.firstSection(this.currentRoot)
    },
    currentParagraph() {
      if (!this.currentSection) return null
      if (this.selectedOutline && Number(this.selectedOutline.level) === 3) {
        return this.findNodeById(this.outlineTree, this.selectedOutline.id) || this.selectedOutline
      }
      return this.firstContentNode(this.currentSection)
    },
    targetWords() {
      const match = String(this.bid.note || '').match(/目标字数：(\d+)/)
      return match ? match[1] : '11,800'
    },
    generatedWords() {
      let count = 0
      Object.keys(this.contentMap).forEach(key => {
        ;(this.contentMap[key] || []).forEach(block => {
          if (block.contentType === 1) {
            count += (this.parseContent(block).text || '').length
          }
        })
      })
      return count || 0
    },
    estimatePages() {
      const value = Number(String(this.targetWords).replace(/,/g, '')) || 11800
      return Math.max(1, Math.ceil(value / 560))
    },
    currentPages() {
      return Math.max(1, Math.ceil((this.generatedWords || 1) / 560))
    },
    currentParagraphBlocks() {
      if (!this.currentParagraph) return []
      return this.contentMap[this.currentParagraph.id] || []
    },
    outlineDialogTitle() {
      const map = { 'add-root': '新增章', 'add-child': '新增子级', 'add-after': '同级后插入', rename: '修改标题' }
      return map[this.outlineDialogMode] || '编辑大纲'
    }
  },
  created() {
    this.bidId = this.$route.query.bidId || this.$route.params.bidId
    this.tenderReportId = this.$route.query.reportId
    this.loadPlans()
    this.loadBid()
    this.loadOutline().then(() => {
      const focusId = this.$route.query.outlineId
      const row = focusId ? this.findRow(focusId) : this.defaultEditorRow()
      if (row) this.selectOutline(row)
    })
    this.loadLatestReport()
  },
  methods: {
    loadPlans() {
      return listBids({ pageNum: 1, pageSize: 20, title: this.planQuery || undefined }).then(res => {
        this.planList = res.rows || []
      })
    },
    loadBid() {
      if (!this.bidId) return
      return getBids(this.bidId).then(res => {
        this.bid = res.data || {}
      })
    },
    loadLatestReport() {
      if (!this.bidId || this.tenderReportId) return
      getLatestPlanReport(this.bidId).then(res => {
        const report = res.data || {}
        this.tenderReportId = report.id
      }).catch(() => {})
    },
    loadOutline() {
      if (!this.bidId) return Promise.resolve()
      return getOutlineTree(this.bidId).then(res => {
        this.outlineTree = res.data || []
        if (!this.currentRootId && this.outlineTree.length) {
          this.currentRootId = this.outlineTree[0].id
        }
      })
    },
    defaultEditorRow() {
      return this.outlineRows.find(item => Number(item.level) === 2) ||
        this.outlineRows.find(item => Number(item.level) === 3) ||
        this.outlineRows[0]
    },
    switchPlan(item) {
      this.$router.replace({ path: '/bid/plan/editor', query: { bidId: item.id } })
      this.bidId = item.id
      this.bid = item
      this.selectedOutline = null
      this.currentRootId = undefined
      this.contentMap = {}
      this.loadedRootMap = {}
      this.tenderReportId = undefined
      this.loadOutline().then(() => {
        const row = this.defaultEditorRow()
        if (row) this.selectOutline(row)
      })
      this.loadLatestReport()
    },
    selectOutline(row) {
      this.selectedOutline = row
      const nextRootId = row.rootId || row.id
      const rootChanged = String(this.currentRootId || '') !== String(nextRootId || '')
      this.currentRootId = nextRootId
      if (rootChanged || !this.loadedRootMap[nextRootId]) {
        this.loadChapterContent()
      }
    },
    firstSection(node) {
      if (!node) return null
      if (Number(node.level) === 2) return node
      const children = node.children || []
      const direct = children.find(item => Number(item.level) === 2)
      if (direct) return direct
      for (const child of children) {
        const section = this.firstSection(child)
        if (section) return section
      }
      return null
    },
    firstContentNode(node) {
      if (!node) return null
      if (Number(node.level) === 3) return node
      const children = node.children || []
      for (const child of children) {
        const leaf = this.firstContentNode(child)
        if (leaf) return leaf
      }
      return null
    },
    loadChapterContent(force) {
      if (!this.currentRoot) return Promise.resolve()
      const rootId = this.currentRoot.id
      if (!force && this.loadedRootMap[rootId]) {
        return Promise.resolve()
      }
      this.contentLoading = true
      const leaves = this.flattenLeaves([this.currentRoot])
      const leafIds = leaves.map(leaf => leaf.id)
      return listContentsByOutlines(leafIds).then(res => {
        const grouped = this.groupContentsByOutline(res.data || [])
        leaves.forEach(leaf => {
          this.$set(this.contentMap, leaf.id, grouped[leaf.id] || [])
        })
        this.$set(this.loadedRootMap, rootId, true)
      }).finally(() => {
        this.contentLoading = false
      })
    },
    generateFull() {
      if (!this.bidId) return
      this.generatingContent = true
      generateContentBlocks({
        bidId: this.bidId,
        scope: 'full',
        mode: 'overwrite',
        requirement: '按当前三级目录重新生成全文内容，保持服务方案专业表达。',
        writingStyle: 'general',
        includeTable: false,
        includeDiagram: false,
        tenderParseReportId: this.tenderReportId
      }).then(() => {
        this.$modal.msgSuccess('全文生成完成')
        this.contentMap = {}
        this.loadedRootMap = {}
        return this.loadChapterContent(true)
      }).finally(() => {
        this.generatingContent = false
      })
    },
    regenerateNode(node) {
      if (!node) return
      this.generatingContent = true
      generateContentBlocks({
        bidId: this.bidId,
        outlineId: node.id,
        scope: 'selected',
        mode: 'overwrite',
        requirement: '请重编当前范围内容，突出技术响应、实施可行性和质量保障。',
        writingStyle: 'general',
        includeTable: false,
        includeDiagram: false,
        tenderParseReportId: this.tenderReportId
      }).then(() => {
        this.$modal.msgSuccess('重编完成')
        return this.loadChapterContent(true)
      }).finally(() => {
        this.generatingContent = false
      })
    },
    runEditorTool(action) {
      const editor = this.$refs.richEditor
      if (!this.currentParagraph || !editor) {
        this.$modal.msgWarning('请先选择段级标题')
        return
      }
      const handlers = {
        text: () => editor.openTextInsertDialog(),
        image: () => editor.openGalleryDialog(),
        table: () => editor.insertDefaultTable(),
        clear: () => editor.clearCharts(),
        refs: () => editor.openRefs(),
        fullscreen: () => editor.toggleFullscreen()
      }
      if (handlers[action]) handlers[action]()
    },
    handlePromptChange(file) {
      if (!this.validatePromptFile(file)) return
      uploadPlanMaterial(this.bidId, file.raw, 'content_prompt').then(res => {
        const material = res.data || {}
        if (!material.id) return undefined
        return parsePlanMaterial(material.id)
      }).then(res => {
        const report = (res && res.data) || {}
        this.tenderReportId = report.id || this.tenderReportId
        this.$modal.msgSuccess('提示词解析完成，开始生成全文')
        this.generateFull()
      })
    },
    validatePromptFile(file) {
      const name = file.name || ''
      const ext = name.split('.').pop().toLowerCase()
      if (['doc', 'docx'].indexOf(ext) === -1) {
        this.$modal.msgWarning('第二阶段仅支持 doc/docx 文件')
        return false
      }
      if (file.raw && file.raw.size > 50 * 1024 * 1024) {
        this.$modal.msgWarning('文件大小不能超过 50MB')
        return false
      }
      return true
    },
    addTable(item) {
      this.addBlock(item, 2, {
        title: item.title + '响应表',
        headers: ['序号', '响应内容', '说明'],
        rows: [['1', item.title, '已按招标要求形成响应']]
      })
    },
    addDiagram(item) {
      this.addBlock(item, 3, {
        title: item.title + '流程图',
        description: '展示当前段落相关流程关系',
        mermaid: 'graph TD; A[输入资料] --> B[' + item.title + ']; B --> C[输出成果];'
      })
    },
    addBlock(item, contentType, content) {
      addContent({
        outlineId: item.id,
        contentType,
        content: JSON.stringify(content),
        sortOrder: ((this.contentMap[item.id] || []).length + 1)
      }).then(() => {
        this.$modal.msgSuccess('内容已插入')
        this.loadChapterContent(true)
      })
    },
    openEditContent(block) {
      this.contentEditBlock = block
      this.contentEditText = this.parseContent(block).text || ''
      this.contentDialogVisible = true
    },
    saveContentEdit() {
      if (!this.contentEditBlock) return
      const block = this.contentEditBlock
      updateContent({
        id: block.id,
        outlineId: block.outlineId,
        contentType: block.contentType,
        sortOrder: block.sortOrder,
        content: JSON.stringify({ text: this.contentEditText, format: { fontSize: 14, bold: false }})
      }).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.contentDialogVisible = false
        this.loadChapterContent(true)
      })
    },
    saveRichContentBlock(payload, done) {
      this.contentSaving = true
      let saved = false
      saveRichContent(payload).then(res => {
        saved = true
        const content = res.data
        if (content && payload.outlineId) {
          this.$set(this.contentMap, payload.outlineId, [content])
        }
      }).catch(() => {
        this.$modal.msgError('富文本内容保存失败')
      }).finally(() => {
        this.contentSaving = false
        if (typeof done === 'function') done(saved)
      })
    },
    exportFull() {
      if (!this.bidId) return
      this.exporting = true
      exportPlanHtml({ bidId: this.bidId, fileFormat: 'html', includeEmptyOutline: true }).then(res => {
        this.exportResult = res.data || {}
        this.exportDialogVisible = true
      }).finally(() => {
        this.exporting = false
      })
    },
    exportSection() {
      if (!this.bidId || !this.currentSection) return
      this.exporting = true
      exportPlanHtml({
        bidId: this.bidId,
        outlineId: this.currentSection.id,
        fileFormat: 'html',
        includeEmptyOutline: true
      }).then(res => {
        this.exportResult = res.data || {}
        this.exportDialogVisible = true
      }).finally(() => {
        this.exporting = false
      })
    },
    openRenameProject() {
      this.projectTitle = this.bid.title || ''
      this.projectDialogVisible = true
    },
    saveProjectTitle() {
      updateBids({ ...this.bid, title: this.projectTitle }).then(() => {
        this.$modal.msgSuccess('方案名称已更新')
        this.projectDialogVisible = false
        this.loadBid()
        this.loadPlans()
      })
    },
    openAddRoot() {
      this.openOutlineDialog('add-root', { bidId: this.bidId, level: 1, title: '', wordLimit: 800 })
    },
    openAddChild(row) {
      if (row.level >= 3) {
        this.$modal.msgWarning('内容标题下不能继续新增子级')
        return
      }
      this.openOutlineDialog('add-child', { bidId: this.bidId, parentId: row.id, level: row.level + 1, title: '', wordLimit: row.level === 1 ? 500 : 300 })
    },
    openRenameOutline(row) {
      this.openOutlineDialog('rename', { id: row.id, title: row.title, level: row.level })
    },
    openOutlineDialog(mode, form) {
      this.outlineDialogMode = mode
      this.outlineForm = { ...form }
      this.outlineDialogVisible = true
      this.$nextTick(() => this.$refs.outlineForm && this.$refs.outlineForm.clearValidate())
    },
    submitOutlineDialog() {
      this.$refs.outlineForm.validate(valid => {
        if (!valid) return
        const action = this.outlineDialogMode === 'rename'
          ? updateOutlineTitle({ id: this.outlineForm.id, title: this.outlineForm.title })
          : insertOutline(this.outlineForm)
        action.then(() => {
          this.$modal.msgSuccess('大纲已保存')
          this.outlineDialogVisible = false
          this.loadOutline()
        })
      })
    },
    removeOutline(row) {
      this.$modal.confirm('删除该节点会同时删除子节点和正文，确认删除吗？').then(() => deleteOutline(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        return this.loadOutline()
      }).catch(() => {})
    },
    moveOutline(row, direction) {
      const siblings = this.findSiblings(row)
      const index = siblings.findIndex(item => item.id === row.id)
      const targetIndex = index + direction
      if (index < 0 || targetIndex < 0 || targetIndex >= siblings.length) {
        this.$modal.msgWarning(direction < 0 ? '已经是同级第一个节点' : '已经是同级最后一个节点')
        return
      }
      const ids = siblings.map(item => item.id)
      const temp = ids[index]
      ids[index] = ids[targetIndex]
      ids[targetIndex] = temp
      sortOutlines({ bidId: this.bidId, parentId: row.parentId, outlineIds: ids }).then(() => this.loadOutline())
    },
    findSiblings(row) {
      if (!row.parentId) return this.outlineTree
      const parent = this.findNodeById(this.outlineTree, row.parentId)
      return parent ? (parent.children || []) : []
    },
    groupContentsByOutline(contents) {
      const grouped = {}
      ;(contents || []).forEach(content => {
        const key = content.outlineId
        if (!grouped[key]) grouped[key] = []
        grouped[key].push(content)
      })
      return grouped
    },
    outlineWordCount(outlineId) {
      let count = 0
      ;(this.contentMap[outlineId] || []).forEach(block => {
        if (block.contentType === 1) count += (this.parseContent(block).text || '').length
      })
      return count
    },
    flattenOutlines(nodes, rows = [], rootId = null) {
      ;(nodes || []).forEach(node => {
        const row = { ...node, rootId: rootId || node.id }
        rows.push(row)
        this.flattenOutlines(node.children || [], rows, rootId || node.id)
      })
      return rows
    },
    flattenLeaves(nodes, rows = []) {
      ;(nodes || []).forEach(node => {
        if (node.level === 3 || !node.children || !node.children.length) {
          rows.push(node)
        } else {
          this.flattenLeaves(node.children, rows)
        }
      })
      return rows
    },
    findNodeById(nodes, id) {
      for (const node of nodes || []) {
        if (String(node.id) === String(id)) return node
        const child = this.findNodeById(node.children || [], id)
        if (child) return child
      }
      return null
    },
    findRow(id) {
      return this.outlineRows.find(item => String(item.id) === String(id))
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
    textParagraphs(block) {
      const text = this.parseContent(block).text || ''
      return text.split(/\n+/).map(item => item.trim()).filter(Boolean)
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
    levelText(level) {
      return Number(level) === 1 ? '章' : Number(level) === 2 ? '节' : '内容标题'
    },
    resourceUrl(url) {
      if (!url) return ''
      if (/^https?:\/\//.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    }
  }
}
</script>

<style scoped>
.plan-reader-page {
  min-height: calc(100vh - 84px);
  display: grid;
  grid-template-columns: 220px 376px minmax(0, 1fr);
  background: #fff;
}
.reader-sidebar {
  border-right: 1px solid #dfe4ee;
  padding: 18px 14px;
  overflow: auto;
}
.side-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: #2f3440;
}
.new-button {
  width: 100%;
  margin: 12px 0 14px;
  border: 0;
  background: linear-gradient(135deg, #4b7bec, #31c6c7);
}
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.plan-card {
  min-height: 78px;
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  text-align: left;
  padding: 10px;
  cursor: pointer;
}
.plan-card.active,
.plan-card:hover {
  border-color: #4b7bec;
  background: #f2f6ff;
}
.plan-name {
  display: block;
  color: #2762d8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.plan-meta {
  display: block;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
.outline-area {
  min-width: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #cfd6e3;
  overflow: hidden;
}
.project-head {
  min-height: 142px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 14px 12px;
  border-bottom: 1px solid #edf0f6;
}
.project-head h2 {
  margin: 0 0 12px;
  color: #303133;
  font-size: 18px;
  line-height: 1.4;
}
.project-head p {
  margin: 6px 0;
  color: #606266;
  font-size: 13px;
}
.project-head p span {
  margin-left: 28px;
}
.danger {
  color: #ff4d4f;
}
.success {
  color: #20bf55;
}
.outline-toolbar {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f6;
  font-weight: 700;
}
.outline-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a8abb2;
}
.outline-empty i {
  font-size: 34px;
  margin-bottom: 10px;
}
.outline-list {
  flex: 1;
  overflow: auto;
  padding: 8px 10px 14px;
}
.outline-node {
  width: 100%;
  min-height: 30px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 8px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  color: #303133;
}
.outline-node:hover,
.outline-node.active {
  background: #eef3ff;
}
.outline-node.level-1 {
  margin-top: 8px;
  font-weight: 700;
}
.outline-node.level-2 {
  padding-left: 18px;
}
.outline-node.level-3 {
  padding-left: 36px;
}
.node-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dot {
  width: 4px;
  height: 4px;
  display: inline-block;
  border-radius: 50%;
  background: #28c76f;
  margin-right: 8px;
  vertical-align: middle;
}
.node-words {
  color: #20bf55;
  font-size: 12px;
}
.node-actions {
  display: none;
  color: #7b8794;
  gap: 5px;
}
.outline-node:hover .node-actions,
.outline-node.active .node-actions {
  display: inline-flex;
}
.bottom-actions {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-top: 1px solid #dfe4ee;
}
.export-button {
  flex: 1;
  border: 0;
  background: linear-gradient(135deg, #7948ff, #1f6bff);
}
.document-area {
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}
.doc-section-bar {
  position: sticky;
  top: 0;
  z-index: 3;
  min-height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid #cfd6e3;
  background: #fff;
}
.section-title-wrap {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
}
.section-rail {
  width: 4px;
  height: 26px;
  background: #6b7280;
}
.doc-section-bar h1 {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 16px;
  color: #111827;
}
.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.doc-paragraph-bar {
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid #dfe4ee;
  background: #fff;
}
.paragraph-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}
.editor-toolbox {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.editor-toolbox .el-button {
  padding: 6px;
  border-color: transparent;
}
.editor-paper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: #fff;
}
.heading-gutter,
.editor-paper ::v-deep .rich-content-editor {
  max-width: 980px;
  margin: 0 auto;
  border-left: 1px solid #dfe4ee;
  border-right: 1px solid #dfe4ee;
  background: #fff;
}
.heading-gutter {
  padding: 26px 34px 0;
}
.heading-row {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
}
.heading-row span {
  color: #b5bdc9;
  font-size: 14px;
}
.heading-row strong {
  color: #111827;
  line-height: 1.45;
}
.h4-row strong {
  font-size: 22px;
}
.h5-row strong {
  font-size: 19px;
}
.editor-paper ::v-deep .rich-content-editor {
  height: auto;
  min-height: 0;
}
.document-area ::v-deep .rich-content-editor {
  flex: 0 0 auto;
}
.editor-paper ::v-deep .editor-stage {
  overflow: visible;
  background: #fff;
}
.editor-paper ::v-deep .rich-content-editor.embedded .tiptap-host {
  min-height: 0;
}
.editor-paper ::v-deep .rich-content-editor.embedded .tiptap-host .tiptap-surface {
  min-height: calc(100vh - 288px);
  padding-top: 8px;
}
.doc-empty {
  height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a8abb2;
}
.doc-empty i {
  font-size: 42px;
  margin-bottom: 12px;
}
.paper {
  max-width: 980px;
  min-height: calc(100vh - 230px);
  margin: 0 auto;
  background: #fff;
  border-left: 1px solid #dfe4ee;
  border-right: 1px solid #dfe4ee;
}
.doc-section {
  border-bottom: 1px solid #dfe4ee;
}
.doc-section h2 {
  margin: 0;
  padding: 20px 32px 10px;
  font-size: 18px;
  color: #111827;
  border-left: 4px solid #111827;
}
.doc-section h2.active {
  color: #1f6bff;
  border-left-color: #1f6bff;
}
.doc-item {
  border-top: 1px solid #edf0f6;
  padding-bottom: 22px;
}
.doc-item.active {
  background: #fbfdff;
}
.item-head {
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 32px;
  background: #fafbfe;
  border-bottom: 1px solid #edf0f6;
}
.item-head h3 {
  margin: 0;
  color: #111827;
  font-size: 16px;
}
.item-tools {
  display: flex;
  align-items: center;
  gap: 6px;
}
.paragraph-empty {
  padding: 28px 32px;
  color: #a8abb2;
}
.block-stack {
  padding: 18px 32px 2px;
}
.content-block {
  position: relative;
  margin-bottom: 16px;
}
.content-block p {
  margin: 0 0 10px;
  text-indent: 2em;
  line-height: 1.9;
  color: #1f2937;
  font-size: 14px;
}
.edit-link {
  position: absolute;
  top: 0;
  right: 0;
  border: 0;
  background: transparent;
  color: #1f6bff;
  cursor: pointer;
}
.table-title,
.diagram-title {
  margin-bottom: 10px;
  font-weight: 700;
  color: #303133;
}
.content-block pre {
  margin: 0;
  padding: 10px;
  border-radius: 4px;
  background: #f5f7fa;
  white-space: pre-wrap;
}
@media (max-width: 1360px) {
  .plan-reader-page {
    grid-template-columns: 200px 340px minmax(0, 1fr);
  }
  .doc-header {
    padding-left: 32px;
    padding-right: 32px;
  }
}
</style>
