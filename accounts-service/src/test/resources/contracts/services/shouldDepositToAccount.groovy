import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Зачисляет деньги на счёт пользователя"
    request {
        method POST()
        url $(consumer(regex('/api/accounts/[A-Za-z0-9_.-]+/deposit')), producer('/api/accounts/petrov/deposit'))
        headers {
            contentType(applicationJson())
        }
        body(amount: $(consumer(regex(positiveInt())), producer(50)))
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                login: fromRequest().path(2),
                balance: 150
        )
    }
}
