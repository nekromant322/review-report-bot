localStorage.clear();
getMentoringPrice();
getRoastingPrice();

async function submit_new_client_cv_roasting() {
    let file = document.getElementById("pdf_input_form").files[0];
    let tgNameInput = document.getElementById("tg_name_input_form");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone_input_form");
    let phone = phoneInput.value;
    let cvPromocodeId = localStorage.getItem("cv_promocode_id");

    if (file === null || typeof file == 'undefined') {
        alert('Выберите pdf файл!');
        return;
    }

    if (tgNameInput.validity.valueMissing) {
        tgNameInput.setCustomValidity('Введите имя пользователя!')
        tgNameInput.reportValidity();
        return;
    }
    if (tgNameInput.validity.patternMismatch) {
        tgNameInput.setCustomValidity('Сомнительно, но окэй');
        tgNameInput.reportValidity();

        const toRemove = ["t.me", "https"];
        tgName = toRemove.reduce((acc, substr) => acc.replace(new RegExp(substr, 'g'), ''), tgName);
        tgName = tgName.replace(/[^\w]/g, '');
    }

    if (phoneInput.validity.valueMissing) {
        phoneInput.setCustomValidity('Введите телефон!');
        phoneInput.reportValidity();
        return;
    }
    if (phoneInput.validity.patternMismatch) {
        phoneInput.setCustomValidity("Чет не похоже на телефон.. \n Уж не ошибся ли ты часом?");
        phoneInput.reportValidity();
        return;
    }
    phone = phone.replace(/\D/g, '');
    phone = '7' + phone.substr(phone.length - 10);

    const form_data = new FormData();
    form_data.append("form_data", file);

    await fetch("./pricing/cv", {
        method: "POST",
        body: form_data,
        headers: {
            'TG-NAME': tgName,
            'PHONE': phone,
            'CV-PROMOCODE-ID': cvPromocodeId
        }
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            throw new Error('Что-то пошло не так :(');
        })
        .then(text => window.open(text).focus())
        .catch((error => {
            alert(error)
        }));
}


async function submit_new_client_mentoringSubscription() {
    let tgNameInput = document.getElementById("tg_name_input_form_mentoring");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone_input_form_mentoring");
    let phone = phoneInput.value;

    if (tgNameInput.validity.valueMissing) {
        tgNameInput.setCustomValidity('Введите имя пользователя!')
        tgNameInput.reportValidity();
        return;
    }
    if (tgNameInput.validity.patternMismatch) {
        const toRemove = ["t.me", "https"];
        tgName = toRemove.reduce((acc, substr) => acc.replace(new RegExp(substr, 'g'), ''), tgName);
        tgName = tgName.replace(/[^\w]/g, '');

        tgNameInput.setCustomValidity('Сомнительно, но окэй');
        tgNameInput.reportValidity();
    }

    if (phoneInput.validity.valueMissing) {
        phoneInput.setCustomValidity('Введите телефон!');
        phoneInput.reportValidity();
        return;
    }
    if (phoneInput.validity.patternMismatch) {
        phoneInput.setCustomValidity("Чет не похоже на телефон.. \n Уж не ошибся ли ты часом?");
        phoneInput.reportValidity();
        return;
    }
    phone = phone.replace(/\D/g, '');
    phone = '7' + phone.substr(phone.length - 10);

    let mentoring_data = {
        'TG-NAME': tgName,
        'PHONE': phone
    }
    await fetch("./pricing/mentoring", {
        method: "POST",
        body: JSON.stringify(mentoring_data),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            throw new Error('Что-то пошло не так :(');
        })
        .then(text => window.open(text).focus())
        .catch((error => {
            alert(error)
        }));
}


async function getMentoringPrice() {
    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("submit_button_mentoring").innerHTML += `К оплате ${mentoring_price}  р.`;
}

async function getRoastingPrice() {
    let response = await fetch("./pricing/roasting/price");
    let roasting_price = await response.text();
    document.getElementById("submit_button_roasting").innerHTML += `К оплате ${roasting_price} р.`;
}


async function roastingPromocodePricing() {
    let promocodeInput = document.getElementById("roasting_promocode_input").value;
    if (promocodeInput.length === 0) {
        alert('Что-то надо ввести');
        return;
    }

    let response = await fetch("./promocodes?text=" + promocodeInput);

    if (response.status == 404) {
        alert("Промокода с таким текстом не существует!");
        return;
    }
    let cv_promocode = await response.json();
    if (!cv_promocode.active || cv_promocode.maxUsesNumber <= cv_promocode.counterUsed) {
        alert("Этот промокод недоступен!\nПопробуйте другой...");
        return;
    }
    if (cv_promocode.serviceType != "RESUME") {
        alert('Промокод не соответствует желаемой услуге!');
        return;
    }

    localStorage.setItem("cv_promocode_id", cv_promocode.id);
    let discountPercent = cv_promocode.discountPercent;

    response = await fetch("./pricing/cv/price");
    let cv_price = await response.text();
    let discount_price = Math.round(cv_price * (1 - discountPercent / 100));
    if (discount_price == 0) {
        alert("Сумма к оплате - 0 р.\nТак не должно быть. Выберите другой промокод.\nИли без него");
        window.location.reload();
        return;
    }
    document.getElementById("submit_button_roasting").innerHTML = `К оплате <s>${cv_price}</s> ${discount_price} р.`;
    document.getElementById("roasting_promocode_input_block").innerHTML = ``;
}