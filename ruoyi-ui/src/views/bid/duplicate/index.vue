<template>
  <div class="duplicate-page">
    <aside class="duplicate-sidebar">
      <div class="sidebar-title">
        <i class="el-icon-s-fold"></i>
        <span>我的查重项目</span>
      </div>

      <el-input
        v-model="queryParams.taskName"
        size="small"
        class="project-search"
        clearable
        placeholder="搜索方案"
        suffix-icon="el-icon-search"
        @keyup.enter.native="loadTasks"
        @clear="loadTasks"
      />

      <div v-loading="loadingTasks" class="project-list">
        <button
          v-for="item in taskList"
          :key="item.id"
          class="project-item"
          :class="{ active: currentTask && currentTask.id === item.id }"
          @click="selectTask(item)"
        >
          <span class="project-name">
            <i class="el-icon-document"></i>
            {{ item.sourceFileName || item.taskName }}
          </span>
          <span class="project-meta">{{ item.createBy || 'username' }}创建于{{ shortDate(item.createTime) }}</span>
        </button>
        <div v-if="!taskList.length && !loadingTasks" class="project-empty">--没有更多查重项目了--</div>
      </div>

      <button class="primary-bottom" @click="openCreateDialog">新建查重项目</button>
    </aside>

    <main class="duplicate-main">
      <section v-if="!currentTask" class="duplicate-home">
        <div class="home-copy">
          <h1>投标方案相似风险检测</h1>
          <p>上传待查重文件，选择对比文件或文库，生成可查看、可下载的查重报告。</p>
        </div>

        <div class="home-steps">
          <div v-for="(item, index) in homeSteps" :key="item.title" class="home-step">
            <div class="step-visual" :class="'tone-' + index">
              <span class="step-index">0{{ index + 1 }}</span>
              <i :class="item.icon"></i>
              <b>{{ item.badge }}</b>
            </div>
            <div class="home-step-title">{{ item.title }}</div>
            <p>{{ item.desc }}</p>
          </div>
        </div>

        <el-button type="primary" icon="el-icon-plus" class="home-create" @click="openCreateDialog">
          新建方案查重
        </el-button>
      </section>

      <section v-else class="duplicate-workspace">
        <div class="process-bar">
          <div
            v-for="(step, index) in processSteps"
            :key="step"
            class="process-step"
            :class="{ done: activeStep > index, current: activeStep === index }"
          >
            <span class="step-dot">
              <i v-if="activeStep > index" class="el-icon-check"></i>
              <span v-else>{{ index + 1 }}</span>
            </span>
            <span>{{ step }}</span>
          </div>
        </div>

        <div class="workspace-body">
          <div class="workspace-left">
            <div class="source-summary">
              <div class="source-heading">
                <span class="tag-blue">待查重文档</span>
                <i class="el-icon-document source-icon"></i>
                <strong>{{ currentTask.sourceFileName || currentTask.taskName }}</strong>
                <span class="file-size">{{ formatSize(currentTask.sourceFileSize) }}</span>
                <button type="button" class="text-action">查看项目预览<i class="el-icon-arrow-down"></i></button>
              </div>
              <div class="source-meta-grid">
                <span>作者 <b>{{ currentTask.createBy || '无' }}</b></span>
                <span>模板 <b>Normal</b></span>
                <span>创建时间 <b>{{ currentTask.createTime || '无' }}</b></span>
                <span>字数 <b>{{ currentTask.sourceTextLength || currentTask.totalTextLength || 0 }}</b></span>
                <span>程序名称 <b>{{ officeName(currentTask.sourceFileType) }}</b></span>
                <span>修改时间 <b>{{ currentTask.updateTime || '无' }}</b></span>
                <span>最后一次保存者 <b>{{ currentTask.updateBy || '无' }}</b></span>
                <span>计算机 <b>无</b></span>
                <span>访问时间 <b>无</b></span>
              </div>
            </div>

            <section class="section-block">
              <div class="section-title">
                <span></span>
                <strong>上传对比文件</strong>
                <em>可上传多个文件</em>
              </div>
              <div
                class="upload-zone"
                :class="{ disabled: compareFiles.length >= 3 }"
                @click="triggerCompareInput"
                @dragover.prevent
                @drop.prevent="handleCompareDrop"
              >
                <i class="el-icon-document upload-doc"></i>
                <p>将查重投标文件拖拽至此区域，或 <b>点击添加</b></p>
                <small>支持.doc/.docx格式,仅支持3个文件,单个文件最大200MB</small>
                <input ref="compareInput" type="file" accept=".doc,.docx" multiple hidden @change="handleCompareInputChange" />
              </div>
            </section>

            <section class="section-block">
              <div class="section-title library-title">
                <span></span>
                <strong>对比文库</strong>
                <em>共{{ compareFiles.length }}份文件，启用{{ compareFiles.length }}份</em>
                <el-select
                  v-model="selectedLibraries"
                  size="mini"
                  multiple
                  collapse-tags
                  placeholder="选择对比文库"
                  class="library-select"
                  @change="saveLibraries"
                >
                  <el-option
                    v-for="item in knowledgeList"
                    :key="item.knowledgeId"
                    :label="item.knowledgeName"
                    :value="item.knowledgeId"
                  />
                </el-select>
              </div>

              <div v-if="compareFiles.length" class="compare-list">
                <div v-for="file in compareFiles" :key="file.id" class="compare-file">
                  <div class="compare-file-main">
                    <span class="file-title">
                      <i class="el-icon-document"></i>
                      {{ file.originalName }}
                    </span>
                    <div class="file-actions">
                      <el-switch :value="true" active-color="#4f6ef7" disabled></el-switch>
                      <el-tooltip content="查看详情" placement="top">
                        <button type="button" @click="toggleFileDetail(file.id)">
                          <i :class="isFileExpanded(file.id) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
                        </button>
                      </el-tooltip>
                      <el-tooltip content="删除" placement="top">
                        <button type="button" @click="removeCompareFile(file)">
                          <i class="el-icon-delete"></i>
                        </button>
                      </el-tooltip>
                    </div>
                  </div>
                  <div v-if="isFileExpanded(file.id)" class="compare-detail">
                    <span>作者：无</span>
                    <span>模板：无</span>
                    <span>创建时间：{{ file.createTime || '无' }}</span>
                    <span>字数：{{ textLength(file.extractedText) }}</span>
                    <span>修改时间：{{ file.updateTime || '无' }}</span>
                    <span>程序名称：{{ officeName(file.fileType) }}</span>
                    <span>最后一次保存者：无</span>
                    <span>计算机：无</span>
                    <span>访问时间：无</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-file-state">
                <i class="el-icon-folder-opened"></i>
                <span>暂无项目</span>
              </div>
            </section>
          </div>

          <aside class="report-panel">
            <div class="report-title"><span></span><strong>查重报告</strong></div>

            <div v-if="isChecking" class="report-empty">
              <i class="el-icon-loading"></i>
              <p>查询排队中</p>
            </div>

            <div v-else-if="isSuccess" class="report-success">
              <div class="report-time">
                <span>{{ reportDate(currentTask.finishTime || currentTask.updateTime) }}</span>
                <b></b>
              </div>
              <div class="report-file-card">
                <i class="el-icon-document"></i>
                <div>
                  <strong>{{ reportName }}</strong>
                  <el-tag size="mini" type="success">查重成功</el-tag>
                </div>
                <div class="report-actions">
                  <el-button size="mini" icon="el-icon-view" @click="openReportDetail">查看详情</el-button>
                  <el-button size="mini" icon="el-icon-download" @click="downloadReport">下载报告</el-button>
                </div>
              </div>
            </div>

            <div v-else class="report-empty">
              <i class="el-icon-document-copy"></i>
              <p>暂无查重报告</p>
            </div>

            <el-button type="primary" icon="el-icon-plus" class="new-task-button" :disabled="!canRunTask" @click="openRunDialog">
              新建查重任务
            </el-button>
          </aside>
        </div>
      </section>
    </main>

    <el-dialog title="新建查重项目" :visible.sync="createDialogVisible" width="602px" append-to-body>
      <div
        class="dialog-upload"
        @click="triggerSourceInput"
        @dragover.prevent
        @drop.prevent="handleSourceDrop"
      >
        <i class="el-icon-document-add"></i>
        <p>将查重投标文件拖拽至此区域，或 <b>点击添加</b></p>
        <small>支持.doc/.docx格式,仅支持1个文件,单个文件最大200MB</small>
        <strong v-if="sourceFile">{{ sourceFile.name }}</strong>
        <input ref="sourceInput" type="file" accept=".doc,.docx" hidden @change="handleSourceInputChange" />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreateProject">新建查重项目</el-button>
      </span>
    </el-dialog>

    <el-dialog title="新建查重任务" :visible.sync="runDialogVisible" width="470px" append-to-body>
      <el-alert
        title="请选择查重范围，不同选择将消耗不同字数。"
        type="warning"
        show-icon
        :closable="false"
        class="scope-alert"
      />
      <div class="scope-row">
        <span>查重范围：</span>
        <el-radio-group v-model="runScope" size="medium">
          <el-radio-button label="image">图片</el-radio-button>
          <el-radio-button label="text">文字</el-radio-button>
          <el-radio-button label="full">全文档</el-radio-button>
        </el-radio-group>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="runDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="runningTask" @click="submitRunTask">新建查重任务</el-button>
      </span>
    </el-dialog>

    <el-drawer
      title="查重报告详情"
      :visible.sync="detailVisible"
      direction="rtl"
      size="44%"
      custom-class="duplicate-report-drawer"
    >
      <div v-loading="loadingReport" class="drawer-body">
        <section class="drawer-section">
          <div class="drawer-heading"><span></span><strong>任务概览</strong></div>
          <div class="overview-grid">
            <p>检测对象：<i class="el-icon-document"></i>{{ currentTask && currentTask.sourceFileName }}</p>
            <p>查重范围：{{ reportValue('checkScope') || scopeText(currentTask && currentTask.checkScope) }}</p>
            <p>对比文件：<i class="el-icon-document"></i>{{ reportValue('compareFileNames') || compareFileNames }}</p>
          </div>
        </section>

        <section class="drawer-section">
          <div class="drawer-heading"><span></span><strong>核心指标</strong></div>
          <div class="metric-grid">
            <div class="metric-card blue">
              <span>字面重复率 <i class="el-icon-info"></i></span>
              <b>{{ percentText(reportValue('textSimilarity')) }}</b>
            </div>
            <div class="metric-card red">
              <span>风险片段数 <i class="el-icon-info"></i></span>
              <b>{{ reportValue('riskCount') || 0 }}处</b>
            </div>
            <div class="metric-card amber">
              <span>图片重复率 <i class="el-icon-info"></i></span>
              <b>{{ percentText(reportValue('imageSimilarity')) }}</b>
            </div>
          </div>
        </section>

        <section class="drawer-section">
          <div class="drawer-heading"><span></span><strong>重复来源</strong></div>
          <el-table :data="reportArray('duplicateSources')" size="mini" border>
            <el-table-column label="来源对比文件" prop="sourceName" min-width="150" show-overflow-tooltip />
            <el-table-column label="字面重复率" width="110">
              <template slot-scope="scope">{{ percentText(scope.row.textSimilarity) }}</template>
            </el-table-column>
            <el-table-column label="重复图片" prop="duplicateImages" width="90" />
            <el-table-column label="相似图片" prop="similarImages" width="90" />
            <el-table-column label="风险等级" width="90">
              <template slot-scope="scope">
                <el-tag size="mini" :type="riskTag(scope.row.riskLevel)">{{ riskText(scope.row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="drawer-section">
          <div class="drawer-heading"><span></span><strong>风险 TOP5</strong></div>
          <div v-for="(item, index) in reportArray('topRisks').slice(0, 5)" :key="index" class="risk-item">
            <div class="risk-line">
              <i class="el-icon-warning"></i>
              <strong>融合相似度：{{ percentText(item.similarity) }}</strong>
              <el-tag size="mini">位置：{{ item.location || '文档正文' }}</el-tag>
              <span>{{ item.compareFile }}</span>
            </div>
            <p><em>原文</em>{{ item.originalText || item.sourceText }}</p>
            <p><em>相似</em>{{ item.matchedText || item.compareText }}</p>
          </div>
        </section>

        <section class="drawer-section">
          <div class="drawer-heading"><span></span><strong>文件属性风险</strong></div>
          <el-table :data="reportArray('attributeRisks')" size="mini" border>
            <el-table-column label="筛选项" prop="field" width="110" />
            <el-table-column label="检测对象" prop="detectedValue" min-width="140" show-overflow-tooltip />
            <el-table-column label="风险等级" width="90">
              <template slot-scope="scope">
                <el-tag size="mini" :type="riskTag(scope.row.riskLevel)">{{ riskText(scope.row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="风险分析" prop="riskAnalysis" min-width="220" show-overflow-tooltip />
          </el-table>
        </section>

        <div class="drawer-download">
          <el-button type="primary" icon="el-icon-download" @click="downloadReport">下载报告</el-button>
          <p>更多详细结果请下载检测报告</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  listDuplicateTasks,
  getDuplicateTask,
  createDuplicateTask,
  uploadDuplicateFiles,
  updateDuplicateLibraries,
  runDuplicateTask,
  deleteDuplicateFile,
  listDuplicateFiles,
  getDuplicateReport,
  exportDuplicateReport
} from '@/api/bid/duplicate'
import { listKnowledge } from '@/api/bid/knowledge'
import { downloadRecordUrl } from '@/api/bid/download'

export default {
  name: 'BidDuplicateIndex',
  data() {
    return {
      loadingTasks: false,
      loadingFiles: false,
      loadingReport: false,
      creating: false,
      runningTask: false,
      createDialogVisible: false,
      runDialogVisible: false,
      detailVisible: false,
      taskList: [],
      fileList: [],
      knowledgeList: [],
      selectedLibraries: [],
      expandedFileIds: [],
      currentTask: null,
      sourceFile: null,
      reportPayload: null,
      runScope: 'full',
      queryParams: {
        pageNum: 1,
        pageSize: 200,
        taskName: undefined
      },
      processSteps: ['上传投标文件', '上传对比文件', '查重任务配置', '获取查重报告'],
      homeSteps: [
        { title: '上传投标文件', icon: 'el-icon-upload2', badge: '待查重', desc: '新建项目并保存源文件' },
        { title: '补充对比来源', icon: 'el-icon-document-copy', badge: '对比源', desc: '上传文件或选择知识库' },
        { title: '配置查重任务', icon: 'el-icon-search', badge: '范围', desc: '选择图片、文字或全文档' },
        { title: '获取检测报告', icon: 'el-icon-warning-outline', badge: '报告', desc: '查看详情并下载 ZIP' }
      ]
    }
  },
  computed: {
    compareFiles() {
      return this.fileList.filter(item => item.fileRole === 'compare')
    },
    isChecking() {
      return this.currentTask && ['waiting', 'running'].includes(this.currentTask.status)
    },
    isSuccess() {
      return this.currentTask && this.currentTask.status === 'success'
    },
    activeStep() {
      if (!this.currentTask) return 0
      if (this.currentTask.status === 'success') return 4
      if (this.isChecking) return 3
      if (this.compareFiles.length || this.selectedLibraries.length) return 2
      return 1
    },
    canRunTask() {
      return !!this.currentTask && !this.isChecking && (this.compareFiles.length > 0 || this.selectedLibraries.length > 0)
    },
    reportName() {
      const name = (this.currentTask && (this.currentTask.sourceFileName || this.currentTask.taskName)) || '投标文件'
      return name.replace(/\.(docx|doc)$/i, '') + '_查重报告.docx'
    },
    compareFileNames() {
      return this.compareFiles.map(item => item.originalName).join('、') || '对比文库'
    }
  },
  created() {
    this.loadTasks()
    this.loadKnowledge()
  },
  methods: {
    loadTasks() {
      this.loadingTasks = true
      return listDuplicateTasks(this.queryParams).then(res => {
        this.taskList = res.rows || []
        if (this.currentTask && !this.taskList.some(item => item.id === this.currentTask.id)) {
          this.currentTask = null
          this.fileList = []
        }
      }).finally(() => {
        this.loadingTasks = false
      })
    },
    loadKnowledge() {
      listKnowledge({ pageNum: 1, pageSize: 200 }).then(res => {
        this.knowledgeList = res.rows || []
      })
    },
    selectTask(item) {
      getDuplicateTask(item.id).then(res => {
        this.currentTask = res.data
        this.runScope = this.currentTask.checkScope || 'full'
        this.selectedLibraries = this.parseLibraryIds(this.currentTask.compareLibraryIds)
        this.reportPayload = null
        this.loadFiles()
      })
    },
    refreshCurrent() {
      if (!this.currentTask) return Promise.resolve()
      return getDuplicateTask(this.currentTask.id).then(res => {
        this.currentTask = res.data
        this.selectedLibraries = this.parseLibraryIds(this.currentTask.compareLibraryIds)
        return this.loadFiles()
      }).then(() => this.loadTasks())
    },
    loadFiles() {
      if (!this.currentTask) return Promise.resolve()
      this.loadingFiles = true
      return listDuplicateFiles({ taskId: this.currentTask.id }).then(res => {
        this.fileList = res.data || []
      }).finally(() => {
        this.loadingFiles = false
      })
    },
    openCreateDialog() {
      this.sourceFile = null
      this.createDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.sourceInput) this.$refs.sourceInput.value = ''
      })
    },
    triggerSourceInput() {
      this.$refs.sourceInput && this.$refs.sourceInput.click()
    },
    handleSourceInputChange(event) {
      const file = event.target.files && event.target.files[0]
      this.pickSourceFile(file)
    },
    handleSourceDrop(event) {
      const file = event.dataTransfer.files && event.dataTransfer.files[0]
      this.pickSourceFile(file)
    },
    pickSourceFile(file) {
      if (!file) return
      if (!this.validateDocFile(file, 200)) return
      this.sourceFile = file
    },
    submitCreateProject() {
      if (!this.sourceFile) {
        this.$modal.msgWarning('请先上传待查重投标文件')
        return
      }
      const data = new FormData()
      data.append('file', this.sourceFile)
      this.creating = true
      createDuplicateTask(data).then(res => {
        this.$modal.msgSuccess('查重项目已创建')
        this.createDialogVisible = false
        this.currentTask = res.data
        this.selectedLibraries = []
        this.fileList = []
        return this.loadTasks().then(() => this.loadFiles())
      }).finally(() => {
        this.creating = false
      })
    },
    triggerCompareInput() {
      if (!this.currentTask || this.compareFiles.length >= 3) return
      this.$refs.compareInput && this.$refs.compareInput.click()
    },
    handleCompareInputChange(event) {
      this.uploadCompareSelection(Array.from(event.target.files || []))
      event.target.value = ''
    },
    handleCompareDrop(event) {
      this.uploadCompareSelection(Array.from(event.dataTransfer.files || []))
    },
    uploadCompareSelection(files) {
      if (!this.currentTask || !files.length) return
      const remain = 3 - this.compareFiles.length
      if (files.length > remain) {
        this.$modal.msgWarning('对比文件最多上传3份')
        files = files.slice(0, remain)
      }
      const validFiles = files.filter(file => this.validateDocFile(file, 200))
      if (!validFiles.length) return
      const data = new FormData()
      validFiles.forEach(file => data.append('files', file))
      uploadDuplicateFiles(this.currentTask.id, data).then(() => {
        this.$modal.msgSuccess('对比文件上传成功')
        this.loadFiles()
      })
    },
    validateDocFile(file, maxMb) {
      const extOk = /\.(doc|docx)$/i.test(file.name)
      if (!extOk) {
        this.$modal.msgWarning('仅支持doc/docx格式文件')
        return false
      }
      if (file.size > maxMb * 1024 * 1024) {
        this.$modal.msgWarning('单个文件不能超过' + maxMb + 'MB')
        return false
      }
      return true
    },
    saveLibraries() {
      if (!this.currentTask) return
      updateDuplicateLibraries(this.currentTask.id, { compareLibraryIds: this.selectedLibraries }).then(() => {
        this.currentTask.compareLibraryIds = this.selectedLibraries.join(',')
      })
    },
    openRunDialog() {
      if (!this.canRunTask) {
        this.$modal.msgWarning('请先上传对比文件或选择对比文库')
        return
      }
      this.runScope = this.currentTask.checkScope || 'full'
      this.runDialogVisible = true
    },
    submitRunTask() {
      this.runningTask = true
      this.runDialogVisible = false
      this.currentTask.status = 'waiting'
      runDuplicateTask(this.currentTask.id, {
        checkScope: this.runScope,
        compareLibraryIds: this.selectedLibraries
      }).then(() => {
        this.$modal.msgSuccess('查重成功')
        return this.refreshCurrent()
      }).finally(() => {
        this.runningTask = false
      })
    },
    openReportDetail() {
      if (!this.currentTask) return
      this.detailVisible = true
      this.loadingReport = true
      getDuplicateReport(this.currentTask.id).then(res => {
        this.reportPayload = res.data || {}
      }).finally(() => {
        this.loadingReport = false
      })
    },
    downloadReport() {
      if (!this.currentTask) return
      exportDuplicateReport(this.currentTask.id).then(res => {
        const record = res.data || {}
        if (record.id) {
          window.open(downloadRecordUrl(record.id), '_blank')
        }
      })
    },
    removeCompareFile(file) {
      this.$modal.confirm('确认删除对比文件"' + file.originalName + '"吗？').then(() => {
        return deleteDuplicateFile(file.id)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.loadFiles()
      }).catch(() => {})
    },
    toggleFileDetail(id) {
      const index = this.expandedFileIds.indexOf(id)
      if (index >= 0) {
        this.expandedFileIds.splice(index, 1)
      } else {
        this.expandedFileIds.push(id)
      }
    },
    isFileExpanded(id) {
      return this.expandedFileIds.includes(id)
    },
    reportValue(key) {
      const report = (this.reportPayload && this.reportPayload.report) || this.safeReportJson()
      return report ? report[key] : ''
    },
    reportArray(key) {
      const value = this.reportValue(key)
      return Array.isArray(value) ? value : []
    },
    safeReportJson() {
      if (!this.currentTask || !this.currentTask.reportJson) return {}
      try {
        return JSON.parse(this.currentTask.reportJson)
      } catch (e) {
        return {}
      }
    },
    parseLibraryIds(value) {
      if (!value) return []
      return String(value).split(',').filter(Boolean).map(item => Number(item))
    },
    formatSize(size) {
      const value = Number(size || 0)
      if (!value) return '0 KB'
      if (value >= 1024 * 1024) return (value / 1024 / 1024).toFixed(2) + ' MB'
      return (value / 1024).toFixed(2) + ' KB'
    },
    shortDate(value) {
      if (!value) return ''
      return String(value).slice(0, 10)
    },
    reportDate(value) {
      const text = value ? String(value) : ''
      return text ? text.slice(5, 16).replace(' ', '\n') : '--'
    },
    percentText(value) {
      const num = Number(value || 0)
      return num.toFixed(num % 1 === 0 ? 0 : 1) + '%'
    },
    textLength(value) {
      return value ? String(value).length : 0
    },
    officeName(type) {
      return /docx?/i.test(type || '') ? 'Microsoft Office Word' : '无'
    },
    scopeText(scope) {
      return { image: '图片', text: '文字', full: '全文档' }[scope] || '全文档'
    },
    riskText(level) {
      return { high: '高', middle: '中', low: '低', none: '无' }[level] || '低'
    },
    riskTag(level) {
      return { high: 'danger', middle: 'warning', low: 'success', none: 'info' }[level] || 'success'
    }
  }
}
</script>

<style scoped>
.duplicate-page {
  display: flex;
  height: calc(100vh - 84px);
  min-height: 720px;
  background: #eef3fb;
  color: #1f2d3d;
}

.duplicate-sidebar {
  position: relative;
  width: 258px;
  flex: 0 0 258px;
  padding: 10px 6px 72px;
  background: #ffffff;
  border-right: 1px solid #dde5f2;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 28px;
  padding: 0 4px;
  font-size: 16px;
  font-weight: 700;
}

.project-search {
  margin: 8px 0 10px;
}

.project-list {
  height: calc(100% - 60px);
  overflow-y: auto;
}

.project-item {
  width: 100%;
  min-height: 74px;
  padding: 12px 14px;
  margin-bottom: 8px;
  text-align: left;
  background: #fff;
  border: 1px solid #d8dee9;
  border-radius: 4px;
  cursor: pointer;
}

.project-item.active {
  border-color: #2f6bff;
  background: #f4f8ff;
}

.project-name {
  display: block;
  overflow: hidden;
  color: #1d3c7c;
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.project-name i {
  margin-right: 6px;
}

.project-meta {
  display: block;
  margin-top: 8px;
  padding-left: 20px;
  color: #606b80;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-empty {
  margin: 18px 0;
  text-align: center;
  color: #8a94a6;
  font-size: 13px;
}

.primary-bottom {
  position: absolute;
  left: 7px;
  right: 10px;
  bottom: 12px;
  height: 40px;
  color: #fff;
  border: 0;
  border-radius: 4px;
  background: linear-gradient(90deg, #2f6bff, #7b4dff);
  cursor: pointer;
}

.duplicate-main {
  flex: 1;
  min-width: 0;
  padding: 0 10px 0 12px;
}

.duplicate-home {
  height: 100%;
  border-radius: 8px;
  background: linear-gradient(180deg, #e7f4ff 0%, #f8fbff 30%, #ffffff 72%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 78px;
}

.home-copy {
  text-align: center;
  max-width: 760px;
}

.home-copy h1 {
  margin: 0;
  color: #17233d;
  font-size: 34px;
  line-height: 44px;
  font-weight: 700;
  letter-spacing: 0;
}

.home-copy p {
  margin: 10px 0 0;
  color: #65748b;
  font-size: 16px;
  line-height: 24px;
}

.home-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px;
  width: min(1240px, calc(100% - 80px));
  margin-top: 54px;
}

.home-step {
  position: relative;
  min-width: 0;
  text-align: center;
}

.home-step:not(:last-child)::after {
  content: "";
  position: absolute;
  right: -29px;
  top: 74px;
  width: 26px;
  height: 14px;
  background: #d7dfeb;
  clip-path: polygon(0 0, 70% 0, 100% 50%, 70% 100%, 0 100%, 30% 50%);
}

.step-visual {
  position: relative;
  height: 154px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid transparent;
}

.step-visual i {
  font-size: 46px;
}

.step-index {
  position: absolute;
  top: 12px;
  left: 14px;
  color: rgba(31, 45, 61, 0.45);
  font-size: 13px;
  font-weight: 700;
}

.step-visual b {
  min-width: 66px;
  font-size: 14px;
  color: #fff;
  font-weight: 500;
  padding: 5px 14px;
  border-radius: 16px;
}

.tone-0 { background: #e7f8f0; border-color: #bfe9d4; color: #22a66b; }
.tone-0 b { background: #42c78a; }
.tone-1 { background: #f0ecff; border-color: #d5c8ff; color: #7559d9; }
.tone-1 b { background: #8c70ea; }
.tone-2 { background: #e9f0ff; border-color: #c6d6ff; color: #3f6ed6; }
.tone-2 b { background: #5b83ef; }
.tone-3 { background: #fff4dc; border-color: #f1d391; color: #d98919; }
.tone-3 b { background: #f1a132; }

.home-step-title {
  margin-top: 18px;
  color: #17233d;
  font-size: 16px;
  font-weight: 700;
}

.home-step p {
  margin: 8px 0 0;
  color: #7b8798;
  font-size: 14px;
  line-height: 22px;
}

.home-step-title,
.home-step p {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.home-create {
  margin-top: 72px;
  width: 260px;
  height: 48px;
  font-size: 15px;
}

@media (max-width: 1280px) {
  .duplicate-home {
    padding-top: 58px;
  }

  .home-steps {
    width: calc(100% - 48px);
    gap: 18px;
    margin-top: 42px;
  }

  .step-visual {
    height: 138px;
  }

  .home-step:not(:last-child)::after {
    right: -19px;
    top: 66px;
  }
}

@media (max-width: 1080px) {
  .home-steps {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: min(620px, calc(100% - 48px));
  }

  .home-step:not(:last-child)::after {
    display: none;
  }
}

.duplicate-workspace {
  height: 100%;
  background: #fff;
  overflow: hidden;
}

.process-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 30px;
  height: 50px;
  padding: 0 160px;
  border-bottom: 1px solid #dbe2ef;
  align-items: center;
}

.process-step {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9aa4b2;
  font-size: 13px;
  position: relative;
}

.process-step:not(:last-child)::after {
  content: "";
  position: absolute;
  left: 116px;
  right: -28px;
  height: 1px;
  background: #dbe2ef;
}

.process-step.done,
.process-step.current {
  color: #2f6bff;
  font-weight: 700;
}

.step-dot {
  position: relative;
  z-index: 1;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #999;
  border: 1px solid #d8dde8;
  background: #fff;
  font-size: 12px;
}

.process-step.done .step-dot,
.process-step.current .step-dot {
  color: #fff;
  border-color: #2f6bff;
  background: #2f6bff;
}

.workspace-body {
  display: grid;
  grid-template-columns: minmax(560px, 1fr) 42%;
  height: calc(100% - 50px);
}

.workspace-left {
  overflow-y: auto;
  padding: 14px 18px 40px 20px;
  border-right: 1px solid #dbe2ef;
}

.source-summary {
  padding: 18px 22px;
  background: #f4f7ff;
  border: 1px solid #d7e1ff;
  border-radius: 4px;
}

.source-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #d8dfef;
  padding-bottom: 12px;
}

.tag-blue {
  color: #2f6bff;
  background: #eaf1ff;
  padding: 4px 9px;
  border-radius: 3px;
  font-weight: 700;
}

.source-icon {
  color: #2f6bff;
  font-size: 18px;
}

.source-heading strong {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.file-size {
  color: #52627a;
  border: 1px solid #d8dee9;
  background: #fff;
  border-radius: 4px;
  padding: 2px 8px;
}

.text-action {
  border: 0;
  background: transparent;
  color: #8a94a6;
  cursor: pointer;
}

.source-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  row-gap: 14px;
  column-gap: 30px;
  margin-top: 16px;
  color: #334155;
  font-size: 13px;
}

.source-meta-grid b {
  margin-left: 10px;
  font-weight: 400;
}

.section-block {
  margin-top: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.section-title > span,
.report-title > span,
.drawer-heading > span {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #2f6bff;
}

.section-title strong,
.report-title strong,
.drawer-heading strong {
  font-size: 16px;
}

.section-title em {
  color: #9aa4b2;
  font-style: normal;
  font-size: 13px;
}

.library-title {
  flex-wrap: wrap;
}

.library-select {
  margin-left: auto;
  width: 220px;
}

.upload-zone,
.dialog-upload {
  min-height: 204px;
  border: 1px dashed #cfd8e6;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.upload-zone.disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.upload-doc,
.dialog-upload > i {
  color: #2f8cff;
  font-size: 48px;
  margin-bottom: 12px;
}

.upload-zone p,
.dialog-upload p {
  margin: 0;
  font-size: 13px;
}

.upload-zone b,
.dialog-upload b {
  color: #2f6bff;
}

.upload-zone small,
.dialog-upload small {
  margin-top: 8px;
  color: #8a94a6;
}

.dialog-upload strong {
  margin-top: 14px;
  color: #2f6bff;
  max-width: 420px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.compare-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.compare-file {
  border: 1px solid #dde5f2;
  border-radius: 4px;
  background: #fff;
}

.compare-file-main {
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
}

.file-title {
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.file-title i {
  color: #2f8cff;
  margin-right: 8px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-actions button {
  width: 24px;
  height: 24px;
  border: 0;
  background: transparent;
  color: #607089;
  cursor: pointer;
}

.compare-detail {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 36px;
  margin: 0 12px 12px;
  padding: 16px 24px;
  background: #f7f9fc;
  color: #334155;
  font-size: 13px;
}

.empty-file-state {
  height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a1aab8;
  border: 1px solid #edf1f7;
  border-radius: 6px;
}

.empty-file-state i {
  font-size: 32px;
  color: #d4dce8;
  margin-bottom: 8px;
}

.report-panel {
  position: relative;
  padding: 16px 20px 96px;
  overflow-y: auto;
}

.report-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-empty {
  height: 420px;
  color: #8a94a6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.report-empty i {
  color: #dbe4fb;
  font-size: 82px;
  margin-bottom: 14px;
}

.report-success {
  margin-top: 34px;
}

.report-time {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-right: 12px;
  color: #1f2937;
  white-space: pre-line;
}

.report-time b {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #72d26b;
  box-shadow: 0 0 0 4px #dff4dc;
}

.report-file-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 78px;
  margin-top: 8px;
  padding: 14px 12px;
  background: #f1f7ff;
  border: 1px solid #cfe0ff;
  border-radius: 6px;
}

.report-file-card > i {
  color: #2f8cff;
  font-size: 38px;
}

.report-file-card strong {
  display: block;
  margin-bottom: 8px;
}

.report-actions {
  display: flex;
  gap: 8px;
}

.new-task-button {
  position: absolute;
  left: 50%;
  bottom: 28px;
  transform: translateX(-50%);
  width: 200px;
}

.scope-alert {
  margin-bottom: 30px;
}

.scope-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 36px 0 24px;
}

.drawer-body {
  padding: 0 24px 36px;
}

.drawer-section {
  margin-bottom: 26px;
}

.drawer-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 40px;
  color: #4b5565;
}

.overview-grid p {
  margin: 0;
}

.overview-grid i {
  color: #2f8cff;
  margin: 0 6px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  height: 130px;
  padding: 16px;
  border-radius: 6px;
}

.metric-card span {
  color: #5f6f86;
}

.metric-card b {
  display: block;
  margin-top: 32px;
  text-align: center;
  font-size: 28px;
  font-weight: 500;
}

.metric-card.blue { background: #f0f4ff; }
.metric-card.blue b { color: #8554d8; }
.metric-card.red { background: #fff3f4; }
.metric-card.red b { color: #ff4d57; }
.metric-card.amber { background: #fffaf1; }
.metric-card.amber b { color: #6b7280; }

.risk-item {
  border: 1px solid #dfe6f0;
  border-radius: 6px;
  padding: 12px 14px;
  margin-bottom: 10px;
}

.risk-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.risk-line i,
.risk-line strong {
  color: #ff4d57;
}

.risk-item p {
  margin: 10px 0 0;
  padding: 8px 10px;
  background: #f5f7fb;
  border-radius: 4px;
  color: #344054;
}

.risk-item em {
  color: #8793a5;
  font-style: normal;
  margin-right: 10px;
}

.drawer-download {
  text-align: center;
  padding: 10px 0 20px;
}

.drawer-download .el-button {
  width: 200px;
}

.drawer-download p {
  color: #7a8596;
  font-size: 12px;
}

</style>
