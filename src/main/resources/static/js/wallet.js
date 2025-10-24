// Wallet Management JavaScript
class WalletManager {
    constructor() {
        this.currentUserId = 1; // Demo user ID - in real app, get from session
        this.init();
    }

    async init() {
        await this.loadWalletBalance();
        await this.loadQuickAddOptions();
        await this.loadPaymentHistory();
        await this.loadBookingHistory();
    }

    async loadWalletBalance() {
        try {
            const response = await fetch(`/api/wallet/balance/${this.currentUserId}`);
            const data = await response.json();
            
            if (response.ok) {
                document.getElementById('balanceAmount').textContent = data.balance.toFixed(2);
            } else {
                this.showToast('Error loading wallet balance', 'error');
            }
        } catch (error) {
            console.error('Error loading wallet balance:', error);
            this.showToast('Failed to load wallet balance', 'error');
        }
    }

    async loadQuickAddOptions() {
        try {
            const response = await fetch(`/api/wallet/quick-add/${this.currentUserId}`);
            const data = await response.json();
            
            if (response.ok) {
                const quickAddGrid = document.getElementById('quickAddGrid');
                quickAddGrid.innerHTML = '';
                
                data.quickAddOptions.forEach(option => {
                    const button = document.createElement('button');
                    button.className = 'quick-add-btn';
                    button.innerHTML = `
                        <div class="quick-add-amount">₹${option.amount}</div>
                        <div class="quick-add-label">${option.label}</div>
                    `;
                    button.onclick = () => this.quickAddMoney(option.amount);
                    quickAddGrid.appendChild(button);
                });
            }
        } catch (error) {
            console.error('Error loading quick add options:', error);
        }
    }

    async quickAddMoney(amount) {
        try {
            const response = await fetch('/api/wallet/add-money', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    userId: this.currentUserId,
                    amount: amount,
                    method: 'DEMO'
                })
            });

            const data = await response.json();
            
            if (response.ok) {
                this.showToast(`₹${amount} added to wallet successfully!`, 'success');
                await this.loadWalletBalance();
                await this.loadPaymentHistory();
            } else {
                this.showToast(data.error || 'Failed to add money', 'error');
            }
        } catch (error) {
            console.error('Error adding money:', error);
            this.showToast('Failed to add money to wallet', 'error');
        }
    }

    async loadPaymentHistory() {
        try {
            const response = await fetch(`/api/wallet/history/${this.currentUserId}`);
            const data = await response.json();
            
            if (response.ok) {
                const tbody = document.getElementById('paymentHistoryBody');
                tbody.innerHTML = '';
                
                if (data.payments && data.payments.length > 0) {
                    data.payments.forEach(payment => {
                        const row = document.createElement('tr');
                        const amount = parseFloat(payment.amount);
                        const isCredit = amount > 0;
                        
                        row.innerHTML = `
                            <td>${new Date(payment.paymentDate).toLocaleDateString()}</td>
                            <td>
                                <span class="payment-type ${isCredit ? 'credit' : 'debit'}">
                                    ${isCredit ? 'Credit' : 'Debit'}
                                </span>
                            </td>
                            <td class="${isCredit ? 'text-success' : 'text-danger'}">
                                ${isCredit ? '+' : ''}₹${Math.abs(amount).toFixed(2)}
                            </td>
                            <td>${payment.method}</td>
                            <td>
                                <span class="status status-success">Completed</span>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });
                } else {
                    tbody.innerHTML = '<tr><td colspan="5" class="text-center">No payment history found</td></tr>';
                }
            } else {
                this.showToast('Error loading payment history', 'error');
            }
        } catch (error) {
            console.error('Error loading payment history:', error);
            this.showToast('Failed to load payment history', 'error');
        }
    }

    async loadBookingHistory() {
        try {
            const response = await fetch(`/api/wallet/bookings/${this.currentUserId}`);
            const data = await response.json();
            
            if (response.ok) {
                const tbody = document.getElementById('bookingHistoryBody');
                tbody.innerHTML = '';
                
                if (data.bookings && data.bookings.length > 0) {
                    data.bookings.forEach(booking => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>#${booking.bookingId}</td>
                            <td>${booking.fromLocation} → ${booking.toLocation}</td>
                            <td>${booking.transportType}</td>
                            <td>₹${booking.fare.toFixed(2)}</td>
                            <td>${new Date(booking.dateTime).toLocaleDateString()}</td>
                            <td>
                                <span class="status status-${booking.status.toLowerCase()}">${booking.status}</span>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });
                } else {
                    tbody.innerHTML = '<tr><td colspan="6" class="text-center">No booking history found</td></tr>';
                }
            } else {
                this.showToast('Error loading booking history', 'error');
            }
        } catch (error) {
            console.error('Error loading booking history:', error);
            this.showToast('Failed to load booking history', 'error');
        }
    }

    showAddMoneyModal() {
        document.getElementById('addMoneyModal').style.display = 'flex';
    }

    closeAddMoneyModal() {
        document.getElementById('addMoneyModal').style.display = 'none';
        document.getElementById('addMoneyForm').reset();
    }

    async refreshBalance() {
        await this.loadWalletBalance();
        this.showToast('Balance refreshed', 'success');
    }

    showToast(message, type = 'info') {
        // Use the existing uiToast function if available
        if (typeof uiToast === 'function') {
            uiToast(message, type);
        } else {
            // Fallback toast implementation
            const toast = document.createElement('div');
            toast.className = `toast toast-${type}`;
            toast.textContent = message;
            document.body.appendChild(toast);
            
            setTimeout(() => {
                toast.remove();
            }, 3000);
        }
    }
}

// Initialize wallet manager when page loads
document.addEventListener('DOMContentLoaded', () => {
    window.walletManager = new WalletManager();
    
    // Add money form submission
    document.getElementById('addMoneyForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const amount = parseFloat(formData.get('amount'));
        const method = formData.get('method');
        
        try {
            const response = await fetch('/api/wallet/add-money', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    userId: window.walletManager.currentUserId,
                    amount: amount,
                    method: method
                })
            });

            const data = await response.json();
            
            if (response.ok) {
                window.walletManager.showToast(`₹${amount} added to wallet successfully!`, 'success');
                window.walletManager.closeAddMoneyModal();
                await window.walletManager.loadWalletBalance();
                await window.walletManager.loadPaymentHistory();
            } else {
                window.walletManager.showToast(data.error || 'Failed to add money', 'error');
            }
        } catch (error) {
            console.error('Error adding money:', error);
            window.walletManager.showToast('Failed to add money to wallet', 'error');
        }
    });
});

// Modal close functionality
function showAddMoneyModal() {
    window.walletManager.showAddMoneyModal();
}

function closeAddMoneyModal() {
    window.walletManager.closeAddMoneyModal();
}

function refreshBalance() {
    window.walletManager.refreshBalance();
}

// Close modal when clicking outside
document.addEventListener('click', (e) => {
    const modal = document.getElementById('addMoneyModal');
    if (e.target === modal) {
        closeAddMoneyModal();
    }
});
