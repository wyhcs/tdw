<template>
  <div class="gallery-workspace">
    <aside class="gallery-sidebar">
      <div class="sidebar-title">
        <i class="el-icon-s-fold"></i>
        <span>我的图库</span>
      </div>

      <div v-loading="galleryLoading" class="gallery-list">
        <div
          v-for="item in galleryList"
          :key="item.id"
          class="gallery-item"
          :class="{ active: currentGallery && currentGallery.id === item.id }"
          @click="selectGallery(item)"
        >
          <div class="gallery-main">
            <i class="el-icon-folder-opened"></i>
            <strong :title="item.galleryName">{{ item.galleryName }}</strong>
            <div class="gallery-actions" @click.stop>
              <el-tooltip content="编辑" placement="top">
                <button class="icon-action" type="button" @click="openGalleryDialog(item)" v-hasPermi="['bid:gallery:edit']">
                  <i class="el-icon-edit"></i>
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="icon-action danger" type="button" @click="removeGallery(item)" v-hasPermi="['bid:gallery:remove']">
                  <i class="el-icon-delete"></i>
                </button>
              </el-tooltip>
            </div>
          </div>
          <div class="gallery-meta">
            <span>{{ item.createBy || '-' }}</span>
            <span>{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <div v-if="!galleryList.length && !galleryLoading" class="empty-tip">--没有更多图库了--</div>
      </div>

      <el-button class="create-button" type="primary" @click="openGalleryDialog()" v-hasPermi="['bid:gallery:add']">
        新建图库
      </el-button>
    </aside>

    <main class="gallery-stage">
      <section v-if="!currentGallery" class="intro-panel">
        <div class="intro-bg intro-bg-one"></div>
        <div class="intro-bg intro-bg-two"></div>
        <div class="intro-content">
          <h1>专属图片库 · AI 智能解析 · 标书一键引用</h1>
          <p>上传专属优质图片至个人图库中，AI 将自动解析图片内容，在AI标书生成时可直接选用引用。</p>
          <div class="intro-flow">
            <div class="intro-feature">
              <span class="feature-icon"><i class="el-icon-camera"></i></span>
              <h3>智能抽取方案图片</h3>
              <p>提前在图片库上传素材，支持直接上传图片，或上传历史技术方案，系统将自动从中抽取图片。</p>
            </div>
            <div class="intro-line"></div>
            <div class="intro-feature">
              <span class="feature-icon"><i class="el-icon-collection"></i></span>
              <h3>标书配图智能调用</h3>
              <p>生成标书时，在图片设置里开启个人图库，系统将自动引用库中适配的图片。</p>
            </div>
          </div>
          <el-button type="primary" class="intro-button" @click="openGalleryDialog()" v-hasPermi="['bid:gallery:add']">
            <i class="el-icon-plus"></i>
            新建图库
          </el-button>
        </div>
      </section>

      <section v-else class="gallery-content" :class="{ 'with-detail': imageList.length }">
        <section class="image-main">
          <header class="panel-header">
            <div class="section-title">
              <span></span>
              <h2>{{ currentGallery.galleryName }}</h2>
            </div>
            <p>{{ currentGallery.description || '我的图库' }}</p>
          </header>

          <section class="upload-actions">
            <el-upload
              :key="imageUploadKey"
              class="upload-card normal"
              drag
              action="#"
              multiple
              :limit="5"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageUpload"
              :on-exceed="handleImageExceed"
              accept=".png,.jpg,.jpeg"
              :disabled="uploading"
              v-hasPermi="['bid:gallery:upload']"
            >
              <i class="el-icon-upload2"></i>
              <div class="upload-title">点击或拖拽上传图片</div>
              <div class="upload-hint">单个文件大小不超过5MB，支持以下文件格式： PNG、JPG、JPEG，每次上传5个</div>
            </el-upload>

            <el-upload
              :key="docUploadKey"
              class="upload-card extract"
              drag
              action="#"
              multiple
              :limit="5"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleDocExtract"
              :on-exceed="handleDocExceed"
              accept=".doc,.docx"
              :disabled="uploading"
              v-hasPermi="['bid:gallery:upload']"
            >
              <i class="el-icon-upload2"></i>
              <div class="upload-title">文档抽图</div>
              <div class="upload-hint">文件大小不超过50MB，支持以下文件格式： docx、doc</div>
            </el-upload>
          </section>

          <div v-if="imageList.length" class="batch-bar">
            <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="toggleAll">全选</el-checkbox>
            <span>已选{{ selectedIds.length }}张</span>
            <el-button type="danger" size="small" @click="deleteSelected" v-hasPermi="['bid:gallery:remove']">删除选中项</el-button>
            <el-button size="small" type="danger" plain @click="deleteFailed">删除失败项</el-button>
          </div>

          <div v-loading="imageLoading || uploading" class="image-body">
            <div v-if="imageList.length" class="image-grid">
              <article
                v-for="image in imageList"
                :key="image.id"
                class="image-card"
                :class="{ active: selectedImage && selectedImage.id === image.id }"
                @click="selectImage(image)"
              >
                <el-checkbox
                  class="image-check"
                  :value="selectedIds.indexOf(image.id) !== -1"
                  @change="checked => toggleImage(image, checked)"
                  @click.native.stop
                />
                <img :src="buildImageUrl(image.imageUrl)" :alt="image.imageName">
                <div class="image-caption">
                  <span :title="image.imageName">{{ image.imageName }}</span>
                  <a class="download-link" :href="buildImageUrl(image.imageUrl)" :download="downloadName(image)" @click.stop>
                    <i class="el-icon-download"></i>
                    下载
                  </a>
                </div>
              </article>
            </div>
            <div v-else class="empty-image-state">
              <div class="empty-illustration">
                <i class="el-icon-picture-outline"></i>
                <span>+</span>
              </div>
              <p>暂无图片信息 点击上方上传</p>
            </div>
          </div>

          <div class="pager">
            <el-pagination
              background
              :current-page.sync="imageQuery.pageNum"
              :page-size="imageQuery.pageSize"
              :page-sizes="[20, 40, 60]"
              :total="imageTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </section>

        <aside v-if="imageList.length" class="detail-panel">
          <template v-if="selectedImage">
            <img class="preview" :src="buildImageUrl(selectedImage.imageUrl)" :alt="selectedImage.imageName">
            <el-form label-position="top">
              <el-form-item label="标题（AI总结）">
                <el-input v-model="imageForm.imageName" />
              </el-form-item>
              <el-form-item>
                <template slot="label">
                  关键字 <span class="hint">在末尾键入关键词并回车即可添加新关键词，保存后生效</span>
                </template>
                <div class="tag-editor">
                  <el-tag
                    v-for="tag in imageTags"
                    :key="tag"
                    closable
                    type="warning"
                    @close="removeTag(tag)"
                  >{{ tag }}</el-tag>
                  <el-input
                    v-model="tagInput"
                    class="tag-input"
                    size="small"
                    placeholder="输入关键词"
                    @keyup.enter.native="addTag"
                  />
                </div>
              </el-form-item>
              <el-button class="save-button" type="primary" :disabled="!isImageChanged" @click="saveImage" v-hasPermi="['bid:gallery:edit']">
                保存修改
              </el-button>
            </el-form>
          </template>
          <div v-else class="detail-empty">
            <div class="empty-illustration small">
              <i class="el-icon-picture-outline"></i>
              <span>+</span>
            </div>
            <p>请选择图片并预览</p>
          </div>
        </aside>
      </section>
    </main>

    <el-dialog :title="galleryForm.id ? '编辑图库' : '新增图库'" :visible.sync="galleryDialogVisible" width="520px" append-to-body>
      <el-form ref="galleryForm" :model="galleryForm" :rules="galleryRules" label-width="92px">
        <el-form-item label="图库名称" prop="galleryName">
          <el-input v-model="galleryForm.galleryName" placeholder="请输入图库名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="图库描述">
          <el-input v-model="galleryForm.description" type="textarea" :rows="5" placeholder="请输入图库描述" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="galleryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGallery">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addGallery, deleteGallery, deleteGalleryImage, extractDocImage, listGalleries, listGalleryImages, updateGallery, updateGalleryImage, uploadGalleryImage } from '@/api/bid/gallery'

