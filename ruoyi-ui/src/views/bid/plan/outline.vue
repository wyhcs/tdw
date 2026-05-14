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
      <header class="project-head">
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
        </div>
        <el-button size="small" icon="el-icon-edit" @click="toggleEdit">{{ editMode ? '退出编辑' : '编辑' }}</el-button>
      </header>

      <div v-if="editMode" class="edit-tabs">
        <button :class="{ active: activeTab === 'word' }" @click="activeTab = 'word'">修改字数</button>
        <button :class="{ active: activeTab === 'writing' }" @click="activeTab = 'writing'">编写方向</button>
        <button :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">新增节点</button>
        <button :class="{ active: activeTab === 'delete' }" @click="activeTab = 'delete'">删除节点</button>
        <button :class="{ active: activeTab === 'sort' }" @click="activeTab = 'sort'">节点排序</button>
      </div>

      <section v-if="!editMode" class="preview-layout">
        <div class="outline-card">
          <div class="preview-head">
            <span>预览目录 {{ rows.length }}</span>
            <small><i class="el-icon-bell" /> 一键差异化目录可改变标题内容，减少查重隐患</small>
          </div>
          <div class="preview-body">
            <div class="outline-tree">
              <template v-if="tree.length">
                <div v-for="chapter in tree" :key="chapter.id" class="preview-node level-1">
                  <i class="el-icon-caret-bottom" />
                  <strong>{{ chapter.title }}</strong>
                  <div v-for="section in chapter.children || []" :key="section.id" class="preview-node level-2">
                    <i class="el-icon-caret-bottom" />
                    <span>{{ section.title }}</span>
                    <div v-for="item in section.children || []" :key="item.id" class="preview-node level-3">
                      <i class="dot" />
                      <span>{{ item.title }}</span>
                      <em>{{ item.wordLimit || 300 }}字</em>
                    </div>
                  </div>
                </div>
              </template>
              <div v-else class="outline-empty">暂无目录，请先生成预览目录。</div>
            </div>
          </div>
          <div class="preview-actions">
            <el-button icon="el-icon-refresh" plain @click="$router.push('/bid/plan/create')">重新生成</el-button>
            <el-button type="primary" @click="wordDialogVisible = true">设置篇幅</el-button>
            <el-button type="primary" plain @click="startContentGenerate">开始生成</el-button>
          </div>
        </div>
        <aside class="helper-panel">
          <div class="helper-card">
            <h3><i class="el-icon-message-solid" /> 消息</h3>
            <p><b>AI标书 4.0 重磅上线！</b></p>
            <p>上线AI质检，可针对招标文件和投标文件进行双重检查。</p>
          </div>
          <div class="helper-card">
            <h3><i class="el-icon-service" /> 客服</h3>
            <p>使用咨询、开具发票，请添加客服微信。</p>
            <div class="qr-row">
              <div />
              <div />
            </div>
          </div>
          <div class="welfare-card">
            <h3>福利</h3>
            <p><b>邀请有礼：</b>每邀请一位好友注册使用，赠送 1 万字数。</p>
            <p><b>邀请返利：</b>购买指定套餐成为合伙人后，最高返利30%。</p>
          </div>
        </aside>
      </section>

      <section v-else class="edit-layout">
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
                <i class="el-icon-caret-bottom" />
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
                @end="sortGroup(chapter.id, chapter.children)"
              >
                <div v-for="section in chapter.children || []" :key="section.id" class="section-block">
                  <div class="node-line level-2">
                    <span v-if="activeTab === 'sort'" class="drag-handle el-icon-rank" />
                    <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[section.id]" @change="checkNode(section, $event)" />
                    <i class="el-icon-caret-bottom" />
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
import WordPresetDialog from './components/WordPresetDialog.vue'

const NodeTitle = {
  props: {
    node: { type: Object, required: true },
    editing: { type: Boolean, default: false }
  },
  data() {
    return {
      value: ''
    }
  },
  watch: {
    node: {
      immediate: true,
      handler(node) {
        this.value = node.titleText || this.cleanTitle(node)
      }
    }
  },
  methods: {
    cleanTitle(node) {
      const title = node.title || ''
      if (Number(node.level) === 1) return title.replace(/^第[一二三四五六七八九十百千万零〇两]+章\s*/, '')
      if (Number(node.level) === 2) return title.replace(/^第[一二三四五六七八九十百千万零〇两]+节\s*/, '')
      return title.replace(/^[（(][一二三四五六七八九十百千万零〇两]+[）)]\s*/, '')
    },
    save() {
      this.$emit('save', this.node, this.value)
    }
  },
  template: `
    <span class="title-editor">
      <template v-if="editing">
        <span class="node-prefix">{{ node.titlePrefix }}</span>
        <el-input v-model="value" size="small" @change="save" @blur="save" />
      </template>
      <template v-else>
        <span>{{ node.title }}</span>
      </template>
    </span>
  `
}

