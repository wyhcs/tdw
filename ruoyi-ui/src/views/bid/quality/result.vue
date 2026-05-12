<template>
  <div class="app-container">
    <el-page-header content="质检结果" @back="$router.push('/bid/quality')" />

    <el-table v-loading="loading" :data="resultList" class="result-table">
      <el-table-column label="严重级别" prop="severity" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.severity === 'error' ? 'danger' : 'warning'">{{ scope.row.severity || 'warning' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="问题" prop="issueTitle" min-width="180" show-overflow-tooltip />
      <el-table-column label="说明" prop="issueDesc" min-width="260" show-overflow-tooltip />
      <el-table-column label="建议" prop="suggestion" min-width="240" show-overflow-tooltip />
      <el-table-column label="大纲ID" prop="outlineId" width="100" align="center" />
      <el-table-column label="内容块ID" prop="contentId" width="110" align="center">
        <template slot-scope="scope">{{ scope.row.contentId || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-position" size="mini" @click="locate(scope.row)">定位</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getResults" />
  </div>
</template>

<script>
import { listQualityResults } from '@/api/bid/quality'

export default {
  name: 'BidQualityResult',
  data() {
    return {
      loading: false,
      total: 0,
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
    this.getResults()
  },
  methods: {
    getResults() {
      this.loading = true
      listQualityResults(this.queryParams).then(res => {
        this.resultList = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    locate(row) {
      const path = this.$route.query.sourceModule === 'tender' ? '/bid/tender/editor' : '/bid/plan/editor'
      this.$router.push({
        path,
        query: {
          bidId: row.bidId || this.queryParams.bidId,
          outlineId: row.outlineId,
          contentId: row.contentId
        }
      })
    }
  }
}
</script>

<style scoped>
.result-table {
  margin-top: 18px;
}
</style>
