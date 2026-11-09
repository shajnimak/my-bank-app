import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Снимает деньги со счёта вошедшего пользователя"
    request {
        method POST()
        url "/api/cash"
        headers {
            contentType(applicationJson())
        }
        body(
                action: "WITHDRAW",
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
                action: "WITHDRAW",
                amount: fromRequest().body('$.amount'),
                balance: 0
        )
    }
}
