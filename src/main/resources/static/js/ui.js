// small toast helper
(function(){
  function ensureWrap(){
    let w = document.querySelector('.toast-wrap');
    if (!w) { w = document.createElement('div'); w.className='toast-wrap'; document.body.appendChild(w); }
    return w;
  }
  window.uiToast = function(msg,type='info',timeout=3500){
    const w = ensureWrap();
    const t = document.createElement('div'); t.className='toast '+type; t.innerHTML = `<span>${msg}</span><span class='close'>&times;</span>`;
    w.appendChild(t);
    t.querySelector('.close').addEventListener('click', ()=> t.remove());
    if (timeout>0) setTimeout(()=> t.remove(), timeout);
  }
})();
