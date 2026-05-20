<template>
  <div class="tender-report-page" :class="{ embedded: embedded }">
    <aside v-if="!embedded" class="project-sidebar">
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

      <div class="project-scroll" v-loading="projectLoading">
        <div v-for="item in tenderList" :key="item.id" class="project-card" :class="{ expanded: String(item.id) === String(bidId) }">
          <div class="project-header" @click="openProject(item)">
            <span class="status-dot" />
            <span class="project-body">
              <span class="project-name">{{ item.title }}</span>
              <span class="project-meta">{{ item.category || '未分类' }} · {{ formatTime(item.createdTime) }}</span>
            </span>
            <i :class="String(item.id) === String(bidId) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
          </div>
          <div v-if="String(item.id) === String(bidId)" class="project-flows">
            <button
              v-for="flow in currentFlows"
              :key="flow.key"
              type="button"
              class="flow-row"
              :class="{ active: activeFlowKey === flow.key }"
              @click.stop="openFlow(flow.key)"
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

      <el-button class="create-button" type="primary" @click="$router.push('/bid/tender')">返回首页</el-button>
    </aside>

    <main v-if="activeWorkbench === 'plan'" class="embedded-plan-workspace">
      <bid-plan-create
        v-if="planContext.bidId"
        :key="planWorkspaceKey"
        embedded
        source-module="tender"
        :initial-bid-id="planContext.bidId"
        :initial-tender-file-id="planContext.tenderFileId"
        :initial-tender-report-id="planContext.tenderReportId"
        @exit="closePlanWorkbench"
      />
    </main>

    <main v-else class="report-workspace" v-loading="loading">
      <section class="report-nav">
        <div class="nav-scroll">
          <el-tooltip
            v-for="row in navRows"
            :key="row.key"
            :content="row.number + ' ' + row.title"
            placement="right"
            :open-delay="250"
          >
            <button
              type="button"
              class="nav-row"
              :class="[{ active: row.key === activeKey }, 'level-' + row.level]"
              @click="scrollToReportRow(row.key)"
            >
              <i :class="row.level === 1 ? 'el-icon-notebook-2' : 'el-icon-document'" />
              <span class="nav-title">{{ row.title }}</span>
            </button>
          </el-tooltip>
        </div>
        <button type="button" class="export-report">导出解析报告</button>
      </section>

      <section ref="contentScroll" class="report-content">
        <div v-if="!report.id" class="empty-report">
          <i class="el-icon-warning-outline" />
          <p>解析报告尚未生成，请返回首页点击解析报告进行解析。</p>
        </div>
        <template v-else>
          <article
            v-for="section in visibleContentSections"
            :key="section.key"
            class="content-section"
            :class="['level-' + section.level, { active: section.key === activeKey }]"
            :data-report-key="section.key"
          >
            <h2 v-if="section.level === 1">
              <span>{{ section.number }}</span>
              {{ section.title }}
            </h2>
            <h3 v-else>
              <span>{{ section.number }}</span>
              {{ section.title }}
            </h3>
            <div v-if="section.rows.length" class="content-list">
              <div v-for="(row, index) in section.rows" :key="index" class="content-row">
                <h4 v-if="row.title">{{ row.title }}</h4>
                <ol v-if="row.lines.length" class="content-lines">
                  <li v-for="(line, lineIndex) in row.lines" :key="lineIndex">{{ displayLine(line) }}</li>
                </ol>
              </div>
            </div>
            <div v-else class="content-empty">暂无解析内容</div>
          </article>
        </template>
      </section>

      <section class="file-preview">
        <div class="preview-toolbar">
          <span>文件</span>
          <span>视图</span>
          <span class="toolbar-spacer" />
          <button type="button" class="toolbar-icon" title="下载文件" @click="downloadPreviewFile">
            <i class="el-icon-download" />
          </button>
          <i class="el-icon-search" />
        </div>
        <div class="preview-body">
          <div v-if="previewComponent" class="office-viewer">
            <div class="office-zoom-layer" :style="officeZoomStyle">
              <component
                :is="previewComponent"
                :key="officeViewerKey"
                v-bind="previewProps"
                class="office-root"
                @rendered="handleOfficeRendered"
                @error="handleOfficeError"
              />
            </div>
          </div>
          <div v-else class="preview-state">
            {{ previewMessage }}
          </div>
        </div>
        <div v-if="previewComponent" class="preview-footer">
          <span class="preview-page-info">{{ currentFile.originalName || '招标文件预览' }}</span>
          <button type="button" class="preview-ctrl" title="适应窗口" @click="resetZoom">
            <i class="el-icon-full-screen" />
          </button>
          <button type="button" class="preview-ctrl" title="缩小" @click="zoomOut">-</button>
          <span class="zoom-label">缩放{{ officeZoom }}%</span>
          <button type="button" class="preview-ctrl" title="放大" @click="zoomIn">+</button>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { getLatestTenderReport, getTenderReportByFile, listTender, listTenderFiles } from '@/api/bid/tender'
