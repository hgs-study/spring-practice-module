package com.example.springstatemachine.action

import com.example.springstatemachine.constant.VendingMachineEvent
import com.example.springstatemachine.constant.VendingMachineState
import com.example.springstatemachine.service.VendingMachineService
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.action.Action
import org.springframework.stereotype.Component

@Component
class InsertMoneyAction: Action<VendingMachineState, VendingMachineEvent>{

    override fun execute(context: StateContext<VendingMachineState, VendingMachineEvent>) {
        val vendingMachineService = VendingMachineService(context.stateMachine)

        when(context.event){
            VendingMachineEvent.INERT_500_WON -> vendingMachineService.plusMoney(500)
            VendingMachineEvent.INERT_1000_WON -> vendingMachineService.plusMoney(1000)
            VendingMachineEvent.INERT_2500_WON -> vendingMachineService.plusMoney(2500)
            VendingMachineEvent.PUSH_BANANA_JUICE -> vendingMachineService.soldOutBananaJuice()
            VendingMachineEvent.PUSH_APPLE_JUICE -> vendingMachineService.soldOutAppleJuice()
        }
    }
}