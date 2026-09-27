<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import WangEditor from 'wangeditor';

const props = defineProps<{
  modelValue: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  change: [];
}>();

const domRef = ref<HTMLElement>();
let editor: WangEditor | null = null;

function renderEditor() {
  if (!domRef.value) return;
  editor = new WangEditor(domRef.value);
  editor.config.height = 420;
  editor.config.zIndex = 10;
  editor.config.placeholder = '编辑章节正文…';
  editor.config.onchange = () => {
    if (!editor) return;
    const html = editor.txt.html() || '';
    if (html !== props.modelValue) {
      emit('update:modelValue', html);
      emit('change');
    }
  };
  editor.create();
  editor.txt.html(props.modelValue || '');
}

watch(
  () => props.modelValue,
  v => {
    if (editor && (editor.txt.html() || '') !== v) {
      editor.txt.html(v || '');
    }
  }
);

onMounted(() => {
  renderEditor();
});

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy();
    editor = null;
  }
});
</script>

<template>
  <div>
    <div ref="domRef" class="bg-white dark:bg-dark" />
  </div>
</template>
