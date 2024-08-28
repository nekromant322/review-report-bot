const pdfInput = document.getElementById('pdf'),
    clearInput = document.getElementById('clear-input'),
    pdfLabel = document.getElementById('pdf-label'),
    burger = document.getElementById('burger'),
    headerLinks = document.getElementById('header-links'),
    linksClose = document.getElementById('links-close'),
    resumeButton = document.getElementById('resume-button'),
    resume = document.getElementById('resume'),
    mentoringButton = document.getElementById('mentoring-button'),
    mentoring = document.getElementById('mentoring'),
    callButton = document.getElementById('call-button'),
    call = document.getElementById('call'),
    popup = document.getElementById('popup'),
    popupTitle = popup.querySelector('.popup__content__header__title'),
    showUpgradeButton = document.getElementById('show-upgrade-button'),
    showMentoringButton = document.getElementById('show-mentoring-button'),
    showCallButton = document.getElementById('show-call-button'),
    popupClose = document.getElementById('popup-close'),
    form = document.getElementById('form'),
    pdfField = form.querySelector('.field_pdf'),
    contractLink = document.querySelector('.field_checkbox a');

const CV_TITLE = 'Апгрейд резюме',
    MENTORING_TITLE = 'Менторинг',
    CALL_TITLE = 'Созвон';

clearInput.addEventListener('click', () => {
    try {
        pdfInput.value = null;
    } catch (ex) {}
    if (pdfInput.value) {
        pdfInput.parentNode.replaceChild(pdfInput.cloneNode(true), pdfInput);
    }
    pdfLabel.innerText = 'выберите файл';
});

pdfInput.addEventListener('change', () => {
    document.getElementById('pdf-label').innerText = pdfInput.files[0].name;
});

burger.addEventListener('click', () => {
    headerLinks.classList.toggle('header__links_active');
});

linksClose.addEventListener('click', () => {
    headerLinks.classList.remove('header__links_active');
});

resumeButton.addEventListener('click', () => {
    resume.scrollIntoView({behavior: 'smooth'});
});

mentoringButton.addEventListener('click', () => {
    mentoring.scrollIntoView({behavior: 'smooth'});
});

callButton.addEventListener('click', () => {
    call.scrollIntoView({behavior: 'smooth'});
});

showUpgradeButton.addEventListener('click', async () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = CV_TITLE;
    pdfField.style.display = 'flex';
    pdfInput.required = true;
    contractLink.href = './others/resume_review_pferta.pdf';

    let response = await fetch("./pricing/roasting/price");
    let roasting_price = await response.text();
    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${roasting_price}  р.`;
});

showCallButton.addEventListener('click', async () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = CALL_TITLE;
    pdfField.style.display = 'none';
    pdfInput.required = false;
    contractLink.href = './others/personal_call_pferta.pdf';

    let response = await fetch("./pricing/call/price");
    let call_price = await response.text();
    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${call_price}  р.`;
});

showMentoringButton.addEventListener('click', async () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = MENTORING_TITLE;
    pdfField.style.display = 'none';
    pdfInput.required = false;
    contractLink.href = './others/mentoring_subscription_pferta.pdf';

    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${mentoring_price}  р.`;
});

popupClose.addEventListener('click', () => {
    popup.classList.add('popup_hidden');
    form.reset();
    clearInput.click();
    document.getElementById('promo').value = '';
    document.getElementById('promo').disabled = false;
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (popupTitle.innerText === CV_TITLE) {
        submit_new_client_cv_roasting();
    } else if (popupTitle.innerText === MENTORING_TITLE) {
        submit_new_client_mentoringSubscription();
    } else if (popupTitle.innerText === CALL_TITLE) {
        submit_new_client_personalCall();
    }
    popup.classList.add('popup_hidden');
    form.reset();
    popupClose.click();
});

async function submit_new_client_cv_roasting() {
    if (popupTitle.innerText !== CV_TITLE) {
        return;
    }
    let cvPromocodeId = localStorage.getItem("cv_promocode_id");
    localStorage.removeItem("cv_promocode_id");
    let file = document.getElementById("pdf").files[0];
    let tgNameInput = document.getElementById("tg");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone");
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

    const headers = {
        'TG-NAME': tgName,
        'PHONE': phone
    };

    if (cvPromocodeId!== null) {
        headers['CV-PROMOCODE-ID'] = cvPromocodeId;
    } else {
        headers['CV-PROMOCODE-ID'] = null;
    }

    await fetch("./pricing/cv", {
        method: "POST",
        body: form_data,
        headers: headers
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
    if (popupTitle.innerText !== MENTORING_TITLE) {
        return;
    }
    let mentoringPromocodeId = localStorage.getItem("mentoring_promocode_id");
    localStorage.removeItem("mentoring_promocode_id");
    let tgNameInput = document.getElementById("tg");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone");
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
    if (mentoringPromocodeId!== null) {
        mentoring_data['MENTORING-PROMOCODE-ID'] = mentoringPromocodeId;
    } else {
        mentoring_data['MENTORING-PROMOCODE-ID'] = null;
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
            } else {
                throw new Error('Что-то пошло не так :(');
            }
        })
        .then(text => window.open(text).focus())
        .catch((error => {
            alert(error)
        }));
}

async function submit_new_client_personalCall() {
    if (popupTitle.innerText !== CALL_TITLE) {
        return;
    }
    let callPromocodeId = localStorage.getItem("call_promocode_id");
    localStorage.removeItem("call_promocode_id");
    let tgNameInput = document.getElementById("tg");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone");
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

    let call_data = {
        'TG-NAME': tgName,
        'PHONE': phone
    }
    if (callPromocodeId !== null) {
        call_data['CALL-PROMOCODE-ID'] = callPromocodeId;
    } else {
        call_data['CALL-PROMOCODE-ID'] = null;
    }

    await fetch("./pricing/call", {
        method: "POST",
        body: JSON.stringify(call_data),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Что-то пошло не так :(');
            }
        })
        .then(text => window.open(text).focus())
        .catch((error => {
            alert(error)
        }));
}

async function getMentoringPrice() {
    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("mentoring_price").innerHTML += `${mentoring_price} руб/мес`;
}

async function getRoastingPrice() {
    let response = await fetch("./pricing/roasting/price");
    let roasting_price = await response.text();
    document.getElementById("roasting_price").innerHTML += `${roasting_price} руб`;
}

async function getCallPrice() {
    let response = await fetch("./pricing/call/price");
    let call_price = await response.text();
    document.getElementById("call_price").innerHTML += `${call_price} руб`;
}

async function roastingPromocodePricing() {
    let promocodeInput = document.getElementById("promo").value;
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

    if (!await checkPromocodeCompatibility("RESUME", cv_promocode.id)) {
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

    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${discount_price}  р.`;
    document.getElementById("promo").disabled = true;
}

