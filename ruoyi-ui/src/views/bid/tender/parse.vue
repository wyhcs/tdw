<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/bid/tender')" content="招标文件解析" />
    <el-card shadow="never" class="mt16">
      <div slot="header" class="card-header">
        <span>招标文件</span>
        <el-button type="primary" size="mini" :disabled="!currentFile" :loading="parsing" @click="handleParse">开始解析</el-button>
      </div>
      <el-table :data="files" size="small">
        <el-table-column label="文件名" prop="originalName" min-width="220" />
        <el-table-column label="类型" prop="fileType" width="100" />
        <el-table-column label="解析状态" prop="parseStatus" width="120" />
        <el-table-column label="上传时间" prop="createTime" width="160" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt16">
      <div slot="header" class="card-header">
        <span>解析报告</span>
        <el-button type="success" size="mini" :disabled="!report.id" @click="openEditor">进入编辑器</el-button>
      </div>
      <el-empty v-if="!report.id" description="暂无解析报告，请先开始解析" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="项目名称">{{ report.projectName }}</el-descriptions-item>
        <el-descriptions-item label="需求摘要">{{ report.requirementSummary }}</el-descriptions-item>
        <el-descriptions-item label="评分项">{{ report.scoreItems }}</el-descriptions-item>
        <el-descriptions-item label="解析JSON">
          <pre class="report-json">{{ report.reportContent }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getLatestTenderReport, getTenderReportByFile, listTenderFiles, parseTenderFile } from '@/api/bid/tender'

export default {
  name: 'BidTenderParse',
  data() {
    return {
      bidId: undefined,
      fileId: undefined,
      files: [],
      report: {},
      parsing: false
    }
  },
  computed: {
    currentFile() {
      if (this.fileId) return this.files.find(item => String(item.id) === String(this.fileId))
      return this.files[0]
    }
  },
  created() {
    this.bidId = this.$route.query.bidId
    this.fileId = this.$route.query.fileId
    this.loadFiles()
  },
  methods: {
    loadFiles() {
      listTenderFiles(this.bidId).then(res => {
        this.files = res.data || []
        if (!this.fileId && this.files.length) this.fileId = this.files[0].id
        this.loadReport()
      })
    },
    loadReport() {
      const action = this.fileId ? getTenderReportByFile(this.fileId) : getLatestTenderReport(this.bidId)
      action.then(res => { this.report = res.data || {} })
    },
    handleParse() {
      if (!this.currentFile) return
      this.parsing = true
      parseTenderFile(this.currentFile.id).then(res => {
        this.report = res.data || {}
        this.$modal.msgSuccess('解析完成')
        this.loadFiles()
      }).finally(() => { this.parsing = false })
    },
    openEditor() {
      this.$router.push({ path: '/bid/tender/editor', query: { bidId: this.bidId, parseReportId: this.report.id }})
    }
  }
}
</script>

<style scoped>
.mt16 { margin-top: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.report-json { margin: 0; white-space: pre-wrap; }
</style>
