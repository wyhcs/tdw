<template>
  <div class="plan-create-page">
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
      <div class="plan-scroll">
        <div v-if="planList.length" class="plan-list">
          <div
            v-for="item in planList"
            :key="item.id"
            class="plan-card"
            :class="{ active: String(item.id) === String(bidId) }"
            role="button"
            tabindex="0"
            @click="openEditor(item)"
            @keyup.enter="openEditor(item)"
          >
            <div class="plan-card-head">
              <span class="plan-name">
                <i class="el-icon-document" />
                {{ item.title }}
              </span>
              <span class="plan-actions">
                <el-tooltip content="编辑标题" placement="top">
                  <button type="button" class="plan-icon-btn edit" @click.stop="renamePlan(item)">
                    <i class="el-icon-edit-outline" />
                  </button>
                </el-tooltip>
                <el-tooltip content="删除方案" placement="top">
                  <button type="button" class="plan-icon-btn delete" @click.stop="removePlan(item)">
                    <i class="el-icon-delete" />
                  </button>
                </el-tooltip>
              </span>
            </div>
            <span class="plan-meta">{{ item.category || '普通' }} · {{ parseTime(item.createdTime, '{y}-{m}-{d}') }}</span>
          </div>
          <div class="plan-end">—没有更多方案了—</div>
        </div>
        <div v-else class="empty-side">
          <i class="el-icon-document" />
          <span>暂无方案，您可先新建方案</span>
        </div>
      </div>

      <el-button class="new-button" type="primary" icon="el-icon-plus" @click="resetCreate">新建方案</el-button>
    </aside>

    <main class="create-main">
      <header class="step-header">
        <el-button size="mini" icon="el-icon-arrow-left" @click="$router.push('/bid/plan')">退出新建</el-button>
        <div class="steps">
          <div v-for="step in steps" :key="step.value" class="step-item" :class="{ active: activeStep >= step.value }">
            <span>{{ step.value }}</span>
            <b>{{ step.label }}</b>
          </div>
        </div>
      </header>

      <div class="workbench">
        <section class="config-panel">
          <el-form ref="form" :model="form" :rules="rules" label-position="top" class="plan-form">
            <el-form-item class="required-form-item" label="方案类型" prop="category">
              <div class="type-grid">
                <el-dropdown
                  v-for="item in categoryOptions"
                  :key="item.value"
                  trigger="click"
                  placement="bottom-start"
                  @command="handleCategoryCommand"
                >
                  <button
                    type="button"
                    class="type-button"
                    :class="{ selected: form.category === item.value }"
                    @click="selectCategory(item.value)"
                  >
                    <i :class="item.icon" />
                    <span>{{ categoryButtonText(item) }}</span>
                    <i class="el-icon-arrow-down type-arrow" />
                  </button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item
                      v-for="option in item.options"
                      :key="item.value + option"
                      :command="{ category: item.value, subcategory: option }"
                    >
                      {{ option }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </el-form-item>

            <el-form-item class="required-form-item" label="选择AI">
              <div class="ai-grid">
                <button
                  v-for="item in aiOptions"
                  :key="item.value"
                  type="button"
                  class="ai-card"
                  :class="{ selected: form.aiEdition === item.value, disabled: item.disabled }"
                  @click="selectAiEdition(item)"
                >
                  <span class="edition-icon" :class="item.value"><i :class="item.icon" /></span>
                  <span class="edition-main">
                    <strong>{{ item.label }}</strong>
                    <em>{{ item.desc }}</em>
                  </span>
                  <span v-if="item.tag" class="edition-tag">{{ item.tag }}</span>
                  <small>{{ item.footer }}</small>
                  <b v-if="item.warn" class="edition-warn"><i class="el-icon-warning-outline" /> {{ item.warn }}</b>
                </button>
              </div>
            </el-form-item>

            <el-form-item class="read-form-item" label="智能读取" prop="file">
              <el-upload
                ref="tenderUpload"
                action="#"
                drag
                :auto-upload="false"
                :limit="1"
                :file-list="tenderFileList"
                :show-file-list="false"
                :on-change="handleTenderChange"
                :on-remove="handleTenderRemove"
              >
                <div v-if="tenderFileList.length" class="upload-ready">
                  <span class="word-mark">W</span>
                  <span class="file-name"><i class="el-icon-document" /> {{ tenderFileList[0].name }}</span>
                  <span class="file-status"><i class="el-icon-success" /> 文件准备就绪</span>
                </div>
                <div v-else class="upload-empty">
                  <i class="el-icon-document-checked upload-icon" />
                  <strong>上传单个招标文件（Word/PDF &lt; 50MB）</strong>
                  <span>智能读取标书信息</span>
                </div>
                <div slot="tip" class="upload-tip">温馨提示：解析标书前将清空基础信息表</div>
              </el-upload>
              <div class="parse-action">
                <el-button size="mini" type="primary" plain :loading="parsingMaterial" :disabled="!form.file" @click="startParsePreview">开始解析</el-button>
              </div>
            </el-form-item>

            <el-form-item class="required-form-item plan-name-item" label="方案名称" prop="title">
              <el-input v-model="form.title" maxlength="100" placeholder="请填写方案名称" />
            </el-form-item>

            <div class="switch-strip">
              <span>技术偏离表：</span>
              <el-switch v-model="form.techDeviation" active-text="启用" inactive-text="" @change="handleTechDeviationToggle" />
              <el-button
                v-if="form.techDeviation"
                class="deviation-select-button"
                size="mini"
                type="primary"
                plain
                icon="el-icon-arrow-down"
                @click="openTechDeviationDialog"
              >
                选择表头内容
              </el-button>
            </div>

            <div v-if="form.techDeviation" class="deviation-template-row">
              <el-input
                type="textarea"
                :value="techDeviationRowText"
                readonly
                :autosize="{ minRows: 3, maxRows: 8 }"
                placeholder="请选择表头内容"
              />
            </div>

            <div v-if="form.category === '工程'" class="switch-strip project-switch">
              <span>工程六表：</span>
              <el-switch v-model="form.projectSixTables" active-text="启用" inactive-text="" />
            </div>

            <div class="dynamic-fields">
              <div
                v-for="section in activeFieldSections"
                :key="section.key"
                class="parse-section"
                :class="{ collapsed: collapsedSections[section.key] }"
              >
                <div class="section-head">
                  <span v-if="section.required" class="required-dot">*</span>
                  <strong>{{ section.label }}：</strong>
                  <el-button
                    v-if="isPrimaryRequirementSection(section) && showTenderProcurementAction"
                    class="inline-extract"
                    size="mini"
                    type="primary"
                    plain
                    :loading="extractingTenderProcurement"
                    :disabled="extractingTenderProcurement || !form.file"
                    @click="extractFieldFromTender(section)"
                  >
                    {{ extractingTenderProcurement ? '正在提取' : '从招标文件重新提取' }}
                  </el-button>
                  <el-upload
                    v-if="section.upload"
                    action="#"
                    :auto-upload="false"
                    :limit="section.limit || 1"
                    :file-list="fieldFileLists[section.key] || []"
                    :show-file-list="false"
                    :on-change="(file, fileList) => handleSectionFileChange(section, file, fileList)"
                    :on-exceed="handleSectionExceed"
                  >
                    <el-button
                      class="inline-upload"
                      size="mini"
                      type="primary"
                      plain
                      icon="el-icon-upload2"
                      :loading="fieldParsing[section.key]"
                      :disabled="isSectionUploadDisabled(section)"
                    >
                      {{ section.uploadLabel }}
                    </el-button>
                  </el-upload>
                  <span v-if="section.hint" class="section-hint">{{ section.hint }}</span>
                  <button type="button" class="collapse-button" @click="toggleSection(section.key)">
                    <i class="el-icon-arrow-up" /> {{ collapsedSections[section.key] ? '展开' : '收起' }}
                  </button>
                </div>
                <div v-show="!collapsedSections[section.key]" class="section-body">
                  <el-input
                    v-model="form.fields[section.key]"
                    type="textarea"
                    :rows="section.rows || 4"
                    maxlength="100000"
                    show-word-limit
                    :placeholder="section.placeholder"
                  />
                  <div v-if="fieldFileLists[section.key] && fieldFileLists[section.key].length" class="field-files">
                    <span v-for="file in fieldFileLists[section.key]" :key="file.uid || file.name">
                      <i class="el-icon-paperclip" /> {{ file.name }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="directory-section">
              <div class="section-head">
                <span class="required-dot">*</span>
                <strong>目录要求：</strong>
                <el-button
                  v-if="form.directoryMode !== 'custom' && showDirectoryExtractActions"
                  class="technical-score-button"
                  size="mini"
                  type="primary"
                  plain
                  icon="el-icon-refresh"
                  :loading="extractingFullScore"
                  :disabled="extractingFullScore || !form.file"
                  @click="extractScoreItemsFromTender"
                >
                  {{ extractingFullScore ? '正在提取完整评分项' : '重新提取完整评分项' }}
                </el-button>
                <el-button
                  v-if="form.directoryMode !== 'custom' && showDirectoryExtractActions"
                  class="technical-score-button"
                  size="mini"
                  type="primary"
                  plain
                  :loading="extractingTechnicalScore"
                  :disabled="extractingFullScore || !(form.fullScoreItems || form.scoreItemsText)"
                  @click="extractTechnicalScoreItems"
                >
                  {{ extractingTechnicalScore ? '提取技术评分项' : '提取技术评分项' }}
                </el-button>
                <button type="button" class="collapse-button" @click="toggleSection('directory')">
                  <i class="el-icon-arrow-up" /> {{ collapsedSections.directory ? '展开' : '收起' }}
                </button>
              </div>
              <div v-show="!collapsedSections.directory" class="directory-body">
                <div class="directory-tabs">
                  <el-button
                    v-for="item in directoryModes"
                    :key="item.value"
                    size="mini"
                    :type="form.directoryMode === item.value ? 'primary' : ''"
                    :plain="form.directoryMode !== item.value"
                    :icon="item.icon"
                    :loading="item.value === 'requirement' && extractingRequirementScore"
                    @click="handleDirectoryModeClick(item)"
                  >
                    {{ item.label }}
                  </el-button>
                </div>
                <el-input
                  v-if="form.directoryMode === 'score' || form.directoryMode === 'requirement'"
                  v-model="form.scoreItemsText"
                  class="score-textarea"
                  type="textarea"
                  maxlength="100000"
                  show-word-limit
                  :autosize="{ minRows: 5, maxRows: 12 }"
                  placeholder="评分项会在解析后自动填入，也可以手动粘贴完整评分标准"
                  @focus="openScoreCompareIfReady"
                  @click.native="openScoreCompareIfReady"
                />
                <div v-if="form.directoryMode === 'custom'" class="custom-chapter-list">
                  <div
                    v-for="(chapter, index) in form.customChapters"
                    :key="chapter.uid"
                    class="custom-chapter-card"
                  >
                    <div class="custom-chapter-head">
                      <strong>第 {{ index + 1 }} 章</strong>
                      <el-button
                        type="text"
                        icon="el-icon-delete"
                        :disabled="form.customChapters.length === 1"
                        @click="removeCustomChapter(index)"
                      />
                    </div>
                    <el-input
                      v-model="chapter.title"
                      size="small"
                      placeholder="请输入章标题"
                    />
                    <el-input
                      v-model="chapter.requirement"
                      class="custom-chapter-requirement"
                      type="textarea"
                      :autosize="{ minRows: 3, maxRows: 6 }"
                      placeholder="节段要求：准确列举该大章下您期望的阶段组成或编写方向。&#10;温馨提示：您可参考招标文件中关于本章的评分项目自行组织语言或直接粘贴。"
                    />
                  </div>
                  <div class="custom-chapter-add">
                    <el-button type="text" @click="addCustomChapter">新增章</el-button>
                  </div>
                </div>
                <div class="directory-tips">
                  <p v-for="line in activeDirectoryTips" :key="line">※ {{ line }}</p>
                </div>
              </div>
            </div>
          </el-form>
        </section>

        <section class="preview-panel">
          <div class="preview-title">
            <span>预览目录 {{ outlineRows.length }}</span>
            <small><i class="el-icon-bell" /> 一键差异化目录可改变标题内容，减少查重隐患</small>
          </div>
          <div class="outline-preview">
            <div v-if="!outlineRows.length" class="outline-empty">
              <div class="outline-column-title">大纲</div>
              <p>暂无目录，请在左侧输入目录要求，点击下方生成按钮，生成目录</p>
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

          <div v-if="outlineRows.length" class="content-stage">
            <div class="stage-title">第二阶段：上传全文段落生成提示词</div>
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="1"
              :file-list="promptFileList"
              :on-change="handlePromptChange"
              :on-remove="handlePromptRemove"
            >
              <el-button size="small" icon="el-icon-upload2">上传{{ form.category }}类-全文段落生成提示词.docx</el-button>
              <span slot="tip" class="el-upload__tip">支持 doc/docx，文件大小不超过 50MB</span>
            </el-upload>
          </div>

          <div class="preview-actions">
            <el-button :type="mode === 'accurate' ? 'primary' : ''" plain @click="mode = 'accurate'">精准模式</el-button>
            <el-button :type="mode === 'rich' ? 'primary' : ''" plain @click="mode = 'rich'">丰富模式</el-button>
            <el-button
              v-if="!outlineRows.length"
              type="primary"
              icon="el-icon-magic-stick"
              :loading="generatingOutline"
              @click="submitGenerateOutline"
            >
              生成目录
            </el-button>
            <el-button
              v-else
              type="primary"
              icon="el-icon-set-up"
              @click="wordDialogVisible = true"
            >
              设置篇幅
            </el-button>
          </div>
        </section>
      </div>
    </main>

    <word-preset-dialog
      :visible.sync="wordDialogVisible"
      :leaf-count="outlineRows.filter(item => Number(item.level) === 3).length"
      :submitting="wordSubmitting"
      @confirm="confirmWordPreset"
    />

    <el-dialog
      title="技术偏离表模板选择"
      :visible.sync="techDeviationDialogVisible"
      width="760px"
      append-to-body
      class="tech-deviation-dialog"
    >
      <div class="deviation-template-card">
        <el-radio v-model="selectedDeviationTemplate" label="template1">技术偏离表模板1</el-radio>
        <el-table
          class="deviation-template-table"
          :data="deviationDraftRows"
          border
          size="mini"
        >
          <el-table-column
            v-for="column in deviationColumns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
          >
            <template slot-scope="scope">
              <el-input
                v-model="scope.row[column.prop]"
                size="mini"
                clearable
                :placeholder="column.label"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="techDeviationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTechDeviationTemplate">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="评分项对比结果"
      :visible.sync="scoreCompareVisible"
      width="860px"
      append-to-body
      class="score-compare-dialog"
    >
      <div class="score-compare-grid">
        <div class="score-compare-pane">
          <h4>完整评分标准</h4>
          <el-input
            v-model="form.fullScoreItems"
            type="textarea"
            :autosize="{ minRows: 12, maxRows: 18 }"
            readonly
          />
        </div>
        <div class="score-compare-pane">
          <h4>技术评分项</h4>
          <el-input
            v-model="form.technicalScoreItems"
            type="textarea"
            :autosize="{ minRows: 12, maxRows: 18 }"
            readonly
          />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="useFullScoreItems">取消</el-button>
        <el-button type="primary" :disabled="!form.technicalScoreItems" @click="applyTechnicalScoreItems">使用技术评分项</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createPlanWithMaterial,
  deleteBid,
  getBids,
  getLatestPlanReport,
  listBids,
  listPlanMaterials,
  parsePlanMaterial,
  updateBids,
  uploadPlanMaterial
} from '@/api/bid/bids'
import { generateOutline, getOutlineTree } from '@/api/bid/outlines'
import { generateContentBlocks } from '@/api/bid/contents'
import { applyPlanWordPreset } from '@/api/bid/planOutline'
import { streamPost } from '@/utils/aiStream'
import WordPresetDialog from './components/WordPresetDialog.vue'

const createEmptyFields = () => ({
  projectOverview: '',
  procurementNeed: '',
  technicalRequirement: '',
  equipmentList: '',
  goodsInfo: '',
  otherAttachment: '',
  engineeringOverview: '',
  engineeringList: '',
  drawingParse: '',
  supervisionOverview: ''
})

const createEmptyDeviationRow = () => ({
  serialNo: '',
  bidDocumentNo: '',
  technicalRequirement: '',
  response: '',
  deviation: '',
  remark: ''
})

let customChapterSeed = 1
const createCustomChapter = () => ({
  uid: 'custom-chapter-' + customChapterSeed++,
  title: '',
  requirement: ''
})

export default {
  name: 'BidPlanCreate',
  components: { WordPresetDialog },
  data() {
    return {
      activeStep: 1,
      steps: [
        { value: 1, label: '选择方案类型' },
        { value: 2, label: '录入基础信息' },
        { value: 3, label: '生成预览目录' },
        { value: 4, label: '调整总字数' },
        { value: 5, label: '生成方案' }
      ],
      planQuery: '',
      planList: [],
      bidId: undefined,
      tenderFileId: undefined,
      tenderReportId: undefined,
      promptReportId: undefined,
      tenderFileList: [],
      promptFileList: [],
      fieldFileLists: {},
      fieldFileIds: {},
      fieldParsing: {},
      outlineTree: [],
      subcategoryMap: {
        服务: '不限',
        货物: '不限',
        工程: '不限',
        监理: '不限',
        IT信息: '不限',
        其他: '不限'
      },
      generatingOutline: false,
      generatingContent: false,
      wordDialogVisible: false,
      wordSubmitting: false,
      techDeviationDialogVisible: false,
      scoreCompareVisible: false,
      parsingMaterial: false,
      extractingTenderProcurement: false,
      extractingFullScore: false,
      extractingTechnicalScore: false,
      extractingRequirementScore: false,
      selectedDeviationTemplate: 'template1',
      parseReady: false,
      parseBuffers: {},
      collapsedSections: {},
      mode: 'accurate',
      form: {
        title: '',
        category: '服务',
        subcategory: '不限',
        aiEdition: 'basic',
        file: null,
        promptFile: null,
        writeStyle: '通用型',
        targetWords: 12000,
        techDeviation: false,
        techDeviationTemplateName: '',
        techDeviationRows: [],
        projectSixTables: false,
        useKnowledge: true,
        directoryMode: 'score',
        scoreItemsText: '',
        tenderScoreItems: '',
        requirementScoreItems: '',
        fullScoreItems: '',
        technicalScoreItems: '',
        useTechnicalScore: false,
        scoreSource: 'tender',
        customChapters: [createCustomChapter()],
        fields: createEmptyFields()
      },
      generateOptions: {
        includeTable: true,
        includeDiagram: true,
        includeGantt: false,
        quoteImages: false
      },
      categoryOptions: [
        { label: '服务', value: '服务', icon: 'el-icon-suitcase', options: ['不限', '物业管理', '审计服务', '广告印刷', '车辆维修'] },
        { label: '货物', value: '货物', icon: 'el-icon-box', options: ['不限', '食堂采购', '安防设备', '百货采购', '建筑采购'] },
        { label: '工程', value: '工程', icon: 'el-icon-office-building', options: ['不限', '市政工程', '房建工程', '拆除工程', '水利工程'] },
        { label: '监理', value: '监理', icon: 'el-icon-user', options: ['不限', '房建监理', '市政监理', '水利监理'] },
        { label: 'IT信息', value: 'IT信息', icon: 'el-icon-monitor', options: ['不限', '软件开发', '信息安全'] },
        { label: '其他', value: '其他', icon: 'el-icon-menu', options: ['不限'] }
      ],
      aiOptions: [
        { label: '基础版', value: 'basic', desc: '无次数限制', footer: '将消耗会员套餐', icon: 'el-icon-s-opportunity' },
        { label: '标准版', value: 'standard', desc: '无次数限制', footer: '将消耗会员套餐', icon: 'el-icon-s-marketing' },
        { label: '旗舰版', value: 'flagship', desc: '今日剩余3次', footer: '将消耗会员套餐', icon: 'el-icon-trophy' },
        { label: '专业版', value: 'pro', desc: '', footer: '', tag: '去充值>', warn: '请充值字数套餐', icon: 'el-icon-medal' }
      ],
      deviationDraftRows: [createEmptyDeviationRow()],
      deviationColumns: [
        { prop: 'serialNo', label: '序号', width: 58 },
        { prop: 'bidDocumentNo', label: '招标文件条目号', width: 126 },
        { prop: 'technicalRequirement', label: '招标文件的技术要求', width: 150 },
        { prop: 'response', label: '投标文件的技术响应', width: 150 },
        { prop: 'deviation', label: '响应/偏离/偏离偏差', width: 144 },
        { prop: 'remark', label: '说明/备注' }
      ],
      fieldSections: {
        服务: [
          { key: 'procurementNeed', label: '采购需求', required: true, upload: true, uploadLabel: '上传采购需求', placeholder: '※ 采购需求', rows: 5 },
          { key: 'otherAttachment', label: '其他附件', upload: true, uploadLabel: '上传附件', placeholder: '※ 其他附件：如技术规范等', rows: 4 }
        ],
        货物: [
          { key: 'procurementNeed', label: '采购需求', required: true, upload: true, uploadLabel: '上传采购需求', placeholder: '※ 采购需求', rows: 6 },
          { key: 'goodsInfo', label: '货物信息', required: true, upload: true, uploadLabel: '上传货物信息', placeholder: '※ 方案要求：方案内容撰写的依据，用于约束方案内容的撰写方向，并为内容的细节生成提供线索和背景信息\n※ 建议：粘贴或填写招标文件中与方案相关的货物参数、规格、交付要求等', rows: 4 },
          { key: 'otherAttachment', label: '其他附件', upload: true, uploadLabel: '上传附件', placeholder: '※ 其他附件：如技术规范等', rows: 4 }
        ],
        工程: [
          { key: 'engineeringOverview', label: '工程概况', required: true, placeholder: '※ 项目概况', rows: 4 },
          { key: 'engineeringList', label: '工程量清单', upload: true, uploadLabel: '上传工程量清单文件', placeholder: '※ 方案要求：方案内容撰写的依据，用于约束方案内容的撰写方向，并为内容的细节生成提供线索和背景信息\n※ 建议：粘贴或填写招标文件中与方案相关的工程量、施工范围、工期要求等', rows: 5 },
          { key: 'drawingParse', label: '图纸解析', upload: true, uploadLabel: '上传工程图纸', hint: '仅支持PDF,JPG,JPEG,PNG格式。（图片支持最多20张）', extensions: ['pdf', 'jpg', 'jpeg', 'png'], limit: 20, placeholder: '※ 图纸解析内容', rows: 4 }
        ],
        监理: [
          { key: 'supervisionOverview', label: '监理概况', required: true, placeholder: '※ 项目概况', rows: 4 },
          { key: 'engineeringOverview', label: '工程概况', required: true, placeholder: '※ 项目概况', rows: 4 },
          { key: 'drawingParse', label: '图纸解析', upload: true, uploadLabel: '上传工程图纸', hint: '仅支持PDF,JPG,JPEG,PNG格式。（图片支持最多20张）', extensions: ['pdf', 'jpg', 'jpeg', 'png'], limit: 20, placeholder: '※ 图纸解析内容', rows: 5 }
        ],
        IT信息: [
          { key: 'projectOverview', label: '项目概况', required: true, placeholder: '※ 采购需求', rows: 5 },
          { key: 'technicalRequirement', label: '技术要求（采购需求）', required: true, upload: true, uploadLabel: '上传技术要求', placeholder: '※ 其他附件：如技术规范等', rows: 5 },
          { key: 'equipmentList', label: '设备清单', upload: true, uploadLabel: '上传设备清单', placeholder: '※ 设备清单', rows: 5 },
          { key: 'otherAttachment', label: '其他附件（技术规范书）', upload: true, uploadLabel: '上传附件', placeholder: '※ 其他附件：如技术规范等', rows: 5 }
        ],
        其他: [
          { key: 'procurementNeed', label: '采购需求', required: true, upload: true, uploadLabel: '上传采购需求', placeholder: '※ 采购需求', rows: 4 },
          { key: 'otherAttachment', label: '其他附件', upload: true, uploadLabel: '上传附件', placeholder: '※ 其他附件：如技术规范等', rows: 5 }
        ]
      },
      directoryModes: [
        { label: '评分项', value: 'score', icon: 'el-icon-document-checked' },
        { label: '定制章', value: 'custom', icon: 'el-icon-document' },
        { label: '按采购需求生成', value: 'requirement', icon: 'el-icon-document-copy' }
      ],
      directoryTipMap: {
        score: [
          '评分标准：方案目录撰写的依据，用于约束方案目录的撰写方向和内容。',
          '建议：粘贴或填写招标文件中的评分标准表。',
          '温馨提示：如果您不希望在方案中体现商务部分的内容，请手动过滤评分标准中的商务评分相关项。'
        ],
        custom: [
          '定制章：可按项目特点输入希望生成的章标题。',
          '建议：一行一个章节方向，系统将围绕章节方向补齐二级、三级目录。',
          '温馨提示：定制章适合已有目录结构但需要AI扩写细化的场景。'
        ],
        requirement: [
          '按采购需求生成：系统将优先读取采购需求、项目概况、货物信息等内容。',
          '建议：先补充左侧基础信息，再生成目录。',
          '温馨提示：采购需求越完整，目录覆盖面越稳定。'
        ]
      },
      categorySamples: {
        服务: {
          title: '高等学校教学资源管理、推荐和分析智慧平台开发',
          procurementNeed: '采购要求如下：\n一、项目解读\n（一）通过整合教学资源，提供个性化推荐、分析学生行为和数据，以及支持决策分析，建立一个高效、智能和个性化的教学资源管理、推荐和分析智慧平台。\n（二）平台包含主要的子系统：统一用户管理、权限管理应用、基础信息管理、资源管理、接口管理、资源智能推荐管理系统、高等教育工作情况可视化系统、教师教研管理系统和基础服务。',
          otherAttachment: '※ 其他附件：如技术规范等'
        },
        货物: {
          procurementNeed: '采购需求',
          goodsInfo: '※ 方案要求：方案内容撰写的依据，用于约束方案内容的撰写方向，并为内容的细节生成提供线索和背景信息\n※ 建议：粘贴或填写招标文件中与方案相关的货物名称、规格参数、数量、供货周期、验收标准等。',
          otherAttachment: '※ 其他附件：如技术规范等'
        },
        工程: {
          engineeringOverview: '※ 项目概况',
          engineeringList: '※ 方案要求：方案内容撰写的依据，用于约束方案内容的撰写方向，并为内容的细节生成提供线索和背景信息\n※ 建议：粘贴或填写招标文件中与方案相关的施工范围、工期要求、质量目标、安全文明施工要求等。',
          drawingParse: '※ 图纸解析内容'
        },
        监理: {
          supervisionOverview: '※ 项目概况',
          engineeringOverview: '※ 项目概况',
          drawingParse: '※ 图纸解析内容'
        },
        IT信息: {
          projectOverview: '※ 采购需求',
          technicalRequirement: '※ 其他附件：如技术规范等',
          equipmentList: '※ 设备清单',
          otherAttachment: '※ 其他附件：如技术规范等'
        },
        其他: {
          procurementNeed: '※ 采购需求',
          otherAttachment: '※ 其他附件：如技术规范等'
        }
      },
      rules: {
        title: [{ required: true, message: '请填写方案名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择方案类型', trigger: 'change' }],
        file: [{ required: true, message: '请上传招标文件', trigger: 'change' }]
      }
    }
  },
  computed: {
    outlineRows() {
      return this.flattenOutlines(this.outlineTree)
    },
    activeFieldSections() {
      return this.fieldSections[this.form.category] || this.fieldSections.其他
    },
    activeDirectoryTips() {
      return this.directoryTipMap[this.form.directoryMode] || []
    },
    showTenderProcurementAction() {
      return !!this.form.file && (
        this.parsingMaterial ||
        this.parseReady ||
        this.extractingTenderProcurement ||
        !!this.form.fields.procurementNeed
      )
    },
    showDirectoryExtractActions() {
      return !!this.form.file && (
        this.parsingMaterial ||
        this.parseReady ||
        this.extractingFullScore ||
        this.extractingTechnicalScore ||
        !!this.form.scoreItemsText
      )
    },
    planCategoryText() {
      return this.form.category + '-' + (this.form.subcategory || '不限')
    },
    techDeviationRowText() {
      if (!this.form.techDeviation) return ''
      return this.formatTechDeviationTableText(this.form.techDeviationRows)
    }
  },
  watch: {
    '$route.query.bidId'(value) {
      if (value) {
        this.loadExistingPlan(value)
      } else if (this.bidId) {
        this.resetCreate(false)
      }
    }
  },
  created() {
    this.loadPlans()
    if (this.$route.query.bidId) {
      this.loadExistingPlan(this.$route.query.bidId)
    }
  },
  methods: {
    loadPlans() {
      return listBids({ pageNum: 1, pageSize: 100, title: this.planQuery || undefined }).then(res => {
        this.planList = res.rows || []
      })
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
          this.$set(row, 'title', title)
          if (String(row.id) === String(this.bidId)) {
            this.form.title = title
          }
          this.$modal.msgSuccess('标题已更新')
          this.loadPlans()
        })
      }).catch(() => {})
    },
    removePlan(row) {
      this.$modal.confirm('确认删除方案"' + row.title + '"吗？').then(() => deleteBid(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (String(row.id) === String(this.bidId)) {
          this.resetCreate()
        }
        this.loadPlans()
      }).catch(() => {})
    },
    resetCreate(updateRoute = true) {
      this.bidId = undefined
      this.tenderFileId = undefined
      this.tenderReportId = undefined
      this.promptReportId = undefined
      this.outlineTree = []
      this.tenderFileList = []
      this.promptFileList = []
      this.fieldFileLists = {}
      this.fieldFileIds = {}
      this.fieldParsing = {}
      this.techDeviationDialogVisible = false
      this.scoreCompareVisible = false
      this.parsingMaterial = false
      this.extractingTenderProcurement = false
      this.extractingFullScore = false
      this.extractingTechnicalScore = false
      this.extractingRequirementScore = false
      this.parseBuffers = {}
      this.selectedDeviationTemplate = 'template1'
      this.deviationDraftRows = [createEmptyDeviationRow()]
      this.subcategoryMap = {
        服务: '不限',
        货物: '不限',
        工程: '不限',
        监理: '不限',
        IT信息: '不限',
        其他: '不限'
      }
      this.collapsedSections = {}
      this.parseReady = false
      this.activeStep = 1
      this.form = {
        title: '',
        category: '服务',
        subcategory: '不限',
        aiEdition: 'basic',
        file: null,
        promptFile: null,
        writeStyle: '通用型',
        targetWords: 12000,
        techDeviation: false,
        techDeviationTemplateName: '',
        techDeviationRows: [],
        projectSixTables: false,
        useKnowledge: true,
        directoryMode: 'score',
        scoreItemsText: '',
        tenderScoreItems: '',
        requirementScoreItems: '',
        fullScoreItems: '',
        technicalScoreItems: '',
        useTechnicalScore: false,
        scoreSource: 'tender',
        customChapters: [createCustomChapter()],
        fields: createEmptyFields()
      }
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      if (updateRoute && this.$route.query.bidId) {
        this.$router.replace({ path: '/bid/plan/create' }).catch(() => {})
      }
    },
    loadExistingPlan(bidId) {
      if (!bidId) return Promise.resolve()
      this.resetCreate(false)
      this.bidId = bidId
      return Promise.all([
        getBids(bidId),
        listPlanMaterials(bidId).catch(() => ({ data: [] })),
        getLatestPlanReport(bidId).catch(() => ({ data: null })),
        getOutlineTree(bidId).catch(() => ({ data: [] }))
      ]).then(([bidRes, filesRes, reportRes, outlineRes]) => {
        this.applyBidInfo(bidRes.data || {})
        this.applyMaterialFiles(filesRes.data || [])
        this.applyParseReport(reportRes.data)
        this.outlineTree = outlineRes.data || []
        if (this.outlineRows.length) {
          this.activeStep = Math.max(this.activeStep, 3)
        } else if (this.parseReady || this.tenderReportId) {
          this.activeStep = Math.max(this.activeStep, 2)
        }
        this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      })
    },
    applyBidInfo(bid) {
      if (!bid || !bid.id) return
      this.bidId = bid.id
      this.form.title = bid.title || ''
      const category = this.parsePlanCategory(bid.category)
      this.form.category = category.category
      this.form.subcategory = category.subcategory
      this.$set(this.subcategoryMap, category.category, category.subcategory)
      this.applyPlanNote(bid.note)
    },
    parsePlanCategory(value) {
      const text = value || '服务-不限'
      const categories = this.categoryOptions.map(item => item.value)
      const category = categories.find(item => text.indexOf(item) === 0) || '服务'
      const rest = text.replace(category, '').replace(/^[-—\s]+/, '')
      const options = (this.categoryOptions.find(item => item.value === category) || {}).options || ['不限']
      return {
        category,
        subcategory: options.indexOf(rest) >= 0 ? rest : '不限'
      }
    },
    applyPlanNote(note) {
      const text = note || ''
      const valueOf = label => {
        const match = text.match(new RegExp(label + '：([^；]+)'))
        return match ? match[1].trim() : ''
      }
      const aiEdition = valueOf('AI版本')
      if (aiEdition) this.form.aiEdition = aiEdition
      const writeStyle = valueOf('写作风格')
      if (writeStyle) this.form.writeStyle = writeStyle
      const targetWords = Number(valueOf('目标字数'))
      if (targetWords) this.form.targetWords = targetWords
      const techDeviation = valueOf('技术偏离表')
      if (techDeviation) this.form.techDeviation = techDeviation === '启用'
      const projectSixTables = valueOf('工程六表')
      if (projectSixTables) this.form.projectSixTables = projectSixTables === '启用'
      const useKnowledge = valueOf('知识库增强')
      if (useKnowledge) this.form.useKnowledge = useKnowledge === '启用'
    },
    applyMaterialFiles(files) {
      const fileList = files || []
      const mainFile = fileList.find(file => file.fileStage === 'outline_source') || fileList[0]
      if (mainFile) {
        this.tenderFileId = mainFile.id
        this.form.file = { name: mainFile.originalName || mainFile.fileName }
        this.tenderFileList = [this.toUploadListItem(mainFile)]
      }
      fileList.forEach(file => {
        if (!file || !file.fileStage || file === mainFile) return
        if (file.fileStage === 'content_prompt') {
          this.promptFileList = [this.toUploadListItem(file)]
          this.promptReportId = file.parseReportId || this.promptReportId
          return
        }
        const sectionKey = this.sectionKeyFromStage(file.fileStage)
        if (sectionKey) {
          this.$set(this.fieldFileLists, sectionKey, [this.toUploadListItem(file)])
          this.$set(this.fieldFileIds, sectionKey, file.id)
        }
      })
    },
    toUploadListItem(file) {
      return {
        uid: 'saved-' + file.id,
        id: file.id,
        name: file.originalName || file.fileName || '已上传文件',
        status: 'success',
        url: file.fileUrl
      }
    },
    sectionKeyFromStage(stage) {
      const map = {
        purchase_requirement: 'procurementNeed',
        goods_info: 'goodsInfo',
        engineering_overview: 'engineeringOverview',
        engineering_list: 'engineeringList',
        drawing_parse: 'drawingParse',
        supervision_overview: 'supervisionOverview',
        project_overview: 'projectOverview',
        technical_requirement: 'technicalRequirement',
        equipment_list: 'equipmentList',
        other_attachment: 'otherAttachment'
      }
      return map[stage]
    },
    applyParseReport(report) {
      if (!report || !report.id) return
      this.tenderReportId = report.id
      this.tenderFileId = report.tenderFileId || this.tenderFileId
      if (report.projectName && !this.form.title) {
        this.form.title = report.projectName
      }
      if (report.requirementSummary) {
        this.$set(this.form.fields, 'procurementNeed', report.requirementSummary)
      }
      if (report.scoreItems) {
        this.form.tenderScoreItems = report.scoreItems
        this.form.fullScoreItems = report.scoreItems
        this.form.scoreItemsText = report.scoreItems
      }
      const content = this.parseReportContent(report.reportContent)
      if (content.schemeType) {
        const category = this.parsePlanCategory(content.schemeType)
        this.form.category = category.category
        this.form.subcategory = category.subcategory
        this.$set(this.subcategoryMap, category.category, category.subcategory)
      }
      if (content.projectName && !this.form.title) {
        this.form.title = content.projectName
      }
      if (content.extractedFields) {
        Object.keys(content.extractedFields).forEach(key => {
          this.$set(this.form.fields, key, content.extractedFields[key] || '')
        })
      }
      if (content.purchaseRequirement) {
        this.$set(this.form.fields, 'procurementNeed', content.purchaseRequirement)
      }
      if (content.otherAttachment) {
        this.$set(this.form.fields, 'otherAttachment', content.otherAttachment)
      }
      if (content.fullScoreItems) {
        this.form.tenderScoreItems = content.fullScoreItems
        this.form.fullScoreItems = content.fullScoreItems
        this.form.scoreItemsText = content.fullScoreItems
      }
      if (content.technicalScoreItems) {
        this.form.technicalScoreItems = content.technicalScoreItems
      }
      this.parseReady = true
    },
    parseReportContent(value) {
      if (!value) return {}
      if (typeof value === 'object') return value
      try {
        return JSON.parse(value)
      } catch (e) {
        return {}
      }
    },
    clearMaterialAndParsedContent(clearTitle) {
      this.bidId = undefined
      this.tenderFileId = undefined
      this.tenderReportId = undefined
      this.promptReportId = undefined
      this.tenderFileList = []
      this.promptFileList = []
      this.fieldFileLists = {}
      this.fieldFileIds = {}
      this.fieldParsing = {}
      this.form.file = null
      this.form.promptFile = null
      this.outlineTree = []
      this.clearParsedContent(clearTitle)
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    clearParsedContent(clearTitle) {
      if (clearTitle) {
        this.form.title = ''
      }
      this.form.fields = createEmptyFields()
      this.form.scoreItemsText = ''
      this.form.tenderScoreItems = ''
      this.form.requirementScoreItems = ''
      this.form.fullScoreItems = ''
      this.form.technicalScoreItems = ''
      this.form.useTechnicalScore = false
      this.form.scoreSource = 'tender'
      this.form.directoryMode = 'score'
      this.form.customChapters = [createCustomChapter()]
      this.parseBuffers = {}
      this.parseReady = false
      this.parsingMaterial = false
      this.extractingTenderProcurement = false
      this.extractingFullScore = false
      this.extractingTechnicalScore = false
      this.extractingRequirementScore = false
      this.scoreCompareVisible = false
      this.activeStep = 1
    },
    selectCategory(category) {
      if (this.form.category !== category) {
        this.form.category = category
        this.form.subcategory = this.subcategoryMap[category] || '不限'
        this.clearMaterialAndParsedContent(true)
      } else {
        this.form.subcategory = this.subcategoryMap[category] || '不限'
      }
    },
    handleCategoryCommand(command) {
      const categoryChanged = this.form.category !== command.category
      this.form.category = command.category
      this.form.subcategory = command.subcategory
      this.$set(this.subcategoryMap, command.category, command.subcategory)
      if (categoryChanged) {
        this.clearMaterialAndParsedContent(true)
      }
      this.$nextTick(() => this.$refs.form && this.$refs.form.validateField('category'))
    },
    categoryButtonText(item) {
      if (!item.options || item.options.length <= 1) return item.label
      return item.label + '-' + (this.subcategoryMap[item.value] || '不限')
    },
    selectAiEdition(item) {
      if (item.disabled) return
      this.form.aiEdition = item.value
    },
    isPrimaryRequirementSection(section) {
      return section && (section.key === 'procurementNeed' || section.key === 'technicalRequirement')
    },
    isSectionUploadDisabled(section) {
      if (!section) return false
      if (this.fieldParsing[section.key]) return true
      if (this.isPrimaryRequirementSection(section)) {
        return !this.parseReady || this.extractingTenderProcurement
      }
      return false
    },
    handleDirectoryModeClick(item) {
      this.form.directoryMode = item.value
      if (item.value === 'requirement') {
        this.extractScoreItemsFromRequirement()
      }
    },
    handleTechDeviationToggle(value) {
      if (value) {
        if (!this.deviationDraftRows.length) {
          this.deviationDraftRows = [createEmptyDeviationRow()]
        }
        return
      }
      this.form.techDeviationTemplateName = ''
      this.form.techDeviationRows = []
      this.deviationDraftRows = [createEmptyDeviationRow()]
      this.techDeviationDialogVisible = false
    },
    openTechDeviationDialog() {
      if (!this.form.techDeviation) {
        this.form.techDeviation = true
      }
      this.deviationDraftRows = this.cloneDeviationRows(this.form.techDeviationRows)
      this.techDeviationDialogVisible = true
    },
    confirmTechDeviationTemplate() {
      this.form.techDeviationTemplateName = '技术偏离表模板1'
      this.form.techDeviationRows = this.cloneDeviationRows(this.deviationDraftRows)
      this.techDeviationDialogVisible = false
    },
    cloneDeviationRows(rows) {
      const source = rows && rows.length ? rows : [createEmptyDeviationRow()]
      return source.map(row => Object.assign(createEmptyDeviationRow(), row))
    },
    formatTechDeviationTableText(rows) {
      const contentRows = (rows || []).filter(row => {
        return this.deviationColumns.some(column => row[column.prop])
      })
      if (!contentRows.length) return ''
      const header = this.deviationColumns.map(column => column.label).join(',')
      const body = contentRows.map(row => {
        return this.deviationColumns.map(column => row[column.prop] || '').join(',')
      })
      return [header].concat(body).join('\n')
    },
    handleTenderChange(file, fileList) {
      if (!this.validateFile(file, ['doc', 'docx', 'pdf'], 50)) {
        this.tenderFileList = []
        this.form.file = null
        return
      }
      this.tenderFileList = fileList.slice(-1)
      this.form.file = file.raw
      this.bidId = undefined
      this.tenderFileId = undefined
      this.tenderReportId = undefined
      this.parseReady = false
      this.clearParsedContent(false)
      if (!this.form.title && file.name) {
        this.form.title = file.name.replace(/\.[^.]+$/, '')
      }
      this.activeStep = Math.max(this.activeStep, 2)
      this.$nextTick(() => this.$refs.form && this.$refs.form.validateField('file'))
    },
    handleTenderRemove() {
      this.tenderFileList = []
      this.form.file = null
      this.parseReady = false
      this.tenderFileId = undefined
      this.tenderReportId = undefined
      this.clearParsedContent(true)
    },
    startParsePreview() {
      if (!this.form.file) {
        this.$modal.msgWarning('请先上传招标文件')
        return
      }
      this.parsingMaterial = true
      this.ensurePlanUploadedForParse()
        .then(() => this.runServiceParseStream())
        .then(() => {
          this.parseReady = true
          this.activeStep = Math.max(this.activeStep, 2)
          this.$modal.msgSuccess('招标文件解析完成')
          this.loadPlans()
        })
        .catch(error => {
          this.$modal.msgError((error && error.message) || '招标文件解析失败')
        })
        .finally(() => {
          this.parsingMaterial = false
          this.extractingTenderProcurement = false
          this.extractingFullScore = false
          this.extractingTechnicalScore = false
        })
    },
    ensurePlanUploadedForParse() {
      if (this.bidId && this.tenderFileId) {
        return Promise.resolve()
      }
      const title = this.form.title || (this.tenderFileList[0] && this.tenderFileList[0].name.replace(/\.[^.]+$/, '')) || 'AI方案解析草稿'
      return createPlanWithMaterial({
        title,
        category: this.planCategoryText,
        note: this.buildPlanNote(),
        file: this.form.file
      }).then(res => {
        const data = res.data || {}
        const bid = data.bid || {}
        const file = data.file || {}
        this.bidId = bid.id
        this.tenderFileId = file.id
        if (bid.title && !this.form.title) {
          this.form.title = bid.title
        }
      })
    },
    uploadSectionFileIfNeeded(sectionKey, stage) {
      if (this.fieldFileIds[sectionKey]) {
        return Promise.resolve(this.fieldFileIds[sectionKey])
      }
      const list = this.fieldFileLists[sectionKey] || []
      const raw = list[0] && (list[0].raw || list[0])
      if (!raw || !this.bidId) {
        return Promise.resolve(undefined)
      }
      return uploadPlanMaterial(this.bidId, raw, stage).then(res => {
        const file = res.data || {}
        if (file.id) {
          this.$set(this.fieldFileIds, sectionKey, file.id)
        }
        return file.id
      })
    },
    runServiceParseStream() {
      this.activeFieldSections.forEach(section => {
        this.form.fields[section.key] = ''
      })
      this.form.scoreItemsText = ''
      this.form.tenderScoreItems = ''
      this.form.requirementScoreItems = ''
      this.form.fullScoreItems = ''
      this.form.technicalScoreItems = ''
      this.form.useTechnicalScore = false
      this.form.scoreSource = 'tender'
      this.extractingTenderProcurement = true
      this.extractingFullScore = true
      this.extractingTechnicalScore = true
      this.parseBuffers = { projectName: '', scoreItems: '', technicalScoreItems: '' }
      this.activeFieldSections.forEach(section => {
        this.$set(this.parseBuffers, section.key, '')
      })
      return streamPost('/tdw/tender/parse/service/stream', {
        tenderFileId: this.tenderFileId,
        category: this.planCategoryText
      }, {
        onEvent: (eventName, payload) => this.handleServiceParseEvent(eventName, payload),
        onError: payload => {
          throw new Error(payload || '方案解析失败')
        }
      })
    },
    handleServiceParseEvent(eventName, payload) {
      if (eventName === 'done') {
        const result = payload || {}
        this.tenderReportId = result.reportId || this.tenderReportId
        this.bidId = result.bidId || this.bidId
        this.tenderFileId = result.tenderFileId || this.tenderFileId
        if (result.projectName) this.form.title = result.projectName
        if (result.extractedFields) {
          Object.keys(result.extractedFields).forEach(key => {
            this.form.fields[key] = result.extractedFields[key]
          })
        } else if (result.purchaseRequirement) {
          this.form.fields.procurementNeed = result.purchaseRequirement
        }
        if (result.fullScoreItems) {
          this.form.tenderScoreItems = result.fullScoreItems
          this.form.fullScoreItems = result.fullScoreItems
          if (!this.form.useTechnicalScore) this.form.scoreItemsText = result.fullScoreItems
        }
        if (result.technicalScoreItems) this.form.technicalScoreItems = result.technicalScoreItems
        this.extractingTenderProcurement = false
        this.extractingFullScore = false
        this.extractingTechnicalScore = false
        return
      }
      const chunk = typeof payload === 'string' ? payload : ''
      const fieldKey = this.parseEventFieldKey(eventName)
      if (!Object.prototype.hasOwnProperty.call(this.parseBuffers, fieldKey)) return
      this.parseBuffers[fieldKey] += chunk
      if (fieldKey === 'projectName') {
        this.form.title = this.parseBuffers.projectName
      } else if (this.form.fields && Object.prototype.hasOwnProperty.call(this.form.fields, fieldKey)) {
        this.form.fields[fieldKey] = this.parseBuffers[fieldKey]
      } else if (fieldKey === 'scoreItems') {
        this.extractingTenderProcurement = false
        this.parseReady = true
        this.form.tenderScoreItems = this.parseBuffers.scoreItems
        this.form.fullScoreItems = this.parseBuffers.scoreItems
        if (!this.form.useTechnicalScore) {
          this.form.scoreItemsText = this.parseBuffers.scoreItems
        }
      } else if (fieldKey === 'technicalScoreItems') {
        this.extractingFullScore = false
        this.form.technicalScoreItems = this.parseBuffers.technicalScoreItems
      }
    },
    parseEventFieldKey(eventName) {
      return eventName === 'purchaseRequirement' ? 'procurementNeed' : eventName
    },
    extractFieldFromTender(section) {
      if (!this.form.file) {
        this.$modal.msgWarning('请先上传招标文件')
        return Promise.resolve()
      }
      const fieldKey = section && section.key ? section.key : 'procurementNeed'
      this.extractingTenderProcurement = true
      this.form.fields[fieldKey] = ''
      let buffer = ''
      return this.ensurePlanUploadedForParse()
        .then(() => streamPost('/tdw/tender/parse/service/field/tender/stream', {
          fileId: this.tenderFileId,
          category: this.planCategoryText,
          fieldKey
        }, {
          onEvent: (eventName, payload) => {
            if (this.parseEventFieldKey(eventName) === fieldKey) {
              buffer += typeof payload === 'string' ? payload : ''
              this.form.fields[fieldKey] = buffer
            } else if (eventName === 'done' && !this.form.fields[fieldKey]) {
              this.form.fields[fieldKey] = typeof payload === 'string' ? payload : ''
            }
          },
          onError: payload => {
            throw new Error(payload || (section.label + '提取失败'))
          }
        }))
        .catch(error => {
          this.$modal.msgError((error && error.message) || (section.label + '提取失败'))
        })
        .finally(() => {
          this.extractingTenderProcurement = false
          this.parseReady = true
        })
    },
    extractScoreItemsFromTender() {
      if (!this.form.file) {
        this.$modal.msgWarning('请先上传招标文件')
        return Promise.resolve()
      }
      this.form.directoryMode = 'score'
      this.extractingFullScore = true
      this.form.scoreItemsText = ''
      this.form.tenderScoreItems = ''
      this.form.fullScoreItems = ''
      this.form.technicalScoreItems = ''
      this.form.useTechnicalScore = false
      this.form.scoreSource = 'tender'
      let buffer = ''
      return this.ensurePlanUploadedForParse()
        .then(() => streamPost('/tdw/tender/parse/service/score-items/tender/stream', {
          fileId: this.tenderFileId,
          category: this.planCategoryText
        }, {
          onEvent: (eventName, payload) => {
            if (eventName === 'scoreItems') {
              buffer += typeof payload === 'string' ? payload : ''
              this.form.scoreItemsText = buffer
              this.form.tenderScoreItems = buffer
              this.form.fullScoreItems = buffer
            } else if (eventName === 'done' && !this.form.scoreItemsText) {
              const text = typeof payload === 'string' ? payload : ''
              this.form.scoreItemsText = text
              this.form.tenderScoreItems = text
              this.form.fullScoreItems = text
            }
          },
          onError: payload => {
            throw new Error(payload || '完整评分项提取失败')
          }
        }))
        .catch(error => {
          this.$modal.msgError((error && error.message) || '完整评分项提取失败')
        })
        .finally(() => {
          this.extractingFullScore = false
        })
    },
    refreshUploadedFieldParses() {
      const tasks = []
      this.activeFieldSections.forEach(section => {
        if ((this.fieldFileLists[section.key] || []).length) {
          tasks.push(() => this.runServiceUploadedFieldParse(section.key))
        }
      })
      return tasks.reduce((promise, task) => promise.then(task), Promise.resolve())
    },
    runServiceUploadedFieldParse(sectionKey) {
      const section = this.activeFieldSections.find(item => item.key === sectionKey)
      if (!section) {
        return Promise.resolve()
      }
      if (!this.form.file) {
        this.$modal.msgWarning('请先上传招标文件')
        return Promise.resolve()
      }
      this.$set(this.fieldParsing, sectionKey, true)
      this.form.fields[sectionKey] = ''
      let buffer = ''
      return this.ensurePlanUploadedForParse()
        .then(() => this.uploadSectionFileIfNeeded(sectionKey, this.fieldStage(sectionKey)))
        .then(fileId => {
          if (!fileId) {
            this.$modal.msgWarning('请先选择' + section.label + '文件')
            return undefined
          }
          return streamPost('/tdw/tender/parse/service/field/upload/stream', {
            fileId,
            category: this.planCategoryText,
            fieldKey: sectionKey
          }, {
            onEvent: (eventName, payload) => {
              if (this.parseEventFieldKey(eventName) === sectionKey) {
                buffer += typeof payload === 'string' ? payload : ''
                this.form.fields[sectionKey] = buffer
              } else if (eventName === 'done' && !this.form.fields[sectionKey]) {
                this.form.fields[sectionKey] = typeof payload === 'string' ? payload : ''
              }
            },
            onError: payload => {
              throw new Error(payload || '文件内容抽取失败')
            }
          })
        })
        .catch(error => {
          this.$modal.msgError((error && error.message) || '文件内容抽取失败')
        })
        .finally(() => {
          this.$set(this.fieldParsing, sectionKey, false)
        })
    },
    fieldStage(sectionKey) {
      const stageMap = {
        procurementNeed: 'purchase_requirement',
        goodsInfo: 'goods_info',
        engineeringOverview: 'engineering_overview',
        engineeringList: 'engineering_list',
        drawingParse: 'drawing_parse',
        supervisionOverview: 'supervision_overview',
        projectOverview: 'project_overview',
        technicalRequirement: 'technical_requirement',
        equipmentList: 'equipment_list',
        otherAttachment: 'other_attachment'
      }
      return stageMap[sectionKey] || sectionKey
    },
    extractScoreItemsFromRequirement() {
      const purchaseRequirement = this.buildRequirementForScoreExtraction()
      if (!purchaseRequirement) {
        this.$modal.msgWarning('请先完成基础内容抽取或手动填写需求内容')
        return Promise.resolve()
      }
      this.extractingRequirementScore = true
      this.form.scoreItemsText = ''
      this.form.requirementScoreItems = ''
      this.form.fullScoreItems = ''
      this.form.technicalScoreItems = ''
      this.form.useTechnicalScore = false
      this.form.scoreSource = 'requirement'
      let buffer = ''
      return streamPost('/tdw/tender/parse/service/score-items/requirement/stream', {
        content: purchaseRequirement,
        category: this.planCategoryText
      }, {
        onEvent: (eventName, payload) => {
          if (eventName === 'scoreItems') {
            buffer += typeof payload === 'string' ? payload : ''
            this.form.scoreItemsText = buffer
            this.form.requirementScoreItems = buffer
            this.form.fullScoreItems = buffer
          } else if (eventName === 'done' && !this.form.scoreItemsText) {
            const text = typeof payload === 'string' ? payload : ''
            this.form.scoreItemsText = text
            this.form.requirementScoreItems = text
            this.form.fullScoreItems = text
          }
        },
        onError: payload => {
          throw new Error(payload || '按采购需求生成评分项失败')
        }
      }).catch(error => {
        this.$modal.msgError((error && error.message) || '按采购需求生成评分项失败')
      }).finally(() => {
        this.extractingRequirementScore = false
      })
    },
    buildRequirementForScoreExtraction() {
      const primaryKeys = ['procurementNeed', 'technicalRequirement', 'projectOverview', 'engineeringOverview', 'supervisionOverview', 'goodsInfo', 'engineeringList', 'equipmentList', 'drawingParse']
      const parts = []
      primaryKeys.forEach(key => {
        const section = this.activeFieldSections.find(item => item.key === key)
        const value = this.form.fields[key]
        if (section && value) {
          parts.push(section.label + '：\n' + value)
        }
      })
      this.activeFieldSections.forEach(section => {
        if (primaryKeys.indexOf(section.key) === -1 && this.form.fields[section.key]) {
          parts.push(section.label + '：\n' + this.form.fields[section.key])
        }
      })
      return parts.join('\n\n')
    },
    extractTechnicalScoreItems() {
      const source = this.form.fullScoreItems || this.form.scoreItemsText
      if (!source) {
        this.$modal.msgWarning('请先解析或填写完整评分标准')
        return
      }
      this.extractingTechnicalScore = true
      this.form.technicalScoreItems = ''
      let buffer = ''
      streamPost('/tdw/tender/parse/service/technical-score/stream', {
        fullScoreItems: source,
        category: this.planCategoryText
      }, {
        onEvent: (eventName, payload) => {
          if (eventName === 'technicalScoreItems') {
            buffer += typeof payload === 'string' ? payload : ''
            this.form.technicalScoreItems = buffer
          } else if (eventName === 'done' && !this.form.technicalScoreItems) {
            this.form.technicalScoreItems = typeof payload === 'string' ? payload : ''
          }
        },
        onError: payload => {
          throw new Error(payload || '技术评分项提取失败')
        }
      }).then(() => {
        this.form.fullScoreItems = source
        this.scoreCompareVisible = true
      }).catch(error => {
        this.$modal.msgError((error && error.message) || '技术评分项提取失败')
      }).finally(() => {
        this.extractingTechnicalScore = false
      })
    },
    openScoreCompareIfReady() {
      if (this.form.fullScoreItems && this.form.technicalScoreItems) {
        this.scoreCompareVisible = true
      }
    },
    applyTechnicalScoreItems() {
      this.form.scoreItemsText = this.form.technicalScoreItems
      this.form.useTechnicalScore = true
      this.scoreCompareVisible = false
    },
    useFullScoreItems() {
      if (this.form.fullScoreItems) {
        this.form.scoreItemsText = this.form.fullScoreItems
      }
      this.form.useTechnicalScore = false
      this.scoreCompareVisible = false
    },
    addCustomChapter() {
      this.form.customChapters.push(createCustomChapter())
    },
    removeCustomChapter(index) {
      if (this.form.customChapters.length <= 1) return
      this.form.customChapters.splice(index, 1)
    },
    applyCategorySample(category) {
      const sample = this.categorySamples[category] || {}
      if (sample.title && !this.form.title) {
        this.form.title = sample.title
      }
      Object.keys(sample).forEach(key => {
        if (key !== 'title' && !this.form.fields[key]) {
          this.form.fields[key] = sample[key]
        }
      })
    },
    handlePromptChange(file, fileList) {
      if (!this.validateFile(file, ['doc', 'docx'], 50)) {
        this.promptFileList = []
        this.form.promptFile = null
        return
      }
      this.promptFileList = fileList.slice(-1)
      this.form.promptFile = file.raw
    },
    handlePromptRemove() {
      this.promptFileList = []
      this.form.promptFile = null
      this.promptReportId = undefined
    },
    handleSectionFileChange(section, file, fileList) {
      const extensions = section.extensions || ['doc', 'docx', 'pdf']
      const maxMb = section.maxMb || 50
      if (!this.validateFile(file, extensions, maxMb)) {
        this.$set(this.fieldFileLists, section.key, [])
        this.$delete(this.fieldFileIds, section.key)
        return
      }
      this.$set(this.fieldFileLists, section.key, fileList.slice(-(section.limit || 1)))
      this.$delete(this.fieldFileIds, section.key)
      this.runServiceUploadedFieldParse(section.key)
    },
    handleSectionExceed() {
      this.$modal.msgWarning('已达到本项允许上传的文件数量')
    },
    toggleSection(key) {
      this.$set(this.collapsedSections, key, !this.collapsedSections[key])
    },
    validateFile(file, extensions, maxMb) {
      const raw = file.raw || file
      const name = file.name || raw.name || ''
      const ext = name.split('.').pop().toLowerCase()
      if (extensions.indexOf(ext) === -1) {
        this.$modal.msgWarning('仅支持 ' + extensions.join('、') + ' 格式')
        return false
      }
      if (raw.size > maxMb * 1024 * 1024) {
        this.$modal.msgWarning('单个文件大小不能超过 ' + maxMb + 'MB')
        return false
      }
      return true
    },
    submitGenerateOutline() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.generatingOutline = true
        this.ensurePlanUploadedForParse().then(() => {
          this.activeStep = 3
          if (this.tenderReportId) {
            return this.tenderReportId
          }
          if (!this.tenderFileId) {
            return undefined
          }
          return parsePlanMaterial(this.tenderFileId).then(parseRes => {
            const report = parseRes.data || {}
            this.tenderReportId = report.id
            return report.id
          })
        }).then(reportId => {
          return generateOutline({
            bidId: this.bidId,
            mode: 'overwrite',
            tenderParseReportId: reportId,
            requirement: this.buildOutlineRequirement()
          })
        }).then(() => {
          if (!this.bidId) return
          return this.loadOutline()
        }).then(() => {
          this.$modal.msgSuccess('目录生成完成')
          this.wordDialogVisible = true
          this.activeStep = 4
          this.loadPlans()
        }).finally(() => {
          this.generatingOutline = false
        })
      })
    },
    submitGenerateContent() {
      if (!this.bidId) {
        this.$modal.msgWarning('请先生成目录')
        return
      }
      this.generatingContent = true
      this.uploadPromptIfNeeded().then(reportId => {
        this.activeStep = 5
        return generateContentBlocks({
          bidId: this.bidId,
          scope: 'full',
          mode: 'overwrite',
          requirement: this.buildContentRequirement(),
          includeTable: this.generateOptions.includeTable,
          includeDiagram: this.generateOptions.includeDiagram,
          tenderParseReportId: reportId || this.tenderReportId
        })
      }).then(() => {
        this.$modal.msgSuccess('方案生成完成')
        this.$router.replace({ path: '/bid/plan/editor', query: { bidId: this.bidId, reportId: this.promptReportId || this.tenderReportId } })
      }).finally(() => {
        this.generatingContent = false
      })
    },
    confirmWordPreset(data) {
      if (!this.bidId) {
        this.$modal.msgWarning('请先生成目录')
        return
      }
      this.wordSubmitting = true
      applyPlanWordPreset(this.bidId, data).then(() => {
        this.wordDialogVisible = false
        this.$router.replace({ path: '/bid/plan/outline', query: { bidId: this.bidId } })
      }).finally(() => {
        this.wordSubmitting = false
      })
    },
    uploadPromptIfNeeded() {
      if (this.promptReportId || !this.form.promptFile) {
        return Promise.resolve(this.promptReportId)
      }
      return uploadPlanMaterial(this.bidId, this.form.promptFile, 'content_prompt').then(res => {
        const file = res.data || {}
        if (!file.id) return undefined
        return parsePlanMaterial(file.id).then(parseRes => {
          const report = parseRes.data || {}
          this.promptReportId = report.id
          return report.id
        })
      })
    },
    loadOutline() {
      return getOutlineTree(this.bidId).then(res => {
        this.outlineTree = res.data || []
      })
    },
    buildPlanNote() {
      return [
        'AI方案',
        '方案类型：' + this.planCategoryText,
        'AI版本：' + this.form.aiEdition,
        '写作风格：' + this.form.writeStyle,
        '目标字数：' + this.form.targetWords,
        '技术偏离表：' + (this.form.techDeviation ? '启用' : '关闭'),
        '工程六表：' + (this.form.projectSixTables ? '启用' : '关闭'),
        '知识库增强：' + (this.form.useKnowledge ? '启用' : '关闭')
      ].filter(Boolean).join('；')
    },
    buildOutlineRequirement() {
      const parts = [
        '方案类型：' + this.planCategoryText,
        '写作风格：' + this.form.writeStyle,
        '目录生成方式：' + this.directoryModeText(),
        '请优先根据评分项生成章、节、内容标题三级目录。'
      ]
      const fieldText = this.buildFieldText()
      if (fieldText) parts.push(fieldText)
      const directoryText = this.form.directoryMode === 'custom' ? this.buildCustomChapterText() : this.buildScoreItemsText()
      if (directoryText) parts.push(directoryText)
      const deviationText = this.buildTechDeviationText()
      if (deviationText) parts.push(deviationText)
      return parts.join('\n')
    },
    buildContentRequirement() {
      const parts = [
        '按已生成三级目录逐节生成正文。',
        '方案类型：' + this.planCategoryText,
        '生成模式：' + (this.mode === 'accurate' ? '精准模式' : '丰富模式'),
        '目标字数：' + this.form.targetWords,
        this.generateOptions.includeGantt ? '需要在合适章节补充甘特图说明。' : '',
        this.generateOptions.quoteImages ? '允许结合私人图库图片生成图示说明。' : ''
      ].filter(Boolean)
      const fieldText = this.buildFieldText()
      if (fieldText) parts.push(fieldText)
      const directoryText = this.form.directoryMode === 'custom' ? this.buildCustomChapterText() : this.buildScoreItemsText()
      if (directoryText) parts.push(directoryText)
      const deviationText = this.buildTechDeviationText()
      if (deviationText) parts.push(deviationText)
      return parts.join('\n')
    },
    buildScoreItemsText() {
      return this.form.scoreItemsText ? '评分项：\n' + this.form.scoreItemsText : ''
    },
    buildCustomChapterText() {
      const chapters = (this.form.customChapters || []).map((chapter, index) => {
        const title = chapter.title && chapter.title.trim()
        const requirement = chapter.requirement && chapter.requirement.trim()
        if (!title && !requirement) return ''
        return [
          '第 ' + (index + 1) + ' 章：' + (title || '未命名章节'),
          requirement ? '节段要求：\n' + requirement : ''
        ].filter(Boolean).join('\n')
      }).filter(Boolean)
      return chapters.length ? '定制章：\n' + chapters.join('\n\n') : ''
    },
    buildTechDeviationText() {
      if (!this.form.techDeviation) return ''
      const tableText = this.formatTechDeviationTableText(this.form.techDeviationRows)
      return tableText ? '技术偏离表：\n' + tableText : '技术偏离表：启用'
    },
    buildFieldText() {
      return this.activeFieldSections.map(section => {
        const value = this.form.fields[section.key]
        return value ? section.label + '：\n' + value : ''
      }).filter(Boolean).join('\n')
    },
    directoryModeText() {
      const mode = this.directoryModes.find(item => item.value === this.form.directoryMode)
      return mode ? mode.label : '评分项'
    },
    flattenOutlines(nodes, rows = []) {
      ;(nodes || []).forEach(node => {
        rows.push(node)
        this.flattenOutlines(node.children || [], rows)
      })
      return rows
    },
    levelText(level) {
      return Number(level) === 1 ? '章' : Number(level) === 2 ? '节' : '目'
    },
    openEditor(row) {
      if (!row || !row.id) return
      if (String(row.id) === String(this.bidId)) return
      this.$router.push({ path: '/bid/plan/create', query: { bidId: row.id } }).catch(() => {})
    },
    openEditorWithOutline(node) {
      if (!this.bidId) return
      this.$router.push({ path: '/bid/plan/outline', query: { bidId: this.bidId, outlineId: node.id } })
    }
  }
}
</script>

