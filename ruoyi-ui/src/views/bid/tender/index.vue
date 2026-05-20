<template>
  <div class="tender-home-page">
    <aside class="project-sidebar">
      <div class="sidebar-title">我的项目</div>
      <el-input
        v-model="queryParams.title"
        size="small"
        clearable
        placeholder="搜索项目"
        prefix-icon="el-icon-search"
        @keyup.enter.native="handleQuery"
        @clear="handleQuery"
      />

      <div class="project-scroll" v-loading="loading">
        <div v-if="tenderList.length" class="project-list">
          <div
            v-for="item in tenderList"
            :key="item.id"
            class="project-card"
            :class="{ expanded: isExpanded(item) }"
          >
            <div
              class="project-header"
              role="button"
              tabindex="0"
              @click="toggleProject(item)"
              @keyup.enter="toggleProject(item)"
            >
              <span class="status-dot" />
              <span class="project-body">
                <span class="project-name">{{ item.title }}</span>
                <span class="project-meta">{{ item.category || '未分类' }} · {{ formatTime(item.createdTime) }}</span>
              </span>
              <span class="project-actions">
                <el-tooltip content="删除项目" placement="top">
                  <button
                    type="button"
                    class="project-icon-btn project-delete"
                    @click.stop="handleDelete(item)"
                    v-hasPermi="['bid:tender:remove']"
                  >
                    <i class="el-icon-delete" />
                  </button>
                </el-tooltip>
                <button type="button" class="project-icon-btn project-toggle" @click.stop="toggleProject(item)">
                  <i :class="isExpanded(item) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
                </button>
              </span>
            </div>

            <div v-show="isExpanded(item)" class="project-flows" v-loading="flowLoadingIds[item.id]">
              <button
                v-for="flow in projectFlows(item)"
                :key="flow.key"
                type="button"
                class="flow-row"
                :class="{ active: isActiveFlow(item, flow.key) }"
                @click.stop="openFlow(item, flow.key)"
              >
                <span class="doc-icon">DOCX</span>
                <span class="flow-info">
                  <span class="flow-title">{{ flow.title }}</span>
                  <span class="flow-tags">
                    <span class="flow-tag primary">{{ flow.tag }}</span>
                    <span class="flow-tag" :class="flow.type">{{ flow.status }}</span>
                  </span>
                </span>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="empty-projects">
          <i class="el-icon-folder-opened" />
          <span>暂无项目</span>
        </div>
      </div>

      <el-button class="create-button side-create" type="primary" @click="handleCreate" v-hasPermi="['bid:tender:add']">新建项目</el-button>
    </aside>

    <main
      class="tender-main"
      :class="{
        'plan-mode': activeWorkbench === 'plan',
        'report-mode': activeWorkbench === 'report',
        'bid-file-mode': activeWorkbench === 'bidFile'
      }"
    >
      <bid-tender-parse
        v-if="activeWorkbench === 'report' && reportContext.bidId"
        :key="reportWorkspaceKey"
        embedded
        :initial-bid-id="reportContext.bidId"
        :initial-file-id="reportContext.fileId"
      />
      <bid-plan-create
        v-else-if="activeWorkbench === 'plan' && planContext.bidId"
        :key="planWorkspaceKey"
        embedded
        source-module="tender"
        :initial-bid-id="planContext.bidId"
        :initial-tender-file-id="planContext.tenderFileId"
        :initial-tender-report-id="planContext.tenderReportId"
        @exit="closePlanWorkbench"
      />
      <bid-document-editor
        v-else-if="activeWorkbench === 'bidFile' && bidDocumentContext.bidId"
        :key="bidDocumentWorkspaceKey"
        :bid-id="bidDocumentContext.bidId"
        :tender-file-id="bidDocumentContext.tenderFileId"
        @generated="handleBidDocumentGenerated"
      />
      <template v-else>
        <section class="hero-section">
          <h1>AI标书</h1>
          <p>
            一键智能解析招标文件，精准识别提取采购需求及评分标准，步骤式生成技术或服务方案，
            原样式抽离组合招标文件中的模板文档，生成高质量商务标文件，快速完成高质量标书制作任务。
          </p>
        </section>

        <section class="feature-grid" aria-label="AI标书能力">
          <article v-for="card in featureCards" :key="card.title" class="feature-card" :class="card.theme">
            <div class="feature-copy">
              <h2>{{ card.title }}</h2>
              <p>{{ card.description }}</p>
            </div>
            <div class="feature-visual" aria-hidden="true">
              <div class="ai-badge">AI</div>
              <div class="window-shape">
                <span class="window-dot" />
                <span class="window-dot" />
                <div class="visual-lines">
                  <span v-for="line in 5" :key="line" />
                </div>
              </div>
            </div>
          </article>
        </section>

        <el-button class="create-button main-create" type="primary" @click="handleCreate" v-hasPermi="['bid:tender:add']">新建项目</el-button>
      </template>
    </main>

    <el-dialog
      title="新建项目"
      :visible.sync="createDialogVisible"
      width="670px"
      :close-on-click-modal="false"
      custom-class="tender-create-dialog"
      @closed="resetCreateDialog"
    >
      <div class="upload-dialog-body">
        <el-upload
          ref="tenderUpload"
          drag
          action="#"
          accept=".doc,.docx,.pdf,.DOC,.DOCX,.PDF"
          :auto-upload="false"
          :limit="1"
          :file-list="createFileList"
          :on-change="handleCreateFileChange"
          :on-remove="handleCreateFileRemove"
          :on-exceed="handleCreateFileExceed"
        >
          <i class="el-icon-upload" />
          <div class="el-upload__text">拖拽文件至此或<span>点击选择招标文件</span></div>
        </el-upload>
        <div class="upload-tip">
          单次上传文件数量不超过1，仅允许.doc,.docx,.pdf,.DOC,.DOCX,.PDF格式文件，单文件大小不超过50MB
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">开始读标</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { deleteBid } from '@/api/bid/bids'
import { createTender, generateBidDocument, getLatestTenderReport, listTender, listTenderFiles, parseTenderInterpretation } from '@/api/bid/tender'

