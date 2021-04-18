export class ErrorMessageMap {
    private static descriptionByCode = {
        "ERR_ACC_OR_PASS": "Указанные данные учетной записи не верны",
        "ERR_ACC_ILLEGAL_STATUS": "Аккаунт заблокирован",
        "ERR_REG_PASS": "Пароль не удовлетворяет критериям безопасности",
        "ERR_UPD_PASS": "Текущий пароль неверный",
        "ERR_UPD_STATUS": "Статус запрещен",
        "ERR_ACC_EMAIL": "Некорректный email",
        "ERR_ACC_QUICKNAME": "Нужно заполнить никнейм",
        "ERR_REG_EMAIL": "Введенный email уже занят",
        "ERR_WORKBOOK": "Рабочая тетрадь не найдена",
        "ERR_NOT_AUTHORIZED": "Требуется авторизация",
        "ERR_ACC_VOUCHER": "Ваучер не действителен",
        "ERR_FORBIDDEN": "Доступ запрещен",
        "ERR_ACC_NEED_CONFIRMATION": "Аккаунт не подтвержден. Для подтверждения аккаунта перейдите по ссылке в письме",
        "ERR_ACC": "Аккаунт не найден",
        "ERR_LINKEDIN": "Некорректная ссылка на linkedin",
        "ERR_HH": "Некорректная ссылка на hh",
        "ERR_GITHUB": "Некорректная ссылка на github",
        "ERR_BITBUCKET" : "Некорректная ссылка на bitbucket",
        "ERR_WEBSITE": "Некорректная ссылка на сайт",
        "ERR_MAX_UPLOAD_SIZE_EXCEEDED": "Ошибка загрузки файла. Размер файла не должен превышать 10 Мб",
        "ERR_WORKBOOK_EXPIRED": "Время на решение теста истекло",
        "ERR_ATTEMPTS_LEFT": "К сожалению, количество попыток исчерпано. Не расстраивайтесь и приходите на стенд T-Systems, у нас еще много интересного!"
    };

    static getDescription(err) {
        // In case of operation exception (custom exception)
        if (err.details) {
            let descr = ErrorMessageMap.descriptionByCode[err.details.textcode];
            return descr ? descr : 'Внутренняя ошибка сервера';
        }
        // Default spring exception or server unresolved
        if (err.status === 401) {
            return 'Необходима повторная авторизация'
        } else if (err.status === 403) {
            return 'Доступ запрещен'
        } else {
            return 'Не удалось подключиться к серверу';
        }
    }
}