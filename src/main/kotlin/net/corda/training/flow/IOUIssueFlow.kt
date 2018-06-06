package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow(val state: IOUState) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        // 1. Take Notary
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        // 2. Create Trx Builder
        val builder = TransactionBuilder(notary)

        // 3. Add Command

        // add output state
        builder.addOutputState(state, IOUContract.IOU_CONTRACT_ID)
        // add command
        builder.addCommand(IOUContract.Commands.Issue(), state.participants.map { it.owningKey })

        // 3. verify trx
        builder.verify(serviceHub)

        // 4. sign trx
        val selfSignTrx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it as Party)}.toSet()

        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val signedTrx = subFlow(CollectSignaturesFlow(selfSignTrx, sessions))

        // Step 7. Finalise the transaction.
        return subFlow(FinalityFlow(signedTrx))

        // Placeholder code to avoid type error when running the tests. Remove before starting the flow task!
        // return serviceHub.signInitialTransaction(
        //        TransactionBuilder(notary = null)
        // )
    }
}

/**
 * This is the flow which signs IOU issuances.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(IOUIssueFlow::class)
class IOUIssueFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction" using (output is IOUState)
            }
        }
        subFlow(signedTransactionFlow)
    }
}