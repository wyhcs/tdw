<template>
  <div class="app-container tool-home">
    <el-page-header content="AI工具" @back="$router.push('/index')" />

    <div class="tool-grid">
      <section class="tool-panel">
        <div class="panel-main">
          <i class="el-icon-picture-outline panel-icon" />
          <div>
            <h3>私人图库</h3>
            <p>管理方案写作中常用的架构图、流程图、截图和图示说明。</p>
          </div>
        </div>
        <el-button type="primary" icon="el-icon-right" @click="$router.push('/bid/tool/gallery')">进入图库</el-button>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BidToolIndex'
}
</script>

<style scoped>
.tool-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.tool-panel {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 18px;
  background: #fff;
}
.panel-main {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 18px;
}
.panel-icon {
  font-size: 36px;
  color: #409eff;
}
.panel-main h3 {
  margin: 0 0 8px;
  color: #303133;
}
.panel-main p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}
</style>