const BidDocumentEditor = resolve => require(['@/views/bid/tender/BidDocumentEditor.vue'], resolve)
const BidPlanCreate = resolve => require(['@/views/bid/plan/create.vue'], resolve)
const BidTenderParse = resolve => require(['@/views/bid/tender/parse.vue'], resolve)

export default {
  name: 'BidTenderIndex',
  components: { BidDocumentEditor, BidPlanCreate, BidTenderParse },
  data() {
    return {
      loading: false,
      creating: false,
      tenderList: [],
      expandedProjectIds: {},
      flowLoadingIds: {},
      parsingProjectIds: {},
      parsingBidFileProjectIds: {},
      projectFlowMap: {},
      activeWorkbench: 'home',
      reportContext: {},
      planContext: {},
      bidDocumentContext: {},
      createDialogVisible: false,
      createFileList: [],
      createFile: null,
      queryParams: {
        pageNum: 1,
        pageSize: 50,
        title: undefined
      },
      featureCards: [
        {
          title: '招标信息解读',
          description: '智能解析招标文件，生成大纲式招标解读及详细招标关键点，结合招标原文件，帮助用户快速解读招标项目信息。',
          theme: 'theme-blue'
        },
        {
          title: '技术标创作',
          description: '智能提取招标文件中的采购需求及评分标准，结合AI方案模块，傻瓜式快速撰写高质量技术或服务方案。',
          theme: 'theme-red'
        },
        {
          title: '商务标智能填写',
          description: '智能抽取招标文件中的模板文件形成符合招标要求的商务标文件，并结合资料库中的供应商信息实现一键智能填空。',
          theme: 'theme-cyan'
        }
      ]
    }
  },
  computed: {
    planWorkspaceKey() {
      return [
        this.planContext.bidId || '',
        this.planContext.tenderFileId || '',
        this.planContext.tenderReportId || ''
      ].join('-')
    },
    reportWorkspaceKey() {
      return [
        this.reportContext.bidId || '',
        this.reportContext.fileId || '',
        this.reportContext.refresh || ''
      ].join('-')
    },
    bidDocumentWorkspaceKey() {
      return [
        this.bidDocumentContext.bidId || '',
        this.bidDocumentContext.tenderFileId || '',
        this.bidDocumentContext.refresh || ''
      ].join('-')
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList(options = {}) {
      const previousExpanded = Object.assign({}, this.expandedProjectIds)
      this.loading = true
      listTender(this.queryParams).then(res => {
        const rows = res.rows || []
        this.tenderList = rows
        this.syncExpandedProjects(rows, previousExpanded, options.expandId)
        this.loadExpandedProjectFlows()
      }).finally(() => {
        this.loading = false
      })
    },
    syncExpandedProjects(rows, previousExpanded, expandId) {
      const nextExpanded = {}
      const hasPreviousExpanded = Object.keys(previousExpanded).some(key => previousExpanded[key])
      rows.forEach((item, index) => {
        const key = String(item.id)
        if (expandId && String(expandId) === key) {
          nextExpanded[key] = true
        } else if (previousExpanded[key]) {
          nextExpanded[key] = true
        } else if (!expandId && !hasPreviousExpanded && index === 0) {
          nextExpanded[key] = true
        }
      })
      this.expandedProjectIds = nextExpanded
    },
    loadExpandedProjectFlows() {
      this.tenderList.forEach(item => {
        if (this.isExpanded(item)) {
          this.loadProjectFlow(item)
        }
      })
    },
    loadProjectFlow(row) {
      if (!row || !row.id) return
      const key = String(row.id)
      this.$set(this.flowLoadingIds, key, true)
      return Promise.all([
        listTenderFiles(row.id).catch(() => ({ data: [] })),
        getLatestTenderReport(row.id).catch(() => ({ data: {} }))
      ]).then(([fileRes, reportRes]) => {
        const files = fileRes.data || []
        const latestFile = files.find(file => file.fileStage !== 'bid_document') || files[0] || {}
        const bidDocument = files.find(file => file.fileStage === 'bid_document') || {}
        const report = reportRes.data || {}
        this.$set(this.projectFlowMap, key, this.buildProjectFlows(row, latestFile, report, bidDocument))
      }).finally(() => {
        this.$set(this.flowLoadingIds, key, false)
      })
    },
    buildProjectFlows(row, latestFile, report, bidDocument) {
      const key = String(row.id)
      const isParsingReport = !!this.parsingProjectIds[key]
      const isParsingBidFile = !!this.parsingBidFileProjectIds[key]
      const hasReport = !!(report && report.id)
      const hasBidDocument = !!(bidDocument && bidDocument.id)
      const fileStatus = isParsingBidFile ? '正在解析' : (hasBidDocument ? '解析成功' : '待解析')
      const fileType = isParsingBidFile ? 'warning' : (hasBidDocument ? 'success' : 'danger')
      return [
        {
          key: 'report',
          fileId: latestFile && latestFile.id,
          title: '解析报告',
          tag: '招标解读',
          status: isParsingReport ? '正在解析' : (hasReport ? '解析成功' : '待解析'),
          type: isParsingReport ? 'warning' : (hasReport ? 'success' : 'danger')
        },
        {
          key: 'bidFile',
          fileId: latestFile && latestFile.id,
          documentFileId: bidDocument && bidDocument.id,
          title: '投标文件',
          tag: '商务标',
          status: fileStatus,
          type: fileType
        },
        {
          key: 'plan',
          fileId: latestFile && latestFile.id,
          reportId: report && latestFile && String(report.tenderFileId) === String(latestFile.id) ? report.id : undefined,
          title: '技术方案',
          tag: '方案',
          status: Number(row.status) === 2 ? '制作完成' : '待制作',
          type: Number(row.status) === 2 ? 'success' : 'danger'
        }
      ]
    },
    projectFlows(row) {
      return this.projectFlowMap[String(row.id)] || this.buildProjectFlows(row, {}, {})
    },
    isExpanded(row) {
      return !!this.expandedProjectIds[String(row.id)]
    },
    isActiveFlow(row, flowKey) {
      if (this.activeWorkbench === 'plan') {
        return flowKey === 'plan' && this.planContext && String(this.planContext.bidId) === String(row.id)
      }
      if (this.activeWorkbench === 'report') {
        return flowKey === 'report' && this.reportContext && String(this.reportContext.bidId) === String(row.id)
      }
      if (this.activeWorkbench === 'bidFile') {
        return flowKey === 'bidFile' && this.bidDocumentContext && String(this.bidDocumentContext.bidId) === String(row.id)
      }
      return false
    },
    toggleProject(row) {
      const key = String(row.id)
      const expanded = !this.expandedProjectIds[key]
      this.$set(this.expandedProjectIds, key, expanded)
      if (expanded) {
        this.loadProjectFlow(row)
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleCreate() {
      this.createDialogVisible = true
    },
    handleCreateFileChange(file, fileList) {
      const rawFile = file.raw || file
      if (!this.validateCreateFile(rawFile)) {
        this.createFileList = []
        this.createFile = null
        return
      }
      this.createFileList = fileList.slice(-1)
      this.createFile = rawFile
    },
    handleCreateFileRemove() {
      this.createFileList = []
      this.createFile = null
    },
    handleCreateFileExceed(files) {
      const file = files[0]
      if (!this.validateCreateFile(file)) return
      this.createFileList = [{ name: file.name, raw: file }]
      this.createFile = file
    },
    validateCreateFile(file) {
      if (!file) return false
      const ext = (file.name || '').split('.').pop().toLowerCase()
      if (!['doc', 'docx', 'pdf'].includes(ext)) {
        this.$modal.msgError('仅支持 doc、docx、pdf 格式文件')
        return false
      }
      if (file.size > 50 * 1024 * 1024) {
        this.$modal.msgError('单个文件大小不能超过50MB')
        return false
      }
      return true
    },
    submitCreate() {
      if (!this.createFile) {
        this.$modal.msgWarning('请先上传招标文件')
        return
      }
      this.creating = true
      createTender({
        title: this.deriveProjectTitle(this.createFile.name),
        file: this.createFile
      }).then(res => {
        const data = res.data || {}
        const bid = data.bid || {}
        const file = data.file || {}
        if (bid.id) {
          this.$set(this.projectFlowMap, String(bid.id), this.buildProjectFlows(bid, file, {}))
        }
        this.$modal.msgSuccess('项目已创建')
        this.createDialogVisible = false
        this.getList({ expandId: bid.id })
      }).finally(() => {
        this.creating = false
      })
    },
    deriveProjectTitle(fileName) {
      const name = fileName || 'AI标书项目'
      return name.replace(/\.[^.]+$/, '') || 'AI标书项目'
    },
    resetCreateDialog() {
      this.createFileList = []
      this.createFile = null
      if (this.$refs.tenderUpload) {
        this.$refs.tenderUpload.clearFiles()
      }
    },
    openFlow(row, flowKey) {
      if (flowKey === 'report') {
        const reportFlow = this.projectFlows(row).find(item => item.key === 'report') || {}
        const reportReady = reportFlow.type === 'success' || reportFlow.status === '解析成功'
        const reportParsing = reportFlow.type === 'warning' || reportFlow.status === '正在解析'
        if (reportReady) {
          this.openParse(row, reportFlow.fileId)
        } else if (reportParsing) {
          this.$modal.msgWarning('解析报告正在解析，请稍候')
        } else {
          this.confirmAndParse(row)
        }
      } else if (flowKey === 'plan') {
        this.openPlan(row)
      } else if (flowKey === 'bidFile') {
        const bidFileFlow = this.projectFlows(row).find(item => item.key === 'bidFile') || {}
        const bidFileReady = bidFileFlow.type === 'success' || bidFileFlow.status === '解析成功'
        const bidFileParsing = bidFileFlow.type === 'warning' || bidFileFlow.status === '正在解析'
        if (bidFileReady) {
          this.openBidDocument(row)
        } else if (bidFileParsing) {
          this.$modal.msgWarning('投标文件正在解析，请稍候')
        } else {
          this.performBidDocumentParse(row)
        }
      } else {
        this.openParse(row)
      }
    },
    confirmAndParse(row) {
      this.$confirm('解析报告尚未解析，是否进行解析？', '温馨提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        center: true,
        customClass: 'tender-parse-confirm'
      }).then(() => {
        this.performParse(row)
      }).catch(() => {})
    },
    performParse(row) {
      const key = String(row.id)
      let parsedFileId
      const reportFlow = this.projectFlows(row).find(item => item.key === 'report') || {}
      this.$set(this.parsingProjectIds, key, true)
      this.updateReportFlow(row, {
        fileId: reportFlow.fileId,
        status: '正在解析',
        type: 'warning'
      })
      this.$modal.msg('已开始解析，请稍候')
      listTenderFiles(row.id).then(res => {
        const files = res.data || []
        const currentFile = this.pickTenderSourceFile(files, reportFlow.fileId)
        if (!currentFile || !currentFile.id) {
          this.$modal.msgWarning('当前项目暂无招标文件')
          return Promise.reject(new Error('no tender file'))
        }
        parsedFileId = currentFile.id
        this.updateReportFlow(row, {
          fileId: parsedFileId,
          status: '正在解析',
          type: 'warning'
        })
        return parseTenderInterpretation(currentFile.id)
      }).then(() => {
        this.$modal.msgSuccess('解析完成')
        this.$delete(this.parsingProjectIds, key)
        this.updateReportFlow(row, {
          fileId: parsedFileId,
          status: '解析成功',
          type: 'success'
        })
        this.loadProjectFlow(row)
      }).catch(error => {
        this.$delete(this.parsingProjectIds, key)
        if (!(error && error.message === 'no tender file')) {
          const message = error && error.response && error.response.data && error.response.data.msg
            ? error.response.data.msg
            : '解析失败，请稍后重试'
          this.$modal.msgError(message)
        }
        return this.loadProjectFlow(row)
      })
    },
    pickTenderSourceFile(files, preferredFileId) {
      const fileList = files || []
      if (preferredFileId) {
        const preferredFile = fileList.find(file => String(file.id) === String(preferredFileId))
        if (preferredFile) return preferredFile
      }
      return fileList.find(file => file.fileStage !== 'bid_document') || fileList[0]
    },
    updateReportFlow(row, patch) {
      const key = String(row.id)
      const flows = this.projectFlows(row).map(flow => {
        if (flow.key !== 'report') return flow
        return Object.assign({}, flow, patch || {})
      })
      this.$set(this.projectFlowMap, key, flows)
    },
    performBidDocumentParse(row) {
      const key = String(row.id)
      let sourceFileId
      const bidFileFlow = this.projectFlows(row).find(item => item.key === 'bidFile') || {}
      this.$set(this.parsingBidFileProjectIds, key, true)
      this.updateBidFileFlow(row, {
        fileId: bidFileFlow.fileId,
        status: '正在解析',
        type: 'warning'
      })
      this.$modal.msg('投标文件正在解析，请稍候')
      listTenderFiles(row.id).then(res => {
        const files = res.data || []
        const currentFile = this.pickTenderSourceFile(files, bidFileFlow.fileId)
        if (!currentFile || !currentFile.id) {
          this.$modal.msgWarning('当前项目暂无招标文件')
          return Promise.reject(new Error('no tender file'))
        }
        sourceFileId = currentFile.id
        this.updateBidFileFlow(row, {
          fileId: sourceFileId,
          status: '正在解析',
          type: 'warning'
        })
        return generateBidDocument(row.id, {
          tenderFileId: sourceFileId,
          generationMode: 'draftBid',
          targetScope: 'allBidBusinessRelated',
          forceRegenerate: false
        })
      }).then(res => {
        const data = res.data || {}
        const documentFile = data.documentFile || {}
        this.$modal.msgSuccess('投标文件解析完成')
        this.$delete(this.parsingBidFileProjectIds, key)
        this.updateBidFileFlow(row, {
          fileId: sourceFileId,
          documentFileId: documentFile.id,
          status: '解析成功',
          type: 'success'
        })
        return this.loadProjectFlow(row)
      }).catch(error => {
        this.$delete(this.parsingBidFileProjectIds, key)
        if (!(error && error.message === 'no tender file')) {
          const message = error && error.response && error.response.data && error.response.data.msg
            ? error.response.data.msg
            : '投标文件解析失败，请稍后重试'
          this.$modal.msgError(message)
        }
        return this.loadProjectFlow(row)
      })
    },
    updateBidFileFlow(row, patch) {
      const key = String(row.id)
      const flows = this.projectFlows(row).map(flow => {
        if (flow.key !== 'bidFile') return flow
        return Object.assign({}, flow, patch || {})
      })
      this.$set(this.projectFlowMap, key, flows)
    },
    openParse(row, fileId) {
      this.$set(this.expandedProjectIds, String(row.id), true)
      this.reportContext = {
        bidId: row.id,
        fileId: fileId,
        refresh: Date.now()
      }
      this.activeWorkbench = 'report'
    },
    openPlan(row) {
      const flows = this.projectFlows(row)
      const planFlow = flows.find(item => item.key === 'plan') || {}
      const reportFlow = flows.find(item => item.key === 'report') || {}
      const context = {
        bidId: row.id,
        tenderFileId: undefined,
        tenderReportId: undefined
      }
      const tenderFileId = planFlow.fileId || reportFlow.fileId
      if (tenderFileId) {
        context.tenderFileId = tenderFileId
      }
      if (planFlow.reportId) {
        context.tenderReportId = planFlow.reportId
      }
      this.$set(this.expandedProjectIds, String(row.id), true)
      this.planContext = context
      this.activeWorkbench = 'plan'
    },
    openBidDocument(row) {
      const flows = this.projectFlows(row)
      const bidFileFlow = flows.find(item => item.key === 'bidFile') || {}
      this.$set(this.expandedProjectIds, String(row.id), true)
      this.bidDocumentContext = {
        bidId: row.id,
        tenderFileId: bidFileFlow.fileId,
        refresh: Date.now()
      }
      this.activeWorkbench = 'bidFile'
    },
    handleBidDocumentGenerated() {
      const bidId = this.bidDocumentContext && this.bidDocumentContext.bidId
      const row = this.tenderList.find(item => String(item.id) === String(bidId))
      if (row) {
        this.loadProjectFlow(row)
      }
    },
    closePlanWorkbench() {
      this.activeWorkbench = 'home'
      this.planContext = {}
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除项目"' + row.title + '"吗？').then(() => deleteBid(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.$delete(this.expandedProjectIds, String(row.id))
        this.$delete(this.projectFlowMap, String(row.id))
        this.getList()
      }).catch(() => {})
    },
    formatTime(time) {
      return this.parseTime(time, '{y}-{m}-{d} {h}:{i}:{s}') || '-'
    }
  }
}
</script>

