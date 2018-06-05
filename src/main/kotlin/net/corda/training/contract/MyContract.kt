package net.corda.training.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.training.state.IOUState

class MyContract : Contract {
    companion object {
        @JvmStatic
        val CONTRACT_ID = "net.corda.training.contract.MyContract"
    }

    interface Commands : CommandData {
        class Issue : Commands, TypeOnlyCommandData() // overwrite HashCodes
        class Settle : Commands, TypeOnlyCommandData()
        class Transfer : Commands, TypeOnlyCommandData()
        class Default : Commands, TypeOnlyCommandData()
    }

    // add '?' to make type nullable, you could potentially get a NullPointerException
    override fun verify(tx: LedgerTransaction) {
        // Commands.Issue().equals(Commands.Issue())
        val cmd = tx.commands.requireSingleCommand<Commands>() //
        when (cmd.value) {
            is Commands.Issue -> verifyIssueTransaction(tx)
            is Commands.Settle -> requireThat {
                // other logic
            }
            else -> throw IllegalArgumentException("Invalid Command")
        }
    }

    fun verifyIssueTransaction(ledgerTx: LedgerTransaction) {
        // if (!ledgerTx.inputs.isEmpty())
        //     throw IllegalArgumentException("Inputs should be empty for issuance tx.")
        requireThat {
            "Inputs should be empty for issuance tx." using (ledgerTx.inputs.isEmpty())
            "There should be only on IOUState" using (ledgerTx.outputs.size == 1)
            val out = ledgerTx.outputsOfType<IOUState>()[0]
            "Amount should be greater than zero" using (out.amount.quantity > 0)
        }
    }
}