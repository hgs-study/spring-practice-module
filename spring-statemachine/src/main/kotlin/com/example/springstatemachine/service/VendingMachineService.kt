package com.example.springstatemachine.service

import com.example.springstatemachine.constant.VendingMachineEvent
import com.example.springstatemachine.constant.VendingMachineState
import com.example.springstatemachine.domain.VendingMachine
import org.springframework.statemachine.StateMachine
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class VendingMachineService(
    private val stateMachine: StateMachine<VendingMachineState, VendingMachineEvent>
) {
    private val vendingMachine: VendingMachine = VendingMachine.init()

    var stateId = VendingMachineState.M_0_WON
        get() = stateMachine.state.id

    @PostConstruct
    fun init(){
        stateMachine.start()
    }

    fun getMoney(): Int = vendingMachine.money

    fun plusMoney(money: Int) = vendingMachine.plusMoney(money)

    fun soldOutBananaJuice() = vendingMachine.soldOutBananaJuice()

    fun soldOutAppleJuice() = vendingMachine.soldOutAppleJuice()

    fun insert500Won() = stateMachine.sendEvent(VendingMachineEvent.INERT_500_WON)

    fun insert1000Won() = stateMachine.sendEvent(VendingMachineEvent.INERT_1000_WON)

    fun insert2500Won() = stateMachine.sendEvent(VendingMachineEvent.INERT_2500_WON)

    fun buyBananaJuice() = stateMachine.sendEvent(VendingMachineEvent.PUSH_BANANA_JUICE)

    fun buyAppleJuice() = stateMachine.sendEvent(VendingMachineEvent.PUSH_APPLE_JUICE)

}