import VueOfficeDocx from '@vue-office/docx'
import VueOfficePdf from '@vue-office/pdf'
import '@vue-office/docx/lib/index.css'

const BidPlanCreate = resolve => require(['@/views/bid/plan/create.vue'], resolve)

export default {
  name: 'BidTenderParse',
  components: {
    BidPlanCreate,
    VueOfficeDocx,
    VueOfficePdf
  },
  props: {
    embedded: {
      type: Boolean,
      default: false
    },
    initialBidId: {
      type: [String, Number],
      default: undefined
    },
    initialFileId: {
      type: [String, Number],
      default: undefined
    }
  },
  data() {
    return {
      bidId: undefined,
      fileId: undefined,
      loading: false,
      projectLoading: false,
      tenderList: [],
      files: [],
      report: {},
      activeWorkbench: 'report',
      planContext: {},
      activeKey: '',
      officeZoom: 100,
      queryParams: {
        pageNum: 1,
        pageSize: 50,
        title: undefined
      }
    }
  },
  computed: {
    activeFlowKey() {
      return this.activeWorkbench === 'plan' ? 'plan' : 'report'
    },
    planWorkspaceKey() {
      return [
        this.planContext.bidId || '',
        this.planContext.tenderFileId || '',
        this.planContext.tenderReportId || ''
      ].join('-')
    },
    currentFile() {
      if (this.fileId) {
        return this.files.find(item => String(item.id) === String(this.fileId)) || this.files[0] || {}
      }
      return this.files[0] || {}
    },
    currentFlows() {
      const hasReport = !!this.report.id
      const fileParsed = hasReport || this.currentFile.parseStatus === 'success' || this.currentFile.businessParseStatus === 'success'
      return [
        { key: 'report', title: '解析报告', tag: '招标解读', status: hasReport ? '解析成功' : '待解析', type: hasReport ? 'success' : 'danger' },
        { key: 'file', title: '招标文件', tag: '商务标', status: fileParsed ? '解析成功' : '待解析', type: fileParsed ? 'success' : 'danger' },
        { key: 'plan', title: '技术方案', tag: '方案', status: '待制作', type: 'danger' }
      ]
    },
    reportTree() {
      const content = this.report.reportContent
      if (!content) return this.report.id ? this.legacyReportTree() : null
      if (typeof content === 'object') {
        return this.isTenderInterpretationTree(content) ? content : this.legacyReportTree()
      }
      try {
        const parsed = JSON.parse(content)
        return this.isTenderInterpretationTree(parsed) ? parsed : this.legacyReportTree()
      } catch (e) {
        return this.legacyReportTree()
      }
    },
    reportSections() {
      const value = this.reportTree && Array.isArray(this.reportTree.v) ? this.reportTree.v : []
      return value.filter(item => item && item.n)
    },
    navRows() {
      const rows = []
      this.reportSections.forEach((section, index) => {
        const number = String(index + 1)
        rows.push({ key: section.k, number, title: section.n, node: section, level: 1, parentKey: '' })
        if (this.hasNavChildren(section)) {
          this.nodeChildren(section).forEach((child, childIndex) => {
            rows.push({
              key: child.k,
              number: number + '.' + (childIndex + 1),
              title: child.n,
              node: child,
              level: 2,
              parentKey: section.k
            })
          })
        }
      })
      return rows
    },
    contentSections() {
      const sections = []
      this.reportSections.forEach((section, index) => {
        const number = String(index + 1)
        const hasChildren = this.hasNavChildren(section)
        sections.push({
          key: section.k,
          number,
          title: section.n,
          level: 1,
          parentKey: '',
          rows: hasChildren ? [] : this.buildContentRows(section)
        })
        if (hasChildren) {
          this.nodeChildren(section).forEach((child, childIndex) => {
            sections.push({
              key: child.k,
              number: number + '.' + (childIndex + 1),
              title: child.n,
              level: 2,
              parentKey: section.k,
              rows: this.buildContentRows(child)
            })
          })
        }
      })
      return sections
    },
    visibleContentSections() {
      return this.contentSections
    },
    previewUrl() {
      const url = this.currentFile.fileUrl || ''
      if (!url) return ''
      if (/^https?:\/\//i.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    },
    previewComponent() {
      if (!this.previewUrl) return ''
      if (this.previewKind === 'pdf') return 'VueOfficePdf'
      if (this.previewKind === 'docx') return 'VueOfficeDocx'
      return ''
    },
    previewMessage() {
      if (!this.previewUrl) return '当前项目暂无可预览的招标文件。'
      if (this.previewKind === 'doc') return 'vue-office 仅支持 docx/pdf 在线预览，doc 文件请先转换为 docx 或 pdf。'
      return '当前文件格式暂不支持在线预览，请下载后查看。'
    },
    previewKind() {
      const type = (this.currentFile.fileType || this.currentFile.originalName || '').toLowerCase()
      if (type.endsWith('pdf') || type === 'pdf') return 'pdf'
      if (type.endsWith('docx') || type === 'docx') return 'docx'
      if (type.endsWith('doc') || type === 'doc') return 'doc'
      return 'unsupported'
    },
    previewProps() {
      const props = { src: this.previewUrl }
      if (this.previewKind === 'docx') {
        props.options = { breakPages: true, inWrapper: true }
      }
      return props
    },
    officeZoomStyle() {
      return {
        zoom: this.officeZoom / 100
      }
    },
    officeViewerKey() {
      return this.previewKind + '-' + this.previewUrl
    }
  },
  watch: {
    initialBidId() {
      this.resetByProps()
    },
    initialFileId() {
      this.resetByProps()
    },
    navRows(rows) {
      if (!this.activeKey && rows.length) {
        this.activeKey = rows[0].key
      }
    },
    previewUrl(value, oldValue) {
      if (value !== oldValue) {
        this.officeZoom = 100
      }
    }
  },
  created() {
    this.bidId = this.initialBidId || this.$route.query.bidId
    this.fileId = this.initialFileId || this.$route.query.fileId
    if (!this.embedded) {
      this.loadProjects()
    }
    this.loadReportPage()
  },
  methods: {
    resetByProps() {
      if (!this.embedded) return
      this.bidId = this.initialBidId
      this.fileId = this.initialFileId
      this.files = []
      this.report = {}
      this.activeWorkbench = 'report'
      this.planContext = {}
      this.activeKey = ''
      this.loadReportPage()
    },
    isTenderInterpretationTree(tree) {
      return !!(tree && tree.k === 'TENDER_INTERPRETATION_REPORT' && Array.isArray(tree.v))
    },
    legacyReportTree() {
      return {
        k: 'TENDER_INTERPRETATION_REPORT',
        n: '招标解读',
        v: [
          { k: 'PROJECT_INTERPRETATION', n: '项目解读', v: this.valueToLines(this.report.requirementSummary) },
          { k: 'PROJECT_INFO', n: '项目基础信息', v: [] },
          { k: 'FORM_REVIEW', n: '形式审查项', v: [
            { k: 'DOC_CATALOG', n: '投标文件目录', v: [] },
            { k: 'BLIND_BID_FORMAT', n: '暗标格式', v: [] },
            { k: 'OTHER_FORM_REQUIREMENTS', n: '其他要求', v: [] },
            { k: 'PACKAGING_REQUIREMENTS', n: '封装要求', v: [] }
          ] },
          { k: 'QUALIFICATION_REVIEW', n: '资格审查项', v: [
            { k: 'QUALIFICATION_REQUIREMENTS', n: '资质要求', v: [] },
            { k: 'PERFORMANCE_REQUIREMENTS', n: '业绩要求', v: [] },
            { k: 'FINANCIAL_REQUIREMENTS', n: '财务要求', v: [] }
          ] },
          { k: 'RESPONSE_REVIEW', n: '响应审查项', v: [
            { k: 'TECHNICAL_REQUIREMENTS', n: '技术要求', v: [] },
            { k: 'PARAMETER_REQUIREMENTS', n: '参数要求', v: [] },
            { k: 'SUBSTANTIVE_TERMS', n: '实质性条款', v: [] }
          ] },
          { k: 'EVALUATION_FRAMEWORK', n: '评审框架', v: [
            { k: 'EVALUATION_METHOD', n: '评审方式', v: [] },
            { k: 'EVALUATION_CRITERIA', n: '评审标准', v: this.valueToLines(this.report.scoreItems) }
          ] },
          { k: 'INVALID_AND_WASTE_BID', n: '无效标与废标项', v: [
            { k: 'WASTE_BID_ITEMS', n: '废标项', v: [] }
          ] },
          { k: 'KEYWORD_EXTRACTION', n: '关键字抓取项', v: [
            { k: 'KW_QUALIFICATION', n: '资格', v: [] },
            { k: 'KW_WASTE_BID', n: '废标', v: [] },
            { k: 'KW_INVALID', n: '无效', v: [] },
            { k: 'KW_RESPONSE', n: '响应', v: [] },
            { k: 'KW_COMMITMENT', n: '承诺', v: [] }
          ] }
        ]
      }
    },
    hasNavChildren(node) {
      return ['FORM_REVIEW', 'QUALIFICATION_REVIEW', 'RESPONSE_REVIEW', 'EVALUATION_FRAMEWORK', 'INVALID_AND_WASTE_BID', 'KEYWORD_EXTRACTION'].includes(node && node.k)
    },
    nodeChildren(node) {
      return Array.isArray(node && node.v) ? node.v.filter(item => item && item.k && item.n) : []
    },
    scrollToReportRow(key) {
      this.activeKey = key
      this.$nextTick(() => {
        const container = this.$refs.contentScroll
        if (!container) return
        const target = container.querySelector('[data-report-key="' + key + '"]')
        if (target && target.scrollIntoView) {
          target.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },
    loadProjects() {
      this.projectLoading = true
      listTender(this.queryParams).then(res => {
        this.tenderList = res.rows || []
      }).finally(() => {
        this.projectLoading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.loadProjects()
    },
    loadReportPage() {
      if (!this.bidId) return
      this.loading = true
      listTenderFiles(this.bidId).then(res => {
        this.files = res.data || []
        const hasCurrentFile = this.fileId && this.files.some(item => String(item.id) === String(this.fileId))
        if ((!this.fileId || !hasCurrentFile) && this.files.length) {
          this.fileId = this.files[0].id
        }
        return this.loadReport()
      }).catch(() => {
        this.files = []
        this.report = {}
        this.$modal.msgError('解析报告加载失败，请稍后重试')
      }).finally(() => {
        this.loading = false
      })
    },
    loadReport() {
      const action = this.fileId
        ? getTenderReportByFile(this.fileId).catch(() => getLatestTenderReport(this.bidId))
        : getLatestTenderReport(this.bidId)
      return action.then(res => {
        this.report = res.data || {}
        if ((!this.activeKey || !this.navRows.some(item => item.key === this.activeKey)) && this.navRows.length) {
          this.activeKey = this.navRows[0].key
        }
      })
    },
    openProject(row) {
      if (String(row.id) === String(this.bidId)) return
      this.$router.push({ path: '/bid/tender/parse', query: { bidId: row.id } })
    },
    openFlow(key) {
      if (key === 'report') {
        this.activeWorkbench = 'report'
        if (this.report && this.report.id && this.navRows.length) {
          this.$nextTick(() => this.scrollToReportRow(this.navRows[0].key))
        } else {
          this.$modal.msgWarning('解析报告尚未生成，请返回首页点击解析报告进行解析。')
        }
      } else if (key === 'file') {
        this.activeWorkbench = 'report'
        if (this.previewUrl) {
          this.$modal.msg('已定位招标文件预览')
        } else {
          this.$modal.msgWarning('当前项目暂无可预览的招标文件。')
        }
      } else if (key === 'plan') {
        this.planContext = {
          bidId: this.bidId,
          tenderFileId: undefined,
          tenderReportId: undefined
        }
        if (this.currentFile && this.currentFile.id) {
          this.planContext.tenderFileId = this.currentFile.id
        }
        if (this.report && this.report.id) {
          this.planContext.tenderReportId = this.report.id
        }
        this.activeWorkbench = 'plan'
      }
    },
    closePlanWorkbench() {
      this.activeWorkbench = 'report'
      this.planContext = {}
    },
    buildContentRows(node) {
      if (!node) return []
      const value = node.v
      if (Array.isArray(value)) {
        if (value.length && value.every(item => item && typeof item === 'object' && item.n !== undefined)) {
          return value.map(item => ({
            title: item.n,
            lines: this.valueToLines(item.v)
          }))
        }
        return [{ title: '', lines: this.valueToLines(value) }]
      }
      return [{ title: '', lines: this.valueToLines(value) }]
    },
    valueToLines(value) {
      if (value === undefined || value === null || value === '') return ['空']
      if (Array.isArray(value)) {
        if (!value.length) return ['空']
        const lines = value.map(item => this.valueToText(item)).filter(Boolean)
        return lines.length ? lines : ['空']
      }
      const lines = String(value).split(/\r?\n/).map(item => item.trim()).filter(Boolean)
      return lines.length ? lines : ['空']
    },
    valueToText(value) {
      if (value === undefined || value === null || value === '') return ''
      if (typeof value === 'string') return value
      if (typeof value === 'number' || typeof value === 'boolean') return String(value)
      if (Array.isArray(value)) return value.map(item => this.valueToText(item)).filter(Boolean).join('；')
      if (typeof value === 'object') {
        if (value.n !== undefined && value.v !== undefined) return value.n + '：' + this.valueToText(value.v)
        return Object.keys(value).map(key => key + '：' + this.valueToText(value[key])).join('；')
      }
      return ''
    },
    displayLine(line) {
      return String(line || '').replace(/^\s*\d+[、.．]\s*/, '')
    },
    handleOfficeRendered() {
      this.$nextTick(() => {})
    },
    handleOfficeError() {
      this.$modal.msgError('文件预览失败，请确认文件格式为 docx 或 pdf。')
    },
    zoomIn() {
      this.officeZoom = Math.min(200, this.officeZoom + 10)
    },
    zoomOut() {
      this.officeZoom = Math.max(40, this.officeZoom - 10)
    },
    resetZoom() {
      this.officeZoom = 100
    },
    downloadPreviewFile() {
      if (this.previewUrl) {
        window.open(this.previewUrl, '_blank')
      }
    },
    formatTime(time) {
      return this.parseTime(time, '{y}-{m}-{d} {h}:{i}:{s}') || '-'
    }
  }
}
</script>

<style scoped>
.tender-report-page {
  height: calc(100vh - 84px);
  min-height: 640px;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  background: #f5f7fb;
  overflow: hidden;
}

.tender-report-page.embedded {
  height: 100%;
  min-height: 0;
  grid-template-columns: minmax(0, 1fr);
}

.project-sidebar {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 18px 12px;
  background: #fff;
  border-right: 1px solid #dfe4ee;
}

.sidebar-title {
  margin-bottom: 18px;
  color: #1f2937;
  font-size: 20px;
  font-weight: 500;
}

.project-sidebar .el-input {
  margin-bottom: 12px;
}

.project-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}

.project-card {
  margin-bottom: 8px;
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.project-card.expanded {
  border-color: #2f6df6;
}

.project-header {
  height: 66px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  cursor: pointer;
}

.status-dot {
  width: 11px;
  height: 11px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #ff4d4f;
}

.project-body,
.flow-info {
  min-width: 0;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.project-name,
.project-meta {
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
  text-align: left;
  cursor: pointer;
}

.flow-row.active,
.flow-row:hover {
  background: #edf5ff;
}

.doc-icon {
  width: 24px;
  height: 30px;
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 3px;
  border-radius: 3px;
  border: 1px solid #9fc0ff;
  background: linear-gradient(180deg, #e8f0ff, #fff);
  color: #3b82f6;
  font-size: 8px;
  font-weight: 700;
}

.flow-title {
  color: #4b5563;
  font-size: 13px;
}

.flow-tags {
  display: flex;
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

.create-button {
  width: 100%;
  height: 44px;
  margin-top: 14px;
  border: 0;
  border-radius: 4px;
  background: linear-gradient(135deg, #2f6df6, #6b5cff);
}

.report-workspace {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-columns: 210px minmax(420px, 1fr) minmax(420px, 35vw);
  gap: 0;
  padding: 0;
  overflow: hidden;
}

.embedded-plan-workspace {
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: #fff;
}

.report-nav,
.report-content,
.file-preview {
  min-height: 0;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow: auto;
}

.report-nav {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 8px 0;
}

.nav-row {
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border: 0;
  background: transparent;
  color: #303133;
  font-size: 14px;
  line-height: 20px;
  text-align: left;
  cursor: pointer;
}

.nav-title {
  flex: 1 1 auto;
  min-width: 0;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.nav-row.level-1 {
  padding: 9px 12px;
  font-weight: 600;
}

.nav-row.level-2 {
  padding: 9px 12px 9px 30px;
}

.nav-row.active,
.nav-row:hover {
  color: #2f6df6;
  background: #eef5ff;
}

.export-report {
  flex: 0 0 auto;
  height: 48px;
  border: 0;
  background: #2f6df6;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

.report-content {
  padding: 14px 24px 28px;
  color: #566273;
  font-size: 16px;
  line-height: 1.72;
  scroll-behavior: smooth;
}

.content-section {
  scroll-margin-top: 16px;
  padding: 0 0 18px;
}

.content-section.level-2 {
  padding-left: 0;
  border-left: 0;
}

.content-section.active {
  background: transparent;
}

.report-content h2,
.report-content h3 {
  margin: 0 0 10px;
  color: #2f6df6;
  font-size: 18px;
}

.report-content h3 {
  color: #1f2937;
  font-size: 16px;
}

.report-content h2 span,
.report-content h3 span {
  margin-right: 8px;
  color: #8b95a7;
  font-weight: 500;
}

.content-row {
  margin-bottom: 16px;
}

.content-row h4 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 16px;
}

.content-row p {
  margin: 0 0 4px;
}

.content-lines {
  margin: 0;
  padding-left: 0;
  list-style-position: inside;
}

.content-lines li {
  margin-bottom: 6px;
}

.content-empty {
  color: #909399;
}

.empty-report {
  min-height: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-report i {
  margin-bottom: 10px;
  font-size: 36px;
}

.file-preview {
  display: flex;
  flex-direction: column;
  background: #dfe2e6;
  border-right: 0;
}

.preview-toolbar {
  height: 36px;
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 0 12px;
  background: #f4f4f4;
  border-bottom: 1px solid #cfd4dc;
  color: #303133;
}

.toolbar-spacer {
  flex: 1 1 auto;
}

.toolbar-icon {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: #303133;
  cursor: pointer;
}

.toolbar-icon:hover {
  color: #2f6df6;
}

.preview-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  padding: 12px 16px;
}

.office-viewer {
  min-height: 100%;
}

.office-zoom-layer {
  min-height: 100%;
  transform-origin: top center;
}

.office-root {
  width: 100%;
  min-height: 100%;
  border: 0;
  background: #fff;
}

.preview-state {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #606266;
  text-align: center;
}

.preview-state.error {
  color: #f56c6c;
}

.preview-footer {
  height: 34px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 12px;
  background: #f7f7f7;
  border-top: 1px solid #cfd4dc;
  color: #303133;
  font-size: 13px;
}

.preview-page-info {
  min-width: 0;
  margin-right: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-ctrl {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: #303133;
  cursor: pointer;
  font-size: 16px;
}

.preview-ctrl:hover {
  background: #e8edf5;
  color: #2f6df6;
}

.zoom-label {
  min-width: 66px;
  text-align: center;
}

.file-preview ::v-deep .docx-wrapper {
  background: transparent;
  padding: 8px 0;
}

.file-preview ::v-deep .docx {
  box-shadow: 0 1px 4px rgba(15, 23, 42, .12);
}
</style>
