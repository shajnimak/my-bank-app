import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Не снимает деньги, если на счёте недостаточно средств"
    priority 1
    request {
        method POST()
        url "/api/cash"
        headers {
            contentType(applicationJson())
        }
        body(
                action: "WITHDRAW",
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
