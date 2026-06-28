<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitLocationEvent, type LocationEventPayload } from '@/api/stip'
import { buildSimulatedLocationEvent } from '@/utils/simulatedData'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submitted: []
}>()

const form = reactive<LocationEventPayload>(buildSimulatedLocationEvent())

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      fillSimulatedData()
    }
  },
)

function fillSimulatedData() {
  Object.assign(form, buildSimulatedLocationEvent())
}

async function submit() {
  const result = await submitLocationEvent({ ...form })
  ElMessage.success(result.accepted ? '位置事件已接入' : '重复事件已忽略')
  emit('update:modelValue', false)
  emit('submitted')
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="接入位置事件" width="640px" @update:model-value="emit('update:modelValue', $event)">
    <el-form label-width="110px" :model="form">
      <el-form-item label="事件 ID">
        <el-input v-model="form.eventId" />
      </el-form-item>
      <el-form-item label="实体编码">
        <el-input v-model="form.entityCode" />
      </el-form-item>
      <el-form-item label="实体类型">
        <el-select v-model="form.entityType">
          <el-option label="车辆" value="VEHICLE" />
          <el-option label="人员" value="PERSON" />
          <el-option label="设备" value="DEVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="数据来源">
        <el-select v-model="form.sourceType">
          <el-option label="GPS" value="GPS" />
          <el-option label="Wi-Fi" value="WIFI" />
          <el-option label="蓝牙" value="BLUETOOTH" />
          <el-option label="传感器" value="SENSOR" />
        </el-select>
      </el-form-item>
      <div class="form-grid">
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.0001" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.0001" />
        </el-form-item>
        <el-form-item label="速度">
          <el-input-number v-model="form.speed" :precision="1" :step="1" />
        </el-form-item>
        <el-form-item label="方向">
          <el-input-number v-model="form.direction" :precision="1" :step="1" />
        </el-form-item>
      </div>
      <el-form-item label="事件时间">
        <el-input v-model="form.eventTime" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="fillSimulatedData">填充模拟数据</el-button>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="submit">提交事件</el-button>
    </template>
  </el-dialog>
</template>

