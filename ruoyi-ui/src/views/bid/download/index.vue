<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="来源模块" prop="sourceModule">
        <el-select v-model="queryParams.sourceModule" placeholder="请选择来源模块" clearable>
          <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="请选择文件类型" clearable>
          <el-option label="HTML" value="html" />
          <el-option label="Word" value="word" />
          <el-option label="DOCX" value="docx" />
          <el-option label="PDF" value="pdf" />
          <el-option label="Excel" value="xlsx" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件名称" prop="fileName">
        <el-input v-model="queryParams.fileName" placeholder="请输入文件名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          size="small"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="recordList">
      <el-table-column label="文件名称" prop="fileName" min-width="240" show-overflow-tooltip />
      <el-table-column label="来源模块" prop="sourceModule" width="120" align="center">
        <template slot-scope="scope">{{ moduleText(scope.row.sourceModule) }}</template>
      </el-table-column>
      <el-table-column label="业务ID" prop="sourceId" width="100" align="center">
        <template slot-scope="scope">{{ scope.row.sourceId || '-' }}</template>
      </el-table-column>
      <el-table-column label="文件类型" prop="fileType" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini">{{ (scope.row.fileType || '-').toUpperCase() }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" prop="fileSize" width="120" align="right">
        <template slot-scope="scope">{{ formatSize(scope.row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="下载次数" prop="downloadCount" width="100" align="center" />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-download" size="mini" @click="handleDownload(scope.row)" v-hasPermi="['bid:download:download']">下载</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)" v-hasPermi="['bid:download:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { deleteDownloadRecord, downloadRecordUrl, listDownloadRecords } from '@/api/bid/download'

export default {
  name: 'BidDownload',
  data() {
    return {
      loading: false,
      total: 0,
      recordList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sourceModule: undefined,
        fileType: undefined,
        fileName: undefined
      },
      moduleOptions: [
        { label: 'AI方案', value: 'plan' },
        { label: 'AI标书', value: 'tender' },
        { label: 'AI质检', value: 'quality' },
        { label: '方案查重', value: 'duplicate' },
        { label: '知识库', value: 'knowledge' }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      listDownloadRecords(params).then(res => {
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
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleDownload(row) {
      window.open(downloadRecordUrl(row.id), '_blank')
      setTimeout(() => this.getList(), 800)
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除下载记录"' + row.fileName + '"吗？').then(() => deleteDownloadRecord(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    moduleText(value) {
      const item = this.moduleOptions.find(option => option.value === value)
      return item ? item.label : value || '-'
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
