<template>
  <div class="app-container bid-plan-page">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="方案名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入方案名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="方案类型" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择类型" clearable>
          <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleCreate" v-hasPermi="['bid:plan:add']">新建方案</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="planList">
      <el-table-column label="方案名称" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" prop="category" width="120" align="center" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === 2 ? 'success' : 'info'">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="userid" width="110" align="center">
        <template slot-scope="scope">{{ scope.row.userid || '-' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdTime" width="150" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createdTime, '{y}-{m}-{d}') }}</template>
      </el-table-column>
      <el-table-column label="备注" prop="note" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit-outline" size="mini" @click="openEditor(scope.row)">编辑器</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)" v-hasPermi="['bid:plan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listBids, deleteBid } from '@/api/bid/bids'

export default {
  name: 'BidPlan',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      planList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        category: undefined,
        status: undefined
      },
      categoryOptions: [
        { label: '服务', value: '服务' },
        { label: '货物', value: '货物' },
        { label: '工程', value: '工程' },
        { label: '监理', value: '监理' },
        { label: 'IT信息', value: 'IT信息' },
        { label: '其它', value: '其它' }
      ],
      statusOptions: [
        { label: '草稿', value: 1 },
        { label: '已完成', value: 2 }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listBids(this.queryParams).then(res => {
        this.planList = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    statusText(status) {
      return Number(status) === 2 ? '已完成' : '草稿'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleCreate() {
      this.$router.push('/bid/plan/create')
    },
    openEditor(row) {
      this.$router.push({ path: '/bid/plan/editor', query: { bidId: row.id } })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除方案"' + row.title + '"吗？').then(() => deleteBid(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>
