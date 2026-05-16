<template>
  <div class="plan-outline-page">
    <aside class="plan-sidebar">
      <div class="side-title">≡ 我的方案</div>
      <el-input
        v-model="planQuery"
        size="small"
        clearable
        prefix-icon="el-icon-search"
        placeholder="搜索方案"
        @keyup.enter.native="loadPlans"
        @clear="loadPlans"
      />
      <div class="plan-list" v-loading="planLoading">
        <button
          v-for="item in planList"
          :key="item.id"
          class="plan-card"
          :class="{ active: String(item.id) === String(bidId) }"
          @click="switchPlan(item)"
        >
          <span class="plan-name">{{ item.title }}</span>
          <span class="plan-meta">{{ item.category || '普通' }} · {{ parseTime(item.createdTime, '{y}-{m}-{d}') }}</span>
          <el-tag size="mini" :type="Number(item.status) === 2 ? 'success' : 'info'">{{ Number(item.status) === 2 ? '已完成' : '普通' }}</el-tag>
        </button>
      </div>
      <div v-if="!planList.length && !planLoading" class="empty-side">没有更多方案了</div>
      <el-button class="new-button" type="primary" @click="$router.push('/bid/plan/create')">新建方案</el-button>
    </aside>

    <main class="outline-main" v-loading="loading">
      <section v-if="!editMode" class="preview-layout">
        <div class="outline-card">
          <div class="outline-top-fixed">
            <header class="project-head project-head-inline">
              <div>
                <h1>{{ bid.title || 'AI方案' }}</h1>
                <p>
                  指导字数：<b class="danger">{{ stats.targetWords || 0 }}</b> 字
                  <span>生成字数：<b class="success">{{ stats.generatedWords || 0 }}</b> 字</span>
                </p>
                <p>
                  预估页数：<b class="danger">{{ stats.estimatePages || 0 }}</b> 页
                  <span>当前页数：<b class="success">{{ stats.currentPages || 0 }}</b> 页</span>
                </p>
                <p class="head-note">注：数据仅供参考，实际请以具体导出结果为准</p>
              </div>
              <el-button size="small" icon="el-icon-edit" @click="toggleEdit">编辑</el-button>
            </header>
          </div>
          <div class="preview-body">
            <div class="outline-tree">
              <template v-if="tree.length">
                <div v-for="chapter in tree" :key="chapter.id" class="preview-node level-1">
                  <div class="preview-title-row chapter-row" @click="togglePreviewCollapse(chapter.id)">
                    <i :class="isPreviewCollapsed(chapter.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" />
                    <strong class="preview-text ellipsis" :title="chapter.title">{{ chapter.title }}</strong>
                  </div>
                  <div v-show="!isPreviewCollapsed(chapter.id)">
                    <div v-for="section in chapter.children || []" :key="section.id" class="preview-node level-2">
                      <div class="preview-title-row section-row" @click="togglePreviewCollapse(section.id)">
                        <i :class="isPreviewCollapsed(section.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" />
                        <span class="preview-text ellipsis" :title="section.title">{{ section.title }}</span>
                      </div>
                      <div v-show="!isPreviewCollapsed(section.id)">
                        <div v-for="item in section.children || []" :key="item.id" class="preview-node level-3">
                          <i class="dot" />
                          <span class="preview-text ellipsis" :title="item.title">{{ item.title }}</span>
                          <em><b class="success">{{ item.contentWords || 0 }}</b> / {{ item.wordLimit || 300 }}</em>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <div v-else class="outline-empty">暂无目录，请先生成预览目录。</div>
            </div>
          </div>
          <div class="preview-actions">
            <el-button plain @click="$router.push('/bid/plan/create')">重编全文</el-button>
            <el-button type="primary" @click="startContentGenerate">开始生成</el-button>
          </div>
        </div>
        <aside class="preview-hero">
          <div class="hero-stage">
            <div class="hero-top">
              <h2>AI方案</h2>
              <p>根据招标方的采购需求、服务需求、技术要求等，平台将分析重组用户自定义约束条件，智能撰写技术方案、服务方案及其他需求方案。</p>
              <p class="hero-top-extra">覆盖项目背景、技术实现、实施计划、运维保障、质量控制等核心章节，支持分层目录生成与内容深度约束。</p>
            </div>
            <div class="hero-card-grid">
              <div class="hero-card pink">
                <h3>快速编写模式</h3>
                <p>用户提供详细编写需求，例如采购需求、服务要求、技术要求，以此为依据生成方案目录与内容，并可快速进入正文生成。</p>
                <div class="hero-icon"><i class="el-icon-document-checked" /></div>
              </div>
              <div class="hero-card blue">
                <h3>快捷评分模式</h3>
                <p>在详细编写需求基础上，添加统一评分标准，用以约束目录及内容生成方向，确保响应重点与评审逻辑一致。</p>
                <div class="hero-icon"><i class="el-icon-edit-outline" /></div>
              </div>
              <div class="hero-card purple">
                <h3>定制评分模式</h3>
                <p>逐一添加精准的大章名称及编写需求，用以生成更准确的方案目录，适配复杂项目与个性化编写要求。</p>
                <div class="hero-icon"><i class="el-icon-chat-dot-square" /></div>
              </div>
            </div>
            <div class="hero-bottom">
              <el-button type="primary" @click="$router.push('/bid/plan/create')">新建方案</el-button>
            </div>
          </div>
        </aside>
      </section>

      <section v-else class="edit-shell">
        <div class="edit-panel">
          <header class="project-head project-head-edit">
            <div>
              <h1>{{ bid.title || 'AI方案' }}</h1>
            </div>
            <el-button size="small" icon="el-icon-edit" @click="toggleEdit">退出编辑</el-button>
          </header>

          <div class="edit-tabs">
            <button :class="{ active: activeTab === 'word' }" @click="activeTab = 'word'">修改字数</button>
            <button :class="{ active: activeTab === 'writing' }" @click="activeTab = 'writing'">编写方向</button>
            <button :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">新增节点</button>
            <button :class="{ active: activeTab === 'delete' }" @click="activeTab = 'delete'">删除节点</button>
            <button :class="{ active: activeTab === 'sort' }" @click="activeTab = 'sort'">节点排序</button>
          </div>

          <div class="edit-summary">
            <p>
              目标字数：<b class="danger">{{ stats.targetWords || 0 }}</b> 字
              <span>生成字数：<b class="success">{{ stats.generatedWords || 0 }}</b> 字</span>
            </p>
            <p>
              预估页数：<b class="danger">{{ stats.estimatePages || 0 }}</b> 页
              <span>当前页数：<b class="success">{{ stats.currentPages || 0 }}</b> 页</span>
            </p>
            <p class="head-note">注：已经生成内容的段落无法修改字数</p>
          </div>

          <div class="edit-layout">
            <div v-if="activeTab === 'delete'" class="bulk-toolbar">
              <el-button type="danger" plain @click="deleteChecked">删除选中项</el-button>
            </div>

            <div v-if="activeTab === 'writing'" class="global-rule">
          <div class="rule-title">
            <i class="el-icon-edit-outline" />
            <strong>方案整体编写要求</strong>
            <el-button size="mini" type="primary" plain @click="saveGlobalRequirement">保存</el-button>
          </div>
          <el-input
            v-model="globalWritingRequirement"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            maxlength="10000"
            show-word-limit
            placeholder="输入方案整体编写要求"
          />
        </div>

        <div class="edit-tree">
          <draggable
            v-model="tree"
            :disabled="activeTab !== 'sort'"
            handle=".drag-handle"
            @end="sortGroup(null, tree)"
          >
            <div v-for="chapter in tree" :key="chapter.id" class="node-block">
              <div class="node-line level-1">
                <span v-if="activeTab === 'sort'" class="drag-handle el-icon-rank" />
                <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[chapter.id]" @change="checkNode(chapter, $event)" />
                <i :class="isCollapsed(chapter.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="toggleCollapse(chapter.id)" />
                <node-title :node="chapter" :editing="activeTab === 'writing'" @save="saveTitle" />
                <node-actions
                  :node="chapter"
                  :tab="activeTab"
                  :word-options="wordOptions"
                  @batch-word="batchWord"
                  @add="openAddDialog"
                  @delete="deleteOne"
                />
              </div>

              <draggable
                v-model="chapter.children"
                :disabled="activeTab !== 'sort'"
                handle=".drag-handle"
                v-show="!isCollapsed(chapter.id)"
                @end="sortGroup(chapter.id, chapter.children)"
              >
                <div v-for="section in chapter.children || []" :key="section.id" class="section-block">
                  <div class="node-line level-2">
                    <span v-if="activeTab === 'sort'" class="drag-handle el-icon-rank" />
                    <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[section.id]" @change="checkNode(section, $event)" />
                    <i :class="isCollapsed(section.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="toggleCollapse(section.id)" />
                    <node-title :node="section" :editing="activeTab === 'writing'" @save="saveTitle" />
                    <node-actions
                      :node="section"
                      :tab="activeTab"
                      :word-options="wordOptions"
                      @batch-word="batchWord"
                      @add="openAddDialog"
                      @delete="deleteOne"
                    />
                  </div>

                  <draggable
                    v-model="section.children"
                    :disabled="activeTab !== 'sort'"
                    handle=".drag-handle"
                    v-show="!isCollapsed(section.id)"
                    @end="sortGroup(section.id, section.children)"
                  >
                    <div v-for="item in section.children || []" :key="item.id" class="leaf-block">
                      <div class="node-line level-3">
                        <span v-if="activeTab === 'sort'" class="drag-handle el-icon-rank" />
                        <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[item.id]" @change="checkNode(item, $event)" />
                        <i class="dot" />
                        <node-title :node="item" :editing="activeTab === 'writing'" @save="saveTitle" />
                        <node-actions
                          :node="item"
                          :tab="activeTab"
                          :word-options="wordOptions"
                          @single-word="saveSingleWord"
                          @add="openAddDialog"
                          @delete="deleteOne"
                        />
                      </div>
                      <div v-if="activeTab === 'writing'" class="writing-box">
                        <div class="writing-row">
                          <label>编写方向：</label>
                          <el-button size="mini" type="primary" icon="el-icon-magic-stick" @click="openAi('direction', item)">AI帮写</el-button>
                          <el-input
                            v-model="item.writingDirection"
                            type="textarea"
                            :autosize="{ minRows: 3, maxRows: 8 }"
                            maxlength="10000"
                            show-word-limit
                            placeholder="请输入编写方向"
                          />
                          <el-button size="mini" @click="saveDirection(item)">保存</el-button>
                        </div>
                        <div class="writing-row">
                          <label>编写要求：</label>
                          <el-button size="mini" type="primary" icon="el-icon-magic-stick" @click="openAi('requirement', item)">AI帮写</el-button>
                          <el-input
                            v-model="item.writingRequirement"
                            type="textarea"
                            :autosize="{ minRows: 2, maxRows: 6 }"
                            maxlength="10000"
                            show-word-limit
                            placeholder="请输入编写要求"
                          />
                          <el-button size="mini" @click="saveRequirement(item)">保存</el-button>
                        </div>
                      </div>
                    </div>
                  </draggable>
                </div>
              </draggable>
            </div>
          </draggable>
            </div>
          </div>
        </div>
        <aside class="preview-hero edit-hero">
          <div class="hero-stage">
            <div class="hero-top">
              <h2>AI方案</h2>
              <p>根据招标方的采购需求、服务需求、技术要求等，配合评分标准或用户自定义约束条件，智能撰写技术方案、服务方案及其他需求方案。提供多种撰写模式，满足用户不同撰写场景。</p>
            </div>
            <div class="hero-card-grid">
              <div class="hero-card pink">
                <h3>快速编写模式</h3>
                <p>用户提供详细编写需求，例如采购需求、服务需求、技术要求等，以此为依据直接生成方案目录与内容。</p>
                <div class="hero-icon"><i class="el-icon-document-checked" /></div>
              </div>
              <div class="hero-card blue">
                <h3>快捷评分模式</h3>
                <p>用户在提供详实编写需求的基础上，添加统一的方案评分标准，用以约束目录及内容生成方向。</p>
                <div class="hero-icon"><i class="el-icon-edit-outline" /></div>
              </div>
              <div class="hero-card purple">
                <h3>定制评分模式</h3>
                <p>用户逐一添加明确的大章名称及编写需求，用以生成更准确的方案目录。</p>
                <div class="hero-icon"><i class="el-icon-chat-dot-square" /></div>
              </div>
            </div>
            <div class="hero-bottom">
              <el-button type="primary" @click="$router.push('/bid/plan/create')">新建方案</el-button>
            </div>
          </div>
        </aside>
      </section>
    </main>

    <word-preset-dialog
      :visible.sync="wordDialogVisible"
      :leaf-count="Number(stats.leafCount || 0)"
      :submitting="wordSubmitting"
      @confirm="confirmWordPreset"
    />
    <add-node-dialog
      :visible.sync="addDialogVisible"
      :node="activeNode"
      :mode="addMode"
      :submitting="addSubmitting"
      @submit="submitAddNode"
    />
    <ai-writing-dialog
      :visible.sync="aiDialogVisible"
      :type="aiType"
      :bid-id="bidId"
      :node="activeNode"
      @use="useAiContent"
    />
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import { listBids } from '@/api/bid/bids'
import {
  addOutlineChild,
  addOutlineParagraph,
  addOutlineSibling,
  applyPlanWordPreset,
  batchUpdateNodeWordLimit,
  deleteOutlineNodes,
  finalizePlanOutline,
  getPlanOutlineEdit,
  getPlanOutlineOverview,
  saveWritingDirection,
  saveGlobalWritingRequirement,
  saveWritingRequirement,
  sortOutlineNodes,
  updateNodeWordLimit,
  updatePlanOutlineTitle
} from '@/api/bid/planOutline'
import AddNodeDialog from './components/AddNodeDialog.vue'
import AiWritingDialog from './components/AiWritingDialog.vue'
import NodeActions from './components/EditNodeActions.vue'
import NodeTitle from './components/EditNodeTitle.vue'
import WordPresetDialog from './components/WordPresetDialog.vue'

