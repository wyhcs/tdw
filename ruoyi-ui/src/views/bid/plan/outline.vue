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
                  <span>生成字数：<b class="success">{{ generatedWordsCount }}</b> 字</span>
                </p>
                <p>
                  预估页数：<b class="danger">{{ stats.estimatePages || 0 }}</b> 页
                  <span>当前页数：<b class="success">{{ currentPageCount }}</b> 页</span>
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
                  <div class="preview-title-row chapter-row">
                    <i :class="isPreviewCollapsed(chapter.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="togglePreviewCollapse(chapter.id)" />
                    <strong
                      class="preview-text ellipsis"
                      :class="{ clickable: true, active: isSelectedContentNode(chapter) }"
                      :title="chapter.title"
                      @click.stop="selectGeneratedNode(chapter)"
                    >{{ chapter.title }}</strong>
                    <em><b class="success">{{ nodeGeneratedWords(chapter) }}</b> / {{ nodeTargetWords(chapter) }}</em>
                  </div>
                  <div v-show="!isPreviewCollapsed(chapter.id)">
                    <div v-for="section in chapter.children || []" :key="section.id" class="preview-node level-2">
                      <div class="preview-title-row section-row">
                        <i :class="isPreviewCollapsed(section.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="togglePreviewCollapse(section.id)" />
                        <span
                          class="preview-text ellipsis"
                          :class="{ clickable: true, active: isSelectedContentNode(section) }"
                          :title="section.title"
                          @click.stop="selectGeneratedNode(section)"
                        >{{ section.title }}</span>
                        <em><b class="success">{{ nodeGeneratedWords(section) }}</b> / {{ nodeTargetWords(section) }}</em>
                      </div>
                      <div v-show="!isPreviewCollapsed(section.id)">
                        <div v-for="item in section.children || []" :key="item.id" class="preview-node level-3">
                          <i class="dot" />
                          <span
                            class="preview-text ellipsis"
                            :class="{ clickable: true, active: isSelectedContentNode(item) }"
                            :title="item.title"
                            @click.stop="selectGeneratedNode(item)"
                          >{{ item.title }}</span>
                          <em><b class="success">{{ nodeGeneratedWords(item) }}</b> / {{ nodeTargetWords(item) }}</em>
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
            <el-button v-if="generatingContent" class="generating-button" type="primary" :loading="true" disabled>生成中...</el-button>
            <template v-else>
              <el-button plain @click="$router.push('/bid/plan/create')">重编全文</el-button>
              <el-button type="primary" @click="startContentGenerate">开始生成</el-button>
            </template>
          </div>
        </div>
        <aside v-if="!selectedContentNode" class="preview-hero">
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
        <aside v-else class="content-pane" v-loading="contentLoading">
          <div class="content-title-stack">
            <div v-if="visibleChapterTitle" class="content-title-row chapter-title-row">
              <strong>{{ visibleChapterTitle.title }}</strong>
              <span class="title-count"><b class="success">{{ nodeGeneratedWords(visibleChapterTitle) }}</b> / {{ nodeTargetWords(visibleChapterTitle) }} 字</span>
              <el-button size="mini" type="primary" plain :loading="rewritingNodeId === visibleChapterTitle.id" @click="regenerateContentNode(visibleChapterTitle)">重编本章</el-button>
              <el-button size="mini" type="primary" plain :loading="exportingNodeId === visibleChapterTitle.id" @click="exportContentNode(visibleChapterTitle)">导出本章</el-button>
              <el-button class="close-pane-button" size="mini" icon="el-icon-close" @click="closeContentPane">关闭</el-button>
            </div>
            <div v-if="visibleSectionTitle" class="content-title-row section-title-row">
              <strong>{{ visibleSectionTitle.title }}</strong>
              <span class="title-count"><b class="success">{{ nodeGeneratedWords(visibleSectionTitle) }}</b> / {{ nodeTargetWords(visibleSectionTitle) }} 字</span>
              <el-button size="mini" type="primary" plain :loading="rewritingNodeId === visibleSectionTitle.id" @click="regenerateContentNode(visibleSectionTitle)">重编本级</el-button>
              <el-button size="mini" type="primary" plain :loading="exportingNodeId === visibleSectionTitle.id" @click="exportContentNode(visibleSectionTitle)">导出本节</el-button>
            </div>
            <div v-if="visibleParagraphTitle" class="content-title-row paragraph-title-row">
              <strong>{{ visibleParagraphTitle.title }}</strong>
              <span class="title-count"><b class="success">{{ nodeGeneratedWords(visibleParagraphTitle) }}</b> / {{ nodeTargetWords(visibleParagraphTitle) }} 字</span>
              <el-button size="mini" type="primary" plain :loading="rewritingNodeId === visibleParagraphTitle.id" @click="regenerateContentNode(visibleParagraphTitle)">重编本段</el-button>
              <div class="editor-toolbox">
                <el-tooltip content="插入文本" placement="bottom">
                  <el-button size="mini" icon="el-icon-edit-outline" @click="runEditorTool('text')" />
                </el-tooltip>
                <el-tooltip content="插入图片" placement="bottom">
                  <el-button size="mini" icon="el-icon-picture-outline" @click="runEditorTool('image')" />
                </el-tooltip>
                <el-tooltip content="插入表格" placement="bottom">
                  <el-button size="mini" icon="el-icon-s-grid" @click="runEditorTool('table')" />
                </el-tooltip>
                <el-tooltip content="清除图表" placement="bottom">
                  <el-button size="mini" icon="el-icon-delete" @click="runEditorTool('clear')" />
                </el-tooltip>
                <el-tooltip content="查看知识库引用" placement="bottom">
                  <el-button size="mini" icon="el-icon-collection" @click="runEditorTool('refs')" />
                </el-tooltip>
                <el-tooltip content="切换全屏" placement="bottom">
                  <el-button size="mini" icon="el-icon-full-screen" @click="runEditorTool('fullscreen')" />
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="content-editor-shell rich-editor-shell">
            <rich-content-editor
              ref="contentRichEditor"
              :bid-id="bidId"
              :selected-outline="selectedContentNode"
              :blocks="selectedContentBlocks"
              :saving-external="contentSaving"
              :show-header="false"
              @save-rich="saveSelectedRichContent"
            />
          </div>
          <p v-if="Number(selectedContentNode.level) !== 3" class="content-note">章级和节级标题默认展示下级段落汇总，编辑后会保存为当前标题的富文本内容。</p>
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

          <div v-if="activeTab === 'word'" class="edit-summary">
            <p>
              目标字数：<b class="danger">{{ stats.targetWords || 0 }}</b> 字
              <span>生成字数：<b class="success">{{ generatedWordsCount }}</b> 字</span>
            </p>
            <p>
              预估页数：<b class="danger">{{ stats.estimatePages || 0 }}</b> 页
              <span>当前页数：<b class="success">{{ currentPageCount }}</b> 页</span>
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
                <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[chapter.id]" @change="checkNode(chapter, $event)" />
                <i :class="isCollapsed(chapter.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="toggleCollapse(chapter.id)" />
                <node-title :node="chapter" :editing="activeTab === 'writing'" @save="saveWritingNode" />
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
                    <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[section.id]" @change="checkNode(section, $event)" />
                    <i :class="isCollapsed(section.id) ? 'el-icon-caret-right' : 'el-icon-caret-bottom'" @click.stop="toggleCollapse(section.id)" />
                    <node-title :node="section" :editing="activeTab === 'writing'" @save="saveWritingNode" />
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
                        <el-checkbox v-if="activeTab === 'delete'" :value="!!checkedMap[item.id]" @change="checkNode(item, $event)" />
                        <i class="dot" />
                        <node-title
                          :node="item"
                          :editing="activeTab === 'writing'"
                          :external-dirty="isWritingDirty(item)"
                          @save="saveWritingNode"
                        />
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
    <el-dialog
      title="方案设置"
      :visible.sync="generateDialogVisible"
      width="560px"
      append-to-body
      custom-class="generate-setting-dialog"
      :close-on-click-modal="false"
    >
      <div class="generate-setting-form">
        <div class="setting-row">
          <label>图表数量：</label>
          <el-radio-group v-model="generateSettings.chartCount" size="small">
            <el-radio-button label="normal">一般</el-radio-button>
            <el-radio-button label="less">较少</el-radio-button>
            <el-radio-button label="more">较多</el-radio-button>
            <el-radio-button label="none">无</el-radio-button>
          </el-radio-group>
        </div>
        <div class="setting-row">
          <label>表格数量：</label>
          <el-radio-group v-model="generateSettings.tableCount" size="small">
            <el-radio-button label="normal">一般</el-radio-button>
            <el-radio-button label="less">较少</el-radio-button>
            <el-radio-button label="more">较多</el-radio-button>
            <el-radio-button label="none">无</el-radio-button>
          </el-radio-group>
        </div>
        <div class="setting-row">
          <label>知识库：</label>
          <div class="setting-actions">
            <el-button size="mini" :class="{ active: generateSettings.knowledgeMode === 'upload' }" @click="setGenerateKnowledgeMode('upload')">上传</el-button>
            <el-button size="mini" :class="{ active: generateSettings.knowledgeMode === 'library' }" @click="setGenerateKnowledgeMode('library')">从知识库选择</el-button>
          </div>
        </div>
        <div class="setting-row compact">
          <label>暗标：</label>
          <el-switch v-model="generateSettings.anonymous" />
        </div>
        <div class="setting-row with-note">
          <label>自动配图：</label>
          <el-radio-group v-model="generateSettings.autoImage" size="small">
            <el-radio-button label="normal">一般</el-radio-button>
            <el-radio-button label="less">较少</el-radio-button>
            <el-radio-button label="none">无</el-radio-button>
          </el-radio-group>
          <span class="setting-note">PNG等图片参考</span>
        </div>
        <div class="setting-row writing-style">
          <label>写作风格：</label>
          <el-radio-group v-model="generateSettings.writingStyle" class="style-grid">
            <el-radio label="general" border>通用型</el-radio>
            <el-radio label="data" border>数据型</el-radio>
            <el-radio label="concise" border>简练型</el-radio>
            <el-radio label="practical" border>实用型</el-radio>
          </el-radio-group>
        </div>
      </div>
      <div slot="footer" class="generate-dialog-footer">
        <el-button size="small" @click="generateDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="generatingContent" @click="confirmContentGenerate">开始生成</el-button>
      </div>
    </el-dialog>
    <el-dialog title="导出完成" :visible.sync="exportDialogVisible" width="520px" append-to-body>
      <div class="export-result">
        <p>导出文件已生成。</p>
        <el-link v-if="exportResult.fileUrl" type="primary" :href="resourceUrl(exportResult.fileUrl)" target="_blank">
          {{ exportResult.downloadName || '打开导出文件' }}
        </el-link>
      </div>
      <div slot="footer">
        <el-button type="primary" @click="exportDialogVisible = false">知道了</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import { exportPlanHtml, listBids } from '@/api/bid/bids'
