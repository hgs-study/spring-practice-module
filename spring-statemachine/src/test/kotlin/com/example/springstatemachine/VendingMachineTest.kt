package com.example.springstatemachine

import com.example.springstatemachine.constant.VendingMachineState
import com.example.springstatemachine.service.VendingMachineService
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class VendingMachineTest @Autowired constructor(
    private val vendingService: VendingMachineService
) {
    @Test
    @DisplayName("자판기에 500원 넣기")
    fun test1(){
        vendingService.insert500Won()

        assertThat(vendingService.stateId).isEqualTo(VendingMachineState.M_500_WON)
    }


    @Test
    @DisplayName("자판기에 500원, 1000원 넣기")
    fun test2(){
        vendingService.insert500Won()
        vendingService.insert1000Won()

        assertThat(vendingService.stateId).isEqualTo(VendingMachineState.M_1500_WON)
    }

    @Test
    @DisplayName("자판기에 500원, 2500원 넣기")
    fun test3(){
        vendingService.insert500Won()
        vendingService.insert2500Won()

        assertThat(vendingService.stateId).isEqualTo(VendingMachineState.M_3000_WON)
    }

    @Test
    @DisplayName("자판기에 500원, 2500원 넣어서 바나나쥬스 구매")
    fun test5(){
        vendingService.insert500Won()
        vendingService.insert2500Won()
        vendingService.buyBananaJuice()

        assertThat(vendingService.stateId).isEqualTo(VendingMachineState.M_0_WON)
    }
}