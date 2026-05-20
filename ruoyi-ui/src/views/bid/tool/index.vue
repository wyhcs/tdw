<template>
  <div class="tool-page">
    <header class="page-title">
      <span class="title-mark"></span>
      <h1>AI工具</h1>
    </header>

    <section class="converter-panel">
      <aside class="tool-card">
        <div class="convert-illustration">
          <div class="file-icon pdf">
            <span>PDF</span>
          </div>
          <i class="el-icon-right arrow"></i>
          <div class="file-icon word">
            <span>W</span>
          </div>
        </div>
        <h2>PDF转Word</h2>
        <p>智能识别PDF，导出高保真可编辑Word</p>
      </aside>

      <el-upload
        :key="uploadKey"
        class="upload-zone"
        drag
        action="#"
        accept=".pdf"
        :limit="1"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleUpload"
        :on-exceed="handleExceed"
        :disabled="uploading"
        v-hasPermi="['bid:tool:upload']"
      >
        <div class="upload-content" v-loading="uploading" element-loading-text="正在转换PDF">
          <div class="pdf-badge">
            <i class="el-icon-document"></i>
          </div>
          <div class="upload-title">
            <span>点击上传</span>PDF文档，或将文档拖拽至此区域
          </div>
          <div class="upload-tip">文件大小不超过50MB，支持转换以下文件格式：pdf，每次上传1个</div>
        </div>
      </el-upload>
    </section>

    <section class="list-section">
      <div class="list-head">
        <h2>转化列表</h2>
        <div class="query-actions">
          <el-input
            v-model="queryParams.fileName"
            size="small"
            clearable
            placeholder="请输入文件名"
            @keyup.enter.native="handleQuery"
          />
          <el-button type="primary" size="small" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="recordList" class="convert-table">
        <el-table-column label="文件名" min-width="360" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="file-name">
              <i class="el-icon-document"></i>
              <span>{{ scope.row.outputName || scope.row.originalName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="160" align="center">
          <template slot-scope="scope">{{ formatSize(scope.row.outputSize || scope.row.sourceSize) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="190" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot="header">
            <span>操作</span>
            <el-button class="refresh-btn" type="text" icon="el-icon-refresh" @click.stop="getList" />
          </template>
          <template slot-scope="scope">
            <el-button
              type="text"
              icon="el-icon-download"
              size="mini"
              @click="handleDownload(scope.row)"
              v-hasPermi="['bid:tool:download']"
            >下载</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              size="mini"
              class="danger-text"
              @click="handleDelete(scope.row)"
              v-hasPermi="['bid:tool:remove']"
            >删除</el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <div class="empty-state">
            <div class="empty-art">
              <i class="el-icon-document-copy"></i>
              <span></span>
            </div>
            <p>暂无转换信息</p>
          </div>
        </template>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </section>
  </div>
</template>

<script>
import { convertRecordDownloadUrl, deleteConvertRecord, listConvertRecords, uploadPdfToWord } from '@/api/bid/tool'

export default {
  name: 'BidAiTools',
  data() {
    return {
      loading: false,
      uploading: false,
      uploadKey: 1,
      total: 0,
      recordList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileName: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listConvertRecords(this.queryParams).then(res => {
        this.recordList = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleUpload(file) {
      if (!file || !file.raw) return
      if (!this.validateFile(file.raw)) {
        this.uploadKey++
        return
      }
      const form = new FormData()
      form.append('file', file.raw)
      this.uploading = true
      uploadPdfToWord(form).then(() => {
        this.$modal.msgSuccess('转换完成')
        this.queryParams.pageNum = 1
        this.getList()
      }).finally(() => {
        this.uploading = false
        this.uploadKey++
      })
    },
    handleExceed() {
      this.$modal.msgWarning('每次只能上传1个PDF文档')
    },
    validateFile(file) {
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      if (ext !== 'pdf') {
        this.$modal.msgWarning('仅支持pdf格式文件')
        return false
      }
      if (file.size > 50 * 1024 * 1024) {
        this.$modal.msgWarning('文件大小不能超过50MB')
        return false
      }
      return true
    },
    handleDownload(row) {
      window.open(convertRecordDownloadUrl(row.id), '_blank')
    },
    handleDelete(row) {
      const name = row.outputName || row.originalName || '该文件'
      this.$modal.confirm('确认删除转换记录"' + name + '"吗？').then(() => deleteConvertRecord(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    formatSize(size) {
      const value = Number(size || 0)
      if (value < 1024) return value + ' B'
      if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB'
      return (value / 1024 / 1024).toFixed(1) + ' MB'
    }
  }
}
</script>

<style scoped>
.tool-page {
  min-height: calc(100vh - 84px);
  background: #fff;
  padding: 18px 22px 24px;
}

.page-title {
  height: 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e7eaf0;
}

.title-mark {
  width: 5px;
  height: 16px;
  border-radius: 4px;
  background: #2f6bff;
}

.page-title h1 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
}

.converter-panel {
  margin: 22px 32px 28px;
  min-height: 258px;
  display: grid;
  grid-template-columns: 282px minmax(0, 1fr);
  gap: 22px;
  padding: 24px;
  border-radius: 8px;
  background: #f2f5fa;
}

.tool-card {
  min-width: 0;
}

.convert-illustration {
  height: 135px;
  border-radius: 8px;
  background: linear-gradient(135deg, #dfe9fb, #edf3ff);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  overflow: hidden;
}

.file-icon {
  position: relative;
  width: 58px;
  height: 64px;
  border-radius: 8px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 10px 22px rgba(45, 84, 160, 0.16);
}

.file-icon::before,
.pdf-badge::before {
  content: "";
  position: absolute;
  right: 0;
  top: 0;
  width: 18px;
  height: 18px;
  border-radius: 0 8px 0 8px;
  background: rgba(255, 255, 255, 0.45);
}

.file-icon.pdf {
  background: #ff4d5d;
}

.file-icon.word {
  background: #2f7df6;
}

.arrow {
  color: #5d8fe8;
  font-size: 28px;
  font-weight: 700;
}

.tool-card h2 {
  margin: 26px 0 6px;
  color: #1f2937;
  font-size: 20px;
  line-height: 28px;
  font-weight: 500;
}

.tool-card p {
  margin: 0;
  color: #8792a2;
  font-size: 14px;
  line-height: 22px;
}

.upload-zone {
  min-width: 0;
}

.upload-zone /deep/ .el-upload,
.upload-zone /deep/ .el-upload-dragger {
  width: 100%;
  height: 214px;
}

.upload-zone /deep/ .el-upload-dragger {
  border: 1px dashed #d2d9e8;
  border-radius: 4px;
  background: #fff;
}

.upload-zone /deep/ .el-upload-dragger:hover {
  border-color: #2f6bff;
}

.upload-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pdf-badge {
  position: relative;
  width: 58px;
  height: 62px;
  border-radius: 8px;
  background: #ff5562;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 24px rgba(255, 85, 98, 0.18);
}

.pdf-badge .el-icon-document {
  font-size: 32px;
}

.upload-title {
  margin-top: 24px;
  color: #4b5563;
  font-size: 14px;
  line-height: 22px;
}

.upload-title span {
  color: #2f6bff;
}

.upload-tip {
  margin-top: 4px;
  color: #8b95a5;
  font-size: 13px;
  line-height: 20px;
}

.list-section {
  margin: 0 32px;
}

.list-head {
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.list-head h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  line-height: 28px;
  font-weight: 600;
}

.query-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.query-actions .el-input {
  width: 248px;
}

.convert-table {
  border-top: 1px solid #eef1f6;
}

.convert-table /deep/ th.el-table__cell {
  background: #f6f6f7;
  color: #8a93a3;
  font-weight: 500;
}

.file-name {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
}

.file-name i {
  color: #2f6bff;
}

.danger-text {
  color: #f56c6c;
}

.refresh-btn {
  margin-left: 4px;
  padding: 0;
  color: #8a93a3;
}

.empty-state {
  min-height: 330px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #b5bdca;
}

.empty-art {
  position: relative;
  width: 112px;
  height: 96px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f0f4fb, #ffffff);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-art i {
  font-size: 54px;
  color: #dbe4f3;
}

.empty-art span {
  position: absolute;
  right: 22px;
  bottom: 22px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 4px solid #c8dcff;
}

.empty-state p {
  margin: 20px 0 0;
  font-size: 13px;
}

@media (max-width: 960px) {
  .converter-panel {
    margin: 18px 0 24px;
    grid-template-columns: 1fr;
  }

  .list-section {
    margin: 0;
  }

  .list-head {
    align-items: stretch;
    flex-direction: column;
  }

  .query-actions {
    justify-content: flex-start;
  }
}
</style>
