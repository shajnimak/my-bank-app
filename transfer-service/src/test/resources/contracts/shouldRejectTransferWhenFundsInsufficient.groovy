import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Не переводит деньги, если на счёте отправителя недостаточно средств"
    priority 1
    request {
        method POST()
        url "/api/transfer"
        headers {
            contentType(applicationJson())
        }
        body(
                toLogin: $(consumer(regex(nonBlank())), producer("petrov")),
                amount: 1000000
        )
    }
    response {
        status CONFLICT()
        headers {
            contentType(applicationJson())
        }
        body(errors: ["Недостаточно средств на счету"])
    }
}
