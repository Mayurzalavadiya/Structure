package com.example.app.data.pojo.response

data class UsersResponse(
    val limit: Int? = null,
    val skip: Int? = null,
    val total: Int? = null,
    val users: List<User?>? = null
) {
    data class User(
        val address: Address? = null,
        val age: Int? = null,
        val bank: Bank? = null,
        val birthDate: String? = null,
        val bloodGroup: String? = null,
        val company: Company? = null,
        val crypto: Crypto? = null,
        val ein: String? = null,
        val email: String? = null,
        val eyeColor: String? = null,
        val firstName: String? = null,
        val gender: String? = null,
        val hair: Hair? = null,
        val height: Double? = null,
        val id: Int? = null,
        val image: String? = null,
        val ip: String? = null,
        val lastName: String? = null,
        val macAddress: String? = null,
        val maidenName: String? = null,
        val password: String? = null,
        val phone: String? = null,
        val role: String? = null,
        val ssn: String? = null,
        val university: String? = null,
        val userAgent: String? = null,
        val username: String? = null,
        val weight: Double? = null
    ) {
        data class Address(
            val address: String? = null,
            val city: String? = null,
            val coordinates: Coordinates? = null,
            val country: String? = null,
            val postalCode: String? = null,
            val state: String? = null,
            val stateCode: String? = null
        ) {
            data class Coordinates(
                val lat: Double? = null,
                val lng: Double? = null
            )
        }

        data class Bank(
            val cardExpire: String? = null,
            val cardNumber: String? = null,
            val cardType: String? = null,
            val currency: String? = null,
            val iban: String? = null
        )

        data class Company(
            val address: Address? = null,
            val department: String? = null,
            val name: String? = null,
            val title: String? = null
        ) {
            data class Address(
                val address: String? = null,
                val city: String? = null,
                val coordinates: Coordinates? = null,
                val country: String? = null,
                val postalCode: String? = null,
                val state: String? = null,
                val stateCode: String? = null
            ) {
                data class Coordinates(
                    val lat: Double? = null,
                    val lng: Double? = null
                )
            }
        }

        data class Crypto(
            val coin: String? = null,
            val network: String? = null,
            val wallet: String? = null
        )

        data class Hair(
            val color: String? = null,
            val type: String? = null
        )
    }
}