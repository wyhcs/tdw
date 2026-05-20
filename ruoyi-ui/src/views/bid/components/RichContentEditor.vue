<template>
  <section class="rich-content-editor" :class="{ fullscreen: fullscreen, embedded: !showHeader }">
    <header v-if="showHeader" class="rich-header">
      <div class="rich-title">
        <span class="title-text">{{ selectedOutline ? selectedOutline.title : '正文编辑器' }}</span>
        <el-tag v-if="selectedOutline" size="mini" effect="plain">{{ levelText(selectedOutline.level) }}</el-tag>
        <span v-if="selectedOutline" class="save-state">{{ saveStateText }}</span>
      </div>
      <div class="rich-actions">
        <el-tooltip content="插入文本" placement="bottom">
          <el-button size="mini" icon="el-icon-edit-outline" :disabled="!selectedOutline" @click="openTextInsertDialog" />
        </el-tooltip>
        <el-tooltip content="插入图片" placement="bottom">
          <el-button size="mini" icon="el-icon-picture-outline" :disabled="!selectedOutline" @click="openGalleryDialog" />
        </el-tooltip>
        <el-tooltip content="插入表格" placement="bottom">
          <el-button size="mini" icon="el-icon-s-grid" :disabled="!selectedOutline" @click="insertDefaultTable" />
        </el-tooltip>
        <el-tooltip content="清除图表" placement="bottom">
          <el-button size="mini" icon="el-icon-delete" :disabled="!selectedOutline" @click="clearCharts" />
        </el-tooltip>
        <el-tooltip content="查看知识库引用" placement="bottom">
          <el-button size="mini" icon="el-icon-collection" :disabled="!selectedOutline" @click="openRefs" />
        </el-tooltip>
        <el-tooltip :content="fullscreen ? '退出全屏' : '切换全屏'" placement="bottom">
          <el-button size="mini" icon="el-icon-full-screen" :disabled="!selectedOutline" @click="toggleFullscreen" />
        </el-tooltip>
      </div>
    </header>

    <div v-if="!showHeader && fullscreen" class="embedded-fullscreen-tools">
      <span class="embedded-save-state">{{ saveStateText }}</span>
      <el-tooltip content="插入文本" placement="bottom">
        <el-button size="mini" icon="el-icon-edit-outline" :disabled="!selectedOutline" @click="openTextInsertDialog" />
      </el-tooltip>
      <el-tooltip content="插入图片" placement="bottom">
        <el-button size="mini" icon="el-icon-picture-outline" :disabled="!selectedOutline" @click="openGalleryDialog" />
      </el-tooltip>
      <el-tooltip content="插入表格" placement="bottom">
        <el-button size="mini" icon="el-icon-s-grid" :disabled="!selectedOutline" @click="insertDefaultTable" />
      </el-tooltip>
      <el-tooltip content="清除图表" placement="bottom">
        <el-button size="mini" icon="el-icon-delete" :disabled="!selectedOutline" @click="clearCharts" />
      </el-tooltip>
      <el-tooltip content="查看知识库引用" placement="bottom">
        <el-button size="mini" icon="el-icon-collection" :disabled="!selectedOutline" @click="openRefs" />
      </el-tooltip>
      <el-button size="mini" type="primary" plain icon="el-icon-close" @click="toggleFullscreen">退出全屏</el-button>
    </div>

    <div v-if="!selectedOutline" class="rich-empty">
      <i class="el-icon-document" />
      <p>请先选择一个大纲节点</p>
    </div>

    <div v-else ref="stage" class="editor-stage" @mousemove="handleStageMouseMove" @mouseleave="handleStageMouseLeave">
      <div v-show="selectionMenu.visible" class="selection-menu" :style="selectionMenu.style">
        <button
          v-for="item in selectionActions"
          :key="item.action"
          type="button"
          :disabled="selectionAiLoading"
          @mousedown.prevent
          @click="runSelectionAi(item.action)"
        >
          {{ item.label }}
        </button>
      </div>
      <div
        v-show="tableMenu.visible"
        class="table-action-menu"
        :style="tableMenu.style"
        @mousedown.prevent
      >
        <button type="button" title="在上方插入一行" @click="runTableCommand('addRowBefore')">
          <i class="el-icon-top" />
        </button>
        <button type="button" title="在下方插入一行" @click="runTableCommand('addRowAfter')">
          <i class="el-icon-bottom" />
        </button>
        <button type="button" title="在左边插入一列" @click="runTableCommand('addColumnBefore')">
          <i class="el-icon-back" />
        </button>
        <button type="button" title="在右边插入一列" @click="runTableCommand('addColumnAfter')">
          <i class="el-icon-right" />
        </button>
        <span class="table-action-separator"></span>
        <button type="button" title="删除行" @click="runTableCommand('deleteRow')">
          <i class="el-icon-minus" />
        </button>
        <button type="button" title="删除列" @click="runTableCommand('deleteColumn')">
          <i class="el-icon-close" />
        </button>
        <button type="button" title="删除表格" @click="runTableCommand('deleteTable')">
          <i class="el-icon-delete" />
        </button>
      </div>
      <button
        v-show="imageButton.visible"
        type="button"
        class="image-edit-button"
        :style="imageButton.style"
        @mousedown.prevent
        @click="openImageEdit"
      >
        点击编辑
      </button>
      <editor-content v-if="editor" :editor="editor" class="tiptap-host" />
    </div>

    <gallery-image-select-dialog :visible.sync="galleryVisible" @select="insertGalleryImage" />

    <el-dialog title="插入文本" :visible.sync="textInsertVisible" width="520px" append-to-body>
      <el-input
        v-model="textInsertValue"
        type="textarea"
        :rows="6"
        maxlength="1000"
        show-word-limit
        placeholder="请输入要插入到光标位置的文本"
      />
      <div slot="footer">
        <el-button @click="textInsertVisible = false">取消</el-button>
        <el-button type="primary" @click="insertTextFromDialog">插入</el-button>
      </div>
    </el-dialog>

    <el-dialog title="知识库引用" :visible.sync="refsVisible" width="560px" append-to-body>
      <el-empty v-if="!knowledgeRefs.length" description="暂无知识库引用" />
      <el-timeline v-else>
        <el-timeline-item v-for="(item, index) in knowledgeRefs" :key="index" :timestamp="item.source || '知识库资料'">
          {{ item.text }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog title="智能图形编辑" :visible.sync="imageEditVisible" width="860px" append-to-body>
      <div class="image-edit-layout">
        <div class="image-preview">
          <img v-if="imageForm.src" :src="imageForm.src" alt="">
          <div v-else class="mermaid-edit-preview">
            <div ref="imageMermaidPreview" class="mermaid-preview-inner"></div>
          </div>
        </div>
        <el-form label-width="72px" class="image-edit-form">
          <el-form-item label="标题">
            <el-input v-model="imageForm.title" maxlength="80" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="imageForm.description" type="textarea" :rows="3" maxlength="240" />
          </el-form-item>
          <el-form-item v-for="index in 4" :key="index" :label="'子项' + index">
            <el-input v-model="imageForm.items[index - 1]" maxlength="80" />
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button @click="resetImageForm">重置</el-button>
        <el-button type="primary" @click="applyImageEdit">应用</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import { Editor, EditorContent } from '@tiptap/vue-2'
import { Node, mergeAttributes } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableHeader from '@tiptap/extension-table-header'
import TableCell from '@tiptap/extension-table-cell'
import mermaid from 'mermaid/dist/mermaid.min.js'
import { applySelectionAi } from '@/api/bid/contents'
import GalleryImageSelectDialog from '@/views/bid/tool/components/GalleryImageSelectDialog.vue'

const BidImage = Image.extend({
  addAttributes() {
    const parentAttrs = this.parent ? this.parent() : {}
    return Object.assign({}, parentAttrs, {
      description: {
        default: null,
        parseHTML: element => element.getAttribute('data-description'),
        renderHTML: attributes => {
          return attributes.description ? { 'data-description': attributes.description } : {}
        }
      }
    })
  }
})

const MermaidBlock = Node.create({
  name: 'mermaidBlock',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: true,

  addAttributes() {
    return {
      code: {
        default: 'flowchart LR\nA[需求归纳] --> B[能力设计]\nB --> C[实施交付]',
        parseHTML: element => element.getAttribute('data-code') || '',
        renderHTML: attributes => ({ 'data-code': attributes.code || '' })
      },
      title: {
        default: '智能总结图',
        parseHTML: element => element.getAttribute('data-title') || '',
        renderHTML: attributes => ({ 'data-title': attributes.title || '' })
      },
      description: {
        default: '',
        parseHTML: element => element.getAttribute('data-description') || '',
        renderHTML: attributes => ({ 'data-description': attributes.description || '' })
      }
    }
  },

  parseHTML() {
    return [{ tag: 'div[data-type="mermaid"]' }]
  },

  renderHTML({ node, HTMLAttributes }) {
    return [
      'div',
      mergeAttributes(HTMLAttributes, {
        'data-type': 'mermaid',
        class: 'mermaid-node',
        contenteditable: 'false'
      }),
      ['div', { class: 'mermaid-title' }, node.attrs.title || '智能总结图'],
      ['div', { class: 'mermaid-description' }, node.attrs.description || ''],
      ['div', { class: 'mermaid-preview' }, ''],
      ['pre', { class: 'mermaid-source' }, node.attrs.code || '']
    ]
  },

  addCommands() {
    return {
      insertMermaid: attrs => ({ commands }) => {
        return commands.insertContent({ type: this.name, attrs })
      }
    }
  }
})

if (mermaid && mermaid.initialize) {
  mermaid.initialize({
    startOnLoad: false,
    securityLevel: 'loose',
    theme: 'default'
  })
}

export default {
  name: 'RichContentEditor',
  components: { EditorContent, GalleryImageSelectDialog },
  props: {
    bidId: { type: [Number, String], default: undefined },
    selectedOutline: { type: Object, default: null },
    blocks: { type: Array, default: () => [] },
    savingExternal: { type: Boolean, default: false },
    showHeader: { type: Boolean, default: true },
    showToolbar: { type: Boolean, default: true }
  },
  data() {
    return {
      editor: null,
      rendering: false,
      dirty: false,
      saving: false,
      saveTimer: null,
      currentOutlineId: undefined,
      pendingInsertRange: null,
      textInsertVisible: false,
      textInsertValue: '',
      galleryVisible: false,
      refsVisible: false,
      fullscreen: false,
      selectionAiLoading: false,
      lastSelectionRange: null,
      selectionMenu: {
        visible: false,
        style: {}
      },
      tableMenu: {
        visible: false,
        style: {},
        cell: null
      },
      imageButton: {
        visible: false,
        style: {}
      },
      selectedGraphicType: '',
      selectedImageNode: null,
      selectedImagePos: null,
      imageEditVisible: false,
      imageForm: {
        src: '',
        title: '',
        description: '',
        items: ['', '', '', '']
      },
      selectionActions: [
        { action: 'expand', label: '扩写' },
        { action: 'shorten', label: '缩写' },
        { action: 'rewrite', label: '改写' },
        { action: 'summaryDiagram', label: '总结图' },
        { action: 'summaryTable', label: '总结表' }
      ]
    }
  },
  computed: {
    saveStateText() {
      if (this.saving || this.savingExternal) return '保存中'
      return this.dirty ? '未保存' : '已保存'
    },
    knowledgeRefs() {
      const refs = []
      ;(this.blocks || []).forEach(block => {
        const parsed = this.parseContent(block)
        ;(parsed.references || []).forEach(item => refs.push(item))
      })
      return refs
    }
  },
  watch: {
    selectedOutline: {
      immediate: true,
      handler() {
        this.currentOutlineId = this.selectedOutline && this.selectedOutline.id
        this.loadBlocksToEditor()
      }
    },
    blocks: {
      deep: true,
      handler() {
        if (!this.dirty) this.loadBlocksToEditor()
      }
    },
    imageEditVisible(value) {
      if (value && this.selectedGraphicType === 'mermaid') {
        this.$nextTick(() => this.renderMermaidPreview(this.$refs.imageMermaidPreview, this.buildMermaidCodeFromForm()))
      }
    },
    imageForm: {
      deep: true,
      handler() {
        if (this.imageEditVisible && this.selectedGraphicType === 'mermaid') {
          this.$nextTick(() => this.renderMermaidPreview(this.$refs.imageMermaidPreview, this.buildMermaidCodeFromForm()))
        }
      }
    }
  },
  mounted() {
    this.initEditor()
    this.loadBlocksToEditor()
  },
  beforeDestroy() {
    if (this.saveTimer) clearTimeout(this.saveTimer)
    this.saveNow()
    if (this.editor) {
      this.editor.destroy()
      this.editor = null
    }
  },
  methods: {
    initEditor() {
      if (this.editor) return
      this.editor = new Editor({
        content: '',
        extensions: [
          StarterKit,
          Placeholder.configure({ placeholder: '请输入正文内容' }),
          BidImage.configure({ allowBase64: true, inline: false }),
          Table.configure({ resizable: true }),
          TableRow,
          TableHeader,
          TableCell,
          MermaidBlock
        ],
        editorProps: {
          attributes: { class: 'tiptap-surface' },
          handleClick: (view, pos, event) => {
            this.handleEditorClick(event)
            return false
          }
        },
        onUpdate: () => {
          if (this.rendering) return
          this.markDirty()
          this.renderMermaidNodes()
        },
        onSelectionUpdate: ({ editor }) => {
          this.handleSelectionChange(editor.state.selection)
        },
        onCreate: () => {
          this.renderMermaidNodes()
        }
      })
    },
    loadBlocksToEditor() {
      this.$nextTick(() => {
        if (!this.editor || !this.selectedOutline) return
        this.rendering = true
        this.editor.commands.setContent(this.blocksToHtml(this.blocks) || '<p></p>', false)
        this.rendering = false
        this.dirty = false
        this.selectionMenu.visible = false
        this.imageButton.visible = false
        this.hideTableMenu()
        this.renderMermaidNodes()
      })
    },
    setExternalStreamingText(text) {
      if (!this.editor || !this.selectedOutline) return false
      this.rendering = true
      this.editor.commands.setContent(this.textToParagraphs(text) || '<p></p>', false)
      this.rendering = false
      this.dirty = false
      this.selectionMenu.visible = false
      this.imageButton.visible = false
      this.hideTableMenu()
      this.renderMermaidNodes()
      return true
    },
    handleSelectionChange(selection) {
      this.imageButton.visible = false
      if (!selection || selection.empty || !this.editor) {
        this.selectionMenu.visible = false
        return
      }
      const selectedText = this.editor.state.doc.textBetween(selection.from, selection.to, '\n').trim()
      if (!selectedText) {
        this.selectionMenu.visible = false
        return
      }
      const nativeRect = this.selectedNativeRect()
      const coords = nativeRect || this.editor.view.coordsAtPos(selection.from)
      const stageRect = this.$refs.stage.getBoundingClientRect()
      const left = nativeRect ? nativeRect.left + nativeRect.width / 2 : coords.left
      const top = nativeRect ? nativeRect.top : coords.top
      this.lastSelectionRange = { from: selection.from, to: selection.to }
      this.selectionMenu = {
        visible: true,
        style: {
          left: Math.max(8, left - stageRect.left - 140 + this.$refs.stage.scrollLeft) + 'px',
          top: Math.max(8, top - stageRect.top - 42 + this.$refs.stage.scrollTop) + 'px'
        }
      }
    },
    selectedNativeRect() {
      const selection = window.getSelection && window.getSelection()
      if (!selection || selection.rangeCount === 0 || selection.isCollapsed || !this.$refs.stage) {
        return null
      }
      const anchor = selection.anchorNode
      const anchorElement = anchor && (anchor.nodeType === 1 ? anchor : anchor.parentNode)
      if (anchorElement && !this.$refs.stage.contains(anchorElement)) {
        return null
      }
      const rect = selection.getRangeAt(0).getBoundingClientRect()
      return rect && rect.width ? rect : null
    },
    handleEditorClick(event) {
      const target = event && event.target
      const image = this.findClosest(target, 'IMG')
      const mermaidNode = this.findClosestByClass(target, 'mermaid-node')
      if (!image && !mermaidNode) {
        this.imageButton.visible = false
        return
      }
      const targetNode = image || mermaidNode
      this.selectedGraphicType = image ? 'image' : 'mermaid'
      this.selectedImageNode = targetNode
      this.selectedImagePos = this.findNodePos(targetNode)
      const stageRect = this.$refs.stage.getBoundingClientRect()
      const rect = targetNode.getBoundingClientRect()
      this.imageButton = {
        visible: true,
        style: {
          left: rect.right - stageRect.left - 74 + this.$refs.stage.scrollLeft + 'px',
          top: rect.top - stageRect.top + 8 + this.$refs.stage.scrollTop + 'px'
        }
      }
    },
    handleStageMouseMove(event) {
      const target = event && event.target
      if (this.findClosestByClass(target, 'table-action-menu')) return
      const cell = this.findClosestTableCell(target)
      if (!cell) {
        this.hideTableMenu()
        return
      }
      this.showTableMenu(cell)
    },
    handleStageMouseLeave() {
      this.hideTableMenu()
    },
    findClosestTableCell(target) {
      let node = target
      while (node && node !== this.$refs.stage) {
        if (node.tagName === 'TD' || node.tagName === 'TH') return node
        node = node.parentNode
      }
      return null
    },
    showTableMenu(cell) {
      if (!cell || !this.$refs.stage) return
      const stageRect = this.$refs.stage.getBoundingClientRect()
      const rect = cell.getBoundingClientRect()
      const topAbove = rect.top - stageRect.top - 38 + this.$refs.stage.scrollTop
      const topBelow = rect.bottom - stageRect.top + 6 + this.$refs.stage.scrollTop
      this.tableMenu = {
        visible: true,
        cell,
        style: {
          left: Math.max(8, rect.left - stageRect.left + this.$refs.stage.scrollLeft) + 'px',
          top: Math.max(8, topAbove > 8 ? topAbove : topBelow) + 'px'
        }
      }
    },
    hideTableMenu() {
      this.tableMenu.visible = false
      this.tableMenu.cell = null
    },
    focusTableCell(cell) {
      if (!this.editor || !cell) return false
      try {
        const pos = this.editor.view.posAtDOM(cell, 0)
        this.editor.chain().focus().setTextSelection(pos + 1).run()
        return true
      } catch (e) {
        this.editor.commands.focus()
        return false
      }
    },
    runTableCommand(command) {
      if (!this.editor || !this.tableMenu.cell) return
      this.focusTableCell(this.tableMenu.cell)
      const chain = this.editor.chain().focus()
      if (!chain[command]) return
      chain[command]().run()
      this.hideTableMenu()
      this.markDirty()
      this.saveNow()
    },
    findClosest(target, tagName) {
      let node = target
      while (node && node !== this.$refs.stage) {
        if (node.tagName === tagName) return node
        node = node.parentNode
      }
      return null
    },
    findClosestByClass(target, className) {
      let node = target
      while (node && node !== this.$refs.stage) {
        if (node.classList && node.classList.contains(className)) return node
        node = node.parentNode
      }
      return null
    },
    findNodePos(node) {
      try {
        return this.editor && this.editor.view ? this.editor.view.posAtDOM(node, 0) : null
      } catch (e) {
        return null
      }
    },
    ensureEditorReady() {
      if (!this.selectedOutline) {
        this.$modal.msgWarning('请先选择段级标题')
        return false
      }
      if (!this.editor) {
        this.initEditor()
      }
      if (!this.editor) {
        this.$modal.msgWarning('编辑器尚未初始化')
        return false
      }
      this.editor.commands.focus()
      return true
    },
    rememberInsertRange() {
      if (!this.editor) return
      const selection = this.editor.state.selection
      this.pendingInsertRange = this.normalizeRange({ from: selection.from, to: selection.to })
    },
    normalizeRange(range) {
      if (!this.editor) return { from: 0, to: 0 }
      const max = this.editor.state.doc.content.size
      const from = Math.min(Math.max(0, Number(range && range.from) || 0), max)
      const to = Math.min(Math.max(from, Number(range && range.to) || from), max)
      return { from, to }
    },
    markDirty() {
      this.dirty = true
      if (this.saveTimer) clearTimeout(this.saveTimer)
      this.saveTimer = setTimeout(() => this.saveNow(), 900)
    },
    saveNow() {
      if (!this.selectedOutline || !this.editor || !this.dirty) return
      if (this.saveTimer) clearTimeout(this.saveTimer)
      this.saving = true
      this.$emit('save-rich', {
        outlineId: this.selectedOutline.id,
        html: this.editor.getHTML(),
        text: this.editor.getText()
      }, success => {
        this.saving = false
        if (success !== false) this.dirty = false
      })
    },
    openTextInsertDialog() {
      if (!this.ensureEditorReady()) return
      this.rememberInsertRange()
      this.textInsertValue = ''
      this.textInsertVisible = true
    },
    insertTextFromDialog() {
      const text = String(this.textInsertValue || '').trim()
      if (!text) {
        this.$modal.msgWarning('请输入要插入的文本')
        return
      }
      this.insertTextBlock(text)
      this.textInsertVisible = false
    },
    insertTextBlock(text) {
      if (!this.ensureEditorReady()) return
      const range = this.normalizeRange(this.pendingInsertRange || this.editor.state.selection)
      const chain = this.editor.chain().focus()
      if (range.to > range.from) {
        chain.deleteRange(range)
      }
      chain.insertContent(this.textToTiptapBlocks(text)).run()
      this.pendingInsertRange = null
      this.selectionMenu.visible = false
      this.markDirty()
      this.saveNow()
    },
    runSelectionAi(action) {
      if (!this.ensureEditorReady() || !this.lastSelectionRange) return
      const range = this.normalizeRange(this.lastSelectionRange)
      const selectedText = this.editor.state.doc.textBetween(range.from, range.to, '\n').trim()
      if (!selectedText) return
      this.selectionAiLoading = true
      applySelectionAi({
        bidId: this.bidId || (this.selectedOutline && this.selectedOutline.bidId),
        outlineId: this.selectedOutline && this.selectedOutline.id,
        action,
        selectedText,
        documentText: this.editor.getText()
      }).then(res => {
        const result = res.data || {}
        if (action === 'summaryDiagram') {
          this.editor.chain().focus().setTextSelection(range.to).insertMermaid(this.buildMermaidAttrs(result)).run()
        } else if (action === 'summaryTable') {
          this.editor.chain().focus().setTextSelection(range.to).insertContent(result.html || this.buildTableHtml(result)).run()
        } else {
          this.editor.chain().focus().deleteRange(range).insertContent(result.text || '').run()
        }
        this.selectionMenu.visible = false
        this.renderMermaidNodes()
        this.markDirty()
        this.saveNow()
      }).catch(() => {
        this.$modal.msgError('选中文本处理失败')
      }).finally(() => {
        this.selectionAiLoading = false
      })
    },
    openGalleryDialog() {
      if (!this.ensureEditorReady()) return
      this.rememberInsertRange()
      this.galleryVisible = true
    },
    insertGalleryImage(image) {
      if (!image || !this.ensureEditorReady()) return
      const range = this.normalizeRange(this.pendingInsertRange || this.editor.state.selection)
      const url = this.resourceUrl(image.imageUrl || image.url)
      const title = image.imageName || image.title || ''
      const chain = this.editor.chain().focus()
      if (range.to > range.from) {
        chain.deleteRange(range)
      }
      chain.insertContent({ type: 'image', attrs: { src: url, alt: title, title }}).run()
      this.pendingInsertRange = null
      this.markDirty()
      this.saveNow()
    },
    insertDefaultTable() {
      if (!this.ensureEditorReady()) return
      const range = this.normalizeRange(this.pendingInsertRange || this.editor.state.selection)
      const chain = this.editor.chain().focus()
      if (range.to > range.from) {
        chain.deleteRange(range)
      }
      chain.insertTable({ rows: 3, cols: 3, withHeaderRow: false }).run()
      this.pendingInsertRange = null
      this.markDirty()
      this.saveNow()
    },
    clearCharts() {
      if (!this.ensureEditorReady()) return
      this.$modal.confirm('确定清除当前内容中的图片、表格和 Mermaid 图表吗？').then(() => {
        const ranges = []
        this.editor.state.doc.descendants((node, pos) => {
          if (['image', 'table', 'mermaidBlock'].indexOf(node.type.name) !== -1) {
            ranges.push({ from: pos, to: pos + node.nodeSize })
          }
        })
        if (!ranges.length) {
          this.$modal.msgWarning('当前内容中没有可清除的图表')
          return
        }
        const tr = this.editor.state.tr
        ranges.reverse().forEach(range => tr.delete(range.from, range.to))
        this.editor.view.dispatch(tr)
        this.imageButton.visible = false
        this.markDirty()
        this.saveNow()
      }).catch(() => {})
    },
    openRefs() {
      if (!this.selectedOutline) return
      this.refsVisible = true
    },
    toggleFullscreen() {
      if (!this.selectedOutline) return
      this.fullscreen = !this.fullscreen
      this.$nextTick(() => this.renderMermaidNodes())
    },
    openImageEdit() {
      if (!this.selectedImageNode) return
      if (this.selectedGraphicType === 'mermaid') {
        const code = this.selectedImageNode.getAttribute('data-code') || ''
        this.imageForm = {
          src: '',
          title: this.selectedImageNode.getAttribute('data-title') || '智能总结图',
          description: this.selectedImageNode.getAttribute('data-description') || '',
          items: this.normalizeItems(this.parseMermaidItems(code))
        }
      } else {
        this.imageForm = {
          src: this.selectedImageNode.getAttribute('src') || '',
          title: this.selectedImageNode.getAttribute('alt') || this.selectedImageNode.getAttribute('title') || '智能图形',
          description: this.selectedImageNode.getAttribute('data-description') || '',
          items: ['', '', '', '']
        }
      }
      this.imageEditVisible = true
    },
    resetImageForm() {
      this.imageForm.title = this.selectedGraphicType === 'mermaid' ? '智能总结图' : '智能图形'
      this.imageForm.description = ''
      this.imageForm.items = ['', '', '', '']
    },
    applyImageEdit() {
      if (!this.editor || this.selectedImagePos == null) return
      const title = this.imageForm.title || (this.selectedGraphicType === 'mermaid' ? '智能总结图' : '智能图形')
      if (this.selectedGraphicType === 'mermaid') {
        this.editor.chain().focus()
          .setNodeSelection(this.selectedImagePos)
          .updateAttributes('mermaidBlock', {
            title,
            description: this.imageForm.description || '',
            code: this.buildMermaidCodeFromForm()
          })
          .run()
        this.renderMermaidNodes()
      } else {
        this.editor.chain().focus()
          .setNodeSelection(this.selectedImagePos)
          .updateAttributes('image', {
            alt: title,
            title,
            description: this.imageForm.description || ''
          })
          .run()
      }
      this.imageEditVisible = false
      this.markDirty()
      this.saveNow()
    },
    renderMermaidNodes() {
      this.$nextTick(() => {
        if (!this.$refs.stage || !mermaid) return
        const nodes = this.$refs.stage.querySelectorAll('.mermaid-node')
        Array.prototype.forEach.call(nodes, node => {
          const code = node.getAttribute('data-code') || ''
          const preview = node.querySelector('.mermaid-preview')
          if (!code || !preview || node.getAttribute('data-rendered-code') === code) return
          this.renderMermaidPreview(preview, code, () => {
            node.setAttribute('data-rendered-code', code)
          })
        })
      })
    },
    renderMermaidPreview(target, code, done) {
      if (!target || !code || !mermaid || !mermaid.render) return
      const id = 'bid-mermaid-' + Date.now() + '-' + Math.floor(Math.random() * 100000)
      const setSvg = svg => {
        target.innerHTML = svg || '<div class="mermaid-error">图表渲染为空</div>'
        if (typeof done === 'function') done()
      }
      try {
        const result = mermaid.render(id, code, svg => setSvg(svg))
        if (typeof result === 'string') {
          setSvg(result)
        } else if (result && typeof result.then === 'function') {
          result.then(output => setSvg(output && output.svg ? output.svg : output)).catch(() => {
            target.innerHTML = '<div class="mermaid-error">Mermaid 图表渲染失败</div>'
          })
        } else if (result && result.svg) {
          setSvg(result.svg)
        }
      } catch (e) {
        target.innerHTML = '<div class="mermaid-error">Mermaid 图表渲染失败</div>'
      }
    },
    buildMermaidAttrs(result) {
      return {
        title: result.title || '智能总结图',
        description: result.description || '',
        code: this.buildMermaidCode(result)
      }
    },
    buildMermaidCode(result) {
      const keywords = this.normalizeItems(result.keywords || ['需求归纳', '能力设计', '过程控制', '交付保障'])
      return 'flowchart LR\n' +
        keywords.map((item, index) => 'N' + index + '["' + this.escapeMermaidLabel(item || ('要点' + (index + 1))) + '"]').join('\n') + '\n' +
        keywords.slice(1).map((item, index) => 'N' + index + ' --> N' + (index + 1)).join('\n')
    },
    buildMermaidCodeFromForm() {
      const keywords = this.normalizeItems(this.imageForm.items)
      return this.buildMermaidCode({ keywords })
    },
    parseMermaidItems(code) {
      const matches = []
      const reg = /\[[\"']?([^"'[\]]+)[\"']?\]/g
      let match = reg.exec(code || '')
      while (match) {
        matches.push(match[1])
        match = reg.exec(code || '')
      }
      return matches
    },
    normalizeItems(items) {
      const normalized = (items || []).map(item => String(item || '').trim()).filter(Boolean).slice(0, 4)
      while (normalized.length < 4) {
        normalized.push('响应要点' + (normalized.length + 1))
      }
      return normalized
    },
    blocksToHtml(blocks) {
      if (!blocks || !blocks.length) return ''
      return blocks.map(block => {
        const content = this.parseContent(block)
        if (block.contentType === 1) {
          if (content.html) return content.html
          return this.textToParagraphs(content.text || block.content || '')
        }
        if (block.contentType === 2) return this.tableBlockToHtml(content)
        return this.diagramBlockToHtml(content)
      }).join('')
    },
    textToTiptapBlocks(text) {
      return String(text || '').split(/\n+/).map(item => item.trim()).filter(Boolean).map(item => ({
        type: 'paragraph',
        content: [{ type: 'text', text: item }]
      }))
    },
    textToParagraphs(text) {
      return String(text || '').split(/\n+/).map(item => item.trim()).filter(Boolean).map(item => '<p>' + this.escapeHtml(item) + '</p>').join('')
    },
    tableBlockToHtml(content) {
      const headers = Array.isArray(content.headers) && content.headers.length ? content.headers : ['内容']
      const rows = Array.isArray(content.rows) ? content.rows : []
      let html = ''
      if (content.title || content.name) html += '<p><strong>' + this.escapeHtml(content.title || content.name) + '</strong></p>'
      html += '<table><thead><tr>' + headers.map(item => '<th>' + this.escapeHtml(item) + '</th>').join('') + '</tr></thead><tbody>'
      rows.forEach(row => {
        html += '<tr>' + headers.map((header, index) => {
          const value = Array.isArray(row) ? row[index] : row[header]
          return '<td>' + this.escapeHtml(value || '') + '</td>'
        }).join('') + '</tr>'
      })
      return html + '</tbody></table>'
    },
    diagramBlockToHtml(content) {
      const imageUrl = content.imageUrl || content.url
      if (imageUrl) {
        return '<p><img src="' + this.escapeAttr(this.resourceUrl(imageUrl)) + '" alt="' + this.escapeAttr(content.title || content.name || '图示') + '"></p>'
      }
      const result = {
        title: content.title || content.name || '智能总结图',
        description: content.description || '',
        keywords: content.keywords || (content.mermaid ? this.parseMermaidItems(content.mermaid) : ['需求归纳', '能力设计', '过程控制', '交付保障'])
      }
      const code = content.mermaid || this.buildMermaidCode(result)
      return '<div data-type="mermaid" data-title="' + this.escapeAttr(result.title) + '" data-description="' + this.escapeAttr(result.description) + '" data-code="' + this.escapeAttr(code) + '"></div>'
    },
    buildTableHtml(result) {
      const headers = result.headers || ['归纳维度', '关键内容', '标书响应建议']
      const rows = result.rows || []
      return '<table><thead><tr>' + headers.map(item => '<th>' + this.escapeHtml(item) + '</th>').join('') + '</tr></thead><tbody>' +
        rows.map(row => {
          const cells = Array.isArray(row) ? row : headers.map(header => row && row[header])
          return '<tr>' + cells.map(cell => '<td>' + this.escapeHtml(cell) + '</td>').join('') + '</tr>'
        }).join('') +
        '</tbody></table>'
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
    levelText(level) {
      return Number(level) === 1 ? '章' : Number(level) === 2 ? '节' : '内容标题'
    },
    resourceUrl(url) {
      if (!url) return ''
      if (/^https?:\/\//.test(url) || /^data:/.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    },
    escapeHtml(value) {
      return String(value == null ? '' : value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    },
    escapeAttr(value) {
      return this.escapeHtml(value).replace(/"/g, '&quot;').replace(/'/g, '&#39;')
    },
    escapeMermaidLabel(value) {
      return String(value == null ? '' : value).replace(/"/g, '\\"').replace(/\n/g, ' ')
    }
  }
}
</script>

<style scoped>
.rich-content-editor {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  color: #1f2937;
}
.rich-content-editor.fullscreen {
  position: fixed;
  z-index: 2001;
  inset: 0;
}
.rich-content-editor.embedded {
  background: transparent;
}
.rich-header,
.embedded-fullscreen-tools {
  min-height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 14px;
  border-bottom: 1px solid #dfe4ee;
  background: #fff;
}
.rich-title {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.title-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 700;
}
.save-state,
.embedded-save-state {
  color: #7b8794;
  font-size: 12px;
}
.rich-actions,
.embedded-fullscreen-tools {
  display: flex;
  align-items: center;
  gap: 6px;
}
.embedded-fullscreen-tools {
  justify-content: flex-end;
}
.embedded-save-state {
  margin-right: auto;
}
.rich-actions .el-button,
.embedded-fullscreen-tools .el-button {
  padding: 6px;
}
.rich-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.rich-empty i {
  font-size: 40px;
  margin-bottom: 12px;
}
.editor-stage {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: #f8f9fc;
}
.tiptap-host {
  max-width: 980px;
  min-height: calc(100vh - 176px);
  margin: 0 auto;
  border-left: 1px solid #dfe4ee;
  border-right: 1px solid #dfe4ee;
  background: #fff;
}
.rich-content-editor.fullscreen .tiptap-host {
  min-height: calc(100vh - 42px);
}
.rich-content-editor.fullscreen.embedded .tiptap-host {
  min-height: calc(100vh - 42px);
}
.rich-content-editor.embedded .tiptap-host {
  max-width: none;
  min-height: calc(100vh - 150px);
  margin: 0;
  border: 0;
}
.tiptap-host ::v-deep .tiptap-surface {
  min-height: calc(100vh - 226px);
  padding: 28px 34px 64px;
  outline: none;
  line-height: 1.9;
  font-size: 16px;
}
.rich-content-editor.embedded .tiptap-host ::v-deep .tiptap-surface {
  min-height: calc(100vh - 218px);
  padding: 14px 34px 64px;
}
.rich-content-editor.fullscreen.embedded .tiptap-host ::v-deep .tiptap-surface {
  min-height: calc(100vh - 96px);
  padding-top: 24px;
}
.tiptap-host ::v-deep .tiptap-surface p {
  margin: 0 0 10px;
}
.tiptap-host ::v-deep .tiptap-surface h3,
.tiptap-host ::v-deep .tiptap-surface h4,
.tiptap-host ::v-deep .tiptap-surface h5 {
  margin: 18px 0 12px;
}
.tiptap-host ::v-deep .tiptap-surface table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0 18px;
}
.tiptap-host ::v-deep .tiptap-surface th,
.tiptap-host ::v-deep .tiptap-surface td {
  border: 1px solid #d7dce5;
  padding: 8px 10px;
  vertical-align: top;
}
.tiptap-host ::v-deep .tiptap-surface th:hover,
.tiptap-host ::v-deep .tiptap-surface td:hover {
  outline: 2px solid #409eff;
  outline-offset: -2px;
}
.tiptap-host ::v-deep .tiptap-surface th p,
.tiptap-host ::v-deep .tiptap-surface td p {
  min-height: 22px;
  margin: 0;
}
.tiptap-host ::v-deep .tiptap-surface th {
  background: #f3f6fb;
  font-weight: 700;
}
.tiptap-host ::v-deep .tiptap-surface img {
  display: block;
  max-width: 92%;
  margin: 14px auto;
  cursor: pointer;
}
.tiptap-host ::v-deep .ProseMirror-selectednode {
  outline: 2px solid #1f6bff;
}
.tiptap-host ::v-deep .mermaid-node {
  position: relative;
  border: 1px solid #9ec5fe;
  border-radius: 6px;
  margin: 14px 0 18px;
  padding: 16px;
  background: #f8fbff;
  cursor: pointer;
}
.tiptap-host ::v-deep .mermaid-title {
  margin-bottom: 6px;
  text-align: center;
  color: #1f2937;
  font-weight: 700;
}
.tiptap-host ::v-deep .mermaid-description {
  margin-bottom: 10px;
  text-align: center;
  color: #607086;
  font-size: 13px;
}
.tiptap-host ::v-deep .mermaid-preview {
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tiptap-host ::v-deep .mermaid-preview svg {
  max-width: 100%;
  height: auto;
}
.tiptap-host ::v-deep .mermaid-source {
  display: none;
}
.tiptap-host ::v-deep .is-empty::before {
  content: attr(data-placeholder);
  float: left;
  height: 0;
  color: #a8abb2;
  pointer-events: none;
}
.selection-menu {
  position: absolute;
  z-index: 8;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 4px;
  border-radius: 6px;
  background: #222833;
  box-shadow: 0 8px 22px rgba(17, 24, 39, .22);
}
.selection-menu button {
  border: 0;
  border-radius: 4px;
  padding: 6px 10px;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
}
.selection-menu button:hover {
  background: rgba(255, 255, 255, .14);
}
.selection-menu button:disabled {
  opacity: .6;
  cursor: wait;
}
.table-action-menu {
  position: absolute;
  z-index: 9;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border: 1px solid #d7dce5;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(17, 24, 39, .16);
}
.table-action-menu button {
  width: 26px;
  height: 26px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: #4b5563;
  cursor: pointer;
  line-height: 26px;
  text-align: center;
}
.table-action-menu button:hover {
  background: #edf3ff;
  color: #1f6bff;
}
.table-action-separator {
  width: 1px;
  height: 18px;
  margin: 0 2px;
  background: #d7dce5;
}
.image-edit-button {
  position: absolute;
  z-index: 7;
  border: 0;
  border-radius: 4px;
  padding: 6px 8px;
  background: #1f6bff;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}
.image-edit-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 22px;
}
.image-preview {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fb;
}
.image-preview img {
  max-width: 100%;
  max-height: 420px;
}
.mermaid-edit-preview {
  width: 100%;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.mermaid-preview-inner svg {
  max-width: 100%;
}
.mermaid-error {
  color: #f56c6c;
  font-size: 13px;
}
.image-edit-form {
  padding-top: 8px;
}
@media (max-width: 1100px) {
  .rich-header {
    align-items: flex-start;
    flex-direction: column;
    padding: 10px 12px;
  }
  .rich-actions,
  .embedded-fullscreen-tools {
    flex-wrap: wrap;
  }
  .image-edit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
