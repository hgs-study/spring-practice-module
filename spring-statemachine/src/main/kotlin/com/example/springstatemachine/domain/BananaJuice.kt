package com.example.springstatemachine.domain

import com.example.springstatemachine.constant.JuiceStatus

class BananaJuice(
    var status: JuiceStatus
) {
    fun soldOut() {
        this.status = JuiceStatus.SOLD_OUT
    }

    companion object {
        fun forSale(): BananaJuice = BananaJuice(JuiceStatus.FOR_SALE)
    }
}