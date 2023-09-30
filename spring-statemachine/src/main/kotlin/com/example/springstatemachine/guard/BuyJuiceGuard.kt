package com.example.springstatemachine.guard

import com.example.springstatemachine.constant.VendingMachineEvent
import com.example.springstatemachine.constant.VendingMachineState
import com.example.springstatemachine.service.VendingMachineService
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.guard.Guard
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class BuyJuiceGuard: Guard<VendingMachineState, VendingMachineEvent> {

    override fun evaluate(context: StateContext<VendingMachineState, VendingMachineEvent>): Boolean {
        val vendingMachineService = VendingMachineService(context.stateMachine)

        return if (vendingMachineService.stateId != VendingMachineState.M_3000_WON) {
            throw IllegalArgumentException ( "Not enough Money" )
            false
        } else
            true
    }
}