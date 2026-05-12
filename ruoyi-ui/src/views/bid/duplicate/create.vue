<template>
  <div class="app-container duplicate-create">
    <el-page-header content="新建方案查重" @back="$router.push('/bid/duplicate')" />

    <el-form ref="form" :model="form" :rules="rules" label-width="120px" class="create-form">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="form.taskName" placeholder="请输入任务名称" />
      </el-form-item>
      <el-form-item label="待查重方案" prop="bidId">
        <el-select v-model="form.bidId" filterable placeholder="请选择方案" class="wide-control">
          <el-option v-for="item in bidList" :key="item.id" :label="item.title" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="比对方式" prop="compareMode">
        <el-radio-group v-model="compareMode">
          <el-radio-button label="file">上传文件</el-radio-button>
          <el-radio-button label="bid">选择方案</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="compareMode === 'bid'" label="比对方案" prop="compareBidId">
        <el-select v-model="form.compareBidId" filterable placeholder="请选择比对方案" class="wide-control">
          <el-option v-for="item in bidList" :key="item.id" :label="item.title" :value="item.id" :disabled="item.id === form.bidId" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="compareMode === 'file'" label="比对文件">
        <el-upload action="#" :auto-upload="false" :limit="1" :on-change="handleFileChange" :on-remove="handleFileRemove">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitForm">创建并执行查重</el-button>
        <el-button @click="$router.push('/bid/duplicate')">取消</el-button>
      </el-form-item>
    </el-form>

    <el-dialog title="查重排队中" :visible.sync="queueDialogVisible" width="360px" append-to-body :show-close="false">
      <div class="queue-state">
        <i class="el-icon-loading" />
        <span>正在拼接大纲与内容块文本，并执行 Mock 查重...</span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBids } from '@/api/bid/bids'
import { createDuplicateTask, runDuplicateTask } from '@/api/bid/duplicate'

export default {
  name: 'BidDuplicateCreate',
  data() {
    return {
      submitting: false,
      queueDialogVisible: false,
      bidList: [],
      compareMode: 'file',
      uploadFile: null,
      form: {
        taskName: '',
        bidId: undefined,
        compareBidId: undefined,
        remark: ''
      },
      rules: {
        bidId: [{ required: true, message: '请选择待查重方案', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadBids()
  },
  methods: {
    loadBids() {
      listBids({ pageNum: 1, pageSize: 100 }).then(res => {
        this.bidList = res.rows || []
      })
    },
    handleFileChange(file) {
      this.uploadFile = file.raw
    },
    handleFileRemove() {
      this.uploadFile = null
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.compareMode === 'bid' && !this.form.compareBidId) {
          this.$modal.msgWarning('请选择比对方案')
          return
        }
        if (this.compareMode === 'file' && !this.uploadFile) {
          this.$modal.msgWarning('请选择比对文件')
          return
        }
        const data = new FormData()
        data.append('bidId', this.form.bidId)
        if (this.form.taskName) {
          data.append('taskName', this.form.taskName)
        }
        if (this.compareMode === 'bid') {
          data.append('compareBidId', this.form.compareBidId)
        }
        if (this.compareMode === 'file' && this.uploadFile) {
          data.append('file', this.uploadFile)
        }
        this.submitting = true
        this.queueDialogVisible = true
        createDuplicateTask(data).then(res => {
          const task = res.data || {}
          return runDuplicateTask(task.id).then(() => task)
        }).then(task => {
          this.$modal.msgSuccess('查重完成')
          this.$router.push({ path: '/bid/duplicate/result', query: { taskId: task.id, bidId: task.bidId }})
        }).finally(() => {
          this.submitting = false
          this.queueDialogVisible = false
        })
      })
    }
  }
}
</script>

<style scoped>
.create-form {
  max-width: 720px;
  margin-top: 24px;
}
.wide-control {
  width: 100%;
}
.queue-state {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
</style>
