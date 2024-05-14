getCVRoastingPrice();
getMentoringPrice();

async function submit_new_client() {
    let file = document.getElementById("pdf_input_form").files[0];
    let tgNameInput = document.getElementById("tg_name_input_form");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone_input_form");
    let phone = phoneInput.value;

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
            'PHONE': phone
        }
    });
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
    });
}

async function getCVRoastingPrice() {
    let response = await fetch("./pricing/cv/price");
    let cv_price = await response.text();
    document.getElementById("submit_button").innerHTML += `К оплате ${cv_price} р.`;
}

async function getMentoringPrice() {
    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("submit_button_mentoring").innerHTML += `К оплате ${mentoring_price} р.`;
}

