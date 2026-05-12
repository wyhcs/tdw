<template>
  <div class="app-container">
    <el-page-header content="方案查重报告" @back="$router.push('/bid/duplicate')" />

    <el-descriptions v-if="task" :column="4" border class="report-summary">
      <el-descriptions-item label="任务名称">{{ task.taskName }}</el-descriptions-item>
      <el-descriptions-item label="方案ID">{{ task.bidId }}</el-descriptions-item>
      <el-descriptions-item label="相似片段">{{ task.issueCount || 0 }}</el-descriptions-item>
      <el-descriptions-item label="整体相似度">{{ task.overallSimilarity || 0 }}%</el-descriptions-item>
    </el-descriptions>

    <el-table v-loading="loading" :data="resultList" class="result-table">
      <el-table-column label="相似度" prop="similarity" width="100" align="center">
        <template slot-scope="scope">
          <el-tag type="danger" size="mini">{{ scope.row.similarity }}%</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="相似片段" prop="similarText" min-width="260" show-overflow-tooltip />
      <el-table-column label="来源" prop="sourceName" min-width="180" show-overflow-tooltip />
      <el-table-column label="来源片段" prop="sourceText" min-width="240" show-overflow-tooltip />
      <el-table-column label="大纲节点" prop="outlineId" width="110" align="center" />
      <el-table-column label="内容块" prop="contentId" width="110" align="center">
        <template slot-scope="scope">{{ scope.row.contentId || '-' }}</template>
      </el-table-column>
      <el-table-column label="建议" prop="suggestion" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="110" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-position" size="mini" @click="locate(scope.row)">定位</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getResults" />
  </div>
</template>

<script>
import { getDuplicateTask, listDuplicateResults } from '@/api/bid/duplicate'

export default {
  name: 'BidDuplicateResult',
  data() {
    return {
      loading: false,
      total: 0,
      task: null,
      resultList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskId: undefined,
        bidId: undefined
      }
    }
  },
  created() {
    this.queryParams.taskId = this.$route.query.taskId
    this.queryParams.bidId = this.$route.query.bidId
    this.getTask()
    this.getResults()
  },
  methods: {
    getTask() {
      if (!this.queryParams.taskId) {
        return
      }
      getDuplicateTask(this.queryParams.taskId).then(res => {
        this.task = res.data
      })
    },
    getResults() {
      this.loading = true
      listDuplicateResults(this.queryParams).then(res => {
        this.resultList = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    locate(row) {
      const bidId = row.bidId || this.queryParams.bidId
      this.$router.push({
        path: '/bid/plan/editor',
        query: {
          bidId,
          outlineId: row.outlineId,
          contentId: row.contentId
        }
      })
    }
  }
}
</script>

<style scoped>
.report-summary {
  margin-top: 20px;
}
.result-table {
  margin-top: 16px;
}
</style>
