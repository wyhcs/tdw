<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="项目名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入项目名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="category">
        <el-input v-model="queryParams.category" placeholder="请输入标书类型" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="$router.push('/bid/tender/create')">新建标书</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="tenderList">
      <el-table-column label="项目名称" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" prop="category" width="140" />
      <el-table-column label="状态" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === 2 ? 'success' : 'info'">{{ scope.row.status === 2 ? '已完成' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdTime" width="160" />
      <el-table-column label="备注" prop="note" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="240" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-document-checked" size="mini" @click="openParse(scope.row)">解析</el-button>
          <el-button type="text" icon="el-icon-edit-outline" size="mini" @click="openEditor(scope.row)">编辑器</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listTender } from '@/api/bid/tender'

export default {
  name: 'BidTenderIndex',
  data() {
    return {
      loading: false,
      total: 0,
      tenderList: [],
      queryParams: { pageNum: 1, pageSize: 10, title: undefined, category: undefined }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTender(this.queryParams).then(res => {
        this.tenderList = res.rows || []
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
    openParse(row) {
      this.$router.push({ path: '/bid/tender/parse', query: { bidId: row.id }})
    },
    openEditor(row) {
      this.$router.push({ path: '/bid/tender/editor', query: { bidId: row.id }})
    }
  }
}
</script>
