<template>
  <div class="plan-home-page">
    <aside class="home-sidebar">
      <div class="side-title">≡ 我的方案</div>
      <el-input
        v-model="queryParams.title"
        size="small"
        clearable
        prefix-icon="el-icon-search"
        placeholder="搜索方案"
        @keyup.enter.native="handleQuery"
        @clear="handleQuery"
      />
      <div class="plan-scroll" v-loading="loading">
        <div v-if="planList.length" class="plan-list">
          <div
            v-for="item in planList"
            :key="item.id"
            class="plan-card"
            :class="{ active: selectedPlan && selectedPlan.id === item.id }"
            role="button"
            tabindex="0"
            @click="selectPlan(item)"
            @keyup.enter="selectPlan(item)"
          >
            <div class="plan-card-head">
              <span class="plan-name">{{ item.title }}</span>
              <span class="plan-actions">
                <el-tooltip content="编辑标题" placement="top">
                  <button type="button" class="plan-action-btn edit" @click.stop="renamePlan(item)" v-hasPermi="['bid:plan:edit']">
                    <i class="el-icon-edit-outline" />
                  </button>
                </el-tooltip>
                <el-tooltip content="删除项目" placement="top">
                  <button type="button" class="plan-action-btn delete" @click.stop="handleDelete(item)" v-hasPermi="['bid:plan:remove']">
                    <i class="el-icon-delete" />
                  </button>
                </el-tooltip>
              </span>
            </div>
            <div class="plan-card-foot">
              <span class="plan-meta">{{ item.category || '普通' }} · {{ parseTime(item.createdTime, '{y}-{m}-{d}') }}</span>
              <el-tag size="mini" :type="Number(item.status) === 2 ? 'success' : 'info'">{{ statusText(item.status) }}</el-tag>
            </div>
          </div>
        </div>
        <div v-else class="empty-side">
          <i class="el-icon-document" />
          <span>暂无方案，您可先新建方案</span>
        </div>
      </div>

      <el-button class="new-button" type="primary" icon="el-icon-plus" @click="handleCreate" v-hasPermi="['bid:plan:add']">新建方案</el-button>
    </aside>

    <main class="home-main">
      <section v-if="selectedPlan" class="project-panel">
        <div class="project-title">
          <div>
            <h1>{{ selectedPlan.title }}</h1>
            <p>{{ selectedPlan.category || '未分类' }} · {{ statusText(selectedPlan.status) }}</p>
          </div>
          <div class="project-actions">
            <el-button type="primary" icon="el-icon-edit-outline" @click="openEditor(selectedPlan)">打开章节</el-button>
            <el-button icon="el-icon-document-add" @click="handleCreate">新建方案</el-button>
            <el-button type="danger" plain icon="el-icon-delete" @click="handleDelete(selectedPlan)" v-hasPermi="['bid:plan:remove']">删除</el-button>
          </div>
        </div>

        <div class="stat-grid">
          <div class="stat-item">
            <span>目录节点</span>
            <b>{{ outlineCount }}</b>
          </div>
          <div class="stat-item">
            <span>创建时间</span>
            <b>{{ parseTime(selectedPlan.createdTime, '{y}-{m}-{d}') || '-' }}</b>
          </div>
          <div class="stat-item">
            <span>写作设置</span>
            <b>{{ selectedPlan.note || '未设置' }}</b>
          </div>
        </div>

        <div class="outline-preview">
          <div class="preview-head">
            <span>方案目录</span>
            <el-button size="mini" icon="el-icon-refresh" @click="loadOutline(selectedPlan.id)">刷新</el-button>
          </div>
          <div v-if="!outlineRows.length" class="outline-empty">
            <i class="el-icon-notebook-2" />
            <p>暂无目录，请进入新建流程上传招标文件并生成目录。</p>
            <el-button type="primary" size="small" @click="handleCreate">生成目录</el-button>
          </div>
          <template v-else>
            <button
              v-for="node in outlineRows"
              :key="node.id"
              class="outline-row"
              :class="'level-' + node.level"
              @click="openEditorWithOutline(node)"
            >
              <span>{{ node.title }}</span>
              <em>{{ levelText(node.level) }}</em>
            </button>
          </template>
        </div>
      </section>

      <section v-else class="home-empty">
        <i class="el-icon-edit-outline" />
        <h2>新建一个 AI方案</h2>
        <p>上传招标文件后，系统会先解析关键信息并生成三级目录，再根据第二阶段提示词生成章节正文。</p>
        <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建方案</el-button>
      </section>
    </main>
  </div>
</template>