export default {
  name: 'BidPlanOutline',
  components: {
    draggable,
    WordPresetDialog,
    AddNodeDialog,
    AiWritingDialog,
    NodeTitle,
    NodeActions
  },
  data() {
    return {
      bidId: undefined,
      bid: {},
      setting: {},
      stats: {},
      tree: [],
      rows: [],
      planList: [],
      planQuery: '',
      loading: false,
      planLoading: false,
      editMode: false,
      activeTab: 'word',
      checkedMap: {},
      collapseMap: {},
      previewCollapseMap: {},
      wordDialogVisible: false,
      wordSubmitting: false,
      addDialogVisible: false,
      addSubmitting: false,
      activeNode: null,
      addMode: 'sibling',
      aiDialogVisible: false,
      aiType: 'direction',
      globalWritingRequirement: '',
      wordOptions: [300, 600, 900, 1200, 1800, 2700, 3600, 4500, 5400, 6300, 7200, 8100, 9000, 9900]
    }
  },
  created() {
    this.bidId = this.$route.query.bidId
    this.loadPlans()
    this.loadOverview()
  },
  methods: {
    loadPlans() {
      this.planLoading = true
      listBids({ pageNum: 1, pageSize: 30, title: this.planQuery || undefined }).then(res => {
        this.planList = res.rows || []
      }).finally(() => {
        this.planLoading = false
      })
    },
    switchPlan(row) {
      this.$router.replace({ path: '/bid/plan/outline', query: { bidId: row.id } })
      this.bidId = row.id
      this.editMode = false
      this.activeTab = 'word'
      this.collapseMap = {}
      this.previewCollapseMap = {}
      this.loadOverview()
    },
    loadOverview() {
      if (!this.bidId) {
        this.$router.replace('/bid/plan')
        return
      }
      this.loading = true
      return getPlanOutlineOverview(this.bidId).then(res => {
        this.refreshData(res.data || {})
      }).finally(() => {
        this.loading = false
      })
    },
    loadEdit() {
      if (!this.bidId) return Promise.resolve()
      this.loading = true
      return getPlanOutlineEdit(this.bidId).then(res => {
        this.refreshData(res.data || {})
      }).finally(() => {
        this.loading = false
      })
    },
    toggleEdit() {
      if (this.editMode) {
        this.editMode = false
        this.checkedMap = {}
        this.collapseMap = {}
        this.loadOverview()
        return
      }
      this.loadEdit().then(() => {
        this.editMode = true
        this.activeTab = 'word'
      }).catch(() => {
        this.editMode = true
        this.activeTab = 'word'
      })
    },
    confirmWordPreset(data) {
      this.wordSubmitting = true
      applyPlanWordPreset(this.bidId, data).then(res => {
        this.refreshData(res.data)
        this.wordDialogVisible = false
        this.editMode = false
        this.$modal.msgSuccess('篇幅设置完成')
      }).finally(() => {
        this.wordSubmitting = false
      })
    },
    refreshData(data) {
      data = data || {}
      this.bid = data.bid || this.bid
      this.setting = data.setting || this.setting
      this.stats = data.stats || this.stats
      this.tree = this.normalizeTree(data.tree || this.tree || [])
      this.rows = data.rows || this.rows
      this.globalWritingRequirement = this.setting.globalWritingRequirement || ''
    },
    normalizeTree(nodes) {
      if (!Array.isArray(nodes)) return []
      return nodes.map(item => {
        const node = { ...item }
        const prefix = node.titlePrefix ? String(node.titlePrefix).trim() : ''
        const text = node.titleText ? String(node.titleText).trim() : ''
        if (!node.title || !String(node.title).trim().length) {
          node.title = [prefix, text].filter(Boolean).join(' ').trim()
        }
        node.wordLimit = Number(node.wordLimit) > 0 ? Number(node.wordLimit) : 300
        node.contentWords = Number(node.contentWords || 0)
        node.children = this.normalizeTree(node.children || [])
        return node
      })
    },
    isCollapsed(nodeId) {
      return !!this.collapseMap[nodeId]
    },
    toggleCollapse(nodeId) {
      this.$set(this.collapseMap, nodeId, !this.collapseMap[nodeId])
    },
    isPreviewCollapsed(nodeId) {
      return !!this.previewCollapseMap[nodeId]
    },
    togglePreviewCollapse(nodeId) {
      this.$set(this.previewCollapseMap, nodeId, !this.previewCollapseMap[nodeId])
    },
    saveSingleWord(node) {
      updateNodeWordLimit(this.bidId, node.id, { wordLimit: node.wordLimit }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('字数已保存')
      })
    },
    batchWord(node, wordLimit) {
      if (!wordLimit) return
      batchUpdateNodeWordLimit(this.bidId, node.id, { wordLimit }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('批量字数已保存')
      })
    },
    saveTitle(node, title) {
      if (!title || title.trim() === (node.titleText || '').trim()) return
      updatePlanOutlineTitle(this.bidId, node.id, { title }).then(res => {
        this.refreshData(res.data)
      })
    },
    saveDirection(node) {
      saveWritingDirection(this.bidId, node.id, {
        content: node.writingDirection || '',
        mode: node.directionMode || ''
      }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('编写方向已保存')
      })
    },
    saveRequirement(node) {
      saveWritingRequirement(this.bidId, node.id, {
        content: node.writingRequirement || '',
        mode: node.requirementMode || ''
      }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('编写要求已保存')
      })
    },
    saveGlobalRequirement() {
      saveGlobalWritingRequirement(this.bidId, {
        content: this.globalWritingRequirement || ''
      }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('整体编写要求已保存')
      })
    },
    openAi(type, node) {
      this.aiType = type
      this.activeNode = node
      this.aiDialogVisible = true
    },
    useAiContent(payload) {
      if (!this.activeNode) return
      if (this.aiType === 'direction') {
        this.$set(this.activeNode, 'writingDirection', payload.content)
        this.$set(this.activeNode, 'directionMode', payload.mode)
      } else {
        this.$set(this.activeNode, 'writingRequirement', payload.content)
        this.$set(this.activeNode, 'requirementMode', payload.mode)
      }
    },
    openAddDialog(node, mode) {
      this.activeNode = node
      this.addMode = Number(node.level) === 3 ? 'paragraph' : mode
      this.addDialogVisible = true
    },
    submitAddNode(data) {
      if (!this.activeNode) return
      this.addSubmitting = true
      const action = this.addMode === 'child'
        ? addOutlineChild
        : this.addMode === 'paragraph'
          ? addOutlineParagraph
          : addOutlineSibling
      action(this.bidId, this.activeNode.id, data).then(res => {
        this.refreshData(res.data)
        this.addDialogVisible = false
        this.$modal.msgSuccess('节点已新增')
      }).finally(() => {
        this.addSubmitting = false
      })
    },
    checkNode(node, checked) {
      this.$set(this.checkedMap, node.id, checked)
      ;(node.children || []).forEach(child => this.checkNode(child, checked))
    },
    checkedIds() {
      return Object.keys(this.checkedMap).filter(id => this.checkedMap[id]).map(id => Number(id))
    },
    deleteChecked() {
      const ids = this.checkedIds()
      if (!ids.length) {
        this.$modal.msgWarning('请选择要删除的节点')
        return
      }
      this.$modal.confirm('确认删除选中的目录节点吗？').then(() => deleteOutlineNodes(this.bidId, { ids })).then(res => {
        this.checkedMap = {}
        this.refreshData(res.data)
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    deleteOne(node) {
      this.$modal.confirm('确认删除“' + node.title + '”吗？').then(() => deleteOutlineNodes(this.bidId, { ids: [node.id] })).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    sortGroup(parentId, nodes) {
      if (this.activeTab !== 'sort' || !nodes || !nodes.length) return
      sortOutlineNodes(this.bidId, {
        parentId,
        orderedIds: nodes.map(item => item.id)
      }).then(res => {
        this.refreshData(res.data)
      })
    },
    startContentGenerate() {
      finalizePlanOutline(this.bidId).then(() => {
        this.$router.push({ path: '/bid/plan/editor', query: { bidId: this.bidId } })
      })
    }
  }
}
</script>

<style scoped>
.plan-outline-page {
  height: calc(100vh - 84px);
  max-height: calc(100vh - 84px);
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  background: #f5f7fb;
  overflow: hidden;
}
.plan-sidebar {
  position: relative;
  border-right: 1px solid #dfe4ee;
  background: #fff;
  padding: 18px 14px 92px;
  overflow: auto;
}
.side-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: #2f3440;
}
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}
.plan-card {
  width: 100%;
  min-height: 76px;
  text-align: left;
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  padding: 10px;
  cursor: pointer;
}
.plan-card.active,
.plan-card:hover {
  border-color: #4b7bec;
  background: #f2f6ff;
}
.plan-name {
  display: block;
  color: #2762d8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.plan-meta {
  display: block;
  margin: 8px 0;
  color: #909399;
  font-size: 12px;
}
.new-button {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 22px;
  width: calc(100% - 36px);
  border: 0;
  background: linear-gradient(135deg, #4b7bec, #31c6c7);
}
.empty-side {
  margin-top: 22px;
  color: #909399;
  text-align: center;
}
.outline-main {
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.project-head {
  min-height: 92px;
  border-bottom: 1px solid #dfe4ee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 22px;
}
.project-head h1 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #1f2d3d;
}
.project-head p {
  margin: 4px 0;
  color: #606266;
}
.project-head .head-note {
  margin-top: 6px;
  color: #9aa3b2;
  font-size: 12px;
}
.project-head-inline {
  min-height: auto;
  border-bottom: 0;
  padding: 14px 0 12px;
  align-items: flex-start;
}
.project-head-inline .el-button {
  margin-top: 2px;
}
.project-head-edit {
  min-height: 50px;
  padding: 12px 18px;
  align-items: center;
}
.project-head-edit h1 {
  margin: 0;
  max-width: 430px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 16px;
  line-height: 1.5;
}
.project-head span {
  margin-left: 48px;
}
.danger {
  color: #f56c6c;
}
.success {
  color: #2fbd55;
}
.edit-tabs {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  height: 58px;
  border-bottom: 1px solid #dfe4ee;
}
.edit-tabs button {
  border: 0;
  background: #fff;
  font-size: 16px;
  color: #606266;
  cursor: pointer;
}
.edit-tabs button.active {
  color: #2563ff;
  border-bottom: 3px solid #2563ff;
}
.preview-layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(560px, 44%) minmax(620px, 56%);
  gap: 0;
  padding: 0;
  height: 100%;
}
.edit-shell {
  flex: 1;
  min-height: 0;
  height: 100%;
  display: grid;
  grid-template-columns: minmax(560px, 600px) minmax(0, 1fr);
  background: #fff;
}
.edit-panel {
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #dfe4ee;
  background: #fff;
  overflow: hidden;
}
.edit-summary {
  padding: 10px 18px 12px;
  border-bottom: 1px solid #dfe4ee;
  color: #606266;
  font-size: 13px;
}
.edit-summary p {
  display: flex;
  align-items: center;
  margin: 6px 0;
}
.edit-summary span {
  margin-left: auto;
  min-width: 190px;
}
.edit-summary .head-note {
  display: block;
  margin-top: 8px;
  color: #9aa3b2;
  font-size: 12px;
}
.outline-card {
  min-width: 0;
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  border-right: 1px solid #dfe4ee;
  padding: 0 10px 0 16px;
  overflow: hidden;
  background: #fff;
}
.outline-top-fixed {
  background: #fff;
  border-bottom: 1px solid #e7ebf3;
}
.preview-body {
  min-height: 0;
  overflow: auto;
  border-bottom: 1px solid #e7ebf3;
  padding-right: 2px;
}
.outline-tree {
  padding: 10px 10px 14px 6px;
}
.preview-node {
  min-height: 34px;
  line-height: 34px;
  color: #606266;
}
.preview-node.level-1 {
  font-weight: 700;
  color: #303133;
}
.preview-node.level-2 {
  margin-left: 24px;
  font-weight: 500;
}
.preview-node.level-3 {
  margin-left: 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.preview-node em {
  margin-left: auto;
  color: #909399;
  font-style: normal;
  min-width: 74px;
  text-align: right;
}
.preview-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  cursor: pointer;
}
.preview-title-row .preview-text {
  flex: 1;
}
.preview-node.level-3 .preview-text {
  flex: 1;
}
.preview-title-row i {
  flex: 0 0 auto;
  color: #606a78;
}
.preview-text {
  min-width: 0;
}
.preview-text.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  max-width: 100%;
}
.dot {
  width: 6px;
  height: 6px;
  display: inline-block;
  border-radius: 50%;
  background: #f56c6c;
}
.outline-empty {
  padding: 80px 0;
  text-align: center;
  color: #a8abb2;
}
.preview-actions {
  min-height: 104px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 10px 0;
  background: #fff;
  border-top: 1px solid #e7ebf3;
}
.preview-actions .el-button {
  width: 100%;
  max-width: 460px;
}
.preview-hero {
  min-width: 0;
  height: 100%;
  min-height: 0;
  background: linear-gradient(135deg, #f3f4fb 0%, #eef2ff 100%);
  padding: 18px 26px 20px;
  display: flex;
  align-items: stretch;
  overflow: hidden;
}
.hero-stage {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  row-gap: 18px;
}
.hero-top {
  text-align: center;
}
.hero-top h2 {
  margin: 0 0 10px;
  font-size: 28px;
  color: #1f2d3d;
}
.hero-top p {
  margin: 0 auto;
  max-width: 980px;
  font-size: 16px;
  line-height: 1.75;
  color: #45576f;
}
.hero-top-extra {
  margin-top: 8px !important;
  font-size: 15px !important;
  color: #56627b !important;
}
.hero-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  align-items: stretch;
  align-content: stretch;
  min-height: 0;
  height: 100%;
}
.hero-card {
  min-height: 0;
  height: 100%;
  min-height: 260px;
  border: 1px solid #e5e9f2;
  border-radius: 10px;
  padding: 18px 16px 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}
