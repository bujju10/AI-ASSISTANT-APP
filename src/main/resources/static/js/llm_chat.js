// Enhanced LLM Chat and Route Suggestion with Error Handling and UI Feedback
class SmartLLM {
    constructor() {
        this.isLoading = false;
        this.chatHistory = [];
        this.init();
    }

    init() {
        // Add loading states and error handling
        this.setupErrorHandling();
    }

    setupErrorHandling() {
        // Global error handler for fetch requests
        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.showError('An unexpected error occurred. Please try again.');
        });
    }

    async getLLMSuggestion(prompt) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            const response = await fetch('/api/llm/route-suggest', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ prompt })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (!data || (!data.choices && !data.error)) {
                throw new Error('Invalid response format from server');
            }

            if (data.error) {
                throw new Error(data.error);
            }

            // Handle different response formats
            if (data.choices && data.choices[0] && data.choices[0].message) {
                return data.choices[0].message.content;
            } else if (data.response) {
                return data.response;
            } else if (data.content) {
                return data.content;
            } else {
                throw new Error('No valid response content found');
            }

        } catch (error) {
            console.error('LLM Suggestion Error:', error);
            throw new Error(`Failed to get AI suggestion: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    async getEnhancedLLMSuggestion(prompt, origin, destination) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            // Use the search-free endpoint that includes internet data
            const response = await fetch('/api/llm/search-free', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ 
                    prompt,
                    origin,
                    destination
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (!data || (!data.choices && !data.error)) {
                throw new Error('Invalid response format from server');
            }

            if (data.error) {
                throw new Error(data.error);
            }

            // Handle different response formats
            if (data.choices && data.choices[0] && data.choices[0].message) {
                return data.choices[0].message.content;
            } else if (data.response) {
                return data.response;
            } else if (data.content) {
                return data.content;
            } else {
                throw new Error('No valid response content found');
            }

        } catch (error) {
            console.error('Enhanced LLM Suggestion Error:', error);
            throw new Error(`Failed to get enhanced AI suggestion: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    async getSmartRouteAnalysis(start, end) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            const response = await fetch('/api/enhanced-llm/smart-route-analysis', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ 
                    start,
                    end
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (data.error) {
                throw new Error(data.error);
            }

            return data;

        } catch (error) {
            console.error('Smart Route Analysis Error:', error);
            throw new Error(`Failed to get smart route analysis: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    async getSmartChat(prompt, start, end) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            const response = await fetch('/api/enhanced-llm/smart-chat', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ 
                    prompt,
                    start,
                    end
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (data.error) {
                throw new Error(data.error);
            }

            return data.content || data.response || data;

        } catch (error) {
            console.error('Smart Chat Error:', error);
            throw new Error(`Failed to get smart chat response: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    async chatWithLLM(prompt) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            // Add to chat history
            this.chatHistory.push({ role: 'user', content: prompt });
            
            const response = await fetch('/api/llm/chat', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ 
                    prompt,
                    history: this.chatHistory.slice(-10) // Send last 10 messages for context
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (!data || (!data.choices && !data.error)) {
                throw new Error('Invalid response format from server');
            }

            if (data.error) {
                throw new Error(data.error);
            }

            let responseText;
            // Handle different response formats
            if (data.choices && data.choices[0] && data.choices[0].message) {
                responseText = data.choices[0].message.content;
            } else if (data.response) {
                responseText = data.response;
            } else if (data.content) {
                responseText = data.content;
            } else {
                throw new Error('No valid response content found');
            }

            // Add to chat history
            this.chatHistory.push({ role: 'assistant', content: responseText });
            
            return responseText;

        } catch (error) {
            console.error('LLM Chat Error:', error);
            throw new Error(`Failed to chat with AI: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    async chatWithLLMEnhanced(prompt, origin, destination) {
        if (this.isLoading) {
            throw new Error('Another request is already in progress');
        }

        this.isLoading = true;
        
        try {
            // Add to chat history
            this.chatHistory.push({ role: 'user', content: prompt });
            
            const response = await fetch('/api/llm/chat', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ 
                    prompt,
                    origin,
                    destination,
                    history: this.chatHistory.slice(-10) // Send last 10 messages for context
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (!data || (!data.choices && !data.error)) {
                throw new Error('Invalid response format from server');
            }

            if (data.error) {
                throw new Error(data.error);
            }

            let responseText;
            // Handle different response formats
            if (data.choices && data.choices[0] && data.choices[0].message) {
                responseText = data.choices[0].message.content;
            } else if (data.response) {
                responseText = data.response;
            } else if (data.content) {
                responseText = data.content;
            } else {
                throw new Error('No valid response content found');
            }

            // Add to chat history
            this.chatHistory.push({ role: 'assistant', content: responseText });
            
            return responseText;

        } catch (error) {
            console.error('Enhanced LLM Chat Error:', error);
            throw new Error(`Failed to chat with enhanced AI: ${error.message}`);
        } finally {
            this.isLoading = false;
        }
    }

    showError(message) {
        if (typeof uiToast === 'function') {
            uiToast(message, 'error');
        } else {
            console.error(message);
        }
    }

    showSuccess(message) {
        if (typeof uiToast === 'function') {
            uiToast(message, 'success');
        } else {
            console.log(message);
        }
    }

    showInfo(message) {
        if (typeof uiToast === 'function') {
            uiToast(message, 'info');
        } else {
            console.log(message);
        }
    }

    getChatHistory() {
        return this.chatHistory;
    }

    clearChatHistory() {
        this.chatHistory = [];
    }

    isLoading() {
        return this.isLoading;
    }
}

// Create global instance
window.smartLLM = new SmartLLM();

// Legacy function exports for backward compatibility
async function getLLMSuggestion(prompt) {
    return await window.smartLLM.getLLMSuggestion(prompt);
}

async function getEnhancedLLMSuggestion(prompt, origin, destination) {
    return await window.smartLLM.getEnhancedLLMSuggestion(prompt, origin, destination);
}

async function chatWithLLM(prompt) {
    return await window.smartLLM.chatWithLLM(prompt);
}

async function chatWithLLMEnhanced(prompt, origin, destination) {
    return await window.smartLLM.chatWithLLMEnhanced(prompt, origin, destination);
}

async function getSmartRouteAnalysis(start, end) {
    return await window.smartLLM.getSmartRouteAnalysis(start, end);
}

async function getSmartChat(prompt, start, end) {
    return await window.smartLLM.getSmartChat(prompt, start, end);
}

// Enhanced UI utilities
class SmartUI {
    constructor() {
        this.init();
    }

    init() {
        this.setupFormValidation();
        this.setupLoadingStates();
        this.setupAnimations();
    }

    setupFormValidation() {
        // Add real-time validation to forms
        document.addEventListener('input', (e) => {
            if (e.target.matches('input[required], textarea[required], select[required]')) {
                this.validateField(e.target);
            }
        });
    }

    validateField(field) {
        const isValid = field.checkValidity();
        const container = field.closest('.form-row');
        
        if (container) {
            if (isValid) {
                container.classList.remove('error');
                container.classList.add('valid');
            } else {
                container.classList.remove('valid');
                container.classList.add('error');
            }
        }
        
        return isValid;
    }

    setupLoadingStates() {
        // Add loading states to buttons
        document.addEventListener('submit', (e) => {
            const form = e.target;
            const submitBtn = form.querySelector('button[type="submit"]');
            
            if (submitBtn && !submitBtn.disabled) {
                this.setButtonLoading(submitBtn, true);
                
                // Reset after 10 seconds as fallback
                setTimeout(() => {
                    this.setButtonLoading(submitBtn, false);
                }, 10000);
            }
        });
    }

    setButtonLoading(button, isLoading) {
        if (isLoading) {
            button.disabled = true;
            button.dataset.originalText = button.innerHTML;
            button.innerHTML = '<span class="loading"></span> Processing...';
        } else {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.innerHTML = button.dataset.originalText;
                delete button.dataset.originalText;
            }
        }
    }

    setupAnimations() {
        // Add smooth animations to cards
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        });

        document.querySelectorAll('.card').forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(card);
        });
    }

    showNotification(message, type = 'info', duration = 5000) {
        if (typeof uiToast === 'function') {
            uiToast(message, type, duration);
        }
    }
}

// Initialize smart UI
document.addEventListener('DOMContentLoaded', () => {
    window.smartUI = new SmartUI();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { SmartLLM, SmartUI };
}