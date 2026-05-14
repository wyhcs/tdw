<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/bid/knowledge')" :content="base.knowledgeName || '知识库详情'" />

    <el-card shadow="never" class="mt16">
      <div slot="header" class="clearfix">
        <span>文件管理</span>
        <el-upload class="upload-inline" action="#" :http-request="handleUpload" :show-file-list="false">
          <el-button size="mini" type="primary" icon="el-icon-upload" v-hasPermi="['bid:knowledge:upload']">上传文件</el-button>
        </el-upload>
      </div>
      <el-table v-loading="loading" :data="fileList">
        <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
        <el-table-column label="用途" prop="fileUsage" width="100" />
        <el-table-column label="模板" prop="isTemplate" width="80" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isTemplate === '1' ? 'success' : 'info'">{{ scope.row.isTemplate === '1' ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="解析状态" prop="parseStatus" width="110" align="center" />
        <el-table-column label="抽图状态" prop="imageStatus" width="110" align="center" />
        <el-table-column label="操作" width="220" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="parseFile(scope.row)">解析</el-button>
            <el-button type="text" size="mini" @click="extractImages(scope.row)">抽图</el-button>
            <el-button type="text" size="mini" @click="loadChunks(scope.row)">片段</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt16">
      <div slot="header">知识片段</div>
      <el-table :data="chunkList">
        <el-table-column label="标题" prop="chunkTitle" width="180" show-overflow-tooltip />
        <el-table-column label="类型" prop="chunkType" width="90" />
        <el-table-column label="内容" prop="chunkContent" min-width="360" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getKnowledge, listKnowledgeFiles, uploadKnowledgeFile, parseKnowledgeFile, extractKnowledgeImages, listKnowledgeChunks } from '@/api/bid/knowledge'

export default {
  name: 'BidKnowledgeDetail',
  data() {
    return {
      loading: false,
      knowledgeId: undefined,
      base: {},
      fileList: [],
      chunkList: []
    }
  },
  created() {
    this.knowledgeId = this.$route.query.knowledgeId
    this.loadBase()
    this.loadFiles()
  },
  methods: {
    loadBase() {
      getKnowledge(this.knowledgeId).then(res => { this.base = res.data || {} })
    },
    loadFiles() {
      this.loading = true
      listKnowledgeFiles({ knowledgeId: this.knowledgeId }).then(res => {
        this.fileList = res.data || []
        this.loading = false
      })
    },
    handleUpload(option) {
      console.log('upload option:', option)

      const realFile = option.file.raw || option.file.file || option.file

      const form = new FormData()
      form.append('file', realFile)
      form.append('knowledgeId', String(this.knowledgeId))
      form.append('fileUsage', 'material')
      form.append('isTemplate', '0')

      for (const item of form.entries()) {
        console.log(item[0], item[1])
      }

      uploadKnowledgeFile(form).then(() => {
        this.$modal.msgSuccess('上传成功')
        this.loadFiles()
      })
    },
    // handleUpload(option) {
    //   const form = new FormData()
    //   form.append('file', option.file)
    //   form.append('knowledgeId', this.knowledgeId)
    //   form.append('fileUsage', 'material')
    //   form.append('isTemplate', '0')
    //   uploadKnowledgeFile(form).then(() => {
    //     this.$modal.msgSuccess('上传成功')
    //     this.loadFiles()
    //   })
    // },
    parseFile(row) {
      parseKnowledgeFile(row.knowledgeFileId).then(() => {
        this.$modal.msgSuccess('解析完成')
        this.loadFiles()
        this.loadChunks(row)
      })
    },
    extractImages(row) {
      extractKnowledgeImages(row.knowledgeFileId).then(() => {
        this.$modal.msgSuccess('抽图完成')
        this.loadFiles()
        this.loadChunks(row)
      })
    },
    loadChunks(row) {
      listKnowledgeChunks({ knowledgeFileId: row.knowledgeFileId }).then(res => {
        this.chunkList = res.data || []
      })
    }
  }
}
</script>

<style scoped>
.mt16 { margin-top: 16px; }
.upload-inline { float: right; }
</style>