.hero-card h3 {
  margin: 0 0 10px;
  font-size: 19px;
  color: #1f2d3d;
  line-height: 1.4;
}
.hero-card p {
  margin: 0;
  font-size: 15px;
  line-height: 1.78;
  color: #3d4a63;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 8;
}
.hero-card.pink {
  background: #fdf4f8;
}
.hero-card.blue {
  background: #f1f5fd;
}
.hero-card.purple {
  background: #f6f3fe;
}
.hero-icon {
  width: 78px;
  height: 78px;
  margin: auto auto 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-icon i {
  font-size: 36px;
  color: #6a78ee;
}
.hero-bottom {
  text-align: center;
  margin-top: 0;
  padding-top: 2px;
}
.hero-bottom .el-button {
  min-width: 146px;
  height: 40px;
  font-size: 14px;
}
.edit-layout {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0 12px 28px;
  overflow: hidden;
}
.bulk-toolbar {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #dfe4ee;
}
.word-note {
  margin: 10px 10px 0;
  color: #2f66ff;
  font-size: 13px;
  font-weight: 600;
}
.global-rule {
  margin: 22px 10px;
  background: #f6f6f7;
  border-radius: 8px;
  padding: 18px;
}
.rule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #303133;
}
.rule-title .el-button {
  margin-left: auto;
}
.edit-tree {
  padding: 10px 0 60px;
  flex: 1;
  min-height: 0;
  overflow: auto;
}
.node-block,
.section-block,
.leaf-block {
  min-width: 0;
}
.node-line {
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px dashed #d9dde6;
  color: #606266;
}
.node-line .el-icon-caret-bottom,
.node-line .el-icon-caret-right {
  cursor: pointer;
  color: #5c6678;
}
.node-line.level-1 {
  margin-top: 8px;
  background: #f7f8fb;
  color: #303133;
  font-weight: 700;
}
.node-line.level-2 {
  margin-left: 24px;
}
.node-line.level-3 {
  margin-left: 52px;
}
.drag-handle {
  cursor: move;
  color: #9aa3b2;
}
.title-editor {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.title-editor > span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-prefix {
  flex: 0 0 auto;
  color: #606266;
}
.node-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.word-select {
  width: 118px;
}
.word-metric {
  min-width: 110px;
  text-align: right;
  color: #909399;
  font-size: 14px;
}
.sort-tip {
  color: #909399;
  font-size: 12px;
}
.writing-box {
  margin-left: 82px;
  margin-bottom: 14px;
  background: #f7f8fb;
  border-radius: 8px;
  padding: 14px 16px;
}
.writing-row {
  position: relative;
  margin-bottom: 14px;
}
.writing-row label {
  display: block;
  margin-bottom: 8px;
  color: #303133;
}
.writing-row .el-button {
  position: absolute;
  right: 0;
  top: 0;
}
.writing-row > .el-button:last-child {
  position: static;
  margin-top: 8px;
}
@media (max-width: 1280px) {
  .edit-shell {
    grid-template-columns: minmax(0, 1fr);
  }
  .edit-hero {
    display: none;
  }
  .preview-layout {
    grid-template-columns: 1fr;
    height: auto;
  }
  .outline-card {
    border-right: 0;
    padding-right: 10px;
  }
  .preview-hero {
    min-height: auto;
    padding: 20px 18px 16px;
  }
  .hero-top h2 {
    font-size: 22px;
  }
  .hero-top p {
    font-size: 14px;
  }
  .hero-card-grid {
    grid-template-columns: 1fr;
  }
  .hero-card {
    height: auto;
    min-height: 160px;
  }
  .hero-card h3 {
    font-size: 16px;
  }
  .hero-card p {
    font-size: 14px;
  }
}
</style>
