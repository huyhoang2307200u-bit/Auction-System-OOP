package com.auction.model;

import com.auction.exception.InvalidBidException;
import com.auction.util.MoneyUtil;
import java.math.BigDecimal;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;

    private String username;
    private String displayName;
    private String password;
    private Role role;
    private BigDecimal balance = BigDecimal.ZERO;

    protected User() {
        super();
    }

    protected User(String username, String displayName, String password, Role role) {
        super();
        this.username = username;
        this.displayName = displayName;
        this.password = password;
        this.role = role;
        this.balance = BigDecimal.ZERO;
    }

    protected User(int ignoredId, String displayName, String username, String password, String ignoredRoleName, Role role) {
        super();
        this.username = username;
        this.displayName = displayName;
        this.password = password;
        this.role = role;
        this.balance = BigDecimal.ZERO;
    }

    public abstract String dashboardTitle();

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return displayName;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public synchronized BigDecimal getBalanceValue() {
        return MoneyUtil.normalize(balance);
    }

    public synchronized double getBalance() {
        return getBalanceValue().doubleValue();
    }

    public synchronized void setBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            this.balance = BigDecimal.ZERO;
            return;
        }
        this.balance = MoneyUtil.normalize(balance);
    }

    public synchronized void setBalance(double balance) {
        setBalance(MoneyUtil.fromDouble(balance));
    }

    public synchronized void deposit(BigDecimal amount) {
        BigDecimal normalized = MoneyUtil.normalize(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        }
        this.balance = MoneyUtil.normalize(this.balance.add(normalized));
    }

    public synchronized void deposit(double amount) {
        deposit(MoneyUtil.fromDouble(amount));
    }

    public synchronized boolean canAfford(BigDecimal amount) {
        BigDecimal normalized = MoneyUtil.normalize(amount);
        return normalized.compareTo(BigDecimal.ZERO) > 0 && this.balance.compareTo(normalized) >= 0;
    }

    public synchronized void debit(BigDecimal amount) throws InvalidBidException {
        BigDecimal normalized = MoneyUtil.normalize(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Số tiền cần trừ phải lớn hơn 0.");
        }
        if (this.balance.compareTo(normalized) < 0) {
            throw new InvalidBidException("Số dư không đủ. Số dư hiện tại: " + getBalanceValue() + ".");
        }
        this.balance = MoneyUtil.normalize(this.balance.subtract(normalized));
    }

    public synchronized void credit(BigDecimal amount) {
        BigDecimal normalized = MoneyUtil.normalize(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        this.balance = MoneyUtil.normalize(this.balance.add(normalized));
    }
}
