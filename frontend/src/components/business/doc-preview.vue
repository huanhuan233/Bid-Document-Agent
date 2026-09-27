<script setup lang="ts">
import type { Bid } from '@/typings/bid';
import { annotationStyleMap } from '@/constants/bid';

defineProps<{
  /** 当前页码（演示样例仅第 12 页有完整内容与标注） */
  page: number;
  annotations: Bid.DocAnnotationVM[];
  /** 缩放因子 0.5-2 */
  scale: number;
  showAnnotations: boolean;
  activeId?: string | null;
  showMarginGuides?: boolean;
}>();

const emit = defineEmits<{
  select: [annotation: Bid.DocAnnotationVM | null];
}>();

const A4_W = 794;
const A4_H = 1123;
</script>

<template>
  <div class="doc-preview flex-col-center overflow-auto">
    <!-- 演示样例页（第 12 页） -->
    <div
      v-if="page === 12"
      class="doc-page relative bg-white"
      :style="{ width: `${A4_W * scale}px`, height: `${A4_H * scale}px`, fontSize: `${14 * scale}px` }"
    >
      <div
        v-if="showMarginGuides"
        class="pointer-events-none absolute inset-0"
        :style="{
          borderLeft: '1px dashed #d9a',
          borderRight: '1px dashed #d9a',
          borderTop: '1px dashed #d9a',
          borderBottom: '1px dashed #d9a',
          margin: `${94 * scale}px ${113 * scale}px`
        }"
      ></div>

      <!-- 页眉/页脚（演示布局：项目名称居中、页码居中） -->
      <div class="absolute left-0 right-0 text-center text-12px c-secondary" :style="{ top: `${40 * scale}px` }">
        XX市智慧城市建设项目招标文件
      </div>
      <div class="absolute left-0 right-0 text-center text-12px c-secondary" :style="{ bottom: `${30 * scale}px` }">
        — {{ page }} —
      </div>

      <!-- 正文 -->
      <div :style="{ padding: `${100 * scale}px ${110 * scale}px` }">
        <div class="text-center font-bold" :style="{ fontSize: `${20 * scale}px` }">第二章 投标人资格要求</div>
        <div class="mt-4" :style="{ fontSize: `${14 * scale}px`, lineHeight: 1.5, textIndent: `${28 * scale}px` }">
          <div class="mt-3"><b>2.1</b> 投标人应具备独立法人资格，具有有效的营业执照。</div>
          <div class="mt-2"><b>2.2</b> 投标人应具备计算机信息系统集成二级及以上资质，财务状况良好。</div>
          <div class="mt-2"><b>2.3</b> 近三年内（2024年1月1日至2026年12月31日）具有类似项目业绩，并提供合同复印件及验收证明。</div>
          <div class="mt-2"><b>2.4</b> 本项目不接受联合体投标。</div>
        </div>

        <div class="mt-6 text-center font-bold" :style="{ fontSize: `${18 * scale}px` }">第三章 技术要求</div>
        <div class="mt-3"><b>3.1 系统功能要求</b></div>
        <div :style="{ fontSize: `${13 * scale}px`, textIndent: `${26 * scale}px` }">
          系统应支持城市综合治理、数据共享、智能分析等功能，数据汇聚接入不少于10类数据源，具体要求如下：
        </div>
        <table
          class="mt-3 w-full border-collapse text-center"
          :style="{ fontSize: `${13 * scale}px` }"
        >
          <thead>
            <tr class="bg-primary/10">
              <th class="border border-solid border-gray-300 p-2">序号</th>
              <th class="border border-solid border-gray-300 p-2">功能模块</th>
              <th class="border border-solid border-gray-300 p-2">功能要求</th>
              <th class="border border-solid border-gray-300 p-2">备注</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="border border-solid border-gray-300 p-2">1</td>
              <td class="border border-solid border-gray-300 p-2">数据汇聚</td>
              <td class="border border-solid border-gray-300 p-2">支持多源异构数据接入，≥10类数据源</td>
              <td class="border border-solid border-gray-300 p-2">—</td>
            </tr>
            <tr>
              <td class="border border-solid border-gray-300 p-2">2</td>
              <td class="border border-solid border-gray-300 p-2">智能分析</td>
              <td class="border border-solid border-gray-300 p-2">具备数据挖掘与智能分析能力</td>
              <td class="border border-solid border-gray-300 p-2">—</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 版式标注叠层：坐标相对文档页，随缩放同步 -->
      <template v-if="showAnnotations">
        <div
          v-for="item in annotations"
          :key="item.id"
          class="absolute cursor-pointer"
          :style="{
            left: `${item.x}%`,
            top: `${item.y}%`,
            width: `${item.w}%`,
            height: `${item.h}%`,
            border: `2px solid ${annotationStyleMap[item.type].border}`,
            background: annotationStyleMap[item.type].bg,
            borderRadius: '4px',
            boxShadow: activeId === item.id ? `0 0 0 3px ${annotationStyleMap[item.type].border}` : 'none'
          }"
          @click.stop="emit('select', item)"
        >
          <span
            class="absolute -top-2 right-2 rounded-4px px-1 text-11px text-white"
            :style="{ background: annotationStyleMap[item.type].labelBg }"
          >
            {{ item.label }}
          </span>
        </div>
      </template>
    </div>

    <!-- 其他页：占位页 -->
    <div
      v-else
      class="doc-page flex-col-center bg-white"
      :style="{ width: `${A4_W * scale}px`, height: `${A4_H * scale}px` }"
    >
      <div class="text-13px c-secondary">{{ page }} 页（演示样例仅第 12 页提供版式内容）</div>
      <ElSkeleton class="mt-4 w-60%" :rows="8" animated />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.doc-page {
  box-shadow: 0 2px 12px rgb(0 21 41 / 12%);
  border-radius: 4px;
}
</style>
