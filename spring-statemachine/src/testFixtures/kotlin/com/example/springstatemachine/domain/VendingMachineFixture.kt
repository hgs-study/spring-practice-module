package com.example.springstatemachine.domain

fun getVendingMachineFixture() =
    VendingMachine(
        count500Won = 0,
        count1000Won = 0,
        count2500Won = 0,
        bananaJuice = BananaJuice.forSale(),
        appleJuice = AppleJuice.forSale()
    )