import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Не зачисляет деньги на несуществующий счёт"
    priority 1
    request {
        method POST()
        url "/api/accounts/unknown/deposit"
        headers {
            contentType(applicationJson())
        }
        body(amount: $(consumer(regex(positiveInt())), producer(50)))
    }
    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body(errors: ["Аккаунт unknown не найден"])
    }
}