export default {
  name: 'BidGallery',
  data() {
    return {
      galleryLoading: false,
      imageLoading: false,
      uploading: false,
      galleryList: [],
      imageList: [],
      imageTotal: 0,
      currentGallery: null,
      selectedImage: null,
      selectedIds: [],
      checkAll: false,
      galleryDialogVisible: false,
      galleryForm: { id: undefined, galleryName: '', description: '' },
      imageForm: { id: undefined, imageName: '', imageTags: '' },
      imageTags: [],
      tagInput: '',
      originalImageForm: '',
      imageUploadKey: 1,
      docUploadKey: 1,
      imageQuery: { pageNum: 1, pageSize: 20 },
      galleryRules: {
        galleryName: [{ required: true, message: '图库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isImageChanged() {
      return JSON.stringify({ imageName: this.imageForm.imageName, imageTags: this.imageTags.join(',') }) !== this.originalImageForm
    },
    isIndeterminate() {
      return this.selectedIds.length > 0 && this.selectedIds.length < this.imageList.length
    }
  },
  created() {
    this.loadGalleries()
  },
  methods: {
    loadGalleries(preserveId) {
      const selectedId = preserveId || (this.currentGallery && this.currentGallery.id)
      this.galleryLoading = true
      listGalleries({ pageNum: 1, pageSize: 100 }).then(res => {
        this.galleryList = res.rows || []
        if (selectedId) {
          const next = this.galleryList.find(item => item.id === selectedId)
          if (next) {
            this.currentGallery = next
          }
        }
      }).finally(() => { this.galleryLoading = false })
    },
    selectGallery(row) {
      this.currentGallery = row
      this.selectedImage = null
      this.selectedIds = []
      this.checkAll = false
      this.imageQuery.pageNum = 1
      this.loadImages()
    },
    loadImages() {
      if (!this.currentGallery) return
      const previousId = this.selectedImage && this.selectedImage.id
      this.imageLoading = true
      listGalleryImages({
        pageNum: this.imageQuery.pageNum,
        pageSize: this.imageQuery.pageSize,
        galleryId: this.currentGallery.id
      }).then(res => {
        this.imageList = res.rows || []
        this.imageTotal = res.total || this.imageList.length
        this.selectedIds = this.selectedIds.filter(id => this.imageList.some(item => item.id === id))
        this.checkAll = this.imageList.length > 0 && this.selectedIds.length === this.imageList.length
        if (previousId) {
          const current = this.imageList.find(item => item.id === previousId)
          if (current) {
            this.selectImage(current)
          } else {
            this.clearSelectedImage()
          }
        }
      }).finally(() => { this.imageLoading = false })
    },
    openGalleryDialog(row) {
      this.galleryForm = row
        ? { id: row.id, galleryName: row.galleryName, description: row.description || '' }
        : { id: undefined, galleryName: '', description: '' }
      this.galleryDialogVisible = true
      this.$nextTick(() => this.$refs.galleryForm && this.$refs.galleryForm.clearValidate())
    },
    submitGallery() {
      this.$refs.galleryForm.validate(valid => {
        if (!valid) return
        const editingId = this.galleryForm.id
        const action = editingId ? updateGallery(this.galleryForm) : addGallery(this.galleryForm)
        action.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.galleryDialogVisible = false
          this.loadGalleries(editingId)
        })
      })
    },
    removeGallery(row) {
      this.$modal.confirm('确认删除图库"' + row.galleryName + '"吗？').then(() => deleteGallery(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.currentGallery && this.currentGallery.id === row.id) {
          this.currentGallery = null
          this.imageList = []
          this.imageTotal = 0
          this.clearSelectedImage()
        }
        this.loadGalleries()
      }).catch(() => {})
    },
    handleImageUpload(file) {
      if (!this.currentGallery || !file.raw) return
      if (!this.validateFile(file.raw, ['png', 'jpg', 'jpeg'], 5, '图片')) return
      const form = new FormData()
      form.append('galleryId', this.currentGallery.id)
      form.append('file', file.raw)
      this.uploading = true
      uploadGalleryImage(form).then(() => {
        this.$modal.msgSuccess('上传成功，已生成标题和关键词')
        this.afterUploadRefresh()
      }).finally(() => {
        this.uploading = false
        this.imageUploadKey++
      })
    },
    handleDocExtract(file) {
      if (!this.currentGallery || !file.raw) return
      if (!this.validateFile(file.raw, ['doc', 'docx'], 50, '文档')) return
      const form = new FormData()
      form.append('galleryId', this.currentGallery.id)
      form.append('file', file.raw)
      this.uploading = true
      extractDocImage(form).then(res => {
        const count = Array.isArray(res.data) ? res.data.length : 0
        this.$modal.msgSuccess(count ? '文档抽图完成，新增' + count + '张图片' : '文档抽图完成')
        this.afterUploadRefresh()
      }).finally(() => {
        this.uploading = false
        this.docUploadKey++
      })
    },
    afterUploadRefresh() {
      this.clearSelectedImage()
      this.imageQuery.pageNum = 1
      this.loadImages()
      this.loadGalleries(this.currentGallery && this.currentGallery.id)
    },
    handleImageExceed() {
      this.$modal.msgWarning('每次最多上传5张图片')
    },
    handleDocExceed() {
      this.$modal.msgWarning('每次最多上传5个文档')
    },
    selectImage(image) {
      this.selectedImage = image
      this.imageForm = { id: image.id, imageName: image.imageName, imageTags: image.imageTags || '' }
      this.imageTags = (image.imageTags || '').split(',').map(item => item.trim()).filter(Boolean)
      this.originalImageForm = JSON.stringify({ imageName: this.imageForm.imageName, imageTags: this.imageTags.join(',') })
      this.tagInput = ''
    },
    clearSelectedImage() {
      this.selectedImage = null
      this.imageForm = { id: undefined, imageName: '', imageTags: '' }
      this.imageTags = []
      this.tagInput = ''
      this.originalImageForm = ''
    },
    toggleImage(image, checked) {
      const id = image.id
      if (checked && this.selectedIds.indexOf(id) === -1) {
        this.selectedIds.push(id)
      }
      if (!checked) {
        this.selectedIds = this.selectedIds.filter(item => item !== id)
      }
      this.checkAll = this.imageList.length > 0 && this.selectedIds.length === this.imageList.length
    },
    toggleAll(checked) {
      this.selectedIds = checked ? this.imageList.map(item => item.id) : []
    },
    deleteSelected() {
      if (!this.selectedIds.length) {
        this.$modal.msgWarning('请先选择图片')
        return
      }
      this.$modal.confirm('确认删除选中的图片吗？').then(() => deleteGalleryImage(this.selectedIds.join(','))).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.selectedImage && this.selectedIds.indexOf(this.selectedImage.id) !== -1) {
          this.clearSelectedImage()
        }
        this.selectedIds = []
        this.checkAll = false
        this.loadImages()
        this.loadGalleries(this.currentGallery && this.currentGallery.id)
      }).catch(() => {})
    },
    deleteFailed() {
      this.$modal.msgSuccess('暂无失败项')
    },
    addTag() {
      const value = this.tagInput.trim()
      if (value && this.imageTags.indexOf(value) === -1) {
        this.imageTags.push(value)
      }
      this.tagInput = ''
    },
    removeTag(tag) {
      this.imageTags = this.imageTags.filter(item => item !== tag)
    },
    saveImage() {
      const payload = {
        id: this.imageForm.id,
        imageName: this.imageForm.imageName,
        imageTags: this.imageTags.join(',')
      }
      updateGalleryImage(payload).then(() => {
        this.$modal.msgSuccess('保存成功')
        if (this.selectedImage) {
          this.selectedImage.imageName = payload.imageName
          this.selectedImage.imageTags = payload.imageTags
        }
        const row = this.imageList.find(item => item.id === payload.id)
        if (row) {
          row.imageName = payload.imageName
          row.imageTags = payload.imageTags
        }
        this.originalImageForm = JSON.stringify({ imageName: payload.imageName, imageTags: payload.imageTags })
      })
    },
    handleSizeChange(size) {
      this.imageQuery.pageSize = size
      this.imageQuery.pageNum = 1
      this.loadImages()
    },
    handlePageChange(page) {
      this.imageQuery.pageNum = page
      this.loadImages()
    },
    validateFile(file, types, maxMb, label) {
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      if (types.indexOf(ext) === -1) {
        this.$modal.msgWarning(label + '格式不符合要求')
        return false
      }
      if (file.size > maxMb * 1024 * 1024) {
        this.$modal.msgWarning(label + '大小不能超过' + maxMb + 'MB')
        return false
      }
      return true
    },
    buildImageUrl(url) {
      if (!url) return ''
      if (/^(https?:|data:|blob:)/i.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    },
    downloadName(image) {
      if (image.originalName) return image.originalName
      const ext = image.imageType ? '.' + image.imageType : ''
      return (image.imageName || '图库图片') + ext
    },
    formatTime(value) {
      return value ? this.parseTime(value, '{y}-{m}-{d} {h}:{i}') : '-'
    }
  }
}
</script>

