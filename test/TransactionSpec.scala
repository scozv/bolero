

class TransactionSpec extends CanFakeHTTP {
  sequential

  "PUT /:id" should {
    "create new tx via PUT"                             in ko
    "be able to create multi tx"                        in ko
    "return error when duplicated tx creating"          in ko
  }

  "GET /:id" should {
    "return specific tx via GET"                        in ko
    "return NOT_FOUND with non-existing tx id"          in ko
  }

  "GET /type" should {
    "return a list of tx id"                            in ko
    "check each tx matched specific :type or not"       in ko
  }

  "GET /sum/:id" should {
    "return 0 when :id not existing"                    in ko
    "return valid sum calculation"                      in ko
    "sum/a = sum/a when a, b belong to same group"      in ko
  }
}
