package com.enjin.ecmp.spigot_framework.wallet;

import com.enjin.enjincoin.sdk.model.service.balances.Balance;
import com.enjin.enjincoin.sdk.model.service.tokens.Token;

import java.math.BigInteger;

/**
 * Thread safe class that represents the current balance
 * and withdrawn amount of a token. Supports operations
 * to add and subtract from the balance and withdraw and
 * deposit tokens to be used in-game.
 */
public class MutableBalance {

    private final String tokenId;
    private final String tokenIndex;
    private Integer balance = 0;
    private Integer withdrawn = 0;

    public MutableBalance(Balance balance) {
        this.tokenId = balance.getTokenId();
        this.tokenIndex = balance.getTokenIndex();
        this.balance = balance.getBalance();
    }

    public String id() {
        return this.tokenId;
    }

    public String index() {
        return tokenIndex;
    }

    public Integer balance() {
        synchronized (this.balance) {
            return this.balance;
        }
    }

    public Integer withdrawn() {
        synchronized (this.withdrawn) {
            return this.withdrawn;
        }
    }

    public Integer amountAvailableForWithdrawal() {
        synchronized (this.balance) {
            synchronized (this.withdrawn) {
                return this.balance - this.withdrawn;
            }
        }
    }

    public Integer subtract(Integer amount) {
        synchronized (this.balance) {
            this.balance -= amount;
            if (this.balance < 0) this.balance = 0;
            return this.balance;
        }
    }

    public Integer add(Integer amount) {
        synchronized (this.balance) {
            this.balance += amount;
            return this.balance;
        }
    }

    public boolean withdraw(Integer amount) {
        synchronized (this.withdrawn) {
            if (amountAvailableForWithdrawal().compareTo(amount) != -1) {
                this.withdrawn += amount;
                return true;
            }
        }

        return false;
    }

    public void deposit(Integer amount) {
        synchronized (this.withdrawn) {
            this.withdrawn -= amount;
            if (this.withdrawn < 0) this.withdrawn = 0;
        }
    }
}
