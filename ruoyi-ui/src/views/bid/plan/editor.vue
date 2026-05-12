<template>
  <bid-editor-workspace
    v-if="bidId"
    :bid-id="bidId"
    back-path="/bid/plan"
    module-type="plan"
    export-name="方案"
    title-suffix="AI方案三级大纲与内容编辑器"
    :focus-outline-id="$route.query.outlineId"
    :focus-content-id="$route.query.contentId"
  />
</template>

<script>
import BidEditorWorkspace from '@/views/bid/components/BidEditorWorkspace.vue'

export default {
  name: 'BidPlanEditor',
  components: { BidEditorWorkspace },
  computed: {
    bidId() {
      return this.$route.query.bidId || this.$route.params.bidId
    }
  }
}
</script>
