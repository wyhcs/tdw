<template>
  <div class="app-container knowledge-page">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
      <el-form-item label="知识库">
        <el-input v-model="queryParams.knowledgeName" placeholder="请输入知识库名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['bid:knowledge:add']">新增知识库</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="knowledgeList">
      <el-table-column label="名称" prop="knowledgeName" min-width="180" show-overflow-tooltip />
      <el-table-column label="描述" prop="description" min-width="240" show-overflow-tooltip />
      <el-table-column label="文件数" prop="fileCount" width="90" align="center" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" type="success">{{ scope.row.status || 'normal' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" size="mini" @click="openDetail(scope.row)">详情</el-button>
          <el-button type="text" icon="el-icon-edit" size="mini" @click="handleRename(scope.row)" v-hasPermi="['bid:knowledge:edit']">重命名</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)" v-hasPermi="['bid:knowledge:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="open" width="520px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="knowledgeName">
          <el-input v-model="form.knowledgeName" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="open = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledge, addKnowledge, renameKnowledge, delKnowledge } from '@/api/bid/knowledge'

export default {
  name: 'BidKnowledge',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      knowledgeList: [],
      open: false,
      dialogTitle: '',
      form: {},
      queryParams: { pageNum: 1, pageSize: 10, knowledgeName: undefined },
      rules: {
        knowledgeName: [{ required: true, message: '知识库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(res => {
        this.knowledgeList = res.rows || []
        this.total = res.total || 0
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
    handleAdd() {
      this.form = { knowledgeName: '', description: '' }
      this.dialogTitle = '新增知识库'
      this.open = true
    },
    handleRename(row) {
      this.form = { knowledgeId: row.knowledgeId, knowledgeName: row.knowledgeName, description: row.description }
      this.dialogTitle = '重命名知识库'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.knowledgeId ? renameKnowledge(this.form) : addKnowledge(this.form)
        request.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除知识库"' + row.knowledgeName + '"吗？').then(() => delKnowledge(row.knowledgeId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    openDetail(row) {
      this.$router.push({ path: '/bid/knowledge/detail', query: { knowledgeId: row.knowledgeId } })
    }
  }
}
</script>