import { generateContentBlocks, listContentsByOutlines, saveRichContent } from '@/api/bid/contents'
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
import RichContentEditor from '@/views/bid/components/RichContentEditor.vue'
import WordPresetDialog from './components/WordPresetDialog.vue'

export default {
  name: 'BidPlanOutline',
  components: {
    draggable,
    WordPresetDialog,
    AddNodeDialog,
    AiWritingDialog,
    NodeTitle,
    NodeActions,
    RichContentEditor
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
      generateDialogVisible: false,
      generatingContent: false,
      generateSettings: {
        chartCount: 'none',
        tableCount: 'none',
        knowledgeMode: '',
        anonymous: false,
        autoImage: 'none',
        writingStyle: 'general'
      },
      contentMap: {},
      selectedContentNode: null,
      contentLoading: false,
      contentSaving: false,
      rewritingNodeId: undefined,
      exportingNodeId: undefined,
      exportDialogVisible: false,
      exportResult: {},
      wordOptions: [300, 600, 900, 1200, 1800, 2700, 3600, 4500, 5400, 6300, 7200, 8100, 9000, 9900]
    }
  },
  computed: {
    generatedWordsCount() {
      const leaves = this.flattenLeaves(this.tree)
      const count = leaves.reduce((total, leaf) => total + this.nodeGeneratedWords(leaf), 0)
      return count || Number(this.stats.generatedWords || 0)
    },
    currentPageCount() {
      if (this.generatedWordsCount > 0) {
        return Math.max(1, Math.ceil(this.generatedWordsCount / 560))
      }
      return Number(this.stats.currentPages || 0)
    },
    selectedContentLevel() {
      return Number(this.selectedContentNode && this.selectedContentNode.level)
    },
    visibleChapterTitle() {
      return this.selectedContentLevel === 1 ? this.selectedContentNode : null
    },
    visibleSectionTitle() {
      if (this.selectedContentLevel === 1) {
        return this.firstSection(this.selectedContentNode)
      }
      if (this.selectedContentLevel === 2) {
        return this.selectedContentNode
      }
      return null
    },
    visibleParagraphTitle() {
      if (this.selectedContentLevel === 1) {
        const section = this.firstSection(this.selectedContentNode)
        return this.firstContentNode(section || this.selectedContentNode)
      }
      if (this.selectedContentLevel === 2) {
        return this.firstContentNode(this.selectedContentNode)
      }
      if (this.selectedContentLevel === 3) {
        return this.selectedContentNode
      }
      return null
    },
    selectedContentBlocks() {
      if (!this.selectedContentNode) return []
      const ownBlocks = this.contentMap[this.selectedContentNode.id] || []
      if (ownBlocks.length || Number(this.selectedContentNode.level) === 3) {
        return ownBlocks
      }
      const text = this.buildNodeContentText(this.selectedContentNode)
      if (!text) return []
      return [{
        id: 'summary-' + this.selectedContentNode.id,
        outlineId: this.selectedContentNode.id,
        contentType: 1,
        sortOrder: 1,
        content: JSON.stringify({ text })
      }]
    }
  },
  created() {
    this.bidId = this.$route.query.bidId
    this.loadPlans()
    const overview = this.loadOverview()
    if (overview && overview.then) {
      overview.then(() => this.loadAllGeneratedContents(true))
    }
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
      this.contentMap = {}
      this.selectedContentNode = null
      this.loadOverview().then(() => this.loadAllGeneratedContents(true))
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
        this.loadOverview().then(() => this.loadAllGeneratedContents(true))
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
      if (this.selectedContentNode) {
        const freshNode = this.findTreeNode(this.tree, this.selectedContentNode.id)
        this.selectedContentNode = freshNode || null
      }
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
        node._originWritingDirection = node.writingDirection || ''
        node._originWritingRequirement = node.writingRequirement || ''
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
    normalizeText(value) {
      return value == null ? '' : String(value).trim()
    },
    isWritingDirectionDirty(node) {
      return this.normalizeText(node && node.writingDirection) !== this.normalizeText(node && node._originWritingDirection)
    },
    isWritingRequirementDirty(node) {
      return this.normalizeText(node && node.writingRequirement) !== this.normalizeText(node && node._originWritingRequirement)
    },
    isWritingDirty(node) {
      return Number(node && node.level) === 3 && (this.isWritingDirectionDirty(node) || this.isWritingRequirementDirty(node))
    },
    findTreeNode(nodes, nodeId) {
      for (const node of nodes || []) {
        if (String(node.id) === String(nodeId)) return node
        const child = this.findTreeNode(node.children || [], nodeId)
        if (child) return child
      }
      return null
    },
    findNodePath(nodes, nodeId, path = []) {
      for (const node of nodes || []) {
        const nextPath = path.concat(node)
        if (String(node.id) === String(nodeId)) return nextPath
        const childPath = this.findNodePath(node.children || [], nodeId, nextPath)
        if (childPath.length) return childPath
      }
      return []
    },
    nodeAtLevel(node, level) {
      if (!node) return null
      if (Number(node.level) === Number(level)) return node
      const path = this.findNodePath(this.tree, node.id)
      return path.find(item => Number(item.level) === Number(level)) || null
    },
    firstSection(node) {
      if (!node) return null
      if (Number(node.level) === 2) return node
      const children = node.children || []
      const direct = children.find(item => Number(item.level) === 2)
      if (direct) return direct
      for (const child of children) {
        const section = this.firstSection(child)
        if (section) return section
      }
      return null
    },
    firstContentNode(node) {
      if (!node) return null
      if (Number(node.level) === 3) return node
      for (const child of node.children || []) {
        const leaf = this.firstContentNode(child)
        if (leaf) return leaf
      }
      return null
    },
    saveWritingNode(node, title, titleDirty) {
      const cleanTitle = this.normalizeText(title)
      const titleChanged = !!titleDirty && !!cleanTitle
      const directionChanged = this.isWritingDirectionDirty(node)
      const requirementChanged = this.isWritingRequirementDirty(node)
      const tasks = []
      if (titleChanged) {
        tasks.push(() => updatePlanOutlineTitle(this.bidId, node.id, { title: cleanTitle }))
      }
      if (Number(node.level) === 3 && directionChanged) {
        tasks.push(() => saveWritingDirection(this.bidId, node.id, {
          content: node.writingDirection || '',
          mode: node.directionMode || ''
        }))
      }
      if (Number(node.level) === 3 && requirementChanged) {
        tasks.push(() => saveWritingRequirement(this.bidId, node.id, {
          content: node.writingRequirement || '',
          mode: node.requirementMode || ''
        }))
      }
      if (!tasks.length) return
      tasks.reduce((chain, task) => {
        return chain.then(() => task())
      }, Promise.resolve()).then(res => {
        if (res && res.data) {
          this.refreshData(res.data)
        }
        this.$modal.msgSuccess('保存成功')
        if (Number(node.level) === 3 && titleChanged) {
          this.$modal.confirm('是否重新生成本段编写方向？').then(() => {
            this.openAi('direction', this.findTreeNode(this.tree, node.id) || node)
          }).catch(() => {})
        }
      })
    },
    saveDirection(node) {
      return saveWritingDirection(this.bidId, node.id, {
        content: node.writingDirection || '',
        mode: node.directionMode || ''
      }).then(res => {
        this.refreshData(res.data)
        this.$modal.msgSuccess('编写方向已保存')
      })
    },
    saveRequirement(node) {
      return saveWritingRequirement(this.bidId, node.id, {
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
      this.generateDialogVisible = true
    },
    setGenerateKnowledgeMode(mode) {
      this.generateSettings.knowledgeMode = this.generateSettings.knowledgeMode === mode ? '' : mode
    },
    confirmContentGenerate() {
      if (!this.bidId || this.generatingContent) return
      this.generateDialogVisible = false
      this.generatingContent = true
      finalizePlanOutline(this.bidId).then(() => {
        this.resetRichEditorDirty()
        this.flattenLeaves(this.tree).forEach(leaf => {
          this.$set(this.contentMap, leaf.id, [])
        })
        return generateContentBlocks({
          bidId: this.bidId,
          scope: 'full',
          mode: 'overwrite',
          requirement: this.buildGenerateRequirement(),
          writingStyle: this.generateSettings.writingStyle,
          includeTable: this.generateSettings.tableCount !== 'none',
          includeDiagram: this.generateSettings.chartCount !== 'none' || this.generateSettings.autoImage !== 'none',
          knowledgeFileIds: [],
          knowledgeChunkIds: []
        }, {
          onContent: payload => this.appendGeneratedContentChunk(payload),
          onGenerated: contents => this.applyGeneratedContents(contents)
        })
      }).then(() => {
        return this.loadOverview()
      }).then(() => {
        return this.loadAllGeneratedContents(true)
      }).then(() => {
        if (this.selectedContentNode) {
          const freshNode = this.findTreeNode(this.tree, this.selectedContentNode.id)
          if (freshNode && this.isNodeGenerated(freshNode)) {
            this.selectedContentNode = freshNode
          }
        }
        this.$modal.msgSuccess('正文生成完成')
      }).finally(() => {
        this.generatingContent = false
      })
    },
    regenerateContentNode(node) {
      if (!node || this.rewritingNodeId) return
      this.rewritingNodeId = node.id
      const active = this.findTreeNode(this.tree, node.id) || node
      this.selectedContentNode = active
      this.clearNodeContentMap(active)
      generateContentBlocks({
        bidId: this.bidId,
        outlineId: node.id,
        scope: 'selected',
        mode: 'overwrite',
        requirement: '请重编当前' + this.levelText(node.level) + '范围内正文内容，保持服务方案专业表达，并与上下级标题语义一致。',
        writingStyle: this.generateSettings.writingStyle,
        includeTable: false,
        includeDiagram: false,
        knowledgeFileIds: [],
        knowledgeChunkIds: []
      }, {
        onContent: payload => this.appendGeneratedContentChunk(payload),
        onGenerated: contents => this.applyGeneratedContents(contents)
      }).then(() => {
        this.$modal.msgSuccess('重编完成')
        return this.loadOverview()
      }).then(() => {
        const active = this.selectedContentNode ? (this.findTreeNode(this.tree, this.selectedContentNode.id) || this.selectedContentNode) : node
        this.selectedContentNode = active
        return this.loadNodeContents(active, true)
      }).finally(() => {
        this.rewritingNodeId = undefined
      })
    },
    exportContentNode(node) {
      if (!node || this.exportingNodeId) return
      this.exportingNodeId = node.id
      exportPlanHtml({
        bidId: this.bidId,
        outlineId: node.id,
        fileFormat: 'docx',
        includeEmptyOutline: true
      }).then(res => {
        this.exportResult = res.data || {}
        this.exportDialogVisible = true
      }).finally(() => {
        this.exportingNodeId = undefined
      })
    },
    resetRichEditorDirty() {
      const editor = this.$refs.contentRichEditor
      if (editor) {
        editor.dirty = false
      }
    },
    clearNodeContentMap(node) {
      if (!node) return
      const targets = [node]
      this.flattenLeaves([node]).forEach(leaf => {
        if (!targets.some(item => String(item.id) === String(leaf.id))) {
          targets.push(leaf)
        }
      })
      this.resetRichEditorDirty()
      targets.forEach(item => {
        this.$set(this.contentMap, item.id, [])
      })
    },
    applyGeneratedContents(contents) {
      const grouped = this.groupContentsByOutline(contents || [])
      this.resetRichEditorDirty()
      Object.keys(grouped).forEach(outlineId => {
        this.$set(this.contentMap, outlineId, grouped[outlineId] || [])
      })
    },
    appendGeneratedContentChunk(payload) {
      const outlineId = payload && payload.outlineId
      const chunk = payload && payload.content
      if (!outlineId || chunk == null || chunk === '') return
      const key = String(outlineId)
      const current = this.contentMap[key] || this.contentMap[outlineId] || []
      const streamId = 'stream-' + key
      const index = current.findIndex(item => String(item.id) === streamId)
      const target = index >= 0 ? current[index] : {
        id: streamId,
        outlineId,
        contentType: 1,
        sortOrder: 1,
        content: JSON.stringify({ text: '' })
      }
      const data = this.parseContent(target)
      data.text = (data.text || '') + String(chunk)
      target.content = JSON.stringify(data)
      const next = index >= 0 ? current.slice() : current.concat(target)
      if (index >= 0) {
        next.splice(index, 1, target)
      }
      this.resetRichEditorDirty()
      this.$set(this.contentMap, outlineId, next)
    },
    runEditorTool(action) {
      const editor = this.$refs.contentRichEditor
      if (!editor) return
      const handlers = {
        text: () => editor.openTextInsertDialog(),
        image: () => editor.openGalleryDialog(),
        table: () => editor.insertDefaultTable(),
        clear: () => editor.clearCharts(),
        refs: () => editor.openRefs(),
        fullscreen: () => editor.toggleFullscreen()
      }
      if (handlers[action]) handlers[action]()
    },
    buildGenerateRequirement() {
      const quantityMap = { normal: '一般', less: '较少', more: '较多', none: '无' }
      const styleMap = { general: '通用型', data: '数据型', concise: '简练型', practical: '实用型' }
      return [
        '请按当前章、节、段目录生成全文正文内容，保持服务方案专业表达。',
        '图表数量：' + quantityMap[this.generateSettings.chartCount],
        '表格数量：' + quantityMap[this.generateSettings.tableCount],
        '自动配图：' + quantityMap[this.generateSettings.autoImage],
        '写作风格：' + styleMap[this.generateSettings.writingStyle],
        '暗标要求：' + (this.generateSettings.anonymous ? '启用，避免出现投标人敏感身份信息。' : '不启用。')
      ].join('\n')
    },
    loadAllGeneratedContents(force) {
      const leaves = this.flattenLeaves(this.tree)
      return this.loadContentsForLeaves(leaves, force)
    },
    loadNodeContents(node, force) {
      if (!node) return Promise.resolve()
      const targets = [node]
      this.flattenLeaves([node]).forEach(leaf => {
        if (!targets.some(item => String(item.id) === String(leaf.id))) {
          targets.push(leaf)
        }
      })
      return this.loadContentsForLeaves(targets, force)
    },
    loadContentsForLeaves(leaves, force) {
      const targets = (leaves || []).filter(leaf => {
        return force || !Object.prototype.hasOwnProperty.call(this.contentMap, leaf.id)
      })
      if (!targets.length) return Promise.resolve()
      this.contentLoading = true
      return listContentsByOutlines(targets.map(leaf => leaf.id)).then(res => {
        const grouped = this.groupContentsByOutline(res.data || [])
        targets.forEach(leaf => {
          this.$set(this.contentMap, leaf.id, grouped[leaf.id] || [])
        })
      }).finally(() => {
        this.contentLoading = false
      })
    },
    groupContentsByOutline(contents) {
      const grouped = {}
      ;(contents || []).forEach(content => {
        const key = content.outlineId
        if (!grouped[key]) grouped[key] = []
        grouped[key].push(content)
      })
      Object.keys(grouped).forEach(key => {
        grouped[key].sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0))
      })
      return grouped
    },
    flattenLeaves(nodes, rows = []) {
      ;(nodes || []).forEach(node => {
        if (!node.children || !node.children.length || Number(node.level) === 3) {
          rows.push(node)
        } else {
          this.flattenLeaves(node.children, rows)
        }
      })
      return rows
    },
    parseContent(block) {
      if (!block || !block.content) return {}
      if (typeof block.content === 'object') return block.content
      try {
        return JSON.parse(block.content)
      } catch (e) {
        return { text: block.content }
      }
    },
    blockText(block) {
      return this.parseContent(block).text || ''
    },
    contentWordCount(blocks) {
      return (blocks || []).reduce((count, block) => {
        return Number(block.contentType) === 1 ? count + this.blockText(block).length : count
      }, 0)
    },
    nodeGeneratedWords(node) {
      if (!node) return 0
      if (node.children && node.children.length) {
        return node.children.reduce((count, child) => count + this.nodeGeneratedWords(child), 0)
      }
      if (Object.prototype.hasOwnProperty.call(this.contentMap, node.id)) {
        return this.contentWordCount(this.contentMap[node.id])
      }
      return Number(node.contentWords || 0)
    },
    nodeTargetWords(node) {
      if (!node) return 0
      if (node.children && node.children.length) {
        return node.children.reduce((count, child) => count + this.nodeTargetWords(child), 0)
      }
      return Number(node.wordLimit || 300)
    },
    isNodeGenerated(node) {
      return this.nodeGeneratedWords(node) > 0
    },
    isSelectedContentNode(node) {
      return !!this.selectedContentNode && String(this.selectedContentNode.id) === String(node.id)
    },
    selectGeneratedNode(node) {
      this.loadNodeContents(node, true).then(() => {
        const freshNode = this.findTreeNode(this.tree, node.id) || node
        this.selectedContentNode = freshNode
      })
    },
    buildNodeContentText(node) {
      const leaves = this.flattenLeaves([node])
      const parts = leaves.map(leaf => {
        const blocks = this.contentMap[leaf.id] || []
        const text = blocks
          .filter(block => Number(block.contentType) === 1)
          .map(block => this.blockText(block).trim())
          .filter(Boolean)
          .join('\n\n')
        if (!text) return ''
        return Number(node.level) === 3 ? text : leaf.title + '\n' + text
      }).filter(Boolean)
      return parts.join('\n\n')
    },
    closeContentPane() {
      this.selectedContentNode = null
    },
    saveSelectedRichContent(payload, done) {
      if (!this.selectedContentNode) return
      this.contentSaving = true
      let saved = false
      saveRichContent(Object.assign({}, payload, { bidId: this.bidId || payload.bidId })).then(res => {
        saved = true
        const content = res.data
        if (content && payload.outlineId) {
          this.$set(this.contentMap, payload.outlineId, [content])
        }
      }).catch(() => {
        this.$modal.msgError('富文本内容保存失败')
      }).finally(() => {
        this.contentSaving = false
        if (typeof done === 'function') done(saved)
      })
    },
    levelText(level) {
      return Number(level) === 1 ? '章级标题' : Number(level) === 2 ? '节级标题' : '段级标题'
    },
    resourceUrl(url) {
      if (!url) return ''
      if (/^https?:\/\//.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
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
  min-height: 76px;
  border-bottom: 1px solid #dfe4ee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}
.project-head h1 {
  margin: 0 0 8px;
  font-size: 18px;
  line-height: 1.25;
  color: #1f2d3d;
}
.project-head p {
  margin: 2px 0;
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
  padding: 10px 0;
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
  font-size: 14px;
  line-height: 1.5;
}
.project-head span {
  margin-left: 28px;
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
  font-size: 14px;
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
  grid-template-columns: minmax(360px, 390px) minmax(0, 1fr);
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
  padding: 0 6px 0 8px;
  overflow: hidden;
  background: #fff;
}
.outline-top-fixed {
  background: #fff;
  border-bottom: 1px solid #e7ebf3;
}
.preview-layout .outline-top-fixed {
  width: 100%;
  max-width: none;
  margin: 0 auto;
}
.preview-body {
  min-height: 0;
  overflow: auto;
  border-bottom: 1px solid #e7ebf3;
  padding-right: 2px;
}
.outline-tree {
  width: 100%;
  max-width: none;
  margin: 0 auto;
  padding: 6px 2px 10px;
  font-size: 13px;
}
.preview-node {
  min-height: 23px;
  line-height: 23px;
  color: #606266;
}
.preview-node.level-1 {
  font-weight: 700;
  color: #303133;
}
.preview-node.level-2 {
  margin-left: 16px;
  font-weight: 500;
}
.preview-node.level-3 {
  margin-left: 32px;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.preview-node em {
  margin-left: auto;
  color: #909399;
  font-style: normal;
  min-width: 64px;
  text-align: right;
  font-size: 12px;
}
.preview-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  cursor: default;
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
  cursor: pointer;
}
.preview-text {
  min-width: 0;
}
.preview-text.clickable {
  cursor: pointer;
  color: #303133;
}
.preview-text.clickable:hover,
.preview-text.active {
  color: #2f66ff;
}
.preview-text.disabled {
  cursor: not-allowed;
  color: #606266;
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
  min-height: 86px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 0;
  background: #fff;
  border-top: 1px solid #e7ebf3;
}
.preview-actions .el-button {
  width: 100%;
  max-width: 400px;
}
.preview-actions .generating-button {
  max-width: 430px;
  border: 0;
  background: linear-gradient(135deg, #2f6dff, #6c42ff);
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
.preview-hero:not(.edit-hero) {
  padding: 46px 26px 42px;
}
.preview-hero:not(.edit-hero) .hero-stage {
  grid-template-rows: auto auto auto;
  row-gap: 38px;
  align-content: space-between;
}
.preview-hero:not(.edit-hero) .hero-card-grid {
  height: auto;
  align-items: stretch;
}
.preview-hero:not(.edit-hero) .hero-card {
  height: auto;
  min-height: 250px;
}
.preview-hero:not(.edit-hero) .hero-icon {
  width: 56px;
  height: 56px;
  margin-top: 24px;
}
.preview-hero:not(.edit-hero) .hero-icon i {
  font-size: 26px;
}
.content-pane {
  min-width: 0;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  padding: 0;
}
.content-title-stack {
  flex: 0 0 auto;
  border-bottom: 1px solid #dfe4ee;
  background: #fff;
}
.content-title-row {
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f6;
  color: #111827;
}
.content-title-row:last-child {
  border-bottom: 0;
}
.content-title-row strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
}
.chapter-title-row {
  justify-content: flex-end;
}
.chapter-title-row strong {
  max-width: 42%;
  font-size: 16px;
}
.section-title-row {
  border-left: 4px solid #6b7280;
}
.paragraph-title-row {
  min-height: 38px;
}
.title-count {
  flex: 0 0 auto;
  color: #8b95a7;
  font-size: 12px;
}
.close-pane-button {
  margin-left: auto;
}
.editor-toolbox {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.editor-toolbox .el-button {
  padding: 6px;
  border-color: #d9e1ef;
}
.content-pane-head {
  max-width: 900px;
  margin: 0 auto 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}
.content-pane-head h2 {
  margin: 6px 0 8px;
  color: #1f2d3d;
  font-size: 24px;
  line-height: 1.35;
}
.content-pane-head p {
  margin: 0;
  color: #6b778c;
  font-size: 13px;
}
.content-level {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: #edf3ff;
  color: #2f66ff;
  font-size: 12px;
}
.content-editor-shell {
  max-width: none;
  min-height: 0;
  margin: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 0;
  border-radius: 0;
  overflow: hidden;
}
.content-editor-shell.rich-editor-shell {
  height: auto;
  flex: 1;
}
.content-editor-shell ::v-deep .rich-content-editor {
  height: 100%;
  min-height: 0;
}
.content-editor-shell ::v-deep .editor-stage {
  background: #fff;
}
.content-editor-shell ::v-deep .tiptap-host {
  max-width: none;
  min-height: 0;
  border: 0;
}
.content-editor-shell ::v-deep .tiptap-host .tiptap-surface {
  min-height: calc(100vh - 214px);
  padding: 18px 28px 42px;
  font-size: 15px;
  line-height: 1.65;
}
.content-editor-shell ::v-deep .tiptap-surface p {
  margin-bottom: 7px;
}
.content-editor-shell ::v-deep .tiptap-surface table {
  margin: 8px 0 12px;
}
.content-editor-shell ::v-deep .tiptap-surface th,
.content-editor-shell ::v-deep .tiptap-surface td {
  padding: 6px 8px;
}
.rich-editor-toolbar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #edf0f6;
  color: #303133;
  font-weight: 700;
}
.rich-editor {
  flex: 1;
  min-height: 540px;
  padding: 18px 22px;
  outline: none;
  color: #1f2d3d;
  font-size: 15px;
  line-height: 1.78;
  white-space: pre-wrap;
  background: #fff;
}
.rich-editor.readonly {
  background: #fbfcff;
}
.rich-editor:empty::before {
  content: '暂无正文内容';
  color: #a8abb2;
}
.content-note {
  max-width: 900px;
  margin: 10px auto 0;
  color: #8b95a7;
  font-size: 12px;
}
.edit-hero {
  padding: 100px 24px 82px;
  align-items: flex-start;
  overflow: auto;
}
.hero-stage {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  row-gap: 18px;
}
.edit-hero .hero-stage {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  grid-template-rows: auto auto auto;
  row-gap: 100px;
  align-content: start;
}
.hero-top {
  text-align: center;
}
.hero-top h2 {
  margin: 8% 0 10px;
  font-size: 32px;
  color: #1f2d3d;
}
.hero-top p {
  margin: 2% auto;
  max-width: 980px;
  font-size: 18px;
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
.edit-hero .hero-card-grid {
  height: auto;
  gap: 14px;
  align-items: start;
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
.edit-hero .hero-card {
  height: auto;
  min-height: 230px;
  padding: 20px 16px 18px;
  justify-content: flex-start;
}
.hero-card h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #1f2d3d;
  line-height: 1.4;
}
.hero-card p {
  margin: 0;
  font-size: 16px;
  line-height: 1.78;
  color: #3d4a63;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 8;
}
.edit-hero .hero-card p {
  font-size: 16px;
  line-height: 1.72;
  -webkit-line-clamp: 5;
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
.edit-hero .hero-icon {
  width: 58px;
  height: 58px;
  margin: 30px auto 0;
}
.hero-icon i {
  font-size: 36px;
  color: #6a78ee;
}
.edit-hero .hero-icon i {
  font-size: 28px;
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
  margin: 14px 10px 10px;
  background: #f6f6f7;
  border-radius: 8px;
  padding: 14px 16px;
}
.rule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
  line-height: 1.35;
}
.rule-title .el-button {
  margin-left: auto;
}
.edit-tree {
  padding: 8px 0 42px;
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
  min-height: 34px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px dashed #d9dde6;
  color: #303133;
  font-size: 14px;
  line-height: 1.35;
}
.node-line .el-icon-caret-bottom,
.node-line .el-icon-caret-right {
  cursor: pointer;
  color: #5c6678;
}
.node-line.level-1 {
  margin-top: 6px;
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
.writing-box {
  margin-left: 74px;
  margin-bottom: 10px;
  background: #f7f8fb;
  border-radius: 8px;
  padding: 10px 14px;
}
.writing-row {
  position: relative;
  margin-bottom: 10px;
}
.writing-row label {
  display: block;
  margin-bottom: 6px;
  color: #303133;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
}
.edit-layout ::v-deep .el-input__inner,
.edit-layout ::v-deep .el-textarea__inner {
  font-size: 14px;
  color: #303133;
}
.edit-layout ::v-deep .el-textarea__inner {
  line-height: 1.45;
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
::v-deep .generate-setting-dialog .el-dialog__body {
  padding: 22px 34px 18px;
}
.generate-setting-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}
.setting-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.setting-row label {
  width: 86px;
  flex: 0 0 86px;
  color: #303133;
  font-size: 14px;
  font-weight: 700;
}
.setting-row.compact {
  gap: 10px;
}
.setting-row.with-note {
  flex-wrap: wrap;
  row-gap: 6px;
}
.setting-note {
  width: 100%;
  margin-left: 102px;
  color: #f56c6c;
  font-size: 11px;
}
.setting-actions {
  display: flex;
  gap: 18px;
}
.setting-actions .el-button.active {
  color: #2f66ff;
  border-color: #8fb4ff;
  background: #f2f6ff;
}
.style-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}
.style-grid ::v-deep .el-radio {
  margin-right: 0;
  height: 34px;
  line-height: 32px;
}
.generate-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
    font-size: 14px;
  }
  .hero-card p {
    font-size: 14px;
  }
}
</style>
