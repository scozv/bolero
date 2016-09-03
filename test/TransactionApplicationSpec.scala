import play.api.test.WithApplication
import models._
import play.api.libs.json.Json

class TransactionApplicationSpec extends CanFakeHTTP {
  sequential

  "data clear up" should {
    "clear previous tx data" in {
      collectionDelete('transactions) must beTrue
    }
  }

  "PUT /:id" should {
    "create new tx via PUT"                             in a1
    "be able to create multi tx"                        in a2
    "return error when duplicated tx creating"          in a3
  }

  "GET /:id" should {
    "return specific tx via GET"                        in b1
    "return NOT_FOUND with non-existing tx id"          in b2
  }

  "GET /type" should {
    "return a list of tx id"                            in c1
    "check each tx matched specific :type or not"       in c1
  }

  "GET /sum/:id" should {
    "return 0 when :id not existing"                    in d1
    "return valid sum calculation"                      in d2
    "sum/a = sum/b when a, b belong to same group"      in d3
  }


  // prepare tx data with id from 1 to 5
  val txData =
    Transaction("1", 100, "cars") ::
    Transaction("2", 200, "food") ::
    Transaction("3", 300, "cars", "2") ::
    Transaction("4", 750, "digital") ::
    Transaction("5", 1000, "shopping", "2") :: Nil

  object routes {
    val PUT_TX = Uri("PUT", "/transactionservice/transaction/:id", auth = false)
  }

  def a1 = new WithApplication {
    // create new tx via PUT

    // 0. put tx with id 1
    val tx = txData.find(_._id == "1").get
    val response = http(routes.PUT_TX.withId("1"), payload = Json.toJson(tx))
    val target = contentValidate[Transaction](response)
    // 0. check status ok
    target._id === "1"
    target.tp === "cars"
  }

  def a2 = new WithApplication {
    // be able to create multi tx

    // 0. for 2 to 5
    txData.filter(_._id > "1").foreach { tx =>
      // 0. put each tx
      val response = http(routes.PUT_TX.withId(tx._id), payload = Json.toJson(tx))
      val target = contentValidate[Transaction](response)
      // 0. check each status
      target._id === tx._id
      target.tp === tx.tp
    }
  }

  def a3 = new WithApplication {
    // return error when duplicated tx creating

    // 0. put tx with id 1 (duplicated)
    val tx = txData.find(_._id == "1").get
    val response = http(routes.PUT_TX.withId("1"), payload = Json.toJson(tx))
    val target = contentValidate[Transaction](response)
    // 0. error return
    ko
  }

  def b1 = new WithApplication {
    // return specific tx via GET

    // 0. get tx with id 1
    // 0. check properties of tx with id 1
    ko
  }

  def b2 = new WithApplication {
    // return NOT_FOUND with non-existing tx id

    // 0. get tx with id 1024 (non existing)
    // 0. NOT_FOUND error
    ko
  }

  def c1 = new WithApplication {
    // return a list of tx id

    // 0. get type/:tp
    // 0. list.forAll (_.type must be same as :tp)
    ko
  }


  def d1 = ko
  def d2 = new WithApplication {
    // return valid sum calculation

    // 0. get the sum from RESTful api
    // 0. sum the prepared data
    // 0. must be equal
    ko
  }

  def d3 = new WithApplication {
    // sum/a = sum/b when a, b belong to same group

    // assuming tx a has the same parent_id as tx b
    // GET sum/a must be equal to sum/b
    ko
  }
}
