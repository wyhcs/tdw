<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="方案ID" prop="bidId">
        <el-input v-model="queryParams.bidId" placeholder="请输入方案ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="排队中" value="waiting" />
          <el-option label="执行中" value="running" />
          <el-option label="成功" value="success" />
          <el-option label="失败" value="fail" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="$router.push('/bid/duplicate/create')" v-hasPermi="['bid:duplicate:add']">新建查重</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
      <el-table-column label="方案ID" prop="bidId" width="100" align="center" />
      <el-table-column label="比对来源" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ scope.row.sourceType === 'bid' ? '方案ID：' + scope.row.compareBidId : '上传文件' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="相似片段" prop="issueCount" width="100" align="center" />
      <el-table-column label="整体相似度" prop="overallSimilarity" width="110" align="center">
        <template slot-scope="scope">{{ scope.row.overallSimilarity || 0 }}%</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-video-play" size="mini" @click="runTask(scope.row)" v-hasPermi="['bid:duplicate:run']">执行</el-button>
          <el-button type="text" icon="el-icon-document" size="mini" @click="openResult(scope.row)" v-hasPermi="['bid:duplicate:query']">报告</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)" v-hasPermi="['bid:duplicate:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="查重排队中" :visible.sync="queueDialogVisible" width="360px" append-to-body>
      <div class="queue-state">
        <i class="el-icon-loading" />
        <span>正在执行 Mock 查重，请稍候...</span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDuplicateTasks, runDuplicateTask, deleteDuplicateTask } from '@/api/bid/duplicate'

export default {
  name: 'BidDuplicateIndex',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      taskList: [],
      queueDialogVisible: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskName: undefined,
        bidId: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDuplicateTasks(this.queryParams).then(res => {
        this.taskList = res.rows || []
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
      this.resetForm('queryForm')
      this.handleQuery()
    },
    statusText(status) {
      const map = { waiting: '排队中', running: '执行中', success: '成功', fail: '失败' }
      return map[status] || status || '-'
    },
    statusType(status) {
      const map = { waiting: 'info', running: 'warning', success: 'success', fail: 'danger' }
      return map[status] || 'info'
    },
    runTask(row) {
      this.queueDialogVisible = true
      runDuplicateTask(row.id).then(() => {
        this.$modal.msgSuccess('查重完成')
        this.openResult(row)
      }).finally(() => {
        this.queueDialogVisible = false
        this.getList()
      })
    },
    openResult(row) {
      this.$router.push({ path: '/bid/duplicate/result', query: { taskId: row.id, bidId: row.bidId }})
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除查重任务"' + row.taskName + '"吗？').then(() => deleteDuplicateTask(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.queue-state {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
</style>
