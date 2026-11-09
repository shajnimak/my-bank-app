import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Отклоняет изменение аккаунта, если пользователю меньше 18 лет"
    priority 1
    request {
        method PUT()
        url "/api/accounts/me"
        headers {
            contentType(applicationJson())
        }
        body(
                name: "Иванов Иван",
                birthdate: "2015-01-01"
        )
    }
    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(errors: ["Возраст должен быть старше 18 лет"])
    }
}