<style scoped>
.gallery-workspace {
  min-height: calc(100vh - 84px);
  background: #f4f7fb;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 12px;
}

.gallery-sidebar {
  background: #fff;
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  padding: 12px;
  border-radius: 0 8px 8px 0;
}

.sidebar-title {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #1f2937;
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 14px;
}

.gallery-list {
  flex: 1;
  overflow: auto;
}

.gallery-item {
  padding: 12px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background .18s ease;
}

.gallery-item + .gallery-item {
  margin-top: 6px;
}

.gallery-item:hover,
.gallery-item.active {
  background: #edf3ff;
}

.gallery-main {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr) 54px;
  align-items: center;
  gap: 6px;
}

.gallery-main > i {
  color: #f5a400;
  font-size: 16px;
}

.gallery-main strong {
  color: #1f2937;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gallery-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  opacity: 0;
  transition: opacity .18s ease;
}

.gallery-item:hover .gallery-actions,
.gallery-item.active .gallery-actions {
  opacity: 1;
}

.icon-action {
  width: 23px;
  height: 23px;
  border: 0;
  background: transparent;
  color: #667085;
  cursor: pointer;
  border-radius: 4px;
  padding: 0;
}

.icon-action:hover {
  background: #dce7ff;
  color: #2f6bff;
}

