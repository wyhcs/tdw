<template>
  <div class="bid-home-page">
    <section class="home-hero">
      <div class="hero-copy">
        <p class="hero-kicker">标书智能写作与管理系统</p>
        <h1>省时、省力、省头脑 AI标书找小晓</h1>
        <p class="hero-subtitle">小晓AI标书精灵，懂标书，更懂标书人</p>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-plus" @click="goModule('/bid/plan/create')">新建AI方案</el-button>
          <el-button icon="el-icon-upload2" @click="goModule('/bid/tender')">上传招标文件</el-button>
        </div>
      </div>
      <div class="hero-status" aria-label="系统概览">
        <div v-for="item in statusCards" :key="item.label" class="status-item">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </section>

    <section class="module-layout">
      <div class="feature-grid">
        <button
          v-for="item in primaryModules"
          :key="item.title"
          type="button"
          class="feature-card"
          :class="item.theme"
          @click="goModule(item.path)"
        >
          <div class="card-copy">
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
          <div class="card-visual" :class="item.visual">
            <span class="visual-page page-a"></span>
            <span class="visual-page page-b"></span>
            <span class="visual-panel">
              <i :class="item.icon"></i>
              <em v-for="line in 4" :key="line"></em>
            </span>
          </div>
          <div class="card-footer">
            <span>{{ item.action }}</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </button>
      </div>

      <aside class="quick-panel">
        <button
          v-for="item in quickModules"
          :key="item.title"
          type="button"
          class="quick-item"
          @click="goModule(item.path)"
        >
          <span :class="['quick-icon', item.theme]"><i :class="item.icon"></i></span>
          <span>
            <strong>{{ item.title }}</strong>
            <em>{{ item.description }}</em>
          </span>
        </button>
      </aside>
    </section>

    <section class="workbench-row">
      <button
        v-for="item in workbenchModules"
        :key="item.title"
        type="button"
        class="workbench-card"
        :class="item.theme"
        @click="goModule(item.path)"
      >
        <span class="workbench-icon"><i :class="item.icon"></i></span>
        <span class="workbench-copy">
          <strong>{{ item.title }}</strong>
          <em>{{ item.description }}</em>
        </span>
        <i class="el-icon-arrow-right"></i>
      </button>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      statusCards: [
        { label: '今日可处理任务', value: '18' },
        { label: '沉淀知识文件', value: '126' },
        { label: '可复用图库素材', value: '84' },
        { label: '质检风险待确认', value: '7' }
      ],
      primaryModules: [
        {
          title: 'AI方案',
          description: '智能解析标书，AI提取评分重点，生成目录并支持在线编辑、一键导出。',
          action: '进入方案写作',
          path: '/bid/plan',
          icon: 'el-icon-magic-stick',
          theme: 'theme-plan',
          visual: 'visual-plan'
        },
        {
          title: 'AI标书',
          description: '一键解析招标文件，提取投标模板，全流程一站式生成投标方案。',
          action: '进入标书写作',
          path: '/bid/tender',
          icon: 'el-icon-document',
          theme: 'theme-tender',
          visual: 'visual-tender'
        },
        {
          title: 'AI质检',
          description: '合规项逐项核对，智能排查废标风险，一键生成合规检测报告。',
          action: '进入质量检查',
          path: '/bid/quality',
          icon: 'el-icon-circle-check',
          theme: 'theme-quality',
          visual: 'visual-quality'
        },
        {
          title: '方案查重',
          description: '跨文件比对，重复内容精准定位，雷同风险实时预警。',
          action: '进入方案查重',
          path: '/bid/duplicate',
          icon: 'el-icon-refresh',
          theme: 'theme-duplicate',
          visual: 'visual-duplicate'
        }
      ],
      quickModules: [
        {
          title: '知识库',
          description: '沉淀资料，驱动组织进化',
          path: '/bid/knowledge',
          icon: 'el-icon-collection',
          theme: 'quick-green'
        },
        {
          title: '私人图库',
          description: '管理图片素材，支持文档抽图',
          path: '/bid/gallery',
          icon: 'el-icon-picture-outline',
          theme: 'quick-violet'
        },
        {
          title: 'AI工具',
          description: '常用标书辅助工具集合',
          path: '/bid/tool',
          icon: 'el-icon-cpu',
          theme: 'quick-blue'
        },
        {
          title: '下载中心',
          description: '统一管理生成文件与报告',
          path: '/bid/download',
          icon: 'el-icon-download',
          theme: 'quick-orange'
        }
      ],
      workbenchModules: [
        {
          title: '资料与素材沉淀',
          description: '从知识库和私人图库中复用组织经验，让每次写标都能带上历史积累。',
          path: '/bid/knowledge',
          icon: 'el-icon-folder-opened',
          theme: 'bench-warm'
        },
        {
          title: '风险检查闭环',
          description: '把质检结果、查重报告和下载归档串起来，减少交付前的遗漏。',
          path: '/bid/quality',
          icon: 'el-icon-warning-outline',
          theme: 'bench-blue'
        },
        {
          title: '成果统一归档',
          description: '方案、标书、报告和转换文件集中查看，方便后续追溯与复用。',
          path: '/bid/download',
          icon: 'el-icon-files',
          theme: 'bench-lilac'
        }
      ]
    }
  },
  methods: {
    goModule(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped lang="scss">
.bid-home-page {
  min-height: calc(100vh - 84px);
  padding: 28px 24px 36px;
  background: linear-gradient(180deg, #eaf6ff 0%, #f8fbff 30%, #ffffff 100%);
  color: #1f2937;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 24px;
  align-items: center;
  min-height: 188px;
  max-width: 1760px;
  margin: 0 auto;
}

.hero-copy {
  text-align: center;
}

.hero-kicker {
  margin: 0 0 12px;
  color: #2f6bff;
  font-size: 14px;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 0;
  font-size: 32px;
  line-height: 1.25;
  font-weight: 800;
  color: #202938;
  letter-spacing: 0;
}

.hero-subtitle {
  margin: 12px 0 22px;
  color: #7b8794;
  font-size: 16px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.hero-status {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.status-item {
  height: 78px;
  padding: 14px 16px;
  border: 1px solid rgba(117, 141, 170, .2);
  border-radius: 8px;
  background: rgba(255, 255, 255, .72);
}

.status-item strong {
  display: block;
  margin-bottom: 6px;
  color: #26364c;
  font-size: 24px;
}

.status-item span {
  color: #6b7280;
  font-size: 13px;
}

.module-layout {
  display: grid;
  grid-template-columns: minmax(0, 4fr) minmax(220px, 280px);
  gap: 20px;
  max-width: 1760px;
  margin: 34px auto 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  display: flex;
  min-height: 400px;
  padding: 22px 20px 18px;
  border: 1px solid #e5eaf2;
  border-radius: 8px;
  background: #fff;
  color: inherit;
  cursor: pointer;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  box-shadow: 0 12px 30px rgba(31, 45, 61, .06);
  transition: transform .18s ease, box-shadow .18s ease, border-color .18s ease;
}

.feature-card:hover {
  border-color: rgba(47, 107, 255, .28);
  box-shadow: 0 18px 36px rgba(31, 45, 61, .1);
  transform: translateY(-3px);
}

.card-copy h2 {
  margin: 0 0 14px;
  color: #2d3748;
  font-size: 22px;
  line-height: 1.25;
}

.card-copy p {
  min-height: 54px;
  margin: 0;
  color: #7b8794;
  font-size: 14px;
  line-height: 1.75;
}

.card-visual {
  position: relative;
  height: 220px;
  margin: 22px 0 16px;
  border-radius: 8px;
  overflow: hidden;
}

.visual-page,
.visual-panel {
  position: absolute;
  border-radius: 6px;
  background: rgba(255, 255, 255, .84);
  box-shadow: 0 8px 22px rgba(31, 45, 61, .1);
}

.page-a {
  left: 28px;
  top: 22px;
  width: 44%;
  height: 146px;
}

.page-b {
  right: 28px;
  top: 42px;
  width: 42%;
  height: 124px;
}

.visual-panel {
  left: 50%;
  bottom: 26px;
  width: 58%;
  height: 92px;
  padding: 18px;
  transform: translateX(-50%);
}

.visual-panel i {
  display: block;
  margin-bottom: 12px;
  font-size: 28px;
}

.visual-panel em {
  display: block;
  height: 8px;
  margin-top: 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, .74);
}

.card-footer {
  display: flex;
  height: 36px;
  align-items: center;
  justify-content: space-between;
  color: #2f6bff;
  font-size: 14px;
  font-weight: 700;
}

.theme-plan .card-visual {
  background: linear-gradient(135deg, #f4efff, #ebe7ff);
}

.theme-plan .visual-panel i,
.theme-plan .card-footer {
  color: #7c4dff;
}

.theme-plan .visual-panel em {
  background: #d7c8ff;
}

.theme-tender .card-visual {
  background: linear-gradient(135deg, #eaf5ff, #eef7ff);
}

.theme-tender .visual-panel i,
.theme-tender .card-footer {
  color: #2f80ed;
}

.theme-tender .visual-panel em {
  background: #c9def8;
}

.theme-quality .card-visual {
  background: linear-gradient(135deg, #e9f8ef, #edfbf6);
}

.theme-quality .visual-panel i,
.theme-quality .card-footer {
  color: #1f9d61;
}

.theme-quality .visual-panel em {
  background: #b8ead0;
}

.theme-duplicate .card-visual {
  background: linear-gradient(135deg, #fff2de, #fff8ed);
}

.theme-duplicate .visual-panel i,
.theme-duplicate .card-footer {
  color: #f18f01;
}

.theme-duplicate .visual-panel em {
  background: #ffd89a;
}

.quick-panel {
  display: flex;
  min-height: 400px;
  padding: 16px;
  border: 1px solid #e5eaf2;
  border-radius: 8px;
  background: #fff;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  box-shadow: 0 12px 30px rgba(31, 45, 61, .06);
}

.quick-item {
  display: flex;
  min-height: 74px;
  padding: 12px 10px;
  border: 0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  align-items: center;
  gap: 14px;
  text-align: left;
  transition: background .18s ease;
}

.quick-item:hover {
  background: #f7f9ff;
}

.quick-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  font-size: 20px;
}

.quick-green {
  background: #e7f8df;
  color: #4ba72f;
}

.quick-violet {
  background: #efe8ff;
  color: #7b4dff;
}

.quick-blue {
  background: #e2f6ff;
  color: #0c9ac8;
}

.quick-orange {
  background: #fff1dc;
  color: #ee8d19;
}

.quick-item strong,
.quick-item em {
  display: block;
}

.quick-item strong {
  margin-bottom: 7px;
  color: #2d3748;
  font-size: 15px;
}

.quick-item em {
  color: #9aa4b2;
  font-size: 12px;
  font-style: normal;
  line-height: 1.45;
}

.workbench-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  max-width: 1760px;
  margin: 22px auto 0;
}

.workbench-card {
  display: grid;
  min-height: 132px;
  padding: 20px;
  border: 1px solid #e6ebf3;
  border-radius: 8px;
  background: #fff;
  color: inherit;
  cursor: pointer;
  grid-template-columns: 48px minmax(0, 1fr) 22px;
  gap: 14px;
  align-items: center;
  text-align: left;
}

.workbench-card:hover {
  border-color: rgba(47, 107, 255, .24);
}

.workbench-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.workbench-copy strong,
.workbench-copy em {
  display: block;
}

.workbench-copy strong {
  margin-bottom: 8px;
  color: #2d3748;
  font-size: 16px;
}

.workbench-copy em {
  color: #7b8794;
  font-size: 13px;
  font-style: normal;
  line-height: 1.6;
}

.bench-warm .workbench-icon {
  background: #fff1e6;
  color: #f97316;
}

.bench-blue .workbench-icon {
  background: #e8f2ff;
  color: #2563eb;
}

.bench-lilac .workbench-icon {
  background: #f0eaff;
  color: #7c3aed;
}

@media (max-width: 1360px) {
  .home-hero,
  .module-layout {
    grid-template-columns: 1fr;
  }

  .hero-status {
    max-width: 720px;
    margin: 0 auto;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-panel {
    min-height: auto;
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .bid-home-page {
    padding: 18px 12px 28px;
  }

  .hero-copy h1 {
    font-size: 26px;
  }

  .hero-actions,
  .hero-status,
  .feature-grid,
  .quick-panel,
  .workbench-row {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    display: grid;
  }

  .feature-card {
    min-height: 360px;
  }
}
</style>
