// Client helper to call server-side free search augmentation
export async function askWithFreeSearch(prompt, origin='', destination=''){
  const resp = await fetch('/api/llm/search-free', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({prompt, origin, destination})
  });
  const data = await resp.json();
  return data;
}