.icon-action.danger:hover {
  background: #ffecec;
  color: #ff4d4f;
}

.gallery-meta {
  display: flex;
  gap: 10px;
  margin: 9px 0 0 28px;
  color: #737b8a;
  font-size: 12px;
  white-space: nowrap;
}

.gallery-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-tip {
  color: #9aa3af;
  margin-top: 24px;
  text-align: center;
}

.create-button {
  width: 100%;
  margin-top: 14px;
  border: 0;
  background: linear-gradient(90deg, #2f6bff 0%, #7b55f5 100%);
}

.gallery-stage {
  min-width: 0;
  min-height: calc(100vh - 84px);
}

.intro-panel {
  position: relative;
  min-height: calc(100vh - 84px);
  overflow: hidden;
  background: linear-gradient(180deg, #eaf4ff 0%, #fff 48%);
  border-radius: 8px 0 0 8px;
}

.intro-bg {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.intro-bg-one {
  width: 190px;
  height: 190px;
  right: 150px;
  top: 150px;
  background: radial-gradient(circle, rgba(47, 107, 255, .22), rgba(47, 107, 255, 0) 68%);
}

.intro-bg-two {
  width: 70px;
  height: 70px;
  right: 240px;
  top: 95px;
  background: radial-gradient(circle, rgba(51, 211, 200, .35), rgba(51, 211, 200, 0) 70%);
}

.intro-content {
  width: min(760px, 90%);
  margin: 0 auto;
  padding-top: 220px;
  text-align: center;
  color: #1f2937;
}

.intro-content h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.35;
  font-weight: 700;
}

.intro-content > p {
  width: min(560px, 100%);
  margin: 22px auto 0;
  color: #758093;
  line-height: 1.7;
}

.intro-flow {
  display: grid;
  grid-template-columns: 1fr 240px 1fr;
  gap: 28px;
  align-items: start;
  margin: 38px 0 44px;
}

.intro-feature {
  text-align: center;
}

.feature-icon {
  width: 54px;
  height: 54px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #2f6bff;
  font-size: 30px;
  background: #edf3ff;
  border-radius: 8px;
}

.intro-feature h3 {
  margin: 20px 0 14px;
  font-size: 17px;
  color: #1f2937;
}

.intro-feature p {
  margin: 0;
  color: #7b8494;
  line-height: 1.7;
}

.intro-line {
  height: 1px;
  background: #d7dfef;
  margin-top: 28px;
}

.intro-button {
  width: 150px;
  border: 0;
  background: linear-gradient(90deg, #2f6bff 0%, #7b55f5 100%);
}

.gallery-content {
  min-height: calc(100vh - 84px);
  background: #fff;
  border-radius: 8px 0 0 8px;
}

.gallery-content.with-detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 374px;
}

.image-main {
  min-width: 0;
  padding: 18px 20px 0;
}

.panel-header {
  border-bottom: 1px solid #e5e9f2;
  padding-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title span {
  width: 4px;
  height: 18px;
  background: #2f6bff;
  border-radius: 3px;
}

.section-title h2 {
  font-size: 20px;
  line-height: 1.3;
  color: #1f2937;
  margin: 0;
}

.panel-header p {
  margin: 10px 0 0 16px;
  color: #7b8494;
}

.upload-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 18px 0 20px;
}

.upload-card {
  height: 104px;
  border-radius: 7px;
}

.upload-card.normal ::v-deep .el-upload-dragger {
  background: #edf3ff;
}

.upload-card.extract ::v-deep .el-upload-dragger {
  background: #f3eafa;
}

.upload-card ::v-deep .el-upload,
.upload-card ::v-deep .el-upload-dragger {
  width: 100%;
  height: 100%;
  border: 0;
}

.upload-card ::v-deep .el-upload-dragger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
}

.upload-card i,
.upload-title {
  color: #2f6bff;
  font-weight: 600;
}

.upload-card.extract i,
.upload-card.extract .upload-title {
  color: #6f4cf6;
}

.upload-card i {
  font-size: 16px;
}

.upload-title {
  margin-top: 6px;
  font-size: 15px;
}

.upload-hint {
  color: #8d96a6;
  margin-top: 12px;
  font-size: 12px;
  line-height: 1.4;
}

.batch-bar {
  min-height: 48px;
  background: #f5f6f8;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 16px;
  margin-bottom: 16px;
}

.batch-bar .el-button:first-of-type {
  margin-left: auto;
}

.image-body {
  min-height: 500px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 192px);
  gap: 18px;
  align-content: start;
}

