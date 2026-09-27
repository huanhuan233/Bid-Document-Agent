import type { Bid } from '@/typings/bid';

/** 素材目录树 */
export const demoMaterialDirectories: Bid.MaterialDirectoryVM[] = [
  {
    id: 'D-Qual', name: '企业资质', count: 4,
    children: [
      { id: 'D-Qual-Biz', name: '营业执照', count: 2 },
      { id: 'D-Qual-Cert', name: '资质证书', count: 2 }
    ]
  },
  {
    id: 'D-Perf', name: '项目业绩', count: 3,
    children: [
      { id: 'D-Perf-Gov', name: '政府项目', count: 2 },
      { id: 'D-Perf-Bid', name: '投标业绩', count: 1 }
    ]
  },
  { id: 'D-Person', name: '人员证书', count: 2 },
  { id: 'D-Scheme', name: '制度方案', count: 2 },
  { id: 'D-Chart', name: '图表图片', count: 2 },
  { id: 'D-Attach', name: '常用附件', count: 2 }
];

/** 企业素材（演示数据；有效期状态按当前日期动态计算） */
const rawMaterials: Omit<Bid.MaterialVM, 'status'>[] = [
  {
    id: 'M-001', name: '企业营业执照.pdf', type: '资质文件', directory: '企业资质/营业执照', tags: ['营业执照', '基础证照'],
    projects: '全部项目', updatedAt: '2026-09-19 14:32', expiry: '2031-12-31', refs: 152, recommended: true,
    sizeLabel: '2.4 MB', version: 'V3', uploader: '张三', previewKind: 'pdf',
    usage: [
      { scene: '1.1 企业基本情况 · 企业资质介绍', user: '李四', time: '2026-09-20 10:24' },
      { scene: '3.2 资格响应 · 法人证明', user: '王五', time: '2026-09-18 16:10' }
    ]
  },
  {
    id: 'M-002', name: 'ISO9001质量管理体系认证.pdf', type: '资质文件', directory: '企业资质/资质证书', tags: ['ISO认证', '质量管理'],
    projects: '全部项目', updatedAt: '2026-09-18 11:20', expiry: '2027-05-20', refs: 86, recommended: false,
    sizeLabel: '1.1 MB', version: 'V2', uploader: '李四', previewKind: 'pdf',
    usage: [{ scene: '3.1 技术管理 · 质量保障', user: '李四', time: '2026-09-19 10:18' }]
  },
  {
    id: 'M-003', name: '安全生产许可证.pdf', type: '资质文件', directory: '企业资质/资质证书', tags: ['安全生产', '许可证'],
    projects: '全部项目', updatedAt: '2026-09-16 09:08', expiry: '2027-03-15', refs: 112, recommended: false,
    sizeLabel: '0.9 MB', version: 'V1', uploader: '张三', previewKind: 'pdf',
    usage: [{ scene: '5.1 安全管理 · 安全资质', user: '赵六', time: '2026-09-12 14:30' }]
  },
  {
    id: 'M-004', name: '智慧园区数据中台项目业绩.docx', type: '业绩证明', directory: '项目业绩/政府项目', tags: ['数据中台', '项目业绩'],
    projects: '智慧城市类项目', updatedAt: '2026-09-15 16:24', expiry: null, refs: 64, recommended: true,
    sizeLabel: '3.2 MB', version: 'V2', uploader: '王五', previewKind: 'docx',
    usage: [{ scene: '2.1 项目经验 · 同类业绩', user: '王五', time: '2026-09-21 09:15' }]
  },
  {
    id: 'M-005', name: '近三年企业业绩清单.xlsx', type: '业绩证明', directory: '项目业绩/投标业绩', tags: ['业绩汇总'],
    projects: '全部项目', updatedAt: '2026-09-12 10:40', expiry: null, refs: 94, recommended: false,
    sizeLabel: '0.6 MB', version: 'V5', uploader: '李四', previewKind: 'other',
    usage: [{ scene: '2.1 项目经验 · 业绩汇总表', user: '张三', time: '2026-09-20 15:22' }]
  },
  {
    id: 'M-006', name: '注册建造师证书（一级）.pdf', type: '人员证书', directory: '人员证书', tags: ['一级建造师', '项目经理'],
    projects: '全部项目', updatedAt: '2026-09-14 14:12', expiry: '2027-08-30', refs: 73, recommended: true,
    sizeLabel: '1.8 MB', version: 'V1', uploader: '赵六', previewKind: 'pdf',
    usage: [{ scene: '4.2 项目团队 · 项目经理资质', user: '赵六', time: '2026-09-19 11:46' }]
  },
  {
    id: 'M-007', name: '中级职称证书汇编.pdf', type: '人员证书', directory: '人员证书', tags: ['职称', '技术人员'],
    projects: '全部项目', updatedAt: '2026-09-10 09:36', expiry: '2026-10-20', refs: 41, recommended: false,
    sizeLabel: '4.5 MB', version: 'V2', uploader: '李四', previewKind: 'pdf',
    usage: [{ scene: '4.2 项目团队 · 技术负责人', user: '李四', time: '2026-09-19 09:41' }]
  },
  {
    id: 'M-008', name: '质量管理制度.pdf', type: '制度文件', directory: '制度方案', tags: ['质量管理', '制度'],
    projects: '全部项目', updatedAt: '2026-09-08 15:30', expiry: null, refs: 38, recommended: false,
    sizeLabel: '2.1 MB', version: 'V1', uploader: '王五', previewKind: 'pdf',
    usage: [{ scene: '5.1 质量管理 · 管理制度', user: '王五', time: '2026-09-18 10:12' }]
  },
  {
    id: 'M-009', name: '数据中台架构图.png', type: '图表图片', directory: '图表图片', tags: ['架构图', '数据中台'],
    projects: '智慧城市类项目', updatedAt: '2026-09-06 17:18', expiry: null, refs: 126, recommended: true,
    sizeLabel: '860 KB', version: 'V3', uploader: '赵六', previewKind: 'image',
    usage: [{ scene: '3.1 总体架构 · 技术架构图', user: '赵六', time: '2026-09-21 14:05' }]
  },
  {
    id: 'M-010', name: '售后服务承诺书.docx', type: '常用附件', directory: '常用附件', tags: ['售后', '承诺函'],
    projects: '全部项目', updatedAt: '2026-09-05 13:50', expiry: null, refs: 89, recommended: false,
    sizeLabel: '420 KB', version: 'V2', uploader: '张三', previewKind: 'docx',
    usage: [{ scene: '6.1 售后服务 · 服务承诺', user: '张三', time: '2026-09-20 16:44' }]
  },
  {
    id: 'M-011', name: '投标文件格式附件.docx', type: '常用附件', directory: '常用附件', tags: ['格式', '附件模板'],
    projects: '全部项目', updatedAt: '2026-09-03 10:05', expiry: null, refs: 56, recommended: false,
    sizeLabel: '310 KB', version: 'V1', uploader: '李四', previewKind: 'docx',
    usage: [{ scene: '7.1 附件编制 · 格式规范', user: '李四', time: '2026-09-19 08:50' }]
  },
  {
    id: 'M-012', name: '公司宣传册（演示样例）.pdf', type: '图表图片', directory: '图表图片', tags: ['宣传', '公司介绍'],
    projects: '全部项目', updatedAt: '2026-08-28 14:00', expiry: null, refs: 22, recommended: false,
    sizeLabel: '12.8 MB', version: 'V1', uploader: '王五', previewKind: 'pdf',
    usage: [{ scene: '1.2 公司介绍 · 企业概况', user: '王五', time: '2026-09-10 11:30' }]
  }
];

/** 按当前日期动态计算有效期状态，避免演示数据随时间推移出现「已过期仍显示有效」的矛盾 */
function computeStatus(expiry: string | null): Bid.MaterialVM['status'] {
  if (!expiry) return '有效';
  const days = (new Date(expiry).getTime() - Date.now()) / 86400000;
  if (days < 0) return '已过期';
  if (days <= 30) return '即将过期';
  return '有效';
}

export const demoMaterials: Bid.MaterialVM[] = rawMaterials.map(item => ({ ...item, status: computeStatus(item.expiry) }));

/** 素材统计（与列表数据一致） */
export const demoMaterialStats = {
  total: 1258,
  expiringSoon: 2,
  hotRefs: 3,
  monthNew: 12
};

/** 素材分类 Tab */
export const materialCategoryTabs = [
  { key: 'all', label: '全部素材' },
  { key: '企业资质', label: '企业资质' },
  { key: '项目业绩', label: '项目业绩' },
  { key: '人员证书', label: '人员证书' },
  { key: '制度方案', label: '制度方案' },
  { key: '图表图片', label: '图表图片' },
  { key: '常用附件', label: '常用附件' }
];
