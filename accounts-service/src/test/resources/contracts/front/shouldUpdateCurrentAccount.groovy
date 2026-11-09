import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Изменяет фамилию, имя и дату рождения вошедшего пользователя"
    request {
        method PUT()
        url "/api/accounts/me"
        headers {
            contentType(applicationJson())
        }
        body(
                name: $(consumer(regex(nonBlank())), producer("Иванов Пётр")),
                birthdate: $(consumer(regex(isoDate())), producer("1990-02-03"))
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                login: "ivanov",
                name: fromRequest().body('$.name'),
                birthdate: fromRequest().body('$.birthdate'),
                balance: 100
        )
    }
}
