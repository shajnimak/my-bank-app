import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Пополняет счёт вошедшего пользователя"
    request {
        method POST()
        url "/api/cash"
        headers {
            contentType(applicationJson())
        }
        body(
                action: "DEPOSIT",
                amount: $(consumer(regex(positiveInt())), producer(100))
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                login: "ivanov",
                action: "DEPOSIT",
                amount: fromRequest().body('$.amount'),
                balance: 200
        )
    }
}
