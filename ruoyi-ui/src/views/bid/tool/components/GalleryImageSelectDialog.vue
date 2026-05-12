<template>
  <el-dialog title="选择图库图片" :visible.sync="innerVisible" width="860px" append-to-body>
    <div class="select-layout">
      <aside class="gallery-list">
        <el-input v-model="galleryQuery.galleryName" size="small" placeholder="搜索图库" clearable @keyup.enter.native="loadGalleries" />
        <el-table v-loading="galleryLoading" :data="galleryList" highlight-current-row @current-change="selectGallery">
          <el-table-column label="图库" prop="galleryName" min-width="140" show-overflow-tooltip />
          <el-table-column label="图片" prop="imageCount" width="70" align="center" />
        </el-table>
      </aside>
      <main class="image-list">
        <el-empty v-if="!currentGallery" description="请选择图库" />
        <el-empty v-else-if="!imageLoading && imageList.length === 0" description="暂无图片" />
        <div v-loading="imageLoading" class="image-grid" v-else>
          <article v-for="image in imageList" :key="image.id" :class="['image-card', { active: selectedImage && selectedImage.id === image.id }]" @click="selectedImage = image">
            <div class="thumb"><img :src="image.imageUrl" :alt="image.imageName"></div>
            <div class="name" :title="image.imageName">{{ image.imageName }}</div>
            <div class="desc" :title="image.description">{{ image.description || '无说明' }}</div>
          </article>
        </div>
      </main>
    </div>
    <div slot="footer">
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedImage" @click="confirm">插入图片</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listGalleries, listGalleryImages } from '@/api/bid/tool'

export default {
  name: 'GalleryImageSelectDialog',
  props: {
    visible: { type: Boolean, default: false }
  },
  data() {
    return {
      innerVisible: false,
      galleryLoading: false,
      imageLoading: false,
      galleryList: [],
      imageList: [],
      currentGallery: null,
      selectedImage: null,
      galleryQuery: { pageNum: 1, pageSize: 100, galleryName: undefined }
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(val) {
        this.innerVisible = val
        if (val) this.loadGalleries()
      }
    },
    innerVisible(val) {
      this.$emit('update:visible', val)
    }
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
      this.selectedImage = null
      this.loadImages()
    },
    loadImages() {
      this.imageLoading = true
      listGalleryImages({ pageNum: 1, pageSize: 200, galleryId: this.currentGallery.id }).then(res => {
        this.imageList = res.rows || []
      }).finally(() => {
        this.imageLoading = false
      })
    },
    confirm() {
      this.$emit('select', this.selectedImage)
      this.innerVisible = false
    }
  }
}
</script>

<style scoped>
.select-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 14px;
  min-height: 440px;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}
.image-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px;
  cursor: pointer;
}
.image-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, .14);
}
.thumb {
  aspect-ratio: 4 / 3;
  background: #f5f7fa;
  overflow: hidden;
  border-radius: 4px;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.name {
  margin-top: 8px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.desc {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
