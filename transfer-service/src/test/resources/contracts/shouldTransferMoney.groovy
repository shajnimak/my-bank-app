import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Переводит деньги со счёта вошедшего пользователя на счёт другого клиента"
    request {
        method POST()
        url "/api/transfer"
        headers {
            contentType(applicationJson())
        }
        body(
                toLogin: $(consumer(regex(nonBlank())), producer("petrov")),
                amount: $(consumer(regex(positiveInt())), producer(100))
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                fromLogin: "ivanov",
                toLogin: fromRequest().body('$.toLogin'),
                amount: fromRequest().body('$.amount'),
                status: "COMPLETED"
        )
    }
}
