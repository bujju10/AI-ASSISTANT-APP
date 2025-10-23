// LLM Chat and Route Suggestion JS
async function getLLMSuggestion(prompt) {
    const response = await fetch('/api/llm/route-suggest', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt })
    });
    let data;
    try {
        data = await response.json();
    } catch (e) {
        throw new Error('Invalid JSON from LLM endpoint');
    }
    if (!response.ok) {
        const err = data && data.error ? data.error : 'LLM endpoint error';
        throw new Error(err);
    }
    // LLM Studio returns { choices: [{ message: { content: ... } }] }
    return data.choices ? data.choices[0].message.content : (data.error || 'No response from LLM');
}

async function chatWithLLM(prompt) {
    const response = await fetch('/api/llm/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt })
    });
    let data;
    try {
        data = await response.json();
    } catch (e) {
        throw new Error('Invalid JSON from LLM endpoint');
    }
    if (!response.ok) {
        const err = data && data.error ? data.error : 'LLM endpoint error';
        throw new Error(err);
    }
    return data.choices ? data.choices[0].message.content : (data.error || 'No response from LLM');
}
