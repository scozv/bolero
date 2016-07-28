package models.interop.pingxx

case class Charge
(
  _id: String,
  chargeObject: String,
  created: Long,
  liveMode: Boolean,
  paid: Boolean,
  refunded: Boolean,
  app: String,
  channel: String,
  orderId: String,
  clientIp: String,
  amount: Int,
  amountSettle: Int,
  currency: String,
  subject: String,
  body: String,
  extra: String,
  timePaid: Long,
  timeExpire: Long,
  transactionId: String,
  failureCode: String,
  failureMessage: String,
  description: String
)
