<template>
  <div class="app-container plan-create-page">
    <el-page-header @back="$router.push('/bid/plan')" content="新建AI方案" />

    <el-card shadow="never" class="form-card">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="plan-form">
        <el-form-item label="方案名称" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入方案名称" />
        </el-form-item>
        <el-form-item label="方案类型" prop="category">
          <el-select v-model="form.category" placeholder="请选择方案类型">
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板" prop="templateId">
          <el-select v-model="form.templateId" clearable placeholder="模板选择占位，后续接入知识库模板">
            <el-option label="暂不使用模板" :value="null" />
            <el-option label="通用方案模板（占位）" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="4" maxlength="255" show-word-limit placeholder="请输入备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">创建并进入编辑器</el-button>
          <el-button @click="$router.push('/bid/plan')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { addBids } from '@/api/bid/bids'

export default {
  name: 'BidPlanCreate',
  data() {
    return {
      submitting: false,
      form: {
        title: '',
        category: '',
        templateId: null,
        status: 1,
        note: ''
      },
      categoryOptions: [
        { label: '服务', value: '服务' },
        { label: '货物', value: '货物' },
        { label: '工程', value: '工程' },
        { label: '监理', value: '监理' },
        { label: 'IT信息', value: 'IT信息' },
        { label: '其它', value: '其它' }
      ],
      rules: {
        title: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }],
        category: [{ required: true, message: '请选择方案类型', trigger: 'change' }]
      }
    }
  },
  methods: {
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        addBids(this.form).then(res => {
          const bid = res.data || {}
          this.$modal.msgSuccess('新建成功')
          this.$router.replace({ path: '/bid/plan/editor', query: { bidId: bid.id } })
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style scoped>
.form-card {
  margin-top: 16px;
  max-width: 780px;
}
.plan-form .el-select {
  width: 100%;
}
</style>
