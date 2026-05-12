<template>
  <el-dialog title="选择知识库文件" :visible.sync="innerVisible" width="760px" append-to-body @close="$emit('update:visible', false)">
    <el-form :inline="true" size="small">
      <el-form-item label="知识库ID">
        <el-input v-model="query.knowledgeId" placeholder="可选" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="loadFiles">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table ref="table" v-loading="loading" :data="fileList" @selection-change="selection = $event">
      <el-table-column type="selection" width="55" />
      <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
      <el-table-column label="用途" prop="fileUsage" width="100" />
      <el-table-column label="解析状态" prop="parseStatus" width="100" />
    </el-table>
    <div slot="footer">
      <el-button type="primary" @click="confirm">确定</el-button>
      <el-button @click="innerVisible = false">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listKnowledgeFiles, listTemplateFiles } from '@/api/bid/knowledge'

export default {
  name: 'KnowledgeFileSelectDialog',
  props: {
    visible: { type: Boolean, default: false },
    templateOnly: { type: Boolean, default: false }
  },
  data() {
    return {
      innerVisible: false,
      loading: false,
      query: { knowledgeId: undefined },
      fileList: [],
      selection: []
    }
  },
  watch: {
    visible(val) {
      this.innerVisible = val
      if (val) this.loadFiles()
    },
    innerVisible(val) {
      this.$emit('update:visible', val)
    }
  },
  methods: {
    loadFiles() {
      this.loading = true
      const api = this.templateOnly ? listTemplateFiles : listKnowledgeFiles
      api(this.query).then(res => {
        this.fileList = res.data || []
        this.loading = false
      })
    },
    confirm() {
      this.$emit('selected', this.selection)
      this.innerVisible = false
    }
  }
}
</script>
