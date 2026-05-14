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
            <i class="el-icon-folder"></i>
            <strong>{{ item.galleryName }}</strong>
            <el-dropdown trigger="click" @command="command => handleGalleryCommand(command, item)">
              <span class="more" @click.stop>...</span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="gallery-meta">
            <span>{{ item.createBy || 'Qiuqingy...' }}</span>
            <span>{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
        </div>
        <div class="empty-tip">--没有更多图库了--</div>
      </div>
      <el-button class="create-button" type="primary" @click="openGalleryDialog()">新建图库</el-button>
    </aside>

    <main class="image-panel">
      <section class="image-main">
        <header class="panel-header">
          <div class="section-title">
            <span></span>
            <h2>{{ currentGallery ? currentGallery.galleryName : '我的图库' }}</h2>
          </div>
          <p>{{ currentGallery ? currentGallery.description || '我的图库' : '请先新建或选择图库' }}</p>
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
            :on-change="handleImageUpload"
            accept=".png,.jpg,.jpeg"
            :disabled="!currentGallery"
          >
            <i class="el-icon-upload2"></i>
            <div class="upload-title">点击或拖拽上传图片</div>
            <div class="upload-hint">单个文件大小不超过5MB，支持以下文件格式： PNG、JPG、JPEG，每次上传5个</div>
          </el-upload>

          <el-upload
            class="upload-card extract"
            drag
            action="#"
            multiple
            :limit="5"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleDocExtract"
            accept=".doc,.docx"
            :disabled="!currentGallery"
          >
            <i class="el-icon-upload2"></i>
            <div class="upload-title">文档抽图</div>
            <div class="upload-hint">文件大小不超过50MB，支持以下文件格式： docx、doc</div>
          </el-upload>
        </section>

        <div class="batch-bar">
          <el-checkbox v-model="checkAll" @change="toggleAll">全选</el-checkbox>
          <span>已选{{ selectedIds.length }}张</span>
          <el-button type="danger" size="small" @click="deleteSelected">删除选中项</el-button>
          <el-button size="small" type="danger" plain @click="deleteFailed">删除失败项</el-button>
        </div>

        <div v-loading="imageLoading" class="image-grid">
          <article
            v-for="image in imageList"
            :key="image.id"
            class="image-card"
            :class="{ active: selectedImage && selectedImage.id === image.id }"
            @click="selectImage(image)"
          >
            <el-checkbox class="image-check" :value="selectedIds.indexOf(image.id) !== -1" @change="checked => toggleImage(image, checked)" @click.native.stop />
            <img :src="image.imageUrl" :alt="image.imageName">
            <div class="image-caption">
              <span>{{ image.imageName }}</span>
              <a :href="image.imageUrl" :download="image.imageName" @click.stop><i class="el-icon-download"></i></a>
            </div>
          </article>
        </div>
        <el-empty v-if="currentGallery && !imageLoading && !imageList.length" description="暂无图片" />
        <div class="pager">
          <span>共 {{ imageList.length }} 条</span>
          <span>共1页</span>
          <el-select size="small" value="20" class="page-size"><el-option label="20条/页" value="20" /></el-select>
          <el-button size="small" icon="el-icon-arrow-left" disabled></el-button>
          <el-button size="small" type="primary">1</el-button>
          <el-button size="small" icon="el-icon-arrow-right" disabled></el-button>
        </div>
      </section>

      <aside class="detail-panel" v-if="selectedImage">
        <img class="preview" :src="selectedImage.imageUrl" :alt="selectedImage.imageName">
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
          <el-button class="save-button" type="primary" :disabled="!isImageChanged" @click="saveImage">保存修改</el-button>
        </el-form>
      </aside>
    </main>

    <el-dialog :title="galleryForm.id ? '编辑图库' : '新建图库'" :visible.sync="galleryDialogVisible" width="520px" append-to-body>
      <el-form ref="galleryForm" :model="galleryForm" :rules="galleryRules" label-width="100px">
        <el-form-item label="图库名称" prop="galleryName">
          <el-input v-model="galleryForm.galleryName" placeholder="请输入图库名称" />
        </el-form-item>
        <el-form-item label="图库描述">
          <el-input v-model="galleryForm.description" type="textarea" :rows="3" placeholder="请输入图库描述" />
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
      galleryList: [],
      imageList: [],
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
      galleryRules: {
        galleryName: [{ required: true, message: '图库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isImageChanged() {
      return JSON.stringify({ imageName: this.imageForm.imageName, imageTags: this.imageTags.join(',') }) !== this.originalImageForm
    }
  },
  created() {
    this.loadGalleries()
  },
  methods: {
    loadGalleries() {
      this.galleryLoading = true
      listGalleries({ pageNum: 1, pageSize: 100 }).then(res => {
        this.galleryList = res.rows || []
        if (!this.currentGallery && this.galleryList.length) this.selectGallery(this.galleryList[0])
      }).finally(() => { this.galleryLoading = false })
    },
    selectGallery(row) {
      this.currentGallery = row
      this.selectedImage = null
      this.selectedIds = []
      this.checkAll = false
      this.loadImages()
    },
    loadImages() {
      if (!this.currentGallery) return
      this.imageLoading = true
      listGalleryImages({ pageNum: 1, pageSize: 200, galleryId: this.currentGallery.id }).then(res => {
        this.imageList = res.rows || []
        if (this.imageList.length) this.selectImage(this.imageList[0])
      }).finally(() => { this.imageLoading = false })
    },
    openGalleryDialog(row) {
      this.galleryForm = row ? { ...row } : { id: undefined, galleryName: '', description: '' }
      this.galleryDialogVisible = true
      this.$nextTick(() => this.$refs.galleryForm && this.$refs.galleryForm.clearValidate())
    },
    submitGallery() {
      this.$refs.galleryForm.validate(valid => {
        if (!valid) return
        const action = this.galleryForm.id ? updateGallery(this.galleryForm) : addGallery(this.galleryForm)
        action.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.galleryDialogVisible = false
          this.currentGallery = null
          this.loadGalleries()
        })
      })
    },
    handleGalleryCommand(command, item) {
      if (command === 'edit') this.openGalleryDialog(item)
      if (command === 'delete') this.removeGallery(item)
    },
    removeGallery(row) {
      this.$modal.confirm('确认删除图库"' + row.galleryName + '"吗？').then(() => deleteGallery(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.currentGallery = null
        this.imageList = []
        this.loadGalleries()
      }).catch(() => {})
    },
    handleImageUpload(file) {
      if (!this.currentGallery || !file.raw || !this.validateFile(file.raw, ['png', 'jpg', 'jpeg'], 5)) return
      const form = new FormData()
      form.append('galleryId', this.currentGallery.id)
      form.append('imageName', file.name.replace(/\.[^.]+$/, ''))
      form.append('file', file.raw)
      uploadGalleryImage(form).then(() => {
        this.$modal.msgSuccess('上传成功')
        this.loadImages()
        this.loadGalleries()
      })
    },
    handleDocExtract(file) {
      if (!this.currentGallery || !file.raw || !this.validateFile(file.raw, ['doc', 'docx'], 50)) return
      const form = new FormData()
      form.append('galleryId', this.currentGallery.id)
      form.append('file', file.raw)
      extractDocImage(form).then(() => {
        this.$modal.msgSuccess('文档抽图完成')
        this.loadImages()
        this.loadGalleries()
      })
    },
    selectImage(image) {
      this.selectedImage = image
      this.imageForm = { id: image.id, imageName: image.imageName, imageTags: image.imageTags || '' }
      this.imageTags = (image.imageTags || '').split(',').filter(Boolean)
      this.originalImageForm = JSON.stringify({ imageName: this.imageForm.imageName, imageTags: this.imageTags.join(',') })
    },
    toggleImage(image, checked) {
      const id = image.id
      if (checked && this.selectedIds.indexOf(id) === -1) this.selectedIds.push(id)
      if (!checked) this.selectedIds = this.selectedIds.filter(item => item !== id)
      this.checkAll = this.selectedIds.length === this.imageList.length
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
        this.selectedIds = []
        this.loadImages()
        this.loadGalleries()
      }).catch(() => {})
    },
    deleteFailed() {
      this.$modal.msgSuccess('暂无失败项')
    },
    addTag() {
      const value = this.tagInput.trim()
      if (value && this.imageTags.indexOf(value) === -1) this.imageTags.push(value)
      this.tagInput = ''
    },
    removeTag(tag) {
      this.imageTags = this.imageTags.filter(item => item !== tag)
    },
    saveImage() {
      updateGalleryImage({ id: this.imageForm.id, imageName: this.imageForm.imageName, imageTags: this.imageTags.join(',') }).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.loadImages()
      })
    },
    validateFile(file, types, maxMb) {
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      if (types.indexOf(ext) === -1 || file.size > maxMb * 1024 * 1024) {
        this.$modal.msgWarning('文件格式或大小不符合要求')
        return false
      }
      return true
    }
  }
}
</script>

