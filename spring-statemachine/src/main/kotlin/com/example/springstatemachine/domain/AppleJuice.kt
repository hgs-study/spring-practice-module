package com.example.springstatemachine.domain

import com.example.springstatemachine.constant.JuiceStatus

class AppleJuice(
    var status: JuiceStatus
) {
    fun soldOut() {
        this.status = JuiceStatus.SOLD_OUT
    }

    companion object {
        fun forSale(): AppleJuice = AppleJuice(JuiceStatus.FOR_SALE)
    }
}