const apiBase = '/api/todos';

const elForm = document.getElementById('create-form');
const elTitle = document.getElementById('title');
const elDescription = document.getElementById('description');
const elCategory = document.getElementById('category');
const elPriority = document.getElementById('priority');
const elDueDate = document.getElementById('dueDate');
const elStatus = document.getElementById('status');
const elList = document.getElementById('todo-list');
const elRefresh = document.getElementById('refresh-btn');
const elCategoryFilter = document.getElementById('category-filter');
const elSort = document.getElementById('sort');

/**
 * 设置页面顶部的状态提示文本。
 *
 * @param {string} text 提示文本
 * @param {'info'|'success'|'error'} type 提示类型（影响样式）
 */
function setStatus(text, type = 'info') {
  elStatus.textContent = text || '';
  elStatus.dataset.type = type;
}

/**
 * 统一封装对后端 API 的请求。
 *
 * - 自动附加 JSON Content-Type
 * - 兼容 204 No Content 响应
 * - 非 2xx 时抛出 Error，优先使用后端返回的 message 字段
 *
 * @param {string} path 请求路径（例如 /api/todos）
 * @param {RequestInit} options fetch 选项（method/body 等）
 * @returns {Promise<any>} JSON 响应体；若 204 则返回 null
 */
async function apiRequest(path, options = {}) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    const message = body && body.message ? body.message : `HTTP ${res.status}`;
    throw new Error(message);
  }
  return body;
}

/**
 * 将单个待办对象渲染为列表项 DOM 节点，并绑定按钮事件。
 *
 * @param {{id:string,title:string,description?:string,completed:boolean}} item 待办对象
 * @returns {HTMLLIElement} 列表项节点
 */
function renderItem(item) {
  const li = document.createElement('li');
  li.className = 'item';
  if (item.completed) li.classList.add('completed');

  const left = document.createElement('div');
  left.className = 'left';

  const title = document.createElement('div');
  title.className = 'title';
  title.textContent = item.title;

  const meta = document.createElement('div');
  meta.className = 'meta';
  const bits = [];
  if (item.category) bits.push(item.category);
  if (item.priority) {
    const p = Number(item.priority);
    const label = p === 3 ? '高' : p === 2 ? '中' : p === 1 ? '低' : String(item.priority);
    bits.push(`优先级:${label}`);
  }
  if (item.dueDate) bits.push(`截止:${String(item.dueDate).replace('T', ' ')}`);
  if (item.description) bits.push(item.description);
  meta.textContent = bits.join(' · ');

  left.appendChild(title);
  left.appendChild(meta);

  const right = document.createElement('div');
  right.className = 'right';

  const toggle = document.createElement('button');
  toggle.type = 'button';
  toggle.textContent = item.completed ? '设为未完成' : '设为完成';
  toggle.addEventListener('click', async () => {
    try {
      await apiRequest(`${apiBase}/${item.id}/toggle`, { method: 'PATCH' });
      await loadTodos();
    } catch (e) {
      setStatus(e.message, 'error');
    }
  });

  const del = document.createElement('button');
  del.type = 'button';
  del.className = 'danger';
  del.textContent = '删除';
  del.addEventListener('click', async () => {
    try {
      await apiRequest(`${apiBase}/${item.id}`, { method: 'DELETE' });
      await loadTodos();
    } catch (e) {
      setStatus(e.message, 'error');
    }
  });

  right.appendChild(toggle);
  right.appendChild(del);

  li.appendChild(left);
  li.appendChild(right);
  return li;
}

/**
 * 从后端拉取待办列表并刷新页面展示。
 *
 * - 先清空列表再重建
 * - 空列表展示占位文案
 * - 捕获异常并显示错误提示
 */
async function loadTodos() {
  setStatus('加载中…');
  try {
    const selectedCategory = elCategoryFilter && elCategoryFilter.value ? elCategoryFilter.value : '';
    const selectedSort = elSort && elSort.value ? elSort.value : '';
    const params = new URLSearchParams();
    if (selectedCategory) params.set('category', selectedCategory);
    if (selectedSort) params.set('sort', selectedSort);
    const listUrl = params.toString() ? `${apiBase}?${params.toString()}` : apiBase;
    const list = await apiRequest(listUrl);
    elList.innerHTML = '';
    if (!list || list.length === 0) {
      const empty = document.createElement('li');
      empty.className = 'empty';
      empty.textContent = '暂无待办事项';
      elList.appendChild(empty);
    } else {
      for (const item of list) {
        elList.appendChild(renderItem(item));
      }
    }
    if (elCategoryFilter) {
      const categories = await apiRequest(`${apiBase}/categories`);
      const keep = elCategoryFilter.value;
      elCategoryFilter.innerHTML = '';
      const all = document.createElement('option');
      all.value = '';
      all.textContent = '全部分类';
      elCategoryFilter.appendChild(all);
      for (const c of categories || []) {
        const opt = document.createElement('option');
        opt.value = c;
        opt.textContent = c;
        elCategoryFilter.appendChild(opt);
      }
      elCategoryFilter.value = keep;
    }
    setStatus('');
  } catch (e) {
    setStatus(e.message, 'error');
  }
}

/**
 * 处理“新增待办”表单提交。
 *
 * - 阻止默认提交
 * - 调用创建接口
 * - 成功后清空输入并刷新列表
 */
elForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  setStatus('提交中…');
  try {
    const title = elTitle.value;
    const description = elDescription.value;
    const category = elCategory ? elCategory.value : '';
    const priority = elPriority && elPriority.value ? Number(elPriority.value) : null;
    const dueDate = elDueDate ? elDueDate.value : '';
    await apiRequest(apiBase, {
      method: 'POST',
      body: JSON.stringify({ title, description, category, priority, dueDate }),
    });
    elTitle.value = '';
    elDescription.value = '';
    if (elCategory) elCategory.value = '';
    if (elPriority) elPriority.value = '';
    if (elDueDate) elDueDate.value = '';
    await loadTodos();
    setStatus('已添加', 'success');
    setTimeout(() => setStatus(''), 1200);
  } catch (err) {
    setStatus(err.message, 'error');
  }
});

/**
 * 刷新按钮：重新加载列表。
 */
elRefresh.addEventListener('click', () => loadTodos());

if (elCategoryFilter) {
  elCategoryFilter.addEventListener('change', () => loadTodos());
}

if (elSort) {
  elSort.addEventListener('change', () => loadTodos());
}

/**
 * 页面初始化：首次加载列表。
 */
loadTodos();
