package dev.scavazzini.clevent.domain.core.serializer

import dev.scavazzini.clevent.CustomerOuterClass
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.model.Product
import javax.inject.Inject

class CustomerProtobufSerializer @Inject constructor() : CustomerSerializer {
    override fun serialize(customer: Customer): ByteArray {
        val builder = CustomerOuterClass.Customer.newBuilder().apply {
            this.balance = customer.balance
            this.addAllProducts(customer.products.serialize())
        }

        return builder.build().toByteArray()
    }

    private fun Map<Product, Int>.serialize(): List<CustomerOuterClass.Product> {
        return map { entry ->
            CustomerOuterClass.Product.newBuilder().apply {
                this.id = entry.key.id.toInt()
                this.quantity = entry.value
            }.build()
        }
    }

    override fun deserialize(data: ByteArray): Customer {
        return CustomerOuterClass.Customer.parseFrom(data).toCustomer()
    }

    private fun CustomerOuterClass.Customer.toCustomer(): Customer {
        return Customer(
            balance = this.balance,
            products = this.productsList.toProducts(),
        )
    }

    private fun List<CustomerOuterClass.Product>.toProducts(): Map<Product, Int> {
        return associate { serializedProduct ->
            Product(
                id = serializedProduct.id.toShort(),
                name = "",
                price = 0,
                category = "",
            ) to serializedProduct.quantity
        }
    }
}
