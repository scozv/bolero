package biz

import biz.interop.CanConnectDB2
import models._

object TransactionBiz extends CanConnectDB2[Transaction] {
  override val collectionName: Symbol = 'transactions
}
