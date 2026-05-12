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
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="openCreate" v-hasPermi="['bid:quality:add']">创建质检任务</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
      <el-table-column label="方案ID" prop="bidId" width="100" align="center" />
      <el-table-column label="来源" prop="sourceModule" width="100" align="center">
        <template slot-scope="scope">{{ scope.row.sourceModule === 'tender' ? 'AI标书' : 'AI方案' }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === 'success' ? 'success' : 'info'">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="问题数" prop="issueCount" width="100" align="center" />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      <el-table-column label="操作" width="260" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-video-play" size="mini" @click="runTask(scope.row)" v-hasPermi="['bid:quality:check']">执行</el-button>
          <el-button type="text" icon="el-icon-document" size="mini" @click="openResult(scope.row)" v-hasPermi="['bid:quality:result']">结果</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)" v-hasPermi="['bid:quality:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="创建质检任务" :visible.sync="createVisible" width="520px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称">
          <el-input v-model="form.taskName" placeholder="为空时自动生成" />
        </el-form-item>
        <el-form-item label="方案ID" prop="bidId">
          <el-input v-model="form.bidId" placeholder="请输入 tdw_bids.id" />
        </el-form-item>
        <el-form-item label="来源模块">
          <el-radio-group v-model="form.sourceModule">
            <el-radio-button label="plan">AI方案</el-radio-button>
            <el-radio-button label="tender">AI标书</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建并执行</el-button>
      </div>
    </el-dialog>

    <el-dialog title="质检过程" :visible.sync="runningVisible" width="360px" append-to-body>
      <div class="running-box">
        <i class="el-icon-loading" />
        <span>正在读取大纲和内容块并生成质检结果...</span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addQualityTask, deleteQualityTask, listQualityTasks, runQualityTask } from '@/api/bid/quality'

export default {
  name: 'BidQualityIndex',
  data() {
    return {
      loading: false,
      total: 0,
      taskList: [],
      createVisible: false,
      runningVisible: false,
      queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, bidId: undefined, status: undefined },
      form: { taskName: '', bidId: undefined, sourceModule: 'plan' },
      rules: { bidId: [{ required: true, message: '请输入方案ID', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQualityTasks(this.queryParams).then(res => {
        this.taskList = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
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
      const map = { waiting: '排队中', running: '执行中', success: '成功' }
      return map[status] || status || '-'
    },
    openCreate() {
      this.form = { taskName: '', bidId: undefined, sourceModule: 'plan' }
      this.createVisible = true
    },
    submitCreate() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.runningVisible = true
        addQualityTask(this.form).then(res => {
          const task = res.data || {}
          return runQualityTask(task.id).then(() => task)
        }).then(task => {
          this.$modal.msgSuccess('质检完成')
          this.createVisible = false
          this.$router.push({ path: '/bid/quality/result', query: { taskId: task.id, bidId: task.bidId, sourceModule: task.sourceModule }})
        }).finally(() => {
          this.runningVisible = false
          this.getList()
        })
      })
    },
    runTask(row) {
      this.runningVisible = true
      runQualityTask(row.id).then(() => {
        this.$modal.msgSuccess('质检完成')
        this.openResult(row)
      }).finally(() => {
        this.runningVisible = false
        this.getList()
      })
    },
    openResult(row) {
      this.$router.push({ path: '/bid/quality/result', query: { taskId: row.id, bidId: row.bidId, sourceModule: row.sourceModule }})
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除质检任务"' + row.taskName + '"吗？').then(() => deleteQualityTask(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.running-box {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
