// small helper to parse a JSON block from LLM text responses
window.parseStructuredJSONFromText = function(text){
  if (!text || typeof text !== 'string') return null;
  // simple approach: look for the first occurrence of '{' that looks like JSON and try to parse until matching '}'
  const first = text.indexOf('{');
  if (first === -1) return null;
  // naive balanced-brace parse
  let depth = 0; let end = -1;
  for (let i=first;i<text.length;i++){
    if (text[i] === '{') depth++;
    if (text[i] === '}') depth--;
    if (depth === 0) { end = i; break; }
  }
  if (end === -1) return null;
  const candidate = text.slice(first, end+1);
  try { return JSON.parse(candidate); } catch(e){ return null; }
};