async function mentoringPromocodePricing() {
    let promocodeInput = document.getElementById("promo").value;
    if (promocodeInput.length === 0) {
        alert('Что-то надо ввести');
        return;
    }

    let response = await fetch("./promocodes?text=" + promocodeInput);

    if (response.status == 404) {
        alert("Промокода с таким текстом не существует!");
        return;
    }
    let mentoring_promocode = await response.json();
    if (!mentoring_promocode.active || mentoring_promocode.maxUsesNumber <= mentoring_promocode.counterUsed) {
        alert("Этот промокод недоступен!\nПопробуйте другой...");
        return;
    }

    if (!await checkPromocodeCompatibility("MENTORING", mentoring_promocode.id)) {
        alert('Промокод не соответствует желаемой услуге!');
        return;
    }

    localStorage.setItem("mentoring_promocode_id", mentoring_promocode.id);
    let discountPercent = mentoring_promocode.discountPercent;

    response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    let discount_price = Math.round(mentoring_price * (1 - discountPercent / 100));
    if (discount_price == 0) {
        alert("Сумма к оплате - 0 р.\nТак не должно быть. Выберите другой промокод.\nИли без него");
        window.location.reload();
        return;
    }

    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${discount_price} р.`;
    document.getElementById("promo").disabled = true;
}

async function callPromocodePricing() {
    let promocodeInput = document.getElementById("promo").value;
    if (promocodeInput.length === 0) {
        alert('Что-то надо ввести');
        return;
    }

    let response = await fetch("./promocodes?text=" + promocodeInput);

    if (response.status == 404) {
        alert("Промокода с таким текстом не существует!");
        return;
    }
    let call_promocode = await response.json();
    if (!call_promocode.active || call_promocode.maxUsesNumber <= call_promocode.counterUsed) {
        alert("Этот промокод недоступен!\nПопробуйте другой...");
        return;
    }

    if (!await checkPromocodeCompatibility("CALL", call_promocode.id)) {
        alert('Промокод не соответствует желаемой услуге!');
        return;
    }

    localStorage.setItem("call_promocode_id", call_promocode.id);
    let discountPercent = call_promocode.discountPercent;

    response = await fetch("./pricing/call/price");
    let call_price = await response.text();
    let discount_price = Math.round(call_price * (1 - discountPercent / 100));
    if (discount_price == 0) {
        alert("Сумма к оплате - 0 р.\nТак не должно быть. Выберите другой промокод.\nИли без него");
        window.location.reload();
        return;
    }

    document.getElementById("pay-button").innerHTML = `К ОПЛАТЕ ${discount_price} р.`;
    document.getElementById("promo").disabled = true;
}

async function checkPromocodeCompatibility(serviceType, promocodeId) {
    return fetch("./servicetypes/checkpromocode?promocode_id=" + promocodeId + "&service_type=" + serviceType)
        .then(response => response.text())
        .then(text => JSON.parse(text));
}

getMentoringPrice();
getRoastingPrice();
getCallPrice();

document.getElementById("promo").addEventListener('input', () => {
    if (popupTitle.innerText === CV_TITLE) {
        roastingPromocodePricing();
    } else if (popupTitle.innerText === MENTORING_TITLE) {
        mentoringPromocodePricing();
    } else if (popupTitle.innerText === CALL_TITLE) {
        callPromocodePricing();
    }
});