.image-card {
  width: 192px;
  height: 150px;
  border-radius: 7px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  background: #f2f5fa;
  border: 2px solid transparent;
}

.image-card.active {
  border-color: #2f6bff;
}

.image-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-check {
  position: absolute;
  top: 8px;
  left: 10px;
  z-index: 2;
}

.image-caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  min-height: 36px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px;
  background: rgba(0, 0, 0, .68);
  color: #fff;
}

.image-caption span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.download-link {
  color: #fff;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
}

.empty-image-state,
.detail-empty {
  min-height: 430px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #7b8494;
}

.empty-illustration {
  position: relative;
  width: 120px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b7c7ef;
  font-size: 72px;
}

.empty-illustration span {
  position: absolute;
  right: 18px;
  bottom: 10px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #b7c7ef;
  color: #fff;
  font-size: 22px;
  line-height: 28px;
}

.empty-illustration.small {
  transform: scale(.9);
}

.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin: 20px 0 6px;
  padding-top: 10px;
}

.detail-panel {
  border-left: 1px solid #e5e9f2;
  padding: 18px 22px;
  min-width: 0;
}

.preview {
  width: 100%;
  height: 220px;
  object-fit: contain;
  background: #f7f9fc;
  border-radius: 6px;
  margin-bottom: 20px;
}

.hint {
  color: #8d96a6;
  font-size: 12px;
  font-weight: 400;
  margin-left: 4px;
}

.tag-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 60px;
  padding: 8px;
}

.tag-editor .el-tag {
  margin: 0 8px 8px 0;
}

.tag-input {
  width: 150px;
}

.save-button {
  display: block;
  width: 154px;
  margin: 26px auto 0;
}

@media (max-width: 1280px) {
  .gallery-content.with-detail {
    grid-template-columns: 1fr;
  }

  .detail-panel {
    border-left: 0;
    border-top: 1px solid #e5e9f2;
  }
}

@media (max-width: 1100px) {
  .gallery-workspace {
    grid-template-columns: 1fr;
  }

  .gallery-sidebar {
    min-height: auto;
    border-radius: 0;
  }

  .upload-actions,
  .intro-flow {
    grid-template-columns: 1fr;
  }

  .intro-line {
    display: none;
  }

  .intro-content {
    padding-top: 90px;
    padding-bottom: 60px;
  }
}
</style>
