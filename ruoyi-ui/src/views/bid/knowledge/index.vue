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
            <div class="library-actions" @click.stop>
              <el-tooltip content="编辑" placement="top">
                <el-button type="text" icon="el-icon-edit" @click="handleRename(item)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button type="text" icon="el-icon-delete" @click="handleDelete(item)" />
              </el-tooltip>
            </div>
          </div>
          <div class="library-meta">
            <span>{{ item.createBy || '--' }}</span>
            <span>{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
        </div>

        <div v-if="!knowledgeList.length && !loading" class="empty-side">暂无知识库</div>
        <div v-else class="empty-tip">--没有更多知识库了--</div>
      </div>

      <el-button class="create-button" type="primary" @click="handleAdd" v-hasPermi="['bid:knowledge:add']">
        新建知识库
      </el-button>
    </aside>

    <main class="content-panel">
      <section v-if="!currentKnowledge" class="home-panel">
        <div class="home-visual"></div>
        <div class="home-content">
          <h1>沉淀知识资产，驱动组织进化</h1>
          <p>正文撰写时，可导入知识库文件，让 AI 基于你选定的知识库素材，生成质量更高、更贴合要求的标书内容。</p>

          <div class="home-features">
            <div class="feature-item">
              <i class="el-icon-document-checked"></i>
              <h3>快速复用</h3>
              <p>历史标书模板、技术方案、资质文件一键调用，减少重复编写</p>
            </div>
            <div class="feature-line"></div>
            <div class="feature-item">
              <i class="el-icon-search"></i>
              <h3>智能检索</h3>
              <p>按行业、项目类型、客户特征快速定位参考案例，缩短准备周期</p>
            </div>
            <div class="feature-line"></div>
            <div class="feature-item">
              <i class="el-icon-user-solid"></i>
              <h3>协同加速</h3>
              <p>多部门共享素材库，打破信息孤岛，并行推进标书制作</p>
            </div>
          </div>

          <el-button type="primary" class="home-create" icon="el-icon-plus" @click="handleAdd">新建知识库</el-button>
        </div>
      </section>

      <section v-else class="detail-panel">
        <header class="panel-header">
          <div class="section-title">
            <span></span>
            <h2>{{ currentKnowledge.knowledgeName }}</h2>
          </div>
        </header>

        <section class="upload-actions">
          <el-upload
            :key="normalUploadKey"
            class="upload-card normal"
            drag
            action="#"
            multiple
            :limit="5"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleKnowledgeUpload"
            :on-exceed="handleUploadExceed"
            accept=".doc,.docx,.pdf"
          >
            <i class="el-icon-upload2"></i>
            <div class="upload-title">点击或拖拽上传文件</div>
            <div class="upload-hint">单个文件不超过100MB，支持以下文件格式： docx、doc、pdf，每次上传5个</div>
          </el-upload>

          <div class="upload-card extract" @click="openExtractDialog">
            <i class="el-icon-upload2"></i>
            <div class="upload-title">点击文档抽图</div>
            <div class="upload-hint">单个文件不超过100MB，支持以下文件格式： docx、doc，每次上传5个</div>
          </div>
        </section>

        <el-table v-if="fileList.length" class="file-table" :data="fileList" v-loading="fileLoading">
          <el-table-column label="文件名" min-width="420">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-document" @click="openRichText(scope.row)">
                {{ scope.row.fileName }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="文件大小" width="150">
            <template slot-scope="scope">{{ formatSize(scope.row.fileSize) }}</template>
          </el-table-column>
          <el-table-column label="上传时间" width="190">
            <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
          <el-table-column label="文件状态" width="150">
            <template slot-scope="scope">
              <div class="status-cell" :class="'status-' + normalizeStatus(scope.row.parseStatus)">
                <span>{{ statusText(scope.row) }}</span>
                <el-progress
                  v-if="normalizeStatus(scope.row.parseStatus) === 'parsing'"
                  :percentage="scope.row.parseProgress || 10"
                  :show-text="false"
                  :stroke-width="8"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center">
            <template slot="header">
              <span>操作</span>
              <el-tooltip content="刷新" placement="top">
                <el-button type="text" icon="el-icon-refresh" class="header-refresh" @click.stop="loadFiles" />
              </el-tooltip>
            </template>
            <template slot-scope="scope">
              <el-button v-if="canRetry(scope.row)" type="text" @click="parseFile(scope.row)">解析</el-button>
              <el-button type="text" @click="openRenameFile(scope.row)">重命名</el-button>
              <el-button type="text" @click="handleDeleteFile(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-else class="knowledge-empty">
          <i class="el-icon-document"></i>
          <p>暂无知识库信息，请点击上方上传</p>
        </div>

        <div class="pager">
          <span>共 {{ fileList.length }} 条</span>
          <span>共{{ fileList.length ? 1 : 0 }}页</span>
          <el-select size="small" value="20" class="page-size">
            <el-option label="20条/页" value="20" />
          </el-select>
          <el-button size="small" icon="el-icon-arrow-left" disabled></el-button>
          <el-button size="small" type="primary">1</el-button>
          <el-button size="small" icon="el-icon-arrow-right" disabled></el-button>
        </div>
      </section>
    </main>

    <el-dialog :title="dialogTitle" :visible.sync="open" width="520px" append-to-body class="knowledge-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="知识库名称：" prop="knowledgeName">
          <el-input v-model="form.knowledgeName" placeholder="请输入知识库名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="知识库描述：">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入知识库描述" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="文档抽图上传" :visible.sync="extractDialogVisible" width="620px" append-to-body class="extract-dialog">
      <el-upload
        ref="extractUpload"
        drag
        action="#"
        multiple
        :limit="5"
        :auto-upload="false"
        :file-list="extractFiles"
        :on-change="onExtractFileChange"
        :on-remove="onExtractFileRemove"
        :on-exceed="handleUploadExceed"
        accept=".doc,.docx"
      >
        <i class="el-icon-document"></i>
        <div>将文件拖拽至此区域，或 <em>点击添加</em></div>
        <div slot="tip" class="el-upload__tip">支持.doc,.docx 格式，仅支持5个文件，单个文件最大100MB</div>
      </el-upload>

      <el-form label-width="132px" class="extract-form">
        <el-form-item label="选择图库（可选）：">
          <el-select v-model="extractGalleryId" clearable placeholder="不选择图库则仅上传文档" style="width: 100%">
            <el-option v-for="item in galleryList" :key="item.id" :label="item.galleryName" :value="item.id">
              <span>{{ item.galleryName }}</span>
              <span class="option-owner">{{ item.createBy || '--' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div class="extract-tip">
        <strong><i class="el-icon-warning"></i> 抽图提示：</strong>
        <p>仅支持doc/docx格式，单次最多5个文件，单个不超过100MB</p>
        <p>选择图库后，将自动提取文档中的图片并保存到图库</p>
        <p>不选择图库则仅上传文档，不进行抽图操作</p>
      </div>
      <div slot="footer">
        <el-button @click="extractDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="extractUploading" @click="submitExtractUpload">开始上传</el-button>
      </div>
    </el-dialog>

    <el-dialog title="重命名文件" :visible.sync="fileRenameVisible" width="460px" append-to-body>
      <el-form ref="fileForm" :model="fileForm" :rules="fileRules" label-width="86px">
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="fileForm.fileName" maxlength="255" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="fileRenameVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFileRename">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="解析内容" :visible.sync="editorVisible" width="980px" append-to-body class="knowledge-editor-dialog">
      <rich-content-editor
        class="knowledge-rich-editor"
        :selected-outline="richEditorOutline"
        :blocks="richTextBlocks"
        @save-rich="saveKnowledgePreview"
      />
    </el-dialog>
  </div>
</template>

<script>
import {
  listKnowledge,
  addKnowledge,
  renameKnowledge,
  delKnowledge,
  listKnowledgeFiles,
  uploadKnowledgeFile,
  parseKnowledgeFile,
  extractKnowledgeUpload,
  listKnowledgeChunks,
  renameKnowledgeFile,
  delKnowledgeFile
} from '@/api/bid/knowledge'
import { listGalleries } from '@/api/bid/gallery'
import RichContentEditor from '@/views/bid/components/RichContentEditor.vue'

export default {
  name: 'BidKnowledge',
  components: { RichContentEditor },
  data() {
    return {
      loading: false,
      fileLoading: false,
      extractUploading: false,
      knowledgeList: [],
      fileList: [],
      galleryList: [],
      currentKnowledge: null,
      open: false,
      extractDialogVisible: false,
      fileRenameVisible: false,
      editorVisible: false,
      dialogTitle: '',
      form: {},
      fileForm: { knowledgeFileId: undefined, fileName: '' },
      extractFiles: [],
      extractGalleryId: undefined,
      richEditorOutline: null,
      richTextHtml: '',
      richTextText: '',
      normalUploadKey: 0,
      parseTimers: {},
      queryParams: { pageNum: 1, pageSize: 100, knowledgeName: undefined },
      rules: {
        knowledgeName: [{ required: true, message: '知识库名称不能为空', trigger: 'blur' }]
      },
      fileRules: {
        fileName: [{ required: true, message: '文件名不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    richTextBlocks() {
      if (!this.richEditorOutline) return []
      return [{
        id: this.richEditorOutline.id,
        outlineId: this.richEditorOutline.id,
        contentType: 1,
        content: JSON.stringify({
          html: this.richTextHtml,
          text: this.richTextText
        })
      }]
    }
  },
  created() {
    this.getList()
  },
  beforeDestroy() {
    Object.keys(this.parseTimers).forEach(key => clearInterval(this.parseTimers[key]))
  },
  methods: {
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(res => {
        this.knowledgeList = res.rows || []
        if (this.currentKnowledge) {
          const latest = this.knowledgeList.find(item => item.knowledgeId === this.currentKnowledge.knowledgeId)
          this.currentKnowledge = latest || null
          if (!latest) this.fileList = []
        }
      }).finally(() => { this.loading = false })
    },
    loadGalleries() {
      return listGalleries({ pageNum: 1, pageSize: 100 }).then(res => {
        this.galleryList = res.rows || []
      })
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
    handleAdd() {
      this.form = { knowledgeName: '', description: '' }
      this.dialogTitle = '新增知识库'
      this.open = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    handleRename(row) {
      this.form = { knowledgeId: row.knowledgeId, knowledgeName: row.knowledgeName, description: row.description }
      this.dialogTitle = '编辑知识库'
      this.open = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
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
        if (this.currentKnowledge && this.currentKnowledge.knowledgeId === row.knowledgeId) {
          this.currentKnowledge = null
          this.fileList = []
        }
        this.getList()
      }).catch(() => {})
    },
    handleKnowledgeUpload(file) {
      if (!this.currentKnowledge || !file.raw) return
      if (!this.validateFile(file.raw, ['doc', 'docx', 'pdf'], 100)) return
      const tempRow = this.createTempFileRow(file.raw)
      this.fileList.unshift(tempRow)
      const form = new FormData()
      form.append('file', file.raw)
      form.append('knowledgeId', this.currentKnowledge.knowledgeId)
      form.append('fileUsage', 'material')
      form.append('isTemplate', '0')
      uploadKnowledgeFile(form).then(res => {
        const id = res.data && res.data.knowledgeFileId
        tempRow.knowledgeFileId = id
        tempRow.parseProgress = 18
        this.startParseProgress(tempRow)
        return id ? parseKnowledgeFile(id) : Promise.resolve()
      }).then(() => {
        this.stopParseProgress(tempRow)
        tempRow.parseStatus = 'success'
        tempRow.parseProgress = 100
        this.$modal.msgSuccess('上传并解析完成')
        this.loadFiles()
        this.getList()
      }).catch(() => {
        this.stopParseProgress(tempRow)
        tempRow.parseStatus = 'fail'
        tempRow.parseProgress = 100
        this.loadFiles()
      }).finally(() => {
        this.normalUploadKey++
      })
    },
    createTempFileRow(file) {
      return {
        tempId: 'temp-' + Date.now() + '-' + Math.random(),
        fileName: file.name,
        fileSize: file.size,
        createTime: new Date(),
        parseStatus: 'parsing',
        parseProgress: 10
      }
    },
    startParseProgress(row) {
      const key = row.tempId || row.knowledgeFileId
      this.stopParseProgress(row)
      this.$set(this.parseTimers, key, setInterval(() => {
        if (row.parseProgress < 88) row.parseProgress += Math.max(1, Math.round((88 - row.parseProgress) / 8))
      }, 900))
    },
    stopParseProgress(row) {
      const key = row && (row.tempId || row.knowledgeFileId)
      if (key && this.parseTimers[key]) {
        clearInterval(this.parseTimers[key])
        this.$delete(this.parseTimers, key)
      }
    },
    openExtractDialog() {
      if (!this.currentKnowledge) return
      this.extractFiles = []
      this.extractGalleryId = undefined
      this.extractDialogVisible = true
      this.loadGalleries()
    },
    onExtractFileChange(file, fileList) {
      if (file.raw && !this.validateFile(file.raw, ['doc', 'docx'], 100, false)) {
        this.$modal.msgWarning('仅支持doc/docx格式，单个文件不超过100MB')
      }
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
      this.extractUploading = true
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
        this.getList()
      }).finally(() => { this.extractUploading = false })
    },
    parseFile(row) {
      if (!row.knowledgeFileId) return
      this.$set(row, 'parseStatus', 'parsing')
      this.$set(row, 'parseProgress', 10)
      this.startParseProgress(row)
      parseKnowledgeFile(row.knowledgeFileId).then(() => {
        this.stopParseProgress(row)
        this.$modal.msgSuccess('解析完成')
        this.loadFiles()
      }).catch(() => {
        this.stopParseProgress(row)
        this.$set(row, 'parseStatus', 'fail')
        this.$set(row, 'parseProgress', 100)
      })
    },
    openRenameFile(row) {
      if (!row.knowledgeFileId) return
      this.fileForm = { knowledgeFileId: row.knowledgeFileId, fileName: row.fileName }
      this.fileRenameVisible = true
      this.$nextTick(() => this.$refs.fileForm && this.$refs.fileForm.clearValidate())
    },
    submitFileRename() {
      this.$refs.fileForm.validate(valid => {
        if (!valid) return
        renameKnowledgeFile(this.fileForm).then(() => {
          this.$modal.msgSuccess('重命名成功')
          this.fileRenameVisible = false
          this.loadFiles()
        })
      })
    },
    handleDeleteFile(row) {
      if (!row.knowledgeFileId) return
      this.$modal.confirm('确认删除文件"' + row.fileName + '"吗？').then(() => delKnowledgeFile(row.knowledgeFileId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.loadFiles()
        this.getList()
      }).catch(() => {})
    },
    openRichText(row) {
      if (!row.knowledgeFileId) return
      listKnowledgeChunks({ knowledgeFileId: row.knowledgeFileId }).then(res => {
        const chunks = res.data || []
        this.richEditorOutline = { id: row.knowledgeFileId, title: row.fileName, level: 3 }
        this.richTextHtml = ''
        this.richTextText = chunks.map(item => item.chunkTitle + '\n' + item.chunkContent).join('\n\n')
        this.editorVisible = true
      })
    },
    saveKnowledgePreview(payload, done) {
      this.richTextHtml = payload.html || ''
      this.richTextText = payload.text || ''
      if (typeof done === 'function') done(true)
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
    handleUploadExceed() {
      this.$modal.msgWarning('每次最多上传5个文件')
    },
    normalizeStatus(status) {
      if (status === 'success') return 'success'
      if (status === 'fail') return 'fail'
      if (status === 'parsing') return 'parsing'
      return 'uploaded'
    },
    statusText(row) {
      const status = this.normalizeStatus(row.parseStatus)
      if (status === 'success') return '解析成功'
      if (status === 'fail') return '解析失败'
      if (status === 'parsing') return '解析中：' + (row.parseProgress || 10) + '%'
      return '待解析'
    },
    canRetry(row) {
      const status = this.normalizeStatus(row.parseStatus)
      return row.knowledgeFileId && (status === 'uploaded' || status === 'fail')
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
  grid-template-columns: 268px minmax(0, 1fr);
  gap: 12px;
}
.library-sidebar,
.content-panel {
  background: #fff;
  min-height: calc(100vh - 84px);
}
.library-sidebar {
  display: flex;
  flex-direction: column;
  padding: 12px 10px;
  border-radius: 0 8px 8px 0;
}
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 18px;
  color: #111827;
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
  border-bottom: 1px solid #edf0f5;
}
.library-item:hover,
.library-item.active {
  background: #edf3ff;
}
.library-main {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr) 58px;
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
  color: #111827;
}
.library-actions {
  opacity: 0;
  display: flex;
  justify-content: flex-end;
  gap: 4px;
}
.library-item:hover .library-actions,
.library-item.active .library-actions {
  opacity: 1;
}
.library-actions .el-button {
  padding: 0;
  color: #5f6b7a;
}
.library-meta {
  display: flex;
  gap: 10px;
  margin: 9px 0 0 30px;
  color: #6b7280;
  font-size: 12px;
}
.empty-side,
.empty-tip {
  text-align: center;
  color: #8a94a6;
  margin-top: 26px;
}
.create-button {
  width: 100%;
  height: 40px;
  margin-top: 14px;
  border: 0;
  background: #4368ff;
}
.content-panel {
  padding: 0;
  border-radius: 8px 0 0 8px;
  overflow: hidden;
}
.home-panel {
  min-height: calc(100vh - 84px);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}
.home-visual {
  position: absolute;
  top: 0;
  left: 27%;
  right: 27%;
  height: 160px;
  background: linear-gradient(180deg, #eaf4ff 0%, rgba(234, 244, 255, 0) 100%);
  border-radius: 0 0 16px 16px;
}
.home-content {
  position: relative;
  width: min(820px, 100%);
  text-align: center;
}
.home-content h1 {
  font-size: 26px;
  font-weight: 500;
  color: #111827;
  margin: 0 0 22px;
  letter-spacing: 0;
}
.home-content > p {
  width: min(620px, 100%);
  margin: 0 auto 44px;
  color: #768196;
  line-height: 1.7;
}
.home-features {
  display: grid;
  grid-template-columns: 1fr 100px 1fr 100px 1fr;
  align-items: start;
  margin-bottom: 44px;
}
.feature-item i {
  width: 42px;
  height: 42px;
  line-height: 42px;
  border-radius: 8px;
  background: #e8f1ff;
  color: #2f6bff;
  font-size: 25px;
}
.feature-item h3 {
  font-size: 18px;
  font-weight: 500;
  color: #1f2937;
  margin: 28px 0 16px;
}
.feature-item p {
  margin: 0;
  color: #768196;
  line-height: 1.7;
}
.feature-line {
  height: 1px;
  margin-top: 22px;
  background: #dce4f2;
}
.home-create {
  width: 168px;
  height: 40px;
  border: 0;
  background: #4368ff;
}
.detail-panel {
  min-height: calc(100vh - 84px);
  padding: 22px;
  display: flex;
  flex-direction: column;
}
.panel-header {
  border-bottom: 1px solid #e5e9f2;
  padding-bottom: 16px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title span {
  width: 4px;
  height: 18px;
  background: #2f6bff;
  border-radius: 3px;
}
.section-title h2 {
  font-size: 18px;
  font-weight: 500;
  margin: 0;
  color: #111827;
}
.upload-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 18px 0;
}
.upload-card {
  height: 102px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.upload-card ::v-deep .el-upload,
.upload-card ::v-deep .el-upload-dragger {
  width: 100%;
  height: 100%;
}
.upload-card ::v-deep .el-upload-dragger {
  border: 0;
  border-radius: 6px;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.upload-card.normal {
  background: #edf3ff;
}
.upload-card.extract {
  background: #f2eafa;
  flex-direction: column;
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
.upload-card i {
  font-size: 18px;
  margin-bottom: 8px;
}
.upload-hint {
  color: #818b9d;
  margin-top: 12px;
  font-size: 12px;
}
.file-table {
  margin-top: 4px;
}
.status-cell {
  min-height: 36px;
  color: #606266;
}
.status-cell > span::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
  background: #9aa3af;
}
.status-success > span::before {
  background: #67c23a;
}
.status-fail > span::before {
  background: #f56c6c;
}
.status-parsing > span::before {
  background: #67c23a;
}
.status-parsing ::v-deep .el-progress {
  width: 96px;
  margin-top: 6px;
}
.header-refresh {
  padding: 0 0 0 6px;
}
.knowledge-empty {
  flex: 1;
  min-height: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8a94a6;
}
.knowledge-empty i {
  font-size: 78px;
  color: #e7ebf2;
  margin-bottom: 18px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: auto;
  padding-top: 14px;
}
.pager > span:first-child {
  margin-right: auto;
}
.page-size {
  width: 120px;
}
.knowledge-dialog ::v-deep .el-dialog__body {
  padding-bottom: 18px;
}
.extract-dialog ::v-deep .el-upload,
.extract-dialog ::v-deep .el-upload-dragger {
  width: 100%;
}
.extract-form {
  margin-top: 18px;
}
.option-owner {
  float: right;
  color: #8a94a6;
  font-size: 12px;
}
.extract-tip {
  background: #fff7e8;
  color: #8a6d3b;
  padding: 14px 18px;
  border-radius: 4px;
}
.extract-tip strong {
  color: #5c4a20;
}
.extract-tip p {
  margin: 8px 0 0 24px;
  font-size: 13px;
}
.knowledge-rich-editor {
  height: 620px;
}
.knowledge-editor-dialog ::v-deep .el-dialog__body {
  padding: 0;
}
@media (max-width: 1100px) {
  .knowledge-workspace {
    grid-template-columns: 1fr;
  }
  .upload-actions,
  .home-features {
    grid-template-columns: 1fr;
  }
  .feature-line {
    display: none;
  }
}
</style>
