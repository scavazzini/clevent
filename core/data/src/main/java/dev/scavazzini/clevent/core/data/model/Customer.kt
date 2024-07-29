package dev.scavazzini.clevent.core.data.model

import dev.scavazzini.clevent.core.data.model.exception.BalanceLimitExceededException
import dev.scavazzini.clevent.core.data.model.exception.InsufficientBalanceException
import java.io.Serializable
import java.util.Objects

class Customer(
    balance: Int = 0,
    products: Map<Product, Int> = mapOf(),
) : Serializable {

    private val _products: MutableMap<Product, Int> = products.toMutableMap()

    var balance = balance
        private set

    val products: Map<Product, Int>
        get() = _products.toMap()

    val total: Int
        get() = _products.map { it.key.price * it.value }.sum()

    /**
     * Adds credit to this customer.
     * @param value Amount to be added
     * @throws IllegalArgumentException
     * @throws BalanceLimitExceededException
     */
    fun recharge(value: Int) {
        if (value < 0) {
            throw IllegalArgumentException("Recharge value must be a positive value.")
        }

        val newBalance: Long = balance.toLong() + value

        if (newBalance > Int.MAX_VALUE) {
            // Balance overflow, reject
            throw BalanceLimitExceededException()
        }

        balance = newBalance.toInt()
    }

    /**
     * Charge credits of this customer.
     * @param value Amount to be charged
     * @throws IllegalArgumentException
     * @throws InsufficientBalanceException
     */
    fun charge(value: Int) {
        if (value < 0) {
            throw IllegalArgumentException("Charge value must be a positive value.")
        }

        if (value > balance) {
            throw InsufficientBalanceException()
        }

        val newBalance: Long = balance.toLong() - value

        if (newBalance < Int.MIN_VALUE) {
            // Balance underflow, reject
            throw BalanceLimitExceededException()
        }

        balance = newBalance.toInt()
    }

    /**
     * Adds products to this customer.
     * @param product Product to be added
     * @param quantity How many units to be added. Default is 1
     * @throws InsufficientBalanceException
     */
    fun addProduct(product: Product, quantity: Int = 1) {
        val total = product.price * quantity
        charge(total)
        _products[product] = _products[product]?.plus(quantity) ?: quantity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val customer = other as Customer
        return balance == customer.balance &&
                _products == customer._products
    }

    override fun hashCode(): Int {
        return Objects.hash(_products, balance)
    }
}

val EMPTY_CUSTOMER = Customer(
    balance = 0,
    products = emptyMap(),
)
