<template>
  <div class="knowledge-workspace">
    <aside class="library-sidebar">
      <div class="sidebar-title">
        <i class="el-icon-s-fold"></i>
        <span>我的知识库</span>
      </div>
      <el-input
        v-model="queryParams.knowledgeName"
        class="search-input"
        size="small"
        placeholder="搜索知识库"
        suffix-icon="el-icon-search"
        clearable
        @keyup.enter.native="getList"
        @clear="getList"
      />
      <div v-loading="loading" class="library-list">
        <div
          v-for="item in knowledgeList"
          :key="item.knowledgeId"
          class="library-item"
          :class="{ active: currentKnowledge && currentKnowledge.knowledgeId === item.knowledgeId }"
          @click="selectKnowledge(item)"
        >
          <div class="library-main">
            <i class="el-icon-folder"></i>
            <strong>{{ item.knowledgeName }}</strong>
            <el-dropdown trigger="click" @command="command => handleLibraryCommand(command, item)">
              <span class="more" @click.stop>...</span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="edit">重命名</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="library-meta">
            <span>{{ item.createBy || 'Qiuqingy...' }}</span>
            <span>{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
        </div>
        <div class="empty-tip">--没有更多知识库了--</div>
      </div>
      <el-button class="create-button" type="primary" @click="handleAdd" v-hasPermi="['bid:knowledge:add']">新建知识库</el-button>
    </aside>

    <main class="content-panel">
      <header class="panel-header">
        <div class="section-title">
          <span></span>
          <h2>{{ currentKnowledge ? currentKnowledge.knowledgeName : '知识库' }}</h2>
        </div>
        <p>{{ currentKnowledge ? currentKnowledge.description || '我的知识库' : '请先新建或选择知识库' }}</p>
      </header>

      <section class="upload-actions">
        <el-upload
          class="upload-card normal"
          drag
          action="#"
          multiple
          :limit="5"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleKnowledgeUpload"
          accept=".doc,.docx,.pdf"
          :disabled="!currentKnowledge"
        >
          <i class="el-icon-upload2"></i>
          <div class="upload-title">点击或拖拽上传文件</div>
          <div class="upload-hint">单个文件不超过100MB，支持以下文件格式： docx、doc,pdf,每次上传5个</div>
        </el-upload>

        <div class="upload-card extract" :class="{ disabled: !currentKnowledge }" @click="openExtractDialog">
          <i class="el-icon-upload2"></i>
          <div class="upload-title">点击文档抽图</div>
          <div class="upload-hint">单个文件不超过100MB，支持以下文件格式： docx、doc，每次上传5个</div>
        </div>
      </section>

      <el-table class="file-table" :data="fileList" v-loading="fileLoading">
        <el-table-column label="文件名" min-width="360">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-document" @click="openRichText(scope.row)">{{ scope.row.fileName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="160">
          <template slot-scope="scope">{{ formatSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="上传时间" prop="createTime" width="190">
          <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
        </el-table-column>
        <el-table-column label="文件状态" width="150">
          <template slot-scope="scope">
            <span class="status-dot"></span>{{ scope.row.parseStatus === 'success' ? '解析成功' : '已上传' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click="parseFile(scope.row)">解析</el-button>
            <el-button type="text" @click="extractExisting(scope.row)">抽图</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <span>共 {{ fileList.length }} 条</span>
        <span>共1页</span>
        <el-select size="small" value="20" class="page-size"><el-option label="20条/页" value="20" /></el-select>
        <el-button size="small" icon="el-icon-arrow-left" disabled></el-button>
        <el-button size="small" type="primary">1</el-button>
        <el-button size="small" icon="el-icon-arrow-right" disabled></el-button>
      </div>
    </main>

    <el-dialog :title="dialogTitle" :visible.sync="open" width="520px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="知识库名称" prop="knowledgeName">
          <el-input v-model="form.knowledgeName" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="知识库描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入知识库描述" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="文档抽图上传" :visible.sync="extractDialogVisible" width="620px" append-to-body>
      <el-upload
        drag
        action="#"
        multiple
        :limit="5"
        :auto-upload="false"
        :file-list="extractFiles"
        :on-change="onExtractFileChange"
        :on-remove="onExtractFileRemove"
        accept=".doc,.docx"
      >
        <i class="el-icon-document"></i>
        <div>将文件拖拽至此区域，或 <em>点击添加</em></div>
        <div slot="tip" class="el-upload__tip">支持.doc,.docx 格式，仅支持5个文件，单个文件最大100MB</div>
      </el-upload>
      <el-form label-width="130px" class="extract-form">
        <el-form-item label="选择图库（可选）：">
          <el-select v-model="extractGalleryId" clearable placeholder="不选择图库则仅上传文档" style="width: 100%">
            <el-option v-for="item in galleryList" :key="item.id" :label="item.galleryName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="extract-tip">
        <strong>抽图提示：</strong>
        <p>仅支持doc/docx格式，单次最多5个文件，单个不超过100MB</p>
        <p>选择图库后，将自动提取文档中的图片并保存到图库</p>
        <p>不选择图库则仅上传文档，不进行抽图操作</p>
      </div>
      <div slot="footer">
        <el-button @click="extractDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExtractUpload">开始上传</el-button>
      </div>
    </el-dialog>

    <el-dialog title="解析内容" :visible.sync="editorVisible" width="760px" append-to-body>
      <el-input type="textarea" :rows="16" v-model="richTextContent" />
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledge, addKnowledge, renameKnowledge, delKnowledge, listKnowledgeFiles, uploadKnowledgeFile, parseKnowledgeFile, extractKnowledgeImages, extractKnowledgeUpload, listKnowledgeChunks } from '@/api/bid/knowledge'
import { listGalleries } from '@/api/bid/gallery'

export default {
  name: 'BidKnowledge',
  data() {
    return {
      loading: false,
      fileLoading: false,
      knowledgeList: [],
      fileList: [],
      galleryList: [],
      currentKnowledge: null,
      open: false,
      extractDialogVisible: false,
      editorVisible: false,
      dialogTitle: '',
      form: {},
      extractFiles: [],
      extractGalleryId: undefined,
      richTextContent: '',
      queryParams: { pageNum: 1, pageSize: 100, knowledgeName: undefined },
      rules: {
        knowledgeName: [{ required: true, message: '知识库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadGalleries()
  },
  methods: {
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(res => {
        this.knowledgeList = res.rows || []
        if (!this.currentKnowledge && this.knowledgeList.length) {
          this.selectKnowledge(this.knowledgeList[0])
        }
      }).finally(() => { this.loading = false })
    },
    loadGalleries() {
      listGalleries({ pageNum: 1, pageSize: 100 }).then(res => { this.galleryList = res.rows || [] })
    },
    selectKnowledge(item) {
      this.currentKnowledge = item
      this.loadFiles()
    },
    loadFiles() {
      if (!this.currentKnowledge) return
      this.fileLoading = true
      listKnowledgeFiles({ knowledgeId: this.currentKnowledge.knowledgeId }).then(res => {
        this.fileList = res.data || []
      }).finally(() => { this.fileLoading = false })
    },
    handleLibraryCommand(command, item) {
      if (command === 'edit') this.handleRename(item)
      if (command === 'delete') this.handleDelete(item)
    },
    handleAdd() {
      this.form = { knowledgeName: '', description: '' }
      this.dialogTitle = '新建知识库'
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
          this.currentKnowledge = null
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除知识库"' + row.knowledgeName + '"吗？').then(() => delKnowledge(row.knowledgeId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.currentKnowledge = null
        this.fileList = []
        this.getList()
      }).catch(() => {})
    },
    handleKnowledgeUpload(file) {
      if (!this.currentKnowledge || !file.raw) return
      if (!this.validateFile(file.raw, ['doc', 'docx', 'pdf'], 100)) return
      const form = new FormData()
      form.append('file', file.raw)
      form.append('knowledgeId', this.currentKnowledge.knowledgeId)
      form.append('fileUsage', 'material')
      form.append('isTemplate', '0')
      uploadKnowledgeFile(form).then(res => {
        const id = res.data && res.data.knowledgeFileId
        const done = id ? parseKnowledgeFile(id) : Promise.resolve()
        done.then(() => {
          this.$modal.msgSuccess('上传并解析完成')
          this.loadFiles()
        })
      })
    },
    openExtractDialog() {
      if (!this.currentKnowledge) return
      this.extractFiles = []
      this.extractGalleryId = undefined
      this.extractDialogVisible = true
      this.loadGalleries()
    },
    onExtractFileChange(file, fileList) {
      this.extractFiles = fileList.filter(item => this.validateFile(item.raw, ['doc', 'docx'], 100, false))
    },
    onExtractFileRemove(file, fileList) {
      this.extractFiles = fileList
    },
    submitExtractUpload() {
      if (!this.extractFiles.length) {
        this.$modal.msgWarning('请先添加抽图文档')
        return
      }
      const tasks = this.extractFiles.map(item => {
        const form = new FormData()
        form.append('file', item.raw)
        form.append('knowledgeId', this.currentKnowledge.knowledgeId)
        if (this.extractGalleryId) form.append('galleryId', this.extractGalleryId)
        return extractKnowledgeUpload(form)
      })
      Promise.all(tasks).then(() => {
        this.$modal.msgSuccess('文档抽图上传完成')
        this.extractDialogVisible = false
        this.loadFiles()
      })
    },
    parseFile(row) {
      parseKnowledgeFile(row.knowledgeFileId).then(() => {
        this.$modal.msgSuccess('解析完成')
        this.loadFiles()
      })
    },
    extractExisting(row) {
      extractKnowledgeImages(row.knowledgeFileId).then(() => {
        this.$modal.msgSuccess('抽图完成')
        this.loadFiles()
      })
    },
    openRichText(row) {
      listKnowledgeChunks({ knowledgeFileId: row.knowledgeFileId }).then(res => {
        const chunks = res.data || []
        this.richTextContent = chunks.map(item => item.chunkTitle + '\n' + item.chunkContent).join('\n\n')
        this.editorVisible = true
      })
    },
    validateFile(file, types, maxMb, showMessage = true) {
      if (!file) return false
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      const valid = types.indexOf(ext) !== -1 && file.size <= maxMb * 1024 * 1024
      if (!valid && showMessage) {
        this.$modal.msgWarning('文件格式或大小不符合要求')
      }
      return valid
    },
    formatSize(size) {
      if (!size) return '0 KB'
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
      return (size / 1024 / 1024).toFixed(2) + ' MB'
    }
  }
}
</script>

<style scoped>
.knowledge-workspace {
  min-height: calc(100vh - 84px);
  background: #f4f7fb;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 12px;
  padding: 0 0 0 0;
}
.library-sidebar,
.content-panel {
  background: #fff;
  min-height: calc(100vh - 84px);
}
.library-sidebar {
  display: flex;
  flex-direction: column;
  padding: 14px 12px;
  border-radius: 0 8px 8px 0;
}
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 18px;
}
.search-input {
  margin-bottom: 12px;
}
.library-list {
  flex: 1;
  overflow: auto;
}
.library-item {
  border-radius: 4px;
  padding: 12px 10px;
  cursor: pointer;
  border-bottom: 1px solid #eef1f6;
}
.library-item.active {
  background: #edf3ff;
}
.library-main {
  display: grid;
  grid-template-columns: 20px 1fr 26px;
  align-items: center;
  gap: 6px;
}
.library-main i {
  color: #f5a400;
}
.library-main strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.more {
  color: #303133;
  font-weight: 700;
}
.library-meta {
  display: flex;
  gap: 10px;
  margin: 10px 0 0 38px;
  color: #737b8a;
  font-size: 13px;
}
.empty-tip {
  text-align: center;
  color: #9aa3af;
  margin-top: 26px;
}
.create-button {
  width: 100%;
  margin-top: 14px;
}
.content-panel {
  padding: 24px;
}
.panel-header {
  border-bottom: 1px solid #e5e9f2;
  padding-bottom: 18px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title span {
  width: 5px;
  height: 18px;
  background: #2f6bff;
  border-radius: 3px;
}
.section-title h2 {
  font-size: 22px;
  margin: 0;
}
.panel-header p {
  margin: 10px 0 0 16px;
  color: #7b8494;
}
.upload-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
  margin: 22px 0;
}
.upload-card {
  height: 112px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.upload-card.normal {
  background: #edf3ff;
}
.upload-card.extract {
  background: #f3eafa;
}
.upload-card.disabled {
  opacity: .6;
  cursor: not-allowed;
}
.upload-card i,
.upload-title {
  color: #2f6bff;
  font-weight: 600;
}
.upload-card.extract i,
.upload-card.extract .upload-title {
  color: #7f4df7;
}
.upload-hint {
  color: #8d96a6;
  margin-top: 12px;
  font-size: 13px;
}
.file-table {
  margin-top: 8px;
}
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
  margin-right: 6px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
  border-top: 1px solid #edf0f5;
  padding-top: 14px;
}
.pager > span:first-child {
  margin-right: auto;
}
.page-size {
  width: 120px;
}
.extract-form {
  margin-top: 18px;
}
.extract-tip {
  background: #fff7e8;
  color: #8a6d3b;
  padding: 14px 20px;
  border-radius: 4px;
}
.extract-tip p {
  margin: 8px 0 0 24px;
}
@media (max-width: 1100px) {
  .knowledge-workspace {
    grid-template-columns: 1fr;
  }
  .upload-actions {
    grid-template-columns: 1fr;
  }
}
</style>
