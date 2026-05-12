<template>
  <div class="app-container tender-create-page">
    <el-page-header @back="$router.push('/bid/tender')" content="新建AI标书" />
    <el-card shadow="never" class="form-card">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="项目名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入标书项目名称" />
        </el-form-item>
        <el-form-item label="标书类型" prop="category">
          <el-select v-model="form.category" placeholder="请选择类型" clearable>
            <el-option label="服务" value="服务" />
            <el-option label="货物" value="货物" />
            <el-option label="工程" value="工程" />
            <el-option label="IT信息" value="IT信息" />
            <el-option label="其它" value="其它" />
          </el-select>
        </el-form-item>
        <el-form-item label="招标文件" prop="file">
          <el-upload action="#" :auto-upload="false" :limit="1" :on-change="handleFileChange" :on-remove="handleFileRemove" :file-list="fileList">
            <el-button size="small" type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" placeholder="可填写项目背景、响应重点等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">创建并进入解析</el-button>
          <el-button @click="$router.push('/bid/tender')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createTender } from '@/api/bid/tender'

export default {
  name: 'BidTenderCreate',
  data() {
    return {
      submitting: false,
      fileList: [],
      form: { title: '', category: '', note: '', file: null },
      rules: {
        title: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        file: [{ required: true, message: '请上传招标文件', trigger: 'change' }]
      }
    }
  },
  methods: {
    handleFileChange(file, fileList) {
      this.fileList = fileList.slice(-1)
      this.form.file = file.raw
    },
    handleFileRemove() {
      this.fileList = []
      this.form.file = null
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        createTender(this.form).then(res => {
          const data = res.data || {}
          const bid = data.bid || {}
          const file = data.file || {}
          this.$modal.msgSuccess('标书项目已创建')
          this.$router.push({ path: '/bid/tender/parse', query: { bidId: bid.id, fileId: file.id }})
        }).finally(() => { this.submitting = false })
      })
    }
  }
}
</script>

<style scoped>
.form-card { max-width: 760px; margin-top: 16px; }
</style>
