package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.training.contract.IOUContract
import net.corda.training.contract.MyContract
import net.corda.training.state.IOUState

@InitiatingFlow
class MyInitiator(val iou : IOUState) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        if(serviceHub.myInfo.legalIdentities.single() != iou.lender) {
            throw IllegalArgumentException()
        }

        // 1. Take Notary
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        // 2. Create Trx Builder
        val builder = TransactionBuilder(notary)
        // add output state
        builder.addOutputState(iou, IOUContract.IOU_CONTRACT_ID)
        // add command
        builder.addCommand(MyContract.Commands.Issue(), iou.lender.owningKey, iou.borrower.owningKey)

        // 3. verify trx
        builder.verify(serviceHub)

        // 5. Self Sign the trx
        val selfSignedTrx = serviceHub.signInitialTransaction(builder)

        // 6. Collect other party's sigs
        val session : FlowSession = initiateFlow(iou.borrower)
        val allSignedTx = subFlow(CollectSignaturesFlow(selfSignedTrx, listOf(session)))

        // 7. Notarise trx
        val notarisedTx = subFlow(FinalityFlow(allSignedTx))

        return notarisedTx
    }
}