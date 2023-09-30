package com.example.springstatemachine.config

import com.example.springstatemachine.action.InsertMoneyAction
import com.example.springstatemachine.constant.VendingMachineEvent
import com.example.springstatemachine.constant.VendingMachineState
import com.example.springstatemachine.guard.BuyJuiceGuard
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.StateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachine
class StatemachineConfig(
    private val insertMoneyAction: InsertMoneyAction,
    private val buyJuiceGuard: BuyJuiceGuard
): StateMachineConfigurerAdapter<VendingMachineState, VendingMachineEvent>() {

    override fun configure(states: StateMachineStateConfigurer<VendingMachineState, VendingMachineEvent>) {
        states
            .withStates()
            .initial(VendingMachineState.M_0_WON)
            .state(VendingMachineState.M_500_WON)
            .state(VendingMachineState.M_1000_WON)
            .state(VendingMachineState.M_1500_WON)
            .state(VendingMachineState.M_2000_WON)
            .state(VendingMachineState.M_2500_WON)
            .state(VendingMachineState.M_3000_WON)
    }

    override fun configure(transitions: StateMachineTransitionConfigurer<VendingMachineState, VendingMachineEvent>) {
        transitions
            .withExternal()
                .source(VendingMachineState.M_0_WON)
                .target(VendingMachineState.M_500_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_0_WON)
                .target(VendingMachineState.M_1000_WON)
                .event(VendingMachineEvent.INERT_1000_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_0_WON)
                .target(VendingMachineState.M_2500_WON)
                .event(VendingMachineEvent.INERT_2500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_500_WON)
                .target(VendingMachineState.M_1000_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_500_WON)
                .target(VendingMachineState.M_1500_WON)
                .event(VendingMachineEvent.INERT_1000_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_500_WON)
                .target(VendingMachineState.M_3000_WON)
                .event(VendingMachineEvent.INERT_2500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_1000_WON)
                .target(VendingMachineState.M_1500_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_1500_WON)
                .target(VendingMachineState.M_2000_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_1500_WON)
                .target(VendingMachineState.M_1500_WON)
                .event(VendingMachineEvent.PUSH_BANANA_JUICE)
                .guard(buyJuiceGuard)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_2000_WON)
                .target(VendingMachineState.M_2500_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_2500_WON)
                .target(VendingMachineState.M_3000_WON)
                .event(VendingMachineEvent.INERT_500_WON)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_3000_WON)
                .target(VendingMachineState.M_0_WON)
                .event(VendingMachineEvent.PUSH_APPLE_JUICE)
                .guard(buyJuiceGuard)
                .action(insertMoneyAction)
            .and()
            .withExternal()
                .source(VendingMachineState.M_3000_WON)
                .target(VendingMachineState.M_0_WON)
                .event(VendingMachineEvent.PUSH_BANANA_JUICE)
                .guard(buyJuiceGuard)
                .action(insertMoneyAction)

    }
}