import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Возвращает данные аккаунта вошедшего пользователя"
    request {
        method GET()
        url "/api/accounts/me"
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                login: "ivanov",
                name: "Иванов Иван",
                birthdate: "2001-01-01",
                balance: 100
        )
    }
}