<script>
import { deleteBid, listBids, updateBids } from '@/api/bid/bids'
import { getOutlineTree } from '@/api/bid/outlines'
import { getPlanOutlineOverview } from '@/api/bid/planOutline'

export default {
  name: 'BidPlan',
  data() {
    return {
      loading: false,
      planList: [],
      selectedPlan: null,
      outlineTree: [],
      outlineCount: 0,
      initialLoadTimer: null,
      idleLoadHandle: null,
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        title: undefined
      }
    }
  },
  computed: {
    outlineRows() {
      return this.flattenOutlines(this.outlineTree)
    }
  },
  created() {
    this.scheduleInitialLoad()
  },
  beforeDestroy() {
    if (this.initialLoadTimer) {
      clearTimeout(this.initialLoadTimer)
    }
    if (this.idleLoadHandle && window.cancelIdleCallback) {
      window.cancelIdleCallback(this.idleLoadHandle)
    }
  },
  methods: {
    scheduleInitialLoad() {
      const load = () => this.getList({ autoSelect: false })
      if (window.requestIdleCallback) {
        this.idleLoadHandle = window.requestIdleCallback(load, { timeout: 1200 })
      } else {
        this.initialLoadTimer = setTimeout(load, 300)
      }
    },
    getList(options = {}) {
      this.loading = true
      listBids(this.queryParams).then(res => {
        this.planList = res.rows || []
        if (options.autoSelect && !this.selectedPlan && this.planList.length) {
          this.selectPlan(this.planList[0])
        } else if (this.selectedPlan) {
          const fresh = this.planList.find(item => item.id === this.selectedPlan.id)
          this.selectedPlan = fresh || this.planList[0] || null
          if (this.selectedPlan) this.loadOutline(this.selectedPlan.id)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.selectedPlan = null
      this.outlineTree = []
      this.getList({ autoSelect: false })
    },
    selectPlan(row) {
      if (!row || !row.id) return
      this.loading = true
      getPlanOutlineOverview(row.id).then(res => {
        const data = res.data || {}
        const rows = data.rows || this.flattenOutlines(data.tree || [])
        const targetPath = this.hasOutlineWordSetting(rows, data.setting)
          ? '/bid/plan/outline'
          : '/bid/plan/create'
        this.$router.push({ path: targetPath, query: { bidId: row.id } }).catch(() => {})
      }).catch(() => {
        this.$router.push({ path: '/bid/plan/create', query: { bidId: row.id } }).catch(() => {})
      }).finally(() => {
        this.loading = false
      })
    },
    loadOutline(bidId) {
      return getOutlineTree(bidId).then(res => {
        this.outlineTree = res.data || []
        this.outlineCount = this.flattenOutlines(this.outlineTree).length
      })
    },
    handleCreate() {
      this.$router.push('/bid/plan/create')
    },
    openEditor(row) {
      this.$router.push({ path: '/bid/plan/outline', query: { bidId: row.id } })
    },
    openEditorWithOutline(node) {
      this.$router.push({ path: '/bid/plan/outline', query: { bidId: this.selectedPlan.id, outlineId: node.id } })
    },
    renamePlan(row) {
      this.$prompt('请输入新的方案标题', '编辑标题', {
        inputValue: row.title,
        inputPlaceholder: '请输入方案标题',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValidator: value => {
          return value && value.trim() ? true : '方案标题不能为空'
        }
      }).then(({ value }) => {
        const title = value.trim()
        return updateBids({ id: row.id, title }).then(() => {
          this.$modal.msgSuccess('标题已更新')
          if (this.selectedPlan && this.selectedPlan.id === row.id) {
            this.selectedPlan = Object.assign({}, this.selectedPlan, { title })
          }
          this.getList()
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除方案"' + row.title + '"吗？').then(() => deleteBid(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.selectedPlan && this.selectedPlan.id === row.id) {
          this.selectedPlan = null
          this.outlineTree = []
          this.outlineCount = 0
        }
        this.getList()
      }).catch(() => {})
    },
    statusText(status) {
      return Number(status) === 2 ? '已完成' : '草稿'
    },
    levelText(level) {
      return Number(level) === 1 ? '章' : Number(level) === 2 ? '节' : '目'
    },
    hasOutlineWordSetting(rows, setting) {
      const leaves = (rows || []).filter(item => Number(item.level) === 3)
      return !!(setting && setting.id) &&
        leaves.length > 0 &&
        leaves.every(item => Number(item.wordLimit || 0) > 0)
    },
    flattenOutlines(nodes, rows = []) {
      ;(nodes || []).forEach(node => {
        rows.push(node)
        this.flattenOutlines(node.children || [], rows)
      })
      return rows
    }
  }
}
</script>

<style scoped>
.plan-home-page {
  height: calc(100vh - 84px);
  min-height: 640px;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  overflow: hidden;
  background: #f5f7fb;
}
.home-sidebar {
  border-right: 1px solid #dfe4ee;
  background: #fff;
  padding: 18px 14px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}
.side-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: #2f3440;
}
.new-button {
  width: 100%;
  flex: 0 0 auto;
  margin-top: 14px;
  border: 0;
  background: linear-gradient(135deg, #4b7bec, #31c6c7);
}
.plan-scroll {
  flex: 1 1 auto;
  min-height: 0;
  margin-top: 12px;
  overflow-y: auto;
  padding-right: 4px;
}
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.plan-card {
  width: 100%;
  min-height: 82px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  padding: 10px;
  text-align: left;
  cursor: pointer;
  outline: none;
  transition: border-color .2s ease, background .2s ease, box-shadow .2s ease;
}
.plan-card.active,
.plan-card:hover {
  border-color: #4b7bec;
  background: #f2f6ff;
}
.plan-card:focus {
  border-color: #4b7bec;
  box-shadow: 0 0 0 2px rgba(75, 123, 236, .14);
}
.plan-card-head,
.plan-card-foot {
  display: flex;
  align-items: center;
  gap: 8px;
}
.plan-card-foot {
  justify-content: space-between;
}
.plan-name {
  flex: 1 1 auto;
  min-width: 0;
  color: #2762d8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.plan-actions {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  pointer-events: none;
  transform: translateY(-2px);
  transition: opacity .16s ease, transform .16s ease;
}
.plan-card:hover .plan-actions,
.plan-card:focus-within .plan-actions {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}
.plan-action-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 5px;
  color: #fff;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}
.plan-action-btn.edit {
  background: #ff6b6b;
}
.plan-action-btn.delete {
  background: #8e959f;
}
.plan-action-btn:hover {
  filter: brightness(.95);
}
.plan-meta {
  min-width: 0;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty-side,
.home-empty,
.outline-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a8abb2;
}
.empty-side {
  height: 100%;
  min-height: 300px;
  gap: 10px;
}
.empty-side i,
.home-empty i,
.outline-empty i {
  font-size: 40px;
  margin-bottom: 12px;
}
.home-main {
  min-width: 0;
  padding: 18px;
  overflow: auto;
}
.project-panel {
  min-height: calc(100vh - 120px);
  background: #fff;
  border: 1px solid #dfe4ee;
}
.project-title {
  min-height: 112px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 26px;
  border-bottom: 1px solid #edf0f6;
}
.project-title h1 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 24px;
}
.project-title p {
  margin: 0;
  color: #606266;
}
.project-actions {
  display: flex;
  gap: 10px;
}
.stat-grid {
  display: grid;
  grid-template-columns: 160px 180px minmax(0, 1fr);
  gap: 12px;
  padding: 18px 26px;
  border-bottom: 1px solid #edf0f6;
}
.stat-item {
  min-height: 72px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
}
.stat-item span {
  display: block;
  color: #909399;
  font-size: 13px;
}
.stat-item b {
  display: block;
  margin-top: 8px;
  color: #303133;
  font-size: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.outline-preview {
  margin: 18px 26px 26px;
  border: 1px solid #dfe4ee;
}
.preview-head {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px;
  border-bottom: 1px solid #dfe4ee;
  font-weight: 700;
}
.outline-empty {
  min-height: 360px;
  gap: 8px;
}
.outline-row {
  width: 100%;
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 0;
  border-bottom: 1px solid #edf0f6;
  background: #fff;
  cursor: pointer;
}
.outline-row:hover {
  background: #f6f8fc;
}
.outline-row.level-1 {
  padding: 0 14px;
  font-weight: 700;
}
.outline-row.level-2 {
  padding: 0 14px 0 36px;
}
.outline-row.level-3 {
  padding: 0 14px 0 60px;
  color: #606266;
}
.outline-row em {
  font-style: normal;
  color: #a8abb2;
  font-size: 12px;
}
.home-empty {
  min-height: calc(100vh - 120px);
  background: #fff;
  border: 1px solid #dfe4ee;
}
.home-empty h2 {
  margin: 0 0 10px;
  color: #303133;
}
.home-empty p {
  max-width: 560px;
  margin: 0 0 20px;
  line-height: 1.7;
  text-align: center;
}
</style>
