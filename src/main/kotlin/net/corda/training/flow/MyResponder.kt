package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy

@InitiatedBy(MyInitiator::class)
class MyResponder(val session : FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {

    }
}