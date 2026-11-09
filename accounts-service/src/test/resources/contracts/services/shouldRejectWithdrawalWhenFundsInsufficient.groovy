import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Не списывает деньги, если на счёте недостаточно средств"
    priority 1
    request {
        method POST()
        url $(consumer(regex('/api/accounts/[A-Za-z0-9_.-]+/withdraw')), producer('/api/accounts/ivanov/withdraw'))
        headers {
            contentType(applicationJson())
        }
        body(amount: 1000000)
    }
    response {
        status CONFLICT()
        headers {
            contentType(applicationJson())
        }
        body(errors: ["Недостаточно средств на счету"])
    }
}
