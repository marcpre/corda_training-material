package net.corda.training

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.util.*

data class IOUStateExample(val borrower: Party,
                           val lender: Party,
                           val amount: Amount<Currency>,
                           override val linearId:
                           UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<Party> = listOf(lender, borrower)

}