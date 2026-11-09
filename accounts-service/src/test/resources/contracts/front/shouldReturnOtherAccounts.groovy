import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Возвращает логины, фамилии и имена других пользователей для переводов"
    request {
        method GET()
        url "/api/accounts"
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                [login: "petrov", name: "Петров Петр"],
                [login: "sidorov", name: "Сидоров Сидор"]
        ])
    }
}
