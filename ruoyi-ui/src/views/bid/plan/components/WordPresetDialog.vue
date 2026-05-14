<template>
  <el-dialog
    title="预设篇幅"
    :visible.sync="innerVisible"
    width="760px"
    append-to-body
    class="word-preset-dialog"
    @close="$emit('update:visible', false)"
  >
    <div class="preset-section highlight">
      <div class="preset-title">
        <i class="el-icon-medal" />
        <strong>自动分配</strong>
        <el-tag size="mini" type="danger">专业旗舰专享</el-tag>
      </div>
      <div class="preset-row">
        <span>默认</span>
        <el-button size="small" :type="selected === 'free' ? 'primary' : ''" plain @click="selectPreset('free', 300)">自由发挥</el-button>
      </div>
    </div>

    <div class="preset-row">
      <span>短篇小页数</span>
      <el-button
        v-for="item in shortOptions"
        :key="item"
        size="small"
        :type="wordLimit === item ? 'primary' : ''"
        plain
        @click="selectPreset(String(item), item)"
      >每段{{ item }}字</el-button>
    </div>

    <div class="preset-row">
      <span>常规中篇幅</span>
      <el-button
        v-for="item in normalOptions"
        :key="item"
        size="small"
        :type="wordLimit === item ? 'primary' : ''"
        plain
        @click="selectPreset(String(item), item)"
      >每段{{ item }}字</el-button>
    </div>

    <div class="preset-section highlight disabled">
      <div class="preset-title">
        <i class="el-icon-medal" />
        <strong>丰富大篇幅</strong>
        <el-tag size="mini" type="danger">专业旗舰专享</el-tag>
      </div>
      <div class="preset-row">
        <span />
        <el-button
          v-for="item in richOptions"
          :key="item"
          size="small"
          :type="wordLimit === item ? 'primary' : ''"
          plain
          @click="selectPreset(String(item), item)"
        >每段{{ item }}字</el-button>
      </div>
    </div>

    <div class="estimate-box">
      <strong>篇幅预测</strong>
      <p>预计字数：<b>{{ estimatedWords }}</b> 字</p>
      <p>预计页数：<b>{{ estimatedPages }}</b> 页</p>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="confirm">确认并完成新建</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'WordPresetDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    leafCount: {
      type: Number,
      default: 0
    },
    submitting: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      selected: '1200',
      wordLimit: 1200,
      pageWords: 380,
      shortOptions: [300, 600, 900],
      normalOptions: [1200, 1800, 2700, 3600, 4500],
      richOptions: [5400, 6300, 7200, 8100, 9000, 9900]
    }
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    estimatedWords() {
      return (this.leafCount || 0) * this.wordLimit
    },
    estimatedPages() {
      return this.estimatedWords ? Math.ceil(this.estimatedWords / this.pageWords) : 0
    }
  },
  methods: {
    selectPreset(preset, wordLimit) {
      this.selected = preset
      this.wordLimit = wordLimit
    },
    confirm() {
      this.$emit('confirm', {
        preset: this.selected,
        wordLimit: this.wordLimit,
        pageWords: this.pageWords
      })
    }
  }
}
</script>

<style scoped>
.word-preset-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}
.preset-section {
  margin-bottom: 14px;
}
.highlight {
  background: #fff0f0;
  border-radius: 4px;
  padding: 10px 12px;
}
.preset-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  color: #1f2d3d;
}
.preset-title i {
  color: #f59f00;
}
.preset-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 14px 0;
  flex-wrap: wrap;
}
.preset-row > span {
  width: 80px;
  color: #909399;
}
.disabled {
  opacity: .75;
}
.estimate-box {
  border-top: 1px dashed #e4e7ed;
  margin-top: 20px;
  padding-top: 18px;
  line-height: 1.9;
}
.estimate-box b {
  color: #f56c6c;
}
</style>