<style scoped>
.tender-home-page {
  height: calc(100vh - 84px);
  min-height: 640px;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  overflow: hidden;
  background: #f5f7fb;
}

.project-sidebar {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 18px 14px;
  background: #fff;
  border-right: 1px solid #dfe4ee;
}

.sidebar-title {
  height: 24px;
  margin-bottom: 18px;
  color: #1f2937;
  font-size: 20px;
  font-weight: 500;
  line-height: 24px;
}

.project-sidebar .el-input {
  margin-bottom: 12px;
}

.project-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-card {
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.project-card:hover {
  border-color: #c8d5ea;
}

.project-header {
  height: 66px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  outline: none;
  transition: background .18s ease, box-shadow .18s ease;
}

.project-header:hover,
.project-header:focus,
.project-card.expanded .project-header {
  background: #f8fbff;
}

.status-dot {
  flex: 0 0 auto;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #ff4d4f;
}

.project-body {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.project-name,
.project-meta {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-name {
  color: #2563eb;
  font-size: 13px;
}

.project-meta {
  color: #6480b6;
  font-size: 13px;
}

.project-actions {
  flex: 0 0 auto;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.project-icon-btn {
  width: 22px;
  height: 22px;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: #8b95a7;
  line-height: 22px;
  cursor: pointer;
}

.project-icon-btn:hover {
  background: #eef2f7;
  color: #1f2937;
}

.project-delete {
  opacity: 0;
  color: #fff;
  background: #909399;
  transition: opacity .16s ease, background .16s ease;
}

.project-card:hover .project-delete,
.project-header:focus-within .project-delete {
  opacity: 1;
}

.project-delete:hover {
  background: #f56c6c;
  color: #fff;
}

.project-flows {
  border-top: 1px solid #edf0f6;
  background: #fbfdff;
}

.flow-row {
  width: 100%;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 32px;
  border: 0;
  border-bottom: 1px solid #edf0f6;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.flow-row:last-child {
  border-bottom: 0;
}

.flow-row.active,
.flow-row:hover {
  background: #f3f8ff;
}

.doc-icon {
  flex: 0 0 auto;
  width: 24px;
  height: 30px;
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 3px;
  border-radius: 3px;
  background: linear-gradient(180deg, #e8f0ff, #ffffff);
  border: 1px solid #9fc0ff;
  color: #3b82f6;
  font-size: 8px;
  font-weight: 700;
}

.flow-info {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.flow-title {
  color: #4b5563;
  font-size: 13px;
}

.flow-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flow-tag {
  height: 20px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
}

.flow-tag.primary {
  color: #2563eb;
  background: #eef5ff;
  border: 1px solid #b9d5ff;
}

.flow-tag.success {
  color: #52c41a;
  background: #f0fff1;
  border: 1px solid #b7eb8f;
}

.flow-tag.danger {
  color: #ff4d4f;
  background: #fff1f0;
  border: 1px solid #ffccc7;
}

.flow-tag.warning {
  color: #fa8c16;
  background: #fff7e6;
  border: 1px solid #ffd591;
}

.empty-projects {
  height: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #94a3b8;
}

.empty-projects i {
  font-size: 34px;
}

.create-button {
  width: 100%;
  height: 44px;
  border: 0;
  border-radius: 4px;
  background: linear-gradient(135deg, #2f6df6, #6b5cff);
  box-shadow: 0 10px 18px rgba(47, 109, 246, .18);
  font-size: 15px;
}

.side-create {
  flex: 0 0 auto;
  margin-top: 20px;
}

.tender-main {
  min-width: 0;
  overflow: auto;
  padding: 70px 7vw 28px;
  background:
    linear-gradient(90deg, rgba(235, 243, 255, .9) 0%, rgba(255, 255, 255, .96) 24%, rgba(255, 255, 255, 1) 100%),
    #fff;
}

.tender-main.plan-mode,
.tender-main.report-mode,
.tender-main.bid-file-mode {
  overflow: hidden;
  padding: 0;
  background: #fff;
}

.hero-section {
  max-width: 760px;
  margin: 0 auto 62px;
  text-align: center;
}

.hero-section h1 {
  margin: 0 0 24px;
  color: #2b3038;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.2;
}

.hero-section p {
  margin: 0;
  color: #5f6878;
  font-size: 16px;
  line-height: 1.9;
}

.feature-grid {
  max-width: 1446px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 32px;
}

.feature-card {
  min-height: 462px;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  padding: 34px 28px;
  border: 1px solid rgba(219, 226, 239, .72);
}

.feature-copy {
  position: relative;
  z-index: 2;
}

.feature-card h2 {
  margin: 0 0 22px;
  color: #172033;
  font-size: 23px;
  font-weight: 700;
  line-height: 1.3;
}

.feature-card p {
  margin: 0;
  color: #566273;
  font-size: 15px;
  line-height: 1.85;
}

.theme-blue {
  background: #f3f6ff;
}

.theme-red {
  background: #fff4f5;
}

.theme-cyan {
  background: #f2f9ff;
}

.feature-visual {
  position: absolute;
  left: 52px;
  right: 34px;
  bottom: -20px;
  height: 255px;
}

.ai-badge {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 4;
  width: 82px;
  height: 82px;
  border-radius: 14px;
  background: rgba(255, 255, 255, .92);
  color: #4d80f5;
  border: 6px solid rgba(221, 231, 255, .86);
  box-shadow: 0 14px 26px rgba(74, 114, 208, .16);
  font-size: 38px;
  font-weight: 800;
  line-height: 70px;
  text-align: center;
}

.theme-red .ai-badge {
  color: #ff7478;
  border-color: rgba(255, 214, 216, .92);
}

.window-shape {
  position: absolute;
  left: 45px;
  right: 0;
  top: 32px;
  bottom: 0;
  border-radius: 24px 24px 8px 8px;
  background: linear-gradient(180deg, rgba(70, 132, 246, .58) 0, rgba(70, 132, 246, .28) 58%, rgba(255, 255, 255, 0) 100%);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, .48);
}

.theme-red .window-shape {
  background: linear-gradient(180deg, rgba(255, 99, 104, .62) 0, rgba(255, 99, 104, .24) 58%, rgba(255, 255, 255, 0) 100%);
}

.theme-cyan .window-shape {
  background: linear-gradient(180deg, rgba(78, 161, 239, .6) 0, rgba(78, 161, 239, .25) 58%, rgba(255, 255, 255, 0) 100%);
}

.window-dot {
  position: relative;
  top: 22px;
  left: 72px;
  display: inline-block;
  width: 14px;
  height: 14px;
  margin-right: 16px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .78);
}

.visual-lines {
  position: absolute;
  left: 54px;
  right: 36px;
  top: 82px;
  display: grid;
  gap: 14px;
}

.visual-lines span {
  height: 18px;
  border-radius: 9px;
  background: rgba(255, 255, 255, .82);
  box-shadow: 0 10px 20px rgba(255, 255, 255, .24);
}

.visual-lines span:nth-child(1),
.visual-lines span:nth-child(3) {
  width: 74%;
}

.visual-lines span:nth-child(2),
.visual-lines span:nth-child(5) {
  width: 48%;
}

.visual-lines span:nth-child(4) {
  width: 64%;
}

.main-create {
  width: 158px;
  display: block;
  margin: 62px auto 0;
}

.upload-dialog-body {
  padding: 6px 0 2px;
}

.upload-dialog-body /deep/ .el-upload,
.upload-dialog-body /deep/ .el-upload-dragger {
  width: 100%;
}

.upload-dialog-body /deep/ .el-upload-dragger {
  height: 338px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-dialog-body /deep/ .el-icon-upload {
  color: #a8abb2;
  font-size: 64px;
  line-height: 1;
}

.upload-dialog-body /deep/ .el-upload__text {
  margin-top: 24px;
  color: #606266;
}

.upload-dialog-body /deep/ .el-upload__text span {
  color: #2f6df6;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.dialog-footer {
  text-align: center;
}

@media (max-width: 1360px) {
  .tender-main {
    padding-left: 5vw;
    padding-right: 5vw;
  }

  .feature-grid {
    gap: 20px;
  }
}

@media (max-width: 1100px) {
  .tender-home-page {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-card {
    min-height: 340px;
  }
}
</style>
