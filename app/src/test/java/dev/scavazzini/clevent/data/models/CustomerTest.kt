package dev.scavazzini.clevent.data.models

import dev.scavazzini.clevent.exceptions.BalanceLimitExceededException
import dev.scavazzini.clevent.exceptions.InsufficientBalanceException
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomerTest {

    private val customer: Customer = Customer()
    private val product1 = Product(1.toShort(), "p1", 1000, "Beer")
    private val product2 = Product(50.toShort(), "p2", 2000, "Beer")

    @Test
    fun shouldRechargeBalance() {
        customer.recharge(50000)
        assertEquals(50000, customer.balance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionWhenRechargeBalanceWithNegativeValue() {
        customer.recharge(-500)
    }

    @Test(expected = BalanceLimitExceededException::class)
    fun shouldThrowExceptionWhenRechargeOverflowsBalance() {
        customer.recharge(Int.MAX_VALUE)
        customer.recharge(500)
    }

    @Test
    fun shouldChargeBalance() {
        customer.recharge(500)
        customer.charge(300)
        assertEquals(200, customer.balance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionWhenChargeWithNegativeValue() {
        customer.charge(-500)
    }

    @Test(expected = InsufficientBalanceException::class)
    fun shouldThrowExceptionWhenCustomerHaveInsufficientBalance() {
        customer.recharge(500)
        customer.charge(600)
    }

    @Test
    fun shouldAddProduct() {
        customer.recharge(5000)
        customer.addProduct(product1)
        customer.addProduct(product2)

        assertEquals(2, customer.products.size)
        assertEquals(2000, customer.balance)
    }

    @Test
    fun shouldAddMultipleProductsAtOnce() {
        customer.recharge(10000)
        customer.addProduct(product1, 3)
        customer.addProduct(product2, 2)

        assertEquals(5, customer.products.values.sum())
        assertEquals(2, customer.products.size)
        assertEquals(3000, customer.balance)
    }

    @Test(expected = InsufficientBalanceException::class)
    fun shouldFailToAddProductWithInsufficientBalance() {
        customer.addProduct(product1)
    }
}
