package com.example.springstatemachine.domain

class VendingMachine(
    var count500Won: Int,
    var count1000Won: Int,
    var count2500Won: Int,
    val bananaJuice: BananaJuice,
    val appleJuice: AppleJuice
) {
    var money: Int = 0
        get() = (count500Won * 500) + (count1000Won * 1000) + (count2500Won * 2500)
        private set

    fun plusMoney(money: Int) {
        when(money){
            500 -> this.count500Won += 1
            1000 -> this.count1000Won += 1
            2500 -> this.count2500Won += 1
        }
    }

    fun soldOutBananaJuice() = this.bananaJuice.soldOut()

    fun soldOutAppleJuice() = this.appleJuice.soldOut()

    companion object {
        fun init(): VendingMachine =
            VendingMachine(
                count500Won = 0,
                count1000Won = 0,
                count2500Won = 0,
                bananaJuice = BananaJuice.forSale(),
                appleJuice = AppleJuice.forSale()
            )
    }
}