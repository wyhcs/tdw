<template>
  <div class="quality-page">
    <aside class="quality-sidebar">
      <div class="sidebar-title">
        <i class="el-icon-s-fold" />
        <span>我的项目</span>
      </div>
      <el-input
        v-model="queryParams.title"
        class="project-search"
        size="small"
        clearable
        placeholder="搜索项目"
        prefix-icon="el-icon-search"
        @keyup.enter.native="loadProjects"
        @clear="loadProjects"
      />

      <div class="project-scroll" v-loading="projectLoading">
        <button
          v-for="project in projectList"
          :key="project.id"
          type="button"
          class="project-card"
          :class="{ active: activeProject && activeProject.id === project.id }"
          @click="openProject(project)"
        >
          <i class="el-icon-document" />
          <span class="project-text">
            <strong>{{ project.title }}</strong>
            <small>{{ project.createBy || 'Qiuqingyuan' }}创建于{{ formatTime(project.createdTime) }}</small>
          </span>
        </button>
        <div v-if="!projectList.length && !projectLoading" class="empty-projects">
          <i class="el-icon-folder-opened" />
          <span>暂无项目</span>
        </div>
        <div v-if="projectList.length" class="project-end">--没有更多项目了--</div>
      </div>

      <el-button class="sidebar-create" type="primary" @click="openCreateProject" v-hasPermi="['bid:quality:add']">
        新建项目
      </el-button>
    </aside>

    <main class="quality-main">
      <template v-if="!activeProject">
        <section class="quality-hero">
          <div class="hero-glow" />
          <h1>投标文件<span>深度解析报告</span></h1>
          <p>四步极简操作，自动生成专业质检报告</p>
          <div class="hero-steps">
            <div v-for="(step, index) in heroSteps" :key="step.title" class="hero-step">
              <div class="hero-visual" :class="'theme-' + index">
                <i :class="step.icon" />
                <span v-for="line in 4" :key="line" />
              </div>
              <strong>{{ step.title }}</strong>
              <em v-if="index < heroSteps.length - 1" class="step-arrow">→</em>
            </div>
          </div>
          <el-button class="hero-create" type="primary" @click="openCreateProject" v-hasPermi="['bid:quality:add']">
            + 新建质检项目
          </el-button>
        </section>
      </template>

      <template v-else>
        <nav class="quality-stepper">
          <span
            v-for="step in workSteps"
            :key="step.key"
            class="work-step"
            :class="{ done: step.done, current: step.current }"
          >
            <i v-if="step.done" class="el-icon-check" />
            <b v-else>{{ step.index }}</b>
            {{ step.title }}
          </span>
        </nav>

        <section class="workspace-shell" v-loading="detailLoading">
          <div class="workspace-left" :class="{ wide: !hasFramework }">
            <section class="project-summary">
              <div class="section-heading">
                <span />
                <strong>项目简介</strong>
              </div>
              <el-dropdown trigger="click" class="summary-action">
                <span>查看项目概览<i class="el-icon-arrow-down el-icon--right" /></span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>{{ activeProject.title }}</el-dropdown-item>
                  <el-dropdown-item>{{ tenderFileName }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <div class="summary-box">
                {{ projectIntro }}
              </div>
            </section>

            <section v-if="!hasFramework" class="framework-empty">
              <el-alert title="暂无质检框架，请智能提取或上传质检框架" type="warning" :closable="false" show-icon />
              <div class="framework-upload-panel">
                <el-button class="extract-button" type="primary" plain :loading="extracting" @click="handleSmartExtract">
                  <i class="el-icon-magic-stick" /> 智能提取
                </el-button>
                <div class="or-line"><span>或</span></div>
                <el-upload
                  ref="frameworkFirstUpload"
                  drag
                  action="#"
                  accept=".xls,.xlsx"
                  :auto-upload="false"
                  :limit="1"
                  :show-file-list="false"
                  :on-change="handleFrameworkFileChange"
                >
                  <i class="el-icon-document" />
                  <div class="el-upload__text">将质检框架拖拽至此区域，或 <span>点击添加</span></div>
                  <div class="el-upload__tip" slot="tip">支持.xlsx,.xls 格式，仅支持1个文件，单个文件最大50MB</div>
                </el-upload>
              </div>
            </section>

            <section v-else class="check-workbench">
              <div class="section-toolbar">
                <div class="section-heading">
                  <span />
                  <strong>检查项目</strong>
                </div>
                <div class="toolbar-actions">
                  <el-button size="small" icon="el-icon-plus" @click="openAddItem" v-hasPermi="['bid:quality:item']">新增检查项</el-button>
                  <el-upload
                    action="#"
                    accept=".xls,.xlsx"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleFrameworkFileChange"
                  >
                    <el-button size="small" icon="el-icon-upload">上传质检框架</el-button>
                  </el-upload>
                  <el-button size="small" type="primary" plain icon="el-icon-download" @click="handleDownloadFramework" v-hasPermi="['bid:quality:export']">
                    下载质检框架
                  </el-button>
                </div>
              </div>

              <div class="check-tabs">
                <button
                  v-for="tab in checkTabs"
                  :key="tab.code"
                  type="button"
                  :class="{ active: activeTab === tab.code }"
                  @click="activeTab = tab.code"
                >
                  <span v-if="!tab.count" class="red-dot" />
                  {{ tab.name }}（{{ tab.count ? tab.count : '空' }}）
                </button>
              </div>

              <div class="select-row">
                <el-checkbox :value="allCurrentEnabled" @change="handleSelectCurrentAll" />
                <span>共 {{ currentItems.length }} 个检查项，已启用 <b>{{ currentEnabledCount }}</b> 个</span>
              </div>

              <div class="check-list">
                <div v-for="(item, index) in currentItems" :key="item.id" class="check-item">
                  <el-checkbox v-model="item.enabledBool" @change="value => handleToggleItem(item, value)" />
                  <strong>({{ index + 1 }})</strong>
                  <span>{{ item.itemName }}</span>
                  <em>{{ severityText(item.severity) }}</em>
                  <button type="button" class="icon-action edit" @click="openEditItem(item)">
                    <i class="el-icon-edit-outline" />
                  </button>
                  <button type="button" class="icon-action danger" @click="handleDeleteItem(item)">
                    <i class="el-icon-delete" />
                  </button>
                </div>
                <div v-if="!currentItems.length" class="empty-checks">
                  <i class="el-icon-document-checked" />
                  <span>当前分类暂无检查项</span>
                </div>
              </div>
            </section>
          </div>

          <aside v-if="hasFramework" class="workspace-right">
            <div class="section-heading">
              <span />
              <strong>质检报告</strong>
            </div>
            <div v-if="!taskList.length && !runningVersion" class="report-empty">
              <div class="empty-illustration">检</div>
              <p>待检测清单已就绪，新建质检版本上传投标文件即可开始质检任务</p>
              <el-button type="primary" @click="openVersionDialog" v-hasPermi="['bid:quality:add']">+ 新建质检版本</el-button>
            </div>
            <div v-else class="report-list">
              <div v-if="runningVersion" class="report-row running">
                <time>{{ nowTime }}</time>
                <span class="timeline-dot pending" />
                <div class="report-file">
                  <i class="word-icon">W</i>
                  <div>
                    <strong>{{ runningFileName }}</strong>
                    <small><i class="el-icon-loading" /> 查询排队中</small>
                  </div>
                </div>
              </div>
              <div v-for="task in taskList" :key="task.id" class="report-row">
                <time>{{ formatDate(task.createTime) }}</time>
                <span class="timeline-dot" :class="{ success: task.status === 'success' }" />
                <div class="report-file">
                  <i class="word-icon">W</i>
                  <div>
                    <strong>{{ task.taskName }}</strong>
                    <small :class="task.status">{{ taskStatusText(task.status) }}</small>
                  </div>
                  <div class="report-actions">
                    <el-button size="mini" plain icon="el-icon-view" :disabled="task.status !== 'success'" @click="openResultDrawer(task)">查看详情</el-button>
                    <el-button size="mini" plain icon="el-icon-download" :disabled="task.status !== 'success'" @click="handleExportReport(task)">下载报告</el-button>
                  </div>
                </div>
              </div>
              <el-button class="new-version-bottom" type="primary" :loading="runningVersion" @click="openVersionDialog" v-hasPermi="['bid:quality:add']">
                + 新建质检版本
              </el-button>
            </div>
          </aside>
        </section>
      </template>
    </main>

    <el-dialog
      title="上传招标文件"
      :visible.sync="createProjectVisible"
      width="760px"
      custom-class="quality-upload-dialog"
      :close-on-click-modal="false"
      @closed="resetCreateProject"
    >
      <el-upload
        ref="projectUpload"
        drag
        action="#"
        accept=".doc,.docx,.pdf"
        :auto-upload="false"
        :limit="1"
        :file-list="projectFileList"
        :on-change="handleProjectFileChange"
        :on-remove="handleProjectFileRemove"
        :on-exceed="handleUploadExceed"
      >
        <i class="el-icon-document" />
        <div class="el-upload__text">将文件拖拽至此区域，或 <span>点击添加</span></div>
        <div class="el-upload__tip" slot="tip">支持.docx,.pdf格式，仅支持1个文件，单个文件最大50MB</div>
      </el-upload>
      <div slot="footer">
        <el-button @click="createProjectVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingProject" @click="submitCreateProject">开始读标</el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="editingItem ? '编辑检查项' : '新增检查项'"
      :visible.sync="itemDialogVisible"
      width="760px"
      :close-on-click-modal="false"
      @closed="resetItemDialog"
    >
      <el-form ref="itemForm" :model="itemForm" :rules="itemRules" label-width="96px">
        <el-form-item label="检查分类">
          <el-select v-model="itemForm.itemType" style="width: 220px">
            <el-option v-for="tab in checkTabOptions" :key="tab.code" :label="tab.name" :value="tab.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查项" prop="itemName">
          <el-input
            v-model="itemForm.itemName"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入检查项题目内容，例如：检查文件是否包含违禁词明？"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">确认添加</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="新建质检项目"
      :visible.sync="versionDialogVisible"
      width="820px"
      custom-class="quality-version-dialog"
      :close-on-click-modal="false"
      @closed="resetVersionDialog"
    >
      <el-upload
        ref="versionUpload"
        drag
        action="#"
        accept=".doc,.docx"
        :auto-upload="false"
        :limit="1"
        :file-list="versionFileList"
        :on-change="handleVersionFileChange"
        :on-remove="handleVersionFileRemove"
        :on-exceed="handleUploadExceed"
      >
        <i class="word-big">W</i>
        <div class="el-upload__text">将文件拖拽至此区域，或 <span>点击添加</span></div>
        <div class="el-upload__tip" slot="tip">支持.doc,.docx格式，仅支持1个文件，单个文件最大300MB</div>
      </el-upload>
      <div class="agreement-row">
        <el-checkbox v-model="versionAgreed" />
        <span>我已阅读并同意《用户隐私信息收集与使用协议》</span>
      </div>
      <div slot="footer">
        <el-button @click="versionDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!versionAgreed || !versionFile" :loading="runningVersion" @click="submitVersion">
          开始质检
        </el-button>
      </div>
    </el-dialog>

    <el-drawer
      title="检查结果"
      :visible.sync="resultDrawerVisible"
      direction="rtl"
      size="44%"
      custom-class="quality-result-drawer"
    >
      <div class="drawer-tabs">
        <button
          v-for="tab in checkTabOptions"
          :key="tab.code"
          type="button"
          :class="{ active: resultActiveTab === tab.code }"
          @click="resultActiveTab = tab.code"
        >
          {{ tab.name }}
        </button>
      </div>
      <div class="drawer-body" v-loading="resultLoading">
        <div v-for="(item, index) in currentResults" :key="item.id" class="result-card">
          <h3>{{ index + 1 }}. {{ item.issueTitle || item.itemName }}</h3>
          <p><b>检查结果：</b><span :class="item.status">{{ resultStatusText(item.status) }}</span></p>
          <p v-for="line in splitSuggestion(item.suggestion)" :key="line">{{ line }}</p>
        </div>
        <div v-if="!currentResults.length && !resultLoading" class="empty-checks">
          <i class="el-icon-document-checked" />
          <span>当前分类暂无检查结果</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import {
  addQualityItem,
  createQualityProject,
  createQualityVersion,
  deleteQualityItem,
  downloadQualityFramework,
  exportQualityReport,
  extractQualityFramework,
  getQualityProjectDetail,
  listQualityProjects,
  listQualityResults,
  updateQualityItem,
  uploadQualityFramework
} from '@/api/bid/quality'

const CHECK_TABS = [
  { code: 'form_review', name: '形式审查' },
  { code: 'qualification_review', name: '资格审查' },
  { code: 'response_review', name: '响应审查' },
  { code: 'reject_review', name: '废标项' }
]

export default {
  name: 'BidQualityIndex',
  data() {
    return {
      projectLoading: false,
      detailLoading: false,
      extracting: false,
      creatingProject: false,
      runningVersion: false,
      projectList: [],
      activeProject: null,
      tenderFile: null,
      framework: null,
      itemList: [],
      taskList: [],
      activeTab: 'form_review',
      queryParams: { pageNum: 1, pageSize: 100, title: '' },
      createProjectVisible: false,
      projectFile: null,
      projectFileList: [],
      itemDialogVisible: false,
      editingItem: null,
      itemForm: { frameworkId: null, itemType: 'form_review', itemName: '' },
      itemRules: { itemName: [{ required: true, message: '请输入检查项', trigger: 'blur' }] },
      versionDialogVisible: false,
      versionFile: null,
      versionFileList: [],
      versionAgreed: false,
      runningFileName: '',
      resultDrawerVisible: false,
      resultLoading: false,
      resultActiveTab: 'form_review',
      resultList: [],
      nowTime: '',
      qualityPollTimer: null,
      qualityPolling: false,
      heroSteps: [
        { title: '第一步：上传招标文件', icon: 'el-icon-upload' },
        { title: '第二步：解析招标信息', icon: 'el-icon-document-checked' },
        { title: '第三步：上传投标文件', icon: 'el-icon-upload2' },
        { title: '第四步：获取检查报告', icon: 'el-icon-finished' }
      ]
    }
  },
  computed: {
    hasFramework() {
      return !!(this.framework && this.framework.id)
    },
    tenderFileName() {
      return this.tenderFile ? this.tenderFile.originalName : '未上传招标文件'
    },
    projectIntro() {
      if (!this.activeProject) return ''
      return this.activeProject.note && this.activeProject.note !== 'AI质检项目'
        ? this.activeProject.note
        : '本项目已上传招标文件，可通过智能提取生成质检框架，或上传已有.xlsx质检框架；完成检查项确认后，上传投标文件生成质检报告。'
    },
    workSteps() {
      const hasProject = !!this.activeProject
      const hasTask = this.taskList.length > 0 || this.runningVersion
      const hasSuccess = this.taskList.some(item => item.status === 'success')
      return [
        { key: 'uploadTender', index: 1, title: '上传招标文件', done: hasProject, current: !hasProject },
        { key: 'parseTender', index: 2, title: '解析招标信息', done: this.hasFramework, current: hasProject && !this.hasFramework },
        { key: 'uploadBid', index: 3, title: '上传投标文件', done: hasTask, current: this.hasFramework && !hasTask },
        { key: 'report', index: 4, title: '获取质检报告', done: hasSuccess, current: hasTask && !hasSuccess }
      ]
    },
    checkTabOptions() {
      return CHECK_TABS
    },
    checkTabs() {
      return CHECK_TABS.map(tab => ({
        ...tab,
        count: this.itemList.filter(item => item.itemType === tab.code).length
      }))
    },
    currentItems() {
      return this.itemList.filter(item => item.itemType === this.activeTab)
    },
    currentEnabledCount() {
      return this.currentItems.filter(item => item.enabledBool).length
    },
    allCurrentEnabled() {
      return this.currentItems.length > 0 && this.currentEnabledCount === this.currentItems.length
    },
    currentResults() {
      return this.resultList.filter(item => (item.issueType || item.itemType) === this.resultActiveTab)
    }
  },
  created() {
    this.loadProjects()
    this.nowTime = this.formatClock(new Date())
  },
  beforeDestroy() {
    this.stopTaskPolling()
  },
  methods: {
    loadProjects() {
      this.projectLoading = true
      return listQualityProjects(this.queryParams).then(res => {
        this.projectList = res.rows || []
        if (this.activeProject) {
          const current = this.projectList.find(item => item.id === this.activeProject.id)
          if (current) this.activeProject = current
        }
      }).finally(() => {
        this.projectLoading = false
      })
    },
    openProject(project) {
      this.stopTaskPolling()
      this.activeProject = project
      this.loadProjectDetail(project.id)
    },
    loadProjectDetail(bidId) {
      this.detailLoading = true
      return getQualityProjectDetail(bidId).then(res => {
        this.applyProjectDetail(res.data || {})
      }).finally(() => {
        this.detailLoading = false
      })
    },
    applyProjectDetail(data) {
      this.tenderFile = data.tenderFile || null
      this.framework = data.framework || null
      this.itemList = (data.items || []).map(item => ({ ...item, enabledBool: item.enabled === '1' }))
      this.taskList = data.tasks || []
      if (this.hasFramework) {
        const firstTab = CHECK_TABS.find(tab => this.itemList.some(item => item.itemType === tab.code))
        this.activeTab = firstTab ? firstTab.code : 'form_review'
      }
      this.refreshTaskPolling()
    },
    openCreateProject() {
      this.createProjectVisible = true
    },
    handleProjectFileChange(file, fileList) {
      if (!this.validateClientFile(file.raw, ['doc', 'docx', 'pdf'], 50)) {
        this.projectFileList = []
        this.projectFile = null
        return
      }
      this.projectFile = file.raw
      this.projectFileList = fileList.slice(-1)
    },
    handleProjectFileRemove() {
      this.projectFile = null
      this.projectFileList = []
    },
    submitCreateProject() {
      if (!this.projectFile) {
        this.$modal.msgWarning('请先上传招标文件')
        return
      }
      this.creatingProject = true
      createQualityProject(this.projectFile).then(res => {
        this.$modal.msgSuccess('项目创建成功')
        this.createProjectVisible = false
        const bid = res.data && res.data.bid
        return this.loadProjects().then(() => {
          if (bid) this.openProject(bid)
        })
      }).finally(() => {
        this.creatingProject = false
      })
    },
    resetCreateProject() {
      this.projectFile = null
      this.projectFileList = []
      this.$refs.projectUpload && this.$refs.projectUpload.clearFiles()
    },
    handleSmartExtract() {
      if (!this.activeProject) return
      this.extracting = true
      extractQualityFramework(this.activeProject.id).then(() => {
        this.$modal.msgSuccess('质检框架提取完成')
        return this.loadProjectDetail(this.activeProject.id)
      }).finally(() => {
        this.extracting = false
      })
    },
    handleFrameworkFileChange(file) {
      if (!this.activeProject) return
      if (!this.validateClientFile(file.raw, ['xls', 'xlsx'], 50)) return
      this.detailLoading = true
      uploadQualityFramework(this.activeProject.id, file.raw).then(() => {
        this.$modal.msgSuccess('质检框架上传成功')
        return this.loadProjectDetail(this.activeProject.id)
      }).finally(() => {
        this.detailLoading = false
      })
    },
    openAddItem() {
      if (!this.framework) {
        this.$modal.msgWarning('请先生成质检框架')
        return
      }
      this.editingItem = null
      this.itemForm = { frameworkId: this.framework.id, itemType: this.activeTab, itemName: '' }
      this.itemDialogVisible = true
    },
    openEditItem(item) {
      this.editingItem = item
      this.itemForm = {
        id: item.id,
        frameworkId: item.frameworkId,
        itemType: item.itemType,
        itemName: item.itemName,
        checkRule: item.checkRule,
        enabled: item.enabled
      }
      this.itemDialogVisible = true
    },
    submitItem() {
      this.$refs.itemForm.validate(valid => {
        if (!valid) return
        const data = {
          ...this.itemForm,
          checkRule: this.itemForm.itemName,
          enabled: '1'
        }
        const request = this.editingItem ? updateQualityItem(data) : addQualityItem(data)
        request.then(() => {
          this.$modal.msgSuccess(this.editingItem ? '检查项已更新' : '检查项已添加')
          this.itemDialogVisible = false
          this.loadProjectDetail(this.activeProject.id)
        })
      })
    },
    resetItemDialog() {
      this.editingItem = null
      this.itemForm = { frameworkId: null, itemType: this.activeTab, itemName: '' }
      this.$refs.itemForm && this.$refs.itemForm.clearValidate()
    },
    handleToggleItem(item, value) {
      updateQualityItem({ id: item.id, frameworkId: item.frameworkId, enabled: value ? '1' : '0' }).then(() => {
        item.enabled = value ? '1' : '0'
      }).catch(() => {
        item.enabledBool = !value
      })
    },
    handleSelectCurrentAll(value) {
      const updates = this.currentItems.map(item => {
        item.enabledBool = value
        item.enabled = value ? '1' : '0'
        return updateQualityItem({ id: item.id, frameworkId: item.frameworkId, enabled: item.enabled })
      })
      Promise.all(updates).then(() => {
        this.$modal.msgSuccess(value ? '已启用当前分类全部检查项' : '已取消当前分类全部检查项')
      })
    },
    handleDeleteItem(item) {
      this.$modal.confirm('确认删除该检查项吗？').then(() => deleteQualityItem(item.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.loadProjectDetail(this.activeProject.id)
      }).catch(() => {})
    },
    handleDownloadFramework() {
      if (!this.framework) return
      downloadQualityFramework(this.framework.id).then(blob => {
        saveAs(blob, (this.framework.frameworkName || '质检框架') + '.xlsx')
      })
    },
    openVersionDialog() {
      this.versionDialogVisible = true
    },
    handleVersionFileChange(file, fileList) {
      if (!this.validateClientFile(file.raw, ['doc', 'docx'], 300)) {
        this.versionFile = null
        this.versionFileList = []
        return
      }
      this.versionFile = file.raw
      this.versionFileList = fileList.slice(-1)
    },
    handleVersionFileRemove() {
      this.versionFile = null
      this.versionFileList = []
    },
    submitVersion() {
      if (!this.versionFile || !this.framework) return
      this.runningVersion = true
      this.runningFileName = this.versionFile.name
      this.nowTime = this.formatClock(new Date())
      this.versionDialogVisible = false
      createQualityVersion({
        bidId: this.activeProject.id,
        frameworkId: this.framework.id,
        file: this.versionFile
      }).then(() => {
        this.$modal.msgSuccess('质检任务已进入队列')
        return this.loadProjectDetail(this.activeProject.id)
      }).finally(() => {
        this.runningVersion = false
        this.runningFileName = ''
      })
    },
    refreshTaskPolling() {
      const hasPending = this.taskList.some(task => task.status === 'waiting' || task.status === 'running')
      if (hasPending) {
        this.startTaskPolling()
      } else {
        this.stopTaskPolling()
      }
    },
    startTaskPolling() {
      if (this.qualityPollTimer || !this.activeProject) return
      this.qualityPollTimer = window.setInterval(() => {
        if (!this.activeProject || this.qualityPolling) return
        this.qualityPolling = true
        getQualityProjectDetail(this.activeProject.id).then(res => {
          this.applyProjectDetail(res.data || {})
        }).finally(() => {
          this.qualityPolling = false
        })
      }, 3000)
    },
    stopTaskPolling() {
      if (this.qualityPollTimer) {
        window.clearInterval(this.qualityPollTimer)
        this.qualityPollTimer = null
      }
      this.qualityPolling = false
    },
    resetVersionDialog() {
      this.versionFile = null
      this.versionFileList = []
      this.versionAgreed = false
      this.$refs.versionUpload && this.$refs.versionUpload.clearFiles()
    },
    openResultDrawer(task) {
      this.resultDrawerVisible = true
      this.resultActiveTab = this.activeTab
      this.resultLoading = true
      listQualityResults({ taskId: task.id, pageNum: 1, pageSize: 500 }).then(res => {
        this.resultList = res.rows || []
        const firstTab = CHECK_TABS.find(tab => this.resultList.some(item => (item.issueType || item.itemType) === tab.code))
        this.resultActiveTab = firstTab ? firstTab.code : 'form_review'
      }).finally(() => {
        this.resultLoading = false
      })
    },
    handleExportReport(task) {
      exportQualityReport(task.id).then(() => {
        this.$modal.msgSuccess('报告已生成，可在下载中心查看')
      })
    },
    validateClientFile(file, extensions, maxMb) {
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      if (!extensions.includes(ext)) {
        this.$modal.msgWarning('文件格式不支持')
        return false
      }
      if (file.size > maxMb * 1024 * 1024) {
        this.$modal.msgWarning('文件大小超出限制')
        return false
      }
      return true
    },
    handleUploadExceed() {
      this.$modal.msgWarning('仅支持上传1个文件')
    },
    severityText(severity) {
      return severity === 'error' ? '必须满足' : severity === 'info' ? '建议检查' : '必须满足'
    },
    taskStatusText(status) {
      const map = { waiting: '查询排队中', running: '查询排队中', success: '质检成功', fail: '质检失败' }
      return map[status] || '查询排队中'
    },
    resultStatusText(status) {
      const map = { pass: '包含', fail: '不包含', uncertain: '无法判断' }
      return map[status] || status || '无法判断'
    },
    splitSuggestion(value) {
      return (value || '').split('\n').filter(Boolean)
    },
    formatTime(value) {
      if (!value) return '-'
      return String(value).replace('T', ' ').slice(0, 10)
    },
    formatDate(value) {
      if (!value) return '--'
      return String(value).replace('T', ' ').slice(5, 16)
    },
    formatClock(date) {
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      const second = String(date.getSeconds()).padStart(2, '0')
      return `${month}-${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style scoped>
.quality-page {
  display: flex;
  height: calc(100vh - 84px);
  min-height: 760px;
  background: #f3f6fb;
  color: #202938;
  overflow: hidden;
}

.quality-sidebar {
  position: relative;
  width: 292px;
  flex: 0 0 292px;
  padding: 14px 12px 70px;
  background: #fff;
  border-right: 1px solid #e5eaf3;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 18px;
  font-weight: 700;
}

.project-search {
  margin-bottom: 16px;
}

.project-scroll {
  height: calc(100% - 86px);
  overflow-y: auto;
}

.project-card {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 82px;
  margin-bottom: 8px;
  padding: 12px 14px;
  border: 1px solid #dfe5ef;
  border-radius: 6px;
  background: #fff;
  color: #3b4556;
  text-align: left;
  cursor: pointer;
}

.project-card.active {
  border-color: #2f68ff;
  color: #1f62ff;
  box-shadow: inset 3px 0 0 #2f68ff;
}

.project-card i {
  font-size: 16px;
}

.project-text {
  min-width: 0;
}

.project-text strong,
.project-text small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-text strong {
  font-size: 15px;
}

.project-text small {
  margin-top: 8px;
  color: #6f7a8b;
}

.empty-projects,
.empty-checks {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 180px;
  color: #9aa5b5;
}

.project-end {
  padding: 18px 0;
  text-align: center;
  color: #8b95a5;
}

.sidebar-create {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 12px;
  width: calc(100% - 24px);
  height: 44px;
  border: 0;
  background: linear-gradient(90deg, #2f6bff, #7a4dff);
}

.quality-main {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.quality-hero {
  position: relative;
  height: 100%;
  padding-top: 150px;
  overflow: hidden;
  text-align: center;
  background:
    radial-gradient(circle at 78% 0%, rgba(186, 226, 255, .62) 0, rgba(223, 243, 255, .5) 26%, rgba(255, 255, 255, 0) 48%),
    radial-gradient(circle at 36% 0%, rgba(226, 243, 255, .72) 0, rgba(241, 250, 255, .58) 30%, rgba(255, 255, 255, 0) 54%),
    linear-gradient(180deg, #f2fbff 0%, #f7fcff 22%, #fff 48%, #fff 100%);
  border: 1px solid #fff;
}

.hero-glow {
  position: absolute;
  inset: 0 0 auto;
  height: 260px;
  background: linear-gradient(180deg, rgba(213, 239, 255, .45) 0%, rgba(255, 255, 255, 0) 78%);
  pointer-events: none;
}

.quality-hero h1 {
  position: relative;
  margin: 0;
  font-size: 32px;
  line-height: 1.3;
}

.quality-hero h1 span {
  margin-left: 4px;
  color: #2367ff;
}

.quality-hero p {
  position: relative;
  margin: 12px 0 54px;
  color: #7d8796;
  font-size: 16px;
}

.hero-steps {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 44px;
}

.hero-step {
  position: relative;
  width: 230px;
}

.hero-step strong {
  display: block;
  margin-top: 14px;
  font-size: 16px;
  font-weight: 500;
}

.hero-visual {
  height: 168px;
  padding: 28px 38px;
  border: 1px solid #fff;
  border-radius: 8px;
  background: #dbeaff;
  box-shadow: 0 16px 32px rgba(28, 95, 180, .08);
}

.hero-visual i {
  display: block;
  margin-bottom: 18px;
  font-size: 44px;
  color: #2f76ff;
}

.hero-visual span {
  display: block;
  height: 10px;
  margin: 10px auto;
  border-radius: 999px;
  background: rgba(255,255,255,.92);
}

.theme-1 { background: #eadbff; }
.theme-2 { background: #d9f6e7; }
.theme-3 { background: #f8ead7; }

.step-arrow {
  position: absolute;
  right: -34px;
  top: 70px;
  color: #d3dceb;
  font-size: 34px;
  font-style: normal;
}

.hero-create {
  width: 224px;
  height: 44px;
  margin-top: 110px;
  border: 0;
  background: linear-gradient(90deg, #2f6bff, #7a4dff);
}

.quality-stepper {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  height: 54px;
  background: #fff;
  border-bottom: 1px solid #e5eaf3;
}

.work-step {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #1f2937;
  font-weight: 600;
}

.work-step::after {
  content: '';
  position: absolute;
  right: 0;
  width: 58%;
  height: 1px;
  background: #dfe5ef;
  transform: translateX(50%);
}

.work-step:last-child::after {
  display: none;
}

.work-step i,
.work-step b {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #d8deea;
  background: #fff;
  font-style: normal;
}

.work-step.done {
  color: #2367ff;
}

.work-step.done i {
  border-color: #2367ff;
  background: #2367ff;
  color: #fff;
}

.work-step.current b {
  border-color: #2367ff;
  color: #2367ff;
}

.workspace-shell {
  display: flex;
  height: calc(100% - 54px);
  background: #fff;
}

.workspace-left {
  width: 50%;
  min-width: 620px;
  padding: 24px 22px;
  overflow-y: auto;
  border-right: 1px solid #e5eaf3;
}

.workspace-left.wide {
  width: 100%;
  border-right: 0;
}

.workspace-right {
  flex: 1;
  min-width: 420px;
  padding: 24px 30px;
  overflow-y: auto;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
}

.section-heading span {
  width: 4px;
  height: 18px;
  border-radius: 3px;
  background: #2f68ff;
}

.project-summary {
  position: relative;
}

.summary-action {
  position: absolute;
  top: 2px;
  right: 0;
  color: #7d8796;
  cursor: pointer;
}

.summary-box {
  min-height: 112px;
  margin-top: 18px;
  padding: 14px 16px;
  color: #8a94a4;
  line-height: 1.8;
  background: #f6f8fc;
  border: 1px solid #dfe5ef;
  border-radius: 4px;
}

.framework-empty {
  max-width: 1360px;
  margin: 52px auto 0;
}

.framework-upload-panel {
  margin-top: 16px;
  padding: 14px;
  border: 1px dashed #d6deeb;
  border-radius: 4px;
}

.extract-button {
  width: 100%;
  height: 36px;
  color: #425eff;
  border: 0;
  background: #f0f4ff;
}

.or-line {
  display: flex;
  align-items: center;
  margin: 18px 0;
  color: #8b95a5;
}

.or-line::before,
.or-line::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #dfe5ef;
}

.or-line span {
  padding: 0 18px;
}

.check-workbench {
  margin-top: 20px;
}

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.check-tabs,
.drawer-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin-top: 24px;
  border-bottom: 2px solid #4d70ff;
}

.check-tabs button,
.drawer-tabs button {
  height: 46px;
  border: 0;
  background: #fff;
  color: #303847;
  font-size: 16px;
  cursor: pointer;
}

.check-tabs button.active,
.drawer-tabs button.active {
  color: #2868ff;
  border-bottom: 4px solid #4d70ff;
  font-weight: 700;
}

.red-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 8px;
  border-radius: 50%;
  background: #f00;
  vertical-align: middle;
}

.select-row {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 68px;
  padding: 0 16px;
  color: #7a8493;
  border-bottom: 1px solid #f0f2f6;
}

.select-row b {
  color: #2f68ff;
}

.check-list {
  padding-right: 4px;
}

.check-item {
  display: grid;
  grid-template-columns: 26px auto 1fr auto 34px 34px;
  align-items: center;
  gap: 10px;
  min-height: 72px;
  margin-bottom: 14px;
  padding: 12px 16px;
  border: 1px solid #dfe5ef;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 4px 10px rgba(31, 41, 55, .03);
}

.check-item strong {
  color: #2367ff;
}

.check-item span {
  line-height: 1.6;
  font-weight: 600;
}

.check-item em {
  padding: 4px 8px;
  color: #f59e0b;
  border: 1px solid #fde6b2;
  border-radius: 4px;
  background: #fff8eb;
  font-style: normal;
}

.icon-action {
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
}

.icon-action.edit {
  color: #2367ff;
}

.icon-action.danger {
  color: #e5261f;
}

.report-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 560px;
  color: #737d8d;
  text-align: center;
}

.empty-illustration {
  width: 188px;
  height: 122px;
  margin-bottom: 34px;
  border-radius: 10px;
  background: #edf3ff;
  color: #d5def0;
  font-size: 86px;
  line-height: 122px;
  font-weight: 800;
}

.report-empty p {
  max-width: 360px;
  line-height: 1.8;
}

.report-empty .el-button,
.new-version-bottom {
  width: 196px;
  height: 46px;
  margin-top: 28px;
  border: 0;
  background: linear-gradient(90deg, #2f6bff, #7a4dff);
}

.report-list {
  margin-top: 22px;
  min-height: 560px;
}

.report-row {
  display: grid;
  grid-template-columns: 86px 18px 1fr;
  gap: 14px;
  align-items: start;
  margin-bottom: 18px;
}

.report-row time {
  color: #8b95a5;
  line-height: 1.4;
  text-align: right;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  margin-top: 10px;
  border-radius: 50%;
  background: #fbd4a2;
}

.timeline-dot.success {
  background: #26dc37;
}

.timeline-dot.pending {
  background: #fbd4a2;
}

.report-file {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 92px;
  padding: 18px 22px;
  border-radius: 4px;
  background: #edf6ff;
}

.word-icon,
.word-big {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 54px;
  border-radius: 6px;
  background: #4d70e8;
  color: #fff;
  font-weight: 800;
}

.word-big {
  margin: 0 auto 18px;
  font-size: 32px;
}

.report-file strong,
.report-file small {
  display: block;
}

.report-file small {
  margin-top: 8px;
  color: #7d8796;
}

.report-file small.success {
  color: #2ed32e;
  font-weight: 700;
}

.report-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.new-version-bottom {
  display: block;
  margin: 48px auto 0;
}

.agreement-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  color: #8b95a5;
}

.drawer-tabs {
  margin-top: 0;
}

.drawer-body {
  height: calc(100vh - 130px);
  padding: 28px 34px 48px;
  overflow-y: auto;
}

.result-card {
  margin-bottom: 24px;
  padding: 28px 24px;
  border: 1px solid #e2e7f0;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(31, 41, 55, .04);
}

.result-card h3 {
  margin: 0 0 20px;
  color: #1685ff;
  font-size: 17px;
}

.result-card p {
  margin: 12px 0;
  color: #556171;
  line-height: 1.8;
}

.result-card .pass {
  color: #23b14d;
  font-weight: 700;
}

.result-card .fail {
  color: #1685ff;
  font-weight: 700;
}

.result-card .uncertain {
  color: #f59e0b;
  font-weight: 700;
}

::v-deep .quality-upload-dialog .el-upload,
::v-deep .quality-upload-dialog .el-upload-dragger,
::v-deep .quality-version-dialog .el-upload,
::v-deep .quality-version-dialog .el-upload-dragger {
  width: 100%;
}

::v-deep .quality-upload-dialog .el-upload-dragger,
::v-deep .quality-version-dialog .el-upload-dragger {
  height: 334px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

::v-deep .framework-upload-panel .el-upload,
::v-deep .framework-upload-panel .el-upload-dragger {
  width: 100%;
}

::v-deep .framework-upload-panel .el-upload-dragger {
  height: 144px;
}

@media (max-width: 1440px) {
  .workspace-left {
    min-width: 560px;
  }

  .toolbar-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .hero-step {
    width: 190px;
  }
}
</style>