<style scoped>
.plan-create-page {
  height: calc(100vh - 84px);
  min-height: 680px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  overflow: hidden;
  background: #f5f7fb;
}
.plan-sidebar {
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
  margin-top: 14px;
  border: 0;
  background: linear-gradient(135deg, #4b7bec, #31c6c7);
  flex: 0 0 auto;
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
  min-height: 70px;
  text-align: left;
  border: 1px solid #d9e1ef;
  border-radius: 6px;
  background: #fff;
  padding: 10px;
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
.plan-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.plan-name {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #2762d8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}
.plan-name i {
  flex: 0 0 auto;
}
.plan-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.plan-icon-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 6px;
  color: #fff;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}
.plan-icon-btn.edit {
  background: #ff6b6b;
}
.plan-icon-btn.delete {
  background: #8e959f;
}
.plan-icon-btn:hover {
  filter: brightness(.96);
}
.plan-meta {
  display: block;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.plan-end {
  padding: 10px 0 4px;
  text-align: center;
  color: #a8abb2;
  font-size: 12px;
}
.empty-side {
  height: 100%;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a8abb2;
  gap: 10px;
}
.empty-side i {
  font-size: 34px;
}
.create-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.step-header {
  height: 54px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 0 0 auto;
  padding: 0 16px;
  border-bottom: 1px solid #dfe4ee;
  background: #fff;
}
.steps {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(5, minmax(110px, 1fr));
  gap: 10px;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #b3b9c6;
  font-size: 13px;
  white-space: nowrap;
}
.step-item span {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #edf0f6;
  color: #fff;
  font-weight: 700;
}
.step-item.active {
  color: #4263eb;
}
.step-item.active span {
  background: #4263eb;
}
.workbench {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(520px, 46%) minmax(520px, 54%);
  overflow: hidden;
}
.config-panel,
.preview-panel {
  min-width: 0;
  background: #fff;
}
.config-panel {
  overflow-y: auto;
  padding: 12px 18px 32px;
  border-right: 1px solid #cfd6e3;
}
.preview-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}
.plan-form ::v-deep .el-form-item__label {
  position: relative;
  padding-bottom: 8px;
  font-weight: 700;
  color: #303133;
}
.required-form-item ::v-deep .el-form-item__label::before,
.read-form-item ::v-deep .el-form-item__label::before {
  content: "·";
  position: absolute;
  left: -10px;
  top: 0;
  color: #ff4d4f;
  font-size: 20px;
  line-height: 1;
}
.type-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(128px, 1fr));
  gap: 12px;
}
.type-button {
  width: 100%;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid #c7ceda;
  border-radius: 4px;
  background: #fff;
  color: #8a9099;
  cursor: pointer;
}
.type-button.selected {
  border-color: #4263eb;
  color: #3154d4;
  background: #f1f5ff;
}
.type-arrow {
  margin-left: auto;
  font-size: 12px;
}
.ai-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(110px, 1fr));
  gap: 10px;
}
.ai-card {
  min-height: 72px;
  position: relative;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  border: 1px solid #3972ff;
  border-radius: 5px;
  background: #eef5ff;
  padding: 8px 8px 24px;
  text-align: left;
  color: #303133;
  cursor: pointer;
}
.ai-card.selected {
  box-shadow: inset 0 0 0 1px #3972ff;
}
.edition-icon {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  background: #17c9e8;
  font-size: 18px;
}
.edition-icon.flagship {
  background: #f59f00;
}
.edition-icon.pro {
  background: #7950f2;
}
.edition-main strong,
.edition-main em {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.edition-main em {
  margin-top: 2px;
  color: #909399;
  font-style: normal;
  font-size: 12px;
}
.edition-tag {
  position: absolute;
  right: 8px;
  top: 30px;
  color: #ff6b35;
  font-size: 12px;
  font-weight: 700;
}
.ai-card small {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(66, 99, 235, 0.12);
  color: #7d8794;
  font-size: 12px;
}
.edition-warn {
  position: absolute;
  right: 7px;
  bottom: 2px;
  color: #ff4d4f;
  font-size: 12px;
  font-weight: 400;
}
.read-form-item {
  margin-top: 10px;
}
.read-form-item ::v-deep .el-upload,
.read-form-item ::v-deep .el-upload-dragger {
  width: 100%;
}
.read-form-item ::v-deep .el-upload-dragger {
  height: 230px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dfe5ef;
  background: #fff;
}
.upload-empty,
.upload-ready {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.upload-icon {
  color: #4263eb;
  font-size: 46px;
}
.upload-empty strong {
  margin-top: 22px;
  color: #8a9099;
  font-size: 16px;
  font-weight: 600;
}
.upload-empty span {
  margin-top: 10px;
  color: #4263eb;
  font-size: 12px;
}
.word-mark {
  width: 48px;
  height: 56px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  color: #fff;
  background: #4b6ff0;
  font-size: 24px;
  font-weight: 700;
}
.file-name {
  margin-top: 20px;
  color: #303133;
}
.file-status {
  margin-top: 12px;
  color: #1fbf5b;
  font-size: 13px;
}
.upload-tip {
  margin: -38px 0 0 12px;
  color: #a8abb2;
  font-size: 14px;
}
.parse-action {
  margin-top: 8px;
  text-align: center;
}
.plan-name-item {
  margin-top: 12px;
}
.switch-strip {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  margin: 2px 0 6px;
  font-weight: 700;
  color: #303133;
}
.deviation-select-button {
  height: 24px;
  padding: 2px 10px;
  border-radius: 2px;
}
.deviation-template-row {
  margin: 0 0 22px;
}
.deviation-template-row ::v-deep .el-textarea__inner {
  line-height: 22px;
  border-radius: 2px;
  color: #606266;
  resize: vertical;
}
.project-switch {
  margin-top: 44px;
}
.switch-strip ::v-deep .el-switch__label {
  color: #fff;
}
.switch-strip ::v-deep .el-switch__core {
  background-color: #edf0f5;
  border-color: #edf0f5;
}
.dynamic-fields {
  margin-top: 52px;
}
.parse-section {
  margin-bottom: 18px;
}
.section-head {
  min-height: 28px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  color: #303133;
}
.required-dot {
  color: #ff4d4f;
  font-size: 20px;
  line-height: 1;
}
.inline-upload {
  height: 24px;
  padding: 2px 8px;
  border-radius: 0;
}
.inline-extract {
  height: 24px;
  padding: 2px 10px;
  border-radius: 2px;
}
.section-hint {
  color: #909399;
  font-size: 12px;
}
.collapse-button {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: #909399;
  cursor: pointer;
}
.section-body {
  margin-top: 6px;
}
.section-body ::v-deep .el-textarea__inner {
  border-radius: 3px;
  color: #606266;
}
.field-files {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  color: #606266;
  font-size: 12px;
}
.directory-section {
  margin-top: 22px;
}
.directory-tabs {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 12px;
}
.technical-score-button {
  height: 24px;
  padding: 2px 10px;
}
.directory-body {
  margin-top: 8px;
}
.score-textarea {
  margin-bottom: 10px;
}
.score-textarea ::v-deep .el-textarea__inner {
  border-radius: 3px;
  line-height: 22px;
  color: #606266;
}
.custom-chapter-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 10px;
}
.custom-chapter-card {
  border: 1px solid #e4e7ed;
  background: #fafbfe;
  padding: 10px 12px 12px;
}
.custom-chapter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #303133;
}
.custom-chapter-head ::v-deep .el-button {
  padding: 0;
  color: #909399;
  font-size: 20px;
}
.custom-chapter-requirement {
  margin-top: 8px;
}
.custom-chapter-requirement ::v-deep .el-textarea__inner {
  line-height: 22px;
  color: #606266;
}
.custom-chapter-add {
  text-align: center;
}
.directory-tips {
  min-height: 116px;
  padding: 16px 18px;
  background: #f7f8fb;
  color: #8a9099;
  line-height: 1.7;
}
.directory-tips p {
  margin: 0;
}
.preview-title {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #cfd6e3;
  padding: 0 14px;
  font-weight: 700;
}
.preview-title small {
  color: #f59f00;
  font-weight: 400;
}
.outline-preview {
  flex: 1;
  min-height: 360px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid #cfd6e3;
}
.outline-empty {
  flex: 1;
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  color: #c0c4cc;
}
.outline-empty p {
  margin: 20px 0 0;
}
.outline-column-title {
  padding: 18px 10px;
  border-right: 1px solid #cfd6e3;
  background: #fafafa;
}
.outline-row {
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 0;
  border-bottom: 1px solid #edf0f6;
  background: #fff;
  text-align: left;
  cursor: pointer;
  color: #303133;
}
.outline-row:hover {
  background: #f6f8fc;
}
.outline-row.level-1 {
  padding: 0 14px;
  font-weight: 700;
}
.outline-row.level-2 {
  padding: 0 14px 0 34px;
}
.outline-row.level-3 {
  padding: 0 14px 0 58px;
  color: #606266;
}
.outline-row em {
  font-style: normal;
  color: #a8abb2;
  font-size: 12px;
}
.content-stage {
  padding: 14px;
  border-bottom: 1px solid #dfe4ee;
  background: #fbfcff;
}
.stage-title {
  margin-bottom: 8px;
  font-weight: 700;
  color: #303133;
}
.preview-actions {
  min-height: 74px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  padding: 0 18px;
  background: #fff;
}
.tech-deviation-dialog ::v-deep .el-dialog__header {
  padding: 20px 24px 14px;
  border-bottom: 1px solid #edf0f6;
}
.tech-deviation-dialog ::v-deep .el-dialog__title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}
.tech-deviation-dialog ::v-deep .el-dialog__body {
  padding: 18px 24px 24px;
}
.tech-deviation-dialog ::v-deep .el-dialog__footer {
  padding: 12px 24px 18px;
  border-top: 1px solid #edf0f6;
}
.deviation-template-card {
  min-height: 126px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
}
.deviation-template-table {
  margin-top: 12px;
}
.deviation-template-table ::v-deep th {
  background: #f5f7fa;
  color: #506176;
  font-weight: 700;
}
.deviation-template-table ::v-deep .el-input__inner {
  height: 26px;
  line-height: 26px;
  padding: 0 6px;
  border: 0;
  background: transparent;
}
.score-compare-dialog ::v-deep .el-dialog__body {
  padding: 18px 24px;
}
.score-compare-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}
.score-compare-pane h4 {
  margin: 0 0 8px;
  color: #303133;
}
.score-compare-pane ::v-deep .el-textarea__inner {
  line-height: 22px;
  color: #606266;
  resize: vertical;
}
@media (max-width: 1280px) {
  .plan-create-page {
    grid-template-columns: 200px minmax(0, 1fr);
  }
  .workbench {
    grid-template-columns: 1fr;
    overflow: auto;
  }
  .config-panel {
    border-right: 0;
    border-bottom: 1px solid #dfe4ee;
  }
  .preview-panel {
    min-height: 560px;
  }
}
</style>
