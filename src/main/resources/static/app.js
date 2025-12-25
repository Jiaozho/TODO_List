const apiBase = '/api/todos';

const elForm = document.getElementById('create-form');
const elTitle = document.getElementById('title');
const elDescription = document.getElementById('description');
const elStatus = document.getElementById('status');
const elList = document.getElementById('todo-list');
const elRefresh = document.getElementById('refresh-btn');

function setStatus(text, type = 'info') {
  elStatus.textContent = text || '';
  elStatus.dataset.type = type;
}

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
  meta.textContent = item.description ? item.description : '';

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

async function loadTodos() {
  setStatus('加载中…');
  try {
    const list = await apiRequest(apiBase);
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
    setStatus('');
  } catch (e) {
    setStatus(e.message, 'error');
  }
}

elForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  setStatus('提交中…');
  try {
    const title = elTitle.value;
    const description = elDescription.value;
    await apiRequest(apiBase, {
      method: 'POST',
      body: JSON.stringify({ title, description }),
    });
    elTitle.value = '';
    elDescription.value = '';
    await loadTodos();
    setStatus('已添加', 'success');
    setTimeout(() => setStatus(''), 1200);
  } catch (err) {
    setStatus(err.message, 'error');
  }
});

elRefresh.addEventListener('click', () => loadTodos());

loadTodos();

