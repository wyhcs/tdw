<template>
  <bid-editor-workspace
    v-if="bidId && loaded"
    :bid-id="bidId"
    back-path="/bid/tender"
    module-type="tender"
    export-name="标书"
    title-suffix="AI标书三级大纲与内容编辑器"
    :tender-parse-report-id="parseReportId"
    :focus-outline-id="$route.query.outlineId"
    :focus-content-id="$route.query.contentId"
  />
</template>

<script>
import BidEditorWorkspace from '@/views/bid/components/BidEditorWorkspace.vue'
import { getLatestTenderReport } from '@/api/bid/tender'

export default {
  name: 'BidTenderEditor',
  components: { BidEditorWorkspace },
  data() {
    return { loaded: false, parseReportId: undefined }
  },
  computed: {
    bidId() {
      return this.$route.query.bidId || this.$route.params.bidId
    }
  },
  created() {
    this.parseReportId = this.$route.query.parseReportId
    if (this.parseReportId || !this.bidId) {
      this.loaded = true
      return
    }
    getLatestTenderReport(this.bidId).then(res => {
      const report = res.data || {}
      this.parseReportId = report.id
    }).finally(() => {
      this.loaded = true
    })
  }
}
</script>