<style scoped>
.gallery-workspace {
  min-height: calc(100vh - 84px);
  background: #f4f7fb;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 12px;
}
.gallery-sidebar {
  background: #fff;
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  padding: 14px 12px;
  border-radius: 0 8px 8px 0;
}
.sidebar-title {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 18px;
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
}
.gallery-item.active {
  background: #edf3ff;
}
.gallery-main {
  display: grid;
  grid-template-columns: 20px 1fr 26px;
  gap: 6px;
  align-items: center;
}
.gallery-main i {
  color: #f5a400;
}
.gallery-main strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.more {
  color: #303133;
  font-weight: 700;
}
.gallery-meta {
  display: flex;
  gap: 10px;
  margin: 10px 0 0 38px;
  color: #737b8a;
  font-size: 13px;
}
.empty-tip {
  margin-top: 24px;
  color: #9aa3af;
  text-align: center;
}
.create-button {
  width: 100%;
  margin-top: 14px;
}
.image-panel {
  background: #fff;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  min-height: calc(100vh - 84px);
}
.image-main {
  padding: 24px;
}
.detail-panel {
  border-left: 1px solid #e5e9f2;
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
}
.upload-card.normal {
  background: #edf3ff;
}
.upload-card.extract {
  background: #f3eafa;
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
.batch-bar {
  height: 54px;
  background: #f5f6f8;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 0 18px;
  margin-bottom: 18px;
}
.batch-bar .el-button:first-of-type {
  margin-left: auto;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 220px));
  gap: 18px;
  min-height: 500px;
  align-content: start;
}
.image-card {
  width: 220px;
  height: 170px;
  border-radius: 8px;
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
}
.image-check {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}
.image-caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 34px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  background: rgba(0, 0, 0, .72);
  color: #fff;
}
.image-caption span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.image-caption a {
  color: #fff;
}
.preview {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 22px;
}
.hint {
  color: #8d96a6;
  font-size: 12px;
  font-weight: 400;
}
.tag-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 68px;
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
  width: 180px;
  margin: 26px auto 0;
}
.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
}
.pager > span:first-child {
  margin-right: auto;
}
.page-size {
  width: 120px;
}
@media (max-width: 1280px) {
  .image-panel {
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
  .upload-actions {
    grid-template-columns: 1fr;
  }
}
</style>
