<template>
  <div class="app-container gallery-page">
    <el-page-header content="私人图库" @back="$router.push('/bid/tool')" />

    <section class="gallery-layout">
      <aside class="gallery-sidebar">
        <div class="sidebar-toolbar">
          <el-input v-model="galleryQuery.galleryName" size="small" placeholder="搜索图库" clearable @keyup.enter.native="loadGalleries" />
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openGalleryDialog()">新增</el-button>
        </div>
        <el-table v-loading="galleryLoading" :data="galleryList" highlight-current-row @current-change="selectGallery">
          <el-table-column label="图库" min-width="160">
            <template slot-scope="scope">
              <div class="gallery-row">
                <div class="cover">
                  <img v-if="scope.row.coverUrl" :src="scope.row.coverUrl">
                  <i v-else class="el-icon-picture-outline" />
                </div>
                <div class="gallery-meta">
                  <div class="name">{{ scope.row.galleryName }}</div>
                  <div class="count">{{ scope.row.imageCount || 0 }} 张</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="86" align="center">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-edit" @click.stop="openGalleryDialog(scope.row)" />
              <el-button type="text" icon="el-icon-delete" @click.stop="removeGallery(scope.row)" />
            </template>
          </el-table-column>
        </el-table>
      </aside>

      <main class="image-panel">
        <div class="image-toolbar">
          <div>
            <h3>{{ currentGallery ? currentGallery.galleryName : '请选择图库' }}</h3>
            <p>{{ currentGallery ? currentGallery.description : '创建图库后上传图片，可在方案编辑器中插入为图片内容块。' }}</p>
          </div>
          <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="handleUploadChange" accept="image/*">
            <el-button type="primary" icon="el-icon-upload" :disabled="!currentGallery" v-hasPermi="['bid:tool:upload']">上传图片</el-button>
          </el-upload>
        </div>

        <el-empty v-if="!currentGallery" description="请选择或新增图库" />
        <el-empty v-else-if="!imageLoading && imageList.length === 0" description="暂无图片" />
        <div v-loading="imageLoading" class="image-grid" v-else>
          <article v-for="image in imageList" :key="image.id" class="image-card">
            <div class="thumb">
              <img :src="image.imageUrl" :alt="image.imageName">
            </div>
            <div class="image-info">
              <div class="image-name" :title="image.imageName">{{ image.imageName }}</div>
              <div class="image-desc" :title="image.description">{{ image.description || '无说明' }}</div>
            </div>
            <div class="image-actions">
              <el-button type="text" icon="el-icon-edit" @click="openImageDialog(image)">重命名</el-button>
              <el-button type="text" icon="el-icon-delete" @click="removeImage(image)">删除</el-button>
            </div>
          </article>
        </div>
      </main>
    </section>

    <el-dialog :title="galleryForm.id ? '编辑图库' : '新增图库'" :visible.sync="galleryDialogVisible" width="520px" append-to-body>
      <el-form ref="galleryForm" :model="galleryForm" :rules="galleryRules" label-width="92px">
        <el-form-item label="图库名称" prop="galleryName">
          <el-input v-model="galleryForm.galleryName" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="图库说明">
          <el-input v-model="galleryForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="galleryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGallery">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="重命名图片" :visible.sync="imageDialogVisible" width="520px" append-to-body>
      <el-form :model="imageForm" label-width="92px">
        <el-form-item label="图片名称">
          <el-input v-model="imageForm.imageName" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="图片说明">
          <el-input v-model="imageForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="imageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImage">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addGallery, deleteGallery, deleteGalleryImage, listGalleries, listGalleryImages, updateGallery, updateGalleryImage, uploadGalleryImage } from '@/api/bid/tool'

export default {
  name: 'BidToolGallery',
  data() {
    return {
      galleryLoading: false,
      imageLoading: false,
      galleryList: [],
      imageList: [],
      currentGallery: null,
      galleryQuery: { pageNum: 1, pageSize: 100, galleryName: undefined },
      galleryDialogVisible: false,
      imageDialogVisible: false,
      galleryForm: { id: undefined, galleryName: '', description: '' },
      imageForm: { id: undefined, imageName: '', description: '' },
      galleryRules: { galleryName: [{ required: true, message: '请输入图库名称', trigger: 'blur' }] }
    }
  },
  created() {
    this.loadGalleries()
  },
  methods: {
    loadGalleries() {
      this.galleryLoading = true
      listGalleries(this.galleryQuery).then(res => {
        this.galleryList = res.rows || []
        if (!this.currentGallery && this.galleryList.length) {
          this.selectGallery(this.galleryList[0])
        }
      }).finally(() => {
        this.galleryLoading = false
      })
    },
    selectGallery(row) {
      if (!row) return
      this.currentGallery = row
      this.loadImages()
    },
    loadImages() {
      if (!this.currentGallery) return
      this.imageLoading = true
      listGalleryImages({ pageNum: 1, pageSize: 200, galleryId: this.currentGallery.id }).then(res => {
        this.imageList = res.rows || []
      }).finally(() => {
        this.imageLoading = false
      })
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
    removeGallery(row) {
      this.$modal.confirm('确认删除图库"' + row.galleryName + '"吗？图库中的图片记录也会删除。').then(() => deleteGallery(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.currentGallery && this.currentGallery.id === row.id) {
          this.currentGallery = null
          this.imageList = []
        }
        this.loadGalleries()
      }).catch(() => {})
    },
    handleUploadChange(file) {
      if (!this.currentGallery || !file || !file.raw) return
      const data = new FormData()
      data.append('galleryId', this.currentGallery.id)
      data.append('imageName', file.name.replace(/\.[^.]+$/, ''))
      data.append('file', file.raw)
      uploadGalleryImage(data).then(() => {
        this.$modal.msgSuccess('上传成功')
        this.loadImages()
        this.loadGalleries()
      })
    },
    openImageDialog(image) {
      this.imageForm = { ...image }
      this.imageDialogVisible = true
    },
    submitImage() {
      updateGalleryImage(this.imageForm).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.imageDialogVisible = false
        this.loadImages()
      })
    },
    removeImage(image) {
      this.$modal.confirm('确认删除图片"' + image.imageName + '"吗？').then(() => deleteGalleryImage(image.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.loadImages()
        this.loadGalleries()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.gallery-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  margin-top: 20px;
  min-height: 640px;
}
.gallery-sidebar,
.image-panel {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  padding: 12px;
}
.sidebar-toolbar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-bottom: 12px;
}
.gallery-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cover {
  width: 48px;
  height: 36px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f5f7fa;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.gallery-meta .name {
  font-weight: 600;
  color: #303133;
}
.gallery-meta .count {
  font-size: 12px;
  color: #909399;
}
.image-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.image-toolbar h3 {
  margin: 0 0 6px;
}
.image-toolbar p {
  margin: 0;
  color: #606266;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.image-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}
.thumb {
  aspect-ratio: 4 / 3;
  background: #f5f7fa;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-info {
  padding: 10px;
}
.image-name {
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.image-desc {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.image-actions {
  display: flex;
  justify-content: space-between;
  padding: 0 10px 8px;
}
@media (max-width: 1100px) {
  .gallery-layout {
    grid-template-columns: 1fr;
  }
}
</style>
