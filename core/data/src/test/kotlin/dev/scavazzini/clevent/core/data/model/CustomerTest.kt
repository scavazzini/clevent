package dev.scavazzini.clevent.core.data.model

import dev.scavazzini.clevent.core.data.model.exception.BalanceLimitExceededException
import dev.scavazzini.clevent.core.data.model.exception.InsufficientBalanceException
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

    @Test
    fun shouldReturnTotalValueOfAddedProducts() {
        val customer = Customer(20000).also {
            it.addProduct(Product(1.toShort(), "p1", 1000, "Beer"), 5)
            it.addProduct(Product(2.toShort(), "p2", 2000, "Beer2"), 2)
            it.addProduct(Product(3.toShort(), "p3", 9000, "Beer3"), 1)
        }

        assertEquals(18000, customer.total)
    }

    @Test
    fun shouldReturnTrueIfEqualsIsCalledOnEqualCustomers() {
        val customer1 = Customer(5000).also { it.addProduct(product1) }
        val customer2 = Customer(5000).also { it.addProduct(product1) }

        assertEquals(true, customer1 == customer2)
    }

    @Test
    fun shouldReturnFalseIfEqualsIsCalledOnCustomerWithDifferentBalance() {
        val customer1 = Customer(5000).also { it.addProduct(product1) }
        val customer2 = Customer(3000).also { it.addProduct(product1) }

        assertEquals(false, customer1 == customer2)
    }

    @Test
    fun shouldReturnFalseIfEqualsIsCalledOnCustomerWithDifferentProducts() {
        val customer1 = Customer(5000).also { it.addProduct(product1) }
        val customer2 = Customer(5000).also { it.addProduct(product2) }

        assertEquals(false, customer1 == customer2)
    }
}
