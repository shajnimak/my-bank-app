import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Списывает деньги со счёта пользователя"
    request {
        method POST()
        url $(consumer(regex('/api/accounts/[A-Za-z0-9_.-]+/withdraw')), producer('/api/accounts/ivanov/withdraw'))
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
                balance: 50
        )
    }
}