const NodeActions = {
  props: {
    node: { type: Object, required: true },
    tab: { type: String, required: true },
    wordOptions: { type: Array, required: true }
  },
  data() {
    return {
      batchValue: undefined
    }
  },
  methods: {
    add(command) {
      this.$emit('add', this.node, command)
    }
  },
  template: `
    <span class="node-actions">
      <template v-if="tab === 'word'">
        <el-select
          v-if="Number(node.level) === 3"
          v-model="node.wordLimit"
          size="mini"
          class="word-select"
          @change="$emit('single-word', node)"
        >
          <el-option v-for="item in wordOptions" :key="item" :label="item + '字'" :value="item" />
        </el-select>
        <el-select
          v-else
          v-model="batchValue"
          size="mini"
          class="word-select"
          placeholder="批量修改"
          @change="$emit('batch-word', node, batchValue)"
        >
          <el-option v-for="item in wordOptions" :key="item" :label="item + '字'" :value="item" />
        </el-select>
      </template>
      <template v-else-if="tab === 'add'">
        <el-dropdown trigger="click" @command="add">
          <el-button size="mini" circle icon="el-icon-plus" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="sibling">{{ Number(node.level) === 3 ? '新增单段' : '新增同级' }}</el-dropdown-item>
            <el-dropdown-item v-if="Number(node.level) < 3" command="child">新增子级</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
      <template v-else-if="tab === 'delete'">
        <el-button size="mini" circle icon="el-icon-delete" @click="$emit('delete', node)" />
      </template>
      <template v-else-if="tab === 'sort'">
        <el-tooltip content="拖拽排序" placement="top">
          <span class="sort-tip">拖拽排序</span>
        </el-tooltip>
      </template>
    </span>
  `
}

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
      this.loadOverview()
    },
    loadOverview() {
      if (!this.bidId) {
        this.$router.replace('/bid/plan')
        return
      }
      this.loading = true
      return getPlanOutlineOverview(this.bidId).then(res => {
        const data = res.data || {}
        this.bid = data.bid || {}
        this.setting = data.setting || {}
        this.stats = data.stats || {}
        this.tree = data.tree || []
        this.rows = data.rows || []
        this.globalWritingRequirement = this.setting.globalWritingRequirement || ''
      }).finally(() => {
        this.loading = false
      })
    },
    toggleEdit() {
      this.editMode = !this.editMode
      if (!this.editMode) {
        this.checkedMap = {}
        this.loadOverview()
      }
    },
    confirmWordPreset(data) {
      this.wordSubmitting = true
      applyPlanWordPreset(this.bidId, data).then(res => {
        this.refreshData(res.data)
        this.wordDialogVisible = false
        this.editMode = true
        this.activeTab = 'word'
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
      this.tree = data.tree || this.tree
      this.rows = data.rows || this.rows
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
  min-height: calc(100vh - 84px);
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  background: #f5f7fb;
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
  overflow: auto;
  background: #fff;
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
  display: grid;
  grid-template-columns: minmax(520px, 58%) minmax(340px, 42%);
  gap: 32px;
  padding: 22px 28px;
}
.outline-card {
  border-right: 1px solid #dfe4ee;
  padding-right: 18px;
}
.preview-head {
  height: 42px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #45576f;
  font-size: 18px;
}
.preview-head small {
  color: #e6a23c;
  font-size: 14px;
}
.preview-body {
  height: calc(100vh - 260px);
  min-height: 520px;
  overflow: auto;
  border: 1px solid #dfe4ee;
  border-radius: 4px;
}
.outline-tree {
  padding: 12px 16px 80px;
}
.preview-node {
  position: relative;
  min-height: 36px;
  line-height: 36px;
  color: #606266;
}
.preview-node.level-1 {
  font-weight: 700;
  color: #303133;
}
.preview-node.level-2 {
  margin-left: 26px;
  font-weight: 500;
}
.preview-node.level-3 {
  margin-left: 54px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.preview-node em {
  margin-left: auto;
  color: #909399;
  font-style: normal;
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
  min-height: 74px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
.helper-panel {
  display: grid;
  align-content: start;
  gap: 24px;
  padding-top: 40px;
}
.helper-card,
.welfare-card {
  background: #fff;
  border-radius: 8px;
  padding: 22px 26px;
  box-shadow: 0 8px 28px rgba(31, 45, 61, .08);
}
.helper-card h3,
.welfare-card h3 {
  margin: 0 0 14px;
  font-size: 20px;
}
.qr-row {
  display: flex;
  gap: 30px;
}
.qr-row div {
  width: 82px;
  height: 82px;
  background: repeating-linear-gradient(45deg, #606a78, #606a78 8px, #fff 8px, #fff 16px);
  border-radius: 4px;
}
.welfare-card {
  border: 1px solid #ffd6d6;
  background: #fffafa;
}
.edit-layout {
  padding: 0 18px 36px;
}
.bulk-toolbar {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #dfe4ee;
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
  .preview-layout {
    grid-template-columns: 1fr;
  }
  .outline-card {
    border-right: 0;
    padding-right: 0;
  }
}
</style>
