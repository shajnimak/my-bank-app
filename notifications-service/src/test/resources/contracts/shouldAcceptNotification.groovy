import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Сервис уведомлений принимает уведомление для пользователя"
    request {
        method POST()
        url "/api/notifications"
        headers {
            contentType(applicationJson())
        }
        body(
                login: $(consumer(regex(nonBlank())), producer("ivanov")),
                message: $(consumer(regex(nonBlank())), producer("Положено 100 руб"))
        )
    }
    response {
        status CREATED()
        headers {
            contentType(applicationJson())
        }
        body(
                id: 1,
                login: fromRequest().body('$.login'),
                message: fromRequest().body('$.message'),
                createdAt: "2026-11-09T10:15:30Z"
        )
    }
}
